package com.sirma.itt.seip.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.InstanceActivatedEvent;
import com.sirma.itt.seip.instance.state.InstanceDeactivatedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirmaenterprise.sep.jms.api.MessageSender;

/**
 * Test for {@link GroupInstanceCreationObserver}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/10/2017
 */
public class GroupInstanceCreationObserverTest {
	@InjectMocks
	private GroupInstanceCreationObserver observer;

	@Mock
	private ResourceService resourceService;
	@Mock
	private TransactionalPermissionChanges transactionalPermissionsChangeBuilder;
	@Mock
	private RemoteUserStoreAdapter remoteUserStore;
	@Mock
	private MessageSender groupCreationMessageSender;
	@Mock
	private MessageSender groupDeletionMessageSender;

	private PermissionsChange.PermissionsChangeBuilder permissionsChangeBuilder;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		permissionsChangeBuilder = spy(PermissionsChange.builder());
		when(transactionalPermissionsChangeBuilder.builder(any())).thenReturn(permissionsChangeBuilder);

		EmfGroup allOther = new EmfGroup("ALL_OTHER", "ALL_OTHER");
		allOther.setId("emf:ALL_OTHER");
		when(resourceService.getAllOtherUsers()).thenReturn(allOther);
	}

	@Test
	public void onAfterInstanceSave_shouldSendMessageToCreateAGroup() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:some_group").toInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		when(resourceService.resourceExists(instance.getId())).thenReturn(Boolean.FALSE);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, new Operation("test", true)));

		verify(resourceService).createGroup(instance);
		verify(permissionsChangeBuilder).addRoleAssignmentChange(eq("emf:ALL_OTHER"), any());
		verify(groupCreationMessageSender).sendText("emf:some_group");
	}

	@Test
	public void onAfterInstanceSave_shouldDoNothingIfGroupExists() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:some_group").toInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		when(resourceService.resourceExists(instance.getId())).thenReturn(Boolean.TRUE);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, new Operation("test", true)));

		verify(resourceService, never()).createGroup(instance);
		verify(permissionsChangeBuilder, never()).addRoleAssignmentChange(eq("emf:ALL_OTHER"), any());
		verify(groupCreationMessageSender, never()).sendText("emf:some_group");
	}

	@Test
	public void onAfterInstanceSave_shouldDoNothingIfNotGroup() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:some_group").toInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.USER);
		instance.setType(type);

		when(resourceService.resourceExists(instance.getId())).thenReturn(Boolean.FALSE);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, new Operation("test", true)));

		verify(resourceService, never()).createGroup(instance);
		verify(permissionsChangeBuilder, never()).addRoleAssignmentChange(eq("emf:ALL_OTHER"), any());
		verify(groupCreationMessageSender, never()).sendText("emf:some_group");
	}

	@Test
	public void onAfterInstanceSave_shouldCreateGroupIfItsNotUserOperation() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:some_group").toInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		when(resourceService.resourceExists(instance.getId())).thenReturn(Boolean.FALSE);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, null, new Operation("test", false)));

		verify(resourceService).createGroup(instance);
		verify(permissionsChangeBuilder).addRoleAssignmentChange(eq("emf:ALL_OTHER"), any());
		verify(groupCreationMessageSender).sendText("emf:some_group");
	}

	@Test
	public void onAfterInstanceSave_shouldUpdateGroupIfUpdate() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:some_group").toInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		when(resourceService.resourceExists(instance.getId())).thenReturn(Boolean.FALSE);

		observer.onAfterInstanceSave(new AfterInstanceSaveEvent(instance, new EmfInstance(), new Operation("test", true)));

		verify(resourceService).updateResource(eq(instance), any());
		verify(resourceService, never()).createGroup(instance);
		verify(permissionsChangeBuilder, never()).addRoleAssignmentChange(eq("emf:ALL_OTHER"), any());
		verify(groupCreationMessageSender, never()).sendText("emf:some_group");
	}

	@Test
	public void onAfterInstanceSave_shouldNotUpdateGroupIfItsNotUserOperation() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:some_group").toInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		when(resourceService.resourceExists(instance.getId())).thenReturn(Boolean.FALSE);

		observer.onAfterInstanceSave(
				new AfterInstanceSaveEvent(instance, new EmfInstance(), new Operation("test", false)));

		verify(resourceService, never()).updateResource(eq(instance), any());
		verify(resourceService, never()).createGroup(instance);
		verify(permissionsChangeBuilder, never()).addRoleAssignmentChange(eq("emf:ALL_OTHER"), any());
		verify(groupCreationMessageSender, never()).sendText("emf:some_group");
	}

	@Test
	public void onNewGroup_createItInRemoteStoreIfNotExist() throws Exception {
		Message message = mock(Message.class);
		when(message.getBody(String.class)).thenReturn("emf:some_group");

		Group group = new EmfGroup("some_group", "some_group");
		when(resourceService.findResource("emf:some_group")).thenReturn(group);
		when(remoteUserStore.isExistingGroup("some_group")).thenReturn(Boolean.FALSE);

		observer.onNewGroup(message);

		verify(remoteUserStore).createGroup(group);
	}

	@Test
	public void onNewGroup_shouldDoNothingIfGroupExistsInRemoteStore() throws Exception {
		Message message = mock(Message.class);
		when(message.getBody(String.class)).thenReturn("emf:some_group");

		Group group = new EmfGroup("some_group", "some_group");
		when(resourceService.findResource("emf:some_group")).thenReturn(group);
		when(remoteUserStore.isExistingGroup("some_group")).thenReturn(Boolean.TRUE);

		observer.onNewGroup(message);

		verify(remoteUserStore, never()).createGroup(group);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onNewGroup_shouldFailIfNotGroup() throws Exception {
		Message message = mock(Message.class);
		when(message.getBody(String.class)).thenReturn("emf:some_user");

		when(resourceService.findResource("emf:some_user")).thenReturn(new EmfUser("some_user"));

		observer.onNewGroup(message);
	}

	@Test
	public void onInstanceDeactivated_shouldClearGroupMembers() {
		Instance instance = new EmfInstance();
		instance.setId("emf:someGroup");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);
		instance.add(ResourceProperties.HAS_MEMBER, "emf:someUser");

		observer.onInstanceDeactivated(new InstanceDeactivatedEvent(instance));

		assertTrue(instance.isValueNull(ResourceProperties.HAS_MEMBER));
		verify(groupDeletionMessageSender).sendText("emf:someGroup");
	}

	@Test
	public void onInstanceDeactivated_shouldDoNothingIfNotGroup() {
		Instance instance = new EmfInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.CASE);
		instance.setType(type);
		instance.add(ResourceProperties.HAS_MEMBER, "emf:someUser");

		observer.onInstanceDeactivated(new InstanceDeactivatedEvent(instance));

		assertFalse(instance.isValueNull(ResourceProperties.HAS_MEMBER));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onDeleteGroup_shouldFailIfNotGroup() throws Exception {
		Message message = mock(Message.class);
		when(message.getBody(String.class)).thenReturn("emf:some_user");

		when(resourceService.findResource("emf:some_user")).thenReturn(new EmfUser("some_user"));

		observer.onDeleteGroup(message);
	}

	@Test
	public void onDeleteGroup_deleteItInRemoteStoreIfExist() throws Exception {
		Message message = mock(Message.class);
		when(message.getBody(String.class)).thenReturn("emf:some_group");

		Group group = new EmfGroup("some_group", "some_group");
		when(resourceService.findResource("emf:some_group")).thenReturn(group);
		when(remoteUserStore.isExistingGroup("some_group")).thenReturn(Boolean.TRUE);

		observer.onDeleteGroup(message);

		verify(remoteUserStore).deleteGroup("some_group");
	}

	@Test
	public void onDeleteGroup_shouldDoNothingIfGroupAlreadyDeletedInRemoteStore() throws Exception {
		Message message = mock(Message.class);
		when(message.getBody(String.class)).thenReturn("emf:some_group");

		Group group = new EmfGroup("some_group", "some_group");
		when(resourceService.findResource("emf:some_group")).thenReturn(group);
		when(remoteUserStore.isExistingGroup("some_group")).thenReturn(Boolean.FALSE);

		observer.onDeleteGroup(message);

		verify(remoteUserStore, never()).deleteGroup("some_group");
	}

	@Test
	public void onInstanceActivated_shouldCreateGroupInRemoteStoreIfNotEveryone() {
		Instance instance = new EmfInstance();
		instance.setId("emf:someGroup");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		observer.onInstanceActivated(new InstanceActivatedEvent(instance));

		verify(groupCreationMessageSender).sendText("emf:someGroup");
	}

	@Test
	public void onInstanceActivated_shouldNotCreateGroupInRemoteStoreIfEveryone() {
		Instance instance = new EmfInstance();
		instance.setId("sec:" + ResourceService.EVERYONE_GROUP_ID);
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);
		when(resourceService.areEqual(any(), any())).thenReturn(Boolean.TRUE);

		observer.onInstanceActivated(new InstanceActivatedEvent(instance));

		verify(groupCreationMessageSender, never()).sendText("sec:" + ResourceService.EVERYONE_GROUP_ID);
	}
}
