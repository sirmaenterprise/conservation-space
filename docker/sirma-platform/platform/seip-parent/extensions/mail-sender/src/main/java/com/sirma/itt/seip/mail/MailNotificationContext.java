package com.sirma.itt.seip.mail;

import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.resources.Resource;

/**
 * The class is the delegate that provides the information to build model dynamically.
 */
public interface MailNotificationContext {

	/**
	 * Constant for empty attachments.
	 */
	MailAttachment[] EMPTY_ATTACHMENTS = {};

	/**
	 * Gets a collection of strings representing the users mails, to which will be send mails.
	 *
	 * @return collection of user mails
	 */
	Collection<String> getSendTo();

	/**
	 * Gets the send from resource. By default it will return null.
	 *
	 * @return the send from user or null
	 */
	default Resource getSendFrom() {
		return null;
	}

	/**
	 * Gets the subject in the sent mail
	 *
	 * @return the subject
	 */
	String getSubject();

	/**
	 * Gets the model that populates the ftl templates in runtime.
	 *
	 * @return the model holding the needed keys/values
	 */
	Map<? extends String, ? extends Object> getModel();

	/**
	 * Gets the template to load. It is the name of the file as<code>notification.ftl</code>.
	 *
	 * @return the template id
	 */
	String getTemplateId();

	/**
	 * Gets the attachments to the given mail. By default returns empty array.
	 *
	 * @return array of {@link MailAttachment}s
	 */
	default MailAttachment[] getAttachments() {
		return EMPTY_ATTACHMENTS;
	}

}