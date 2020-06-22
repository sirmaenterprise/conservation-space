package com.sirma.itt.seip.resources.synchronization;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;

/**
 * Synchronization configuration for user from the configured remote store provider to internal users store.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2017
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 5)
public class RemoteUserSyncProvider extends BaseExternalProviderToSepResourceSynchronizationConfig {

	public static final String NAME = "remoteUserSyncProvider";

	@Inject
	private RemoteUserStoreAdapter remoteUserStore;
	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected SynchronizationProvider<Collection<Resource>> loadLocalResources() {
		return () -> resourceService
				.getAllUsers()
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
	protected SynchronizationProvider<Collection<Resource>> loadRemoteResources() {
		return () -> {
			try {
				return remoteUserStore.getAllUsers().stream().map(Resource.class::cast).collect(Collectors.toList());
			} catch (Exception e) {
				throw new SynchronizationException(e);
			}
		};
	}
}
