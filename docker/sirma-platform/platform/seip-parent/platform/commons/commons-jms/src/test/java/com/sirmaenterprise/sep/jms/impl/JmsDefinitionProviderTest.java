package com.sirmaenterprise.sep.jms.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;
import com.sirmaenterprise.sep.jms.provision.DestinationDefinition;

/**
 * Test for {@link DefaultJmsContextProvider}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/05/2017
 */
public class JmsDefinitionProviderTest {
	@Mock
	private ProcessAnnotatedType processAnnotatedType;
	@Mock
	private AnnotatedType annotatedType;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void registerDestination() throws Exception {
		when(annotatedType.getJavaClass()).thenReturn(ValidObservers.class);
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		provider.registerDestination(processAnnotatedType);

		Set<ReceiverDefinition> definitions = provider.getDefinitions();
		assertEquals(3, definitions.size());
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unchecked")
	public void registerDestination_ShouldFail_onDuplicateObservers() throws Exception {
		when(annotatedType.getJavaClass()).thenReturn(DuplicateObservers.class);
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		provider.registerDestination(processAnnotatedType);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unchecked")
	public void registerDestination_ShouldFail_InvalidObserverArguments() throws Exception {
		when(annotatedType.getJavaClass()).thenReturn(InvalidArguments.class);
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		provider.registerDestination(processAnnotatedType);
	}

	@DestinationDef("queueName")
	private static final String DESTINATION_DEF = "queueName";
	@DestinationDef(value="topicName", type = DestinationType.TOPIC)
	private static final String DESTINATION_TOPIC_DEF = "topicName";
	@DestinationDef
	private static final String DESTINATION_DEF_DEFAULT_VALUE = "defaultQueueName";
	private static final String CONSTANT = "queueName";
	@DestinationDef
	private static final String EMPTY_CONSTANT = "";
	@Resource(mappedName = "queueName")
	private Queue resource;
	private Queue randomQueue;
	@DestinationDef
	private static String staticField = "queue";
	@DestinationDef
	private final String finalField = "queue";
	@DestinationDef
	private static final Integer INT_FIELD = 42;

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_collectQueueNames_fromDefinedQueues() throws Exception {
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		AnnotatedType annotatedType = mock(AnnotatedType.class);

		AnnotatedField<?> queueFieldWithAnnotation = mock(AnnotatedField.class);
		when(queueFieldWithAnnotation.getBaseType()).thenReturn(Queue.class);
		Resource resourceAnnotation = mock(Resource.class);
		when(resourceAnnotation.mappedName()).thenReturn("queueName");
		when(queueFieldWithAnnotation.getAnnotation(Resource.class)).thenReturn(resourceAnnotation);
		when(queueFieldWithAnnotation.getJavaMember()).thenReturn(this.getClass().getDeclaredField("resource"));

		AnnotatedField<?> stringFieldWithAnnotation = mock(AnnotatedField.class);
		when(stringFieldWithAnnotation.getBaseType()).thenReturn(String.class);
		DestinationDef destinationDefAnnotation = mock(DestinationDef.class);
		when(destinationDefAnnotation.value()).thenReturn("");
		when(destinationDefAnnotation.type()).thenReturn(DestinationType.QUEUE);
		when(stringFieldWithAnnotation.getAnnotation(DestinationDef.class)).thenReturn(destinationDefAnnotation);
		when(stringFieldWithAnnotation.getJavaMember())
				.thenReturn(this.getClass().getDeclaredField("DESTINATION_DEF_DEFAULT_VALUE"));

		AnnotatedField<?> stringConstantWithAnnotation = mock(AnnotatedField.class);
		when(stringConstantWithAnnotation.getBaseType()).thenReturn(String.class);
		DestinationDef destinationDefAnnotation2 = mock(DestinationDef.class);
		when(destinationDefAnnotation2.value()).thenReturn("queueName2");
		when(destinationDefAnnotation2.type()).thenReturn(DestinationType.QUEUE);
		when(stringConstantWithAnnotation.getAnnotation(DestinationDef.class)).thenReturn(destinationDefAnnotation2);
		when(stringConstantWithAnnotation.getJavaMember())
				.thenReturn(this.getClass().getDeclaredField("DESTINATION_DEF"));

		AnnotatedField<?> topicStringConstantWithAnnotation = mock(AnnotatedField.class);
		when(topicStringConstantWithAnnotation.getBaseType()).thenReturn(String.class);
		DestinationDef topicDestinationDef = mock(DestinationDef.class);
		when(topicDestinationDef.value()).thenReturn("topicName");
		when(topicDestinationDef.type()).thenReturn(DestinationType.TOPIC);
		when(topicStringConstantWithAnnotation.getAnnotation(DestinationDef.class)).thenReturn(topicDestinationDef);
		when(topicStringConstantWithAnnotation.getJavaMember())
				.thenReturn(this.getClass().getDeclaredField("DESTINATION_TOPIC_DEF"));

		AnnotatedField<?> anotherField = mock(AnnotatedField.class);
		when(anotherField.getBaseType()).thenReturn(String.class);
		when(anotherField.getJavaMember()).thenReturn(this.getClass().getDeclaredField("CONSTANT"));

		AnnotatedField<?> queueFieldWithoutAnnotation = mock(AnnotatedField.class);
		when(queueFieldWithoutAnnotation.getBaseType()).thenReturn(Queue.class);
		when(queueFieldWithoutAnnotation.getJavaMember()).thenReturn(this.getClass().getDeclaredField("randomQueue"));

		when(annotatedType.getFields())
				.thenReturn(new LinkedHashSet(Arrays.asList(stringFieldWithAnnotation, queueFieldWithoutAnnotation,
						anotherField, topicStringConstantWithAnnotation, stringConstantWithAnnotation)));

		ProcessAnnotatedType<?> processAnnotatedType = mock(ProcessAnnotatedType.class);
		when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

		provider.onAnnotatedType(processAnnotatedType);

		Iterator<Entry<String, DestinationDefinition>> queueAddresses = provider.getAddresses().entrySet().iterator();
		Assert.assertEquals("queueName2", queueAddresses.next().getKey());
		Assert.assertEquals("defaultQueueName", queueAddresses.next().getKey());
		Assert.assertEquals("topicName", queueAddresses.next().getKey());
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_failIfInvalidDefinitionIsUsed() throws Exception {
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		AnnotatedType annotatedType = mock(AnnotatedType.class);

		AnnotatedField<?> stringConstantWithAnnotation = mock(AnnotatedField.class);
		when(stringConstantWithAnnotation.getBaseType()).thenReturn(String.class);
		DestinationDef destinationDef = mock(DestinationDef.class);
		when(destinationDef.value()).thenReturn("");
		when(stringConstantWithAnnotation.getAnnotation(DestinationDef.class))
				.thenReturn(destinationDef);
		when(stringConstantWithAnnotation.getJavaMember()).thenReturn(
				this.getClass().getDeclaredField("EMPTY_CONSTANT"));

		when(annotatedType.getFields()).thenReturn(Collections.singleton(stringConstantWithAnnotation));

		ProcessAnnotatedType<?> processAnnotatedType = mock(ProcessAnnotatedType.class);
		when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

		provider.onAnnotatedType(processAnnotatedType);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_failIfAnnotationPlacedOnNonStaticField() throws Exception {
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		AnnotatedType annotatedType = mock(AnnotatedType.class);

		AnnotatedField<?> stringConstantWithAnnotation = mock(AnnotatedField.class);
		when(stringConstantWithAnnotation.getBaseType()).thenReturn(String.class);
		DestinationDef destinationDef = mock(DestinationDef.class);
		when(destinationDef.value()).thenReturn("");
		when(stringConstantWithAnnotation.getAnnotation(DestinationDef.class))
				.thenReturn(destinationDef);
		when(stringConstantWithAnnotation.getJavaMember()).thenReturn(this.getClass().getDeclaredField("finalField"));

		when(annotatedType.getFields()).thenReturn(Collections.singleton(stringConstantWithAnnotation));

		ProcessAnnotatedType<?> processAnnotatedType = mock(ProcessAnnotatedType.class);
		when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

		provider.onAnnotatedType(processAnnotatedType);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_failIfAnnotationPlacedOnNonFinalField() throws Exception {
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		AnnotatedType annotatedType = mock(AnnotatedType.class);

		AnnotatedField<?> stringConstantWithAnnotation = mock(AnnotatedField.class);
		when(stringConstantWithAnnotation.getBaseType()).thenReturn(String.class);
		DestinationDef destinationDef = mock(DestinationDef.class);
		when(destinationDef.value()).thenReturn("");
		when(stringConstantWithAnnotation.getAnnotation(DestinationDef.class))
				.thenReturn(destinationDef);
		when(stringConstantWithAnnotation.getJavaMember()).thenReturn(this.getClass().getDeclaredField("staticField"));

		when(annotatedType.getFields()).thenReturn(Collections.singleton(stringConstantWithAnnotation));

		ProcessAnnotatedType<?> processAnnotatedType = mock(ProcessAnnotatedType.class);
		when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

		provider.onAnnotatedType(processAnnotatedType);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_failIfAnnotationPlacedOnNonStringConstantField() throws Exception {
		JmsDefinitionProvider provider = new JmsDefinitionProvider();
		AnnotatedType annotatedType = mock(AnnotatedType.class);

		AnnotatedField<?> stringConstantWithAnnotation = mock(AnnotatedField.class);
		when(stringConstantWithAnnotation.getBaseType()).thenReturn(String.class);
		DestinationDef destinationDef = mock(DestinationDef.class);
		when(destinationDef.value()).thenReturn("");
		when(stringConstantWithAnnotation.getAnnotation(DestinationDef.class))
				.thenReturn(destinationDef);
		when(stringConstantWithAnnotation.getJavaMember()).thenReturn(this.getClass().getDeclaredField("INT_FIELD"));

		when(annotatedType.getFields()).thenReturn(Collections.singleton(stringConstantWithAnnotation));

		ProcessAnnotatedType<?> processAnnotatedType = mock(ProcessAnnotatedType.class);
		when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

		provider.onAnnotatedType(processAnnotatedType);
	}

	class ValidObservers {
		@QueueListener("queue1")
		void onMessage(Message message) {
			assertNotNull(message);
		}

		@QueueListener("queue2")
		void onTextMessage(TextMessage message) {
			assertNotNull(message);
		}
		
		@TopicListener(jndi = "topic2", subscription = "id")
		void onTopicMessage(Message message) {
			assertNotNull(message);
		}
	}

	class DuplicateObservers {
		@QueueListener("queue1")
		void onMessage(Message message) {
			assertNotNull(message);
		}

		@QueueListener("queue1")
		void onMessageDuplicate(Message message) {
			assertNotNull(message);
		}

		@QueueListener("queue2")
		void onTextMessage(TextMessage message) {
			assertNotNull(message);
		}
	}

	class InvalidArguments {
		@QueueListener("queue1")
		void onMessage(String message) {
			assertNotNull(message);
		}
	}
}
