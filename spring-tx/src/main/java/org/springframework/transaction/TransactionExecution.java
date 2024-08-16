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

/**
 * TransactionExecution：事务执行接口
 *
 * @author Juergen Hoeller
 * @since 5.2
 */
public interface TransactionExecution {

	/**
	 * 判断当前事务是否是一个新的事务
	 */
	boolean isNewTransaction();

	/**
	 * 将当前事务设置为RollbackOnly：若被标记成了RollbackOnly，意味着事务只能被回滚
	 */
	void setRollbackOnly();

	/**
	 * 判断当前事务是否为RollbackOnly
	 */
	boolean isRollbackOnly();

	/**
	 * 判断当前事务是否完成：回滚或提交都意味着事务完成了
	 */
	boolean isCompleted();

}
