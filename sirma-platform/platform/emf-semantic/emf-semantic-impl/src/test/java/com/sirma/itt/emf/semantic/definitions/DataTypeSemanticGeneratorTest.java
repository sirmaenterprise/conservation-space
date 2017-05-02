package com.sirma.itt.emf.semantic.definitions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBException;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.TransactionalRepositoryConnectionMock;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

/**
 * Test for {@link DataTypeSemanticGenerator}
 *
 * @author BBonev
 */
public class DataTypeSemanticGeneratorTest extends GeneralSemanticTest<DataTypeSemanticGenerator> {

	private final DataTypeSemanticGenerator cut = new DataTypeSemanticGenerator();

	private TransactionalRepositoryConnection transactionalRepositoryConnection;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		transactionalRepositoryConnection = new TransactionalRepositoryConnectionMock(context);

		ReflectionUtils.setField(cut, "connectionProvider", new InstanceProxyMock<>(transactionalRepositoryConnection));

		try {
			transactionalRepositoryConnection.afterBegin();
		} catch (EJBException | RemoteException e) {
			fail("", e);
		}
	}

	@Test
	public void testTypeGeneration() throws IOException {
		List<FileDescriptor> list = cut.getDefinitions(DataTypeDefinition.class);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		FileDescriptor descriptor = list.get(0);
		String string = IOUtils.toString(descriptor.getInputStream());
		assertNotNull(string);
		assertTrue("Should have at least one type", string.contains("<type>"));
	}

	@AfterMethod
	public void commitTransaction() {
		try {
			transactionalRepositoryConnection.beforeCompletion();
			transactionalRepositoryConnection.afterCompletion(true);
		} catch (EJBException | RemoteException e) {
			fail("transaction commit failed", e);
		}
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
