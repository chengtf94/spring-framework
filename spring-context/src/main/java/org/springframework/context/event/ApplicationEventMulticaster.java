package org.springframework.context.event;

import java.util.function.Predicate;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 应用事件组播器接口：负责管理应用监听器集合，并发布事件给它们
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 */
public interface ApplicationEventMulticaster {

	/** 添加应用监听器实例、应用监听器Bean名称 */
	void addApplicationListener(ApplicationListener<?> listener);
	void addApplicationListenerBean(String listenerBeanName);

	/** 移除应用监听器实例、应用监听器Bean名称 */
	void removeApplicationListener(ApplicationListener<?> listener);
	void removeApplicationListenerBean(String listenerBeanName);

	/** 移除所有匹配的应用监听器实例、应用监听器Bean名称 */
	void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate);
	void removeApplicationListenerBeans(Predicate<String> predicate);

	/** 移除所有的应用监听器 */
	void removeAllListeners();

	/** 组播事件 */
	void multicastEvent(ApplicationEvent event);
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);

}
