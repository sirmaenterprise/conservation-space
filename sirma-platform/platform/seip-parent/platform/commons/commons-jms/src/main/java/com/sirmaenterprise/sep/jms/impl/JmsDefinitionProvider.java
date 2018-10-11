package com.sirmaenterprise.sep.jms.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Resource;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Queue;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;
import com.sirmaenterprise.sep.jms.api.QueueReceiverDefinition;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;
import com.sirmaenterprise.sep.jms.api.TopicReceiverDefinition;
import com.sirmaenterprise.sep.jms.provision.DestinationDefinition;

/**
 * Collects and provides the JMS definition annotation used for the automatic activation of receivers and Queue/Topic
 * provisioning.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/05/2017
 */
public class JmsDefinitionProvider implements Extension {

	private Set<ReceiverDefinition> definitions = new HashSet<>();

	private Map<String, DestinationDefinition> addressToDefinition = CollectionUtils.createHashMap(32);

	/**
	 * Check all methods for {@link QueueListener} and {@link TopicListener} annotations and
	 * register {@link QueueReceiverDefinition} or {@link TopicReceiverDefinition} for them
	 *
	 * @param processAnnotatedType
	 *            the processed class
	 * @param <T>
	 *            the class type
	 */
	<T> void registerDestination(@Observes ProcessAnnotatedType<T> processAnnotatedType) {
		AnnotatedType<T> type = processAnnotatedType.getAnnotatedType();
		Method[] methods = type.getJavaClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getDeclaredAnnotation(QueueListener.class) != null) {
				QueueListener annotation = method.getDeclaredAnnotation(QueueListener.class);
				if (annotation != null && MethodMessageConsumer.isAcceptable(method)) {
					registerQueueReceiver(method, annotation);
				} else {
					throw new IllegalArgumentException("Method " + type.getJavaClass() + "." + method.getName()
							+ " annotated with " + QueueListener.class + " should have argument that accepts "
							+ Message.class + " or any if it's sub types and optionally " + JMSContext.class);
				}
			} else if (method.getDeclaredAnnotation(TopicListener.class) != null) {
				TopicListener annotation = method.getDeclaredAnnotation(TopicListener.class);
				if (annotation != null && MethodMessageConsumer.isAcceptable(method)) {
					registerTopicReceiver(method, annotation);
				} else {
					throw new IllegalArgumentException("Method " + type.getJavaClass() + "." + method.getName()
							+ " annotated with " + TopicListener.class + " should have argument that accepts "
							+ Message.class + " or any if it's sub types and optionally " + JMSContext.class);
				}
			}
		}
	}

	/**
	 * Collect all queue addresses from all fields of type {@link Queue}, annotated with
	 * {@link Resource} and all object annotated with {@link DestinationDef}.
	 *
	 * @param annotatedType
	 *            the annotated type
	 */
	<X> void onAnnotatedType(@Observes ProcessAnnotatedType<X> annotatedType) {
		// Collect all DestinationDef-annotated objects.
		collectFieldsFromType(annotatedType, field -> field.getAnnotation(DestinationDef.class) != null,
				JmsDefinitionProvider::resolveDestinationName);
	}

	private static <X> Pair<String, DestinationDefinition> resolveDestinationName(AnnotatedField<? super X> field) {
		DestinationDef def = field.getAnnotation(DestinationDef.class);
		DestinationDefinition definition = DestinationDefinition.from(def);
		Field javaMember = field.getJavaMember();
		javaMember.setAccessible(true);

		if (StringUtils.isNotBlank(def.value())) {
			return new Pair<>(def.value(), definition);
		}
		if (isValidConstant(javaMember)) {
			try {
				String value = (String) javaMember.get(javaMember.getDeclaringClass());
				if (StringUtils.isNotBlank(value)) {
					definition.setAddress(value);
					return new Pair<>(value, definition);
				}
				throw new IllegalArgumentException("The declared constant " + getFieldPath(javaMember) + " has "
						+ DestinationDef.class.getName() + " annotation but does not have a valid value");
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Cannot access the declared constant " + getFieldPath(javaMember),
						e);
			}
		}
		throw new IllegalArgumentException(
				"The declared field " + getFieldPath(javaMember) + " has " + DestinationDef.class.getName()
						+ " annotation but it's not a constant with a string type that has non blank value");
	}

	private static String getFieldPath(Field javaMember) {
		return javaMember.getDeclaringClass().getCanonicalName() + "." + javaMember.getName();
	}

	private static boolean isValidConstant(Field javaMember) {
		return Modifier.isStatic(javaMember.getModifiers()) && Modifier.isFinal(javaMember.getModifiers())
				&& String.class.equals(javaMember.getType());
	}

	private <X> void collectFieldsFromType(ProcessAnnotatedType<X> annotatedType,
			Predicate<AnnotatedField<? super X>> fieldsFilter,
			Function<AnnotatedField<? super X>, Pair<String, DestinationDefinition>> fieldsMapper) {
		addressToDefinition.putAll(annotatedType
				.getAnnotatedType()
				.getFields()
				.stream()
				.filter(fieldsFilter)
				.map(fieldsMapper)
				.collect(Pair.toMap()));
	}

	private void registerQueueReceiver(Method method, QueueListener listen) {
		ReceiverDefinition definition = new QueueReceiverDefinition(listen, new MethodMessageConsumer(method));
		Optional<ReceiverDefinition> existingDefinition = definitions.stream().filter(def -> def.equals(definition))
				.findFirst();
		if (existingDefinition.isPresent()) {
			throw new IllegalArgumentException("Found more than one queue definition that accepts messages from" + " "
					+ listen.value() + "[" + listen.selector() + "]\n1: " + existingDefinition + "\n2: " + definition);
		}
		definitions.add(definition);
	}

	private void registerTopicReceiver(Method method, TopicListener listen) {
		definitions.add(new TopicReceiverDefinition(listen, new MethodMessageConsumer(method)));
	}

	/**
	 * Provides registered {@link QueueReceiverDefinition}s and the corresponding message consumers
	 * if any
	 *
	 * @return the all collected JMS receiver definitions
	 */
	public Set<ReceiverDefinition> getDefinitions() {
		return definitions;
	}

	/**
	 * Provides registered JMS addresses and their definitions
	 *
	 * @return the all collected JMS addresses
	 */
	public Map<String, DestinationDefinition> getAddresses() {
		return addressToDefinition;
	}
}
