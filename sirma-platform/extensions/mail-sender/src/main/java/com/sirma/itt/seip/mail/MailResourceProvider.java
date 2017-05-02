package com.sirma.itt.seip.mail;

import java.io.Serializable;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * Defines method for extraction information, used in mails and more specific in mail template building.
 *
 * @author A. Kunchev
 */
public interface MailResourceProvider {

	/**
	 * Gets property for the instance. The value is returned in in the user language, if it has value for it.
	 *
	 * @param instance
	 *            the instance from, which the property is extracted
	 * @param user
	 *            the user, used to calculate the language on which we should return the value
	 * @param property
	 *            the searched property
	 * @return the value of the passed property in specific language or null if there is no such property
	 */
	String getDisplayableProperty(Instance instance, Resource user, String property);

	/**
	 * Gets the role of the specific user for given instance.
	 *
	 * @param instance
	 *            the instance for which will be returned the user role
	 * @param user
	 *            the user which role is searched
	 * @return the identifier of the role of the user for the passed instance
	 */
	String getUserRole(Instance instance, Resource user);

	/**
	 * Gets the value for the label with passed id.
	 *
	 * @param labelId
	 *            the id of the label, which value is searched
	 * @return the value of the passed label
	 */
	String getLabel(String labelId);

	/**
	 * Extracts {@link Resource} by passed id and {@link ResourceType}.
	 *
	 * @param id
	 *            the id of the resource
	 * @param type
	 *            the resource type
	 * @return {@link Resource} or null if the resource is not found
	 */
	Resource getResource(String id, ResourceType type);

	/**
	 * Extracts {@link Resource} by passed id.
	 *
	 * @param id
	 *            the id of the resource
	 * @return {@link Resource} or null if the resource is not found
	 */
	Resource getResource(Serializable id);

}
