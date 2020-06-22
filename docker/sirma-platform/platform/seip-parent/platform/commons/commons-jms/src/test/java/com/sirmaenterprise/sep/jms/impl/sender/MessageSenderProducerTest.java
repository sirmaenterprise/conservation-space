package com.sirmaenterprise.sep.jms.impl.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.jms.CompletionListener;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriters;

/**
 * Test for {@link MessageSenderProducer}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 23/05/2017
 */
public class MessageSenderProducerTest {
	@InjectMocks
	private MessageSenderProducer producer;
	@Mock
	private SenderService senderService;
	@Spy
	private InstanceProxyMock<SenderService> senderServiceInstance = new InstanceProxyMock<>();
	@Mock
	private MessageSender messageSender;
	@Mock
	private BeanManager beanManager;
	@Mock
	private InjectionPoint point;
	@Mock
	private Annotated annotated;
	@Mock
	private Bean<?> bean;
	@Spy
	private MessageWriters messageWriters;

	@JmsSender(destination = "destinationQueue", writer = CustomWriter.class, async = CustomListener.class,
			replyTo = "replyQueue",
			timeToLive = 1000, jmsType = "testMessage", priority = 8, deliveryDelay = 2000,
			persistent = false)
	MessageSender sender;

	@JmsSender(destination = "destinationQueue")
	MessageSender defaultSender;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		senderServiceInstance.set(senderService);

		when(senderService.createSender(anyString(), any(SendOptions.class))).thenReturn(messageSender);
		when(point.getAnnotated()).thenReturn(annotated);

		when(beanManager.getBeans(any(), any())).thenReturn(Collections.singleton(bean));
		when(beanManager.resolve(anySet())).then(a -> a.getArgumentAt(0, Set.class).iterator().next());

		when(beanManager.getReference(any(Bean.class), eq(CustomWriter.class), any())).thenReturn(new CustomWriter());
		when(beanManager.getReference(any(Bean.class), eq(CustomListener.class), any())).thenReturn(
				new CustomListener());
	}

	@Test
	public void produceWithEverythingSet() throws Exception {
		when(annotated.getAnnotation(JmsSender.class)).then(a -> MessageSenderProducerTest.class.getDeclaredField
				("sender").getAnnotation(JmsSender.class));

		when(messageSender.isActive()).thenReturn(Boolean.TRUE);

		MessageSender produce = producer.produce(point, beanManager);
		assertNotNull(produce);
		assertTrue(produce.isActive());

		verify(senderService).createSender(eq("destinationQueue"), argThat(CustomMatcher.of((SendOptions
				options) -> {
			assertEquals("replyQueue", options.getReplyTo());
			assertEquals(1000, options.getTimeToLive());
			assertEquals("testMessage", options.getJmsType());
			assertEquals(8, options.getPriority());
			assertEquals(2000, options.getDeliveryDelay());
			assertEquals(DeliveryMode.NON_PERSISTENT, options.getDeliveryMode());
			assertTrue(options.getWriter() instanceof CustomWriter);
			assertTrue(options.getCompletionListener() instanceof CustomListener);
		})));
	}

	@Test
	public void produceUsingDefault() throws Exception {
		when(annotated.getAnnotation(JmsSender.class)).then(a -> MessageSenderProducerTest.class.getDeclaredField
				("defaultSender").getAnnotation(JmsSender.class));

		when(messageSender.isActive()).thenReturn(Boolean.TRUE);

		MessageSender produce = producer.produce(point, beanManager);
		assertNotNull(produce);
		assertTrue(produce.isActive());

		verify(senderService).createSender(eq("destinationQueue"), argThat(CustomMatcher.of((SendOptions
				options) -> {
			assertNull(options.getReplyTo());
			assertEquals(Message.DEFAULT_TIME_TO_LIVE, options.getTimeToLive());
			assertNull(options.getJmsType());
			assertEquals(Message.DEFAULT_PRIORITY, options.getPriority());
			assertEquals(Message.DEFAULT_DELIVERY_DELAY, options.getDeliveryDelay());
			assertEquals(Message.DEFAULT_DELIVERY_MODE, options.getDeliveryMode());
			assertNull(options.getWriter());
			assertNull(options.getCompletionListener());
		})));
	}

	private static class CustomWriter implements MessageWriter<Object, Message> {

		@Override
		public Message write(Object data, JMSContext context) throws JMSException {
			return null;
		}
	}

	private static class CustomListener implements CompletionListener {

		@Override
		public void onCompletion(Message message) {

		}

		@Override
		public void onException(Message message, Exception exception) {

		}
	}
}
