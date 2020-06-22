package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.permissions.EntityPermissions;

/**
 * Tests for {@link EntityPermissionDao};
 *
 * @author Adrian Mitev
 */
public class EntityPermissionDaoTest {

	@Mock
	private DbDao dbDao;

	@InjectMocks
	private EntityPermissionDao entityPermissionDao;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldNotProvideEntityPermissionWhenNotExistInDb() {
		List<Object[]> permissions = new ArrayList<>();

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<Object>(permissions));

		Optional<EntityPermission> result = entityPermissionDao.fetchHierarchyWithAssignments("o1");

		assertNotNull(result);
		assertFalse(result.isPresent());
	}

	/**
	 * Verifies that {@link EntityPermission} hierarchy is properly built with libraries properly assigned for each
	 * participant in the hierarchy and verifies that the authority role assignments are properly set.
	 */
	@Test
	public void shouldFetchTheWholeHierarchyWithLibrariesAndAssignments() {
		List<Object[]> permissions = new ArrayList<>();
		permissions.add(new Object[] { "o1", null, (short) 2, "l1", (short) 1, (short) 2 });
		permissions.add(new Object[] { "o2", "o1", (short) 1, "l2", (short) 1, (short) 2 });
		permissions.add(new Object[] { "l1", null, (short) 2, null, (short) 2, (short) 2 });
		permissions.add(new Object[] { "l2", null, (short) 2, null, (short) 2, (short) 2 });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(permissions));

		List<Object[]> assignments = new ArrayList<>();
		assignments.add(new Object[] { "o1", "admin", "MANAGER" });
		assignments.add(new Object[] { "o1", "regular_user", "COLLABORATOR" });
		assignments.add(new Object[] { "o2", "regular_user", "MANAGER" });
		assignments.add(new Object[] { "l1", "admin", "COLLABORATOR" });
		assignments.add(new Object[] { "l2", "admin", "CONSUMER" });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(assignments));

		Optional<EntityPermission> result = entityPermissionDao.fetchHierarchyWithAssignments("o2");
		assertNotNull(result);
		assertTrue(result.isPresent());

		verifyEntityPermission(result.get(), "o2", "o1", true, "l2", true,
				new String[][] { { "regular_user", "MANAGER" } });

		EntityPermission parent = result.get().getParent();

		verifyEntityPermission(parent, "o1", null, false, "l1", true,
				new String[][] { { "admin", "MANAGER" }, { "regular_user", "COLLABORATOR" } });

		EntityPermission library = result.get().getLibrary();

		verifyEntityPermission(library, "l2", null, false, null, false, new String[][] { { "admin", "CONSUMER" } });

		EntityPermission parentLibrary = parent.getLibrary();

		verifyEntityPermission(parentLibrary, "l1", null, false, null, false,
				new String[][] { { "admin", "COLLABORATOR" } });
	}

	private static void verifyEntityPermission(EntityPermission entityPermission, String targetId, String parent,
			boolean inheritFromParent, String library, boolean inheritFromLibrary, String[][] roles) {
		assertEquals(entityPermission.getTargetId(), targetId);

		if (parent != null) {
			assertEquals(entityPermission.getParent().getTargetId(), parent);
		} else {
			assertNull(entityPermission.getParent());
		}

		assertEquals(inheritFromParent, entityPermission.getInheritFromParent());

		if (library != null) {
			assertEquals(entityPermission.getLibrary().getTargetId(), library);
		} else {
			assertNull(entityPermission.getLibrary());
		}

		assertEquals(inheritFromLibrary, entityPermission.getInheritFromLibrary());

		verifyAssignments(entityPermission.getAssignments(), roles);
	}

	private static void verifyAssignments(Set<AuthorityRoleAssignment> assignments, String[][] expectations) {
		assertEquals(assignments.size(), expectations.length);

		for (AuthorityRoleAssignment assignment : assignments) {
			boolean valid = false;

			for (String[] expectation : expectations) {
				if (expectation[0].equals(assignment.getAuthority()) && expectation[1].equals(assignment.getRole())) {
					valid = true;
					break;
				}
			}

			if (!valid) {
				fail("Unexpected assignment: " + assignment.getAuthority() + " " + assignment.getRole());
			}
		}
	}

	@Test
	public void fetchHierarchyWithAssignments_nullId_nullExpected() {
		Optional<EntityPermission> object = entityPermissionDao.fetchHierarchyWithAssignments(null);
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void fetchHierarchyWithAssignments_emptyId_nullExpected() {
		Optional<EntityPermission> object = entityPermissionDao.fetchHierarchyWithAssignments("");
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void shouldPreventHierarchyLoops() {
		List<Object[]> permissions = new ArrayList<>();
		permissions.add(new Object[] { "o1", "o2", (short) 1, "l1", (short) 1, (short) 2 });
		permissions.add(new Object[] { "o2", "o1", (short) 1, "l2", (short) 1, (short) 2 });
		permissions.add(new Object[] { "l1", null, (short) 2, null, (short) 2, (short) 2 });
		permissions.add(new Object[] { "l2", null, (short) 2, null, (short) 2, (short) 2 });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(permissions));

		List<Object[]> assignments = new ArrayList<>();
		assignments.add(new Object[] { "o1", "admin", "MANAGER" });
		assignments.add(new Object[] { "o1", "regular_user", "COLLABORATOR" });
		assignments.add(new Object[] { "o2", "regular_user", "MANAGER" });
		assignments.add(new Object[] { "l1", "admin", "COLLABORATOR" });
		assignments.add(new Object[] { "l2", "admin", "CONSUMER" });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(assignments));

		Optional<EntityPermission> result = entityPermissionDao.fetchHierarchyWithAssignments("o2");
		assertNotNull(result);
		assertTrue(result.isPresent());

		EntityPermission parent = result.get().getParent();

		assertNull(parent.getParent());
	}

	@Test
	public void load_nullId_nullExpected() {
		Optional<EntityPermission> object = entityPermissionDao.load(null);
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void load_emptyId_nullExpected() {
		Optional<EntityPermission> object = entityPermissionDao.load("");
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void load_emptyFetchResult_nullExpected() {
		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_BY_TARGET_ID_KEY), anyListOf(Pair.class)))
				.thenReturn(emptyList());
		Optional<EntityPermission> object = entityPermissionDao.load("instance-id");
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void load_emptyFetchResult_notNullExpected() {
		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_BY_TARGET_ID_KEY), anyListOf(Pair.class)))
				.thenReturn(Arrays.asList(new EntityPermission()));
		Optional<EntityPermission> object = entityPermissionDao.load("instance-id");
		assertNotNull(object);
		assertTrue(object.isPresent());
	}

	@Test
	public void loadWithAssignments_nullId_nullExpected() {
		Optional<EntityPermission> object = entityPermissionDao.loadWithAssignments(null);
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void loadWithAssignments_emptyId_nullExpected() {
		Optional<EntityPermission> object = entityPermissionDao.loadWithAssignments("");
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void loadWithAssignments_emptyFetchResult_nullExpected() {
		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS_KEY),
				anyListOf(Pair.class))).thenReturn(emptyList());
		Optional<EntityPermission> object = entityPermissionDao.loadWithAssignments("instance-id");
		assertNotNull(object);
		assertFalse(object.isPresent());
	}

	@Test
	public void loadWithAssignments_emptyFetchResult_notNullExpected() {
		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS_KEY),
				anyListOf(Pair.class))).thenReturn(Arrays.asList(new EntityPermission()));
		Optional<EntityPermission> object = entityPermissionDao.loadWithAssignments("instance-id");
		assertNotNull(object);
		assertTrue(object.isPresent());
	}

	@Test
	public void save_internalServiceCalled() {
		EntityPermission permission = new EntityPermission();
		entityPermissionDao.save(permission);
		verify(dbDao).saveOrUpdate(permission);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteAssignments_internalServiceCalled() {
		entityPermissionDao.deleteAssignments(10L);
		verify(dbDao).executeUpdate(eq(EntityPermission.QUERY_DELETE_ASSIGNMENTS_KEY), anyListOf(Pair.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteAssignments_doNothingOnNullId() {
		entityPermissionDao.deleteAssignments(null);
		verify(dbDao, never()).executeUpdate(eq(EntityPermission.QUERY_DELETE_ASSIGNMENTS_KEY), anyListOf(Pair.class));
	}

	@Test
	public void fetchHierarchyWithAssignmentsForInstances_nullCollection_expectedEmptyMap() {
		Map<String, EntityPermission> results = entityPermissionDao.fetchHierarchyWithAssignmentsForInstances(null);
		assertTrue(results.isEmpty());
	}

	@Test
	public void fetchHierarchyWithAssignmentsForInstances_emptyCollection_expectedEmptyMap() {
		Map<String, EntityPermission> results = entityPermissionDao
				.fetchHierarchyWithAssignmentsForInstances(emptyList());
		assertTrue(results.isEmpty());
	}

	@Test
	public void fetchHierarchyWithAssignmentsForInstances_noPermissionFetched_expectedEmptyMap() {
		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY),
				anyListOf(Pair.class))).thenReturn(emptyList());
		Map<String, EntityPermission> results = entityPermissionDao
				.fetchHierarchyWithAssignmentsForInstances(Arrays.asList("instance-id-1", "instance-id-2"));
		assertTrue(results.isEmpty());
	}

	@Test
	public void fetchHierarchyWithAssignmentsForInstances_permissionFetched_expectedNotEmptyMap() {
		List<Object[]> permissions = new ArrayList<>();
		permissions.add(new Object[] { "instance-id", null, (short) 2, "library-id", (short) 1, (short) 2 });
		permissions.add(new Object[] { "library-id", null, (short) 2, null, (short) 2, (short) 1 });
		permissions.add(new Object[] { "instance-id-2", null, (short) 2, null, (short) 2, (short) 2 });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(permissions));

		List<Object[]> assignments = new ArrayList<>();
		assignments.add(new Object[] { "instance-id", "admin", "MANAGER" });
		assignments.add(new Object[] { "library-id", "admin", "COLLABORATOR" });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(assignments));

		Map<String, EntityPermission> results = entityPermissionDao
				.fetchHierarchyWithAssignmentsForInstances(Arrays.asList("instance-id", "instance-id-2"));
		assertFalse(results.isEmpty());
	}

	@Test
	public void getDescendants_emptyId() {
		assertTrue(entityPermissionDao.getDescendants("").isEmpty());
		verifyZeroInteractions(dbDao);
	}

	@Test
	public void getDescendants_nullId() {
		assertTrue(entityPermissionDao.getDescendants(null).isEmpty());
		verifyZeroInteractions(dbDao);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getDescendants_internalServiceCalled() {
		entityPermissionDao.getDescendants("instance-id");
		verify(dbDao).fetchWithNamed(eq(EntityPermission.QUERY_GET_DESCENDANTS_KEY), anyListOf(Pair.class));
	}

	@Test
	public void fetchPermissions_shouldReturnEmptyOptionalIfNotFoundInDb() {
		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY),
				anyListOf(Pair.class))).thenReturn(emptyList());
		Optional<EntityPermissions> entityPermissions = entityPermissionDao.fetchPermissions("emf:instance");
		assertFalse(entityPermissions.isPresent());
	}

	@Test
	public void fetchPermissions_shouldReturnTheInstancePermissions() {
		List<Object[]> permissions = new ArrayList<>();
		permissions.add(new Object[] { "instance-id", "parent-id", (short) 2, "library-id", (short) 1, (short) 2 });
		permissions.add(new Object[] { "library-id", null, (short) 2, null, (short) 2, (short) 1 });
		permissions.add(new Object[] { "parent-id", null, (short) 2, null, (short) 2, (short) 2 });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(permissions));

		List<Object[]> assignments = new ArrayList<>();
		assignments.add(new Object[] { "instance-id", "admin", "MANAGER" });
		assignments.add(new Object[] { "library-id", "admin", "COLLABORATOR" });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(assignments));

		Optional<EntityPermissions> entityPermissions = entityPermissionDao.fetchPermissions("instance-id");
		assertTrue(entityPermissions.isPresent());
		EntityPermissions foundPermissions = entityPermissions.get();
		assertEquals("instance-id", foundPermissions.getEntityId());
		assertEquals("parent-id", foundPermissions.getParent());
		assertEquals("library-id", foundPermissions.getLibrary());
		assertFalse(foundPermissions.isInheritFromParent());
		assertTrue(foundPermissions.isInheritFromLibrary());
		List<EntityPermissions.Assignment> assignmentList = foundPermissions.getAssignments().collect(Collectors.toList());
		assertFalse(assignmentList.isEmpty());
		EntityPermissions.Assignment assignment = assignmentList.get(0);
		assertEquals("admin", assignment.getAuthority());
		assertEquals("MANAGER", assignment.getRole());
	}

	@Test
	public void should_ReturnInstancePermissions_When_InstanceHasNoInheritedPermissions() {
		List<Object[]> permissions = new ArrayList<>();
		permissions.add(new Object[] { "instance-id", "parent-id", (short) 2, "library-id", (short) 2, (short) 2 });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(permissions));

		List<Object[]> assignments = new ArrayList<>();
		assignments.add(new Object[] { "instance-id", "admin", "MANAGER" });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(assignments));

		Optional<EntityPermissions> entityPermissions = entityPermissionDao.fetchPermissions("instance-id");
		assertTrue(entityPermissions.isPresent());

		EntityPermissions foundPermissions = entityPermissions.get();
		assertEquals("instance-id", foundPermissions.getEntityId());
		assertNull(foundPermissions.getParent());
		assertNull(foundPermissions.getLibrary());
		assertFalse(foundPermissions.isInheritFromParent());
		assertFalse(foundPermissions.isInheritFromLibrary());

		List<EntityPermissions.Assignment> assignmentList = foundPermissions.getAssignments()
				.collect(Collectors.toList());
		assertFalse(assignmentList.isEmpty());
		EntityPermissions.Assignment assignment = assignmentList.get(0);
		assertEquals("admin", assignment.getAuthority());
		assertEquals("MANAGER", assignment.getRole());
	}

	@Test
	public void should_ProperlySetIsLibraryFlag_When_InstanceIsLibrary() {
		List<Object[]> permissions = new ArrayList<>();
		permissions.add(new Object[] { "instance-id", "parent-id", (short) 2, "library-id", (short) 2, (short) 2 });
		permissions.add(new Object[] { "library-id", null, (short) 2, null, (short) 2, (short) 1 });

		when(dbDao.fetchWithNamed(eq(EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY), any()))
				.thenReturn(new ArrayList<>(permissions));

		Optional<EntityPermissions> entityPermissions = entityPermissionDao.fetchPermissions("library-id");
		assertTrue(entityPermissions.isPresent());

		EntityPermissions libraryPermissions = entityPermissions.get();
		assertEquals("library-id", libraryPermissions.getEntityId());
		assertTrue(libraryPermissions.isLibrary());

		entityPermissions = entityPermissionDao.fetchPermissions("instance-id");
		assertTrue(entityPermissions.isPresent());

		EntityPermissions instancePermissions = entityPermissions.get();
		assertEquals("instance-id", instancePermissions.getEntityId());
		assertFalse(instancePermissions.isLibrary());
	}

}
