package com.sirma.itt.seip.resources;

import java.util.function.Predicate;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Base interface for resource/user in the application.
 *
 * @author bbanchev
 */
public interface Resource extends Instance, Named {

	/**
	 * Predicate filter that checks if the resource is a user
	 */
	Predicate<Resource> IS_USER = resource -> resource.getType() == ResourceType.USER;

	/**
	 * Predicate filter that checks if a resource is a group.
	 */
	Predicate<Resource> IS_GROUP = resource -> resource.getType() == ResourceType.GROUP;

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	String getDisplayName();

	/**
	 * Sets the display name.
	 *
	 * @param newDisplayName
	 *            the new display name
	 */
	void setDisplayName(String newDisplayName);

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	ResourceType getType();

	/**
	 * Get the resource source. This should be some id of the system where the user is synchronized from. If may
	 * represent a current system or external.
	 *
	 * @return source id if any. If <code>null</code> it may mean it's created internally
	 */
	String getSource();

	/**
	 * Check for specific resource type.
	 *
	 * @param type
	 *            resource type
	 * @return predicate
	 */
	static Predicate<Resource> isType(ResourceType type) {
		return resource -> resource.getType() == type;
	}

	/**
	 * Gets the resource mail.
	 *
	 * @return the user or group mail or null if there is no mail property set
	 */
	default String getEmail() {
		return getString(ResourceProperties.EMAIL);
	}

	/**
	 * Set the resource email property
	 *
	 * @param mail the email to set.
	 */
	default void setEmail(String mail) {
		add(ResourceProperties.EMAIL, mail);
	}

	/**
	 * Sets the resource as active or inactive.
	 *
	 * @param active
	 *            the new active
	 */
	void setActive(boolean active);

	/**
	 * Checks if the resource active and could perform operations with data
	 *
	 * @return true, if is active
	 */
	boolean isActive();

	/**
	 * Returns the name of the current resource. The name is public identifier for the given resource. For example for
	 * users the name is the user name that uniquely identifies the user in the current tenant. For groups is the group
	 * identifier.
	 *
	 * @return the name of the resource
	 */
	@Override
	String getName();

	/**
	 * Sets the resource name. For users it's the user name and for groups is the public group identifier.
	 *
	 * @param name
	 *            of the resource to set
	 */
	void setName(String name);

}
