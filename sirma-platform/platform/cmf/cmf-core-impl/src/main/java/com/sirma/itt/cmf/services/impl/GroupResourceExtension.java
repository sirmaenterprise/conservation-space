package com.sirma.itt.cmf.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResourcesUtil;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProviderExtension;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;

/**
 * The GroupResourceExtension provides extension for group resources.
 *
 * @author BBanchev
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ResourceProviderExtension.TARGET_NAME, order = 1)
public class GroupResourceExtension implements ResourceProviderExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(GroupResourceExtension.class);

	@Inject
	private Instance<CMFGroupService> groupAdapter;
	@Inject
	private ResourceService resourceService;

	@Override
	public boolean isApplicable(ResourceType type) {
		return ResourceType.GROUP == type;
	}

	@Override
	public Resource getResource(String resourceId) {
		if (StringUtils.isNullOrEmpty(resourceId)) {
			return null;
		}
		try {
			return groupAdapter.get().findGroup(resourceId.toLowerCase());
		} catch (Exception e) {
			LOGGER.warn("Could not resolve group {} due to: {}", resourceId, e.getMessage());
			LOGGER.trace("", e);
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> List<R> getContainedResources(Resource resource) {
		if (resource != null && isApplicable(resource.getType())) {
			try {
				return groupAdapter.get().getUsersInAuthority(convertToGroup(resource)).stream().map(userId -> {
					R found = (R) resourceService.load(userId);
					if (found == null) {
						LOGGER.warn("Could not find resource with id {}", userId);
					}
					return found;
				}).filter(Objects::nonNull).collect(Collectors.toList());
			} catch (Exception e) {
				LOGGER.warn("Could not resolve authorities for resource {} due to {}", resource.getId(),
						e.getMessage());
				LOGGER.trace("", e);
			}
		}
		throw new EmfRuntimeException("Unsupported operation for type GROUP");
	}

	@Override
	public List<String> getContainedResourceIdentifiers(Resource resource) {
		if (resource != null && isApplicable(resource.getType())) {
			try {
				return groupAdapter.get().getUsersInAuthority(convertToGroup(resource));
			} catch (Exception e) {
				LOGGER.warn("Could not resolve contained resources for {} due to {}", resource.getId(), e.getMessage());
				LOGGER.trace("", e);
			}
		}
		throw new EmfRuntimeException("Unsupported operation for type GROUP");
	}

	/**
	 * Convert to group.
	 *
	 * @param resource
	 *            the resource
	 * @return the group
	 */
	private static Group convertToGroup(Resource resource) {
		if (resource instanceof Group) {
			return (Group) resource;
		} else if (resource instanceof GenericProxy && ((GenericProxy<?>) resource).getTarget() instanceof Group) {
			return (Group) ((GenericProxy<?>) resource).getTarget();
		}
		return new EmfGroup(resource.getName(), resource.getDisplayName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> List<R> getContainingResources(Resource resource) {
		try {
			return (List<R>) groupAdapter
					.get()
						.getAuthorities(EmfResourcesUtil.getActualResource(resource, resourceService));
		} catch (Exception e) {
			LOGGER.warn("Could not resolve authorities for resource {} due to {}", resource.getId(), e.getMessage());
			LOGGER.trace("", e);
		}
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R getResource(String resourceId, boolean synch) {
		if (synch) {
			throw new EmfRuntimeException("Not implemented operation for type GROUP");
		}
		return (R) getResource(resourceId);
	}
}
