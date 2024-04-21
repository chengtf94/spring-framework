package org.springframework.scheduling.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 可调度方法注解
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Dave Syer
 * @author Chris Beams
 * @author Victor Brown
 * @author Sam Brannen
 * @since 3.0
 * @see EnableScheduling
 * @see ScheduledAnnotationBeanPostProcessor
 * @see Schedules
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Schedules.class)
public @interface Scheduled {
	String CRON_DISABLED = ScheduledTaskRegistrar.CRON_DISABLED;

	/** A cron-like expression */
	String cron() default "";

	String zone() default "";
	long fixedDelay() default -1;
	String fixedDelayString() default "";
	long fixedRate() default -1;
	String fixedRateString() default "";
	long initialDelay() default -1;
	String initialDelayString() default "";
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
