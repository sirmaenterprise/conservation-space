package com.sirma.itt.emf.mail;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implements the {@link MailService} methods.
 * 
 * @author Adrian Mitev
 */
@Named("mailService")
@ApplicationScoped
public class MailServiceImpl implements MailService {

	@Inject
	private MessageSender sender;

	@Inject
	private MailQueue queue;

	@Override
	public void sendMessage(MailMessage message) {
		sender.sendMessage(message);
	}

	@Override
	public void sendMessage(String recipient, String subject, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject,
				new String[] { recipient }, attachments);
		sender.sendMessage(mailMessage);
	}

	@Override
	public void sendMessage(Collection<String> recipients, String subject, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject,
				recipients.toArray(new String[recipients.size()]), attachments);
		sender.sendMessage(mailMessage);
	}

	@Override
	public void sendMessage(Collection<String> recipients, String subject, String message,
			String from, MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject, from,
				recipients.toArray(new String[recipients.size()]), attachments);
		sender.sendMessage(mailMessage);
	}

	@Override
	public void enqueueMessage(MailMessage message) {
		queue.enqueueMessage(message);
	}

	@Override
	public void enqueueMessage(String recipient, String subject, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject,
				new String[] { recipient }, attachments);
		enqueueMessage(mailMessage);
	}

	@Override
	public void enqueueMessage(Collection<String> recipients, String subject, String message,
			MailAttachment... attachments) {
		MailMessage mailMessage = sender.constructMessage(message, subject,
				recipients.toArray(new String[recipients.size()]), attachments);
		enqueueMessage(mailMessage);
	}
}
