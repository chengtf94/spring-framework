/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans;

import java.io.Serializable;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * setter注入的属性
 * Object to hold information and value for an individual bean property.
 * Using an object here, rather than just storing all properties in
 * a map keyed by property name, allows for more flexibility, and the
 * ability to handle indexed properties etc in an optimized way.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 13 May 2001
 * @see PropertyValues
 * @see BeanWrapper
 */
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

	/**
	 * 属性名
	 */
	private final String name;

	/**
	 * 属性值
	 */
	@Nullable
	private final Object value;

	private boolean optional = false;

	private boolean converted = false;

	@Nullable
	private Object convertedValue;

	/** Package-visible field that indicates whether conversion is necessary. */
	@Nullable
	volatile Boolean conversionNecessary;

	/** Package-visible field for caching the resolved property path tokens. */
	@Nullable
	transient volatile Object resolvedTokens;

	/**
	 * 构造方法
	 */
	public PropertyValue(String name, @Nullable Object value) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.value = value;
	}

	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = original.getValue();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original.getSource());
		copyAttributesFrom(original);
	}

	public PropertyValue(PropertyValue original, @Nullable Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original);
		copyAttributesFrom(original);
	}

	public String getName() {
		return this.name;
	}

	@Nullable
	public Object getValue() {
		return this.value;
	}

	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		Object source = getSource();
		while (source instanceof PropertyValue && source != original) {
			original = (PropertyValue) source;
			source = original.getSource();
		}
		return original;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isOptional() {
		return this.optional;
	}

	public synchronized boolean isConverted() {
		return this.converted;
	}

	public synchronized void setConvertedValue(@Nullable Object value) {
		this.converted = true;
		this.convertedValue = value;
	}

	@Nullable
	public synchronized Object getConvertedValue() {
		return this.convertedValue;
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherPv.value) &&
				ObjectUtils.nullSafeEquals(getSource(), otherPv.getSource()));
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
	}

	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}

}
