/*
 * Copyright 2002-2020 the original author or authors.
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

import java.io.Flushable;

import org.springframework.core.Ordered;

/**
 * TransactionSynchronization：事务同步接口
 * Interface for transaction synchronization callbacks.
 *
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see TransactionSynchronizationManager
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
 */
public interface TransactionSynchronization extends Ordered, Flushable {

	/**
	 * 事务完成的状态
	 */
	int STATUS_COMMITTED = 0;	// 提交
	int STATUS_ROLLED_BACK = 1; // 回滚
	int STATUS_UNKNOWN = 2;     // 异常状态，例如在事务执行时出现异常，然后回滚，回滚时又出现异常，就会被标记成状态2

	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	/**
	 * Suspend this synchronization.
	 * @see TransactionSynchronizationManager#unbindResource
	 */
	default void suspend() {
	}

	/**
	 * Resume this synchronization.
	 * @see TransactionSynchronizationManager#bindResource
	 */
	default void resume() {
	}

	/**
	 * Flush the underlying session to the datastore, if applicable: for example, a Hibernate/JPA session.
	 */
	@Override
	default void flush() {
	}

	/**
	 * Invoked before transaction commit (before "beforeCompletion").
	 */
	default void beforeCommit(boolean readOnly) {
	}

	/**
	 * Invoked before transaction commit/rollback.
	 */
	default void beforeCompletion() {
	}

	/**
	 * Invoked after transaction commit. Can perform further operations right
	 * <i>after</i> the main transaction has <i>successfully</i> committed.
	 */
	default void afterCommit() {
	}

	/**
	 * Invoked after transaction commit/rollback.
	 * Can perform resource cleanup <i>after</i> transaction completion.
	 */
	default void afterCompletion(int status) {
	}

}
