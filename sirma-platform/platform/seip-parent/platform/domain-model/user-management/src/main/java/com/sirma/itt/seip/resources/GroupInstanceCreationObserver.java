package com.sirma.itt.seip.resources;

import java.lang.invoke.MethodHandles;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.InstanceActivatedEvent;
import com.sirma.itt.seip.instance.state.InstanceDeactivatedEvent;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.MessageSender;

/**
 * Observer that listens for group instance CRUD operations and to perform synchronization between the sep database,
 * the relational group database and the remote resource store.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 23/10/2017
 */
class GroupInstanceCreationObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@DestinationDef
	private static final String GROUP_CREATED = "java:/jms.queue.CreateRemoteGroup";
	@DestinationDef
	private static final String GROUP_DELETED = "java:/jms.queue.DeleteRemoteGroup";

	@Inject
	private ResourceService resourceService;

	@Inject
	private TransactionalPermissionChanges transactionalPermissionsChangeBuilder;

	@Inject
	private RemoteUserStoreAdapter remoteUserStore;

	@Inject
	@JmsSender(destination = GROUP_CREATED)
	private MessageSender groupCreationMessageSender;

	@Inject
	@JmsSender(destination = GROUP_DELETED)
	private MessageSender groupDeletionMessageSender;

	/**
	 * Observes after instance save event and. Updates or creates the group in remote store. For creation/update in
	 * remote store a JMS queue is used.
	 *
	 * @param event after instance save event
	 */
	void onAfterInstanceSave(@Observes AfterInstanceSaveEvent event) {
		if (event.getInstanceToSave().type().is(ObjectTypes.GROUP)) {
			Instance instance = event.getInstanceToSave();

			try {
				// when saving user properties triggers audit entry that is not desired when we are performing
				// instance integration because the audit operation is already triggered/logged by the functionality
				// before reaching here
				Options.DISABLE_AUDIT_LOG.enable();
				if (event.getCurrentInstance() == null) {
					createNewGroup(instance);
				} else if (event.getOperation().isUserOperation()) {
					// avoid updating group on system operations, such as synchronization, update only on user operation
					resourceService.updateResource(instance, event.getOperation());
				}
				// for now we do not have anything to update for the groups
			} finally {
				Options.DISABLE_AUDIT_LOG.disable();
			}
		}
	}

	/**
	 * Listens for group deactivation event to clear all member properties
	 *
	 * @param event instance deactivation event
	 */
	void onInstanceDeactivated(@Observes InstanceDeactivatedEvent event) {
		Instance instance = event.getInstance();
		if (instance.type().is(ObjectTypes.GROUP)) {
			// this will remove all statements in semantic database and also will trigger update in IDP and relational db
			instance.remove(ResourceProperties.HAS_MEMBER);
			groupDeletionMessageSender.sendText(instance.getId().toString());
		}
	}

	/**
	 * Listens for group activation event to create the group back in the remote store
	 *
	 * @param event instance deactivation event
	 */
	void onInstanceActivated(@Observes InstanceActivatedEvent event) {
		Instance instance = event.getInstance();
		if (instance.type().is(ObjectTypes.GROUP)
				&& instance.getId() != null
				&& !resourceService.areEqual(resourceService.getAllOtherUsers(), instance)) {
			groupCreationMessageSender.sendText(instance.getId().toString());
		}
	}

	private void createNewGroup(Instance instance) {
		if (!resourceService.resourceExists(instance.getId())) {
			resourceService.createGroup(instance);
			assignPermissions(instance);
			groupCreationMessageSender.sendText(instance.getId().toString());
		}
	}

	private void assignPermissions(Instance instance) {
		InstanceReference reference = instance.toReference();
		PermissionsChange.PermissionsChangeBuilder builder = transactionalPermissionsChangeBuilder.builder(reference);
		String allOtherUsersId = (String) resourceService.getAllOtherUsers().getId();

		builder.addRoleAssignmentChange(allOtherUsersId, SecurityModel.BaseRoles.CONSUMER.getIdentifier());
	}

	/**
	 * Listens for messages when new groups are created and should be created in the remote user/group store
	 *
	 * @param message a messages carrying information about the group that need to be created
	 * @throws JMSException if cannot read the message body
	 * @throws RemoteStoreException if the remote store is unavailable
	 */
	@QueueListener(GROUP_CREATED)
	void onNewGroup(Message message) throws JMSException, RemoteStoreException {
		String groupIdentifier = message.getBody(String.class);
		Resource resource = resourceService.findResource(groupIdentifier);

		if (resource instanceof Group) {
			if (!remoteUserStore.isExistingGroup(resource.getName())) {
				remoteUserStore.createGroup((Group) resource);
				LOGGER.info("Created group {} in remote store", resource.getName());
			} else {
				LOGGER.warn(
						"Tried to create group {} in the remote user store, but that group already exists there. Create skipped",
						resource.getName());
			}
		} else {
			throw new RollbackedRuntimeException(
					"Cannot create group " + groupIdentifier + " because not present in the system or not an group");
		}
	}

	/**
	 * Listens for messages when groups are deactivated/deleted and should be removed from the remote user/group store
	 *
	 * @param message a messages carrying information about the group that need to be deleted
	 * @throws JMSException if cannot read the message body
	 * @throws RemoteStoreException if the remote store is unavailable
	 */
	@QueueListener(GROUP_DELETED)
	void onDeleteGroup(Message message) throws JMSException, RemoteStoreException {
		String groupIdentifier = message.getBody(String.class);
		Resource resource = resourceService.findResource(groupIdentifier);

		if (resource instanceof Group) {
			if (remoteUserStore.isExistingGroup(resource.getName())) {
				remoteUserStore.deleteGroup(resource.getName());
				LOGGER.info("Deleted group {} from remote store", resource.getName());
			} else {
				LOGGER.warn(
						"Tried to delete group {} from the remote user store, but that group is already deleted. Delete skipped",
						resource.getName());
			}
		} else {
			throw new RollbackedRuntimeException(
					"Cannot delete group " + groupIdentifier + " because not present in the system or not an group");
		}
	}
}
