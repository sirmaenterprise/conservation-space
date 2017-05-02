package com.sirma.itt.seip.resources;

import java.util.List;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * ResourceProviderExtension is extension to for resource derived classes.
 */
@Documentation("Provides specific resource on request")
public interface ResourceProviderExtension extends Plugin {

	/** The target name. */
	String TARGET_NAME = "ResourceProviderExtension";

	/**
	 * Checks if is applicable for provided resource type. Any invoke on this extension should be pre checked with this
	 * method.
	 *
	 * @param type
	 *            the type
	 * @return true, if is applicable
	 */
	boolean isApplicable(ResourceType type);

	/**
	 * Gets the resource by id.
	 *
	 * @param resourceId
	 *            the resource identifier
	 * @return the resource
	 */
	Resource getResource(String resourceId);

	/**
	 * Gets the resources that are contained in provided resource. Used for groups mostly
	 *
	 * @param <R>
	 *            the generic type of resource
	 * @param resource
	 *            the resource to check what is contained in
	 * @return the contained resources list
	 */
	<R extends Resource> List<R> getContainedResources(Resource resource);

	/**
	 * Gets the resource identifiers that are contained in provided resource. Used for groups mostly
	 *
	 * @param resource
	 *            the resource to check what is contained in
	 * @return the contained resources list
	 */
	List<String> getContainedResourceIdentifiers(Resource resource);

	/**
	 * Gets the resources that are containing the provided resource. Used for user mostly
	 *
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the resource
	 * @return the list of resources resource is part of as child
	 */
	<R extends Resource> List<R> getContainingResources(Resource resource);

	/**
	 * Gets the resource by id as {@link #getResource(String)} but if 'findAndSynch' param is set to true, resource is
	 * retrieved from underlying system with forced request. If not found after synch null is returned
	 *
	 * @param <R>
	 *            the generic type
	 * @param resourceId
	 *            the resource identifier
	 * @param findAndSynch
	 *            force retrieve of resource
	 * @return the resource if found or null
	 */
	<R extends Resource> R getResource(String resourceId, boolean findAndSynch);
}
