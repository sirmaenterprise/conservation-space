package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link InstanceAccessEvaluatorImpl}
 *
 * @author BBonev
 */
public class InstanceAccessEvaluatorImplTest {

	private static final String SOME_USER = "emf:someUser";
	private static final String VALID_INSTANCE_ID = "emf:instanceId";
	private static final String INVALID_INSTANCE_ID = "emf:invalidInstanceId";

	@InjectMocks
	private InstanceAccessEvaluatorImpl accessEvaluator;

	@Mock
	private PermissionService permissionService;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private InstanceContextInitializer contextInitializer;
	@Mock
	private ResourceService resourceService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private AuthorityService authorityService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		EmfUser currentUser = new EmfUser("user");
		currentUser.setId("emf:user");

		when(securityContext.getEffectiveAuthentication()).thenReturn(currentUser);

		when(resourceService.findResource(SOME_USER)).then(a -> {
			EmfUser user = new EmfUser();
			user.setId(a.getArgumentAt(0, String.class));
			return user;
		});

		when(instanceTypeResolver.resolveReference(VALID_INSTANCE_ID))
				.then(a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));
		when(instanceTypeResolver.resolveReference(INVALID_INSTANCE_ID)).then(a -> Optional.empty());
	}

	@Test
	public void testInvalidData() throws Exception {
		Serializable identifier = null;
		assertFalse(accessEvaluator.canRead(identifier));
		assertFalse(accessEvaluator.canRead(identifier, null));
		assertFalse(accessEvaluator.canWrite(identifier));
		assertFalse(accessEvaluator.canWrite(identifier, null));
		assertFalse(accessEvaluator.isAtLeastRole(identifier, null));
		assertFalse(accessEvaluator.isAtLeastRole(identifier, null, null));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testCanRead_InstanceNotFound() throws Exception {
		accessEvaluator.canRead(INVALID_INSTANCE_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testWriteRead_InstanceNotFound() throws Exception {
		accessEvaluator.canWrite(INVALID_INSTANCE_ID);
	}

	@Test
	public void testCanRead_fromReference_asAdmin() throws Exception {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.TRUE);

		assertTrue(accessEvaluator.canRead(InstanceReferenceMock.createGeneric(VALID_INSTANCE_ID)));

		verify(permissionService, never()).getPermissionModel(any());
	}

	@Test
	public void testCanRead_NotAllowed() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.NO_PERMISSION);

		assertFalse(accessEvaluator.canRead(VALID_INSTANCE_ID));
	}

	@Test
	public void testCanRead_minimumRole() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.VIEWER);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID));
	}

	@Test
	public void testCanWrite_NotAllowed() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONSUMER);

		assertFalse(accessEvaluator.canWrite(VALID_INSTANCE_ID));
	}

	@Test
	public void testCanWrite_minimumRole() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONTRIBUTOR);

		assertTrue(accessEvaluator.canWrite(VALID_INSTANCE_ID));
	}

	@Test
	public void testCanRead_libraryModel() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createLibraryPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONSUMER);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID));
		verify(contextInitializer, never()).restoreHierarchy(any(InstanceReference.class));
	}

	@Test
	public void testCanRead_specialModel() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createLibraryPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONSUMER);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID));
		verify(contextInitializer, never()).restoreHierarchy(any(InstanceReference.class));
	}

	@Test
	public void testCanRead_customUser() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.ADMINISTRATOR);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID, SOME_USER));
		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID, "unknownUser"));
	}

	@Test
	public void testCanWrite_customUser() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.ADMINISTRATOR);

		assertTrue(accessEvaluator.canWrite(VALID_INSTANCE_ID, SOME_USER));
		assertTrue(accessEvaluator.canWrite(VALID_INSTANCE_ID, "unknownUser"));
	}

	@Test
	public void testIsAtLeastRole_NotAllowed() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.NO_PERMISSION);

		assertFalse(accessEvaluator.isAtLeastRole(VALID_INSTANCE_ID, null));
	}

	@Test
	public void testisAtLeastRole_minimumRole() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.VIEWER);

		assertTrue(accessEvaluator.isAtLeastRole(VALID_INSTANCE_ID, SecurityModel.BaseRoles.VIEWER));
	}

	private void mockRole(RoleIdentifier toReturn) {
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), anyString())).then(a -> {
			ResourceRole resourceRole = new ResourceRole();
			resourceRole.setRole(toReturn);
			resourceRole.setTargetReference(a.getArgumentAt(0, InstanceReference.class).getIdentifier());
			resourceRole.setAuthorityId(a.getArgumentAt(1, String.class));
			return resourceRole;
		});
	}

	private static PermissionModelType createSpecialPermissionModel() {
		return new PermissionModelType(false, false, true);
	}

	private static PermissionModelType createLibraryPermissionModel() {
		return new PermissionModelType(false, true, false);
	}

	/**
	 * Tests for getAccessPermissions(java.util.Collection) <br>
	 * also tests isAtLeastRole(java.util.Collection, Serializable, RoleIdentifier, RoleIdentifier)
	 */

	@Test
	public void getAccessPermissions_nullCollection_emptyMapExpected() {
		Collection<InstanceReference> references = null;
		assertTrue(accessEvaluator.getAccessPermissions(references, "authotity-id").isEmpty());
	}

	@Test
	public void getAccessPermissions_emptyCollection_emptyMapExpected() {
		assertTrue(accessEvaluator.getAccessPermissions(emptyList(), "authotity-id").isEmpty());
	}

	@Test
	public void getAccessPermissions_adminUser_fullAccessExpected() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.TRUE);
		Map<Serializable, InstanceAccessPermissions> permissions = accessEvaluator
				.getAccessPermissions(Arrays.asList("instance-id-1", "instance-id-2"), "admin-id");

		InstanceAccessPermissions firstInstancePermissions = permissions.get("instance-id-1");
		assertEquals(InstanceAccessPermissions.CAN_WRITE, firstInstancePermissions);

		InstanceAccessPermissions secondInstancePermissions = permissions.get("instance-id-2");
		assertEquals(InstanceAccessPermissions.CAN_WRITE, secondInstancePermissions);
	}

	@Test
	public void getAccessPermissions_noInstancesResolved_emptyMapExpected() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		Map<Serializable, InstanceAccessPermissions> permissions = accessEvaluator
				.getAccessPermissions(Arrays.asList("instance-id-1", "instance-id-2"), "user-id");
		assertTrue(permissions.isEmpty());
	}

	@Test
	public void getAccessPermissions_noPermissionsResolved_emptyMapExpected() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		when(instanceTypeResolver.resolveReferences(anySetOf(Serializable.class)))
				.thenReturn(Arrays.asList(InstanceReferenceMock.createGeneric("instance-id-1"),
						InstanceReferenceMock.createGeneric("instance-id-2")));
		when(permissionService.getPermissionAssignment(anyCollectionOf(InstanceReference.class), anyString()))
				.thenReturn(emptyMap());
		Map<Serializable, InstanceAccessPermissions> permissions = accessEvaluator
				.getAccessPermissions(Arrays.asList("instance-id-1", "instance-id-2"), "user-id");
		assertTrue(permissions.isEmpty());
	}

	@Test
	public void getAccessPermissions_permissionsCalculated() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);

		InstanceReferenceMock noPermissionsReference = InstanceReferenceMock.createGeneric("instance-id-1");
		InstanceReferenceMock fullAccessReference = InstanceReferenceMock.createGeneric("instance-id-2");

		Map<InstanceReference, ResourceRole> permissions = new HashMap<>(2);
		ResourceRole noPermissionRole = prepareResourceRole(noPermissionsReference, BaseRoles.NO_PERMISSION);
		ResourceRole fullAccessRole = prepareResourceRole(fullAccessReference, BaseRoles.ADMINISTRATOR);
		permissions.put(noPermissionsReference, noPermissionRole);
		permissions.put(fullAccessReference, fullAccessRole);

		when(permissionService.getPermissionAssignment(anyCollectionOf(InstanceReference.class), anyString()))
				.thenReturn(permissions);

		when(instanceTypeResolver.resolveReferences(anySetOf(Serializable.class)))
				.thenReturn(Arrays.asList(noPermissionsReference, fullAccessReference));

		Map<Serializable, InstanceAccessPermissions> results = accessEvaluator
				.getAccessPermissions(Arrays.asList("instance-id-1", "instance-id-2"), SOME_USER);
		assertFalse(results.isEmpty());

		InstanceAccessPermissions noPermissions = results.get("instance-id-1");
		assertEquals(InstanceAccessPermissions.NO_ACCESS, noPermissions);

		InstanceAccessPermissions fullAccessPermissions = results.get("instance-id-2");
		assertEquals(InstanceAccessPermissions.CAN_WRITE, fullAccessPermissions);
	}

	@Test
	public void getAccessPermission_shouldReturnNoAccess_onNullIdentifier() throws Exception {
		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission(null);
		assertNotNull(accessPermission);
		assertEquals(InstanceAccessPermissions.NO_ACCESS, accessPermission);
	}

	@Test
	public void getAccessPermission_shouldReturnNoAccess_onMissingPermissions() throws Exception {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);

		InstanceReferenceMock refernce = InstanceReferenceMock.createGeneric("instance-id-1");

		when(permissionService.getPermissionAssignment(anyCollectionOf(InstanceReference.class), anyString()))
				.thenReturn(Collections.emptyMap());

		when(instanceTypeResolver.resolveReferences(anySetOf(Serializable.class))).thenReturn(Arrays.asList(refernce));

		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission("instance-id-1");
		assertNotNull(accessPermission);
		assertEquals(InstanceAccessPermissions.NO_ACCESS, accessPermission);
	}

	@Test
	public void getAccessPermission_shouldReturnCorrectAccess() throws Exception {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);

		InstanceReferenceMock readAccess = InstanceReferenceMock.createGeneric("instance-id-1");

		Map<InstanceReference, ResourceRole> permissions = new HashMap<>(2);
		permissions.put(readAccess, prepareResourceRole(readAccess, BaseRoles.CONSUMER));

		when(permissionService.getPermissionAssignment(anyCollectionOf(InstanceReference.class), anyString()))
				.thenReturn(permissions);

		when(instanceTypeResolver.resolveReferences(anySetOf(Serializable.class)))
				.thenReturn(Arrays.asList(readAccess));

		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission("instance-id-1");
		assertNotNull(accessPermission);
		assertEquals(InstanceAccessPermissions.CAN_READ, accessPermission);
	}

	private static ResourceRole prepareResourceRole(InstanceReference reference, RoleIdentifier toReturn) {
		ResourceRole resourceRole = new ResourceRole();
		resourceRole.setRole(toReturn);
		resourceRole.setTargetReference(reference.getIdentifier());
		resourceRole.setAuthorityId(SOME_USER);
		return resourceRole;
	}

	@Test(expected = NullPointerException.class)
	public void isAtLeastRole_incorrectRoles_nullReadRole() {
		accessEvaluator.isAtLeastRole(Collections.singleton("instance-id"), null, null, BaseRoles.VIEWER);
	}

	@Test(expected = NullPointerException.class)
	public void isAtLeastRole_incorrectRoles_nullWriteRole() {
		accessEvaluator.isAtLeastRole(Collections.singleton("instance-id"), null, BaseRoles.VIEWER, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void isAtLeastRole_incorrectRoles_readWithMorePermissionsThenWrite() {
		accessEvaluator.isAtLeastRole(Collections.singleton("instance-id"), null, BaseRoles.MANAGER, BaseRoles.VIEWER);
	}

}
