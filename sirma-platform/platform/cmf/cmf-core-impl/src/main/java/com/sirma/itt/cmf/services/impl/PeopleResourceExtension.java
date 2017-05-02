package com.sirma.itt.cmf.services.impl;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfResourcesUtil;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProviderExtension;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.resources.adapter.CMFUserService;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * The PeopleResourceExtension provides extension for user resources.
 *
 * @author BBanchev
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ResourceProviderExtension.TARGET_NAME, order = 0)
public class PeopleResourceExtension implements ResourceProviderExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private Instance<CMFUserService> userService;
	@Inject
	private Instance<CMFGroupService> groupAdapter;
	@Inject
	private ResourceService resourceService;
	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public boolean isApplicable(ResourceType type) {
		return ResourceType.USER == type;
	}

	@Override
	public Resource getResource(String id) {
		com.sirma.itt.seip.security.User systemUser = securityContextManager.getSystemUser();
		if (nullSafeEquals(systemUser.getIdentityId(), id)) {
			return new EmfUser(systemUser);
		}
		try {
			return userService.get().findUser(id);
		} catch (Exception e) {
			LOGGER.warn("Could not resolve user {} due to {}", id, e.getMessage());
			LOGGER.trace("", e);
		}
		return null;
	}

	@Override
	public <R extends Resource> List<R> getContainedResources(Resource resource) {
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> List<R> getContainingResources(Resource resource) {
		try {
			return (List<R>) groupAdapter
					.get()
						.getAuthorities(EmfResourcesUtil.getActualResource(resource, resourceService));
		} catch (Exception e) {
			LOGGER.warn("Could not load containing resources due to {}", e.getMessage());
			LOGGER.trace("", e);
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getContainedResourceIdentifiers(Resource resource) {
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R getResource(String resourceId, boolean synch) {
		Resource synchronize = getResource(resourceId);
		if (synch && synchronize != null) {
			synchronize = resourceService.saveResource(synchronize);
		}
		return (R) synchronize;
	}

}
