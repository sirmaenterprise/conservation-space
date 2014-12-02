package com.sirma.itt.cmf.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.GroupService;
import com.sirma.itt.emf.resources.ResourceProviderExtension;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.GroupService.GroupSorter;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.Group;

/**
 * The GroupResourceExtension provides extension for group resources.
 */
@ApplicationScoped
@Extension(target = ResourceProviderExtension.TARGET_NAME, order = 1)
public class GroupResourceExtension implements ResourceProviderExtension {

	/** The group service. */
	@Inject
	private GroupService groupService;
	@Inject
	private ResourceService resourceService;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R extends Resource> List<R> getAllResources(String sortColumn) {
		GroupSorter sorter = null;
		if (sortColumn != null) {
			sorter = GroupService.GroupSorter.valueOf(sortColumn);
		}
		return (List<R>) groupService.getSortedGroup(sorter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isApplicable(ResourceType type) {
		return ResourceType.GROUP == type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R getResource(String resourceId) {
		return (R) groupService.findGroup(resourceId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends Resource> List<R> getContainedResources(Resource resource) {
		if ((resource != null) && isApplicable(resource.getType())) {
			List<String> usersInAuthority = groupService
					.getUsersInAuthority(convertToGroup(resource));

			List<R> users = new ArrayList<R>(usersInAuthority.size());
			for (String userId : usersInAuthority) {
				users.add((R) resourceService.getResource(userId, ResourceType.USER));
			}
			return users;
		}
		throw new RuntimeException("Unsupported operation for type GROUP");
	}

	@Override
	public List<String> getContainedResourceIdentifiers(Resource resource) {
		if ((resource != null) && isApplicable(resource.getType())) {
			List<String> usersInAuthority = groupService
					.getUsersInAuthority(convertToGroup(resource));
			return usersInAuthority;
		}
		throw new RuntimeException("Unsupported operation for type GROUP");
	}

	/**
	 * Convert to group.
	 *
	 * @param resource
	 *            the resource
	 * @return the group
	 */
	private Group convertToGroup(Resource resource) {
		if (resource instanceof Group) {
			return (Group) resource;
		} else if ((resource instanceof GenericProxy)
				&& (((GenericProxy<?>) resource).getTarget() instanceof Group)) {
			return (Group) ((GenericProxy<?>) resource).getTarget();
		}
		return new EmfGroup(resource.getIdentifier(), resource.getDisplayName());
	}

	@Override
	public <R extends Resource> List<R> getContainingResources(Resource resource) {
		throw new RuntimeException("Unsupported operation for type GROUP");
	}

	@Override
	public <R extends Resource> R getResource(String resourceId, boolean synch) {
		if (synch) {
			throw new RuntimeException("Not implemented operation for type GROUP");
		}
		return getResource(resourceId);
	}
}
