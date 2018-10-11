package com.sirma.sep.content.extract;

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotFoundRuntimeException;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;

/**
 * Listener for content events to extract the text content and persist it to the content instance using the
 * {@link ContentExtractor}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/07/2018
 */
@ApplicationScoped
public class ContentExtractHandler {

	private static final String SELECTOR =
			ContentCommunicationConstants.PURPOSE + " = '" + Content.PRIMARY_CONTENT + "'";
	@Inject
	private InstanceContentService instanceContentService;
	@Inject
	private ContentExtractor contentExtractor;

	@TopicListener(jndi = ContentDestinations.CONTENT_TOPIC, subscription = "contentExtractListener",
			selector = SELECTOR, txTimeout = 10, timeoutUnit = TimeUnit.MINUTES)
	void onContent(Message contentMessage) throws JMSException {
		String contentId = contentMessage.getStringProperty(ContentCommunicationConstants.CONTENT_ID);

		ContentInfo content = instanceContentService.getContent(contentId, null);
		if (!content.exists()) {
			throw new ContentNotFoundRuntimeException();
		}
		String instanceId = contentMessage.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID);
		contentExtractor.extractAndPersist(instanceId, content);
	}

}
