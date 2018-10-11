package com.sirmaenterprise.sep.jms.api;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import com.sirmaenterprise.sep.jms.convert.MapMessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;

/**
 * Main entry point for sending JMS messages. The api defines different methods to build and send different kinds of
 * JMS messages.
 * <br>Methods that does not accept {@link SendOptions} rely on the default options for message sending.
 * <br>Methods should be stateless and using method with custom options will not affect method using default options.
 * <br>The service manages a {@link JMSContext} per transaction so using this service outside a transaction scope
 * will result in {@link javax.enterprise.context.ContextNotActiveException} to be thrown.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 18/05/2017
 */
public interface SenderService {

	/**
	 * Sends headers only message, filled by the given message initializer, to the given destination using default options.
	 * The send message will not have specific type like {@link MapMessage} or {@link TextMessage}. <br>This is
	 * useful when no special data need to be send the the data is small (2-3 properties).
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer that will fill the message payload
	 */
	void send(String destination, JmsMessageInitializer<Message> initializer);

	/**
	 * Sends headers only message, filled by the given message initializer, to the given destination using the specified
	 * options. The send message will not have specific type like {@link MapMessage} or {@link TextMessage}. <br>This is
	 * useful when no special data need to be send the the data is small (2-3 properties).
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer that will fill the message payload
	 * @param options any custom options to be used while sending this message
	 * @see #send(String, JmsMessageInitializer)
	 */
	void send(String destination, JmsMessageInitializer<Message> initializer, SendOptions options);

	/**
	 * Sends a message with payload the given data converted by the {@link MessageWriter} provided by the
	 * {@link SendOptions#withWriter(MessageWriter)}. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param destination the message destination instance where the message must be delivered
	 * @param data the data to convert and send
	 * @param <D> the message type
	 */
	<D> void send(Destination destination, D data);

	/**
	 * Sends a message with payload the given data converted by the {@link MessageWriter} provided by the
	 * {@link SendOptions#withWriter(MessageWriter)}. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param destination the message destination instance where the message must be delivered
	 * @param data the data to convert and send
	 * @param options the send options that can be used to override any of the options
	 * @param <D> the message type
	 */
	<D> void send(Destination destination, D data, SendOptions options);

	/**
	 * Sends a message with payload the given data converted by the {@link MessageWriter} provided by the
	 * {@link SendOptions#withWriter(MessageWriter)}. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the data to convert and send
	 * @param <D> the message type
	 */
	<D> void send(String destination, D data);

	/**
	 * Sends a message with payload the given data converted by the {@link MessageWriter} provided by the
	 * {@link SendOptions#withWriter(MessageWriter)}. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the data to convert and send
	 * @param options the send options that can be used to override any of the options
	 * @param <D> the message type
	 */
	<D> void send(String destination, D data, SendOptions options);

	/**
	 * Sends a message with payload the given data converted by the given {@link MessageWriter}. If the writer is not
	 * set the one from {@link SendOptions#withWriter(MessageWriter)} will be used. If no writer is set a
	 * {@link com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException} will be thrown.
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the data to convert and send
	 * @param messageWriter the message writer to use for message producing
	 * @param options the send options that can be used to override any of the options
	 * @param <D> the data type
	 */
	<D> void send(String destination, D data, MessageWriter<D, Message> messageWriter, SendOptions options);

	/**
	 * Sends {@link ObjectMessage} using the given send options. The message will be build using a writer selected in
	 * order:<ol>
	 * <li>writer argument</li>
	 * <li>{@link com.sirmaenterprise.sep.jms.convert.MessageWriters#getWriterFor(Class)} with
	 * {@code ObjectMessageWriter.class} as second argument</li>
	 * <li>{@link ObjectMessageWriter#instance()} if the argument implements {@link Serializable}</li>
	 * </ol>
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the data to send
	 * @param writer the explicit writer to use
	 * @param <D> the data type
	 */
	<D> void sendObject(String destination, D data, ObjectMessageWriter<D> writer);

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
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the data to send
	 * @param writer the explicit writer to use
	 * @param options any other options to override
	 * @param <D> the data type
	 */
	<D> void sendObject(String destination, D data, ObjectMessageWriter<D> writer, SendOptions options);

	/**
	 * Send {@link ObjectMessage} build by the given initializer to the given destination using the default options. The
	 * method should create empty object message and pass it to the given initializer to fill any additional data.
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer to be used for populating message data
	 */
	void sendObject(String destination, JmsMessageInitializer<ObjectMessage> initializer);

	/**
	 * Sends {@link ObjectMessage} build by the given initializer to the given destination using the specified options. The
	 * method should create empty object message and pass it to the given initializer to fill any additional data.
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer to be used for populating message data
	 * @param options any custom options to use whne sending the message
	 */
	void sendObject(String destination, JmsMessageInitializer<ObjectMessage> initializer, SendOptions options);

	/**
	 * Sends {@link TextMessage} to the given destination and sets the given text data as payload of the message. The
	 * send will be performed using the default options
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the text data to send
	 */
	void sendText(String destination, String data);

	/**
	 * Sends {@link TextMessage} to the given destination and using the specified options. The given text data will be
	 * set as payload of the message
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the text data to send
	 * @param options the options to use when sending the message
	 */
	void sendText(String destination, String data, SendOptions options);

	/**
	 * Sends {@link TextMessage} to the given destination. The message will be created and passed to the given initializer
	 * to fill the data and any additional properties before sending the message. This method is useful when
	 * additional properties need to be set to the message other than the payload
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer to call to fill the message data
	 */
	void sendText(String destination, JmsMessageInitializer<TextMessage> initializer);

	/**
	 * Sends {@link TextMessage} to the given destination using the specified options. The message will be created and
	 * passed to the given initializer to fill the data and any additional properties before sending the message. This
	 * method is useful when additional properties need to be set to the message other than the payload
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer to call to fill the message data
	 * @param options the options to use when sending the message
	 */
	void sendText(String destination, JmsMessageInitializer<TextMessage> initializer, SendOptions options);

	/**
	 * Sends {@link MapMessage} populated with data from the given source object using the default send options.
	 * The data conversion will be done using: <ol><li>the given map message writer</li>
	 * <li>registered writer via {@link #registerMapWriter(Class, MapMessageWriter) registerMapWriter} or one of the
	 * {@link #registerWriter} methods</li></ol>
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the data to convert and send
	 * @param writer the writer to use, it will override any already registered converter for the same type
	 * @param <D> the data type
	 * @see #registerMapWriter(Class, MapMessageWriter)
	 * @see #registerWriter(Class, Class)
	 * @see #registerWriter(Class, MessageWriter)
	 */
	<D> void sendMap(String destination, D data, MapMessageWriter<D> writer);

	/**
	 * Sends {@link MapMessage} populated with data from the given source object using the given send options.
	 * The data conversion will be done using:
	 * <ol><li>the given map message writer</li>
	 * <li>{@link SendOptions#withMapWriter(MapMessageWriter)}</li>
	 * <li>registered writer via {@link #registerMapWriter(Class, MapMessageWriter) registerMapWriter} or one of the
	 * {@link #registerWriter} methods</li></ol>
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param data the data to convert and send
	 * @param writer the writer to use, it will override any already registered converter for the same type
	 * @param options the send options that can be used to override any of the options
	 * @param <D> the data type
	 * @see #registerMapWriter(Class, MapMessageWriter)
	 * @see #registerWriter(Class, Class)
	 * @see #registerWriter(Class, MessageWriter)
	 */
	<D> void sendMap(String destination, D data, MapMessageWriter<D> writer, SendOptions options);

	/**
	 * Sends {@link MapMessage} populated by the given message initializer. The method should create empty map message
	 * and pass it to the given initializer to set any additional properties and/or payload data. The send will happen
	 * using the default send options.
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer to call to populate the message before send
	 */
	void sendMap(String destination, JmsMessageInitializer<MapMessage> initializer);

	/**
	 * Sends {@link MapMessage} populated by the given message initializer. The method should create empty map message
	 * and pass it to the given initializer to set any additional properties and/or payload data. The send will happen
	 * using the given send options.
	 *
	 * @param destination the message destination JNDI where the message must be delivered
	 * @param initializer the initializer to call to populate the message before send
	 * @param options the send options that can be used to override any of the options
	 */
	void sendMap(String destination, JmsMessageInitializer<MapMessage> initializer, SendOptions options);

	/**
	 * Create a {@link MessageSender} that will send message only to the given destination. The sender will be
	 * initialized with default send options.<br>This is useful when the client need to send batches of messages to
	 * the same destination.<br>The returned sender will be automatically closed when the transaction is committed.
	 *
	 * @param destination the destination where all send messages via the build sender will be delivered.
	 * @return a message sender instance that delivers messages to the specified destination
	 */
	default MessageSender createSender(String destination) {
		return createSender(destination, SendOptions.create());
	}

	/**
	 * Create a {@link MessageSender} that will send message only to the given destination. The sender will be
	 * initialized with the given send options.<br>This is useful when the client need to send batches of messages to
	 * the same destination.<br>The returned sender will be automatically closed when the transaction is committed.
	 *
	 * @param destination the destination where all send messages via the build sender will be delivered.
	 * @param options the options to set in the message sender as default
	 * @return a message sender instance that delivers messages to the specified destination
	 */
	MessageSender createSender(String destination, SendOptions options);

	/**
	 * Gets the current active {@link JMSContext} that is used for message sending by the current service instance.
	 * Note that this context is valid only for the transaction that accessed this service instance and will be
	 * closed at the end of the transaction
	 *
	 * @return the current JMS context
	 */
	JMSContext getCurrentContext();

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
}
