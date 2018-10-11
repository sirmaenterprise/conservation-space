package com.sirma.itt.seip.instance.integration;

import java.io.Serializable;

import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.sep.content.Content;

/**
 * Plugable dispatcher responsible for providing the destination system name that can store or retrieve additional
 * information for instance.
 *
 * @author BBonev
 */
public interface InstanceDispatcher extends Plugin {

	/** The target name. */
	String TARGET_NAME = "InstanceDispatcher";

	/**
	 * Gets the content management system that stores/provides the content for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @param content
	 *            the content
	 * @return the content management system
	 */
	String getContentManagementSystem(Serializable instance, Content content);

	/**
	 * Gets the view management system that stores/provides the view content of the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @param content
	 *            the content
	 * @return the view management system
	 */
	String getViewManagementSystem(Serializable instance, Content content);

	/**
	 * Gets the data source system that stores/provides the properties and additional data for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the data source system
	 */
	String getDataSourceSystem(Serializable instance);
}
