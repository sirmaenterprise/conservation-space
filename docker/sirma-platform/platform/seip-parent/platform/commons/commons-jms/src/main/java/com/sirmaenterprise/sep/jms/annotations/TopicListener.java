package com.sirmaenterprise.sep.jms.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Defines JMS message observer method on a {@link javax.jms.Topic} destination. <br>
 *     The method should accept one argument of type {@link javax.jms.Message} and
 * one optional argument of type {@link javax.jms.JMSContext}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/05/2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD })
public @interface TopicListener {
	/**
	 * Defines the topic JNDI name to listen to
	 *
	 * @return the destination name
	 */
	String jndi();

	/**
	 * The subscription id to use.
	 *
	 * @return the subscription id
	 */
	String subscription();

	/**
	 * JMS message selector to apply when accepting messages
	 *
	 * @return the selector query
	 */
	String selector() default "";

	/**
	 * If the subscription should be durable or not
	 *
	 * @return true for durable subscription
	 */
	boolean durable() default true;
	
	/**
	 * The maximum concurrent receivers to use. Used to split the work of processing messages on the
	 * current topic subscription across multiple threads.
	 *
	 * @return the concurrent receivers
	 */
	int concurrencyLevel() default 1;

	/**
	 * Can be used to specify the transaction timeout while processing the incoming messages. The default timeout is the
	 * global transaction timeout for the application JTA environment.
	 *
	 * @return the value to use for transaction timeout. If used {@code 0} then the default value will be used. Negative
	 * value is not allowed and will result in exceptions
	 */
	int txTimeout() default 0;

	/**
	 * Specifies the time unit used for the {@link #txTimeout()} value. The default unit format is {@code seconds}.
	 *
	 * @return the time unit format
	 */
	TimeUnit timeoutUnit() default TimeUnit.SECONDS;
}
