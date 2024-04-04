package org.springframework.context.event;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * 默认事件监听器工程
 *
 * @author Stephane Nicoll
 * @since 4.2
 */
public class DefaultEventListenerFactory implements EventListenerFactory, Ordered {

	private int order = LOWEST_PRECEDENCE;
	public void setOrder(int order) {
		this.order = order;
	}
	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public boolean supportsMethod(Method method) {
		return true;
	}

	@Override
	public ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method) {
		// 创建应用监听器方法适配器实例
		return new ApplicationListenerMethodAdapter(beanName, type, method);
	}

}
