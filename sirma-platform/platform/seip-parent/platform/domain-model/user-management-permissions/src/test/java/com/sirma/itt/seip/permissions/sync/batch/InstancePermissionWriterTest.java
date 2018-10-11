package com.sirma.itt.seip.permissions.sync.batch;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * Test for {@link InstancePermissionWriter}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 10/07/2017
 */
public class InstancePermissionWriterTest {
	public static final String INSTANCE_ID = "emf:instance-id";
	public static final String NEW_PARENT = "emf:new-parent";
	public static final String OLD_PARENT = "emf:old-parent";
	public static final String NEW_LIBRARY = "emf:new-library";
	public static final String OLD_LIBRARY = "emf:old-library";
	public static final String SECURITY_ROLE_TYPE_READ = "conc:SecurityRoleTypes-Read";
	public static final String SECURITY_ROLE_TYPE_MANAGER = "conc:SecurityRoleTypes-Manager";
	public static final String SECURITY_ROLE_TYPE_WRITE = "conc:SecurityRoleTypes-Read-Write";
	public static final String USER_1 = "emf:user1";
	@InjectMocks
	private InstancePermissionWriter writer;

	@Mock
	private NamespaceRegistryService registryService;
	@Mock
	private PermissionSyncUtil syncUtil;
	@Mock
	private RepositoryConnection connection;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(registryService.getDataGraph()).thenReturn(EMF.DATA_CONTEXT);
		when(registryService.buildUri(anyString()))
				.then(a -> createIRI(a.getArgumentAt(0, String.class)));

		when(syncUtil.isManagerRoleType("conc:SecurityRoleTypes-Manager")).thenReturn(Boolean.TRUE);
	}

	private static IRI createIRI(String iri) {
		return SimpleValueFactory.getInstance().createIRI(iri);
	}

	@Test
	public void writeItems_shouldHandleNewParent() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.parentInheritanceChanged(Collections.emptySet(), NEW_PARENT);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).add(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(NEW_PARENT), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandleParentRemoval() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.parentInheritanceChanged(Collections.singleton(OLD_PARENT), null);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).remove(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(OLD_PARENT), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandleParentChange() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.parentInheritanceChanged(Collections.singleton(OLD_PARENT), NEW_PARENT);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).add(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(NEW_PARENT), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
		verify(connection).remove(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(OLD_PARENT), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandleMoreThanOneParentChange() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.parentInheritanceChanged(new HashSet<>(Arrays.asList(OLD_PARENT, NEW_PARENT)), NEW_PARENT);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).remove(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(OLD_PARENT), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandleNewLibrary() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.libraryInheritanceChanged(Collections.emptySet(), NEW_LIBRARY);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).add(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(NEW_LIBRARY), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandleLibraryRemoval() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.libraryInheritanceChanged(Collections.singleton(OLD_LIBRARY), null);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).remove(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(OLD_LIBRARY), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandleLibraryChange() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.libraryInheritanceChanged(Collections.singleton(OLD_LIBRARY), NEW_LIBRARY);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).add(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(NEW_LIBRARY), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
		verify(connection).remove(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(OLD_LIBRARY), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandleMoreThanOneLibraryChange() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.libraryInheritanceChanged(new HashSet<>(Arrays.asList(OLD_LIBRARY, NEW_LIBRARY)), NEW_LIBRARY);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).remove(argThat(CustomMatcher.of((Model model) ->
						model.contains(createIRI(OLD_LIBRARY), Security.HAS_PERMISSION, createIRI(INSTANCE_ID)))),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandlePermissionsAdd_generateMissingPermissionRoleInstance() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.addRoleChange(USER_1, SECURITY_ROLE_TYPE_READ, null);

		writer.writeItems(Collections.singletonList(diff));
		verify(connection).add(argThat(CustomMatcher.of((Model model) -> {
					String roleId = buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_READ);
					return model.contains(createIRI(USER_1), Security.ASSIGNED_TO, createIRI(roleId))
							&& model.contains(createIRI(roleId), RDF.TYPE, Security.ROLE)
							&& model.contains(createIRI(roleId), Security.HAS_ROLE_TYPE, createIRI(SECURITY_ROLE_TYPE_READ));
				})),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandlePermissionsAdd_generateMissingPermissionManagerRoleInstance() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		diff.addRoleChange(USER_1, SECURITY_ROLE_TYPE_MANAGER, null);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).add(argThat(CustomMatcher.of((Model model) -> {
					String roleId = buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_MANAGER);
					return model.contains(createIRI(USER_1), Security.ASSIGNED_TO, createIRI(roleId))
							&& model.contains(createIRI(roleId), RDF.TYPE, Security.ROLE)
							&& model.contains(createIRI(roleId), Security.HAS_ROLE_TYPE, createIRI(SECURITY_ROLE_TYPE_MANAGER))
							&& model.contains(createIRI(roleId), Security.IS_MANAGER_OF, createIRI(INSTANCE_ID));
				})),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandlePermissionsAdd_withAlreadyDefinedRoleInstance() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		Map<String, String> instanceRoles = new HashMap<>();
		instanceRoles.put(SECURITY_ROLE_TYPE_READ, buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_READ));
		diff.setInstanceRoles(instanceRoles);
		diff.addRoleChange(USER_1, SECURITY_ROLE_TYPE_READ, null);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).add(argThat(CustomMatcher.of((Model model) -> {
					String roleId = buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_READ);
					return model.contains(createIRI(USER_1), Security.ASSIGNED_TO, createIRI(roleId))
							&& !model.contains(createIRI(roleId), RDF.TYPE, Security.ROLE)
							&& !model.contains(createIRI(roleId), Security.HAS_ROLE_TYPE, createIRI
							(SECURITY_ROLE_TYPE_READ));
				})),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandlePermissionsRemoval() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		Map<String, String> instanceRoles = new HashMap<>();
		instanceRoles.put(SECURITY_ROLE_TYPE_READ, buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_READ));
		diff.setInstanceRoles(instanceRoles);
		diff.addRoleChange(USER_1, null, SECURITY_ROLE_TYPE_READ);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).remove(argThat(CustomMatcher.of((Model model) -> {
					String roleId = buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_READ);
					return model.contains(createIRI(USER_1), Security.ASSIGNED_TO, createIRI(roleId));
				})),
				eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void writeItems_shouldHandlePermissionsChange() throws Exception {
		PermissionsDiff diff = new PermissionsDiff(INSTANCE_ID);
		Map<String, String> instanceRoles = new HashMap<>();
		instanceRoles.put(SECURITY_ROLE_TYPE_READ, buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_READ));
		diff.setInstanceRoles(instanceRoles);
		diff.addRoleChange(USER_1, SECURITY_ROLE_TYPE_WRITE, SECURITY_ROLE_TYPE_READ);

		writer.writeItems(Collections.singletonList(diff));

		verify(connection).add(argThat(CustomMatcher.of((Model model) -> {
					String roleId = buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_WRITE);
					return model.contains(createIRI(USER_1), Security.ASSIGNED_TO, createIRI(roleId))
							&& model.contains(createIRI(roleId), RDF.TYPE, Security.ROLE)
							&& model.contains(createIRI(roleId), Security.HAS_ROLE_TYPE, createIRI(SECURITY_ROLE_TYPE_WRITE));
				})),
				eq(EMF.DATA_CONTEXT));
		verify(connection).remove(argThat(CustomMatcher.of((Model model) -> {
					String roleId = buildInstanceRole(INSTANCE_ID, SECURITY_ROLE_TYPE_READ);
					return model.contains(createIRI(USER_1), Security.ASSIGNED_TO, createIRI(roleId));
				})),
				eq(EMF.DATA_CONTEXT));
	}

	private String buildInstanceRole(String instanceId, String roleType) {
		return instanceId + "_" + roleType.split(":")[1];
	}

}
