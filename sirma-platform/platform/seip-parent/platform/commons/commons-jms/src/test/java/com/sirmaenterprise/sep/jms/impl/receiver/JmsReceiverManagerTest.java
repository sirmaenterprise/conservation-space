package com.sirmaenterprise.sep.jms.impl.receiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import javax.jms.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;
import com.sirmaenterprise.sep.jms.api.MessageReceiver;
import com.sirmaenterprise.sep.jms.api.QueueReceiverDefinition;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;
import com.sirmaenterprise.sep.jms.impl.MethodMessageConsumer;

/**
 * Test for {@link JmsReceiverManager}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class JmsReceiverManagerTest {

	@InjectMocks
	private JmsReceiverManager receiverManager;

	@Spy
	private ConfigurationProperty<Integer> maxConcurrentThreads = new ConfigurationPropertyMock<>(5);

	@Mock
	private MessageReceiverStore messageReceiverStore;
	@Spy
	private Statistics statistics = Statistics.NO_OP;
	@Mock
	private EventService eventService;
	@Mock
	private MessageReceiver receiver;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(messageReceiverStore.getReceiver(any())).thenReturn(receiver);
	}

	@After
	public void tearDown() throws Exception {
		receiverManager.shutdown();
	}

	@Test
	public void registerJmsListener() throws Exception {

		mockPassThroghMessageConsumer();

		MessageConsumer consumer1 = mock(MessageConsumer.class);
		MessageConsumer consumer2 = mock(MessageConsumer.class);

		int added = receiverManager.registerJmsListener(mockDefinition("validListener", consumer1));
		assertEquals(1, added);
		added = receiverManager.registerJmsListener(mockDefinition("failingListener", consumer2));
		assertEquals(2, added);

		verify(consumer1, never()).accept(any(), any());
		verify(consumer2, never()).accept(any(), any());
	}

	private ReceiverDefinition mockDefinition(String methodName, MessageConsumer consumer) throws
																						   NoSuchMethodException {
		Method method = getClass().getDeclaredMethod(methodName, javax.jms.Message.class);
		MessageConsumer aConsumer = consumer != null ? consumer : new MethodMessageConsumer(method);
		return new QueueReceiverDefinition(method.getAnnotation(QueueListener.class), aConsumer);
	}

	@Test
	public void startShouldStartAllRegisteredListeners() throws Exception {
		mockPassThroghMessageConsumer();

		MessageConsumer consumer1 = mock(MessageConsumer.class);
		MessageConsumer consumer2 = mock(MessageConsumer.class);

		int added = receiverManager.registerJmsListener(mockDefinition("validListener", consumer1));
		assertEquals(1, added);
		added = receiverManager.registerJmsListener(mockDefinition("failingListener", consumer2));
		assertEquals(2, added);

		receiverManager.start();

		verify(consumer1, timeout(1000).atLeastOnce()).accept(any(), any());
		verify(consumer2, timeout(1000).atLeastOnce()).accept(any(), any());
	}

	private void mockPassThroghMessageConsumer() {
		Message message = mock(Message.class);
		doAnswer(a -> {
			MessageConsumer consumer = a.getArgumentAt(0, MessageConsumer.class);
			consumer.accept(message, null);
			return null;
		}).when(receiver).waitMessage(any(), anyLong());
	}

	@Test
	public void startShouldStartAllNotStartedListenersAfterTheLastStart() throws Exception {
		mockPassThroghMessageConsumer();

		MessageConsumer consumer1 = mock(MessageConsumer.class);
		MessageConsumer consumer2 = mock(MessageConsumer.class);

		int added = receiverManager.registerJmsListener(mockDefinition("validListener", consumer1));
		assertEquals(1, added);

		receiverManager.start();

		verify(consumer1, timeout(1000).atLeastOnce()).accept(any(), any());

		added = receiverManager.registerJmsListener(mockDefinition("failingListener", consumer2));
		assertEquals(2, added);
		receiverManager.start();

		verify(consumer2, timeout(1000).atLeastOnce()).accept(any(), any());
	}

	@Test
	public void restart() throws Exception {
		mockPassThroghMessageConsumer();

		MessageConsumer consumer1 = mock(MessageConsumer.class);

		int added = receiverManager.registerJmsListener(mockDefinition("validListener", consumer1));
		assertEquals(1, added);

		receiverManager.restart();

		verify(consumer1, timeout(1000).atLeastOnce()).accept(any(), any());
	}

	@Test
	public void shutdown() throws Exception {
		mockPassThroghMessageConsumer();

		CountDownLatch latch = new CountDownLatch(1);
		MessageConsumer consumer1 = mock(MessageConsumer.class);
		doAnswer(a -> {
			// notify when the thread is started
			latch.countDown();
			return null;
		}).when(consumer1).accept(any(), any());

		int added = receiverManager.registerJmsListener(mockDefinition("validListener", consumer1));
		assertEquals(1, added);

		receiverManager.start();

		// wait for the thread to start properly
		latch.await();

		receiverManager.shutdown();

		assertEquals("STOPPED", receiverManager.getInfo().getValues().get(0).getReaders().get(0).getStatus());
	}

	@Test
	public void getStatus() throws Exception {
		mockPassThroghMessageConsumer();

		MessageConsumer consumer1 = mock(MessageConsumer.class);

		int added = receiverManager.registerJmsListener(mockDefinition("validListener", consumer1));
		assertEquals(1, added);

		receiverManager.start();

		ReceiversInfo status = receiverManager.getInfo();
		assertNotNull(status);
		assertEquals(1, status.getValues().size());
	}

	@QueueListener("java:/jms.queue.TestQueue")
	static void validListener(Message message) {
		assertNotNull(message);
	}

	@QueueListener(value = "java:/jms.queue.FailingQueue", concurrencyLevel = 2)
	static void failingListener(Message message) {
		fail("Failing message processing");
	}

}
