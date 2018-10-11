package com.sirma.itt.seip.permissions.sync.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.SimpleResultItem;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link CalculateInstancePermissionProcessor}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/07/2017
 */
public class CalculateInstancePermissionProcessorTest {
	private static final String NAMESPACE = "conc:";
	private static final String READ_WRITE = NAMESPACE + "SecurityRoleTypes-Read-Write";
	private static final String READ = NAMESPACE + "SecurityRoleTypes-Read";
	private static final String MANAGER = NAMESPACE + "SecurityRoleTypes-Manager";

	private static final String INSTANCE = "emf:instance";
	private static final InstanceReferenceMock REFERENCE_MOCK = InstanceReferenceMock.createGeneric(INSTANCE);

	@InjectMocks
	private CalculateInstancePermissionProcessor processor;

	@Mock
	private PermissionSyncUtil syncUtil;
	@Mock
	private PermissionService permissionService;
	@Mock
	private NamespaceRegistryService registryService;
	@Mock
	private SearchService searchService;

	private static Map<String, String> roleTypes = new HashMap<>();

	static {
		roleTypes.put("CONSUMER", READ);
		roleTypes.put("COLLABORATOR", READ_WRITE);
		roleTypes.put("CONTRIBUTOR", READ_WRITE);
		roleTypes.put("CREATOR", READ_WRITE);
		roleTypes.put("MANAGER", MANAGER);
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(syncUtil.prepareSearchArguments(anyString(), anyBoolean())).thenReturn(new SearchArguments<>());
		when(syncUtil.getRoleTypesMapping()).thenReturn(roleTypes);

		when(registryService.getDataGraph()).thenReturn(EMF.DATA_CONTEXT);
		when(registryService.buildUri(anyString())).then(a -> SimpleValueFactory.getInstance().createIRI(a
				.getArgumentAt(0, String.class)));
	}

	@Test
	public void should_ProduceNull_IfBothAreEmpty() throws Exception {
		mockRelationalData(null);
		mockSemanticDataWithNoData();

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_NotGenerateDiffForNewParent_IfInheritanceIsNotEnabled() throws Exception {
		EntityPermissions relationalPermissions = new RelationalPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:parent")
				.build();
		mockRelationalData(relationalPermissions);
		mockSemanticDataWithNoData();

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_GenerateDiffForNewParent_IfParentInheritanceIsEnabled() throws Exception {
		EntityPermissions relationalPermissions = new RelationalPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:parent")
				.setParentInheritance()
				.build();
		mockRelationalData(relationalPermissions);
		mockSemanticDataWithNoData();

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		assertEquals("emf:parent", item.getParentInheritanceToAdd());
		assertTrue(item.isParentInheritanceChanged());
	}

	@Test
	public void should_shouldNotGenerateDiffToRemovedParent_IfParentInheritanceIsEnabledAndNoParentIsSet()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setParentInheritance()
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:oldParent")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_NotGenerateDiffForRemovedParent_IfParentInheritanceIsNotEnabled()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setParent("emf:parent")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:oldParent").setParentInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		assertTrue(item.isParentInheritanceChanged());
		assertNull(item.getParentInheritanceToAdd());
		assertTrue(item.getParentInheritanceToRemove().contains("emf:oldParent"));
	}

	@Test
	public void should_NotGenerateDiffForParentChange_IfParentInheritanceIsEnabledButParentsAreTheSame()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setParent("emf:parent").setParentInheritance()
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:parent").setParentInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_GenerateDiffForParentChange_IfParentInheritanceIsEnabledAndParentsDiffer()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setParent("emf:parent").setParentInheritance()
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:oldParent").setParentInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		assertTrue(item.isParentInheritanceChanged());
		assertEquals("emf:parent", item.getParentInheritanceToAdd());
		assertTrue(item.getParentInheritanceToRemove().contains("emf:oldParent"));
	}

	@Test
	public void should_NotGenerateDiffForNewLibrary_IfInheritanceIsNotEnabled() throws Exception {
		EntityPermissions relationalPermissions = new RelationalPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Case")
				.build();
		mockRelationalData(relationalPermissions);
		mockSemanticDataWithNoData();

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_GenerateDiffForNewLibrary_IfLibraryInheritanceIsEnabled() throws Exception {
		EntityPermissions relationalPermissions = new RelationalPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Case")
				.setLibraryInheritance()
				.build();
		mockRelationalData(relationalPermissions);
		mockSemanticDataWithNoData();

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		assertEquals("emf:Case", item.getLibraryInheritanceToAdd());
		assertTrue(item.isLibraryPermissionsChanged());
	}

	@Test
	public void should_NotGenerateDiffToRemovedLibrary_IfLibraryInheritanceIsDisabledAndNoLibraryIsSet()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE)
				.addPermission("emf:user1", "CONSUMER")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Case")
				.addPermission("emf:user1", "CONSUMER")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(null);
	}

	/**
	 * Generally if this case happen on live server means broken data in the relational database as all instances
	 * should have library class assigned
	 */
	@Test
	public void should_shouldNotGenerateDiffToRemovedLibrary_IfLibraryInheritanceIsEnabledAndNoLibraryIsSet()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setLibraryInheritance()
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Case")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_NotGenerateDiffForRemovedLibrary_IfLibraryInheritanceIsNotEnabled()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setLibrary("emf:Case")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Document").setLibraryInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		assertTrue(item.isLibraryPermissionsChanged());
		assertNull(item.getLibraryInheritanceToAdd());
		assertTrue(item.getLibraryInheritanceToRemove().contains("emf:Document"));
	}

	@Test
	public void should_NotGenerateDiffForLibraryChange_IfLibraryInheritanceIsEnabledButLibrariesAreTheSame()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setLibrary("emf:Case")
				.setLibraryInheritance()
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Case").setLibraryInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_GenerateDiffForLibraryChange_IfLibraryInheritanceIsEnabledAndLibrariesDiffer()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).setLibrary("emf:Case")
				.setLibraryInheritance()
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Document").setLibraryInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		assertTrue(item.isLibraryPermissionsChanged());
		assertEquals("emf:Case", item.getLibraryInheritanceToAdd());
		assertTrue(item.getLibraryInheritanceToRemove().contains("emf:Document"));
	}

	@Test
	public void should_GenerateDiffForAssignmentChange()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).addPermission("emf:user1",
				"CONSUMER")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.addPermission("emf:user1", "COLLABORATOR")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		item.getToAdd().forEach(entity -> {
			assertEquals("emf:user1", entity.authority);
			assertEquals(READ, entity.roleType);
		});
		item.getToRemove().forEach(entity -> {
			assertEquals("emf:user1", entity.authority);
			assertEquals(READ_WRITE, entity.roleType);
		});
	}

	@Test
	public void should_GenerateDiffForAssignmentAdd() throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).addPermission("emf:user1",
				"CONSUMER")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		item.getToAdd().forEach(entity -> {
			assertEquals("emf:user1", entity.authority);
			assertEquals(READ, entity.roleType);
		});
		assertEquals(0L, item.getToRemove().count());
	}

	@Test
	public void should_GenerateDiffForAssignmentRemove() throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE)
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.addPermission("emf:user1", "COLLABORATOR")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		assertEquals(0L, item.getToAdd().count());
		item.getToRemove().forEach(entity -> {
			assertEquals("emf:user1", entity.authority);
			assertEquals(READ_WRITE, entity.roleType);
		});
	}

	@Test
	public void should_GenerateDiffForAssignmentChange_withManagerPermissions()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).addPermission("emf:user1",
				"CONSUMER")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.addPermission("emf:user1", "COLLABORATOR")
				.addPermission("emf:user1", "MANAGER")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		item.getToAdd().forEach(entity -> {
			assertEquals("emf:user1", entity.authority);
			assertEquals(READ, entity.roleType);
		});
		long count = item.getToRemove()
				.filter(entity -> READ_WRITE.equals(entity.roleType) || MANAGER.equals(entity.roleType))
				.filter(entity -> "emf:user1".equals(entity.authority))
				.count();
		assertEquals(2, count);
	}

	@Test
	public void should_GenerateDiffForAssignmentChange_PermissionsUpgrade()
			throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE).addPermission("emf:user1",
				"COLLABORATOR")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.addPermission("emf:user1", "CONSUMER")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
		assertTrue(item.hasChanges());
		item.getToAdd().forEach(entity -> {
			assertEquals("emf:user1", entity.authority);
			assertEquals(READ_WRITE, entity.roleType);
		});
		item.getToRemove().forEach(entity -> {
			assertEquals("emf:user1", entity.authority);
			assertEquals(READ, entity.roleType);
		});
	}

	//==================== Special cases

	/**
	 * See CMF-25913
	 */
	@Test
	public void should_NotGenerateDiff_IfParentAndLibraryIsTheSame_ParentInheritanceIsDisabled() throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:Template")
				.setLibrary("emf:Template")
				.setLibraryInheritance()
				.build());

		// the semantic query cannot distinguish the case when the parent and the library is the same instance
		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:Template")
				.setLibrary("emf:Template")
				.setLibraryInheritance()
				.setParentInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull("Should not have a diff according to CMF-25913", item);
	}

	/**
	 * See CMF-25913
	 */
	@Test
	public void should_NotGenerateDiff_IfParentAndLibraryIsTheSame_LibraryInheritanceIsDisabled() throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:Template")
				.setLibrary("emf:Template")
				.setParentInheritance()
				.build());

		// the semantic query cannot distinguish the case when the parent and the library is the same instance
		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:Template")
				.setLibrary("emf:Template")
				.setLibraryInheritance()
				.setParentInheritance()
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull("Should not have a diff according to CMF-25913", item);
	}

	@Test
	public void should_NotGenerateDiff_IfParentInheritanceIsEnabledByNoParentIsSetAndThereIsParentInSemantic() throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE)
				.setParentInheritance()
				.setLibrary("emf:Case")
				.setLibraryInheritance()
				.addPermission("emf:test", "MANAGER")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setParent("emf:test")
				.setLibrary("emf:Case")
				.setLibraryInheritance()
				.addPermission("emf:test", "MANAGER")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_NotGenerateDiff_IfLibraryInheritanceIsNotEnabledAndLibraryIsNotSetButThereIsNotEnabledLibraryInSemantic() throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE)
				.addPermission("emf:test", "CONSUMER")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Case")
				.addPermission("emf:test", "CONSUMER")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNull(item);
	}

	@Test
	public void should_GenerateDiff_ifParentInheritanceIsEnabledInRDBButNotInSemantic() throws Exception {
		mockRelationalData(new RelationalPermissionBuilder().setInstance(INSTANCE)
				.addPermission("emf:test", "MANAGER")
				.setParent("emf:parent")
				.setParentInheritance()
				.setLibrary("emf:Image")
				.build());

		mockSemanticData(new SemanticPermissionBuilder().setInstance(INSTANCE)
				.setLibrary("emf:Image")
				.setParent("emf:parent")
				.addPermission("emf:test", "MANAGER")
				.build());

		PermissionsDiff item = (PermissionsDiff) processor.processItem(REFERENCE_MOCK);
		assertNotNull(item);
	}

	private void mockRelationalData(EntityPermissions permissions) {
		when(permissionService.getPermissionsInfo(any())).thenReturn(Optional.ofNullable(permissions));
	}

	private void mockSemanticDataWithNoData() {
		when(searchService.stream(any(), any())).then(a -> Stream.empty());
	}

	private void mockSemanticData(Collection<ResultItem> items) {
		when(searchService.stream(any(), any())).then(a -> items.stream());
	}

	private abstract static class PermissionsBuilder<R, B extends PermissionsBuilder<R, B>> {
		String instance;
		String parent;
		String library;
		List<Pair<String, String>> permissions = new LinkedList<>();
		boolean inheritFromLibrary;
		boolean inheritFromParent;
		boolean isLibrary;

		B setInstance(String instance) {
			this.instance = instance;
			return (B) this;
		}

		B asLibrary() {
			isLibrary = true;
			return (B) this;
		}

		B setParentInheritance() {
			inheritFromParent = true;
			return (B) this;
		}

		B setLibraryInheritance() {
			inheritFromLibrary = true;
			return (B) this;
		}

		B setParent(String parent) {
			this.parent = parent;
			return (B) this;
		}

		B setLibrary(String library) {
			this.library = library;
			return (B) this;
		}

		B addPermission(String authority, String role) {
			permissions.add(new Pair<>(authority, role));
			return (B) this;
		}

		abstract R build();
	}

	private static class RelationalPermissionBuilder
			extends PermissionsBuilder<EntityPermissions, RelationalPermissionBuilder> {

		EntityPermissions build() {
			EntityPermissions ep = new EntityPermissions(instance, parent, library, inheritFromParent,
					inheritFromLibrary, isLibrary);
			permissions.forEach(p -> ep.addAssignment(p.getFirst(), p.getSecond()));
			return ep;
		}
	}

	private static class SemanticPermissionBuilder extends PermissionsBuilder<Collection<ResultItem>,
			SemanticPermissionBuilder> {

		@Override
		Collection<ResultItem> build() {
			List<ResultItem> items = new LinkedList<>();

			if (parent != null) {
				SimpleResultItem item = SimpleResultItem.create().add("parent", parent);
				if (inheritFromParent) {
					item.add("inheritFromParent", inheritFromParent);
				}
				items.add(item);
			}

			if (library != null) {
				SimpleResultItem item = SimpleResultItem.create().add("library", library);
				if (inheritFromLibrary) {
					item.add("inheritFromLibrary", inheritFromLibrary);
				}
				items.add(item);
			}

			permissions.stream()
					.map(p ->
							SimpleResultItem.create()
									.add("assignedTo", p.getFirst())
									.add("isLibrary", isLibrary)
									.add("roleType", roleTypes.get(p.getSecond()))
									.add("role", instance + "_" + roleTypes.get(p.getSecond()).substring(NAMESPACE.length()))
					).forEach(items::add);

			return items;
		}
	}

}
