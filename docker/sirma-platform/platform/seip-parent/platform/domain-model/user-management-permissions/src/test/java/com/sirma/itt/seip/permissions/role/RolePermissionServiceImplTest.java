package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionAssignmentChange;
import com.sirma.itt.seip.permissions.PermissionInheritanceChange;
import com.sirma.itt.seip.permissions.PermissionModelChangedEvent;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionsRestored;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link RolePermissionServiceImpl}.
 */
@SuppressWarnings("synthetic-access")
public class RolePermissionServiceImplTest {

	@InjectMocks
	private RolePermissionServiceImpl permissionService;

	@Mock
	private EntityPermissionDao entityPermissionDao;

	@Mock
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Mock
	private EventService eventService;

	@Mock
	private ResourceService resourceService;

	@Mock
	private RoleService roleService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Captor
	private ArgumentCaptor<PermissionModelChangedEvent> eventCaptor;

	@Captor
	private ArgumentCaptor<EntityPermission> entityPermissionCaptor;

	private Map<String, EntityPermission> cachedEntityPermissions;

	private static final String MANAGER = SecurityModel.BaseRoles.MANAGER.getIdentifier();
	private static final String COLLABORATOR = SecurityModel.BaseRoles.COLLABORATOR.getIdentifier();
	private static final String CONSUMER = SecurityModel.BaseRoles.CONSUMER.getIdentifier();

	private static final String ALL_OTHER_USERS = "ALL_OTHER_USERS";

	@Before
	public void init() {
		cachedEntityPermissions = new HashMap<>();

		MockitoAnnotations.initMocks(this);

		when(resourceService.loadByDbId(anyString())).then(invocation -> {
			EmfResource resource = new EmfResource();
			resource.setId(invocation.getArgumentAt(0, String.class));
			return resource;
		});

		when(roleService.getRoleIdentifier(anyString())).then(invocation -> {
			return SecurityModel.BaseRoles.getIdentifier(invocation.getArgumentAt(0, String.class));
		});

		when(roleService.isManagerRole(anyString()))
				.thenAnswer(invocation -> MANAGER.equals(invocation.getArgumentAt(0, String.class)));

		when(roleService.getManagerRole()).thenReturn(SecurityModel.BaseRoles.getIdentifier(MANAGER));

		when(resourceService.getAllOtherUsers()).thenReturn(createUser(ALL_OTHER_USERS));

		when(roleService.getActiveRoles()).thenReturn(SecurityModel.BaseRoles.PUBLIC);
	}

	// --- getPermissionAssignments()

	/**
	 * For instance with special permissions, without parent and library inheritance, without library managers. Should
	 * provide only the special permissions.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvideSpecialPermissions() {
		InstanceReference library = InstanceReferenceMock.createGeneric("l1");
		withInstance(library).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");
		withInstance(instance)
				.havingLibrary(library)
					.havingSpecialPermissions(new String[][] { { "Admin", MANAGER } })
					.build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER } });
	}

	/**
	 * For instance with special permissions for ALL_OTHER_USERS shouldn't be overridden.
	 */
	@Test
	public void getPermissionAssignmentsShouldNotOverrideAllOtherUsersPermissions() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance)
				.havingLibrary(library)
					.havingSpecialPermissions(new String[][] { { ALL_OTHER_USERS, MANAGER } })
					.build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { ALL_OTHER_USERS, MANAGER } });
	}

	/**
	 * For instance without special permissions, without parent and library inheritance, with library managers. Should
	 * provide the special permissions and library managers.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvideSpecialPermissionsAndLibraryManagers() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library).havingSpecialPermissions(new String[][] { { "M1", MANAGER } }).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance)
				.havingLibrary(library)
					.havingSpecialPermissions(new String[][] { { "Admin", MANAGER } })
					.build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER }, { "M1", MANAGER } });
	}

	/**
	 * For instance with special permissions, with parent inheritance, without library inheritance, without library
	 * managers.<br/>
	 * Should provide the special permissions and the parent permissions
	 */
	@Test
	public void getPermissionAssignmentsShouldProvideParentPermissions() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");

		InstanceReference parent = InstanceReferenceMock.createGeneric("parent");
		withInstance(parent).havingSpecialPermissions(new String[][] { { "C1", COLLABORATOR } }).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance)
				.havingSpecialPermissions(new String[][] { { "Admin", MANAGER } })
					.havingLibrary(library)
					.havingParent(parent)
					.inheritingFromParent()
					.build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER }, { "C1", COLLABORATOR } });
	}

	/**
	 * For instance with special permissions, with parent inheritance, without library inheritance, with library
	 * managers.<br/>
	 * Should provide the special permissions, parent permissions and library permissions.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvideParentAndLibraryPermissions() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library)
				.havingSpecialPermissions(new String[][] { { "M1", MANAGER }, { "C1", COLLABORATOR } })
					.build();

		InstanceReference parent = InstanceReferenceMock.createGeneric("parent");
		withInstance(parent).havingSpecialPermissions(new String[][] { { "C2", COLLABORATOR } }).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");

		withInstance(instance)
				.havingSpecialPermissions(new String[][] { { "Admin", MANAGER } })
					.havingParent(parent)
					.inheritingFromParent()
					.havingLibrary(library)
					.inheritingFromLibrary()
					.build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER }, { "C1", COLLABORATOR },
				{ "C2", COLLABORATOR }, { "M1", MANAGER } });
	}

	/**
	 * For imported instance the permission service does not know about the instances so no permissions will be present.
	 * The permissions are resolved based on the library configuration for inheritance of the search instance<br/>
	 * Should parent permissions and library permissions provided.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvidePermissionsBasedOnLibraryInheritanceWhenNoInheriatanceConfigured() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library)
				.havingSpecialPermissions(new String[][] { { "M1", MANAGER }, { "C1", COLLABORATOR } })
					.build();

		ClassInstance type = new ClassInstance();
		type.add(INHERIT_LIBRARY_PERMISSIONS, Boolean.TRUE);
		type.add(INHERIT_PARENT_PERMISSIONS, Boolean.TRUE);
		library.setType(type);

		InstanceReference parent = InstanceReferenceMock.createGeneric("parent");
		withInstance(parent).build();

		InstanceReference reference = InstanceReferenceMock.createGeneric("1");

		when(hierarchyResolver.getLibrary(reference)).thenReturn(library);
		when(instanceTypeResolver.resolveReferences(anyCollection())).thenReturn(Collections.singleton(reference));

		// will force no entries in the database
		when(entityPermissionDao.fetchHierarchyWithAssignmentsForInstances(anyCollection())).thenReturn(null);

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(reference);

		verifyAssignments(permissionAssignments, new Object[][] { { "C1", COLLABORATOR }, { "M1", MANAGER } });
	}

	/**
	 * For instance with special permissions, with parent inheritance where the parent inherits from the same library as
	 * the instance, with library inheritance, with library managers.<br/>
	 * Should provide the special permissions, parent permissions and library permissions.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvideParentInheritingFromLibraryAndLibraryPermissions() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library)
				.havingSpecialPermissions(new String[][] { { "M1", MANAGER }, { "C1", COLLABORATOR } })
					.build();

		InstanceReference parent = InstanceReferenceMock.createGeneric("parent");
		withInstance(parent)
				.havingSpecialPermissions(new String[][] { { "C2", COLLABORATOR } })
					.havingLibrary(library)
					.inheritingFromLibrary()
					.build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");

		withInstance(instance)
				.havingSpecialPermissions(new String[][] { { "Admin", MANAGER } })
					.havingParent(parent)
					.inheritingFromParent()
					.havingLibrary(library)
					.inheritingFromLibrary()
					.build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER }, { "C1", COLLABORATOR },
				{ "C2", COLLABORATOR }, { "M1", MANAGER } });
	}

	/**
	 * For instance with special permissions, with parent inheritance where the parent inherits from the same library as
	 * the instance, with library inheritance, with library managers.<br/>
	 * Should provide the special permissions parent permissions and library permissions.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvideParentInheritingFromLibraryAndOnlyLibraryManagers() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library)
				.havingSpecialPermissions(new String[][] { { "M1", MANAGER }, { "C1", COLLABORATOR } })
					.build();

		InstanceReference parent = InstanceReferenceMock.createGeneric("parent");
		withInstance(parent)
				.havingSpecialPermissions(new String[][] { { "C2", COLLABORATOR } })
					.havingLibrary(library)
					.inheritingFromLibrary()
					.build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");

		withInstance(instance)
				.havingSpecialPermissions(new String[][] { { "Admin", MANAGER } })
					.havingParent(parent)
					.inheritingFromParent()
					.havingLibrary(library)
					.build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER }, { "C1", COLLABORATOR },
				{ "C2", COLLABORATOR }, { "M1", MANAGER } });
	}

	/**
	 * For instance without special permissions, with parent inheritance where the parent with MANAGER permissions
	 * inherits from another parent with COSUMER permissions. <br/>
	 * Should provide permissions of the direct parent - MANAGER.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvidePermissionsFromParentInheritingFromAnotherParent() {
		InstanceReference top = InstanceReferenceMock.createGeneric("top");
		withInstance(top).havingSpecialPermissions(new String[][] { { "Admin", COLLABORATOR } }).build();

		InstanceReference library = InstanceReferenceMock.createGeneric("library");
		withInstance(library).havingSpecialPermissions(new String[][] { { "Admin", MANAGER } }).build();

		InstanceReference parent = InstanceReferenceMock.createGeneric("parent");
		withInstance(parent)
				.havingParent(parent)
					.inheritingFromParent()
					.havingLibrary(library)
					.inheritingFromLibrary()
					.build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance).havingParent(parent).inheritingFromParent().build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER } });
	}

	/**
	 * Verifies that manager permissions are inherited even if the parent inheritance is disabled.
	 */
	@Test
	public void getPermissionAssignmentsShouldProvideParentManagersEventIfParentInheritanceIsDisabled() {
		InstanceReference top = InstanceReferenceMock.createGeneric("top");
		withInstance(top)
				.havingSpecialPermissions(new String[][] { { "Admin", MANAGER }, { "user1", COLLABORATOR } })
					.build();

		InstanceReference O1 = InstanceReferenceMock.createGeneric("1");
		withInstance(O1).havingParent(top).build();

		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(O1);

		verifyAssignments(permissionAssignments, new Object[][] { { "Admin", MANAGER } });
	}

	/**
	 * The scenario: (M prefix is used for managers, U prefix is used for regular roles) for brevity<br/>
	 * Preconditions: <br/>
	 * Library L1 with assignments - M1, U1, U2 <br/>
	 * Library L2 with assignments - M2, M3, U3 <br/>
	 * Library L3 with assignments - M4, U4, U5 <br/>
	 * Library L4 with assignments - M5, M6, U6, U7 <br/>
	 * <br/>
	 * Object O1 in library L2, inheriting permissions from the library <br />
	 * Object O2 in library L1, inheriting permissions from parent O1 <br />
	 * Object O3 in library L3, inheriting permissions from parent O2 and from the library <br />
	 * Object O4 in library L4, with special permissions U3 (object creator), no permission inheritance <br/>
	 * <br/>
	 * Expectations: <br/>
	 * Object O1 should have - M2, M3, U3<br/>
	 * Object O2 should have - M1, M2, M3, U3<br />
	 * Object O3 should have - M1, M2, M3, M4, U3, U4, U5<br />
	 * Object O4 should have - M5, M6, U3<br />
	 */
	@Test
	public void testGetPermissionAssignmentsUsingBroadScenario() {
		InstanceReference L1 = InstanceReferenceMock.createGeneric("L1");
		withInstance(L1)
				.havingSpecialPermissions(
						new String[][] { { "M1", MANAGER }, { "U1", COLLABORATOR }, { "U2", COLLABORATOR } })
					.build();

		InstanceReference L2 = InstanceReferenceMock.createGeneric("L2");
		withInstance(L2)
				.havingSpecialPermissions(
						new String[][] { { "M2", MANAGER }, { "M3", MANAGER }, { "U3", COLLABORATOR } })
					.build();

		InstanceReference L3 = InstanceReferenceMock.createGeneric("L3");
		withInstance(L3)
				.havingSpecialPermissions(
						new String[][] { { "M4", MANAGER }, { "U4", COLLABORATOR }, { "U5", COLLABORATOR } })
					.build();

		InstanceReference L4 = InstanceReferenceMock.createGeneric("L4");
		withInstance(L4)
				.havingSpecialPermissions(new String[][] { { "M5", MANAGER }, { "M6", MANAGER }, { "U6", COLLABORATOR },
						{ "U7", COLLABORATOR } })
					.build();

		InstanceReference O1 = InstanceReferenceMock.createGeneric("O1");
		withInstance(O1).havingLibrary(L2).inheritingFromLibrary().build();

		Map<String, ResourceRole> O1permissions = permissionService.getPermissionAssignments(O1);

		verifyAssignments(O1permissions,
				new Object[][] { { "M2", MANAGER }, { "M3", MANAGER }, { "U3", COLLABORATOR } });

		InstanceReference O2 = InstanceReferenceMock.createGeneric("O2");
		withInstance(O2).havingParent(O1).inheritingFromParent().havingLibrary(L1).build();

		Map<String, ResourceRole> O2permissions = permissionService.getPermissionAssignments(O2);
		verifyAssignments(O2permissions,
				new Object[][] { { "M1", MANAGER }, { "M2", MANAGER }, { "M3", MANAGER }, { "U3", COLLABORATOR } });

		InstanceReference O3 = InstanceReferenceMock.createGeneric("O3");
		withInstance(O3).havingParent(O2).inheritingFromParent().havingLibrary(L3).inheritingFromLibrary().build();

		Map<String, ResourceRole> O3permissions = permissionService.getPermissionAssignments(O3);
		verifyAssignments(O3permissions, new Object[][] { { "M1", MANAGER }, { "M2", MANAGER }, { "M3", MANAGER },
				{ "M4", MANAGER }, { "U3", COLLABORATOR }, { "U4", COLLABORATOR }, { "U5", COLLABORATOR } });

		InstanceReference O4 = InstanceReferenceMock.createGeneric("O4");
		withInstance(O4).havingLibrary(L4).havingSpecialPermissions(new String[][] { { "U3", MANAGER } }).build();

		Map<String, ResourceRole> O4permissions = permissionService.getPermissionAssignments(O4);
		verifyAssignments(O4permissions, new Object[][] { { "M5", MANAGER }, { "M6", MANAGER }, { "U3", MANAGER } });
	}

	@Test
	public void getPermissionAssignmentsShouldProperlyCalculateActivePermissions() {
		InstanceReference library = InstanceReferenceMock.createGeneric("L1");
		withInstance(library).havingSpecialPermissions(new String[][] { { "John", "CONSUMER" } }).build();

		InstanceReference parent = InstanceReferenceMock.createGeneric("P1");
		withInstance(parent)
				.havingLibrary(library)
					.havingSpecialPermissions(new String[][] { { "John", "VISITOR" } })
					.build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("O1");
		withInstance(instance)
				.havingSpecialPermissions(new String[][] { { "John", COLLABORATOR } })
					.havingParent(parent)
					.inheritingFromParent()
					.havingLibrary(library)
					.inheritingFromLibrary()
					.build();

		Map<String, ResourceRole> assignments = permissionService.getPermissionAssignments(instance);
		verifyAssignments(assignments, new Object[][] { { "John", COLLABORATOR } });
	}

	/**
	 * The library permissions external filter (includeLibraryPermissions parameter) should override the current
	 * inheritance settings.<br/>
	 * The library permissions should be inherited even if the inheritance is not set.
	 */
	@Test
	public void getPermissionAssignmentsShouldSupportExternalFilterForLibraryPermission() {
		InstanceReference library = InstanceReferenceMock.createGeneric("L1");
		withInstance(library).havingSpecialPermissions(new String[][] { { "John", COLLABORATOR } }).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("O1");
		withInstance(instance).havingLibrary(library).build();

		Map<String, ResourceRole> assignments = permissionService.getPermissionAssignments(instance, null, true);

		verifyAssignments(assignments, new Object[][] { { "John", COLLABORATOR } });
	}

	/**
	 * The parent permissions external filter (includeParentPermissions parameter) should override the current
	 * inheritance settings.<br/>
	 * The parent permissions should be inherited even if the inheritance is not set.
	 */
	@Test
	public void getPermissionAssignmentsShouldSupportExternalFilterForParentPermission() {
		InstanceReference parent = InstanceReferenceMock.createGeneric("P1");
		withInstance(parent).havingSpecialPermissions(new String[][] { { "John", COLLABORATOR } }).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("O1");
		withInstance(instance).havingParent(parent).build();

		Map<String, ResourceRole> assignments = permissionService.getPermissionAssignments(instance, true, null);

		verifyAssignments(assignments, new Object[][] { { "John", COLLABORATOR } });
	}

	private static void verifyAssignments(Map<String, ResourceRole> permissionAssignments,
			Object[][] expectedAssignments) {
		assertEquals(expectedAssignments.length, permissionAssignments.size());

		for (Object[] expectedAssignment : expectedAssignments) {
			String authority = (String) expectedAssignment[0];
			ResourceRole assignment = permissionAssignments.get(authority);
			assertEquals(expectedAssignment[1], assignment.getRole().getIdentifier());
			if (expectedAssignment.length > 2) {
				assertEquals(expectedAssignment[2], permissionAssignments);
			}
		}
	}

	private static InstanceReference createInstance(String id, boolean inheritFromParent, boolean inheritFromLibrary) {
		InstanceReference instance = InstanceReferenceMock.createGeneric(id);
		InstanceType type = Mockito.mock(InstanceType.class);
		when(type.hasTrait(eq(DefaultProperties.INHERIT_PARENT_PERMISSIONS))).thenReturn(inheritFromParent);
		when(type.hasTrait(eq(DefaultProperties.INHERIT_LIBRARY_PERMISSIONS))).thenReturn(inheritFromLibrary);
		instance.setType(type);

		return instance;
	}

	private static AuthorityRoleAssignment createAuthorityRoleAssignment(String authorityId, String role) {
		AuthorityRoleAssignment assignment = new AuthorityRoleAssignment(authorityId, role);
		// for entity uniqueness
		assignment.setId((long) (authorityId + role).hashCode());
		return assignment;
	}

	private InstanceWithPermissionsBuilder withInstance(InstanceReference reference) {
		return new InstanceWithPermissionsBuilder(reference);
	}

	private class InstanceWithPermissionsBuilder {
		private InstanceReference instance;

		private List<AuthorityRoleAssignment> assignments;

		private InstanceReference parent;

		private boolean inheritFromParent;

		private InstanceReference library;

		private boolean inheritFromLibrary;

		private InstanceWithPermissionsBuilder(InstanceReference instance) {
			instance.setReferenceType(new DataTypeDefinitionMock("1"));
			this.instance = instance;
			assignments = new ArrayList<>();
		}

		private InstanceWithPermissionsBuilder havingSpecialPermissions(String[][] roles) {
			for (String[] role : roles) {
				assignments.add(createAuthorityRoleAssignment(role[0], role[1]));
			}
			return this;
		}

		private InstanceWithPermissionsBuilder havingParent(InstanceReference newParent) {
			parent = newParent;
			return this;
		}

		private InstanceWithPermissionsBuilder inheritingFromParent() {
			inheritFromParent = true;
			return this;
		}

		private InstanceWithPermissionsBuilder havingLibrary(InstanceReference newLibrary) {
			library = newLibrary;
			return this;
		}

		private InstanceWithPermissionsBuilder inheritingFromLibrary() {
			inheritFromLibrary = true;
			return this;
		}

		private void build() {
			EntityPermission permission = new EntityPermission();
			permission.setId((long) instance.getId().hashCode());
			permission.setTargetId(instance.getId());

			if (parent != null) {
				EntityPermission cachedParent = cachedEntityPermissions.get(parent.getId());
				permission.setParent(cachedParent);
			}

			if (library != null) {
				EntityPermission cachedLibrary = cachedEntityPermissions.get(library.getId());
				permission.setLibrary(cachedLibrary);
			}

			if (inheritFromParent) {
				permission.setInheritFromParent(Boolean.TRUE);
			}

			if (inheritFromLibrary) {
				permission.setInheritFromLibrary(Boolean.TRUE);
			}

			permission.getAssignments().addAll(assignments);

			cachedEntityPermissions.put(permission.getTargetId(), permission);

			Map<String, EntityPermission> resultMap = new HashMap<>(1);
			resultMap.put(instance.getId(), permission);
			when(entityPermissionDao.fetchHierarchyWithAssignmentsForInstances(anyCollectionOf(Serializable.class)))
					.thenReturn(resultMap);
			when(entityPermissionDao.loadWithAssignments(eq(instance.getId()))).thenReturn(Optional.of(permission));
			when(entityPermissionDao.load(eq(instance.getId()))).thenReturn(Optional.of(permission));

			when(instanceTypeResolver.resolveReference(instance.getId())).thenReturn(Optional.of(instance));
		}

	}

	// --- getPermissionAssignment()

	@Test
	public void getPermissionAssignmentShouldReturnUserPermissions() {
		String authorityId = "admin";

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance).havingSpecialPermissions(new String[][] { { authorityId, COLLABORATOR } }).build();

		when(resourceService.getContainingResources(authorityId)).thenReturn(new ArrayList<>());

		ResourceRole assignment = permissionService.getPermissionAssignment(instance, authorityId);

		assertEquals(assignment.getRole().getIdentifier(), COLLABORATOR);
	}

	@Test
	public void getPermissionAssignmentShouldPreferUserPermissionsInsteadAllOtherUsers() {
		String authorityId = "admin";

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance)
				.havingSpecialPermissions(
						new String[][] { { authorityId, COLLABORATOR, }, { ALL_OTHER_USERS, MANAGER } })
					.build();

		when(resourceService.getContainingResources(authorityId)).thenReturn(new ArrayList<>());

		ResourceRole assignment = permissionService.getPermissionAssignment(instance, authorityId);

		assertEquals(assignment.getRole().getIdentifier(), COLLABORATOR);
	}

	@Test
	public void getPermissionAssignmentShouldReturnGroupPermissionsIfNoUserPermissions() {
		String authorityId = "admin";

		when(resourceService.getContainingResources(authorityId))
				.thenReturn(Arrays.asList(createUser("GROUP_ADMINS"), createUser("GROUP_SLACKERS")));

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance)
				.havingSpecialPermissions(
						new String[][] { { "GROUP_ADMINS", COLLABORATOR, }, { ALL_OTHER_USERS, MANAGER } })
					.build();

		ResourceRole assignment = permissionService.getPermissionAssignment(instance, authorityId);

		assertEquals(assignment.getRole().getIdentifier(), COLLABORATOR);
	}

	@Test
	public void getPermissionAssignmentShouldReturnNullPermissionsIfNoUserPermissionsAndNoGroups() {
		String authorityId = "admin";

		when(resourceService.getContainingResources(authorityId)).thenReturn(new ArrayList<>());

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance).havingSpecialPermissions(new String[][] { { "GROUP_ADMINS", COLLABORATOR, } }).build();

		ResourceRole assignment = permissionService.getPermissionAssignment(instance, authorityId);

		assertNull(assignment);
	}

	@Test
	public void getPermissionAssignmentShouldReturnNullIfNoUserOrGroupPermissions() {
		String authorityId = "admin";

		when(resourceService.getContainingResources(authorityId))
				.thenReturn(Arrays.asList(createUser("GROUP_SLACKERS")));

		InstanceReference instance = InstanceReferenceMock.createGeneric("1");
		withInstance(instance)
				.havingSpecialPermissions(
						new String[][] { { "GROUP_ADMINS", COLLABORATOR, }, { ALL_OTHER_USERS, MANAGER } })
					.build();

		ResourceRole assignment = permissionService.getPermissionAssignment(instance, authorityId);

		assertNull(assignment);
	}

	@Test
	public void getPermissionAssignment_ShouldWorkWithAllOtherUsers() {
		String authority = "user";
		when(resourceService.getContainingResources(authority)).thenReturn(asList(createGroup(ALL_OTHER_USERS)));

		InstanceReference instance = InstanceReferenceMock.createGeneric("instanceId");
		withInstance(instance)
				.havingSpecialPermissions(new String[][] { { ALL_OTHER_USERS, COLLABORATOR } })
				.build();

		ResourceRole role = permissionService.getPermissionAssignment(instance, authority);

		assertEquals(COLLABORATOR, role.getRole().getIdentifier());
	}

	@Test
	public void getPermissionAssignment_ShouldReturnGroupPermissionsInsteadAllOtherUsers() {
		String authority = "user";
		when(resourceService.getContainingResources(authority))
				.thenReturn(asList(createGroup(ALL_OTHER_USERS), createGroup("emf:GROUP_group1")));

		InstanceReference instance = InstanceReferenceMock.createGeneric("instanceId");
		withInstance(instance)
				.havingSpecialPermissions(
						new String[][] { { ALL_OTHER_USERS, CONSUMER }, { "emf:GROUP_group1", COLLABORATOR } })
				.build();

		ResourceRole role = permissionService.getPermissionAssignment(instance, authority);

		assertEquals(COLLABORATOR, role.getRole().getIdentifier());
	}

	@Test
	public void getPermissionAssignment_ShouldPrioritizeGroupPermissions() {
		String authority = "user";
		when(resourceService.getContainingResources(authority))
				.thenReturn(asList(createGroup(ALL_OTHER_USERS), createGroup("emf:GROUP_group1")));

		InstanceReference instance = InstanceReferenceMock.createGeneric("instanceId");
		withInstance(instance)
				.havingSpecialPermissions(
						new String[][] { { ALL_OTHER_USERS, COLLABORATOR }, { "emf:GROUP_group1", CONSUMER } })
				.build();

		ResourceRole role = permissionService.getPermissionAssignment(instance, authority);

		assertEquals(CONSUMER, role.getRole().getIdentifier());
	}

	@Test
	public void getPermissionAssignment_ShouldTakeAllOtherUsersRole_When_UserHasNoOtherAssignment() {
		String authority = "user";
		when(resourceService.getContainingResources(authority)).thenReturn(asList(createGroup(ALL_OTHER_USERS)));

		InstanceReference instance = InstanceReferenceMock.createGeneric("instanceId");
		withInstance(instance).havingSpecialPermissions(
				new String[][] { { ALL_OTHER_USERS, COLLABORATOR }, { "emf:GROUP_group1", CONSUMER } }).build();

		ResourceRole role = permissionService.getPermissionAssignment(instance, authority);

		assertEquals(COLLABORATOR, role.getRole().getIdentifier());
	}

	@Test
	public void getPermissionAssignment_ShouldFindMaxRole_When_MultipleGroupsHaveAssignments() {
		String authority = "user";
		when(resourceService.getContainingResources(authority)).thenReturn(
				asList(createGroup(ALL_OTHER_USERS), createGroup("emf:GROUP_group1"), createGroup("emf:GROUP_group2"),
						createGroup("emf:GROUP_group3")));

		InstanceReference instance = InstanceReferenceMock.createGeneric("instanceId");
		withInstance(instance).havingSpecialPermissions(new String[][] { { ALL_OTHER_USERS, COLLABORATOR },
				{ "emf:GROUP_group1", MANAGER }, { "emf:GROUP_group2", CONSUMER } }).build();

		ResourceRole role = permissionService.getPermissionAssignment(instance, authority);

		assertEquals(MANAGER, role.getRole().getIdentifier());
	}

	private static EmfUser createUser(String id) {
		EmfUser user = new EmfUser();
		user.setId(id);
		return user;
	}

	private static EmfGroup createGroup(String id) {
		EmfGroup group = new EmfGroup();
		group.setId(id);
		return group;
	}

	// --- setPermissions()

	@Test
	public void shouldAddAssignmentAndSetParentAndLibrary() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");
		when(hierarchyResolver.isInstanceRoot(instance.getId())).thenReturn(true);

		InstanceReference library = InstanceReferenceMock.createGeneric("l1");
		withInstance(library).build();

		InstanceReference parent = InstanceReferenceMock.createGeneric("p1");
		withInstance(parent).build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.addRoleAssignmentChange("admin", MANAGER);

		builder.parentChange(parent.getId());
		builder.inheritFromParentChange(true);

		builder.libraryChange(library.getId());
		builder.inheritFromLibraryChange(true);

		when(entityPermissionDao.loadWithAssignments(anyString())).thenReturn(Optional.empty());

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), parent.getId(), true,
				library.getId(), true, new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(3));
		verifyRoleChangeIsPresent(changesSet, "admin", null, MANAGER);
		verifyInheritanceChangeIsPresent(changesSet, null, library.getId(), false);
		verifyInheritanceChangeIsPresent(changesSet, null, parent.getId(), false);
	}

	@Test
	public void addAssignmentChangeShouldOverrideAssignedRole() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
				.inheritingFromLibrary()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER }, { "regular_user", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.addRoleAssignmentChange("regular_user", COLLABORATOR);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), null, false, null, true,
				new String[][] { { "admin", MANAGER }, { "regular_user", COLLABORATOR } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(1));
		verifyRoleChangeIsPresent(changesSet, "regular_user", MANAGER, COLLABORATOR);
	}

	@Test
	public void removeAssignmentChangeShouldRemoveAssignment() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
				.inheritingFromLibrary()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER }, { "regular_user", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.removeRoleAssignmentChange("admin", MANAGER);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), null, false, null, true,
				new String[][] { { "regular_user", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(1));
		verifyRoleChangeIsPresent(changesSet, "admin", MANAGER, null);
	}

	@Test
	public void libraryChangeShouldChangeTheLibrary() {
		InstanceReference oldLibrary = InstanceReferenceMock.createGeneric("l1");
		withInstance(oldLibrary).build();

		InstanceReference newLibrary = InstanceReferenceMock.createGeneric("l2");
		withInstance(newLibrary).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		InstanceReference parent = InstanceReferenceMock.createGeneric("p1");
		withInstance(parent).build();

		withInstance(instance)
				.havingParent(parent)
					.havingLibrary(oldLibrary)
					.inheritingFromLibrary()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();

		builder.libraryChange(newLibrary.getId());
		builder.inheritFromLibraryChange(false);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), parent.getId(), false,
				newLibrary.getId(), false, new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(1));
		verifyInheritanceChangeIsPresent(changesSet, oldLibrary.getId(), newLibrary.getId(), true);
	}

	@Test
	public void libraryChangeShouldRemoveTheLibrary() {
		InstanceReference oldLibrary = InstanceReferenceMock.createGeneric("l1");
		withInstance(oldLibrary).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
				.havingLibrary(oldLibrary)
					.inheritingFromLibrary()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();

		builder.libraryChange(null);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), null, false, null, true,
				new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(1));
		verifyInheritanceChangeIsPresent(changesSet, oldLibrary.getId(), null, false);
	}

	@Test
	public void parentChangeShouldChangeTheParent() {
		InstanceReference oldParent = InstanceReferenceMock.createGeneric("p1");
		withInstance(oldParent).build();

		InstanceReference newParent = InstanceReferenceMock.createGeneric("p2");
		withInstance(newParent).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
				.havingParent(oldParent)
					.inheritingFromParent()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();

		builder.parentChange(newParent.getId());
		builder.inheritFromParentChange(false);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), newParent.getId(), false, null,
				false, new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(1));
		verifyInheritanceChangeIsPresent(changesSet, oldParent.getId(), newParent.getId(), true);
	}

	@Test
	public void parentChangeShouldRemoveTheParent() {
		InstanceReference oldParent = InstanceReferenceMock.createGeneric("p1");
		withInstance(oldParent).build();

		InstanceReference newParent = InstanceReferenceMock.createGeneric("p2");
		withInstance(newParent).build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
				.havingParent(oldParent)
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.parentChange(newParent.getId());

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), newParent.getId(), false, null,
				false, new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(1));
		verifyInheritanceChangeIsPresent(changesSet, oldParent.getId(), newParent.getId(), true);
	}

	@Test
	public void setPermissionsShallProperlySetIsLibraryFlag() {
		when(entityPermissionDao.loadWithAssignments(anyString())).thenReturn(Optional.empty());
		List<PermissionsChange> changes = PermissionsChange.builder().setLibraryIndicatorChange(true).build();

		setPermissionsAndCapture(InstanceReferenceMock.createGeneric("l1"), changes);

		EntityPermission entityPermission = entityPermissionCaptor.getValue();
		assertTrue(entityPermission.isLibrary());
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldEnsureAtLeastOneManager() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library)
			.havingSpecialPermissions(new String[][] {{"everyone", CONSUMER}})
			.build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");
		withInstance(instance)
			.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
				.havingLibrary(library)
				.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.removeRoleAssignmentChange("admin", MANAGER);

		setPermissionsAndCapture(instance, builder.build());
	}

	@Test
	public void shouldEnsureAtLeastOneManagerShouldCheckLibraryPermissions() {
		InstanceReference library = InstanceReferenceMock.createGeneric("books");
		withInstance(library)
			.havingSpecialPermissions(new String[][] {{"user", MANAGER}})
			.build();

		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
			.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
			.havingLibrary(library)
			.build();

		List<PermissionsChange> changes = PermissionsChange.builder()
					.removeRoleAssignmentChange("admin", MANAGER)
					.build();

		setPermissionsAndCapture(instance, changes);
	}

	@Test
	public void shouldEnsureAtLeastOneManagerShouldNotHandleGroups() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		type.setId("emf:Group");
		instance.setType(type);

		withInstance(instance)
			.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
			.build();

		List<PermissionsChange> changes = PermissionsChange.builder()
					.removeRoleAssignmentChange("admin", MANAGER)
					.build();

		setPermissionsAndCapture(instance, changes);
	}

	@Test
	public void shouldEnsureAtLeastOneManagerShouldNotHandleLibraries() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance).havingSpecialPermissions(new String[][] { { "admin", MANAGER } }).build();

		List<PermissionsChange> changes = PermissionsChange
				.builder()
					.removeRoleAssignmentChange("admin", MANAGER)
					.setLibraryIndicatorChange(true)
					.build();

		setPermissionsAndCapture(instance, changes);
	}

	@Test
	public void shouldDoNothingIfExistingPermissionIsAdded() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
				.inheritingFromLibrary()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.addRoleAssignmentChange("admin", MANAGER);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), null, false, null, true,
				new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertTrue(changesSet.isEmpty());
	}

	@Test
	public void shouldDoNothingIfExistingPermissionIsRemoved() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		withInstance(instance)
				.inheritingFromLibrary()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.removeRoleAssignmentChange("admin", COLLABORATOR);
		builder.removeRoleAssignmentChange("regular_user", COLLABORATOR);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), null, false, null, true,
				new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertTrue(changesSet.isEmpty());
	}

	@Test
	public void shouldDoNothingIfNewParentIsTheSameAsOld() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		InstanceReference parent = InstanceReferenceMock.createGeneric("p1");
		withInstance(parent).build();

		withInstance(instance)
				.havingParent(parent)
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.parentChange(parent.getId());

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), parent.getId(), false, null, false,
				new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertTrue(changesSet.isEmpty());
	}

	@Test
	public void shouldDoNothingIfNewLibraryIsTheSameAsOld() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		InstanceReference library = InstanceReferenceMock.createGeneric("l1");
		withInstance(library).build();

		withInstance(instance)
				.havingLibrary(library)
					.inheritingFromLibrary()
					.havingSpecialPermissions(new String[][] { { "admin", MANAGER } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.libraryChange(library.getId());

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), null, false, library.getId(), true,
				new String[][] { { "admin", MANAGER } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertTrue(changesSet.isEmpty());
	}

	@Test
	public void shouldNotSetNoPermissionsForAllOtherUsersIfPresent() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");

		InstanceReference library = InstanceReferenceMock.createGeneric("l1");
		withInstance(library).build();

		withInstance(instance)
				.havingLibrary(library)
					.havingSpecialPermissions(
							new String[][] { { "admin", MANAGER }, { ALL_OTHER_USERS, COLLABORATOR } })
					.build();

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.parentChange(null);

		setPermissionsAndCapture(instance, builder.build());

		verifyEntityPermission(entityPermissionCaptor.getValue(), instance.getId(), null, false, library.getId(), false,
				new String[][] { { "admin", MANAGER }, { ALL_OTHER_USERS, COLLABORATOR } });

		Collection<PermissionAssignmentChange> changesSet = eventCaptor.getValue().getChangesSet();
		assertThat(changesSet.size(), is(0));
	}

	private static void verifyEntityPermission(EntityPermission entityPermission, String targetId, String parent,
			boolean inheritFromParent, String library, boolean inheritFromLibrary, String[][] expectedAssignments) {
		assertEquals(targetId, entityPermission.getTargetId());

		String parentId = entityPermission.getParent() != null ? entityPermission.getParent().getTargetId() : null;
		assertEquals(parent, parentId);

		assertEquals(inheritFromParent, entityPermission.getInheritFromParent());

		String libraryId = entityPermission.getLibrary() != null ? entityPermission.getLibrary().getTargetId() : null;
		assertEquals(library, libraryId);

		assertEquals(inheritFromLibrary, entityPermission.getInheritFromLibrary());

		verifyAssignments(entityPermission.getAssignments(), expectedAssignments);
	}

	private static void verifyAssignments(Collection<AuthorityRoleAssignment> assignments,
			Object[][] expectedAssignments) {
		assertEquals(expectedAssignments.length, assignments.size());

		for (Object[] expectedAssignment : expectedAssignments) {
			boolean found = false;

			for (AuthorityRoleAssignment authorityRoleAssignment : assignments) {
				if (authorityRoleAssignment.getAuthority().equals(expectedAssignment[0])
						&& authorityRoleAssignment.getRole().equals(expectedAssignment[1])) {
					found = true;
					break;
				}
			}

			if (!found) {
				fail("Expected assignment for authority '" + expectedAssignment[0] + "' and role '"
						+ expectedAssignment[1] + "' not found");
			}
		}
	}

	private static void verifyRoleChangeIsPresent(Collection<PermissionAssignmentChange> changesSet, String authority,
			String roleBefore, String roleAfter) {
		boolean present = changesSet
				.stream()
					.filter(change -> authority.equals(change.getAuthorityId()))
					.filter(change -> nullSafeEquals(roleBefore, change.getRoleIdBefore()))
					.filter(change -> nullSafeEquals(roleAfter, change.getRoleIdAfter()))
					.findFirst()
					.isPresent();

		if (!present) {
			fail("No PermissionAssignmentChange (" + authority + "," + roleBefore + "," + roleAfter + ") was found");
		}
	}

	private static void verifyInheritanceChangeIsPresent(Collection<PermissionAssignmentChange> changesSet,
			String inheritFromBefore, String inheritFromAfter, boolean managersOnly) {
		boolean present = changesSet
				.stream()
					.filter(change -> change instanceof PermissionInheritanceChange)
					.map(change -> (PermissionInheritanceChange) change)
					.filter(change -> nullSafeEquals(inheritFromBefore, change.getInheritedFromBefore()))
					.filter(change -> nullSafeEquals(inheritFromAfter, change.getInheritedFromAfter()))
					.filter(change -> managersOnly == change.isManagersOnly())
					.findFirst()
					.isPresent();

		if (!present) {
			fail("No PermissionInheritanceChange (" + inheritFromBefore + "," + inheritFromAfter + "," + managersOnly
					+ ") was found");
		}
	}

	private void setPermissionsAndCapture(InstanceReference instance, Collection<PermissionsChange> changes) {
		permissionService.setPermissions(instance, changes);

		verify(entityPermissionDao).save(entityPermissionCaptor.capture());
		verify(eventService).fire(eventCaptor.capture());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setPermissionsShallNotPassNullReference() {
		permissionService.setPermissions(null, new ArrayList<>());
	}

	@Test
	public void setPermissionsShallDoNothingOnNullChangesList() {
		permissionService.setPermissions(InstanceReferenceMock.createGeneric("o1"), null);

		verify(entityPermissionDao, never()).save(any());
	}

	@Test
	public void setPermissionsShallDoNothingOnEmptyChangesList() {
		permissionService.setPermissions(InstanceReferenceMock.createGeneric("o1"), new ArrayList<>());

		verify(entityPermissionDao, never()).save(any());
	}

	// --- getPermissionModel()

	@Test
	public void getPermissionModelShouldReturnUndefinedPermissionModelWhenNoInstanceIsProvided() {
		PermissionModelType permissionModelType = permissionService.getPermissionModel(null);
		assertFalse(permissionModelType.isDefined());
	}

	@Test
	public void getPermissionModelShouldReturnInheritedPermissionModelWhenInheritFromParentIsTrue() {
		EntityPermission entityPermission = new EntityPermission();
		entityPermission.setTargetId("id");
		entityPermission.setInheritFromParent(true);

		PermissionModelType permissionModelType = getPermissionModel(entityPermission);

		assertTrue(permissionModelType.isInherited());
	}

	@Test
	public void getPermissionModelShouldReturnLibraryPermissionModelWhenInheritFromLibraryIsTrue() {
		EntityPermission entityPermission = new EntityPermission();
		entityPermission.setTargetId("id");
		entityPermission.setInheritFromLibrary(true);

		PermissionModelType permissionModelType = getPermissionModel(entityPermission);

		assertTrue(permissionModelType.isLibrary());
	}

	@Test
	public void getPermissionModelShouldReturnSpecialWhenSpecialAssignmentsAreProvided() {
		EntityPermission entityPermission = new EntityPermission();
		entityPermission.setTargetId("id");
		entityPermission.getAssignments().add(new AuthorityRoleAssignment("", ""));

		PermissionModelType permissionModelType = getPermissionModel(entityPermission);

		assertTrue(permissionModelType.isSpecial());
	}

	@Test
	public void getPermissionModelShouldReturnUndefinedWhenEntityPermissionDoesNotExist() {
		when(entityPermissionDao.loadWithAssignments(anyString())).thenReturn(Optional.empty());

		PermissionModelType permissionModelType = permissionService
				.getPermissionModel(InstanceReferenceMock.createGeneric(""));

		assertFalse(permissionModelType.isDefined());
	}

	private PermissionModelType getPermissionModel(EntityPermission entityPermission) {
		when(entityPermissionDao.loadWithAssignments(entityPermission.getTargetId()))
				.thenReturn(Optional.of(entityPermission));

		return permissionService
				.getPermissionModel(InstanceReferenceMock.createGeneric(entityPermission.getTargetId()));
	}

	// --- restoreParentPermissions()

	@Test
	public void shouldRestoreParentPermissions() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("o1");
		withInstance(instance).build();

		InstanceReference o1_1 = createInstance("o1_1", false, false);
		withInstance(o1_1)
				.inheritingFromParent()
					.havingParent(instance)
					.havingSpecialPermissions(
							new String[][] { { "admin", MANAGER }, { ALL_OTHER_USERS, COLLABORATOR } })
					.build();

		InstanceReference o1_2 = createInstance("o1_2", false, false);
		withInstance(o1_2)
				.havingParent(instance)
					.havingSpecialPermissions(new String[][] { { "regular_user", MANAGER } })
					.build();

		InstanceReference o1_3 = createInstance("o1_3", false, false);
		withInstance(o1_3).havingParent(instance).build();

		// also add the instance itself, because the query returns all the descendants + root
		List<String> ids = Arrays.asList(instance.getId(), o1_1.getId(), o1_2.getId(), o1_3.getId());
		when(entityPermissionDao.getDescendants(eq(instance.getId()))).thenReturn(new ArrayList<>(ids));

		List<String> idsWithoutRoot = new ArrayList<>(ids);
		idsWithoutRoot.remove(instance.getId());

		List<InstanceReference> references = Arrays.asList(o1_1, o1_2, o1_3);

		when(instanceTypeResolver.resolveReferences(eq(idsWithoutRoot))).thenReturn(references);

		permissionService.restoreParentPermissions(instance);

		ArgumentCaptor<EmfEvent> localEventCaptor = ArgumentCaptor.forClass(EmfEvent.class);

		verify(eventService, times(4)).fire(localEventCaptor.capture());

		// verify o1_1
		String targetId = o1_1.getId();
		EntityPermission entityPermission = cachedEntityPermissions.get(targetId);
		verify(entityPermissionDao).deleteAssignments(entityPermission.getId());
		assertFalse(entityPermission.getInheritFromParent());
		verify(entityPermissionDao).save(entityPermission);

		PermissionModelChangedEvent changeEvent = (PermissionModelChangedEvent) localEventCaptor.getAllValues().get(0);
		assertEquals(changeEvent.getInstance(), o1_1);
		Collection<PermissionAssignmentChange> changesSet = changeEvent.getChangesSet();
		assertThat(changesSet.size(), is(3));
		verifyRoleChangeIsPresent(changesSet, "admin", MANAGER, null);
		verifyRoleChangeIsPresent(changesSet, ALL_OTHER_USERS, COLLABORATOR, null);
		verifyInheritanceChangeIsPresent(changesSet, "o1", "o1", true);

		PermissionsRestored permissionsRestored = (PermissionsRestored) localEventCaptor.getAllValues().get(1);
		assertEquals(targetId, permissionsRestored.getInstance().getId());

		// verify o1_2
		targetId = o1_2.getId();
		entityPermission = cachedEntityPermissions.get(targetId);
		verify(entityPermissionDao).deleteAssignments(entityPermission.getId());
		assertFalse(entityPermission.getInheritFromParent());
		verify(entityPermissionDao, never()).save(entityPermission);

		changeEvent = (PermissionModelChangedEvent) localEventCaptor.getAllValues().get(2);
		assertEquals(changeEvent.getInstance(), o1_2);
		changesSet = changeEvent.getChangesSet();
		assertThat(changesSet.size(), is(1));
		verifyRoleChangeIsPresent(changesSet, "regular_user", MANAGER, null);

		permissionsRestored = (PermissionsRestored) localEventCaptor.getAllValues().get(3);
		assertEquals(targetId, permissionsRestored.getInstance().getId());

		// verify o1_2
		targetId = o1_2.getId();
		entityPermission = cachedEntityPermissions.get(targetId);
		verify(entityPermissionDao).deleteAssignments(entityPermission.getId());
		assertFalse(entityPermission.getInheritFromParent());
		verify(entityPermissionDao, never()).save(entityPermission);
		// PermissionModelChangedEvent is already verified in the previous cases
	}

	/**
	 * Given the following hierarchy: Instance o1<br/>
	 * Instance o2 that: has a parent o1, has inherit from parent flag == false, and library which inherit from parent
	 * flag == true.<br/>
	 * Instance o3 that: has a parent o2, has inherit from parent flag == false, and library which inherit from parent
	 * flag == false.<br/>
	 * Instance o4 that: has a parent o3, has inherit from parent flag == false, and library which inherit from parent
	 * flag == true.<br/>
	 * Then: <br/>
	 * For instance o2, the inherit from parent flag should be set to true and a change should be added to the
	 * permissions model changed event.
	 */
	@Test
	public void shouldRestoreParentPermissionsForScenario1() {
		InstanceReference o1 = InstanceReferenceMock.createGeneric("o1");
		withInstance(o1).build();

		InstanceReference o2 = createInstance("o2", true, false);
		withInstance(o2).havingParent(o1).build();

		InstanceReference o3 = createInstance("o3", false, false);
		withInstance(o3).havingParent(o2).build();

		InstanceReference o4 = createInstance("o4", false, false);
		withInstance(o4).havingParent(o3).build();

		List<String> ids = Arrays.asList(o1.getId(), o2.getId(), o3.getId(), o4.getId());
		when(entityPermissionDao.getDescendants(eq(o1.getId()))).thenReturn(new ArrayList<>(ids));

		List<String> idsWithoutRoot = new ArrayList<>(ids);
		idsWithoutRoot.remove(o1.getId());

		List<InstanceReference> references = Arrays.asList(o2, o3, o4);

		when(instanceTypeResolver.resolveReferences(eq(idsWithoutRoot))).thenReturn(references);

		permissionService.restoreParentPermissions(o1);

		ArgumentCaptor<EmfEvent> localEventCaptor = ArgumentCaptor.forClass(EmfEvent.class);

		verify(eventService, times(2)).fire(localEventCaptor.capture());

		PermissionModelChangedEvent event = (PermissionModelChangedEvent) localEventCaptor.getAllValues().get(0);
		assertEquals(event.getInstance(), o2);
		Collection<PermissionAssignmentChange> changesSet = event.getChangesSet();
		assertEquals(1, changesSet.size());
		verifyInheritanceChangeIsPresent(changesSet, "o1", "o1", false);

		PermissionsRestored permissionsRestored = (PermissionsRestored) localEventCaptor.getAllValues().get(1);
		assertEquals(o2.getId(), permissionsRestored.getInstance().getId());
	}

	@Test
	public void getPermissionAssignmentsForInstances_nullCollectionOfReferences_emptyMapExpected() {
		assertTrue(permissionService.getPermissionAssignmentsForInstances(null, null, null).isEmpty());
	}

	@Test
	public void getPermissionAssignmentsForInstances_emptyCollectionOfReferences_emptyMapExpected() {
		assertTrue(permissionService.getPermissionAssignmentsForInstances(emptyList(), null, null).isEmpty());
	}

	@Test
	public void getPermissionAssignmentsForInstances_notEmptyMapExpected() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		InstanceReferenceMock reference = new InstanceReferenceMock(instance);

		Map<String, EntityPermission> daoResult = new HashMap<>(1);
		daoResult.put("instance-id", new EntityPermission());
		when(entityPermissionDao.fetchHierarchyWithAssignmentsForInstances(anyCollection())).thenReturn(daoResult);

		Map<InstanceReference, Map<String, ResourceRole>> results = permissionService
				.getPermissionAssignmentsForInstances(Arrays.asList(reference), null, null);
		assertFalse(results.isEmpty());
	}

	@Test
	public void getPermissionAssignment_nullCollectionOfReferences_emptyMapExpected() {
		Collection<InstanceReference> references = null;
		assertTrue(permissionService.getPermissionAssignment(references, "authority-id").isEmpty());
	}

	@Test
	public void getPermissionAssignment_emptyCollectionOfReferences_emptyMapExpected() {
		assertTrue(permissionService.getPermissionAssignment(emptyList(), "authority-id").isEmpty());
	}

	@Test
	public void getPermissionAssignment_nullAuthorityId_emptyMapExpected() {
		Map<InstanceReference, ResourceRole> result = permissionService
				.getPermissionAssignment(Arrays.asList(new InstanceReferenceMock()), null);
		assertTrue(result.isEmpty());
	}

	@Test
	public void getPermissionAssignment_emptyAuthorityId_emptyMapExpected() {
		Map<InstanceReference, ResourceRole> result = permissionService
				.getPermissionAssignment(Arrays.asList(new InstanceReferenceMock()), "");
		assertTrue(result.isEmpty());
	}

	@Test
	public void getPermissionsInfo_shouldReturnEmptyOptionalIfNullReference() {
		Optional<EntityPermissions> info = permissionService.getPermissionsInfo(null);
		assertFalse(info.isPresent());
	}

	@Test
	public void getPermissionsInfo_shouldReturnEmptyOptionalIfNullReferenceId() {
		Optional<EntityPermissions> info = permissionService.getPermissionsInfo(mock(InstanceReference.class));
		assertFalse(info.isPresent());
	}

	@Test
	public void getPermissionsInfo_shouldReturnPermissionsInfo() {
		when(entityPermissionDao.fetchPermissions("emf:instance"))
				.thenReturn(Optional.of(new EntityPermissions("emf:instance")));
		Optional<EntityPermissions> info = permissionService
				.getPermissionsInfo(InstanceReferenceMock.createGeneric("emf:instance"));
		assertTrue(info.isPresent());
		verify(entityPermissionDao).fetchPermissions("emf:instance");
	}

}
