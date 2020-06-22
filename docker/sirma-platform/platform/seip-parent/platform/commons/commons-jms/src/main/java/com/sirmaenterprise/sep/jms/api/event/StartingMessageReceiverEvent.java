package com.sirmaenterprise.sep.jms.api.event;

import com.sirma.itt.seip.event.EmfEvent;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;
import com.sirmaenterprise.sep.jms.api.QueueReceiverDefinition;

/**
 * Event filred for each {@link QueueReceiverDefinition} that will be started for listening. The observers could
 * modified the message consumer in order to add additional logic)
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class StartingMessageReceiverEvent implements EmfEvent {
	private final ReceiverDefinition receiverDefinition;
	private MessageConsumer messageConsumer;

	/**
	 * Initialize new event with the definition that will be started for listening.
	 *
	 * @param receiverDefinition the definition to notify for
	 */
	public StartingMessageReceiverEvent(ReceiverDefinition receiverDefinition) {
		this.receiverDefinition = receiverDefinition;
		messageConsumer = receiverDefinition.getMessageConsumer();
	}

	public ReceiverDefinition getReceiverDefinition() {
		return receiverDefinition;
	}

	public MessageConsumer getMessageConsumer() {
		return messageConsumer;
	}

	public void setMessageConsumer(MessageConsumer messageConsumer) {
		this.messageConsumer = messageConsumer;
	}
}
