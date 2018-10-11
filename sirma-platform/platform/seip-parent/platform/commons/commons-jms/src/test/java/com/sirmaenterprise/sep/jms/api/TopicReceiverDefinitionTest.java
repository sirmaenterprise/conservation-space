package com.sirmaenterprise.sep.jms.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.lang.reflect.Method;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Topic;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirmaenterprise.sep.jms.annotations.TopicListener;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/10/2017
 */
public class TopicReceiverDefinitionTest {

	private static final String TOPIC_NAME = "java:/jms.topic.Topic";
	public static final String SELECTOR = "test = value";
	@Mock
	private Topic destination;
	@Mock
	private JMSContext jmsContext;
	@Mock
	private JMSConsumer jmsConsumer;

	@TopicListener(jndi = TOPIC_NAME, subscription = "durableConsumer", selector = SELECTOR)
	private void durableConsumer() {}

	@TopicListener(jndi = TOPIC_NAME, subscription = "durableAndShared", concurrencyLevel = 2, selector = "test = value")
	private void durableAndShared() {}

	@TopicListener(jndi = TOPIC_NAME, subscription = "nonDurable", durable = false, selector = "test = value")
	private void nonDurable() {}

	@BeforeMethod
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(jmsContext.createDurableConsumer(any(), any(), anyString(), eq(false))).thenReturn(jmsConsumer);
		when(jmsContext.createSharedDurableConsumer(any(), any(), anyString())).thenReturn(jmsConsumer);
		when(jmsContext.createSharedConsumer(any(), any(), anyString())).thenReturn(jmsConsumer);

	}

	private TopicReceiverDefinition createDefinition(String methodName) throws NoSuchMethodException {
		Method method = this.getClass().getDeclaredMethod(methodName);
		TopicListener listener = method.getAnnotation(TopicListener.class);
		return new TopicReceiverDefinition(listener, mock(MessageConsumer.class));
	}

	@Test
	public void testCreateConsumer_shouldCreateDurableConsumer_whenDurableAndSingleInstance() throws Exception {
		JMSConsumer consumer = createDefinition("durableConsumer").createConsumer(destination, jmsContext);
		assertNotNull(consumer);
		verify(jmsContext).createDurableConsumer(destination, "durableConsumer", SELECTOR, false);
	}

	@Test
	public void testCreateConsumer_shouldCreateShredDurableConsumer_whenDurableAndMultiInstance() throws Exception {
		JMSConsumer consumer = createDefinition("durableAndShared").createConsumer(destination, jmsContext);
		assertNotNull(consumer);
		verify(jmsContext).createSharedDurableConsumer(destination, "durableAndShared", SELECTOR);
	}

	@Test
	public void testCreateConsumer_shouldCreateNonDurableConsumer_whenNotDurable() throws Exception {
		JMSConsumer consumer = createDefinition("nonDurable").createConsumer(destination, jmsContext);
		assertNotNull(consumer);
		verify(jmsContext).createSharedConsumer(destination, "nonDurable", SELECTOR);
	}
}
