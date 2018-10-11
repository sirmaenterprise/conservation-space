package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.transaction.TransactionScoped;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;

/**
 * Observer that listens for adding and removing of {@value #HAS_MEMBER} relation and sends a JMS messages to
 * {@value #CHANGE_MEMBERSHIP_QUEUE}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/10/2017
 */
@TransactionScoped
class GroupMembersSynchronizationObserver implements Serializable {

	@DestinationDef
	static final String CHANGE_MEMBERSHIP_QUEUE = "java:/jms.queue.ChangeGroupMembership";

	private static final String HAS_MEMBER = "ptop:hasMember";

	static final String GROUP_KEY = "group";
	static final String ADD_OP = "add";
	static final String REMOVE_OP = "remove";

	/**
	 * TODO: remove next sprint; its replaced by RemoteUserStoreQueueListener
	 */
	@Inject
	@JmsSender(destination = CHANGE_MEMBERSHIP_QUEUE)
	private MessageSender changeMembershipSender;

	@Inject
	@JmsSender(destination = RemoteUserStoreQueueListener.OPERATIONS_QUEUE)
	private MessageSender remoteUserStoreMessageSender;

	@Inject
	private ResourceService resourceService;

	@Inject
	private GroupMembersChangesBuffer changesBuffer;

	@PostConstruct
	void init() {
		changesBuffer.registerOnTransactionCompletionHandler(this::sendChanges);
	}

	/**
	 * Listens for new members to add
	 *
	 * @param event the event to process
	 */
	void onGroupMemberAdded(@Observes ObjectPropertyAddEvent event) {
		if (HAS_MEMBER.equals(event.getObjectPropertyName())) {
			Serializable sourceId = event.getSourceId();
			Serializable targetId = event.getTargetId();
			changesBuffer.addMember(sourceId, targetId);
		}
	}

	/**
	 * Listens for members to remove
	 *
	 * @param event the event to process
	 */
	void onGroupMemberRemoved(@Observes ObjectPropertyRemoveEvent event) {
		if (HAS_MEMBER.equals(event.getObjectPropertyName())) {
			Serializable sourceId = event.getSourceId();
			Serializable targetId = event.getTargetId();
			changesBuffer.removeMember(sourceId, targetId);
		}
	}

	private void sendChanges(Collection<GroupChange> groupChanges) {
		Map<Serializable, Pair<List<String>, List<String>>> changes = new HashMap<>();
		for (GroupChange change : groupChanges) {
			switch (change.getChangeType()) {
				case ADD:
					changes.computeIfAbsent(change.getGroupId(),
							id -> new Pair<>(new LinkedList<>(), new LinkedList<>()))
							.getFirst()
							.add(change.getMemberId().toString());
					break;
				case REMOVE:
					changes.computeIfAbsent(change.getGroupId(),
							id -> new Pair<>(new LinkedList<>(), new LinkedList<>()))
							.getSecond()
							.add(change.getMemberId().toString());
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		if (changes.isEmpty()) {
			return;
		}

		// Everyone is internal group in IDP and members of it cannot be modified, otherwise IDP throws exceptions
		changes.remove(resourceService.getAllOtherUsers().getId());

		changes.forEach((groupId, modifications) -> {
			JsonObjectBuilder changesJson = Json.createObjectBuilder();
			JSON.addIfNotNull(changesJson, GROUP_KEY, groupId);
			JSON.addIfNotNull(changesJson, ADD_OP, modifications.getFirst());
			JSON.addIfNotNull(changesJson, REMOVE_OP, modifications.getSecond());

			Resource group = resourceService.findResource(groupId);
			resourceService.modifyMembers(group, modifications.getFirst(), modifications.getSecond());

			SendOptions options = remoteUserStoreMessageSender.getDefaultSendOptions();
			options.withProperty(RemoteUserStoreQueueListener.OPERATION,
					RemoteUserStoreQueueListener.MODIFY_GROUP_MEMBERS);
			remoteUserStoreMessageSender.sendText(changesJson.build().toString(), options);
		});
	}
}
