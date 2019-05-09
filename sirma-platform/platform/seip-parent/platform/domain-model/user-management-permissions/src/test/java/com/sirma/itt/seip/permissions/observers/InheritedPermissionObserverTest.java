package com.sirma.itt.seip.permissions.observers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.event.ParentChangedEvent;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromLibraryChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromParentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.LibraryChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.ParentChange;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.TransactionalPermissionChangesFake;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test the functionality of {@link InheritedPermissionObserver}
 *
 * @author BBonev
 * @author Vilizar Tsonev
 */
public class InheritedPermissionObserverTest {

	@InjectMocks
	private InheritedPermissionObserver permissionObserver;
	@Mock
	private PermissionService permissionService;
	@Mock
	private RoleService roleService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private InstancePermissionsHierarchyResolver hierarchyResolver;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Spy
	private InstanceContextServiceMock contextService;
	@Captor
	private ArgumentCaptor<Collection<PermissionsChange>> captor;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(roleService.getManagerRole()).thenReturn(SecurityModel.BaseRoles.MANAGER);
		EmfUser user = new EmfUser("testUser");
		user.setId("emf:testUser");
		when(securityContext.getAuthenticated()).thenReturn(user);
	}

	@Test
	public void onAfterInstanceCreated_shouldDoNothingIfUserInstanceIsSaved() throws Exception {
		TransactionalPermissionChanges permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:someUser", true, false);
		ClassInstance type = (ClassInstance) instance.type();
		type.setCategory(ObjectTypes.USER);

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));

		verify(hierarchyResolver, never()).getLibrary(any());
	}

	@Test
	public void onAfterInstanceCreated_shouldPopulateLibraryAndSpecialManagerPermissionsWhenParentIsNotEligible()
			throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance parent = create("emf:parent", false, false);
		contextService.bindContext(instance, parent);

		EmfInstance library = create("emf:library", false, false);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));

		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(5, changes.size());

		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals("Parent inheritance should be disabled if parent is not eligible", false,
					 inheritFromParentChange.getValue());

		LibraryChange libraryChange = findChange(changes, LibraryChange.class);
		assertEquals(library.getId(), libraryChange.getValue());

		InheritFromLibraryChange inheritFromLibraryChange = findChange(changes, InheritFromLibraryChange.class);
		assertEquals(false, inheritFromLibraryChange.getValue());

		AddRoleAssignmentChange addRoleAssignmentChange = findChange(changes, AddRoleAssignmentChange.class);
		assertEquals(SecurityModel.BaseRoles.MANAGER.getIdentifier(), addRoleAssignmentChange.getRole());
	}

	@Test
	public void onAfterInstanceCreated_shouldPopulateLibraryAndSpecialManagerPermissionsWhenNoParentIsProvided()
			throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance library = create("emf:parent", false, false);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));
		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(4, changes.size());

		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals(true, inheritFromParentChange.getValue());

		LibraryChange libraryChange = findChange(changes, LibraryChange.class);
		assertEquals(library.getId(), libraryChange.getValue());

		InheritFromLibraryChange inheritFromLibraryChange = findChange(changes, InheritFromLibraryChange.class);
		assertEquals(false, inheritFromLibraryChange.getValue());

		AddRoleAssignmentChange addRoleAssignmentChange = findChange(changes, AddRoleAssignmentChange.class);
		assertEquals(SecurityModel.BaseRoles.MANAGER.getIdentifier(), addRoleAssignmentChange.getRole());
	}

	@Test
	public void onAfterInstanceCreated_shouldPopulateSpecialManagerPermissionsEventWithParentProvided()
			throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", false, false);

		EmfInstance library = create("emf:parent", false, false);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));
		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(4, changes.size());

		LibraryChange libraryChange = findChange(changes, LibraryChange.class);
		assertEquals(library.getId(), libraryChange.getValue());

		InheritFromLibraryChange inheritFromLibraryChange = findChange(changes, InheritFromLibraryChange.class);
		assertEquals(false, inheritFromLibraryChange.getValue());

		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals(false, inheritFromParentChange.getValue());

		AddRoleAssignmentChange addRoleAssignmentChange = findChange(changes, AddRoleAssignmentChange.class);
		assertEquals(SecurityModel.BaseRoles.MANAGER.getIdentifier(), addRoleAssignmentChange.getRole());
	}

	@Test
	public void onAfterInstanceCreated_shouldNotPopulateSpecialManagerPermissionsWhenAnEligibleParentIsProvided()
			throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance parent = create("emf:parent", false, false);
		contextService.bindContext(instance, parent);

		// make the parent eligible
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(true);

		EmfInstance library = create("emf:library", false, false);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));

		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(4, changes.size());

		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals(true, inheritFromParentChange.getValue());

		LibraryChange libraryChange = findChange(changes, LibraryChange.class);
		assertEquals(library.getId(), libraryChange.getValue());

		InheritFromLibraryChange inheritFromLibraryChange = findChange(changes, InheritFromLibraryChange.class);
		assertEquals(false, inheritFromLibraryChange.getValue());

		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals(parent.getId(), parentChange.getValue());
	}

	@Test
	public void onMoveShouldSetNewParentIfEligible() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);
		InstanceReference reference = new InstanceReferenceMock(instance);
		instance.setReference(reference);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		ReflectionUtils.setFieldValue(parent, "reference", parentReference);

		when(permissionService.getPermissionsInfo(reference))
				.thenReturn(Optional.of(new EntityPermissions("emf:instance", "emf:parent", "", false, false,false)));

		// make the parent eligible
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(true);

		permissionObserver.onParentChanged(new ParentChangedEvent(instance, null, parent));

		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(1, changes.size());

		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals(parent.getId(), parentChange.getValue());
	}

	@Test
	public void onMoveShouldSetNewParentInheritanceIfEligible() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);
		InstanceReference reference = new InstanceReferenceMock(instance);
		instance.setReference(reference);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		ReflectionUtils.setFieldValue(parent, "reference", parentReference);

		when(permissionService.getPermissionsInfo(reference))
				.thenReturn(Optional.of(new EntityPermissions("emf:instance", "emf:parent", "", true, false,false)));

		// make the parent eligible
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(false);

		permissionObserver.onParentChanged(new ParentChangedEvent(instance, null, parent));

		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(2, changes.size());

		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals(parent.getId(), parentChange.getValue());
		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals(false, inheritFromParentChange.getValue());
	}

	@Test
	public void onMoveShouldRemoveTheParentIfParentIsNotEligible() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);
		InstanceReference reference = new InstanceReferenceMock(instance);
		instance.setReference(reference);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		parent.setReference(parentReference);

		when(permissionService.getPermissionsInfo(reference))
				.thenReturn(Optional.of(new EntityPermissions("emf:instance", "emf:parent", "", false, false,false)));

		permissionObserver.onParentChanged(new ParentChangedEvent(instance, null, parent));

		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals("Should have only the parent change", 1, changes.size());
		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals("emf:parent", parentChange.getValue());
	}

	@Test
	public void onMoveShouldRemoveTheParentIfParentIsNoParent() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);
		InstanceReference reference = new InstanceReferenceMock(instance);
		instance.setReference(reference);

		when(permissionService.getPermissionsInfo(any())).thenReturn(Optional.empty());

		permissionObserver.onParentChanged(new ParentChangedEvent(instance, null, null));

		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals(null, parentChange.getValue());
	}

	@Test
	public void should_SetManagersRoleAsSpecialPermissions_When_ParentIsRemoved() {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);
		InstanceReference reference = new InstanceReferenceMock(instance);
		instance.setReference(reference);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		parent.setReference(parentReference);

		// users with manager role.
		String managerOne = "emf:managerOne@tenant.bg";
		String managerTwo = "emf:managerTwo@tenant.bg";


		// users with not manager role.
		String contributor = "emf:contributor@tenant.bg";
		String administrator = "emf:administrator@tenant.bg";
		String collaborator = "emf:collaborator@tenant.bg";
		String creator = "emf:creator@tenant.bg";
		String consumer = "emf:consumer@tenant.bg";
		String viewer = "emf:viewer@tenant.bg";

		Map<String, ResourceRole> oldPermissionAssignments = new HashMap<>(3);
		addPermissionAssignments(oldPermissionAssignments, managerOne, "MANAGER");
		addPermissionAssignments(oldPermissionAssignments, contributor, "CONTRIBUTOR");
		addPermissionAssignments(oldPermissionAssignments, managerTwo, "MANAGER");
		addPermissionAssignments(oldPermissionAssignments, administrator, "ADMINISTRATOR");
		addPermissionAssignments(oldPermissionAssignments, collaborator, "COLLABORATOR");
		addPermissionAssignments(oldPermissionAssignments, creator, "CREATOR");
		addPermissionAssignments(oldPermissionAssignments, consumer, "CONSUMER");
		addPermissionAssignments(oldPermissionAssignments, viewer, "VIEWER");

		when(permissionService.getPermissionAssignments(reference)).thenReturn(oldPermissionAssignments);

		permissionObserver.onParentChanged(new ParentChangedEvent(instance, parent, null));

		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		// verify than new parent is null.
		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertNull(parentChange.getValue());

		// verify that all non managers are not set.
		List<String> allAddRoleAssignmentChange = findAllAddRoleAssignmentChange(changes);
		assertTrue(allAddRoleAssignmentChange.contains(managerOne));
		assertTrue(allAddRoleAssignmentChange.contains(managerTwo));
		assertFalse(allAddRoleAssignmentChange.contains(contributor));
		assertFalse(allAddRoleAssignmentChange.contains(administrator));
		assertFalse(allAddRoleAssignmentChange.contains(collaborator));
		assertFalse(allAddRoleAssignmentChange.contains(contributor));
		assertFalse(allAddRoleAssignmentChange.contains(creator));
		assertFalse(allAddRoleAssignmentChange.contains(consumer));
		assertFalse(allAddRoleAssignmentChange.contains(viewer));
	}

	@Test
	public void onLibraryChange_shouldDoNothingIfLibraryIsTheSame() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", false, false);

		permissionObserver.onLibraryChange(new AfterInstanceSaveEvent(instance, instance, new Operation(
				ActionTypeConstants.CHANGE_TYPE)));
		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void onLibraryChange_shouldDoNothingIfDuringCreate() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", false, false);

		permissionObserver.onLibraryChange(new AfterInstanceSaveEvent(instance, null, new Operation(
				ActionTypeConstants.CREATE)));
		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void onLibraryChange_shouldUpdateLibraryPermissions() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instanceToSave = create("emf:instance", false, true);
		((ClassInstance)instanceToSave.type()).setId("someOtherType");
		EmfInstance oldInstance = create("emf:instance", false, false);

		EmfInstance library = create("emf:library", false, true);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onLibraryChange(new AfterInstanceSaveEvent(instanceToSave, oldInstance, new Operation(
				ActionTypeConstants.CHANGE_TYPE)));
		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(3, changes.size());

		LibraryChange libraryChange = findChange(changes, LibraryChange.class);
		assertEquals(library.getId(), libraryChange.getValue());

		InheritFromLibraryChange inheritFromLibraryChange = findChange(changes, InheritFromLibraryChange.class);
		assertEquals(true, inheritFromLibraryChange.getValue());

		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals(false, inheritFromParentChange.getValue());
	}

	@Test
	public void onLibraryChange_shouldUpdateParentPermissions_ifParentApplicable() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instanceToSave = create("emf:instance", true, true);
		((ClassInstance)instanceToSave.type()).setId("someOtherType");
		EmfInstance oldInstance = create("emf:instance", false, false);

		EmfInstance library = create("emf:library", true, true);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());
		EmfInstance parent = create("emf:parent", false, false);
		when(contextService.getContext(instanceToSave)).thenReturn(Optional.of(parent.toReference()));
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(Boolean.TRUE);

		permissionObserver.onLibraryChange(new AfterInstanceSaveEvent(instanceToSave, oldInstance, new Operation(
				ActionTypeConstants.CHANGE_TYPE)));
		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(3, changes.size());

		LibraryChange libraryChange = findChange(changes, LibraryChange.class);
		assertEquals(library.getId(), libraryChange.getValue());

		InheritFromLibraryChange inheritFromLibraryChange = findChange(changes, InheritFromLibraryChange.class);
		assertEquals(true, inheritFromLibraryChange.getValue());

		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals(true, inheritFromParentChange.getValue());
	}

	@Test
	public void onLibraryChange_shouldNotEnableParentPermissions_ifParentIsNotApplicable() throws Exception {
		TransactionalPermissionChangesFake permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instanceToSave = create("emf:instance", true, true);
		((ClassInstance)instanceToSave.type()).setId("someOtherType");
		EmfInstance oldInstance = create("emf:instance", false, false);

		EmfInstance library = create("emf:library", true, true);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());
		EmfInstance parent = create("emf:parent", false, false);
		when(contextService.getContext(instanceToSave)).thenReturn(Optional.of(parent.toReference()));
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(Boolean.FALSE);

		permissionObserver.onLibraryChange(new AfterInstanceSaveEvent(instanceToSave, oldInstance, new Operation(
				ActionTypeConstants.CHANGE_TYPE)));
		// this will actually trigger the save
		permissionsChangeBuilder.beforeCompletion();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(3, changes.size());

		LibraryChange libraryChange = findChange(changes, LibraryChange.class);
		assertEquals(library.getId(), libraryChange.getValue());

		InheritFromLibraryChange inheritFromLibraryChange = findChange(changes, InheritFromLibraryChange.class);
		assertEquals(true, inheritFromLibraryChange.getValue());

		InheritFromParentChange inheritFromParentChange = findChange(changes, InheritFromParentChange.class);
		assertEquals(false, inheritFromParentChange.getValue());
	}

	private void addPermissionAssignments(Map<String, ResourceRole> permissionAssignments, String authority,
			String roleIdentifier) {
		permissionAssignments.put(authority, createResourceRole(authority, roleIdentifier));
	}

	private ResourceRole createResourceRole(String authority, String roleIdentifier) {
		ResourceRole resourceRole = new ResourceRole();
		resourceRole.setAuthorityId(authority);
		resourceRole.setRole(SecurityModel.BaseRoles.getIdentifier(roleIdentifier));
		return resourceRole;
	}

	/**
	 * Mocks the instance used for the tests.
	 *
	 * @param id
	 *            is the id of the insatnce
	 * @param inheritParentPermissions
	 *            matters for class instances only. Indicates if parent inheritance is enabled for the instances from
	 * that class
	 * @param inheritLibraryPermissions
	 *            matters for class instances only. Indicates if parent inheritance is enabled for the instances from
	 * that class
	 * @return the mocked test instance
	 */
	private static EmfInstance create(String id, boolean inheritParentPermissions, boolean inheritLibraryPermissions) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		InstanceReference reference = new InstanceReferenceMock(id, mock(DataTypeDefinition.class), instance);
		ReflectionUtils.setFieldValue(instance, "reference", reference);
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId("sampleId");
		instanceType.add(INHERIT_PARENT_PERMISSIONS, inheritParentPermissions);
		instanceType.add(INHERIT_LIBRARY_PERMISSIONS, inheritLibraryPermissions);

		instance.setType(instanceType);

		return instance;
	}

	private TransactionalPermissionChangesFake mockPermissionChanges() {
		TransactionalPermissionChangesFake permissionsChangeBuilder = new TransactionalPermissionChangesFake();
		permissionsChangeBuilder.setPermissionService(permissionService);
		ReflectionUtils.setFieldValue(permissionObserver, "permissionsChangeBuilder",
				permissionsChangeBuilder);
		return permissionsChangeBuilder;
	}

	@SuppressWarnings("unchecked")
	private <T extends PermissionsChange> T findChange(Collection<PermissionsChange> changes, Class<T> type) {
		for (PermissionsChange permissionsChange : changes) {
			if (permissionsChange.getClass().isAssignableFrom(type)) {
				return (T) permissionsChange;
			}
		}

		fail("No change + of type " + type + " is found");
		return null;
	}

	private List<String> findAllAddRoleAssignmentChange(Collection<PermissionsChange> changes) {
		return changes.stream()
				.filter(permissionsChange -> permissionsChange instanceof AddRoleAssignmentChange)
				.map(permissionsChange -> ((AddRoleAssignmentChange) permissionsChange).getAuthority())
				.collect(Collectors.toList());

	}
}
