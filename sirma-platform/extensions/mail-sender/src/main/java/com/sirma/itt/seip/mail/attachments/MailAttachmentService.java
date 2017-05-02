package com.sirma.itt.seip.mail.attachments;

import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

/**
 * Defines methods for processing mail attachments.
 *
 * @author A. Kunchev
 */
public interface MailAttachmentService {

	/**
	 * Creates {@link MimeBodyPart} out of the passed {@link MailAttachment}. Primary used in mails building process.
	 *
	 * @param attachment
	 *            the attachment from which will be build body part
	 * @return {@link MimeBodyPart} or null if the attachment missing some information
	 * @throws MessagingException
	 */
	MimeBodyPart getAttachmentPart(MailAttachment attachment) throws MessagingException;

	/**
	 * Creates collection of {@link MimeBodyPart} from the passed {@link MailAttachment}s. Also constrains the size of
	 * the attachments. The max size of the attachment is configurable (check system configurations). Primary used in
	 * mails building process.
	 *
	 * @param attachments
	 *            array of attachments for which will be build body parts
	 * @return collection of {@link MimeBodyPart}. Note that if the size of the attachments is too big, they will not be
	 *         returned in the result. Also may return empty collection, if there is a problem with the passed
	 *         attachments.
	 * @throws MessagingException
	 */
	Collection<MimeBodyPart> getAttachmentParts(MailAttachment[] attachments) throws MessagingException;

	/**
	 * Deletes the mail attachments stored content, if for the attachments are set content id.
	 *
	 * @param attachments
	 *            the attachments which content should be deleted
	 */
	void deleteMailAttachmentsContent(MailAttachment[] attachments);

}
