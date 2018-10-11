package com.sirmaenterprise.sep.jms.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Defines JMS message observer method on a {@link javax.jms.Queue} destination.<br>
 *     The method should accept one
 * argument of type {@link javax.jms.Message}and one optional argument of type {@link javax.jms.JMSContext}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/05/2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD })
public @interface QueueListener {
	/**
	 * Defines the queue JNDI name to listen to. <br>If such destination does not exists it will be created.
	 *
	 * @return the destination name
	 */
	String value();

	/**
	 * JMS message selector to apply when accepting messages<br>For more information about the syntax of the selector
	 * check the JMS {@link javax.jms.Message} documentation
	 *
	 * @return the selector query
	 * @see javax.jms.Message
	 */
	String selector() default "";

	/**
	 * The maximum concurrent receivers to use. Note that the API implementation may impose restrictions on the
	 * maximum allowed receivers per destination(listener).
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
