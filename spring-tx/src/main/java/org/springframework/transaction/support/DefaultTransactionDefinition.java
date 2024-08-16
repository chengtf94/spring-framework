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

package org.springframework.transaction.support;

import java.io.Serializable;

import org.springframework.core.Constants;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionDefinition;

/**
 * DefaultTransactionDefinition：默认事务定义
 *
 * <p>Base class for both {@link TransactionTemplate} and
 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute}.
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 */
@SuppressWarnings("serial")
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {

	/** Prefix for the propagation constants defined in TransactionDefinition. */
	public static final String PREFIX_PROPAGATION = "PROPAGATION_";

	/** Prefix for the isolation constants defined in TransactionDefinition. */
	public static final String PREFIX_ISOLATION = "ISOLATION_";

	/** Prefix for transaction timeout values in description strings. */
	public static final String PREFIX_TIMEOUT = "timeout_";

	/** Marker for read-only transactions in description strings. */
	public static final String READ_ONLY_MARKER = "readOnly";

	/** Constants instance for TransactionDefinition. */
	static final Constants constants = new Constants(TransactionDefinition.class);

	/**
	 * 默认事务属性值
	 */
	private int propagationBehavior = PROPAGATION_REQUIRED;
	private int isolationLevel = ISOLATION_DEFAULT;
	private int timeout = TIMEOUT_DEFAULT;
	private boolean readOnly = false;

	@Nullable
	private String name;

	/**
	 * 构造方法
	 */
	public DefaultTransactionDefinition() {
	}

	public DefaultTransactionDefinition(TransactionDefinition other) {
		this.propagationBehavior = other.getPropagationBehavior();
		this.isolationLevel = other.getIsolationLevel();
		this.timeout = other.getTimeout();
		this.readOnly = other.isReadOnly();
		this.name = other.getName();
	}

	public DefaultTransactionDefinition(int propagationBehavior) {
		this.propagationBehavior = propagationBehavior;
	}

	/**
	 * Set the propagation behavior by the name of the corresponding constant in TransactionDefinition, e.g. "PROPAGATION_REQUIRED".
	 */
	public final void setPropagationBehaviorName(String constantName) throws IllegalArgumentException {
		if (!constantName.startsWith(PREFIX_PROPAGATION)) {
			throw new IllegalArgumentException("Only propagation constants allowed");
		}
		setPropagationBehavior(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set the propagation behavior. Must be one of the propagation constants in the TransactionDefinition interface.
	 */
	public final void setPropagationBehavior(int propagationBehavior) {
		if (!constants.getValues(PREFIX_PROPAGATION).contains(propagationBehavior)) {
			throw new IllegalArgumentException("Only values of propagation constants allowed");
		}
		this.propagationBehavior = propagationBehavior;
	}

	@Override
	public final int getPropagationBehavior() {
		return this.propagationBehavior;
	}

	/**
	 * Set the isolation level by the name of the corresponding constant in TransactionDefinition, e.g. "ISOLATION_DEFAULT".
	 */
	public final void setIsolationLevelName(String constantName) throws IllegalArgumentException {
		if (!constantName.startsWith(PREFIX_ISOLATION)) {
			throw new IllegalArgumentException("Only isolation constants allowed");
		}
		setIsolationLevel(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set the isolation level. Must be one of the isolation constants in the TransactionDefinition interface.
	 */
	public final void setIsolationLevel(int isolationLevel) {
		if (!constants.getValues(PREFIX_ISOLATION).contains(isolationLevel)) {
			throw new IllegalArgumentException("Only values of isolation constants allowed");
		}
		this.isolationLevel = isolationLevel;
	}

	@Override
	public final int getIsolationLevel() {
		return this.isolationLevel;
	}

	/**
	 * Set the timeout to apply, as number of seconds. Default is TIMEOUT_DEFAULT (-1).
	 */
	public final void setTimeout(int timeout) {
		if (timeout < TIMEOUT_DEFAULT) {
			throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
		}
		this.timeout = timeout;
	}

	@Override
	public final int getTimeout() {
		return this.timeout;
	}

	/**
	 * Set whether to optimize as read-only transaction. Default is "false".
	 */
	public final void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public final boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * Set the name of this transaction. Default is none.
	 */
	public final void setName(String name) {
		this.name = name;
	}

	@Override
	@Nullable
	public final String getName() {
		return this.name;
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof TransactionDefinition && toString().equals(other.toString())));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return getDefinitionDescription().toString();
	}

	/**
	 * Return an identifying description for this transaction definition.
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 */
	protected final StringBuilder getDefinitionDescription() {
		StringBuilder result = new StringBuilder();
		result.append(constants.toCode(this.propagationBehavior, PREFIX_PROPAGATION));
		result.append(',');
		result.append(constants.toCode(this.isolationLevel, PREFIX_ISOLATION));
		if (this.timeout != TIMEOUT_DEFAULT) {
			result.append(',');
			result.append(PREFIX_TIMEOUT).append(this.timeout);
		}
		if (this.readOnly) {
			result.append(',');
			result.append(READ_ONLY_MARKER);
		}
		return result;
	}

}
