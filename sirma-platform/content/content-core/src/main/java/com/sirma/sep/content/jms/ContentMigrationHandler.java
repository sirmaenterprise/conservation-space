package com.sirma.sep.content.jms;

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.JsonObject;

import com.sirma.itt.seip.json.JSON;
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

	@DestinationDef
	private static final String QUEUE_DEF = ContentDestinations.MOVE_CONTENT_QUEUE;

	@Inject
	private ContentStoreManagementService storeManagementService;

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
		storeManagementService.moveContent(contentId, targetStore);
		return null;
	}
}
