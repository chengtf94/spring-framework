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

package org.springframework.context.support;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * AbstractRefreshableConfigApplicationContext：可刷新、可配置的ApplicationContext抽象基类
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public abstract class AbstractRefreshableConfigApplicationContext
		extends AbstractRefreshableApplicationContext
		implements BeanNameAware, InitializingBean {

	/**
	 * 配置位置数组
	 */
	@Nullable
	private String[] configLocations;

	private boolean setIdCalled = false;

	/**
	 * 构造方法
	 */
	public AbstractRefreshableConfigApplicationContext() {
	}

	public AbstractRefreshableConfigApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}

	/**
	 * 设置配置位置
	 */
	public void setConfigLocation(String location) {
		setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
	}

	/**
	 * 设置配置位置
	 */
	public void setConfigLocations(@Nullable String... locations) {
		if (locations != null) {
			Assert.noNullElements(locations, "Config locations must not be null");
			this.configLocations = new String[locations.length];
			for (int i = 0; i < locations.length; i++) {
				this.configLocations[i] = resolvePath(locations[i]).trim();
			}
		} else {
			this.configLocations = null;
		}
	}

	/**
	 * 解析指定的路径：Resolve the given path, replacing placeholders with corresponding
	 */
	protected String resolvePath(String path) {
		return getEnvironment().resolveRequiredPlaceholders(path);
	}

	@Nullable
	protected String[] getConfigLocations() {
		return (this.configLocations != null ? this.configLocations : getDefaultConfigLocations());
	}

	@Nullable
	protected String[] getDefaultConfigLocations() {
		return null;
	}

	@Override
	public void setId(String id) {
		super.setId(id);
		this.setIdCalled = true;
	}

	@Override
	public void setBeanName(String name) {
		if (!this.setIdCalled) {
			super.setId(name);
			setDisplayName("ApplicationContext '" + name + "'");
		}
	}

	@Override
	public void afterPropertiesSet() {
		if (!isActive()) {
			// 刷新容器
			refresh();
		}
	}

}
