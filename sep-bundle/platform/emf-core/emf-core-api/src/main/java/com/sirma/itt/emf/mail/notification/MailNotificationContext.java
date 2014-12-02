package com.sirma.itt.emf.mail.notification;

import java.util.Collection;
import java.util.Map;

import com.sirma.itt.emf.resources.model.Resource;

/**
 * The class is the delegate that provides the information to build model dynamically.
 */
public interface MailNotificationContext {

	/**
	 * Gets the send to list of users which are used to extract emails from.
	 * 
	 * @return the send to list of users
	 */
	Collection<Resource> getSendTo();

	/**
	 * Gets the send from resource. If null, default is to be used
	 * 
	 * @return the send from user
	 */
	Resource getSendFrom();

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

}