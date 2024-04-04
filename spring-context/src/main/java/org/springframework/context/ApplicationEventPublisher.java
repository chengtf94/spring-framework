package org.springframework.context;

/**
 * 应用事件发布器接口
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 1.1.1
 */
@FunctionalInterface
public interface ApplicationEventPublisher {

	/** 发布事件 */
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}
	void publishEvent(Object event);

}
