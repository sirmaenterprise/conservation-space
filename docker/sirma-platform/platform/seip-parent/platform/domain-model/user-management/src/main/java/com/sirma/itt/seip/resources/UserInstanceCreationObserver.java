package com.sirma.itt.seip.resources;

import java.util.Collections;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;

/**
 * Observer that integrates instance creation or update of type user with the remote user store. It listens for
 * {@link AfterInstanceSaveEvent} and creates or updates the user in JMS queue.
 *
 * @author smustafov
 */
class UserInstanceCreationObserver {

	@DestinationDef
	static final String USER_ACCOUNT_CREATION = "java:/jms.queue.CreateRemoteUserAccount";
	@DestinationDef
	static final String USER_ACCOUNT_UPDATE = "java:/jms.queue.UpdateRemoteUserAccount";

	@Inject
	private ResourceService resourceService;

	@Inject
	private TransactionalPermissionChanges transactionalPermissionsChangeBuilder;

	@Inject
	@JmsSender(destination = RemoteUserStoreQueueListener.OPERATIONS_QUEUE)
	private MessageSender remoteUserStoreMessageSender;

	/**
	 * TODO: remove next sprint; its replaced by RemoteUserStoreQueueListener
	 */
	@Inject
	@JmsSender(destination = USER_ACCOUNT_CREATION)
	private MessageSender userCreationMessageSender;

	/**
	 * TODO: remove next sprint; its replaced by RemoteUserStoreQueueListener
	 */
	@Inject
	@JmsSender(destination = USER_ACCOUNT_UPDATE)
	private MessageSender userUpdateMessageSender;

	/**
	 * Observes after instance save event and. Updates or creates the user in remote store. For creation/update in
	 * remote store a JMS queue is used.
	 *
	 * @param event after instance save event
	 */
	void onAfterInstanceSave(@Observes AfterInstanceSaveEvent event) {
		if (event.getInstanceToSave().type().is(ObjectTypes.USER)) {
			Instance instance = event.getInstanceToSave();

			try {
				// when saving user properties triggers audit entry that is not desired when we are performing
				// instance integration because the audit operation is already triggered/logged by the functionality
				// before reaching here
				Options.DISABLE_AUDIT_LOG.enable();
				if (event.getCurrentInstance() == null) {
					createNewUser(instance);
				} else if (event.getOperation().isUserOperation()) {
					// avoid updating user on system operations, such as synchronization, update only on user operations
					updateUserOnSave(instance, event.getOperation());
				}
			} finally {
				Options.DISABLE_AUDIT_LOG.disable();
			}
		}
	}

	private void createNewUser(Instance instance) {
		if (!resourceService.resourceExists(instance.getId())) {
			User created = resourceService.createUser(instance);
			addToEveryoneGroup((String) created.getId());
			assignPermissions(instance);
			sendRemoteUserStoreMessage(instance.getId().toString(), RemoteUserStoreQueueListener.CREATE_USER);
		}
	}

	private void addToEveryoneGroup(String createdUserId) {
		// add the user to the Everyone group so it's updated in the relational DB immediate on save
		Resource everyoneGroup = resourceService.getAllOtherUsers();
		resourceService.modifyMembers(everyoneGroup, Collections.singleton(createdUserId), Collections.emptyList());
	}

	private void assignPermissions(Instance instance) {
		InstanceReference reference = instance.toReference();
		PermissionsChangeBuilder builder = transactionalPermissionsChangeBuilder.builder(reference);
		String allOtherUsersId = (String) resourceService.getAllOtherUsers().getId();

		builder.addRoleAssignmentChange((String) instance.getId(), SecurityModel.BaseRoles.MANAGER.getIdentifier());
		builder.addRoleAssignmentChange(allOtherUsersId, SecurityModel.BaseRoles.CONSUMER.getIdentifier());
	}

	private void sendRemoteUserStoreMessage(String data, String operation) {
		SendOptions options = remoteUserStoreMessageSender.getDefaultSendOptions();
		options.withProperty(RemoteUserStoreQueueListener.OPERATION, operation);
		remoteUserStoreMessageSender.sendText(data, options);
	}

	private void updateUserOnSave(Instance instance, Operation operation) {
		resourceService.updateResource(instance, operation);
		sendRemoteUserStoreMessage(instance.getId().toString(), RemoteUserStoreQueueListener.UPDATE_USER);
	}

}
