package com.sirma.itt.seip.resources.synchronization;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;

/**
 * Synchronization configuration that imports all groups from DMS to internal database. The default behavior is to
 * delete the groups that are no longer found in the remote source.
 *
 * @author BBonev
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 5.5)
public class DmsToSeipGroupSynchronizationConfig extends BaseDmsToSeipResourceSynchronizationConfig {

	public static final String NAME = "alfresco4ToSeipGroups";
	@Inject
	private Instance<CMFGroupService> groupService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected SynchronizationProvider<Collection<? extends Resource>> loadLocalResources() {
		return () -> resourceService
				.getAllResources(ResourceType.GROUP, null)
					.stream()
					.map(resource -> copyResource(resource))
					.peek(resource -> cleanSystemProperties(resource))
					.collect(Collectors.toList());
	}

	@Override
	protected SynchronizationProvider<Collection<? extends Resource>> loadRemoteResources() {
		return () -> {
			try {
				return groupService.get().getAllGroups();
			} catch (Exception e) {
				throw new SynchronizationException(e);
			}
		};
	}
}
