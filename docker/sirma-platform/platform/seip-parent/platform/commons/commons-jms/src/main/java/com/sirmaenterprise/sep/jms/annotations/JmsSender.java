package com.sirmaenterprise.sep.jms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import javax.jms.CompletionListener;
import javax.jms.Message;

import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.security.SecurityMode;

/**
 * Define an injection for a {@link com.sirmaenterprise.sep.jms.api.MessageSender} that is preconfigured using the
 * configurations defined in the annotation. The defined sender could send messages only to the defined JMS
 * destination at {@link #destination()}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/05/2017
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface JmsSender {

	/**
	 * Defines the target JMS destination where the send messages should be delivered. If no such destination exists,
	 * one will be created.
	 *
	 * @return the JNDI name of the JMS destination Queue or Topic
	 */
	@Nonbinding
	String destination();

	/**
	 * The JNDI name of the reply queue where the replies should be send.
	 *
	 * @return the JNDI name of the reply destination
	 * @see com.sirmaenterprise.sep.jms.api.SendOptions#replyTo(String)
	 */
	@Nonbinding
	String replyTo() default "";

	/**
	 * Defines optional JMS message type
	 *
	 * @return the message type to set to all send messages
	 * @see com.sirmaenterprise.sep.jms.api.SendOptions#asJmsType(String)
	 */
	@Nonbinding
	String jmsType() default "";

	/**
	 * Defines an expiration for all sent messages in milliseconds.
	 *
	 * @return the expiration time in milliseconds
	 * @see com.sirmaenterprise.sep.jms.api.SendOptions#expireAfter(long)
	 */
	@Nonbinding
	long timeToLive() default Message.DEFAULT_TIME_TO_LIVE;

	/**
	 * Define a delivery delay for sent messages. The messages will be delivered to the destination queue after the
	 * specified delay based on the actual message send time.
	 *
	 * @return the delivery delay in milliseconds
	 * @see com.sirmaenterprise.sep.jms.api.SendOptions#delayWith(long)
	 */
	@Nonbinding
	long deliveryDelay() default Message.DEFAULT_DELIVERY_DELAY;

	/**
	 * Define if the messages should be persistent or not. By default messages are persistent and will not be lost on
	 * JMS provider restart.
	 *
	 * @return the messages should be persistent
	 * @see SendOptions#persistent()
	 * @see SendOptions#nonPersistent()
	 * @see javax.jms.DeliveryMode
	 */
	@Nonbinding
	boolean persistent() default true;

	/**
	 * Defines the send messages priority. Allowed values are between 0 and 9 inclusive. Different values will result
	 * in exception during injection time
	 *
	 * @return the message priority
	 * @see com.sirmaenterprise.sep.jms.api.SendOptions#withPriority(int)
	 */
	@Nonbinding
	int priority() default Message.DEFAULT_PRIORITY;

	/**
	 * Defines the security mode to be used when sending/receiving messages.
	 *
	 * @return the security mode
	 * @see SecurityMode
	 */
	@Nonbinding
	SecurityMode security() default SecurityMode.DEFAULT;

	/**
	 * Defines a message writer to use for generic send methods. The class will be instantiated using the registered
	 * {@link com.sirmaenterprise.sep.jms.convert.MessageWriterBuilder}
	 *
	 * @return a writer class to instantiate and use for generic message sending
	 * @see com.sirmaenterprise.sep.jms.api.SendOptions#withWriter(MessageWriter)
	 */
	@Nonbinding
	Class<? extends MessageWriter> writer() default MessageWriter.class;

	/**
	 * Defines a default {@link CompletionListener} to be used for asynchronous message delivery. The specified class
	 * should be CDI resolvable bean. If specified the message delivery will happen asynchronously and the specified
	 * class will be notified for each message successful or failed delivery send using the annotated
	 * {@link com.sirmaenterprise.sep.jms.api.MessageSender}
	 *
	 * @return an async completion listener class to use
	 * @see com.sirmaenterprise.sep.jms.api.SendOptions#async(CompletionListener)
	 */
	@Nonbinding
	Class<? extends CompletionListener> async() default CompletionListener.class;
}
