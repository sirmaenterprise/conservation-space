package com.sirma.sep.model.management;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * JMS handler for model update events. The handler reads a set of model changes and applies them to the actual model
 * instance one after another. On success sends a notification back the sender to notify him for the completed task
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/08/2018
 */
@Singleton
public class ModelUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private ModelUpdater modelUpdater;
	@Inject
	private ModelManagementService modelManagementService;
	@Inject
	private ModelPersistence modelPersistence;
	@Inject
	private SenderService senderService;

	@QueueListener(ModelPersistence.MODEL_UPDATE_QUEUE)
	void onModelChange(Message message) throws JMSException {
		String requestId = message.getBody(String.class);
		LOGGER.info("Received asynchronous request for model update for request id {}", requestId);
		ModelChanges changes = modelPersistence.readChanges(requestId);

		try {
			Models model = modelManagementService.getModels();
			modelUpdater.actualUpdate(model, changes);
		} finally {
			// no matter the update result we should notify for the change
			// may be we should return status success/fail and what failed
			if (message.getJMSReplyTo() != null) {
				senderService.send(message.getJMSReplyTo(), requestId);
			}
		}
	}
}
