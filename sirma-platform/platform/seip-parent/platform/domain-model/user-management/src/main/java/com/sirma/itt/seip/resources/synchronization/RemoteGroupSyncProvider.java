package com.sirma.itt.seip.resources.synchronization;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;

/**
 * Synchronization configuration that imports all groups from DMS to internal database. The default behavior is to
 * delete the groups that are no longer found in the remote source.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2017
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 5.5)
public class RemoteGroupSyncProvider extends BaseExternalProviderToSepResourceSynchronizationConfig {

	public static final String NAME = "remoteGroupSyncProvider";

	@Inject
	private RemoteUserStoreAdapter remoteUserStore;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected SynchronizationProvider<Collection<Resource>> loadLocalResources() {
		return () -> resourceService.getAllGroups()
				.stream()
				.map(this::copyResource)
				.peek(this::cleanSystemProperties)
				.collect(Collectors.toList());
	}

	@Override
	protected SynchronizationProvider<Collection<Resource>> loadRemoteResources() {
		return () -> {
			try {
				return remoteUserStore.getAllGroups().stream().map(Resource.class::cast).collect(Collectors.toList());
			} catch (Exception e) {
				throw new SynchronizationException(e);
			}
		};
	}
}
