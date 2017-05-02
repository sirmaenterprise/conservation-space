package com.sirma.itt.seip.resources.synchronization;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFUserService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;

/**
 * Synchronization configuration for user from Alfresco 4 to internal users store.
 *
 * @author BBonev
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 5)
public class DmsToSeipUserSynchronizationConfig extends BaseDmsToSeipResourceSynchronizationConfig {

	public static final String NAME = "alfresco4ToSeipUsers";

	@Inject
	private Instance<CMFUserService> userService;
	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected SynchronizationProvider<Collection<? extends Resource>> loadLocalResources() {
		return () -> resourceService
				.getAllResources(ResourceType.USER, null)
					.stream()
					.filter(filterOutSystemUser())
					.map(this::copyResource)
					.peek(this::cleanSystemProperties)
					.collect(Collectors.toList());
	}

	private Predicate<? super Resource> filterOutSystemUser() {
		User systemUser = securityContextManager.getSystemUser();
		return resource -> !systemUser.getSystemId().equals(resource.getId());
	}

	@Override
	protected SynchronizationProvider<Collection<? extends Resource>> loadRemoteResources() {
		return () -> {
			try {
				return userService.get().getAllUsers();
			} catch (Exception e) {
				throw new SynchronizationException(e);
			}
		};
	}
}
