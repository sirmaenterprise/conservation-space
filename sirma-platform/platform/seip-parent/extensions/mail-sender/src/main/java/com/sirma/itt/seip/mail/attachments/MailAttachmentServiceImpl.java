package com.sirma.itt.seip.mail.attachments;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.mail.CustomInputStreamDataSouce;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Concrete implementation of {@link MailAttachmentService}. Contains logic that builds mail body parts for the mail
 * attachments. Here should go all the logic related to {@link MailAttachment}s.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class MailAttachmentServiceImpl implements MailAttachmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "mail.attachment.maxSize", type = Long.class, defaultValue = "15", label = "The max size of the mail attachments in MB.")
	private ConfigurationProperty<Long> maxAttachmentSize;

	/**
	 * Construct and return {@link MimeBodyPart}. For the content are used two types of {@link DataSource}s. When the
	 * attachments have content id the used data source is {@link CustomInputStreamDataSouce} and the information about
	 * it is extract, using the {@link InstanceContentService}. When for the attachment is set content the data source
	 * is {@link ByteArrayDataSource}. If the attachment does not have name for some reason, it is automatically
	 * generated.
	 * <p>
	 * This method returns <b>null</b> when: <br />
	 * - the attachment does not have content id or content set <br />
	 * - the mime type for the attachment content can't be extracted or resolved <br />
	 * - the passed attachment is null <br />
	 * - the size of the attachment content is over the max attachment size (check the system configuration about it)
	 * </p>
	 */
	@Override
	public MimeBodyPart getAttachmentPart(MailAttachment attachment) throws MessagingException {
		if (attachment == null) {
			LOGGER.debug("The attachment is null.");
			return null;
		}

		return buildBodyPartFromAttachment(attachment);
	}

	/**
	 * Constructs collection of {@link MimeBodyPart}s from the passed attachments. The method check the size for every
	 * attachment and if it is over the max size the attachment is skipped. If some of the attachments does not have
	 * name for some reason, it is automatically generated. If the attachments content is set as content id, the content
	 * is extract, using {@link InstanceContentService}. If the content is set directly in the attachment it is copied
	 * directly. For the {@link DataSource}s are used {@link ByteArrayDataSource}, when the content is passed directly
	 * and {@link CustomInputStreamDataSouce}, when the content is exact by content service.
	 * <p>
	 * This method returns <b>empty collection</b> when: <br />
	 * - the passed input argument is null or empty <br />
	 * - there are problems with the attachments and they can't be add to the collection
	 * </p>
	 *
	 * @see #getAttachmentPart(MailAttachment)
	 */
	@Override
	public Collection<MimeBodyPart> getAttachmentParts(MailAttachment[] attachments) throws MessagingException {
		if (attachments == null || attachments.length == 0) {
			LOGGER.warn("There are no attachments. Empty collection will be return.");
			return Collections.emptyList();
		}

		List<MimeBodyPart> bodyParts = new ArrayList<>();
		for (MailAttachment attachment : attachments) {
			MimeBodyPart bodyPart = getAttachmentPart(attachment);
			if (bodyPart != null) {
				bodyParts.add(bodyPart);
			}
		}

		return bodyParts;
	}

	private MimeBodyPart buildBodyPartFromAttachment(MailAttachment attachment) throws MessagingException {
		DataSource dataSource = null;
		long attachmentMaxSize = getAttachmentMaxSize();

		if (StringUtils.isNotBlank(attachment.getContentId())) {
			dataSource = getContentUsingId(attachment, attachmentMaxSize);
		} else if (attachment.getContent() != null) {
			dataSource = getContentFromAttachment(attachment, attachmentMaxSize);
		}

		if (dataSource == null) {
			LOGGER.debug("No contetn for the attachment with name [{}]. It will be skiped.", attachment.getFileName());
			return null;
		}

		MimeBodyPart attachmentBodyPart = new MimeBodyPart();
		attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
		String fileName = attachment.getFileName();

		if (StringUtils.isBlank(fileName)) {
			fileName = "Generated_name-" + UUID.randomUUID();
		}

		attachmentBodyPart.setFileName(fileName);
		return attachmentBodyPart;
	}

	private long getAttachmentMaxSize() {
		return maxAttachmentSize.get().longValue() * 1024L * 1024L;
	}

	private DataSource getContentUsingId(MailAttachment attachment, long attachmentMaxSize) {
		ContentInfo content = instanceContentService.getContent(attachment.getContentId(), null);
		long contentSize = content.getLength();
		if (contentSize > attachmentMaxSize || contentSize == 0) {
			LOGGER.warn("The attachment [{}] size is over the max allowed or 0. It will be skipped.",
					attachment.getFileName());
			return null;
		}

		String mimeType = content.getMimeType();
		if (StringUtils.isNotBlank(mimeType)) {
			return new CustomInputStreamDataSouce(content::getInputStream, mimeType);
		}
		return null;
	}

	private static DataSource getContentFromAttachment(MailAttachment attachment, long attachmentMaxSize) {
		int contentSize = attachment.getContent().length;
		if (contentSize > attachmentMaxSize || contentSize == 0) {
			LOGGER.warn("The attachment [{}] size is over the max allowed or 0. It will be skipped.",
					attachment.getFileName());
			return null;
		}

		String mimeType = attachment.getMimeType();
		if (StringUtils.isNotBlank(mimeType)) {
			return new ByteArrayDataSource(attachment.getContent(), mimeType);
		}
		return null;
	}

	@Override
	public void deleteMailAttachmentsContent(MailAttachment[] attachments) {
		if (attachments != null && attachments.length > 0) {
			Arrays.asList(attachments).stream().map(MailAttachment::getContentId).filter(Objects::nonNull).forEach(
					id -> instanceContentService.deleteContent(id, null));
		}
	}

}