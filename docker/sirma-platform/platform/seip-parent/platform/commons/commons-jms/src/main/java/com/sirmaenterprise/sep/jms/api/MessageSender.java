package com.sirmaenterprise.sep.jms.api;

import java.io.Serializable;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import com.sirmaenterprise.sep.jms.convert.MapMessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;

/**
 * Provides message sending facility for a single destination.<p>
 * All send methods that does not accept {@link SendOptions} parameter will use the options returned by the
 * method {@link #getDefaultSendOptions()}.<br>
 * All send methods that accept {@link SendOptions} parameter will use that options and will not be merged in
 * any way with the default options. If the sender need to override any of the default options for the
 * sender it should use a copy of the default options, modify them and pass them to any of the send methods.
 * <pre><code>
 *     MessageSender sender = ....;
 *     SendOptions options = sender.getDefaultSendOptions().noCorrelationId();
 *     sender.send(data, options);
 * </code></pre>
 * <p>
 * There are two main ways to obtain instance of type {@code MessageSender}:<ol>
 * <li>Create new instance via {@link SenderService#createSender} methods
 * <pre><code>
 *  &#64;Inject
 *  private SenderService senderService;
 *  .....
 *  MessageSender sender = senderService.createSender("java:/jms.queue.TestQueue");
 *  getData().forEach(sender::send);
 * </code></pre></li>
 * <li>Inject a {@code MessageSender}
 * <pre><code>
 *  &#64;Inject
 *  &#64;JmsSender(destination = "java:/jms.queue.TestQueue")
 *  private MessageSender testQueueSender;
 *  ...
 *  getData().forEach(testQueueSender::send);
 * </code></pre></li>
 * </ol>
 * </p>
 * <p>Note that the this class is not guaranteed to be thread safe. The user is discouraged of using single sender
 * in more than one thread, as it may lead to unexpected results. If the user need to send messages in separate
 * threads to a single destination it should obtain an instance per thread. This is mainly a limitation of the JMS
 * API as some the of send options are global for the used {@link javax.jms.JMSProducer}</p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/05/2017
 */
public interface MessageSender {

	/**
	 * Sends headers only message, filled by the given message initializer using default options.
	 * The send message will not have specific type like {@link MapMessage} or {@link TextMessage}. <br>This is
	 * useful when no special data need to be send the the data is small (2-3 properties).
	 *
	 * @param initializer the initializer that will fill the message payload
	 */
	default void send(JmsMessageInitializer<Message> initializer) {
		send(initializer, getDefaultSendOptions());
	}

	/**
	 * Sends headers only message, filled by the given message initializer using the specified options. The send message
	 * will not have specific type like {@link MapMessage} or {@link TextMessage}. <br>This is useful when no
	 * special data need to be send the the data is small (2-3 properties).
	 *
	 * @param initializer the initializer that will fill the message payload
	 * @param options any custom options to be used while sending this message
	 * @see #send(JmsMessageInitializer)
	 */
	void send(JmsMessageInitializer<Message> initializer, SendOptions options);

	/**
	 * Sends a message with payload the given data converted by the {@link MessageWriter} provided by the
	 * {@link SendOptions#withWriter(MessageWriter)}. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param data the data to convert and send
	 * @param <D> the message type
	 */
	default <D> void send(D data) {
		send(data, getDefaultSendOptions());
	}

	/**
	 * Sends a message with payload the given data converted by the {@link MessageWriter} provided by the
	 * {@link SendOptions#withWriter(MessageWriter)}. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param data the data to convert and send
	 * @param options the send options that can be used to override any of the options
	 * @param <D> the message type
	 */
	default <D> void send(D data, SendOptions options) {
		send(data, options.getWriter(), options);
	}

	/**
	 * Sends a message with payload the given data converted by the given {@link MessageWriter}. If the writer is not
	 * set the one from {@link SendOptions#withWriter(MessageWriter)} will be used. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param data the data to convert and send
	 * @param messageWriter the message writer to use for message producing
	 * @param options the send options that can be used to override any of the options
	 * @param <D> the data type
	 */
	<D> void send(D data, MessageWriter<D, ? extends Message> messageWriter, SendOptions options);

	/**
	 * Sends {@link ObjectMessage} using the given send options. The message will be build using a writer selected in
	 * order:<ol>
	 * <li>writer argument</li>
	 * <li>{@link com.sirmaenterprise.sep.jms.convert.MessageWriters#getWriterFor(Class)} with
	 * {@code ObjectMessageWriter.class} as second argument</li>
	 * <li>{@link ObjectMessageWriter#instance()} if the argument implements {@link Serializable}</li>
	 * </ol>
	 *
	 * @param data the data to send
	 * @param writer the explicit writer to use
	 * @param <D> the data type
	 */
	default <D> void sendObject(D data, ObjectMessageWriter<D> writer) {
		sendObject(data, writer, getDefaultSendOptions());
	}

	/**
	 * Sends {@link ObjectMessage} using the given send options. The message will be build using a writer selected in
	 * order:<ol>
	 * <li>writer argument</li>
	 * <li>{@link SendOptions#getWriter()}</li>
	 * <li>{@link com.sirmaenterprise.sep.jms.convert.MessageWriters#getWriterFor(Class)} with
	 * {@code ObjectMessageWriter.class} as second argument</li>
	 * <li>{@link ObjectMessageWriter#instance()} if the argument implements {@link Serializable}</li>
	 * </ol>
	 *
	 * @param data the data to send
	 * @param writer the explicit writer to use
	 * @param options any other options to override
	 * @param <D> the data type
	 */
	<D> void sendObject(D data, ObjectMessageWriter<D> writer, SendOptions options);

	/**
	 * Send {@link ObjectMessage} build by the given initializer using the default options. The method should create
	 * empty object message and pass it to the given initializer to fill any additional data.
	 *
	 * @param initializer the initializer to be used for populating message data
	 */
	default void sendObject(JmsMessageInitializer<ObjectMessage> initializer) {
		sendObject(initializer, getDefaultSendOptions());
	}

	/**
	 * Sends {@link ObjectMessage} build by the given initializer using the specified options. The method should create
	 * empty object message and pass it to the given initializer to fill any additional data.
	 *
	 * @param initializer the initializer to be used for populating message data
	 * @param options any custom options to use whne sending the message
	 */
	void sendObject(JmsMessageInitializer<ObjectMessage> initializer, SendOptions options);

	/**
	 * Sends {@link TextMessage} by setting the given text data as payload of the message. The send will be
	 * performed using the default options
	 *
	 * @param data the text data to send
	 */
	default void sendText(String data) {
		sendText(data, getDefaultSendOptions());
	}

	/**
	 * Sends {@link TextMessage} using the specified options. The given text data will be set as payload of the message
	 *
	 * @param data the text data to send
	 * @param options the options to use when sending the message
	 */
	void sendText(String data, SendOptions options);

	/**
	 * Sends {@link TextMessage}. The message will be created and passed to the given initializer
	 * to fill the data and any additional properties before sending the message. This method is useful when
	 * additional properties need to be set to the message other than the payload
	 *
	 * @param initializer the initializer to call to fill the message data
	 */
	default void sendText(JmsMessageInitializer<TextMessage> initializer) {
		sendText(initializer, getDefaultSendOptions());
	}

	/**
	 * Sends {@link TextMessage} using the specified options. The message will be created and passed to the given
	 * initializer to fill the data and any additional properties before sending the message. This
	 * method is useful when additional properties need to be set to the message other than the payload
	 *
	 * @param initializer the initializer to call to fill the message data
	 * @param options the options to use when sending the message
	 */
	void sendText(JmsMessageInitializer<TextMessage> initializer, SendOptions options);

	/**
	 * Sends {@link MapMessage} populated with data from the given source object using the default send options.
	 * The data conversion will be done using: <ol><li>the given map message writer</li>
	 * <li>registered writer via {@link #registerMapWriter(Class, MapMessageWriter) registerMapWriter} or one of the
	 * {@link #registerWriter} methods</li></ol>
	 *
	 * @param data the data to convert and send
	 * @param writer the writer to use, it will override any already registered converter for the same type
	 * @param <D> the data type
	 * @see #registerMapWriter(Class, MapMessageWriter)
	 * @see #registerWriter(Class, Class)
	 * @see #registerWriter(Class, MessageWriter)
	 */
	default <D> void sendMap(D data, MapMessageWriter<D> writer) {
		sendMap(data, writer, getDefaultSendOptions());
	}

	/**
	 * Sends {@link MapMessage} populated with data from the given source object using the given send options.
	 * The data conversion will be done using:
	 * <ol><li>the given map message writer</li>
	 * <li>{@link SendOptions#withMapWriter(MapMessageWriter)}</li>
	 * <li>registered writer via {@link #registerMapWriter(Class, MapMessageWriter) registerMapWriter} or one of the
	 * {@link #registerWriter} methods</li></ol>
	 *
	 * @param data the data to convert and send
	 * @param writer the writer to use, it will override any already registered converter for the same type
	 * @param options the send options that can be used to override any of the options
	 * @param <D> the data type
	 * @see #registerMapWriter(Class, MapMessageWriter)
	 * @see #registerWriter(Class, Class)
	 * @see #registerWriter(Class, MessageWriter)
	 */
	<D> void sendMap(D data, MapMessageWriter<D> writer, SendOptions options);

	/**
	 * Sends {@link MapMessage} populated by the given message initializer. The method should create empty map message
	 * and pass it to the given initializer to set any additional properties and/or payload data. The send will happen
	 * using the default send options.
	 *
	 * @param initializer the initializer to call to populate the message before send
	 */
	default void sendMap(JmsMessageInitializer<MapMessage> initializer) {
		sendMap(initializer, getDefaultSendOptions());
	}

	/**
	 * Sends {@link MapMessage} populated by the given message initializer. The method should create empty map message
	 * and pass it to the given initializer to set any additional properties and/or payload data. The send will happen
	 * using the given send options.
	 *
	 * @param initializer the initializer to call to populate the message before send
	 * @param options the send options that can be used to override any of the options
	 */
	void sendMap(JmsMessageInitializer<MapMessage> initializer, SendOptions options);

	/**
	 * Register a {@link MessageWriter} class for the given data type class. <br>If the given class is sub class of one
	 * of the {@link MapMessageWriter} or {@link ObjectMessageWriter} it will be registered for conversions used in
	 * the corresponding {@link #sendMap} and {@link #sendObject} methods.
	 *
	 * @param dataType the source data type that is supported by the given writer class
	 * @param writerClass the writer class. This class should be instantiable by at least one
	 * {@link com.sirmaenterprise.sep.jms.convert.MessageWriterBuilder}
	 * @param <T> the data type
	 */
	<T> void registerWriter(Class<T> dataType, Class<? extends MessageWriter> writerClass);

	/**
	 * Register a generic {@link MessageWriter} class for the given data type class. <br>If the given writer
	 * implements one of the {@link MapMessageWriter} or {@link ObjectMessageWriter} it will be registered for conversions used in
	 * the corresponding {@link #sendMap} and {@link #sendObject} methods.<br>Note that when the class instance is
	 * lambda expression then writer instance will only report the generic interface. If this is not desired use one
	 * of the specific {@link #registerMapWriter(Class, MapMessageWriter) registerMapWriter} or
	 * {@link #registerObjectWriter(Class, ObjectMessageWriter) registerObjectWriter} methods.
	 *
	 * @param dataType the source data type that is supported by the given writer class
	 * @param writer the writer to register.
	 * @param <T> the data type
	 */
	<T> void registerWriter(Class<T> dataType, MessageWriter<T, ? extends Message> writer);

	/**
	 * Register {@link MapMessageWriter} instance used when converting data by the {@link #sendMap} methods.
	 *
	 * @param dataType the source data type that is supported by the given converter
	 * @param writer the writer to register
	 * @param <T> the data type
	 */
	<T> void registerMapWriter(Class<T> dataType, MapMessageWriter<T> writer);

	/**
	 * Register {@link ObjectMessageWriter} instance used when converting data by the {@link #sendObject} methods.
	 *
	 * @param dataType the source data type that is supported by the given converter
	 * @param writer the writer to register
	 * @param <T> the data type
	 */
	<T extends Serializable> void registerObjectWriter(Class<T> dataType, ObjectMessageWriter<T> writer);

	/**
	 * Gets the JNDI name of the destination where all messages are going when using the current sender.
	 *
	 * @return the destination JNDI name
	 */
	String getDestination();

	/**
	 * Get a copy of the default send options used. Note that changes to the returned instance will not be affected
	 * to the actual options. If need to change any of the options for all send methods you should create new sender
	 * instance with the new options.
	 *
	 * @return a copy of the current default send options
	 */
	SendOptions getDefaultSendOptions();

	/**
	 * If the current sender is active and can send messages. Not active and closed instances are disconnected from
	 * an active transaction and will result in {@link com.sirmaenterprise.sep.jms.exception.JmsRuntimeException}.
	 * <br>Note that senders will automatically become inactive when the active transaction during they were created is
	 * completed successfully or with rollback.
	 *
	 * @return if the current sender is active
	 */
	boolean isActive();

	/**
	 * Manually mark the current sender as closed. This does not have any affect on automatically managed senders
	 */
	void close();
}
