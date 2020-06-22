package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
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
	private ResourceService resourceService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private AuthorityService authorityService;
	@Mock
	private RoleRegistry roleRegistry;
	@Mock
	private InstanceService instanceService;

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
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
	}

	@Test
	public void invalidData() throws Exception {
		Serializable identifier = null;
		assertFalse(accessEvaluator.canRead(identifier));
		assertFalse(accessEvaluator.canRead(identifier, null));
		assertFalse(accessEvaluator.canWrite(identifier));
		assertFalse(accessEvaluator.canWrite(identifier, null));
		assertFalse(accessEvaluator.isAtLeastRole(identifier, null));
		assertFalse(accessEvaluator.isAtLeastRole(identifier, null, null));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void canRead_InstanceNotFound() throws Exception {
		accessEvaluator.canRead(INVALID_INSTANCE_ID);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void writeRead_InstanceNotFound() throws Exception {
		accessEvaluator.canWrite(INVALID_INSTANCE_ID);
	}

	@Test
	public void canRead_fromReference_asAdmin() throws Exception {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.TRUE);

		assertTrue(accessEvaluator.canRead(InstanceReferenceMock.createGeneric(VALID_INSTANCE_ID)));

		verify(permissionService, never()).getPermissionModel(any());
	}

	@Test
	public void canRead_NotAllowed() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.NO_PERMISSION);

		assertFalse(accessEvaluator.canRead(VALID_INSTANCE_ID));
	}

	@Test
	public void canRead_minimumRole() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.VIEWER);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID));
	}

	@Test
	public void canRead_minimumRole_versionInstance() throws Exception {
		final String VERSION_ID = "emf:versionId";
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.VIEWER);
		InstanceReferenceMock instanceReference = InstanceReferenceMock
				.createGeneric(new EmfInstance(VERSION_ID + "-v1.1"));
		when(instanceTypeResolver.resolveReference(VERSION_ID)).thenReturn(Optional.of(instanceReference));

		// the version suffix is trimmed when loading instances for permissions checks
		assertTrue(accessEvaluator.canRead(instanceReference));
	}

	@Test
	public void canRead_minimumRole_versionOfDeletedInstance() throws Exception {
		final String VERSION_ID = "emf:versionOfDeletedId";
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.VIEWER);
		when(instanceTypeResolver.resolveReference(VERSION_ID)).thenReturn(Optional.empty());
		when(instanceService.loadDeleted(VERSION_ID))
				.thenReturn(Optional.of(InstanceReferenceMock.createGeneric(new EmfInstance(VERSION_ID)).toInstance()));

		// the version suffix is trimmed when loading instances for permissions checks
		assertTrue(accessEvaluator.canRead(VERSION_ID + "-v1.1"));
	}

	@Test
	public void canRead_libraryModel() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createLibraryPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONSUMER);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID));
	}

	@Test
	public void canRead_specialModel() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createLibraryPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONSUMER);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID));
	}

	@Test
	public void canRead_customUser() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.ADMINISTRATOR);

		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID, SOME_USER));
		assertTrue(accessEvaluator.canRead(VALID_INSTANCE_ID, "unknownUser"));
	}

	@Test
	public void canWrite_NotAllowed() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONSUMER);

		assertFalse(accessEvaluator.canWrite(VALID_INSTANCE_ID));
	}

	@Test
	public void canWrite_minimumRole() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.CONTRIBUTOR);

		assertTrue(accessEvaluator.canWrite(VALID_INSTANCE_ID));
	}

	@Test
	public void canWrite_customUser() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.ADMINISTRATOR);

		assertTrue(accessEvaluator.canWrite(VALID_INSTANCE_ID, SOME_USER));
		assertTrue(accessEvaluator.canWrite(VALID_INSTANCE_ID, "unknownUser"));
	}

	@Test
	public void isAtLeastRole_NotAllowed() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.NO_PERMISSION);

		assertFalse(accessEvaluator.isAtLeastRole(VALID_INSTANCE_ID, null));
	}

	@Test
	public void isAtLeastRole_minimumRole() throws Exception {
		when(permissionService.getPermissionModel(any())).thenReturn(createSpecialPermissionModel());
		mockRole(SecurityModel.BaseRoles.VIEWER);

		assertTrue(accessEvaluator.isAtLeastRole(VALID_INSTANCE_ID, SecurityModel.BaseRoles.VIEWER));
	}

	@Test
	public void isAtleastRole_should_ReturnFalse_When_RoleIsNull() {
		assertFalse(accessEvaluator.isAtLeastRole(VALID_INSTANCE_ID, SecurityModel.BaseRoles.CONSUMER));
	}

	private void mockRole(RoleIdentifier toReturn) {
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), anyString())).then(a -> {
			ResourceRole resourceRole = new ResourceRole();
			resourceRole.setRole(toReturn);
			resourceRole.setTargetReference(a.getArgumentAt(0, InstanceReference.class).getId());
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
	 * Tests for getAccessPermissions(java.util.Collection)
	 */

	@Test
	public void getAccessPermissions_nullCollection_emptyMapExpected() {
		Collection<Instance> references = null;
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
				.getAccessPermissions(Arrays.asList(InstanceReferenceMock.createGeneric("instance-id-1").toInstance(),
						InstanceReferenceMock.createGeneric("instance-id-2").toInstance(),
						InstanceReferenceMock.createGeneric("instance-id-3").toInstance()), "admin-id");

		InstanceAccessPermissions firstInstancePermissions = permissions.get("instance-id-1");
		assertEquals(InstanceAccessPermissions.CAN_WRITE, firstInstancePermissions);

		InstanceAccessPermissions secondInstancePermissions = permissions.get("instance-id-2");
		assertEquals(InstanceAccessPermissions.CAN_WRITE, secondInstancePermissions);

		InstanceAccessPermissions thirdInstancePermissions = permissions.get("instance-id-3");
		assertEquals(InstanceAccessPermissions.CAN_WRITE, thirdInstancePermissions);
	}

	@Test
	public void getAccessPermissions_noInstances_emptyMapExpected() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		Map<Serializable, InstanceAccessPermissions> permissions = accessEvaluator
				.getAccessPermissions(Collections.emptyList(), "user-id");
		assertTrue(permissions.isEmpty());
	}

	@Test
	public void getAccessPermissions_noPermissionsResolved_noAccessExpected() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		when(authorityService.getAllowedActionNames(any(Instance.class), anyString()))
				.thenReturn(Collections.emptySet());
		Map<Serializable, InstanceAccessPermissions> permissions = accessEvaluator
				.getAccessPermissions(Arrays.asList(new EmfInstance("id-v1.5"), new EmfInstance("id")), "user-id");

		permissions.entrySet().forEach(entry -> assertEquals(InstanceAccessPermissions.NO_ACCESS, entry.getValue()));
	}

	@Test
	public void getAccessPermissions_permissionsCalculated() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);

		Instance noPermissionsInstance = InstanceReferenceMock.createGeneric("instance-id-1").toInstance();
		Instance fullAccessInstance = InstanceReferenceMock.createGeneric("instance-id-2").toInstance();

		when(authorityService.getAllowedActionNames(noPermissionsInstance, null)).thenReturn(Collections.emptySet());
		when(authorityService.getAllowedActionNames(fullAccessInstance, null)).thenReturn(
				new HashSet<>(Arrays.asList(ActionTypeConstants.APPROVE, ActionTypeConstants.EDIT_DETAILS)));

		Map<Serializable, InstanceAccessPermissions> results = accessEvaluator
				.getAccessPermissions(Arrays.asList(noPermissionsInstance, fullAccessInstance), SOME_USER);
		assertFalse(results.isEmpty());

		InstanceAccessPermissions noPermissions = results.get("instance-id-1");
		assertEquals(InstanceAccessPermissions.NO_ACCESS, noPermissions);

		InstanceAccessPermissions fullAccessPermissions = results.get("instance-id-2");
		assertEquals(InstanceAccessPermissions.CAN_WRITE, fullAccessPermissions);
	}

	@Test
	public void getAccessPermission_shouldReturnNoAccess_onNullIdentifier() {
		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission(null);
		assertNotNull(accessPermission);
		assertEquals(InstanceAccessPermissions.NO_ACCESS, accessPermission);
	}

	@Test
	public void getAccessPermission_shouldReturnNoAccess_onMissingActions() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);

		Instance instance = InstanceReferenceMock.createGeneric("instance-id-1").toInstance();
		when(authorityService.getAllowedActionNames(instance, null)).thenReturn(Collections.emptySet());

		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission(instance);
		assertNotNull(accessPermission);
		assertEquals(InstanceAccessPermissions.NO_ACCESS, accessPermission);
	}

	@Test
	public void getAccessPermission_shouldReturnCorrectAccess() throws Exception {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);

		Instance readAccess = InstanceReferenceMock.createGeneric("instance-id-1").toInstance();
		when(authorityService.getAllowedActionNames(readAccess, null))
				.thenReturn(new HashSet<>(Arrays.asList(ActionTypeConstants.PRINT, ActionTypeConstants.READ)));

		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission(readAccess);
		assertNotNull(accessPermission);
		assertEquals(InstanceAccessPermissions.CAN_READ, accessPermission);
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

	@Test
	public void isAtLeastRole_emptyIdentifiersCollection_resultEmptyMap() {
		Map<Serializable, InstanceAccessPermissions> result = accessEvaluator.isAtLeastRole(Collections.emptyList(),
				null, BaseRoles.VIEWER, BaseRoles.MANAGER);
		assertTrue(result.isEmpty());
	}

	@Test
	public void isAtLeastRole_nullIdentifiersCollection_resultEmptyMap() {
		Map<Serializable, InstanceAccessPermissions> result = accessEvaluator.isAtLeastRole(null, null,
				BaseRoles.VIEWER, BaseRoles.MANAGER);
		assertTrue(result.isEmpty());
	}

	@Test
	public void isAtLeastRole_versionIdentifiers_adminUser_resultMapWithNormalIds() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.TRUE);
		Map<Serializable, InstanceAccessPermissions> result = accessEvaluator.isAtLeastRole(
				Arrays.asList(new EmfInstance("instance-id-v1.0"),
						InstanceReferenceMock.createGeneric("instance-reference-id-v2.5"), "id-v1.2"),
				null, BaseRoles.VIEWER, BaseRoles.MANAGER);
		assertFalse(result.keySet().stream().anyMatch(InstanceVersionService::isVersion));
	}

	@Test
	public void isAtLeastRole_duplicatedMapKeysCheck_versionIdentifiers_adminUser_resultMapWithNormalIds() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.TRUE);
		Map<Serializable, InstanceAccessPermissions> result = accessEvaluator.isAtLeastRole(
				Arrays.asList(new EmfInstance("instance-id-v1.0"),
						InstanceReferenceMock.createGeneric("instance-id-v2.5"), "instance-id-v1.2"),
				null, BaseRoles.VIEWER, BaseRoles.MANAGER);
		assertFalse(result.keySet().stream().anyMatch(InstanceVersionService::isVersion));
	}

	@Test
	public void isAtLeastRole_instancesCantBeResolved_resultEmptyMap() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		when(instanceTypeResolver.resolveReferences(anyCollectionOf(Serializable.class))).thenReturn(emptyList());
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.empty());
		Map<Serializable, InstanceAccessPermissions> result = accessEvaluator.isAtLeastRole(
				Arrays.asList(new EmfInstance("instance-id"), new EmfInstance("version-instance-id-v1.5"),
						InstanceReferenceMock.createGeneric("instance-reference-id"),
						InstanceReferenceMock.createGeneric("version-instance-reference-id-v2.5"), "id", "id-v1.2"),
				null, BaseRoles.VIEWER, BaseRoles.MANAGER);
		assertTrue(result.isEmpty());
	}

	@Test
	public void isAtLeastRole_nonAdminUser_noPermissions_resultEmptyMap() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		when(permissionService.getPermissionAssignment(anyCollectionOf(InstanceReference.class), anyString()))
				.thenReturn(emptyMap());
		Map<Serializable, InstanceAccessPermissions> result = accessEvaluator.isAtLeastRole(
				Collections.singleton(InstanceReferenceMock.createGeneric("instance-reference")), null,
				BaseRoles.VIEWER, BaseRoles.MANAGER);
		assertTrue(result.isEmpty());
	}

	@Test
	public void isAtLeastRole_nonAdminUser_withDifferentPermissionForDifferentInstances() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		InstanceReference canWrite = InstanceReferenceMock.createGeneric("1");
		InstanceReference canRead = InstanceReferenceMock.createGeneric("2");
		InstanceReference noPermissions = InstanceReferenceMock.createGeneric("3");

		Map<InstanceReference, ResourceRole> permissions = new HashMap<>(3);
		permissions.put(canWrite, buildRole(BaseRoles.MANAGER));
		permissions.put(canRead, buildRole(BaseRoles.VIEWER));
		permissions.put(noPermissions, buildRole(BaseRoles.NO_PERMISSION));

		when(permissionService.getPermissionAssignment(anyCollection(), anyString())).thenReturn(permissions);
		Map<Serializable, InstanceAccessPermissions> result = accessEvaluator.isAtLeastRole(
				Arrays.asList(canWrite, canRead, noPermissions), null, BaseRoles.VIEWER, BaseRoles.MANAGER);

		assertEquals(InstanceAccessPermissions.CAN_WRITE, result.get("1"));
		assertEquals(InstanceAccessPermissions.CAN_READ, result.get("2"));
		assertEquals(InstanceAccessPermissions.NO_ACCESS, result.get("3"));
	}

	private static ResourceRole buildRole(RoleIdentifier roleIdentifier) {
		ResourceRole role = new ResourceRole();
		role.setRole(roleIdentifier);
		return role;
	}

	@Test
	public void actionAllowed_withInvalidData() {
		assertFalse(accessEvaluator.actionAllowed(null, SOME_USER, "action"));
		assertFalse(accessEvaluator.actionAllowed(VALID_INSTANCE_ID, SOME_USER, null));
		assertFalse(accessEvaluator.actionAllowed(VALID_INSTANCE_ID, SOME_USER, ""));
	}

	@Test
	public void actionAllowed_withAdminUser() {
		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.TRUE);
		assertTrue(accessEvaluator.actionAllowed(VALID_INSTANCE_ID, SOME_USER, "action"));
	}

	@Test
	public void actionAllowed_withNullResourceRole() {
		assertFalse(accessEvaluator.actionAllowed(VALID_INSTANCE_ID, SOME_USER, "action"));
	}

	@Test
	public void actionAllowed_shouldReturnTrue_whenThereIsActionForRole() {
		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("action"));
		Role role = new Role(new RoleId(), actions);

		mockRole(SecurityModel.BaseRoles.CONTRIBUTOR);
		when(roleRegistry.find(any(RoleIdentifier.class))).thenReturn(role);

		assertTrue(accessEvaluator.actionAllowed(VALID_INSTANCE_ID, SOME_USER, "action"));
	}

	@Test
	public void actionAllowed_shouldReturnFalse_whenThereIsNoActionForRole() {
		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("action"));
		Role role = new Role(new RoleId(), actions);

		mockRole(SecurityModel.BaseRoles.CONTRIBUTOR);
		when(roleRegistry.find(any(RoleIdentifier.class))).thenReturn(role);

		assertFalse(accessEvaluator.actionAllowed(VALID_INSTANCE_ID, SOME_USER, "notExistingAction"));
	}
}