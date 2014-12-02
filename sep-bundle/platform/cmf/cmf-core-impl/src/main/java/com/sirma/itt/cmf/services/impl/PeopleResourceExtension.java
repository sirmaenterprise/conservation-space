package com.sirma.itt.cmf.services.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.GroupService;
import com.sirma.itt.emf.resources.PeopleService;
import com.sirma.itt.emf.resources.ResourceProviderExtension;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.User;

/**
 * The PeopleResourceExtension provides extension for user resources.
 */
@ApplicationScoped
@Extension(target = ResourceProviderExtension.TARGET_NAME, order = 0)
public class PeopleResourceExtension implements ResourceProviderExtension {

	/** The people service. */
	@Inject
	private PeopleService peopleService;
	/** The group service. */
	@Inject
	private GroupService groupService;
	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	@Inject
	private CMFUserService userService;

	private static final Logger LOGGER = Logger.getLogger(PeopleResourceExtension.class);

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R extends Resource> List<R> getAllResources(String sortColumn) {
		return (List<R>) peopleService.getSortedUsers(sortColumn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isApplicable(ResourceType type) {
		return ResourceType.USER == type;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R extends Resource> R getResource(String id) {
		return (R) peopleService.findUser(id);
	}

	@Override
	public <R extends Resource> List<R> getContainedResources(Resource resource) {
		throw new RuntimeException("Unsupported operation for type USER");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> List<R> getContainingResources(Resource resource) {
		if ((resource != null) && isApplicable(resource.getType())) {
			return (List<R>) groupService.getAuthorities(convertToUser(resource));
		}
		throw new RuntimeException("Unsupported operation for type USER");
	}

	/**
	 * Convert to group.
	 *
	 * @param resource
	 *            the resource
	 * @return the group
	 */
	private User convertToUser(Resource resource) {
		if (resource instanceof User) {
			return (User) resource;
		} else if ((resource instanceof GenericProxy)
				&& (((GenericProxy<?>) resource).getTarget() instanceof User)) {
			return (User) ((GenericProxy<?>) resource).getTarget();
		}
		return (User) resourceService.getResource(resource.getIdentifier(), ResourceType.USER);
	}

	@Override
	public List<String> getContainedResourceIdentifiers(Resource resource) {
		throw new RuntimeException("Unsupported operation for type USER");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends Resource> R getResource(String resourceId, boolean synch) {
		if (synch) {
			try {
				User synchronize = userService.findUser(resourceId);
				User created = resourceService.getOrCreateResource(synchronize);
				return (R) created;
			} catch (DMSException e) {
				LOGGER.error("Synchronization error: " + e.getMessage(), e);
				return null;
			}
		}
		return getResource(resourceId);
	}

}
