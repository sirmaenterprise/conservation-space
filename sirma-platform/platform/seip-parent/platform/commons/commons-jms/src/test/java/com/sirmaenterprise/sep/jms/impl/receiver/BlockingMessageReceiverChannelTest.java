package com.sirmaenterprise.sep.jms.impl.receiver;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.IllegalStateRuntimeException;
import javax.jms.JMSContext;
import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;
import com.sirmaenterprise.sep.jms.api.MessageReceiver;
import com.sirmaenterprise.sep.jms.api.MessageReceiverResponse;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;

/**
 * Test for {@link BlockingMessageReceiverChannel}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class BlockingMessageReceiverChannelTest {

	private BlockingMessageReceiverChannel channel;

	@Mock
	private MessageReceiver messageReceiver;
	@Mock
	private ReceiverDefinition receiverDefinition;
	@Mock
	private MessageConsumer messageConsumer;
	@Spy
	private Statistics statistics = Statistics.NO_OP;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(receiverDefinition.getDestinationJndi()).thenReturn("testQueue");

		channel = new BlockingMessageReceiverChannel(receiverDefinition, messageReceiver, messageConsumer, statistics);
	}

	@Test
	public void run_shouldWaitSomeTimeForMessages() throws Exception {
		AtomicBoolean flag = new AtomicBoolean(true);
		doAnswer(a -> {
			// run once and fail not to block forever
			if (flag.compareAndSet(true, false)) {
				a.getArgumentAt(0, MessageConsumer.class).accept(mock(Message.class), mock(JMSContext.class));
				return null;
			}
			throw new IllegalStateRuntimeException("");
		}).when(messageReceiver).waitMessage(any(), anyLong());

		channel.run();
		verify(messageReceiver, times(2)).waitMessage(any(), anyLong());
		verify(messageReceiver, never()).readMessage(any());
		verify(messageReceiver, never()).waitMessage(any());

		verify(messageConsumer).accept(any(), any());

		assertEquals("FAILED", channel.getStatus());
	}

	@Test
	public void shouldUpdateStatisticsOnNewMessages() throws Exception {
		AtomicBoolean flag = new AtomicBoolean(true);
		doAnswer(a -> {
			// run once and fail not to block forever
			if (flag.compareAndSet(true, false)) {
				a.getArgumentAt(0, MessageConsumer.class).accept(mock(Message.class), mock(JMSContext.class));
				return null;
			}
			throw new IllegalStateRuntimeException("Connection closed");
		}).when(messageReceiver).waitMessage(any(), anyLong());

		channel.run();

		verify(statistics).updateMeter(any(), eq("testqueue"));
	}
	
	@Test
	public void shutdown_ShouldStopProcessing() throws Exception {

		doAnswer(a -> {
			a.getArgumentAt(0, MessageConsumer.class).accept(mock(Message.class), mock(JMSContext.class));
			return null;
		}).when(messageReceiver).waitMessage(any(), anyLong());

		CountDownLatch latch = new CountDownLatch(1);
		Thread thread = new Thread(() -> {
			latch.countDown();
			channel.run();
		});
		thread.start();

		// wait for the other thread to start
		latch.await(5, TimeUnit.SECONDS);

		thread.interrupt();
		channel.shutdown();

		assertEquals("STOPPED", channel.getStatus());
	}

	@Test
	public void should_waitOnDestinationTimeout() throws InterruptedException {
		when(messageReceiver.waitMessage(any(MessageConsumer.class), anyLong())).thenReturn(MessageReceiverResponse.FAILED_RECEIVING);

		CountDownLatch latch = new CountDownLatch(1);
		Thread thread = new Thread(() -> {
			latch.countDown();
			channel.run();
		});
		thread.start();

		// wait for the other thread to start
		latch.await(5, TimeUnit.SECONDS);

		thread.interrupt();
		channel.shutdown();
		assertEquals("STOPPED", channel.getStatus());
	}
}
