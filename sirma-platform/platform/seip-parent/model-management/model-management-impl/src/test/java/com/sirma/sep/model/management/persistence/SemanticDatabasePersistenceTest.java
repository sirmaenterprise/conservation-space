package com.sirma.sep.model.management.persistence;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurations;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurationsImpl;

/**
 * Test for {@link SemanticDatabasePersistence}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/08/2018
 */
public class SemanticDatabasePersistenceTest {
	@InjectMocks
	private SemanticDatabasePersistence databasePersistence;
	@Mock
	private RepositoryConnection repositoryConnection;
	@Spy
	private ValueFactory valueFactory = SimpleValueFactory.getInstance();
	@Mock
	private ModelManagementDeploymentConfigurations deploymentConfigurations;
	private IRI context = SimpleValueFactory.getInstance().createIRI(ModelManagementDeploymentConfigurationsImpl.DEFAULT_SEMANTIC_CONTEXT);

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(deploymentConfigurations.getSemanticContext()).thenReturn(new ConfigurationPropertyMock<>(context));
	}

	@Test
	public void saveChanges_ShouldNotFailIfDidntFindOldStatements() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		oldStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE")));

		RepositoryResult<Statement> mockResult = mockEmptyResult();
		when(repositoryConnection.getStatements(EMF.CASE, EMF.STATUS, null)).thenReturn(mockResult);
		databasePersistence.saveChanges(new LinkedList<>(), oldStatements);
	}

	@Test
	public void saveChanges_ShouldPersistChangesIfTheDatabaseStateMatches() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		oldStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE")));
		List<Statement> newStatements = new LinkedList<>();
		newStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("INACTIVE")));

		RepositoryResult<Statement> mockResult = mockResult(
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE")));
		when(repositoryConnection.getStatements(EMF.CASE, EMF.STATUS, null)).thenReturn(mockResult);
		databasePersistence.saveChanges(newStatements, oldStatements);

		verify(repositoryConnection).remove(oldStatements.get(0));
		verify(repositoryConnection).add(newStatements, context);
	}

	@Test
	public void saveChanges_shouldRemoveAllNonLanguageStringLiterals() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		oldStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE")));
		List<Statement> newStatements = new LinkedList<>();
		newStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("INACTIVE")));

		List<Statement> databaseState = Arrays.asList(
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE")),
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("DRAFT")));
		RepositoryResult<Statement> mockResult = mockResult(databaseState.toArray(new Statement[2]));
		when(repositoryConnection.getStatements(EMF.CASE, EMF.STATUS, null)).thenReturn(mockResult);
		databasePersistence.saveChanges(newStatements, oldStatements);

		ArgumentCaptor<Statement> removedStatements = ArgumentCaptor.forClass(Statement.class);
		verify(repositoryConnection, times(2)).remove(removedStatements.capture());
		verify(repositoryConnection).add(newStatements, context);
		Assert.assertEquals(databaseState, removedStatements.getAllValues());
	}

	@Test
	public void saveChanges_shouldRemoveSameLanguageLiteralIfConflictDetected() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		oldStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("Active", "en")));
		List<Statement> newStatements = new LinkedList<>();
		newStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("Inactive", "en")));

		List<Statement> databaseState = Arrays.asList(
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE", "en")),
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("DRAFT", "de")));
		RepositoryResult<Statement> mockResult = mockResult(databaseState.toArray(new Statement[2]));
		when(repositoryConnection.getStatements(EMF.CASE, EMF.STATUS, null)).thenReturn(mockResult);
		databasePersistence.saveChanges(newStatements, oldStatements);

		verify(repositoryConnection).remove(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE", "en")));
		verify(repositoryConnection).add(newStatements, context);
	}

	@Test
	public void saveChanges_shouldOnlyTheSpecifiedNonStringLiteral() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		oldStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(1)));
		List<Statement> newStatements = new LinkedList<>();
		newStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(2)));

		List<Statement> databaseState = Arrays.asList(
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(1)),
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(3)));
		RepositoryResult<Statement> mockResult = mockResult(databaseState.toArray(new Statement[2]));
		when(repositoryConnection.getStatements(EMF.CASE, EMF.STATUS, null)).thenReturn(mockResult);
		databasePersistence.saveChanges(newStatements, oldStatements);

		verify(repositoryConnection).remove(oldStatements.get(0));
		verify(repositoryConnection).add(newStatements, context);
	}

	@Test
	public void saveChanges_shouldRemoveNothingIfCannotDetermineTheExactStatementToRemove() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		oldStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(1)));
		List<Statement> newStatements = new LinkedList<>();
		newStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(2)));

		List<Statement> databaseState = Arrays.asList(
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(4)),
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral(3)));
		RepositoryResult<Statement> mockResult = mockResult(databaseState.toArray(new Statement[2]));
		when(repositoryConnection.getStatements(EMF.CASE, EMF.STATUS, null)).thenReturn(mockResult);
		databasePersistence.saveChanges(newStatements, oldStatements);

		verify(repositoryConnection, never()).remove(any(Statement.class));
		verify(repositoryConnection).add(newStatements, context);
	}

	@Test
	public void saveChanges_ShouldAllowOnlyAddition() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		List<Statement> newStatements = new LinkedList<>();
		newStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("INACTIVE")));

		databasePersistence.saveChanges(newStatements, oldStatements);

		verify(repositoryConnection, never()).remove(oldStatements);
		verify(repositoryConnection).add(newStatements, context);
	}

	@Test
	public void saveChanges_ShouldAllowOnlyRemoval() throws Exception {
		List<Statement> oldStatements = new LinkedList<>();
		oldStatements.add(valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE")));
		List<Statement> newStatements = new LinkedList<>();

		RepositoryResult<Statement> mockResult = mockResult(
				valueFactory.createStatement(EMF.CASE, EMF.STATUS, valueFactory.createLiteral("ACTIVE")));
		when(repositoryConnection.getStatements(EMF.CASE, EMF.STATUS, null)).thenReturn(mockResult);
		databasePersistence.saveChanges(newStatements, oldStatements);

		verify(repositoryConnection).remove(oldStatements.get(0));
		verify(repositoryConnection, never()).add(newStatements, context);
	}

	private RepositoryResult<Statement> mockResult(Statement... statements) {
		Iterator<Statement> statementList = Arrays.asList(statements).iterator();
		return new RepositoryResult<>(new CloseableIteration<Statement, RepositoryException>() {
			@Override
			public void close() throws RepositoryException {

			}

			@Override
			public boolean hasNext() throws RepositoryException {
				return statementList.hasNext();
			}

			@Override
			public Statement next() throws RepositoryException {
				return statementList.next();
			}

			@Override
			public void remove() throws RepositoryException {
				statementList.remove();
			}
		});
	}

	private RepositoryResult<Statement> mockEmptyResult() {
		return new RepositoryResult<>(new CloseableIteration<Statement, RepositoryException>() {
			@Override
			public void close() throws RepositoryException {

			}

			@Override
			public boolean hasNext() throws RepositoryException {
				return false;
			}

			@Override
			public Statement next() throws RepositoryException {
				throw new NoSuchElementException();
			}

			@Override
			public void remove() throws RepositoryException {
				throw new NoSuchElementException();
			}
		});
	}
}
