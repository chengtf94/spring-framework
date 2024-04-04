package org.springframework.context.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.annotation.AliasFor;

/**
 * 事件监听器注解：用于标注某个方法为应用事件监听器
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 4.2
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {

	/** 监听器处理的事件类型、生效条件（SpEL）、唯一标识 */
	@AliasFor("classes")
	Class<?>[] value() default {};
	@AliasFor("value")
	Class<?>[] classes() default {};
	String condition() default "";
	String id() default "";

}
