package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.resources.GroupMembersSynchronizationObserver.GROUP_KEY;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.json.JSON;

/**
 * Tests for {@link RemoteUserStoreQueueListener}.
 *
 * @author smustafov
 */
public class RemoteUserStoreQueueListenerTest {

	@InjectMocks
	private RemoteUserStoreQueueListener listener;

	@Mock
	private ResourceService resourceService;
	@Mock
	private RemoteUserStoreAdapter remoteUserStore;

	@Before
	public void beforeEach() {
		initMocks(this);

		when(resourceService.loadByDbId(anyList())).then(a -> {
			List<String> ids = a.getArgumentAt(0, List.class);
			return ids.stream().map(id -> id.substring(id.indexOf(':') + 1)).map(this::mockUser).collect(toList());
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowException_ForInvalidOperation() throws Exception {
		listener.processMessage(mockJmsMessage("invalidOp", "emf:user"));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void updateRemoteUserAccount_ShouldThrowException_When_UserNotFound() throws Exception {
		Message message = mockJmsMessage(RemoteUserStoreQueueListener.UPDATE_USER, "emf:notExisting");
		listener.processMessage(message);
	}

	@Test
	public void updateRemoteUserAccount_ShouldUpdateUser_When_UserExistsInRemoteStore() throws Exception {
		String userId = "emf:user";
		Message message = mockJmsMessage(RemoteUserStoreQueueListener.UPDATE_USER, userId);
		mockUser(userId, true);
		EmfUser user = mockUser(userId, true);

		listener.processMessage(message);

		verify(remoteUserStore).updateUser(user);
	}

	@Test
	public void updateRemoteUserAccount_ShouldDoNothing_When_UserDoesNotExistsInRemoteStore() throws Exception {
		String userId = "emf:user";
		Message message = mockJmsMessage(RemoteUserStoreQueueListener.UPDATE_USER, userId);
		mockUser(userId, false);

		listener.processMessage(message);

		verify(remoteUserStore, never()).updateUser(any());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void createRemoteUserAccount_ShouldThrowException_When_GroupIsPassed() throws Exception {
		String groupId = "emf:group";
		Message message = mockJmsMessage(RemoteUserStoreQueueListener.CREATE_USER, groupId);
		when(resourceService.findResource(groupId)).thenReturn(new EmfGroup());

		listener.processMessage(message);
	}

	@Test
	public void createRemoteUserAccount_ShouldCreateUserInRemoteStore_IfUserDoesNotExists() throws Exception {
		String userId = "emf:user";
		Message message = mockJmsMessage(RemoteUserStoreQueueListener.CREATE_USER, userId);
		EmfUser user = mockUser(userId, false);

		listener.processMessage(message);

		verify(remoteUserStore).createUser(user);
	}

	@Test
	public void createRemoteUserAccount_ShouldNotCreateUserInRemoteStore_IfTheUserExists() throws Exception {
		String userId = "emf:user";
		Message message = mockJmsMessage(RemoteUserStoreQueueListener.CREATE_USER, userId);
		mockUser(userId, true);

		listener.processMessage(message);

		verify(remoteUserStore, never()).createUser(any());
	}

	@Test
	public void changeMembership_doNothingDuringAdd_ifGroupIsNotFound() throws Exception {
		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "add", "emf:member-test.com"));
		verify(remoteUserStore, never()).updateUsersInGroup(any(), any(), any());
	}

	@Test
	public void changeMembership_doNothingDuringAdd_ifPassedUserInsteadOfGroup() throws Exception {
		Resource group = mockUser("GROUP_group");
		when(resourceService.findResource("emf:GROUP_group")).thenReturn(group);

		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "add", "emf:member-test.com"));

		verify(remoteUserStore, never()).updateUsersInGroup(any(), any(), any());
	}

	@Test
	public void changeMembership_doNothingDuringAdd_ifGroupIsNotActive() throws Exception {
		mockGroup(false);

		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "add", "emf:member-test.com"));

		verify(remoteUserStore, never()).updateUsersInGroup(any(), any(), any());
	}

	@Test
	public void changeMembership_registerUserToGroup_allDataIsFound() throws Exception {
		mockGroup(true);

		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "add", "emf:member-test.com"));

		verify(remoteUserStore)
				.updateUsersInGroup(eq("GROUP_group"), anyList(), eq(Collections.singletonList("member-test.com")));
	}

	@Test
	public void changeMembership_doNothingDuringRemove_ifGroupIsNotFound() throws Exception {
		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "remove", "emf:member-test.com"));
		verify(remoteUserStore, never()).updateUsersInGroup(any(), any(), any());
	}

	@Test
	public void changeMembership_doNothingDuringRemove_ifGroupIsNotGroup() throws Exception {
		Resource group = mockUser("GROUP_group");
		when(resourceService.findResource("emf:GROUP_group")).thenReturn(group);

		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "remove", "emf:member-test.com"));

		verify(remoteUserStore, never()).updateUsersInGroup(any(), any(), any());
	}

	@Test
	public void changeMembership_unregisterUserToGroup_allDataIsFound() throws Exception {
		mockGroup(true);

		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "remove", "emf:member-test.com"));

		verify(remoteUserStore)
				.updateUsersInGroup(eq("GROUP_group"), eq(Collections.singletonList("member-test.com")), anyList());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void changeMembership_failIfCannotUpdateRemoteStoreAdapter() throws Exception {
		mockGroup(true);

		doThrow(RemoteStoreException.class).when(remoteUserStore).updateUsersInGroup(any(), anyList(), anyList());

		listener.processMessage(mockMembershipMessage("emf:GROUP_group", "remove", "emf:member-test.com"));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void changeMembership_failIfCannotReadMessageBody() throws Exception {
		Message message = mockJmsMessage(RemoteUserStoreQueueListener.MODIFY_GROUP_MEMBERS, "");
		doThrow(JMSException.class).when(message).getBody(any());

		listener.processMessage(message);
	}

	private Message mockJmsMessage(String operation, String body) throws JMSException {
		Message message = mock(Message.class);
		when(message.getStringProperty(RemoteUserStoreQueueListener.OPERATION)).thenReturn(operation);
		when(message.getBody(String.class)).thenReturn(body);
		return message;
	}

	private EmfUser mockUser(String userId, boolean userExistsInRemoteSore) throws RemoteStoreException {
		EmfUser user = new EmfUser(userId);
		when(resourceService.findResource(userId)).thenReturn(user);
		when(remoteUserStore.isExistingUser(userId)).thenReturn(userExistsInRemoteSore);
		return user;
	}

	private Resource mockUser(String name) {
		EmfUser user = new EmfUser(name);
		user.setId("emf:" + name);
		return user;
	}

	private static Message mockMembershipMessage(String group, String op, String... member) throws JMSException {
		JsonObjectBuilder data = Json.createObjectBuilder();
		JSON.addIfNotNull(data, GROUP_KEY, group);
		JSON.addIfNotNull(data, op, Arrays.asList(member));
		Message message = mock(Message.class);
		when(message.getBody(eq(String.class))).thenReturn(data.build().toString());
		when(message.getStringProperty(RemoteUserStoreQueueListener.OPERATION))
				.thenReturn(RemoteUserStoreQueueListener.MODIFY_GROUP_MEMBERS);
		return message;
	}

	private Resource mockGroup(boolean active) {
		EmfGroup group = new EmfGroup("GROUP_group", "GROUP_group");
		group.setId("emf:GROUP_group");
		group.setActive(active);
		when(resourceService.findResource("emf:GROUP_group")).thenReturn(group);
		return group;
	}

}