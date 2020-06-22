package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.resources.ResourceProperties.USER_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;

/**
 * Tests for {@link UserInstanceCreationObserver}.
 *
 * @author smustafov
 */
public class UserInstanceCreationObserverTest {

	private static final String TENANT_ID = "tenant.com";
	private static final String ALL_OTHERS_GROUP_ID = "sec:allOthers";

	@InjectMocks
	private UserInstanceCreationObserver observer;

	@Mock
	private ResourceService resourceService;

	@Mock
	private TransactionalPermissionChanges transactionalPermissionsChangeBuilder;

	private PermissionsChangeBuilder permissionsBuilder;

	@Mock
	private MessageSender remoteUserStoreMessageSender;

	private Operation editOperation = new Operation(ActionTypeConstants.EDIT_DETAILS, ActionTypeConstants.EDIT_DETAILS,
			true);

	@Before
	public void before() {
		initMocks(this);

		EmfGroup allOthers = new EmfGroup(ALL_OTHERS_GROUP_ID, ALL_OTHERS_GROUP_ID);
		allOthers.setId(ALL_OTHERS_GROUP_ID);
		when(resourceService.getAllOtherUsers()).thenReturn(allOthers);

		permissionsBuilder = PermissionsChange.builder();
		when(transactionalPermissionsChangeBuilder.builder(any(InstanceReference.class)))
				.thenReturn(permissionsBuilder);

		when(remoteUserStoreMessageSender.getDefaultSendOptions()).thenReturn(SendOptions.create());
	}

	@Test
	public void onAfterInstanceSave_ShouldDoNothing_When_InstanceIsNotUser() {
		EmfInstance instance = createInstance(null, "task");

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, editOperation));
		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, instance, editOperation));

		verify(resourceService, never()).createUser(any(User.class));
	}

	@Test
	public void onAfterInstanceSave_ShouldCreateUser_When_OperationIsNotTriggeredByUser() {
		String instanceId = "emf:john-" + TENANT_ID;
		EmfInstance instance = createInstance(instanceId, "user");
		instance.add(USER_ID, "john@" + TENANT_ID);
		instance.setReference(InstanceReferenceMock.createGeneric(instanceId));

		when(resourceService.resourceExists(any())).thenReturn(Boolean.FALSE);
		EmfUser user = new EmfUser();
		user.setId(instanceId);
		when(resourceService.createUser(instance)).thenReturn(user);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, Operation.NO_OPERATION));

		verify(resourceService).createUser(any(User.class));
		verifyMessageSender(RemoteUserStoreQueueListener.CREATE_USER, instanceId);
	}

	@Test
	public void onAfterInstanceSave_ShouldCreateUserInLocalAndRemoteStore_ifUserDoesNotExists() {
		String instanceId = "emf:john-" + TENANT_ID;
		EmfInstance instance = createInstance(instanceId, "user");
		instance.add(USER_ID, "john@" + TENANT_ID);
		instance.setReference(InstanceReferenceMock.createGeneric(instanceId));
		when(resourceService.resourceExists(any())).thenReturn(Boolean.FALSE);
		EmfUser user = new EmfUser();
		user.setId(instanceId);
		when(resourceService.createUser(instance)).thenReturn(user);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, editOperation));

		verify(resourceService).createUser(any(User.class));
		verifyMessageSender(RemoteUserStoreQueueListener.CREATE_USER, instanceId);
	}

	@Test
	public void onAfterInstanceSave_ShouldNotCreateUserInLocalAndRemoteStore_ifUserDoesExists() {
		String instanceId = "emf:john-" + TENANT_ID;
		EmfInstance instance = createInstance(instanceId, "user");
		instance.add(USER_ID, "john@" + TENANT_ID);
		instance.setReference(InstanceReferenceMock.createGeneric(instanceId));
		when(resourceService.resourceExists(any())).thenReturn(Boolean.TRUE);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, editOperation));

		verify(resourceService, never()).createUser(any(User.class));
		verify(remoteUserStoreMessageSender, never()).sendText(instanceId);
	}

	@Test
	public void onAfterInstanceSave_ShouldUpdateUser_When_UserIsNotNew() {
		String instanceId = "user";
		EmfInstance instance = createInstance(instanceId, instanceId);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, instance, editOperation));

		verify(resourceService).updateResource(instance, editOperation);
		verifyMessageSender(RemoteUserStoreQueueListener.UPDATE_USER, instanceId);
	}

	@Test
	public void onAfterInstanceSave_ShouldNotUpdateUser_When_ItsNotUserOperation() {
		String instanceId = "user";
		EmfInstance instance = createInstance(instanceId, instanceId);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, instance, Operation.NO_OPERATION));

		verify(resourceService, never()).updateResource(any(Instance.class), any(Operation.class));
		verify(remoteUserStoreMessageSender, never()).sendText(anyString(), any(SendOptions.class));
	}

	@Test
	public void onAfterInstanceSave_ShouldSetPermissionsForCreatedUser() {
		String instanceId = "emf:john-" + TENANT_ID;
		EmfInstance instance = createInstance(instanceId, "user");
		instance.add(USER_ID, "john@" + TENANT_ID);
		instance.setReference(InstanceReferenceMock.createGeneric(instanceId));

		EmfUser user = new EmfUser();
		user.setId(instanceId);
		when(resourceService.createUser(instance)).thenReturn(user);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, editOperation));

		List<PermissionsChange> permissionsChanges = permissionsBuilder.build();
		assertEquals(2, permissionsChanges.size());

		AddRoleAssignmentChange managerPermissions = (AddRoleAssignmentChange) permissionsChanges.get(0);
		assertEquals(SecurityModel.BaseRoles.MANAGER.getIdentifier(), managerPermissions.getRole());
		assertEquals(instanceId, managerPermissions.getAuthority());

		AddRoleAssignmentChange consumerPermissions = (AddRoleAssignmentChange) permissionsChanges.get(1);
		assertEquals(SecurityModel.BaseRoles.CONSUMER.getIdentifier(), consumerPermissions.getRole());
		assertEquals(ALL_OTHERS_GROUP_ID, consumerPermissions.getAuthority());
	}

	@Test
	public void onAfterInstanceSave_ShouldAddNewUser_ToEveryoneGroup() {
		String instanceId = "user";
		EmfInstance instance = createInstance(instanceId, instanceId);
		instance.setReference(InstanceReferenceMock.createGeneric(instanceId));

		EmfUser user = new EmfUser();
		user.setId(instanceId);
		when(resourceService.createUser(instance)).thenReturn(user);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, editOperation));

		verifyUserAddedToEveryoneGroup(instanceId);
	}

	private void verifyUserAddedToEveryoneGroup(String instanceId) {
		ArgumentCaptor<Resource> resourceArgumentCaptor = ArgumentCaptor.forClass(Resource.class);
		ArgumentCaptor<Collection> addMembersArgumentCaptor = ArgumentCaptor.forClass(Collection.class);
		ArgumentCaptor<Collection> removeMembersArgumentCaptor = ArgumentCaptor.forClass(Collection.class);

		verify(resourceService).modifyMembers(resourceArgumentCaptor.capture(), addMembersArgumentCaptor.capture(),
				removeMembersArgumentCaptor.capture());

		assertEquals(ALL_OTHERS_GROUP_ID, resourceArgumentCaptor.getValue().getId());
		assertEquals(Collections.singleton(instanceId), addMembersArgumentCaptor.getValue());
		assertTrue(removeMembersArgumentCaptor.getValue().isEmpty());
	}

	private static EmfInstance createInstance(String id, String category) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		instance.setType(InstanceTypeFake.buildForCategory(category));
		return instance;
	}

	private void verifyMessageSender(String operation, String expected) {
		ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<SendOptions> optionsCaptor = ArgumentCaptor.forClass(SendOptions.class);

		verify(remoteUserStoreMessageSender).sendText(dataCaptor.capture(), optionsCaptor.capture());

		assertEquals(operation, optionsCaptor.getValue().getProperties().get(RemoteUserStoreQueueListener.OPERATION));
		assertEquals(expected, dataCaptor.getValue());
	}

}
