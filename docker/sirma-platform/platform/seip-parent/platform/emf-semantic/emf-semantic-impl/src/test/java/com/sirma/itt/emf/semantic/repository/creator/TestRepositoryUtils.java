package com.sirma.itt.emf.semantic.repository.creator;

import java.util.List;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Assert;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.seip.io.ResourceSource;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * @author kirq4e
 */
public class TestRepositoryUtils extends GeneralSemanticTest<RepositoryUtils> {

	private static final String TEST_DATA_FOLDER = TEST_DATA_REPOSITORY + "RepositoryUtils/";

	public static final String TEST_TRIG_FILE = TEST_DATA_FOLDER + "testFile.trig";
	public static final String TEST_RDF_FILE = TEST_DATA_FOLDER + "testFile.ttl";
	public static final String TEST_TRIG_MULTIPLE_GRAPHS_FILE = TEST_DATA_FOLDER + "testMultipleGraphsFile.trig";

	public static final String TEST_TRIG_MULTIPLE_GRAPHS_INITIAL_ONTOLOGY_FILE = TEST_DATA_FOLDER
			+ "testMultipleGraphsInitialOntology.ttl";

	public static final String TEST_TRIG_MULTIPLE_GRAPHS_ONTOLOGY_FILE = TEST_DATA_FOLDER
			+ "testMultipleGraphsOntologyFile.trig";

	public static final String TEST_TRIG_ONTOLOGY_FILE = TEST_DATA_FOLDER + "testOntology.trig";
	public static final String TEST_TRIG_REMOVE_GRAPH_FILE = TEST_DATA_FOLDER + "testRemoveGraph.trig";
	public static final String TEST_TRIG_REMOVE_ALL_PREDICATE_GRAPH_FILE = TEST_DATA_FOLDER
			+ "testRemoveAllPredicateGraph.trig";
	public static final String TEST_TRIG_REMOVE_ONTOLOGY_FILE = TEST_DATA_FOLDER + "testRemoveOntology.trig";

	public static final String TEST_TRIG_REMOVE_INSERT_GRAPH_FILE = TEST_DATA_FOLDER + "testRemoveInsertGraph.trig";
	public static final String TEST_TRIG_INSERT_EXISTING_STATEMENT_FILE = TEST_DATA_FOLDER + "testInsertExistingStatement.trig";

	public static final IRI TEST_GRAPH = SimpleValueFactory.getInstance().createIRI(DEFAULT_NAMESPACE);

	@BeforeMethod
	public void init() {
		RepositoryConnection connection = null;
		try {
			connection = getConnectionFactory().produceManagedConnection();

			connection.clear();
		} finally {
			commitTransaction();
		}
	}

	@Test
	public void testAddTrigFileToRepository() {
		Model model = loadFileToRepository(TEST_TRIG_FILE);

		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {
			Assert.assertTrue(readOnlyConnection.getContextIDs().next().equals(TEST_GRAPH));

			Model graph = RepositoryUtils.loadGraph(readOnlyConnection, TEST_GRAPH);

			for (Statement statement : model) {
				Assert.assertTrue(
						graph.contains(statement.getSubject(), statement.getPredicate(), statement.getObject()));
			}
		}
	}

	@Test
	public void testAddRdfFileToRepository() {
		Model model = loadFileToRepository(TEST_RDF_FILE);

		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {
			Model graph = RepositoryUtils.loadGraph(readOnlyConnection, EMF.DATA_CONTEXT);

			for (Statement statement : model) {
				Assert.assertTrue(
						graph.contains(statement.getSubject(), statement.getPredicate(), statement.getObject()));
			}
		}
	}

	@Test
	public void testAddTrigMultipleGraphFileToRepository() {
		Model model = loadFileToRepository(TEST_TRIG_MULTIPLE_GRAPHS_FILE);

		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {

			List<Resource> contexts = Iterations.asList(readOnlyConnection.getContextIDs());
			Assert.assertEquals(3, contexts.size());

			for (Statement statement : model) {
				Assert.assertTrue(readOnlyConnection
						.getStatements(statement.getSubject(), statement.getPredicate(), statement.getObject(),
								statement.getContext())
							.hasNext());
			}
		}
	}

	@Test
	public void testAddTrigMultipleGraphOntologyFileToRepository() {
		Model model;
		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {
			// initial load of ontology
			model = loadFileToRepository(TEST_TRIG_MULTIPLE_GRAPHS_INITIAL_ONTOLOGY_FILE);

			Assert.assertEquals(1, Iterations.asList(readOnlyConnection.getContextIDs()).size());

			model = loadFileToRepository(TEST_TRIG_MULTIPLE_GRAPHS_ONTOLOGY_FILE);

			List<Resource> contexts = Iterations.asList(readOnlyConnection.getContextIDs());
			Assert.assertEquals(4, contexts.size());

			for (Statement statement : model) {
				Assert.assertTrue(readOnlyConnection
						.getStatements(statement.getSubject(), statement.getPredicate(), statement.getObject(),
								statement.getContext())
							.hasNext());
			}

			// check if the missing statement is added
			Assert.assertFalse(readOnlyConnection.getStatements(null, OWL.IMPORTS, null).hasNext());
		}
	}

	@Test
	public void testAddTrigOntologyFileToRepository() {
		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {

			loadFileToRepository(TEST_TRIG_ONTOLOGY_FILE);

			List<Resource> contexts = Iterations.asList(readOnlyConnection.getContextIDs());
			Assert.assertEquals(1, contexts.size());
			Assert.assertEquals(EMF.NAMESPACE.substring(0, EMF.NAMESPACE.length() - 1), contexts.get(0).stringValue());
		}
	}

	@Test
	public void testRemoveStatementsFromRepository() {
		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {

			loadFileToRepository(TEST_TRIG_ONTOLOGY_FILE);

			loadFileToRepository(TEST_TRIG_REMOVE_GRAPH_FILE);

			List<Resource> contexts = Iterations.asList(readOnlyConnection.getContextIDs());
			Assert.assertEquals(1, contexts.size());
			Assert.assertEquals(EMF.NAMESPACE.substring(0, EMF.NAMESPACE.length() - 1), contexts.get(0).stringValue());

			Assert.assertFalse(readOnlyConnection.getStatements(null, OWL.IMPORTS, null).hasNext());

		}
	}

	@Test
	public void testRemoveAllPredicateFromRepository() {
		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {

			loadFileToRepository(TEST_TRIG_ONTOLOGY_FILE);

			loadFileToRepository(TEST_TRIG_REMOVE_ALL_PREDICATE_GRAPH_FILE);

			// deleted all statements in the context so the context will be missing
			List<Resource> contexts = Iterations.asList(readOnlyConnection.getContextIDs());
			Assert.assertEquals(1, contexts.size());

			Assert.assertFalse(readOnlyConnection
					.getStatements(SimpleValueFactory.getInstance().createIRI(
							EMF.NAMESPACE.substring(0, EMF.NAMESPACE.length() - 1)), null, null)
						.hasNext());
		}
	}

	@Test
	public void testRemoveOntologyFromRepository() {
		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {

			loadFileToRepository(TEST_TRIG_ONTOLOGY_FILE);

			loadFileToRepository(TEST_TRIG_REMOVE_ONTOLOGY_FILE);

			List<Resource> contexts = Iterations.asList(readOnlyConnection.getContextIDs());
			Assert.assertEquals(1, contexts.size());

			Assert.assertFalse(readOnlyConnection
					.getStatements(SimpleValueFactory.getInstance().createIRI(
							EMF.NAMESPACE.substring(0, EMF.NAMESPACE.length() - 1)), null, null)
						.hasNext());
		}
	}

	@Test
	public void testRemoveInsertGraph() {
		try (RepositoryConnection readOnlyConnection = connectionFactory.produceReadOnlyConnection()) {

			loadFileToRepository(TEST_TRIG_ONTOLOGY_FILE);

			loadFileToRepository(TEST_TRIG_REMOVE_INSERT_GRAPH_FILE);

			List<Resource> contexts = Iterations.asList(readOnlyConnection.getContextIDs());
			Assert.assertEquals(1, contexts.size());

			Assert.assertTrue(readOnlyConnection.getStatements(EMF.CASE, EMF.IS_SEARCHABLE, SimpleValueFactory.getInstance().createLiteral(Boolean.TRUE)).hasNext());
		}
	}
	
	@Test
	public void testInsertExistingStatements() {
		noTransaction();
		loadFileToRepository(TEST_TRIG_ONTOLOGY_FILE);

		RepositoryConnection connection = Mockito.spy(connectionFactory.produceConnection());

		connectionFactory = Mockito.spy(connectionFactory);
		Mockito.doReturn(connection).when(connectionFactory).produceConnection();

		loadFileToRepository(TEST_TRIG_INSERT_EXISTING_STATEMENT_FILE);

		Mockito.doCallRealMethod().when(connectionFactory).produceConnection();

		Mockito.verify(connection, Mockito.never()).add(Mockito.anyCollection(), Mockito.any());
	}

	private static String getPathToFile(String fileName) {
		return TestRepositoryUtils.class.getResource(fileName).getFile();
	}

	private Model loadFileToRepository(String fileName) {
		pauseTransaction();
		RepositoryConnection connection = null;
		Model model;
		try {
			connection = getConnectionFactory().produceConnection();

			model = RepositoryUtils.addFileToRepository(connection, new ResourceSource(getPathToFile(fileName)));
		} finally {
			getConnectionFactory().disposeConnection(connection);
			resumeTransaction();
		}
		return model;
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}
}
