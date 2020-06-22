package com.sirmaenterprise.sep.jms.annotations;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a JMS Queue destination.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/05/2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD })
public @interface DestinationDef {

	/**
	 * Destination name
	 *
	 * @return the destination name
	 */
	String value() default "";

	/**
	 * Specifies the destination type. Default is JMS Queue.
	 *
	 * @return the destination type
	 */
	DestinationType type() default DestinationType.QUEUE;

	/**
	 * Defines the expiration time that will be used for messages using the default expiration time
	 *
	 * @return the expiry delay for the current destination. Negative value means application default.
	 */
	long expiryDelay() default -1;

	/**
	 * Defines how long to wait before attempting redelivery of a cancelled message
	 *
	 * @return the delay between redelivery attempts. Negative value means application default.
	 */
	long redeliveryDelay() default -1L;

	/**
	 * Multiplier to apply to the {@link #redeliveryDelay()} parameter
	 *
	 * @return redelivery delay multiplier. Zero or negative value means application default.
	 */
	double redeliveryMultiplier() default 0.0;

	/**
	 * Defines how many time a cancelled message can be redelivered before sending to the {@link #deadLetterAddress()}
	 *
	 * @return the number of times to retry to deliver a messages
	 */
	int maxRedeliveryAttempts() default -1;

	/**
	 * Maximum value for the {@link #redeliveryDelay()} delay (in ms).
	 *
	 * @return the maximum redelivery delay in milliseconds
	 */
	long maxRedeliveryDelay() default -1L;

	/**
	 * The max bytes size. The messages to keep in memory before triggering the {@link #addressFullPolicy()}. <br>
	 * Note that only messages in memory can be selected by destination selector and browsed via
	 * {@link javax.jms.QueueBrowser}.
	 *
	 * @return the total memory to use for messages for the current destination before triggering the
	 * {@link #addressFullPolicy()}
	 */
	long maxSize() default -1L;

	/**
	 * The paging size. When the {@link #maxSize()} is reached and {@link #addressFullPolicy()} is set to
	 * {@link AddressFullMessagePolicyType#PAGE} then any new messages will be written to files with size up to the
	 * given size. Notes that pages are loaded fully and they cannot be bigger than the {@link #maxSize()}
	 *
	 * @return the paging files size
	 */
	long pageSize() default -1L;

	/**
	 * The number of page files to keep in memory to optimize IO during paging navigation.
	 *
	 * @return cached pages count
	 */
	int pageMaxCacheSize() default -1;

	/**
	 * Determines what happens when an address where {@link #maxSize()} is specified becomes full. (PAGE, DROP or BLOCK)
	 *
	 * @return the full address message policy.
	 */
	AddressFullMessagePolicyType addressFullPolicy() default AddressFullMessagePolicyType.PAGE;

	/**
	 * Day limit for the message counter history.
	 *
	 * @return message counter history days limit
	 */
	int messageCounterHistoryDayLimit() default -1;

	/**
	 * Defines whether a queue only uses last values or not
	 *
	 * @return true if the destination is last value queue
	 */
	boolean lastValueQueue() default false;

	/**
	 * The dead letter address
	 *
	 * @return custom dead letter address
	 */
	String deadLetterAddress() default "";

	/**
	 * Defines where to send a message that has expired.
	 *
	 * @return custom expiry address.
	 */
	String expiryAddress() default "";

}
