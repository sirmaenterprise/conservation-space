package com.sirmaenterprise.sep.jms.impl.receiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Queue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.jms.api.JmsContextProvider;
import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;
import com.sirmaenterprise.sep.jms.api.MessageConsumerListener;
import com.sirmaenterprise.sep.jms.api.MessageReceiver;
import com.sirmaenterprise.sep.jms.api.MessageReceiverResponse;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;

/**
 * Test for {@link MessageReceiverStore}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/05/2017
 */
public class MessageReceiverStoreTest {

	@InjectMocks
	private MessageReceiverStore receiverStore;

	@Mock
	private JmsContextProvider contextProvider;
	@Mock
	private JmsDestinationResolver destinationResolver;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private JMSConsumer jmsConsumer;
	@Mock
	private JMSContext jmsContext;

	@Mock
	private Message message;

	@Mock
	private MessageConsumerListener listener;

	@Spy
	private InstanceProxyMock<MessageConsumerListener> messageListener = new InstanceProxyMock<>();

	@Mock
	private ReceiverDefinition receiverDefinition;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(destinationResolver.resolveQueue(anyString())).then(a -> mock(Queue.class));

		when(jmsContext.createConsumer(any(Queue.class), anyString())).thenReturn(jmsConsumer);
		when(contextProvider.provide()).thenReturn(jmsContext);
		messageListener.set(listener);
		when(jmsConsumer.receive()).thenReturn(message, null);
		when(jmsConsumer.receiveNoWait()).thenReturn(message, null);
		when(jmsConsumer.receive(1000L)).thenReturn(message, null);

		when(receiverDefinition.createConsumer(any(), any())).thenReturn(jmsConsumer);
		when(receiverDefinition.getDestinationJndi()).thenReturn("testQueue");
	}

	@Test
	public void getReceiver() throws Exception {
		MessageReceiver receiver = receiverStore.getReceiver(receiverDefinition);
		assertNotNull(receiver);
		verify(destinationResolver).resolve("testQueue");
	}

	@Test
	public void receiver_ReadMessage_ShouldCallJmsContextReceiveNoWaitMethod() {
		MessageReceiver receiver = receiverStore.getReceiver(receiverDefinition);
		MessageConsumer consumer = mock(MessageConsumer.class);
		receiver.readMessage(consumer);

		verify(jmsConsumer).receiveNoWait();
		verify(transactionSupport).invokeInNewTx(any(Callable.class), eq(0), eq(TimeUnit.SECONDS));
		verify(listener).beforeMessage(any());
		verify(listener).onSuccess();
	}

	@Test
	public void receiver_WaitMessage_ShouldCallJmsContextReceiveMethod() {
		MessageReceiver receiver = receiverStore.getReceiver(receiverDefinition);
		MessageConsumer consumer = mock(MessageConsumer.class);
		receiver.waitMessage(consumer);

		verify(jmsConsumer).receive();
		verify(transactionSupport, never()).invokeInNewTx(any(Executable.class), anyInt(), any());
		verify(listener).beforeMessage(any());
		verify(listener).onSuccess();
	}

	@Test
	public void receiver_WaitWithTimeoutMessage_ShouldCallJmsContextReceiveWithTimeoutMethod() {
		MessageReceiver receiver = receiverStore.getReceiver(receiverDefinition);
		MessageConsumer consumer = mock(MessageConsumer.class);
		receiver.waitMessage(consumer, 1000L);

		verify(jmsConsumer).receive(eq(1000L));
		verify(transactionSupport).invokeInNewTx(any(Callable.class), eq(0), eq(TimeUnit.SECONDS));
		verify(listener).beforeMessage(any());
		verify(listener).onSuccess();
	}

	@Test
	public void receiver_WaitWithTimeoutMessage_ShouldNotCallListenersIfNoMessageIsReceived() {
		reset(jmsConsumer);

		MessageReceiver receiver = receiverStore.getReceiver(receiverDefinition);
		MessageConsumer consumer = mock(MessageConsumer.class);
		receiver.waitMessage(consumer, 1000L);

		verify(jmsConsumer).receive(eq(1000L));
		verify(transactionSupport).invokeInNewTx(any(Callable.class), eq(0), eq(TimeUnit.SECONDS));
		verify(listener, never()).beforeMessage(any());
		verify(listener, never()).onSuccess();
	}

	@Test
	public void receiver_WaitWithTimeoutMessage_ShouldCallListenersOnErrorMethodIfFailedToConsumeMessage() {
		MessageReceiver receiver = receiverStore.getReceiver(receiverDefinition);
		MessageConsumer consumer = mock(MessageConsumer.class);
		doThrow(RuntimeException.class).when(consumer).accept(any(), any());
		MessageReceiverResponse response = receiver.waitMessage(consumer, 1000L);

		assertEquals(MessageReceiverResponse.FAILED_CONSUMING, response);
		verify(jmsConsumer).receive(eq(1000L));
		verify(transactionSupport).invokeInNewTx(any(Callable.class), eq(0), eq(TimeUnit.SECONDS));
		verify(listener).beforeMessage(any());
		verify(listener).onError(any(RuntimeException.class));
	}
}
