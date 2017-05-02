package com.sirma.itt.seip.permissions.observers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.event.AfterInstanceMoveEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromLibraryChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromParentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.LibraryChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.ParentChange;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

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
	@Captor
	private ArgumentCaptor<Collection<PermissionsChange>> captor;

	@Before
	public void beforeMethod() {
		permissionObserver = new InheritedPermissionObserver();
		MockitoAnnotations.initMocks(this);

		when(roleService.getManagerRole()).thenReturn(SecurityModel.BaseRoles.MANAGER);
		EmfUser user = new EmfUser("testUser");
		user.setId("emf:testUser");
		when(securityContext.getAuthenticated()).thenReturn(user);
	}

	@Test
	public void shouldPopulateLibraryAndSpecialManagerPermissionsWhenParentIsNotEligible() throws Exception {
		TransactionalPermissionChanges permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		parent.setReference(parentReference);

		instance.setOwningInstance(parent);

		EmfInstance library = create("emf:library", false, false);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));

		// this will actually trigger the save
		permissionsChangeBuilder.registerOnTransactionEnd();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(changes.size(), 4);

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
	public void shouldPopulateLibraryAndSpecialManagerPermissionsWhenNoParentIsProvided() throws Exception {
		TransactionalPermissionChanges permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance library = create("emf:parent", false, false);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));
		// this will actually trigger the save
		permissionsChangeBuilder.registerOnTransactionEnd();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(changes.size(), 4);

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
	public void shouldNotPopulateSpecialManagerPermissionsWhenAnEligibleParentIsProvided() {
		TransactionalPermissionChanges permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		parent.setReference(parentReference);

		// make the parent eligible
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(true);

		instance.setOwningInstance(parent);

		EmfInstance library = create("emf:library", false, false);
		when(hierarchyResolver.getLibrary(any())).thenReturn(library.toReference());

		permissionObserver.onAfterInstanceCreated(new AfterInstancePersistEvent<>(instance));

		// this will actually trigger the save
		permissionsChangeBuilder.registerOnTransactionEnd();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(changes.size(), 4);

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
	public void onMoveShouldSetNewParentIfEligible() {
		TransactionalPermissionChanges permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		parent.setReference(parentReference);

		// make the parent eligible
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(true);

		permissionObserver.onInstanceMoved(new AfterInstanceMoveEvent(instance, null, parent));

		// this will actually trigger the save
		permissionsChangeBuilder.registerOnTransactionEnd();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		assertEquals(changes.size(), 1);

		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals(parent.getId(), parentChange.getValue());
	}

	@Test
	public void onMoveShouldRemoveTheParentIfParentIsNotEligible() {
		TransactionalPermissionChanges permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		EmfInstance parent = create("emf:parent", false, false);
		InstanceReference parentReference = new InstanceReferenceMock(parent);
		parent.setReference(parentReference);

		permissionObserver.onInstanceMoved(new AfterInstanceMoveEvent(instance, null, parent));

		// this will actually trigger the save
		permissionsChangeBuilder.registerOnTransactionEnd();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals(null, parentChange.getValue());
	}

	@Test
	public void onMoveShouldRemoveTheParentIfParentIsNoParent() {
		TransactionalPermissionChanges permissionsChangeBuilder = mockPermissionChanges();
		EmfInstance instance = create("emf:instance", true, false);

		permissionObserver.onInstanceMoved(new AfterInstanceMoveEvent(instance, null, null));

		// this will actually trigger the save
		permissionsChangeBuilder.registerOnTransactionEnd();

		verify(permissionService).setPermissions(any(), captor.capture());

		Collection<PermissionsChange> changes = captor.getValue();

		ParentChange parentChange = findChange(changes, ParentChange.class);
		assertEquals(null, parentChange.getValue());
	}

	/**
	 * Mocks the instance used for the tests.
	 *
	 * @param id is the id of the insatnce
	 * @param inheritParentPermissions matters for class instances only. Indicates if parent inheritance is enabled for the instances from
	 * that class
	 * @param inheritLibraryPermissions matters for class instances only. Indicates if parent inheritance is enabled for the instances from
	 * that class
	 * @return the mocked test instance
	 */
	private static EmfInstance create(String id, boolean inheritParentPermissions, boolean inheritLibraryPermissions) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		InstanceReference reference = new InstanceReferenceMock(id, mock(DataTypeDefinition.class), instance);
		instance.setReference(reference);
		InstanceType instanceType = Mockito.mock(InstanceType.class);
		Mockito.when(instanceType.getId()).thenReturn("sampleId");
		when(instanceType.is(anyString())).thenReturn(true);
		when(instanceType.hasTrait(eq(INHERIT_PARENT_PERMISSIONS))).thenReturn(inheritParentPermissions);
		when(instanceType.hasTrait(eq(INHERIT_LIBRARY_PERMISSIONS))).thenReturn(inheritLibraryPermissions);

		instance.setType(instanceType);

		return instance;
	}

	private TransactionalPermissionChanges mockPermissionChanges() {
		TransactionalPermissionChanges permissionsChangeBuilder = new TransactionalPermissionChanges(permissionService,
				new InstanceProxyMock<>(new TransactionSupportFake()));
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(permissionObserver, "permissionsChangeBuilder",
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
}
