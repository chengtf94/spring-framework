/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * ChildBeanDefinition：子Bean定义，主要用于继承场景，当前Bean定义会默认继承了parentName对应Bean'定义
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see GenericBeanDefinition
 * @see RootBeanDefinition
 */
@SuppressWarnings("serial")
public class ChildBeanDefinition extends AbstractBeanDefinition {

	/**
	 * 父Bean名称
	 */
	@Nullable
	private String parentName;

	/**
	 * 构造方法
	 */
	public ChildBeanDefinition(String parentName) {
		super();
		this.parentName = parentName;
	}

	public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
		super(null, pvs);
		this.parentName = parentName;
	}

	public ChildBeanDefinition(
			String parentName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		this.parentName = parentName;
	}

	public ChildBeanDefinition(
			String parentName, Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClass(beanClass);
	}

	public ChildBeanDefinition(
			String parentName, String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClassName(beanClassName);
	}

	public ChildBeanDefinition(ChildBeanDefinition original) {
		super(original);
	}

	@Override
	public void setParentName(@Nullable String parentName) {
		this.parentName = parentName;
	}

	@Override
	@Nullable
	public String getParentName() {
		return this.parentName;
	}

	@Override
	public void validate() throws BeanDefinitionValidationException {
		super.validate();
		if (this.parentName == null) {
			throw new BeanDefinitionValidationException("'parentName' must be set in ChildBeanDefinition");
		}
	}

	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new ChildBeanDefinition(this);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ChildBeanDefinition)) {
			return false;
		}
		ChildBeanDefinition that = (ChildBeanDefinition) other;
		return (ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.parentName) * 29 + super.hashCode();
	}

	@Override
	public String toString() {
		return "Child bean with parent '" + this.parentName + "': " + super.toString();
	}

}
