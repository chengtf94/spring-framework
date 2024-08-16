/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.transaction.interceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * DefaultTransactionAttribute：默认事务属性
 * Spring's common transaction attribute implementation. Rolls back on runtime, but not checked, exceptions by default.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Paluch
 * @since 16.03.2003
 */
@SuppressWarnings("serial")
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

	@Nullable
	private String descriptor;

	@Nullable
	private String timeoutString;

	@Nullable
	private String qualifier;

	private Collection<String> labels = Collections.emptyList();

	/**
	 * 构造方法
	 */
	public DefaultTransactionAttribute() {
		super();
	}

	public DefaultTransactionAttribute(TransactionAttribute other) {
		super(other);
	}

	public DefaultTransactionAttribute(int propagationBehavior) {
		super(propagationBehavior);
	}

	public void setDescriptor(@Nullable String descriptor) {
		this.descriptor = descriptor;
	}

	@Nullable
	public String getDescriptor() {
		return this.descriptor;
	}

	public void setTimeoutString(@Nullable String timeoutString) {
		this.timeoutString = timeoutString;
	}

	@Nullable
	public String getTimeoutString() {
		return this.timeoutString;
	}

	public void setQualifier(@Nullable String qualifier) {
		this.qualifier = qualifier;
	}

	@Override
	@Nullable
	public String getQualifier() {
		return this.qualifier;
	}

	public void setLabels(Collection<String> labels) {
		this.labels = labels;
	}

	@Override
	public Collection<String> getLabels() {
		return this.labels;
	}

	@Override
	public boolean rollbackOn(Throwable ex) {
		return (ex instanceof RuntimeException || ex instanceof Error);
	}

	/**
	 * Resolve attribute values that are defined as resolvable Strings:
	 * {@link #setTimeoutString}, {@link #setQualifier}, {@link #setLabels}.
	 * This is typically used for resolving "${...}" placeholders.
	 * @param resolver the embedded value resolver to apply, if any
	 * @since 5.3
	 */
	public void resolveAttributeStrings(@Nullable StringValueResolver resolver) {
		String timeoutString = this.timeoutString;
		if (StringUtils.hasText(timeoutString)) {
			if (resolver != null) {
				timeoutString = resolver.resolveStringValue(timeoutString);
			}
			if (StringUtils.hasLength(timeoutString)) {
				try {
					setTimeout(Integer.parseInt(timeoutString));
				}
				catch (RuntimeException ex) {
					throw new IllegalArgumentException(
							"Invalid timeoutString value \"" + timeoutString + "\" - cannot parse into int");
				}
			}
		}

		if (resolver != null) {
			if (this.qualifier != null) {
				this.qualifier = resolver.resolveStringValue(this.qualifier);
			}
			Set<String> resolvedLabels = new LinkedHashSet<>(this.labels.size());
			for (String label : this.labels) {
				resolvedLabels.add(resolver.resolveStringValue(label));
			}
			this.labels = resolvedLabels;
		}
	}

	/**
	 * Return an identifying description for this transaction attribute.
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 */
	protected final StringBuilder getAttributeDescription() {
		StringBuilder result = getDefinitionDescription();
		if (StringUtils.hasText(this.qualifier)) {
			result.append("; '").append(this.qualifier).append('\'');
		}
		if (!this.labels.isEmpty()) {
			result.append("; ").append(this.labels);
		}
		return result;
	}

}
