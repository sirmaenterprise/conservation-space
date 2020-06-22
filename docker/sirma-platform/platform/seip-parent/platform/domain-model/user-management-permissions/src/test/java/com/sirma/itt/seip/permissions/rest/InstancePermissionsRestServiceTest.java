package com.sirma.itt.seip.permissions.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromLibraryChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromParentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.RemoveRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleAssignments;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link InstancePermissionsRestService}
 *
 * @author BBonev
 */
public class InstancePermissionsRestServiceTest {

	private static final String INSTANCE_ID = "emf:instanceId";
	@InjectMocks
	private InstancePermissionsRestService restService;
	@Mock
	private PermissionService permissionService;
	@Mock
	private RoleService roleService;
	@Mock
	private AuthorityService authorityService;
	@Mock
	private InstanceTypeResolver typeResolver;
	@Mock
	private ResourceService resourceService;
	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;
	@Mock
	private InstanceTypes instanceTypes;
	@Captor
	private ArgumentCaptor<Collection<PermissionsChange>> captor;
	@Mock
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(roleService.getRoleIdentifier(anyString())).then(a -> new RoleId(a.getArgumentAt(0, String.class), 0));
	}

	@Test(expected = ResourceException.class)
	public void loadPermissions_notValidInstance() throws Exception {
		mockNonExistingInstance(INSTANCE_ID);

		restService.loadPermissions(INSTANCE_ID, true, true);
	}

	@Test
	public void loadPermissions() throws Exception {
		mockExistingInstance(INSTANCE_ID);
		mockPermissionsForLoading();
		mockInstanceTypes(true, true);

		Permissions permissions = restService.loadPermissions(INSTANCE_ID, null, null);
		verifyPermissions(permissions, true);
		assertTrue(permissions.isAllowInheritLibraryPermissions());
		assertTrue(permissions.isAllowInheritParentPermissions());

		permissions = restService.loadPermissions(INSTANCE_ID, null, null);
		assertNotNull(permissions);
		assertTrue(permissions.isInheritedPermissions());

		permissions = restService.loadPermissions(INSTANCE_ID, null, null);
		assertNotNull(permissions);
		assertTrue(permissions.isInheritedPermissions());
	}

	private static void verifyPermissions(Permissions permissions, boolean isManager) {
		assertNotNull(permissions);

		assertTrue(permissions.isEditAllowed());
		assertTrue(permissions.isRestoreAllowed());
		assertFalse(permissions.isInheritedPermissions());
		assertFalse(permissions.isRoot());

		PermissionEntry permissionEntry = permissions.getForAuthority("emf:user1");
		assertNotNull(permissionEntry);
		assertEquals(isManager, permissionEntry.isManager());
		assertEquals("CONSUMER", permissionEntry.getSpecial());
		assertEquals("COLLABORATOR", permissionEntry.getInherited());
		assertEquals("MANAGER", permissionEntry.getLibrary());
		if (isManager) {
			assertEquals("MANAGER", permissionEntry.getCalculated());
		}

		assertNotNull(permissionEntry.getAuthority());
		assertEquals("emf:user1", permissionEntry.getAuthority());
	}

	@SuppressWarnings("boxing")
	private void mockPermissionsForLoading() {
		when(permissionService.getPermissionModel(any())).thenReturn(new PermissionModelType(false, false, true),
				new PermissionModelType(true, false, false), new PermissionModelType(true, false, true));
		when(authorityService.getAllowedActionNames(any(Instance.class), any())).thenReturn(new HashSet<>(
				Arrays.asList(ActionTypeConstants.MANAGE_PERMISSIONS, ActionTypeConstants.RESTORE_PERMISSIONS)));
		when(permissionService.checkIsRoot(any())).thenReturn(Boolean.FALSE);

		Map<String, ResourceRole> permissionAssignments = new HashMap<>();

		ResourceRole resourceRole = new ResourceRole();
		RoleAssignments assignments = new RoleAssignments("MANAGER");
		assignments.addAssignment("CONSUMER", PermissionModelType.SPECIAL);
		assignments.addAssignment("COLLABORATOR", PermissionModelType.INHERITED);
		assignments.addAssignment("MANAGER", PermissionModelType.LIBRARY);
		resourceRole.setRoleAssignments(assignments);

		final String AUTHORITY_ID = "emf:user1";
		resourceRole.setAuthorityId(AUTHORITY_ID);

		permissionAssignments.put(AUTHORITY_ID, resourceRole);

		when(permissionService.getPermissionAssignments(any(InstanceReference.class), any(Boolean.class),
				any(Boolean.class))).thenReturn(permissionAssignments);

		when(roleService.isManagerRole(any(RoleIdentifier.class)))
				.then(a -> a.getArgumentAt(0, RoleIdentifier.class).getIdentifier().equals("MANAGER"));
	}

	@Test(expected = ResourceException.class)
	public void savePermissions_InvalidInstance() throws Exception {
		mockNonExistingInstance(INSTANCE_ID);

		restService.save(INSTANCE_ID, new Permissions());
	}

	@Test
	public void savePermissions() throws Exception {
		mockExistingInstance(INSTANCE_ID);
		mockPermissionsForLoading();
		mockInstanceTypes(true, true);

		when(resourceService.getAllOtherUsers()).then(a -> {
			EmfGroup group = new EmfGroup("emf:allOthers", "emf:allOthers");
			group.setId("emf:allOthers");
			return group;
		});
		when(instanceAccessEvaluator.actionAllowed(any(InstanceReference.class),
				eq(ActionTypeConstants.MANAGE_PERMISSIONS))).thenReturn(Boolean.TRUE);

		Permissions permissions = new Permissions();
		permissions.setInheritedPermissions(false);
		permissions.setInheritedLibraryPermissions(true);
		permissions.getForAuthority("admin").setSpecial("consumer");
		permissions.getForAuthority("third_party").setSpecial("manager");

		EntityPermissions entityPermission = new EntityPermissions(INSTANCE_ID, null, null, true, false, false);

		entityPermission.addAssignment("admin", "manager");
		entityPermission.addAssignment("regular_user", "collaborator");

		when(permissionService.getPermissionsInfo(any())).thenReturn(Optional.of(entityPermission));

		// mock for missing parent
		mockNonExistingInstance(null);

		Permissions saved = restService.save(INSTANCE_ID, permissions);

		verify(permissionService).setPermissions(any(), captor.capture());

		List<PermissionsChange> changes = (List<PermissionsChange>) captor.getValue();
		assertEquals(5, changes.size());

		AddRoleAssignmentChange change1 = (AddRoleAssignmentChange) changes.get(0);
		assertEquals("admin", change1.getAuthority());
		assertEquals("consumer", change1.getRole());

		AddRoleAssignmentChange change2 = (AddRoleAssignmentChange) changes.get(1);
		assertEquals("third_party", change2.getAuthority());
		assertEquals("manager", change2.getRole());

		RemoveRoleAssignmentChange change3 = (RemoveRoleAssignmentChange) changes.get(2);
		assertEquals("regular_user", change3.getAuthority());
		assertEquals("collaborator", change3.getRole());

		InheritFromParentChange change4 = (InheritFromParentChange) changes.get(3);
		assertFalse(change4.getValue());

		InheritFromLibraryChange change5 = (InheritFromLibraryChange) changes.get(4);
		assertTrue(change5.getValue());

		verifyPermissions(saved, true);
	}

	@Test
	public void savePermissions_shouldSetParentInheritanceIfParentIsEligible() throws Exception {
		mockExistingInstance(INSTANCE_ID);
		mockPermissionsForLoading();
		mockInstanceTypes(true, true);

		when(resourceService.getAllOtherUsers()).then(a -> {
			EmfGroup group = new EmfGroup("emf:allOthers", "emf:allOthers");
			group.setId("emf:allOthers");
			return group;
		});
		when(instanceAccessEvaluator.actionAllowed(any(InstanceReference.class),
				eq(ActionTypeConstants.MANAGE_PERMISSIONS))).thenReturn(Boolean.TRUE);

		Permissions permissions = new Permissions();
		permissions.setInheritedPermissions(true);
		permissions.setInheritedLibraryPermissions(true);

		EntityPermissions entityPermission = new EntityPermissions(INSTANCE_ID, "emf:parent", null, false, true, false);

		when(permissionService.getPermissionsInfo(any())).thenReturn(Optional.of(entityPermission));

		mockExistingInstance("emf:parent");
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(Boolean.TRUE);

		restService.save(INSTANCE_ID, permissions);

		verify(permissionService).setPermissions(any(), captor.capture());

		List<PermissionsChange> changes = (List<PermissionsChange>) captor.getValue();
		assertEquals(1, changes.size());

		InheritFromParentChange change4 = (InheritFromParentChange) changes.get(0);
		assertTrue("Parent inheritance should be enabled", change4.getValue());
	}

	@Test
	public void savePermissions_shouldDisableParentInheritanceIfParentIsNotEligible() throws Exception {
		mockExistingInstance(INSTANCE_ID);
		mockPermissionsForLoading();
		mockInstanceTypes(true, true);

		when(resourceService.getAllOtherUsers()).then(a -> {
			EmfGroup group = new EmfGroup("emf:allOthers", "emf:allOthers");
			group.setId("emf:allOthers");
			return group;
		});
		when(instanceAccessEvaluator.actionAllowed(any(InstanceReference.class),
				eq(ActionTypeConstants.MANAGE_PERMISSIONS))).thenReturn(Boolean.TRUE);

		Permissions permissions = new Permissions();
		permissions.setInheritedPermissions(true);
		permissions.setInheritedLibraryPermissions(true);

		EntityPermissions entityPermission = new EntityPermissions(INSTANCE_ID, "emf:parent", null, false, true, false);

		when(permissionService.getPermissionsInfo(any())).thenReturn(Optional.of(entityPermission));

		mockExistingInstance("emf:parent");
		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(Boolean.FALSE);

		restService.save(INSTANCE_ID, permissions);

		verify(permissionService).setPermissions(any(), captor.capture());

		List<PermissionsChange> changes = (List<PermissionsChange>) captor.getValue();
		assertEquals(1, changes.size());

		InheritFromParentChange change1 = (InheritFromParentChange) changes.get(0);
		assertFalse("Parent inheritance should be disabled", change1.getValue());
	}

	@Test(expected = NoPermissionsException.class)
	public void savePermissions_notAllowed() throws Exception {
		mockExistingInstance(INSTANCE_ID);
		mockPermissionsForLoading();

		when(resourceService.getAllOtherUsers()).then(a -> {
			EmfGroup group = new EmfGroup("emf:allOthers", "emf:allOthers");
			group.setId("emf:allOthers");
			return group;
		});
		when(instanceAccessEvaluator.actionAllowed(any(InstanceReference.class),
				eq(ActionTypeConstants.MANAGE_PERMISSIONS))).thenReturn(Boolean.FALSE);

		Permissions permissions = new Permissions();
		permissions.getForAuthority("emf:user1").setSpecial("MANAGER");
		permissions.getForAuthority("emf:allOthers").setSpecial("NO_PERMISSIONS");

		restService.save(INSTANCE_ID, permissions);
	}

	@Test(expected = ResourceException.class)
	public void restorePermissions_nonExistingInstance() throws Exception {
		mockNonExistingInstance(INSTANCE_ID);
		restService.restorePermissions(INSTANCE_ID);
	}

	@Test
	public void getRoles() throws Exception {
		when(roleService.getActiveRoles()).thenReturn(Arrays.asList(mock(RoleIdentifier.class)));

		Collection<RoleIdentifier> roles = restService.getActiveRoles();

		assertNotNull(roles);
		verify(roleService).getActiveRoles();
	}

	private void mockInstanceTypes(boolean inheritParentPermissions, boolean inheritLibraryPermissions) {
		ClassInstance classInstance = mock(ClassInstance.class);
		when(classInstance.hasTrait(eq(INHERIT_PARENT_PERMISSIONS))).thenReturn(inheritParentPermissions);
		when(classInstance.hasTrait(eq(INHERIT_LIBRARY_PERMISSIONS))).thenReturn(inheritLibraryPermissions);
		Optional<InstanceType> optional = Optional.of(classInstance);
		when(instanceTypes.from(Mockito.anyString())).thenReturn(optional);
	}

	private void mockExistingInstance(String instanceId) {
		InstanceType instanceType = Mockito.mock(InstanceType.class);
		Mockito.when(instanceType.getId()).thenReturn("sampleId");
		when(instanceType.is(eq("classinstance"))).thenReturn(true);

		EmfInstance instance = new EmfInstance(instanceId);
		instance.setType(instanceType);
		InstanceReferenceMock referenceMock = new InstanceReferenceMock(instance);

		when(typeResolver.resolveReference(instanceId)).thenReturn(Optional.of(referenceMock));
	}

	private void mockNonExistingInstance(String instanceId) {
		when(typeResolver.resolveReference(instanceId)).thenReturn(Optional.empty());
	}
}
