/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * AutowiredAnnotationBeanPostProcessor：@Autowired、@Value注解 Bean后置处理器
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @since 2.5
 * @see Autowired
 * @see Value
 */
public class AutowiredAnnotationBeanPostProcessor implements
		SmartInstantiationAwareBeanPostProcessor,
		MergedBeanDefinitionPostProcessor,
		PriorityOrdered,
		BeanFactoryAware {
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 'autowired'注解类型集合
	 */
	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

	private String requiredParameterName = "required";

	private boolean requiredParameterValue = true;

	/**
	 * 顺序：默认为Ordered.LOWEST_PRECEDENCE - 2，值越小优先级越大
	 */
	private int order = Ordered.LOWEST_PRECEDENCE - 2;

	/**
	 * Bean工厂
	 */
	@Nullable
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * 元数据读取器工厂
	 */
	@Nullable
	private MetadataReaderFactory metadataReaderFactory;

	private final Set<String> lookupMethodsChecked = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

	private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);

	private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

	/**
	 * 构造方法
	 */
	@SuppressWarnings("unchecked")
	public AutowiredAnnotationBeanPostProcessor() {
		this.autowiredAnnotationTypes.add(Autowired.class);
		this.autowiredAnnotationTypes.add(Value.class);
		try {
			this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
					ClassUtils.forName("javax.inject.Inject", AutowiredAnnotationBeanPostProcessor.class.getClassLoader()));
			logger.trace("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
	}

	@Override
	@Nullable
	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, final String beanName)
			throws BeanCreationException {

		// Let's check for lookup methods here...
		if (!this.lookupMethodsChecked.contains(beanName)) {
			if (AnnotationUtils.isCandidateClass(beanClass, Lookup.class)) {
				try {
					Class<?> targetClass = beanClass;
					do {
						ReflectionUtils.doWithLocalMethods(targetClass, method -> {
							Lookup lookup = method.getAnnotation(Lookup.class);
							if (lookup != null) {
								Assert.state(this.beanFactory != null, "No BeanFactory available");
								LookupOverride override = new LookupOverride(method, lookup.value());
								try {
									RootBeanDefinition mbd = (RootBeanDefinition)
											this.beanFactory.getMergedBeanDefinition(beanName);
									mbd.getMethodOverrides().addOverride(override);
								}
								catch (NoSuchBeanDefinitionException ex) {
									throw new BeanCreationException(beanName,
											"Cannot apply @Lookup to beans without corresponding bean definition");
								}
							}
						});
						targetClass = targetClass.getSuperclass();
					}
					while (targetClass != null && targetClass != Object.class);

				}
				catch (IllegalStateException ex) {
					throw new BeanCreationException(beanName, "Lookup method resolution failed", ex);
				}
			}
			this.lookupMethodsChecked.add(beanName);
		}

		// Quick check on the concurrent map first, with minimal locking.
		Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
		if (candidateConstructors == null) {
			// Fully synchronized resolution now...
			synchronized (this.candidateConstructorsCache) {
				candidateConstructors = this.candidateConstructorsCache.get(beanClass);
				if (candidateConstructors == null) {
					Constructor<?>[] rawCandidates;
					try {
						rawCandidates = beanClass.getDeclaredConstructors();
					}
					catch (Throwable ex) {
						throw new BeanCreationException(beanName,
								"Resolution of declared constructors on bean Class [" + beanClass.getName() +
										"] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
					}
					List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
					Constructor<?> requiredConstructor = null;
					Constructor<?> defaultConstructor = null;
					Constructor<?> primaryConstructor = BeanUtils.findPrimaryConstructor(beanClass);
					int nonSyntheticConstructors = 0;
					for (Constructor<?> candidate : rawCandidates) {
						if (!candidate.isSynthetic()) {
							nonSyntheticConstructors++;
						}
						else if (primaryConstructor != null) {
							continue;
						}
						MergedAnnotation<?> ann = findAutowiredAnnotation(candidate);
						if (ann == null) {
							Class<?> userClass = ClassUtils.getUserClass(beanClass);
							if (userClass != beanClass) {
								try {
									Constructor<?> superCtor =
											userClass.getDeclaredConstructor(candidate.getParameterTypes());
									ann = findAutowiredAnnotation(superCtor);
								}
								catch (NoSuchMethodException ex) {
									// Simply proceed, no equivalent superclass constructor found...
								}
							}
						}
						if (ann != null) {
							if (requiredConstructor != null) {
								throw new BeanCreationException(beanName,
										"Invalid autowire-marked constructor: " + candidate +
												". Found constructor with 'required' Autowired annotation already: " +
												requiredConstructor);
							}
							boolean required = determineRequiredStatus(ann);
							if (required) {
								if (!candidates.isEmpty()) {
									throw new BeanCreationException(beanName,
											"Invalid autowire-marked constructors: " + candidates +
													". Found constructor with 'required' Autowired annotation: " +
													candidate);
								}
								requiredConstructor = candidate;
							}
							candidates.add(candidate);
						}
						else if (candidate.getParameterCount() == 0) {
							defaultConstructor = candidate;
						}
					}
					if (!candidates.isEmpty()) {
						// Add default constructor to list of optional constructors, as fallback.
						if (requiredConstructor == null) {
							if (defaultConstructor != null) {
								candidates.add(defaultConstructor);
							}
							else if (candidates.size() == 1 && logger.isInfoEnabled()) {
								logger.info("Inconsistent constructor declaration on bean with name '" + beanName +
										"': single autowire-marked constructor flagged as optional - " +
										"this constructor is effectively required since there is no " +
										"default constructor to fall back to: " + candidates.get(0));
							}
						}
						candidateConstructors = candidates.toArray(new Constructor<?>[0]);
					}
					else if (rawCandidates.length == 1 && rawCandidates[0].getParameterCount() > 0) {
						candidateConstructors = new Constructor<?>[] {rawCandidates[0]};
					}
					else if (nonSyntheticConstructors == 2 && primaryConstructor != null &&
							defaultConstructor != null && !primaryConstructor.equals(defaultConstructor)) {
						candidateConstructors = new Constructor<?>[] {primaryConstructor, defaultConstructor};
					}
					else if (nonSyntheticConstructors == 1 && primaryConstructor != null) {
						candidateConstructors = new Constructor<?>[] {primaryConstructor};
					}
					else {
						candidateConstructors = new Constructor<?>[0];
					}
					this.candidateConstructorsCache.put(beanClass, candidateConstructors);
				}
			}
		}
		return (candidateConstructors.length > 0 ? candidateConstructors : null);
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
		InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		}
		catch (BeanCreationException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
		}
		return pvs;
	}

	@Deprecated
	@Override
	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {

		return postProcessProperties(pvs, bean, beanName);
	}


	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		// 查找自动注入元数据
		InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
		metadata.checkConfigMembers(beanDefinition);
	}

	@Override
	public void resetBeanDefinition(String beanName) {
		this.lookupMethodsChecked.remove(beanName);
		this.injectionMetadataCache.remove(beanName);
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
		this.metadataReaderFactory = new SimpleMetadataReaderFactory(this.beanFactory.getBeanClassLoader());
	}











	/**
	 * Set the 'autowired' annotation type, to be used on constructors, fields, setter methods, and arbitrary config methods.
	 */
	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.add(autowiredAnnotationType);
	}

	/**
	 * Set the 'autowired' annotation types, to be used on constructors, fields, setter methods, and arbitrary config methods.
	 */
	public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
		Assert.notEmpty(autowiredAnnotationTypes, "'autowiredAnnotationTypes' must not be empty");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
	}

	public void setRequiredParameterName(String requiredParameterName) {
		this.requiredParameterName = requiredParameterName;
	}

	public void setRequiredParameterValue(boolean requiredParameterValue) {
		this.requiredParameterValue = requiredParameterValue;
	}

	public void setOrder(int order) {
		this.order = order;
	}











	/**
	 * 'Native' processing method for direct calls with an arbitrary target instance,
	 * resolving all of its fields and methods which are annotated with one of the
	 * configured 'autowired' annotation types.
	 * @param bean the target instance to process
	 * @throws BeanCreationException if autowiring failed
	 * @see #setAutowiredAnnotationTypes(Set)
	 */
	public void processInjection(Object bean) throws BeanCreationException {
		Class<?> clazz = bean.getClass();
		InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz, null);
		try {
			metadata.inject(bean, null, null);
		}
		catch (BeanCreationException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					"Injection of autowired dependencies failed for class [" + clazz + "]", ex);
		}
	}

	private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
		// Fall back to class name as cache key, for backwards compatibility with custom callers.
		String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					metadata = buildAutowiringMetadata(clazz);
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
		if (!AnnotationUtils.isCandidateClass(clazz, this.autowiredAnnotationTypes)) {
			return InjectionMetadata.EMPTY;
		}

		final List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = clazz;

		do {
			final List<InjectionMetadata.InjectedElement> fieldElements = new ArrayList<>();
			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				MergedAnnotation<?> ann = findAutowiredAnnotation(field);
				if (ann != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation is not supported on static fields: " + field);
						}
						return;
					}
					boolean required = determineRequiredStatus(ann);
					fieldElements.add(new AutowiredFieldElement(field, required));
				}
			});

			final List<InjectionMetadata.InjectedElement> methodElements = new ArrayList<>();
			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
				if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
					return;
				}
				MergedAnnotation<?> ann = findAutowiredAnnotation(bridgedMethod);
				if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
					if (Modifier.isStatic(method.getModifiers())) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation is not supported on static methods: " + method);
						}
						return;
					}
					if (method.getParameterCount() == 0) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation should only be used on methods with parameters: " +
									method);
						}
					}
					boolean required = determineRequiredStatus(ann);
					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
					methodElements.add(new AutowiredMethodElement(method, required, pd));
				}
			});

			elements.addAll(0, sortMethodElements(methodElements, targetClass));
			elements.addAll(0, fieldElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		return InjectionMetadata.forElements(elements, clazz);
	}

	@Nullable
	private MergedAnnotation<?> findAutowiredAnnotation(AccessibleObject ao) {
		MergedAnnotations annotations = MergedAnnotations.from(ao);
		for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
			MergedAnnotation<?> annotation = annotations.get(type);
			if (annotation.isPresent()) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Determine if the annotated field or method requires its dependency.
	 * <p>A 'required' dependency means that autowiring should fail when no beans
	 * are found. Otherwise, the autowiring process will simply bypass the field
	 * or method when no beans are found.
	 * @param ann the Autowired annotation
	 * @return whether the annotation indicates that a dependency is required
	 */
	@SuppressWarnings("deprecation")
	protected boolean determineRequiredStatus(MergedAnnotation<?> ann) {
		return determineRequiredStatus(ann.<AnnotationAttributes> asMap(
			mergedAnnotation -> new AnnotationAttributes(mergedAnnotation.getType())));
	}

	/**
	 * Determine if the annotated field or method requires its dependency.
	 * <p>A 'required' dependency means that autowiring should fail when no beans
	 * are found. Otherwise, the autowiring process will simply bypass the field
	 * or method when no beans are found.
	 * @param ann the Autowired annotation
	 * @return whether the annotation indicates that a dependency is required
	 * @deprecated since 5.2, in favor of {@link #determineRequiredStatus(MergedAnnotation)}
	 */
	@Deprecated
	protected boolean determineRequiredStatus(AnnotationAttributes ann) {
		return (!ann.containsKey(this.requiredParameterName) ||
				this.requiredParameterValue == ann.getBoolean(this.requiredParameterName));
	}

	/**
	 * Obtain all beans of the given type as autowire candidates.
	 * @param type the type of the bean
	 * @return the target beans, or an empty Collection if no bean of this type is found
	 * @throws BeansException if bean retrieval failed
	 * @deprecated since 5.3.24 since it is unused in the meantime
	 */
	@Deprecated
	protected <T> Map<String, T> findAutowireCandidates(Class<T> type) throws BeansException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("No BeanFactory configured - " +
					"override the getBeanOfType method or specify the 'beanFactory' property");
		}
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
	}

	/**
	 * Sort the method elements via ASM for deterministic declaration order if possible.
	 */
	private List<InjectionMetadata.InjectedElement> sortMethodElements(
			List<InjectionMetadata.InjectedElement> methodElements, Class<?> targetClass) {

		if (this.metadataReaderFactory != null && methodElements.size() > 1) {
			// Try reading the class file via ASM for deterministic declaration order...
			// Unfortunately, the JVM's standard reflection returns methods in arbitrary
			// order, even between different runs of the same application on the same JVM.
			try {
				AnnotationMetadata asm =
						this.metadataReaderFactory.getMetadataReader(targetClass.getName()).getAnnotationMetadata();
				Set<MethodMetadata> asmMethods = asm.getAnnotatedMethods(Autowired.class.getName());
				if (asmMethods.size() >= methodElements.size()) {
					List<InjectionMetadata.InjectedElement> candidateMethods = new ArrayList<>(methodElements);
					List<InjectionMetadata.InjectedElement> selectedMethods = new ArrayList<>(asmMethods.size());
					for (MethodMetadata asmMethod : asmMethods) {
						for (Iterator<InjectionMetadata.InjectedElement> it = candidateMethods.iterator(); it.hasNext();) {
							InjectionMetadata.InjectedElement element = it.next();
							if (element.getMember().getName().equals(asmMethod.getMethodName())) {
								selectedMethods.add(element);
								it.remove();
								break;
							}
						}
					}
					if (selectedMethods.size() == methodElements.size()) {
						// All reflection-detected methods found in ASM method set -> proceed
						return selectedMethods;
					}
				}
			}
			catch (IOException ex) {
				logger.debug("Failed to read class file via ASM for determining @Autowired method order", ex);
				// No worries, let's continue with the reflection metadata we started with...
			}
		}
		return methodElements;
	}

	/**
	 * Register the specified bean as dependent on the autowired beans.
	 */
	private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames) {
		if (beanName != null) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory != null && this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Autowiring by type from bean name '" + beanName +
							"' to bean named '" + autowiredBeanName + "'");
				}
			}
		}
	}

	/**
	 * Resolve the specified cached method argument or field value.
	 */
	@Nullable
	private Object resolveCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
		if (cachedArgument instanceof DependencyDescriptor) {
			DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
			Assert.state(this.beanFactory != null, "No BeanFactory available");
			return this.beanFactory.resolveDependency(descriptor, beanName, null, null);
		}
		else {
			return cachedArgument;
		}
	}

	/**
	 * Class representing injection information about an annotated field.
	 */
	private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private volatile boolean cached;

		@Nullable
		private volatile Object cachedFieldValue;

		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			Object value;
			if (this.cached) {
				try {
					value = resolveCachedArgument(beanName, this.cachedFieldValue);
				}
				catch (BeansException ex) {
					// Unexpected target bean mismatch for cached argument -> re-resolve
					this.cached = false;
					logger.debug("Failed to resolve cached argument", ex);
					value = resolveFieldValue(field, bean, beanName);
				}
			}
			else {
				value = resolveFieldValue(field, bean, beanName);
			}
			if (value != null) {
				ReflectionUtils.makeAccessible(field);
				field.set(bean, value);
			}
		}

		@Nullable
		private Object resolveFieldValue(Field field, Object bean, @Nullable String beanName) {
			DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
			desc.setContainingClass(bean.getClass());
			Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
			Assert.state(beanFactory != null, "No BeanFactory available");
			TypeConverter typeConverter = beanFactory.getTypeConverter();
			Object value;
			try {
				value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
			}
			synchronized (this) {
				if (!this.cached) {
					if (value != null || this.required) {
						Object cachedFieldValue = desc;
						registerDependentBeans(beanName, autowiredBeanNames);
						if (value != null && autowiredBeanNames.size() == 1) {
							String autowiredBeanName = autowiredBeanNames.iterator().next();
							if (beanFactory.containsBean(autowiredBeanName) &&
									beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
								cachedFieldValue = new ShortcutDependencyDescriptor(desc, autowiredBeanName);
							}
						}
						this.cachedFieldValue = cachedFieldValue;
						this.cached = true;
					}
					else {
						this.cachedFieldValue = null;
						// cached flag remains false
					}
				}
			}
			return value;
		}
	}


	/**
	 * Class representing injection information about an annotated method.
	 */
	private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private volatile boolean cached;

		@Nullable
		private volatile Object[] cachedMethodArguments;

		public AutowiredMethodElement(Method method, boolean required, @Nullable PropertyDescriptor pd) {
			super(method, pd);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			if (checkPropertySkipping(pvs)) {
				return;
			}
			Method method = (Method) this.member;
			Object[] arguments;
			if (this.cached) {
				try {
					arguments = resolveCachedArguments(beanName, this.cachedMethodArguments);
				}
				catch (BeansException ex) {
					// Unexpected target bean mismatch for cached argument -> re-resolve
					this.cached = false;
					logger.debug("Failed to resolve cached argument", ex);
					arguments = resolveMethodArguments(method, bean, beanName);
				}
			}
			else {
				arguments = resolveMethodArguments(method, bean, beanName);
			}
			if (arguments != null) {
				try {
					ReflectionUtils.makeAccessible(method);
					method.invoke(bean, arguments);
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}

		@Nullable
		private Object[] resolveCachedArguments(@Nullable String beanName, @Nullable Object[] cachedMethodArguments) {
			if (cachedMethodArguments == null) {
				return null;
			}
			Object[] arguments = new Object[cachedMethodArguments.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = resolveCachedArgument(beanName, cachedMethodArguments[i]);
			}
			return arguments;
		}

		@Nullable
		private Object[] resolveMethodArguments(Method method, Object bean, @Nullable String beanName) {
			int argumentCount = method.getParameterCount();
			Object[] arguments = new Object[argumentCount];
			DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
			Set<String> autowiredBeanNames = new LinkedHashSet<>(argumentCount * 2);
			Assert.state(beanFactory != null, "No BeanFactory available");
			TypeConverter typeConverter = beanFactory.getTypeConverter();
			for (int i = 0; i < arguments.length; i++) {
				MethodParameter methodParam = new MethodParameter(method, i);
				DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, this.required);
				currDesc.setContainingClass(bean.getClass());
				descriptors[i] = currDesc;
				try {
					Object arg = beanFactory.resolveDependency(currDesc, beanName, autowiredBeanNames, typeConverter);
					if (arg == null && !this.required) {
						arguments = null;
						break;
					}
					arguments[i] = arg;
				}
				catch (BeansException ex) {
					throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex);
				}
			}
			synchronized (this) {
				if (!this.cached) {
					if (arguments != null) {
						DependencyDescriptor[] cachedMethodArguments = Arrays.copyOf(descriptors, argumentCount);
						registerDependentBeans(beanName, autowiredBeanNames);
						if (autowiredBeanNames.size() == argumentCount) {
							Iterator<String> it = autowiredBeanNames.iterator();
							Class<?>[] paramTypes = method.getParameterTypes();
							for (int i = 0; i < paramTypes.length; i++) {
								String autowiredBeanName = it.next();
								if (arguments[i] != null && beanFactory.containsBean(autowiredBeanName) &&
										beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
									cachedMethodArguments[i] = new ShortcutDependencyDescriptor(
											descriptors[i], autowiredBeanName);
								}
							}
						}
						this.cachedMethodArguments = cachedMethodArguments;
						this.cached = true;
					}
					else {
						this.cachedMethodArguments = null;
						// cached flag remains false
					}
				}
			}
			return arguments;
		}
	}


	/**
	 * DependencyDescriptor variant with a pre-resolved target bean name.
	 */
	@SuppressWarnings("serial")
	private static class ShortcutDependencyDescriptor extends DependencyDescriptor {

		private final String shortcut;

		public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut) {
			super(original);
			this.shortcut = shortcut;
		}

		@Override
		public Object resolveShortcut(BeanFactory beanFactory) {
			return beanFactory.getBean(this.shortcut, getDependencyType());
		}
	}

}
