package com.sirma.sep.content.extract;

import static com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants.INSTANCE_ID;
import static com.sirma.sep.content.jms.ContentCommunicationConstants.CONTENT_ID;
import static com.sirma.sep.content.jms.ContentCommunicationConstants.PURPOSE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.rest.exceptions.HTTPClientRuntimeException;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotFoundRuntimeException;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;

/**
 * Listener for content events to extract the text content and persist it to the content instance using the
 * {@link ContentExtractor}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/07/2018
 */
@ApplicationScoped
public class ContentExtractHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@DestinationDef
	private static final String DELAYED_QUEUE_DEF = "java:/jms.queue.DelayedContentExtract";
	private static final String RETRY_COUNT = "retryCount";
	private static final int MAX_DELAYED_RETRIES = 5;

	private static final String SELECTOR = PURPOSE + " = '" + Content.PRIMARY_CONTENT + "'";

	@Inject
	private InstanceContentService instanceContentService;
	@Inject
	private ContentExtractor contentExtractor;
	@Inject
	private SenderService senderService;

	/**
	 * Extracts the content for specific files. There are cases where the content is not available due to asynchronous
	 * transfer between stores. In that case the processing will be rescheduled for delayed processing by sending
	 * message to backup queue. This is done in order to avoid blocking of the current queue.
	 *
	 * @param contentMessage contains the information needed for the content extraction
	 * @throws JMSException in case of error during the processing of the message
	 */
	@TopicListener(jndi = ContentDestinations.CONTENT_TOPIC, subscription = "contentExtractListener", selector = SELECTOR, txTimeout = 10, timeoutUnit = TimeUnit.MINUTES)
	void onContent(Message contentMessage) throws JMSException {
		String contentId = contentMessage.getStringProperty(CONTENT_ID);
		ContentInfo content = instanceContentService.getContent(contentId, null);
		if (!content.exists()) {
			throw new ContentNotFoundRuntimeException();
		}

		try {
			String instanceId = contentMessage.getStringProperty(INSTANCE_ID);
			contentExtractor.extractAndPersist(instanceId, content);
		} catch (HTTPClientRuntimeException e) {
			LOGGER.warn("Failed to retrieve the file for content-{}. The extract will be reschaduled.", contentId);
			LOGGER.trace("", e);
			sendToDelayedQueue(contentMessage);
		}
	}

	private void sendToDelayedQueue(Message currentMessage) throws JMSException {
		int retryConunt = 0;
		if (currentMessage.propertyExists(RETRY_COUNT)) {
			retryConunt = currentMessage.getIntProperty(RETRY_COUNT);
		}

		if (retryConunt == MAX_DELAYED_RETRIES) {
			LOGGER.info("Reached max retries for delayed content extraction for instance - {}."
							+ " The actual file for content with id - {} might not exist.",
					currentMessage.getStringProperty(INSTANCE_ID), currentMessage.getStringProperty(CONTENT_ID));
			return;
		}

		Map<String, Serializable> attributes = CollectionUtils.createHashMap(3);
		attributes.put(CONTENT_ID, currentMessage.getStringProperty(CONTENT_ID));
		attributes.put(INSTANCE_ID, currentMessage.getStringProperty(INSTANCE_ID));
		attributes.put(RETRY_COUNT, ++retryConunt);
		senderService.sendObject(DELAYED_QUEUE_DEF, attributes, ContentExtractHandler::enrichMessage,
				SendOptions.create().delayWith(30, TimeUnit.MINUTES));
	}

	// the next commit will removed this
	public static void enrichMessage(Map<String, Serializable> attributes, Message message) {
		attributes.forEach((k, v) -> {
			try {
				message.setObjectProperty(k, v);
			} catch (JMSException e) {
				throw new JmsRuntimeException("An error occurred while entiching the message headers.", e);
			}
		});
	}

	/**
	 * Handles the processing of the rescheduled content extractions.
	 *
	 * @param message contains the information needed for the content extraction
	 * @throws JMSException in case of error during the processing of the message
	 */
	@QueueListener(value = DELAYED_QUEUE_DEF, txTimeout = 10, timeoutUnit = TimeUnit.MINUTES)
	void delayedQueueProcessor(Message message) throws JMSException {
		onContent(message);
	}
}