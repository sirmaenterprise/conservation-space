package com.sirmaenterprise.sep.jms.impl.sender;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.jms.CompletionListener;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.util.CDI;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.MapMessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriters;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;
import com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException;

/**
 * Producer for {@link MessageSender} instances. The instances are build using the
 * {@link SenderService#createSender(String, SendOptions)} method.
 * <br>Any configured {@link MessageWriter} and {@link CompletionListener} instances will be resolved as CDI beans by
 * the configured class names using default qualifier.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/05/2017
 */
@ApplicationScoped
public class MessageSenderProducer {
	private static final Class[] SENDER_TYPE = { MessageSender.class };

	@Inject
	private Instance<SenderService> senderService;
	@Inject
	private MessageWriters messageWriters;

	/**
	 * Builds a {@link Proxy} instance of {@link MessageSender} to allow lazy bean initialization when there is
	 * active transaction otherwise {@link javax.enterprise.context.ContextNotActiveException} is thrown.
	 *
	 * @param point the injection point to read the {@link JmsSender} annotation
	 * @param beanManager the manager used for message writer and completion listener resolving
	 * @return the build message sender.
	 */
	@Produces
	@JmsSender(destination = "")
	MessageSender produce(InjectionPoint point, BeanManager beanManager) {
		JmsSender annotation = point.getAnnotated().getAnnotation(JmsSender.class);
		SendOptions sendOptions = buildOptions(annotation, beanManager);
		return buildSenderProxy(annotation.destination(), sendOptions);
	}

	private SendOptions buildOptions(JmsSender annotation, BeanManager beanManager) {
		SendOptions sendOptions = buildOptionsFromAnnotation(annotation);

		setWriters(annotation, sendOptions);

		// build the completion listener if not the default value
		if (!CompletionListener.class.equals(annotation.async())) {
			CompletionListener listener = CDI.instantiateBean(annotation.async(), beanManager, CDI.getDefaultLiteral());
			sendOptions.async(listener);
		}
		return sendOptions;
	}

	private static SendOptions buildOptionsFromAnnotation(JmsSender sender) {
		SendOptions sendOptions = SendOptions.create()
				.withPriority(sender.priority())
				.expireAfter(sender.timeToLive())
				.delayWith(sender.deliveryDelay())
				.replyTo(StringUtils.trimToNull(sender.replyTo()))
				.asJmsType(StringUtils.trimToNull(sender.jmsType()));

		if (!sender.persistent()) {
			sendOptions.nonPersistent();
		}

		switch (sender.security()) {
			case SYSTEM:
				sendOptions.asSystem();
				break;
			case TENANT_ADMIN:
				sendOptions.asTenantAdmin();
				break;
			default:
				sendOptions.asCurrentUser();
		}
		return sendOptions;
	}

	private void setWriters(JmsSender annotation, SendOptions sendOptions) {
		sendOptions.withWriter(resolveWriter(annotation.writer()));
	}

	private MessageWriter resolveWriter(Class<? extends MessageWriter> writerClass) {
		if (writerClass.equals(MessageWriter.class)) {
			// this is the default annotation valid, so we ignore it
			return null;
		} else if (writerClass.equals(MapMessageWriter.DefaultMapMessageWriter.class)) {
			return MapMessageWriter.instance();
		} else if (writerClass.equals(ObjectMessageWriter.DefaultObjectMessageWriter.class)) {
			return ObjectMessageWriter.instance();
		}

		return messageWriters.buildWriter(writerClass).orElseThrow(() -> new MissingMessageWriterException(
				"Cannot instantiate writer class " + writerClass));
	}

	private MessageSender buildSenderProxy(String destination, SendOptions sendOptions) {
		Instance<SenderService> service = senderService;
		return MessageSender.class.cast(Proxy.newProxyInstance(getClass().getClassLoader(), SENDER_TYPE,
				new MessageSenderInvocationHandler(() -> service.get().createSender(destination, sendOptions))));
	}

	/**
	 * Invocation handler used in MessageSender proxy instances
	 */
	private static class MessageSenderInvocationHandler implements InvocationHandler {

		private final Supplier<MessageSender> senderSupplier;

		MessageSenderInvocationHandler(Supplier<MessageSender> senderSupplier) {
			this.senderSupplier = senderSupplier;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// create new sender service for each method invocation
			// this is needed in order for the JMS transaction management to work properly with CDI integration
			// we does not know when the transaction is closed and this here should be removed as well
			MessageSender sender = senderSupplier.get();
			try {
				return method.invoke(sender, args);
			} catch (InvocationTargetException e) { // NOSONAR
				// reflections api does not throw the original exception directly but wraps it in
				// InvocationTargetException. So we just unwrap it and throw the original exception
				throw e.getTargetException();
			}
		}
	}
}
