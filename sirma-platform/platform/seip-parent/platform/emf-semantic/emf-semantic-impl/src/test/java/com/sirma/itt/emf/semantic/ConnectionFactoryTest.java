package com.sirma.itt.emf.semantic;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;

/**
 * Test for {@link ConnectionFactoryImpl}
 *
 * @author kirq4e
 */
public class ConnectionFactoryTest extends GeneralSemanticTest<ConnectionFactoryImpl> {

	/**
	 * Tests get connection method
	 */
	@Test
	public void testGetConnection() {
		noTransaction();
		RepositoryConnection connection;
		try {
			connection = connectionFactory.produceConnection();

			Assert.assertNotNull(connection);
			Assert.assertTrue(connection.isOpen());

			connectionFactory.disposeConnection(connection);

			Assert.assertFalse(connection.isOpen());

			// Tests get value factory method

			ValueFactory vf = connectionFactory.produceValueFactory();
			Assert.assertNotNull(vf);

			connectionFactory.tearDown();

			Assert.assertFalse(connection.isOpen());

		} catch (RepositoryException e) {
			e.printStackTrace();
			Assert.fail("Exception: " + e.getMessage());
		}
	}

	@Override
	protected String getTestDataFile() {
		// no test data needed so far
		return null;
	}

}
