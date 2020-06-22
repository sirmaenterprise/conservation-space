package com.sirma.itt.seip.mail;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.mail.attachments.MailAttachment;

/**
 * Implements the {@link MailService} methods.
 *
 * @author Adrian Mitev
 */
@Named("mailService")
@ApplicationScoped
class MailServiceImpl implements MailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private MessageSender sender;

	@Inject
	private MailQueue queue;

	@Override
	public void sendMessage(MailMessage message) {
		sender.sendMessage(message);
	}

	@Override
	public void sendMessage(String recipient, String subject, String mailGroupId, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, mailGroupId, new String[] { recipient },
				attachments);
		LOGGER.info("Sending message from system as {} to {}", mailMessage.getFrom(), recipient);
		sender.sendMessage(mailMessage);
	}

	@Override
	public void sendMessage(Collection<String> recipients, String subject, String mailGroupId, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, mailGroupId,
				recipients.toArray(new String[recipients.size()]), attachments);
		LOGGER.info("Sending message from system as {} to {}", mailMessage.getFrom(), recipients);
		sender.sendMessage(mailMessage);
	}

	@Override
	public void sendMessage(Collection<String> recipients, String subject, String mailGroupId, String message,
			String from, MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, mailGroupId, from,
				recipients.toArray(new String[recipients.size()]), attachments);
		LOGGER.info("Sending message from {} to {}", from, recipients);
		sender.sendMessage(mailMessage);
	}

	@Override
	public void enqueueMessage(MailMessage message) {
		queue.enqueueMessage(message);
	}

	@Override
	public void enqueueMessage(String recipient, String subject, String mailGroupId, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, mailGroupId, new String[] { recipient },
				attachments);
		logSystemMessageEnqueue(mailMessage, recipient, null);
		enqueueMessage(mailMessage);
	}

	@Override
	public void enqueueMessage(Collection<String> recipients, String subject, String mailGroupId, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, mailGroupId,
				recipients.toArray(new String[recipients.size()]), attachments);
		logSystemMessageEnqueue(mailMessage, null, recipients);
		enqueueMessage(mailMessage);
	}

	@Override
	public void enqueueMessage(Collection<String> recipients, Collection<String> ccRecipients, String subject, String mailGroupId,
			String message, MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, mailGroupId, recipients.toArray(new String[recipients.size()]),
				ccRecipients.toArray(new String[ccRecipients.size()]), attachments);
		logSystemMessageEnqueue(mailMessage, null, recipients);
		enqueueMessage(mailMessage);
	}

	@Override
	public void enqueueMessage(Collection<String> recipients, String subject, String mailGroupId, String message,
			String from, MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, mailGroupId, from,
				recipients.toArray(new String[recipients.size()]), attachments);
		LOGGER.info("Enqueue message from {} to {}", from, recipients);
		enqueueMessage(mailMessage);
	}

	private static void logSystemMessageEnqueue(MailMessage mailMessage, String recipient, Collection<String> recipients) {
		Object recipientValue = recipient == null ? recipients : recipient;
		String from = mailMessage.getFrom();
		LOGGER.info("Enqueue message from system as {} to {}", from, recipientValue);
	}
}
