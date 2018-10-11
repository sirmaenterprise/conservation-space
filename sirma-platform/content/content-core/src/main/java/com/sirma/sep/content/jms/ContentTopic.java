package com.sirma.sep.content.jms;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.IdResolver;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.event.ContentAssignedEvent;
import com.sirma.sep.content.event.ContentUpdatedEvent;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;

/**
 * An observer for the content that will put header only messages that expire in 2 weeks in the
 * content topic. The messages contain the following data:
 * </p>
 * <ul>
 * <li>instanceId</li>
 * <li>contentId</li>
 * <li>mimetype</li>
 * <li>purpose</li>
 * </ul>
 * 
 * @author nvelkov
 * @see ContentDestinations
 */
@ApplicationScoped
public class ContentTopic {

	@DestinationDef(type = DestinationType.TOPIC)
	public static final String CONTENT_TOPIC = ContentDestinations.CONTENT_TOPIC;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private SenderService senderService;

	@Inject
	private IdResolver idResolver;

	/**
	 * Observe any {@link ContentAssignedEvent}, extract the content and put it in the content
	 * topic.
	 * 
	 * @param event
	 *            the {@link ContentAssignedEvent}
	 */
	public void onContentAssigned(@Observes ContentAssignedEvent event) {
		ContentInfo info = instanceContentService.getContent(event.getContentId(), null);
		addToTopic(info, event.getInstanceId());
	}

	/**
	 * Observe any {@link ContentUpdatedEvent}, extract the content and put it in the content topic.
	 * <p>
	 * 
	 * @param event
	 *            the {@link ContentUpdatedEvent}
	 */
	public void onContentUpdated(@Observes ContentUpdatedEvent event) {
		Optional<Serializable> instanceId = idResolver.resolve(event.getOwner());
		ContentInfo newContent = event.getNewContent();
		instanceId.ifPresent(id -> addToTopic(newContent, id));
	}

	/**
	 * Add the content to the topic.
	 * 
	 * @param content
	 *            the content
	 * @param instanceId
	 *            the instanceId
	 */
	private void addToTopic(ContentInfo content, Serializable instanceId) {
		Map<String, Serializable> attributes = CollectionUtils.createHashMap(4);
		attributes.put(InstanceCommunicationConstants.INSTANCE_ID, instanceId);
		attributes.put(ContentCommunicationConstants.CONTENT_ID, content.getContentId());
		attributes.put(InstanceCommunicationConstants.MIMETYPE, content.getMimeType());
		attributes.put(ContentCommunicationConstants.PURPOSE, content.getContentPurpose());
		attributes.put(ContentCommunicationConstants.REMOTE_SOURCE_NAME, content.getRemoteSourceName());
		senderService.sendObject(CONTENT_TOPIC, attributes, ContentTopic::enrichMessage,
				SendOptions.create().expireAfter(14, TimeUnit.DAYS).delayWith(5, TimeUnit.SECONDS));
	}

	private static void enrichMessage(Map<String, Serializable> attributes, Message message) {
		for (Entry<String, Serializable> entry : attributes.entrySet()) {
			try {
				message.setObjectProperty(entry.getKey(), entry.getValue());
			} catch (JMSException e) {
				throw new JmsRuntimeException(
						"An error occurred while populating jms message headers in the content topic.", e);
			}
		}
	}
}
