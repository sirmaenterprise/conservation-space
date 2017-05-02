/**
 * Copyright (c) 2010 16.08.2010 , Sirma ITT.
 */
package com.sirma.itt.seip.mail;

import java.util.Arrays;

import com.sirma.itt.seip.mail.attachments.MailAttachment;

/**
 * Object representing email message.
 *
 * @author B.Bonev
 * @author Adrian Mitev
 */
public class MailMessage {

	private String subject;

	private String content;

	private String from;

	private String[] recipients;

	/**
	 * The "Cc" (carbon copy) recipients.
	 */
	private String[] ccRecipients = new String[0];

	private String mimeFormat;

	private MailAttachment[] attachments;

	private String mailGroupId;

	/**
	 * Getter method for subject.
	 *
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Setter method for subject.
	 *
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Getter method for content.
	 *
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Setter method for content.
	 *
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Getter method for from.
	 *
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Setter method for from.
	 *
	 * @param from
	 *            the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * Getter method for recipients.
	 *
	 * @return the recipients
	 */
	public String[] getRecipients() {
		return recipients;
	}

	/**
	 * Setter method for recipients.
	 *
	 * @param recipients
	 *            the recipients to set
	 */
	public void setRecipients(String[] recipients) {
		if (recipients == null) {
			this.recipients = null;
		} else {
			this.recipients = Arrays.copyOf(recipients, recipients.length);
		}

	}

	/**
	 * @return - the "Cc" (carbon copy) recipients.
	 */
	public String[] getCcRecipients() {
		return ccRecipients;
	}

	/**
	 * Set the "Cc" (carbon copy) recipients.
	 * @param ccRecipients
	 */
	public void setCcRecipients(String[] ccRecipients) {
		if (ccRecipients == null) {
			this.ccRecipients = new String[0];
		} else {
			this.ccRecipients = Arrays.copyOf(ccRecipients, ccRecipients.length);
		}
	}

	/**
	 * Setter method for mimeFormat.
	 *
	 * @param mimeFormat
	 *            the mimeFormat to set
	 */
	public void setMimeFormat(String mimeFormat) {
		this.mimeFormat = mimeFormat;
	}

	/**
	 * Getter method for mimeFormat.
	 *
	 * @return the mimeFormat
	 */
	public String getMimeFormat() {
		return mimeFormat;
	}

	/**
	 * Getter method for attachments.
	 *
	 * @return the attachments
	 */
	public MailAttachment[] getAttachments() {
		return attachments;
	}

	/**
	 * Setter method for attachments.
	 *
	 * @param attachments
	 *            the attachments to set
	 */
	public void setAttachments(MailAttachment[] attachments) {
		if (attachments == null) {
			this.attachments = null;
		} else {
			this.attachments = Arrays.copyOf(attachments, attachments.length);
		}
	}

	/**
	 * Getter for the mail group id, which is used to extract the mails with the same message.
	 * 
	 * @return the mailGroupId
	 */
	public String getMailGroupId() {
		return mailGroupId;
	}

	/**
	 * Setter for the mail group id, which is used to extract the mails with the same message.
	 * 
	 * @param mailGroupId
	 *            the mailGroupId to set
	 */
	public void setMailGroupId(String mailGroupId) {
		this.mailGroupId = mailGroupId;
	}

}
