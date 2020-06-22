package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.resources.GroupMembersSynchronizationObserver.ADD_OP;
import static com.sirma.itt.seip.resources.GroupMembersSynchronizationObserver.CHANGE_MEMBERSHIP_QUEUE;
import static com.sirma.itt.seip.resources.GroupMembersSynchronizationObserver.GROUP_KEY;
import static com.sirma.itt.seip.resources.GroupMembersSynchronizationObserver.REMOVE_OP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;

/**
 * Queue listener that processes update and create of users, and groups membership modifications in the remote user store.
 * There is only one queue - {@link RemoteUserStoreQueueListener#OPERATIONS_QUEUE} which should be used for all of the
 * above operations. This is because those operation have to be processed in specific order. For example, the create of
 * user should be first and then that user should be added to a group as member.
 * The starting point of the listener is the {@link RemoteUserStoreQueueListener#processMessage(Message)} method.
 *
 * @author smustafov
 */
@Singleton
class RemoteUserStoreQueueListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String OPERATION = "remoteUserStoreOperation";
	static final String CREATE_USER = "createUser";
	static final String UPDATE_USER = "updateUser";
	static final String MODIFY_GROUP_MEMBERS = "modifyGroupMembers";

	@DestinationDef
	static final String OPERATIONS_QUEUE = "java:/jms.queue.RemoteUserStoreOperations";

	@Inject
	private ResourceService resourceService;
	@Inject
	private RemoteUserStoreAdapter remoteUserStore;

	@QueueListener(OPERATIONS_QUEUE)
	void processMessage(Message message) {
		try {
			executeOperation(message);
		} catch (IllegalArgumentException | RollbackedRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	private void executeOperation(Message message) throws JMSException, RemoteStoreException {
		String operation = message.getStringProperty(OPERATION);
		switch (operation) {
			case CREATE_USER:
				createRemoteUserAccount(message);
				break;
			case UPDATE_USER:
				updateRemoteUserAccount(message);
				break;
			case MODIFY_GROUP_MEMBERS:
				changeMembership(message);
				break;
			default:
				throw new IllegalArgumentException("Invalid remote user store queue operation: " + operation);
		}
	}

	/**
	 * Updates user in the remote store using a JMS queue.
	 *
	 * @param message JMS message object which contains id of the user to be updated
	 * @throws JMSException         when JMS error occurs
	 * @throws RemoteStoreException if there is a problem with the remote store communication
	 */
	@QueueListener(UserInstanceCreationObserver.USER_ACCOUNT_UPDATE)
	void updateRemoteUserAccount(Message message) throws JMSException, RemoteStoreException {
		String userIdentifier = message.getBody(String.class);
		Resource resource = resourceService.findResource(userIdentifier);

		LOGGER.debug("Starting to update user: {}", userIdentifier);

		if (resource instanceof User) {
			if (remoteUserStore.isExistingUser(resource.getName())) {
				remoteUserStore.updateUser((User) resource);
			} else {
				LOGGER.warn(
						"Tried to update user {} in the remote store, but the user does not exists there. Update skipped",
						resource.getName());
			}
		} else {
			throw new RollbackedRuntimeException(
					"Cannot update user " + userIdentifier + " because not present in the system or not an user");
		}
	}

	/**
	 * Creates user in the remote store using a JMS queue.
	 *
	 * @param message JMS message object which contains id of the user to be created
	 * @throws JMSException         when JMS error occurs
	 * @throws RemoteStoreException if there is a problem with the remote store communication
	 */
	@QueueListener(UserInstanceCreationObserver.USER_ACCOUNT_CREATION)
	void createRemoteUserAccount(Message message) throws JMSException, RemoteStoreException {
		String userIdentifier = message.getBody(String.class);
		Resource resource = resourceService.findResource(userIdentifier);

		LOGGER.debug("Starting to create user: {}", userIdentifier);

		if (resource instanceof User) {
			if (!remoteUserStore.isExistingUser(resource.getName())) {
				remoteUserStore.createUser((User) resource);
			} else {
				LOGGER.warn(
						"Tried to create user {} in the remote user store, but that user already exists there. Create skipped",
						resource.getName());
			}
		} else {
			throw new RollbackedRuntimeException(
					"Cannot create user " + userIdentifier + " because not present in the system or not an user");
		}
	}

	/**
	 * Changes memberships of given group. Processes adding and removing members.
	 * A json is expected to be found in the message body. Example format of the json:
	 * <pre>
	 * {
	 * "group": "groupDbId",
	 * "add": ["userDbId1", "userDbId2", ...],
	 * "remove": ["userDbId1", "userDbId2", ...]
	 * }
	 * </pre>
	 *
	 * @param message JMS message object which in the body contains json payload represented as string
	 */
	@QueueListener(CHANGE_MEMBERSHIP_QUEUE)
	void changeMembership(Message message) throws JMSException {
		JSON.readObject(message.getBody(String.class), jsonData -> {
			try {
				LOGGER.debug("Starting to process group memberships with json: {}", jsonData);
				processGroupMemberships(jsonData);
				return null;
			} catch (RemoteStoreException e) {
				throw new RollbackedRuntimeException(e);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void processGroupMemberships(JsonObject jsonData) throws RemoteStoreException {
		Map<String, Serializable> data = (Map<String, Serializable>) JSON.readJsonValue(jsonData);
		Serializable groupId = data.get(GROUP_KEY);

		Optional<Resource> group = getGroup(groupId);
		if (!group.isPresent()) {
			return;
		}

		List<String> resourcesToAdd = getResourceNames((List<Serializable>) data.get(ADD_OP));
		List<String> resourcesToRemove = getResourceNames((List<Serializable>) data.get(REMOVE_OP));

		if (!group.get().isActive() && isNotEmpty(resourcesToAdd)) {
			LOGGER.warn("Tried to add members {} to inactive group {}", resourcesToAdd, groupId);
			resourcesToAdd.clear();
		}

		if (isEmpty(resourcesToAdd) && isEmpty(resourcesToRemove)) {
			return;
		}

		remoteUserStore.updateUsersInGroup(group.get().getName(), resourcesToRemove, resourcesToAdd);
		LOGGER.info("Changed group members of {}\n    Added: {}\n    Removed: {}", groupId, resourcesToAdd,
				resourcesToRemove);
	}

	private Optional<Resource> getGroup(Serializable groupId) {
		Resource resource = resourceService.findResource(groupId);
		if (resource == null) {
			LOGGER.warn("Could not find group {}", groupId);
			return Optional.empty();
		} else if (resource.getType() != ResourceType.GROUP) {
			LOGGER.warn("Requested resource is not a group {}", groupId);
			return Optional.empty();
		}
		return Optional.of(resource);
	}

	private List<String> getResourceNames(List<Serializable> resources) {
		if (isEmpty(resources)) {
			return Collections.emptyList();
		}
		return resourceService.loadByDbId(resources).stream()
				.map(Resource.class::cast)
				.map(Resource::getName)
				.collect(Collectors.toList());
	}

}
