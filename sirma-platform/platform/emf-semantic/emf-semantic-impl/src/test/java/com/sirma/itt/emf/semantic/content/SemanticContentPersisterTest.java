package com.sirma.itt.emf.semantic.content;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link SemanticContentPersister}
 *
 * @author BBonev
 */
public class SemanticContentPersisterTest {

	@InjectMocks
	private SemanticContentPersister contentPersister;

	@Mock
	private TransactionalRepositoryConnection repositoryConnection;
	@Spy
	private InstanceProxyMock<TransactionalRepositoryConnection> connection = new InstanceProxyMock<>();
	@Mock
	private NamespaceRegistryService namespaceRegistryService;
	@Spy
	private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

	@Before
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);
		connection.set(repositoryConnection);

		when(repositoryConnection.prepareUpdate(eq(QueryLanguage.SPARQL), anyString())).thenReturn(mock(Update.class));

		when(namespaceRegistryService.getDataGraph()).thenReturn(EMF.DATA_CONTEXT);
		when(namespaceRegistryService.buildUri(anyString()))
				.then(a -> valueFactory.createURI(a.getArgumentAt(0, String.class)));
	}

	@Test
	public void addPrimaryContent() throws Exception {
		contentPersister.savePrimaryContent("emf:instanceId", "textToSave");

		verify(repositoryConnection).prepareUpdate(eq(QueryLanguage.SPARQL),
				argThat(CustomMatcher.of((String query) -> {
					return query.contains(
							"delete { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#content> ?oldValue. }} insert { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#content> \"textToSave\"^^<http://www.w3.org/2001/XMLSchema#string>. }} where { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { optional { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#content> ?oldValue. }}}");
				})));
	}

	@Test
	public void addPrimaryContent_noContent() throws Exception {
		contentPersister.savePrimaryContent("emf:instanceId", null);
		contentPersister.savePrimaryContent("emf:instanceId", "   ");
		contentPersister.savePrimaryContent("emf:instanceId", "  \n\n  \t  ");

		verify(repositoryConnection, times(3)).prepareUpdate(eq(QueryLanguage.SPARQL),
				argThat(CustomMatcher.of((String query) -> {
					return query.contains(
							"delete { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#content> ?oldValue. }} where { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { optional { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#content> ?oldValue. }}}");
				})));
	}

	@Test(expected = SemanticPersistenceException.class)
	public void addPrimaryContent_failToPersist() throws Exception {
		Update update = mock(Update.class);
		doThrow(UpdateExecutionException.class).when(update).execute();

		reset(repositoryConnection);
		when(repositoryConnection.prepareUpdate(eq(QueryLanguage.SPARQL), anyString())).thenReturn(update);

		contentPersister.savePrimaryContent("emf:instanceId", "textToSave");
	}

	@Test
	public void addPrimaryView() throws Exception {
		contentPersister.savePrimaryView("emf:instanceId", "textToSave");

		verify(repositoryConnection).prepareUpdate(eq(QueryLanguage.SPARQL),
				argThat(CustomMatcher.of((String query) -> {
					return query.contains(
							"delete { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#viewContent> ?oldValue. }} insert { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#viewContent> \"textToSave\"^^<http://www.w3.org/2001/XMLSchema#string>. }} where { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { optional { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#viewContent> ?oldValue. }}}");
				})));

	}

	@Test
	public void saveWidgetsContentTest() throws Exception {
		contentPersister.saveWidgetsContent("emf:instanceId", "widgetTextToSave");

		verify(repositoryConnection).prepareUpdate(eq(QueryLanguage.SPARQL),
				argThat(CustomMatcher.of((String query) -> {
					return query.contains(
							"delete { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#viewWidgetsContent> ?oldValue. }} insert { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#viewWidgetsContent> \"widgetTextToSave\"^^<http://www.w3.org/2001/XMLSchema#string>. }} where { GRAPH<http://ittruse.ittbg.com/data/enterpriseManagementFramework> { optional { <emf:instanceId> <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#viewWidgetsContent> ?oldValue. }}}");
				})));

	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNPEOnNullInstanceId() throws Exception {
		contentPersister.savePrimaryView(null, "textToSave");
	}
}
