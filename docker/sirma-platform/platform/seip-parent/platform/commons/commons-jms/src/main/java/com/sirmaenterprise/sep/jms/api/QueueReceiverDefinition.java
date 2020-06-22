package com.sirmaenterprise.sep.jms.api;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Objects;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;

import com.sirmaenterprise.sep.jms.annotations.QueueListener;

/**
 * {@link ReceiverDefinition} that wraps the {@link QueueListener} annotation and uses it to translate the
 * annotations data as receiver definition.<br> The definition is unique only by the specified destination JNDI name and
 * the message selector.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/05/2017
 */
public class QueueReceiverDefinition implements ReceiverDefinition {

	private final QueueListener listen;
	private final MessageConsumer messageConsumer;
	private final int transactionTimeout;

	/**
	 * Initialize new JMS receiver definition to report the information about the given {@link QueueListener} annotation
	 * and the given {@link MessageConsumer} as message receiver target.
	 *
	 * @param listen the receiver definition source
	 * @param messageConsumer the message consumer to be invoked upon message delivery on the specified destination
	 */
	public QueueReceiverDefinition(QueueListener listen, MessageConsumer messageConsumer) {
		this.listen = Objects.requireNonNull(listen, "Annotation definition is required");
		this.messageConsumer = Objects.requireNonNull(messageConsumer, "Message consumer is required");
		transactionTimeout = (int) listen.timeoutUnit().toSeconds(listen.txTimeout());
	}

	/**
	 * The full destination name. The specified name will be looked up via Initial Context and will be used for
	 * message receiving
	 *
	 * @return the destination name to bind to.
	 */
	@Override
	public String getDestinationJndi() {
		return listen.value();
	}

	/**
	 * The maximum concurrent parallel consumers to assign to the given destination.
	 *
	 * @return the concurrency level
	 */
	@Override
	public int getConcurrenceLevel() {
		return listen.concurrencyLevel();
	}

	/**
	 * Optional message selector to be used when reading messages. For more information check {@link Message}
	 * documentation
	 *
	 * @return the selector query
	 * @see Message
	 */
	@Override
	public String getSelector() {
		return listen.selector();
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
		return jmsContext.createConsumer(destination, getSelector());
	}

	@Override
	public int getTransactionTimeout() {
		return transactionTimeout;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof QueueReceiverDefinition)) {
			return false;
		}

		ReceiverDefinition that = (ReceiverDefinition) o;
		return nullSafeEquals(getDestinationJndi(), that.getDestinationJndi()) &&
				nullSafeEquals(getSelector(), that.getSelector());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getDestinationJndi().hashCode();
		result = prime * result + getSelector().hashCode();
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("QueueReceiverDefinition{");
		sb.append("listen=").append(listen);
		sb.append(", messageConsumer=").append(messageConsumer);
		sb.append('}');
		return sb.toString();
	}
}
