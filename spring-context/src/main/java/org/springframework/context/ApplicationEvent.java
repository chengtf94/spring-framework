package org.springframework.context;

import java.time.Clock;
import java.util.EventObject;

/**
 * 应用事件基类
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class ApplicationEvent extends EventObject {
	private static final long serialVersionUID = 7099057708183571937L;

	/** 事件发生时间戳 */
	private final long timestamp;
	public final long getTimestamp() {
		return this.timestamp;
	}

	/** 构造方法 */
	public ApplicationEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}
	public ApplicationEvent(Object source, Clock clock) {
		super(source);
		this.timestamp = clock.millis();
	}

}
