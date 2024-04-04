package org.springframework.context;

import java.util.EventListener;
import java.util.function.Consumer;

/**
 * 应用监听器接口
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	/** 处理应用事件 */
	void onApplicationEvent(E event);

	/** 基于给定的负载数据事件消费者创建应用监听器 */
	static <T> ApplicationListener<PayloadApplicationEvent<T>> forPayload(Consumer<T> consumer) {
		return event -> consumer.accept(event.getPayload());
	}

}
