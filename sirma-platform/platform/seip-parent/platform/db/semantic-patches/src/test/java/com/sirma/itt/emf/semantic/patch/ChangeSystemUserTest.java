package com.sirma.itt.emf.semantic.patch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import liquibase.exception.CustomChangeException;

/**
 * Tests for {@link ChangeSystemUser}.
 *
 * @author smustafov
 */
public class ChangeSystemUserTest {

	private static final String TENANT_ID = "cia.test";
	private static final String CURRENT_USER_NAME = "system";

	/**
	 * Selects all instances with their emf:createdBy and emf:modifiedBy.
	 */
	private static final String ALL_INSTANCES_CREATORS_SPARQL_QUERY = "SELECT ?createdBy ?modifiedBy WHERE { {"
			+ "emf:System-cia.test emf:createdBy ?createdBy . emf:System-cia.test emf:modifiedBy ?modifiedBy } "
			+ "UNION { emf:docTemplate emf:createdBy ?createdBy . emf:docTemplate emf:modifiedBy ?modifiedBy }"
			+ "UNION { emf:imageTemplate emf:createdBy ?createdBy . emf:docTemplate emf:modifiedBy ?modifiedBy }"
			+ "UNION { emf:userTemplate emf:createdBy ?createdBy . emf:docTemplate emf:modifiedBy ?modifiedBy }"
			+ "UNION { emf:videoTemplate emf:createdBy ?createdBy . emf:docTemplate emf:modifiedBy ?modifiedBy } }";

	private static final String GET_ROLES_SPARQL_QUERY = "SELECT ?role WHERE { emf:System-cia.test sec:assignedTo ?role }";
	private static final String GET_ROLES_COUNT_SPARQL_QUERY = "SELECT (count(?role) as ?roleCount) WHERE { emf:system-cia.test sec:assignedTo ?role }";

	@InjectMocks
	private ChangeSystemUser patch;

	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private ConnectionFactory connectionFactory;

	private static final Set<String> EXPECTED_ROLES = new HashSet<>();

	static {
		EXPECTED_ROLES.add(EMF.NAMESPACE + "docTemplate_SecurityRoleTypes-Manager");
		EXPECTED_ROLES.add(EMF.NAMESPACE + "caseTemplate_SecurityRoleTypes-Read");
		EXPECTED_ROLES.add(EMF.NAMESPACE + "projectTemplate_SecurityRoleTypes-Read-Write");
		EXPECTED_ROLES.add(EMF.NAMESPACE + "imageTemplate_SecurityRoleTypes-Manager");
	}

	@Before
	public void beforeEach() throws Exception {
		initMocks(this);

		mockSemanticRepository();

		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);
		when(securityConfiguration.getSystemUserName()).thenReturn(new ConfigurationPropertyMock<>(CURRENT_USER_NAME));
	}

	@Test
	public void should_DoNothing_When_ConfigIsSetToCorrectUserName() throws Exception {
		when(securityConfiguration.getSystemUserName())
				.thenReturn(new ConfigurationPropertyMock<>(SecurityContext.SYSTEM_USER_NAME));

		patch.execute(null);

		verify(connectionFactory, never()).produceConnection();
	}

	@Test
	public void should_Rollback_When_SemanticThrowsException() throws Exception {
		RepositoryConnection connection = mock(RepositoryConnection.class);
		when(connectionFactory.produceConnection()).thenReturn(connection);
		doThrow(RepositoryException.class).when(connection).prepareUpdate(any(), any());

		try {
			patch.execute(null);
		} catch (CustomChangeException e) {
			verify(connection).rollback();
		}
	}

	@Test(expected = CustomChangeException.class)
	public void should_ThrowException_When_SemanticThrowsException() throws Exception {
		RepositoryConnection connection = mock(RepositoryConnection.class);
		when(connectionFactory.produceConnection()).thenReturn(connection);
		doThrow(RepositoryException.class).when(connection).prepareUpdate(any(), any());

		patch.execute(null);
	}

	@Test
	public void should_CopyStatements() throws Exception {
		RepositoryConnection connection = connectionFactory.produceConnection();
		initTestData(connection);

		patch.execute(null);

		verifySemantic(connection);
	}

	private static void verifySemantic(RepositoryConnection connection) {
		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
		verify(connection).prepareUpdate(eq(QueryLanguage.SPARQL), queryCaptor.capture());

		assertQuery(queryCaptor.getValue());
		assertCreatorStatements(connection);
		assertPermissionsCopied(connection);
		assertPermissionsRemoved(connection);
	}

	private static void assertQuery(String query) {
		assertTrue(query.contains(EMF.PREFIX + ":" + CURRENT_USER_NAME + "-" + TENANT_ID));
		assertTrue(query.contains(EMF.PREFIX + ":" + SecurityContext.SYSTEM_USER_NAME + "-" + TENANT_ID));
		assertFalse(query.contains("%currentUser%"));
		assertFalse(query.contains("%tenantId%"));
	}

	private static void assertCreatorStatements(RepositoryConnection connection) {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, ALL_INSTANCES_CREATORS_SPARQL_QUERY,
				CollectionUtils.emptyMap(), false);
		String fullUri = EMF.NAMESPACE + SecurityContext.SYSTEM_USER_NAME + "-" + TENANT_ID;
		try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : iterator) {
				Value createdBy = bindingSet.getValue("createdBy");
				Value modifiedBy = bindingSet.getValue("modifiedBy");
				assertEquals(fullUri, createdBy.stringValue());
				assertEquals(fullUri, modifiedBy.stringValue());
			}
		}
	}

	private static void assertPermissionsCopied(RepositoryConnection connection) {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, GET_ROLES_SPARQL_QUERY,
				CollectionUtils.emptyMap(), false);
		try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : iterator) {
				Value role = bindingSet.getValue("role");
				assertTrue(EXPECTED_ROLES.contains(role.stringValue()));
			}
		}
	}

	private static void assertPermissionsRemoved(RepositoryConnection connection) {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, GET_ROLES_COUNT_SPARQL_QUERY,
				CollectionUtils.emptyMap(), false);
		try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			Value roleCount = iterator.next().getValue("roleCount");
			assertEquals("0", roleCount.stringValue());
		}
	}

	private void mockSemanticRepository() {
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection connection = spy(repository.getConnection());
		when(connectionFactory.produceConnection()).thenReturn(connection);
	}

	private void initTestData(RepositoryConnection connection) throws Exception {
		try (InputStream inputStream = getClass().getResourceAsStream("/testData/systemUserTestData.trig")) {
			connection.begin();
			connection.add(inputStream, "http://example.org#", RDFFormat.TRIG);
			connection.commit();
		}
	}

}
