/*
 * Copyright 2002-2014 the original author or authors.
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

/**
 * SavepointManager：保存点管理接口，隔离级别为NESTED时就是通过设置回滚点来实现的
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionStatus
 * @see TransactionDefinition#PROPAGATION_NESTED
 * @see java.sql.Savepoint
 */
public interface SavepointManager {

	/**
	 * 创建保存点：Create a new savepoint.
	 */
	Object createSavepoint() throws TransactionException;

	/**
	 * 回滚到指定保存点：Roll back to the given savepoint.
	 */
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

	/**
	 * 移除保存点：Explicitly release the given savepoint.
	 */
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
