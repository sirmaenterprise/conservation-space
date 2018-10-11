package com.sirmaenterprise.sep.jms.api;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Objects;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Topic;

import com.sirmaenterprise.sep.jms.annotations.TopicListener;

/**
 * {@link ReceiverDefinition} that wraps the {@link TopicListener} annotation and uses it to
 * translate the annotations data as receiver definition.
 *
 * @author nvelkov
 */
public class TopicReceiverDefinition implements ReceiverDefinition {

	private final TopicListener topicDefinition;
	private final MessageConsumer messageConsumer;
	private final int transactionTimeout;

	/**
	 * Initialize new JMS receiver definition to report the information about the given
	 * {@link TopicListener} annotation and the given {@link MessageConsumer} as message receiver
	 * target.
	 *
	 * @param topicDefinition
	 *            the receiver definition source
	 * @param messageConsumer
	 *            the message consumer to be invoked upon message delivery on the specified
	 *            destination
	 */
	public TopicReceiverDefinition(TopicListener topicDefinition, MessageConsumer messageConsumer) {
		this.topicDefinition = Objects.requireNonNull(topicDefinition, "Annotation definition is required");
		this.messageConsumer = Objects.requireNonNull(messageConsumer, "Message consumer is required");
		transactionTimeout = (int) topicDefinition.timeoutUnit().toSeconds(topicDefinition.txTimeout());
	}

	/**
	 * The full destination name. The specified name will be looked up via Initial Context and will
	 * be used for message receiving
	 *
	 * @return the destination name to bind to.
	 */
	@Override
	public String getDestinationJndi() {
		return topicDefinition.jndi();
	}

	/**
	 * Optional message selector to be used when reading messages. For more information check
	 * {@link javax.jms.Message} documentation
	 *
	 * @return the selector query
	 * @see javax.jms.Message
	 */
	@Override
	public String getSelector() {
		return topicDefinition.selector();
	}

	/**
	 * The registered message consumer to be called for the given receiver destination.
	 *
	 * @return the consumer instance
	 */
	@Override
	public MessageConsumer getMessageConsumer() {
		return messageConsumer;
	}

	@Override
	public JMSConsumer createConsumer(Destination destination, JMSContext jmsContext) {
		if (topicDefinition.durable()) {
			if (topicDefinition.concurrencyLevel() == 1) {
				return jmsContext.createDurableConsumer((Topic) destination, topicDefinition.subscription(), getSelector(), false);
			}
			return jmsContext.createSharedDurableConsumer((Topic) destination, topicDefinition.subscription(), getSelector());
		}
		return jmsContext.createSharedConsumer((Topic) destination, topicDefinition.subscription(), getSelector());
	}

	@Override
	public int getTransactionTimeout() {
		return transactionTimeout;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("TopicReceiverDefinition{");
		sb.append("topicDefinition=").append(topicDefinition);
		sb.append(", messageConsumer=").append(messageConsumer);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TopicReceiverDefinition)) {
			return false;
		}

		TopicReceiverDefinition that = (TopicReceiverDefinition) o;
		return nullSafeEquals(getDestinationJndi(), that.getDestinationJndi())
				&& nullSafeEquals(getSelector(), that.getSelector())
				&& nullSafeEquals(topicDefinition.subscription(), that.topicDefinition.subscription());
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + getDestinationJndi().hashCode();
		result = PRIME * result + getSelector().hashCode();
		result = PRIME * result + topicDefinition.selector().hashCode();
		return result;
	}

	@Override
	public int getConcurrenceLevel() {
		return topicDefinition.concurrencyLevel();
	}
}
