package com.sirma.itt.seip.resources.synchronization;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.event.ResourceSynchronizationRequredEvent;
import com.sirma.itt.seip.resources.event.ResourcesSynchronizationEndedEvent;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationResultState;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.tasks.Schedule;

/**
 * ResourceSynchronization provides methods that can trigger inbound synchronization of users and/or groups.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ResourceSynchronization {

	@Inject
	private SynchronizationRunner synchronizationRunner;
	@Inject
	private EventService eventService;

	@OnTenantAdd
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.AFTER_APP_START, order = 1000)
	protected static void insertSystemUser(SecurityConfiguration configurations, ResourceService resourceService) {
		Resource systemUser = (Resource) configurations.getSystemUser().get();
		if (!resourceService.resourceExists(systemUser.getName())) {
			resourceService.saveResource(systemUser);
		}
		Resource adminUser = (Resource) configurations.getAdminUser().get();
		if (!resourceService.resourceExists(adminUser.getName())) {
			resourceService.saveResource(adminUser);
		}
	}

	/**
	 * Synchronize users, groups and group members
	 */
	@OnTenantAdd
	@RunAsAllTenantAdmins
	@Startup(async = true, phase = StartupPhase.AFTER_APP_START)
	public void synchronizeAll() {
		synchronizeUsers();
		synchronizeGroups();
		synchronizeGroupsMembers();
		eventService.fire(new ResourceSynchronizationRequredEvent(true));
	}

	/**
	 * Synchronize users.
	 */
	@RunAsAllTenantAdmins
	@Schedule(identifier = "REINIT_PEOPLE_CACHE")
	@ConfigurationPropertyDefinition(name = "cache.user.update.schedule", defaultValue = "0 0/15 * ? * *", sensitive = true, system = true, label = "Cron like expression for interval of users synchronization.")
	public void synchronizeUsers() {
		SynchronizationResultState resultState = synchronizationRunner
				.runSynchronization(DmsToSeipUserSynchronizationConfig.NAME);
		// notify only if we have any changes
		if (resultState.getResult() != null && resultState.getResult().hasChanges()) {
			eventService.fire(new ResourcesSynchronizationEndedEvent(ResourceType.USER));
		}
	}

	/**
	 * Synchronize groups.
	 */
	@RunAsAllTenantAdmins
	@Schedule(identifier = "REINIT_GROUP_CACHE")
	@ConfigurationPropertyDefinition(name = "cache.group.update.schedule", defaultValue = "0 2/15 * ? * *", sensitive = true, system = true, label = "Cron like expression for interval of groups synchronization.")
	public void synchronizeGroups() {
		synchronizationRunner.runSynchronization(DmsToSeipGroupSynchronizationConfig.NAME);
	}

	/**
	 * Synchronize groups members
	 */
	@RunAsAllTenantAdmins
	@Schedule(identifier = "RELOAD_GROUP_MEMBERS")
	@ConfigurationPropertyDefinition(name = "cache.group.members.schedule", defaultValue = "0 3/15 * ? * *", sensitive = true, system = true, label = "Cron like expression for interval of groups members.")
	public void synchronizeGroupsMembers() {
		SynchronizationResultState resultState = synchronizationRunner
				.runSynchronization(DmsToSeipGroupMembersSynchronizationConfig.NAME);
		// notify only if we have any changes
		if (resultState.getResult() != null && resultState.getResult().hasChanges()) {
			// fire event for groups after members are synchronized
			eventService.fire(new ResourcesSynchronizationEndedEvent(ResourceType.GROUP));
		}
	}

}
