package org.springframework.context.event;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationListener;

/**
 * 事件监听器工厂：用于给带有EventListener注解的方法创建对应的应用监听器实例
 *
 * @author Stephane Nicoll
 * @since 4.2
 */
public interface EventListenerFactory {

	/** 检查是否支持该方法 */
	boolean supportsMethod(Method method);

	/** 创建应用监听器实例 */
	ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method);

}
