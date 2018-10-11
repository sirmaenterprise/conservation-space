package com.sirma.itt.emf.semantic.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.permissions.PermissionInheritanceChange;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.semantic.NamespaceRegistryService;

public class PermissionsChangeObserverTest {

	private static final IRI INSTANCE_ID = SimpleValueFactory.getInstance().createIRI("http://o1");

	@InjectMocks
	private PermissionsChangeObserver observer;

	@Mock
	private NamespaceRegistryService registryService;

	@Mock
	private javax.enterprise.inject.Instance<RepositoryConnection> repositoryConnection;

	@Mock
	private SearchService searchService;

	private static final String HAS_PERMISSION = "hasPermission";
	private static final String MANAGER_OF = "isManagerOf";

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		RepositoryConnection connection = mock(RepositoryConnection.class);

		when(repositoryConnection.get()).thenReturn(connection);
		when(registryService.buildUri(any()))
				.thenAnswer(invokation -> SimpleValueFactory.getInstance().createIRI(invokation.getArgumentAt(0, String.class)));

		when(searchService.getFilter(any(), any(), any())).thenReturn(new SearchArguments<>());
	}

	@Test
	public void shouldAssignPermissionsForAddedInheritance() {
		PermissionInheritanceChange change = new PermissionInheritanceChange(null, "http://parent", true);

		LinkedHashModel addModel = new LinkedHashModel();
		LinkedHashModel removeModel = new LinkedHashModel();

		observer.handleInheritanceChange(INSTANCE_ID, addModel, removeModel, change);

		verifyModel(addModel, new String[] { MANAGER_OF });
		verifyModel(removeModel, new String[0]);
	}

	@Test
	public void shouldAssignPermissionsForAddedInheritanceWithoutManagers() {
		PermissionInheritanceChange change = new PermissionInheritanceChange(null, "http://parent", false);

		LinkedHashModel addModel = new LinkedHashModel();
		LinkedHashModel removeModel = new LinkedHashModel();

		observer.handleInheritanceChange(INSTANCE_ID, addModel, removeModel, change);

		verifyModel(addModel, new String[] { HAS_PERMISSION, MANAGER_OF });
		verifyModel(removeModel, new String[0]);
	}

	@Test
	public void shouldAssignPermissionsForChangeInTheInheritance() {
		String instance = "http://parent";
		PermissionInheritanceChange change = new PermissionInheritanceChange(instance, instance, false);

		LinkedHashModel addModel = new LinkedHashModel();
		LinkedHashModel removeModel = new LinkedHashModel();

		observer.handleInheritanceChange(INSTANCE_ID, addModel, removeModel, change);

		verifyModel(addModel, new String[] { MANAGER_OF, HAS_PERMISSION });
		verifyModel(removeModel, new String[0]);
	}

	@Test
	public void shouldAssignPermissionsForChangeInTheInheritanceByOnlyAddingManagersAndRemovingTheOthers() {
		String instance = "http://parent";

		PermissionInheritanceChange change = new PermissionInheritanceChange(instance, instance, true);

		LinkedHashModel addModel = new LinkedHashModel();
		LinkedHashModel removeModel = new LinkedHashModel();

		observer.handleInheritanceChange(INSTANCE_ID, addModel, removeModel, change);

		verifyModel(addModel, new String[] { MANAGER_OF });
		verifyModel(removeModel, new String[] { HAS_PERMISSION });
	}

	@Test
	public void shouldRemovePermissionsFromTheOldParentAndAssignToTheNewParent() {
		PermissionInheritanceChange change = new PermissionInheritanceChange("http://old", "http://new", false);

		LinkedHashModel addModel = new LinkedHashModel();
		LinkedHashModel removeModel = new LinkedHashModel();

		observer.handleInheritanceChange(INSTANCE_ID, addModel, removeModel, change);

		verifyModel(addModel, new String[] { MANAGER_OF, HAS_PERMISSION });
		verifyModel(removeModel, new String[] { HAS_PERMISSION, MANAGER_OF });
	}

	@Test
	public void shouldRemoveOldPermissionsForChangedInheritanceWithoutTouchingTheManagers() {
		PermissionInheritanceChange change = new PermissionInheritanceChange("http://admin", null, false);

		LinkedHashModel addModel = new LinkedHashModel();
		LinkedHashModel removeModel = new LinkedHashModel();

		observer.handleInheritanceChange(INSTANCE_ID, addModel, removeModel, change);

		verifyModel(addModel, new String[0]);
		verifyModel(removeModel, new String[] { HAS_PERMISSION });
	}

	private static void verifyModel(LinkedHashModel model, String[] keys) {
		assertEquals(model.size(), keys.length);

		int i = 0;
		for (Iterator<?> it = model.iterator(); it.hasNext(); i++) {
			assertTrue(it.next().toString().contains(keys[i]));
		}

	}
}
