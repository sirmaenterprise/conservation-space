package com.sirma.sep.ocr.jms;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.LocalStore;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.BytesMessageWriter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Content OCR topic listener responsible for consuming any OCR-able messages from the content
 * topic, adding the actual content to them and putting them in the OCR queue. An OCR-able content
 * is any topic with a primaryContent purpose and mimetype starting with image or application/pdf
 * mimetype.
 *
 * @author nvelkov
 */
@ApplicationScoped
public class ContentOCRQueue {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String APPLICATION_PDF = "application/pdf";

	private static final String OCR_SELECTOR = "purpose = '" + Content.PRIMARY_CONTENT
			+ "' AND (mimetype LIKE 'image%' or mimetype = '" + APPLICATION_PDF + "')";

	@DestinationDef
	public static final String CONTENT_OCR_QUEUE = "java:/jms.queue.OCRQueue";

	@Inject
	private SenderService senderService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ocr.default.language", defaultValue = "eng", sensitive = true, label =
			"Property used to store the default ocr language. The language can be either a single value or a list of "
					+ "comma separated values. For the concrete values, please refer to code list #25.")
	private ConfigurationProperty<String> defaultOcrLanguage;

	/**
	 * Listen on the Content topic for any OCR-able contents and put them in the ocr queue.
	 *
	 * @param message the message
	 * @throws JMSException if the JMS provider fails to get some property value from the incoming message
	 * due to some internal error.
	 */
	@TopicListener(jndi = ContentDestinations.CONTENT_TOPIC, subscription = "ocrListener", selector = OCR_SELECTOR)
	public void onContentAdded(Message message) throws JMSException {
		String contentId = message.getStringProperty(ContentCommunicationConstants.CONTENT_ID);
		String contentPurpose = message.getStringProperty(ContentCommunicationConstants.PURPOSE);
		ContentInfo content = instanceContentService.getContent(contentId, contentPurpose);
		Optional<InstanceReference> instanceReference = instanceTypeResolver.resolveReference(content.getInstanceId());
		if (!instanceReference.isPresent() || !isTypeSupported(instanceReference.get())) {
			LOGGER.trace("Content {} not supported for ocr!", content.getName());
			return;
		}

		LOGGER.info("Content {} fetched from content topic for OCR conversion.", content.getName());
		if (content.exists()) {
			Instance instance = instanceReference.get().toInstance();
			// Create an empty ocr content bound to the instance, so later on when the content is
			// actually ocred, it can be updated to this content id instead of the instance
			// directly. This is done to avoid race conditions with multiple ocr services and
			// multiple incoming ocred contents to the same instance.
			String ocredContentId = createEmptyContent(content, instance.getId());

			// Save one empty ocr content for the latest instance version too, so we can provide
			// proper instance version ocr.
			Serializable instanceVersionId = InstanceVersionService.buildVersionId(instance);
			String ocredVersionContentId = createEmptyContent(content, instanceVersionId);

			Pair<String, String> nameAndExtension = FileUtil.splitNameAndExtension(content.getName());
			Serializable instanceId = (Serializable) message.getObjectProperty(InstanceCommunicationConstants.INSTANCE_ID);
			try (BufferedInputStream contentStream = new BufferedInputStream(content.getInputStream())) {
				senderService.send(CONTENT_OCR_QUEUE, contentStream, SendOptions.create()
						.withWriter(BytesMessageWriter.instance())
						.withProperty(InstanceCommunicationConstants.MIMETYPE, content.getMimeType())
						.withProperty(InstanceCommunicationConstants.INSTANCE_ID, instanceId)
						.withProperty(ContentCommunicationConstants.FILE_NAME, nameAndExtension.getFirst())
						.withProperty(ContentCommunicationConstants.FILE_EXTENSION, "." + nameAndExtension.getSecond())
						.withProperty(OCRContentMessageAttributes.OCRED_CONTENT_ID, ocredContentId)
						.withProperty(OCRContentMessageAttributes.OCRED_VERSION_CONTENT_ID, ocredVersionContentId)
						.withProperty(InstanceCommunicationConstants.INSTANCE_VERSION_ID, instanceVersionId)
						.withProperty(OCRContentMessageAttributes.OCR_LANGUAGE, getOcrLanguage(instance)));
			} catch (IOException e) {
				throw new RollbackedRuntimeException("Cannot consume the content stream!", e);
			}
		} else {
			throw new RollbackedRuntimeException("Content " + contentId + " is missing, will try again!");
		}
	}

	private String createEmptyContent(ContentInfo content, Serializable instanceId) {
		ContentImport emptyContent = ContentImport
				.createEmpty()
				.setName(content.getName())
				.setMimeType(content.getMimeType())
				.setPurpose("ocr")
				.setRemoteSourceName(LocalStore.NAME)
				.setInstanceId(instanceId);
		return instanceContentService.importContent(emptyContent);
	}

	private boolean isTypeSupported(InstanceReference instance) {
		InstanceType type = instance.getType();
		if (type == null) {
			LOGGER.warn("Tried ocr-ing content for instance {} with no semantic type.", instance.getId());
			return false;
		}
		// return false if the current class belongs to image class hierarchy. We don't want to ocr
		// images.
		for (String parentType : semanticDefinitionService.getHierarchy(type.getId().toString())) {
			if (parentType.toLowerCase().contains("image")) {
				return false;
			}
		}
		return true;
	}

	private String getOcrLanguage(Instance instance) {
		String ocrLanguage = instance.getString("ocrLanguage");
		if (StringUtils.isBlank(ocrLanguage)) {
			// The string replacement is done this way, because the configuration is intended to be either a single
			// language or a comma separated string of languages. The string is converted to lan1+lan2+lan3 because this
			// is the format expected from the ocr service.
			ocrLanguage = defaultOcrLanguage.get().replaceAll("\\s*,\\s*", "+");
		}
		return ocrLanguage;
	}
}
