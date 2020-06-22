package com.sirma.sep.content.jms;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.sep.content.ContentEntity;
import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.DeleteContentData;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMS listener for messages at {@value ContentDestinations#DELETE_CONTENT_QUEUE}. The observer performs actual content
 * delete for the described content files.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/01/2018
 */
@Singleton
class DeleteContentQueueHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@DestinationDef
	private static final String QUEUE_DEF = ContentDestinations.DELETE_CONTENT_QUEUE;

	@Inject
	private ContentStoreProvider contentStoreProvider;
	@Inject
	private ContentEntityDao contentEntityDao;
	@Inject
	private SecurityContextManager securityContextManager;

	@QueueListener(value = QUEUE_DEF)
	void onDeleteContent(Message message) throws JMSException {
		String body = message.getBody(String.class);
		DeleteContentData data = DeleteContentData.fromJsonString(body);

		LOGGER.info("Received async content deletion request for {}", data.getContentId());

		if (data.isDeleteContent()) {
			String storeName = data.getStoreName();
			// if the store is not found this should trigger message to be send to the DLQ
			// in the case that the store is removed or something else is changed we may have a logic that handles the
			// change in the store
			ContentStore store = contentStoreProvider.findStore(storeName)
					.orElseThrow(() -> new IllegalStateException("Could not load content store " + storeName));
			// do actual content deletion
			LOGGER.debug("Deleting from {} file contents of {}", storeName, data.getContentId());
			store.delete(data);
		}

		// delete entity if requested
		if (!data.isContentOnly()) {
			securityContextManager.executeAsTenant(data.getTenantId())
					.consumer(this::deleteEntity, data.getContentId());
		}
	}

	private void deleteEntity(String contentId) {
		try {
			LOGGER.debug("Deleting content {} from database", contentId);
			ContentEntity entity = contentEntityDao.getEntity(contentId, "any");
			if (entity != null) {
				contentEntityDao.delete(entity);
			}
		} catch (RuntimeException e) {
			// this may happen if the content database is deleted for example during tenant deletion
			LOGGER.warn("Failed to deleted entity {} from database due to: {}", contentId, e.getMessage());
		}
	}
}
