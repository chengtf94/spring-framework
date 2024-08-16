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

package org.springframework.jdbc.support;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.core.SpringProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.Nullable;

/**
 * JdbcTransactionManager：基于Spring JDBC 或 MyBatis 时使用，增加了SQL异常转译
 *
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 5.3
 * @see DataSourceTransactionManager
 * @see #setDataSource
 * @see #setExceptionTranslator
 */
@SuppressWarnings("serial")
public class JdbcTransactionManager extends DataSourceTransactionManager {

	/**
	 * Boolean flag controlled by a {@code spring.xml.ignore} system property that instructs Spring to
	 * ignore XML, i.e. to not initialize the XML-related infrastructure.
	 * <p>The default is "false".
	 */
	private static final boolean shouldIgnoreXml = SpringProperties.getFlag("spring.xml.ignore");

	/**
	 * SQL异常转译器
	 */
	@Nullable
	private volatile SQLExceptionTranslator exceptionTranslator;

	private boolean lazyInit = true;

	/**
	 * 构造方法
	 */
	public JdbcTransactionManager() {
		super();
	}

	public JdbcTransactionManager(DataSource dataSource) {
		this();
		setDataSource(dataSource);
		afterPropertiesSet();
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}

	/**
	 * Return the exception translator for this instance.
	 * <p>Creates a default {@link SQLErrorCodeSQLExceptionTranslator} for the specified DataSource if none set.
	 * @see #getDataSource()
	 */
	public SQLExceptionTranslator getExceptionTranslator() {
		SQLExceptionTranslator exceptionTranslator = this.exceptionTranslator;
		if (exceptionTranslator != null) {
			return exceptionTranslator;
		}
		synchronized (this) {
			exceptionTranslator = this.exceptionTranslator;
			if (exceptionTranslator == null) {
				if (shouldIgnoreXml) {
					exceptionTranslator = new SQLExceptionSubclassTranslator();
				} else {
					exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(obtainDataSource());
				}
				this.exceptionTranslator = exceptionTranslator;
			}
			return exceptionTranslator;
		}
	}

	/**
	 * This implementation attempts to use the {@link SQLExceptionTranslator},
	 * falling back to a {@link org.springframework.transaction.TransactionSystemException}.
	 * @see #getExceptionTranslator()
	 * @see DataSourceTransactionManager#translateException
	 */
	@Override
	protected RuntimeException translateException(String task, SQLException ex) {
		DataAccessException dae = getExceptionTranslator().translate(task, null, ex);
		if (dae != null) {
			return dae;
		}
		return super.translateException(task, ex);
	}

	/**
	 * Specify the database product name for the DataSource that this transaction manager uses.
	 */
	public void setDatabaseProductName(String dbName) {
		if (!shouldIgnoreXml) {
			this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dbName);
		}
	}

	/**
	 * Set the exception translator for this instance.
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * Set whether to lazily initialize the SQLExceptionTranslator for this transaction manager,
	 * on first encounter of an SQLException. Default is "true"
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public boolean isLazyInit() {
		return this.lazyInit;
	}

}
