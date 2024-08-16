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

package org.springframework.transaction.interceptor;

import java.util.Collection;

import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionDefinition;

/**
 * TransactionAttribute：事务属性接口，在TransactionDefinition的基础上，新增了两个事务的属性
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Paluch
 * @since 16.03.2003
 * @see DefaultTransactionAttribute
 * @see RuleBasedTransactionAttribute
 */
public interface TransactionAttribute extends TransactionDefinition {

	/**
	 * 获取指定事务使用的事务管理器的名称：Return a qualifier value associated with this transaction attribute.
	 * <p>This may be used for choosing a corresponding transaction manager to process this specific transaction.
	 * @since 3.0
	 */
	@Nullable
	String getQualifier();

	/**
	 * Return labels associated with this transaction attribute.
	 * <p>This may be used for applying specific transactional behavior or follow a purely descriptive nature.
	 * @since 5.3
	 */
	Collection<String> getLabels();

	/**
	 * 判断在出现某种异常时是否执行回滚：Should we roll back on the given exception?
	 * @param ex the exception to evaluate
	 * @return whether to perform a rollback or not
	 */
	boolean rollbackOn(Throwable ex);

}
