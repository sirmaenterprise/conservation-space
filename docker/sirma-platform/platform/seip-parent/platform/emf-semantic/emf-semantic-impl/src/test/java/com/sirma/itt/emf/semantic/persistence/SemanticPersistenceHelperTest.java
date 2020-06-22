package com.sirma.itt.emf.semantic.persistence;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * @author kirq4e
 */
public class SemanticPersistenceHelperTest extends GeneralSemanticTest<SemanticPersistenceHelper> {

	private RepositoryConnection connection;

	@BeforeMethod
	public void init() {
		connection = connectionFactory.produceManagedConnection();
	}

	@AfterMethod
	public void tearDown() {
		try {
			beginTransaction();
			connection.clear(EMF.DATA_CONTEXT);
			commitTransaction();
		} catch (RepositoryException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test
	public void testSaveModel() {
		LinkedHashModel model = createTestModel(false);
		SemanticPersistenceHelper.saveModel(connection, model, EMF.DATA_CONTEXT);
		commitTransaction();

		try {
			RepositoryResult<Statement> statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE, null, true,
					EMF.DATA_CONTEXT);
			Assert.assertTrue(statements.hasNext());
			statements.close();
		} catch (RepositoryException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	private static LinkedHashModel createTestModel(boolean isEmpty) {
		LinkedHashModel model = new LinkedHashModel();
		if (!isEmpty) {
			model.add(EMF.CASE, EMF.IS_ACTIVE, connectionFactory.produceValueFactory().createLiteral(false),
					(Resource) null);
		}
		return model;
	}

	@Test
	public void testSaveEmptyModel() {
		LinkedHashModel model = createTestModel(true);
		SemanticPersistenceHelper.saveModel(connection, model, EMF.DATA_CONTEXT);

		try {
			RepositoryResult<Statement> statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE, null, true,
					EMF.DATA_CONTEXT);
			Assert.assertFalse(statements.hasNext());
			statements.close();
		} catch (RepositoryException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(expectedExceptions = SemanticPersistenceException.class)
	public void testSaveModelBrokenConnection() throws RepositoryException {
		LinkedHashModel model = createTestModel(false);
		RepositoryConnection connectionMock = Mockito.mock(RepositoryConnection.class);
		Mockito.doThrow(RepositoryException.class)
				.when(connectionMock)
				.add(Matchers.any(LinkedHashModel.class), Matchers.any());
		SemanticPersistenceHelper.saveModel(connectionMock, model, EMF.DATA_CONTEXT);
	}

	@Test
	public void testUpdateModel() {
		LinkedHashModel model = createTestModel(false);
		SemanticPersistenceHelper.saveModel(connection, model, EMF.DATA_CONTEXT);

		commitTransaction();
		beginTransaction();

		model = new LinkedHashModel();
		model.add(EMF.CASE, EMF.IS_ACTIVE, connectionFactory.produceValueFactory().createLiteral(true),
				(Resource) null);

		SemanticPersistenceHelper.updateModel(connection, model, EMF.DATA_CONTEXT);
		commitTransaction();

		try {
			RepositoryResult<Statement> statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE,
					connectionFactory.produceValueFactory().createLiteral(true), true, EMF.DATA_CONTEXT);
			Assert.assertTrue(statements.hasNext());
			statements.close();
		} catch (RepositoryException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test
	public void testUpdateEmptyModel() {
		LinkedHashModel model = createTestModel(true);
		SemanticPersistenceHelper.updateModel(connection, model, EMF.DATA_CONTEXT);

		try {
			RepositoryResult<Statement> statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE, null, true,
					EMF.DATA_CONTEXT);
			Assert.assertFalse(statements.hasNext());
			statements.close();
		} catch (RepositoryException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(expectedExceptions = SemanticPersistenceException.class)
	public void testUpdateModelWithBrokenConnection() throws RepositoryException {
		LinkedHashModel model = createTestModel(false);
		RepositoryConnection connectionMock = Mockito.mock(RepositoryConnection.class);
		Mockito.doThrow(RepositoryException.class)
				.when(connectionMock)
				.add(Matchers.any(LinkedHashModel.class), Matchers.any());
		SemanticPersistenceHelper.updateModel(connectionMock, model, EMF.DATA_CONTEXT);
	}

	@Test
	public void testRemoveModel() throws RepositoryException {
		LinkedHashModel model = createTestModel(false);
		SemanticPersistenceHelper.saveModel(connection, model, null);

		commitTransaction();
		RepositoryResult<Statement> statements = null;
		try {
			statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE, null, true, (Resource) null);
			Assert.assertTrue(statements.hasNext());
		} finally {
			if (statements != null) {
				statements.close();
			}
		}

		beginTransaction();

		SemanticPersistenceHelper.removeModel(connection, model);
		commitTransaction();
		try {
			statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE, null, true, (Resource) null);
			Assert.assertFalse(statements.hasNext());
		} finally {
			if (statements != null) {
				statements.close();
			}
		}
	}

	@Test
	public void testRemoveModel_WithDates() throws RepositoryException {
		LinkedHashModel model = new LinkedHashModel();

		TimeZone currentTz = TimeZone.getDefault();
		Date initialDate = new Date();
		model.add(EMF.CASE, EMF.MODIFIED_ON, connectionFactory.produceValueFactory().createLiteral(initialDate),
				(Resource) null);
		SemanticPersistenceHelper.saveModel(connection, model, null);
		commitTransaction();

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		try {
			RepositoryResult<Statement> statements = null;
			try {
				statements = connection.getStatements(EMF.CASE, EMF.MODIFIED_ON, null, true, (Resource) null);
				Assert.assertTrue(statements.hasNext());
			} finally {
				if (statements != null) {
					statements.close();
				}
			}

			model.clear();
			// the date value now is with different time zone
			model.add(EMF.CASE, EMF.MODIFIED_ON, connectionFactory.produceValueFactory().createLiteral(initialDate),
					(Resource) null);

			beginTransaction();
			SemanticPersistenceHelper.removeModel(connection, model);
			commitTransaction();
			try {
				statements = connection.getStatements(EMF.CASE, EMF.MODIFIED_ON, null, true, (Resource) null);
				Assert.assertFalse(statements.hasNext(), "Expected the date to be removed!");
			} finally {
				if (statements != null) {
					statements.close();
				}
			}
		} finally {
			// restore time zone
			TimeZone.setDefault(currentTz);
		}
	}

	@Test
	public void testRemoveModelWithGraph() throws RepositoryException {
		LinkedHashModel model = createTestModel(false);
		SemanticPersistenceHelper.saveModel(connection, model, EMF.DATA_CONTEXT);
		commitTransaction();

		RepositoryResult<Statement> statements = null;
		try {
			statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE, null, true, EMF.DATA_CONTEXT);
			Assert.assertTrue(statements.hasNext());
		} finally {
			if (statements != null) {
				statements.close();
			}
		}
		beginTransaction();
		SemanticPersistenceHelper.removeModel(connection, model, EMF.DATA_CONTEXT);
		commitTransaction();
		try {
			statements = connection.getStatements(EMF.CASE, EMF.IS_ACTIVE, null, true, EMF.DATA_CONTEXT);
			Assert.assertFalse(statements.hasNext());
		} finally {
			if (statements != null) {
				statements.close();
			}
		}
	}

	@Test(expectedExceptions = SemanticPersistenceException.class)
	public void testRemoveModelWithBrokenConnection() throws RepositoryException {
		LinkedHashModel model = createTestModel(false);
		RepositoryConnection connectionMock = Mockito.mock(RepositoryConnection.class);
		Mockito.doThrow(RepositoryException.class)
				.when(connectionMock)
				.remove(Matchers.any(LinkedHashModel.class), Matchers.any());
		SemanticPersistenceHelper.removeModel(connectionMock, model, EMF.DATA_CONTEXT);
	}

	@Test
	public void testCreateStatement() {
		Statement statement = SemanticPersistenceHelper.createStatement("emf:instance", "emf:part", "test",
				new NamespaceRegistryMock(context), connectionFactory.produceValueFactory());
		Assert.assertNotNull(statement);
		Assert.assertEquals(((IRI) statement.getSubject()).getLocalName(), "instance");
		Assert.assertEquals(statement.getPredicate().getLocalName(), "part");
		Assert.assertEquals(statement.getObject().stringValue(), "test");

	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
