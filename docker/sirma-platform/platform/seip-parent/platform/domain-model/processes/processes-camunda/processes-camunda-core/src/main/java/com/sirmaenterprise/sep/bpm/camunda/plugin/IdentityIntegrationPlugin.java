package com.sirmaenterprise.sep.bpm.camunda.plugin;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.event.AttachedChildToResourceEvent;
import com.sirma.itt.seip.resources.event.ResourceAddedEvent;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMSecurityService;
import com.sirmaenterprise.sep.bpm.camunda.service.MultiEngineServicesProducer;

/**
 * Db integration for user and group synchronization in Camunda. In additions the group/user hierarchy is updated as
 * well in Camunda. All users/groups are inserted in Camunda with their db ids.
 *
 * @author bbanchev
 */
@Singleton
public class IdentityIntegrationPlugin implements ProcessEnginePlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** id of the camunda adming group. */
	public static final String GROUP_CAMUNDA_ADMIN = "camunda-admin";
	@Inject
	private MultiEngineServicesProducer engineServicesProducer;
	@Inject
	private BPMSecurityService bpmSecurityService;
	@Inject
	private ResourceService resourceService;
	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		// not used event
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		// not used event
	}

	/**
	 * {@inheritDoc} Loads all users and groups and builds their relations<br>
	 * Executes method in new TX to prevent caching of last used dao
	 */
	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine) {
		List<Resource> allUsers = resourceService.getAllResources(ResourceType.USER, null);
		for (Resource user : allUsers) {
			getOrAddUser(processEngine.getIdentityService(), user);
		}
		List<Resource> allGroups = resourceService.getAllResources(ResourceType.GROUP, null);
		for (Resource localGroup : allGroups) {
			getOrAddGroup(processEngine.getIdentityService(), localGroup).ifPresent(group -> {
				List<Instance> containedResources = resourceService.getContainedResources(group.getId());
				for (Instance member : containedResources) {
					addUserToGroup(processEngine.getIdentityService(), group, (Resource) member);
				}
			});
		}
		// update the admin member
		getOrAddGroup(processEngine.getIdentityService(), GROUP_CAMUNDA_ADMIN).ifPresent(adminGroup -> {
			com.sirma.itt.seip.security.User adminUser = securityContextManager.getAdminUser();
			if (adminUser instanceof Resource) {
				addUserToGroup(processEngine.getIdentityService(), adminGroup, (Resource) adminUser);
			}
		});
	}

	/**
	 * Add new resources on event for adding. Supports user and group synchronization
	 *
	 * @param event
	 *            the event payload
	 */
	public void onNewResource(@Observes ResourceAddedEvent event) {
		if (!isEngineAvailable()) {
			return;
		}
		Resource resource = event.getInstance();
		if (resource.getType() == ResourceType.USER) {
			getOrAddUser(engineServicesProducer.identityService(), resource);
		} else if (resource.getType() == ResourceType.GROUP) {
			getOrAddGroup(engineServicesProducer.identityService(), resource);
		}
	}

	/**
	 * Add a resource to a group on {@link AttachedChildToResourceEvent}. Supports only children which are resources
	 *
	 * @param event
	 *            the event payload
	 */
	public void onResourceAddedToGroup(@Observes AttachedChildToResourceEvent event) {
		if (!isEngineAvailable()) {
			return;
		}
		if (event.getInstance() != null && event.getChild() instanceof Resource) {
			Resource child = (Resource) event.getChild();
			getOrAddGroup(engineServicesProducer.identityService(), event.getInstance())
					.ifPresent(group -> addUserToGroup(engineServicesProducer.identityService(), group, child));
		}
	}

	private boolean isEngineAvailable() {
		if (!bpmSecurityService.isEngineAvailable()) {
			LOGGER.error("Identities could not be updated in Camunda. Process Engine status: {}",
					bpmSecurityService.getProcessEngineStatus());
			return false;
		}
		return true;
	}

	private static Optional<Group> getOrAddGroup(IdentityService identityService, Resource group) {
		return getOrAddGroup(identityService, extractResourceId(group));
	}

	private static Optional<Group> getOrAddGroup(IdentityService identityService, String id) {
		GroupQuery existingGroupQuery = identityService.createGroupQuery().groupId(id);
		if (existingGroupQuery.count() == 0) {
			if (identityService.isReadOnly()) {
				return Optional.empty();
			}
			Group newGroup = identityService.newGroup(id);
			LOGGER.debug("Adding group {}", id);
			identityService.saveGroup(newGroup);
			return Optional.of(newGroup);
		}
		return Optional.ofNullable(existingGroupQuery.singleResult());
	}

	private static void getOrAddUser(IdentityService identityService, Resource user) {
		String id = extractResourceId(user);
		UserQuery existingUserQuery = identityService.createUserQuery().userId(id);
		if (existingUserQuery.count() == 0) {
			if (identityService.isReadOnly()) {
				return;
			}
			User newUser = identityService.newUser(id);
			newUser.setFirstName(user.getAsString(ResourceProperties.FIRST_NAME));
			newUser.setLastName(user.getAsString(ResourceProperties.LAST_NAME));
			newUser.setEmail(user.getAsString(ResourceProperties.EMAIL));
			newUser.setId(id);
			LOGGER.debug("Adding user {}", id);
			identityService.saveUser(newUser);
		}
	}

	private static void addUserToGroup(IdentityService identityService, Group group, Resource member) {
		// make sure the member is added before adding the assignment or we will get NPE from Camunda
		if (member.getType() == ResourceType.USER) {
			getOrAddUser(identityService, member);
		} else {
			getOrAddGroup(identityService, member);
		}
		String groupId = group.getId();
		UserQuery membersOfGroup = identityService
				.createUserQuery()
					.userId(extractResourceId(member))
					.memberOfGroup(groupId);
		if (membersOfGroup.count() == 1) {
			return;
		}
		String userId = extractResourceId(member);
		LOGGER.debug("Adding {} to group {}", userId, groupId);
		try {
			identityService.createMembership(userId, groupId);
		} catch (Exception e) {
			LOGGER.error("Failed to add user " + userId + " to group " + groupId, e);
			throw e;
		}
	}

	private static String extractResourceId(Resource resource) {
		return resource.getId().toString();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
