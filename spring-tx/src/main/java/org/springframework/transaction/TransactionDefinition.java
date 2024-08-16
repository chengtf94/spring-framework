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

package org.springframework.transaction;

import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

/**
 * TransactionDefinition：事务定义接口
 * Interface that defines Spring-compliant transaction properties.
 * Based on the propagation behavior definitions analogous to EJB CMT attributes.
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 * @see PlatformTransactionManager#getTransaction(TransactionDefinition)
 * @see org.springframework.transaction.support.DefaultTransactionDefinition
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 */
public interface TransactionDefinition {

	/**
	 * 7种事务传播机制
	 * @see Propagation
	 */
	int PROPAGATION_REQUIRED = 0;		// 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。
	int PROPAGATION_SUPPORTS = 1;		// 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。
	int PROPAGATION_MANDATORY = 2;      // 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。
	int PROPAGATION_REQUIRES_NEW = 3;   // 创建一个新的事务，如果当前存在事务，则把当前事务挂起。
	int PROPAGATION_NOT_SUPPORTED = 4;  // 以非事务方式运行，如果当前存在事务，则把当前事务挂起。
	int PROPAGATION_NEVER = 5;          // 以非事务方式运行，如果当前存在事务，则抛出异常。
	int PROPAGATION_NESTED = 6;         // 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于TransactionDefinition.PROPAGATION_REQUIRED。

	/**
	 * 4种事务隔离级别：-1代表的是使用数据库默认的隔离级别，例如在MySQL下使用的就是ISOLATION_REPEATABLE_READ（可重复读）
	 * @see java.sql.Connection
	 * @see Isolation
	 */
	int ISOLATION_DEFAULT = -1;
	int ISOLATION_READ_UNCOMMITTED = 1;  // same as java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;
	int ISOLATION_READ_COMMITTED = 2;  // same as java.sql.Connection.TRANSACTION_READ_COMMITTED;
	int ISOLATION_REPEATABLE_READ = 4;  // same as java.sql.Connection.TRANSACTION_REPEATABLE_READ;
	int ISOLATION_SERIALIZABLE = 8;  // same as java.sql.Connection.TRANSACTION_SERIALIZABLE;

	/**
	 * 超时时间，默认不限制时间
	 */
	int TIMEOUT_DEFAULT = -1;

	/**
	 * 获取事务的传播行为：Return the propagation behavior.
	 */
	default int getPropagationBehavior() {
		return PROPAGATION_REQUIRED;
	}

	/**
	 * 获取事务的隔离级别：Return the isolation level.
	 */
	default int getIsolationLevel() {
		return ISOLATION_DEFAULT;
	}

	/**
	 * 获取事务的超时时间：Return the transaction timeout.
	 */
	default int getTimeout() {
		return TIMEOUT_DEFAULT;
	}

	/**
	 * 是否为只读事务：默认为非只读事务，Return whether to optimize as a read-only transaction.
	 */
	default boolean isReadOnly() {
		return false;
	}

	/**
	 * 获取事务的名称：Return the name of this transaction. Can be {@code null}.
	 */
	@Nullable
	default String getName() {
		return null;
	}


	// Static builder methods

	/**
	 * 返回一个只读的TransactionDefinition：只对属性提供了getter方法，所有属性都是接口中定义的默认值
	 */
	static TransactionDefinition withDefaults() {
		return StaticTransactionDefinition.INSTANCE;
	}

}
