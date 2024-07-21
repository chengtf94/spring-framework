/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.beans.factory;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * ObjectProvider：用于Bean延迟依赖查找
 * A variant of {@link ObjectFactory} designed specifically for injection points,
 * allowing for programmatic optionality and lenient not-unique handling.
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @param <T> the object type
 * @see BeanFactory#getBeanProvider
 * @see org.springframework.beans.factory.annotation.Autowired
 */
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

	/**
	 * Return an instance (possibly shared or independent) of the object managed by this factory.
	 */
	T getObject(Object... args) throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object managed by this factory.
	 */
	@Nullable
	T getIfAvailable() throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object managed by this factory.
	 */
	default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfAvailable();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * Consume an instance (possibly shared or independent) of the object managed by this factory, if available.
	 */
	default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfAvailable();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * Return an instance (possibly shared or independent) of the object managed by this factory.
	 */
	@Nullable
	T getIfUnique() throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object managed by this factory.
	 */
	default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfUnique();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * Consume an instance (possibly shared or independent) of the object managed by this factory, if unique.
	 */
	default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfUnique();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * Return an {@link Iterator} over all matching object instances,
	 */
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * Return a sequential {@link Stream} over all matching object instances, without specific ordering guarantees (but typically in registration order).
	 */
	default Stream<T> stream() {
		throw new UnsupportedOperationException("Multi element access not supported");
	}

	/**
	 * Return a sequential {@link Stream} over all matching object instances,
	 * pre-ordered according to the factory's common order comparator.
	 */
	default Stream<T> orderedStream() {
		throw new UnsupportedOperationException("Ordered element access not supported");
	}

}
