package org.springframework.context;

import java.util.function.Consumer;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
import org.springframework.util.Assert;

/**
 * 带有负载数据的应用事件
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.2
 */
@SuppressWarnings("serial")
public class PayloadApplicationEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

	/** 负载数据 */
	private final T payload;
	public T getPayload() {
		return this.payload;
	}

	/** 构造方法 */
	public PayloadApplicationEvent(Object source, T payload) {
		super(source);
		Assert.notNull(payload, "Payload must not be null");
		this.payload = payload;
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getPayload()));
	}

}
