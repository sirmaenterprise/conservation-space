package com.sirma.itt.seip.mail;

import java.util.Collection;

import com.sirma.itt.seip.mail.attachments.MailAttachment;

/**
 * Contains service methods for email sending.
 *
 * @author Adrian Mitev
 */
public interface MailService {

	/**
	 * Sends a mail message.
	 *
	 * @param message
	 *            message to send.
	 */
	void sendMessage(MailMessage message);

	/**
	 * Sends a mail message to a single recipient.
	 *
	 * @param recipient
	 *            mail recipient.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message
	 *            mail content.
	 * @param attachments
	 *            represents files/contents to be attached to a mail message.
	 */
	void sendMessage(String recipient, String subject, String mailGroupId, String message,
			MailAttachment... attachments);

	/**
	 * Sends a mail message to multiple recipients.
	 *
	 * @param recipients
	 *            mail recipients.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message
	 *            mail content.
	 * @param attachments
	 *            represents files/contents to be attached to a mail message.
	 */
	void sendMessage(Collection<String> recipients, String subject, String mailGroupId, String message,
			MailAttachment... attachments);

	/**
	 * Sends a mail message to multiple recipients.
	 *
	 * @param recipients
	 *            mail recipients.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message
	 *            mail content.
	 * @param from
	 *            is the user initiated the mail
	 * @param attachments
	 *            represents files/contents to be attached to a mail message.
	 */
	void sendMessage(Collection<String> recipients, String subject, String mailGroupId, String message, String from,
			MailAttachment... attachments);

	/**
	 * Adds a mail message to the queue for async sending.
	 *
	 * @param message
	 *            message to enqueue.
	 */
	void enqueueMessage(MailMessage message);

	/**
	 * Enqueues a message for a single recipient.
	 *
	 * @param recipient
	 *            mail recipient.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message
	 *            mail content.
	 * @param attachments
	 *            represents files/contents to be attached to a mail message.
	 */
	void enqueueMessage(String recipient, String subject, String mailGroupId, String message,
			MailAttachment... attachments);

	/**
	 * Enqueues a message to multiple recipients.
	 *
	 * @param recipients
	 *            mail recipients.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message
	 *            mail content.
	 * @param attachments
	 *            represents files/contents to be attached to a mail message.
	 */
	void enqueueMessage(Collection<String> recipients, String subject, String mailGroupId, String message,
			MailAttachment... attachments);
	
	/**
	 * Enqueues a message to multiple recipients.
	 *
	 * @param recipients         mail recipients.
	 * @param cc 				 the "Cc" (carbon copy) recipients.
	 * @param subject            mail subject.
	 * @param mailGroupId        the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message            mail content.
	 * @param attachments        represents files/contents to be attached to a mail message.
	 */
	void enqueueMessage(Collection<String> recipients, Collection<String> cc, String subject, String mailGroupId, String message,
			MailAttachment... attachments);

	/**
	 * Enqueues a mail message to multiple recipients.
	 *
	 * @param recipients
	 *            mail recipients.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message
	 *            mail content.
	 * @param from
	 *            is the user initiated the mail
	 * @param attachments
	 *            represents files/contents to be attached to a mail message.
	 */
	void enqueueMessage(Collection<String> recipients, String subject, String mailGroupId, String message, String from,
			MailAttachment... attachments);
}
