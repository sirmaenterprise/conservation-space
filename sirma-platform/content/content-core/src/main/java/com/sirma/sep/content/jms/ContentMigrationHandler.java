package com.sirma.sep.content.jms;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.ContentCorruptedException;
import com.sirma.sep.content.ContentStoreManagementService;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;

/**
 * JMS handler for content migration. Executes a singe content move request persistently. <br>
 * There are 2 queue handlers defined to process instances in parallel instead of single reader with 2 instances of the
 * same reader. This is done so that a single instance and it's different contents to be processed by a single handler
 * and not in parallel, but to allow concurrency of the whole processing. In order to achive this in the send message a
 * special JMS header property (index) should be passed with values 0 or 1. The values can be calculated using the last digit of
 * the instance id. This way all content entries will be processed on the same thread and we will have sort of
 * parallelism.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/04/2018
 */
@ApplicationScoped
public class ContentMigrationHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@DestinationDef(maxRedeliveryAttempts = 5)
	private static final String QUEUE_DEF = ContentDestinations.MOVE_CONTENT_QUEUE;

	@Inject
	private ContentStoreManagementService storeManagementService;
	@Inject
	private TransactionSupport transactionSupport;

	@QueueListener(value = QUEUE_DEF, txTimeout = 1, timeoutUnit = TimeUnit.HOURS, selector = "index = '0'")
	void onMigrateContentEven(Message message) throws JMSException {
		String body = message.getBody(String.class);
		JSON.readObject(body, this::migrateContent);
	}

	@QueueListener(value = QUEUE_DEF, txTimeout = 1, timeoutUnit = TimeUnit.HOURS, selector = "index = '1'")
	void onMigrateContentOdd(Message message) throws JMSException {
		JSON.readObject(message.getBody(String.class), this::migrateContent);
	}

	private Void migrateContent(JsonObject jsonObject) {
		String contentId = jsonObject.getString("contentId");
		String targetStore = jsonObject.getString("targetStore");
		try {
			// wrap the move in a separate transaction so we can control when the message processing fails or not
			// the time is less then the original transaction so we should not fail due to timeout
			transactionSupport.invokeInNewTx(() -> storeManagementService.moveContent(contentId, targetStore), 59,
					TimeUnit.MINUTES);
		} catch (ContentCorruptedException e) {
			// when this is thrown we cannot expect the file to fix it's size on the next retry so just move to other
			LOGGER.warn(e.getMessage());
		}
		return null;
	}
}
