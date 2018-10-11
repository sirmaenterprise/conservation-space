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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.search.SearchServiceMock;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test for {@link DataTypeSemanticGenerator}
 *
 * @author BBonev
 */
public class DataTypeSemanticGeneratorTest extends GeneralSemanticTest<DataTypeSemanticGenerator> {

	private final DataTypeSemanticGenerator cut = new DataTypeSemanticGenerator();

	@BeforeClass
	@Override
	public void beforeClass() throws Exception {
		super.beforeClass();
		ReflectionUtils.setFieldValue(cut, "searchService", new SearchServiceMock(context));
		ReflectionUtils.setFieldValue(cut, "namespaceRegistry", new NamespaceRegistryMock(context));
	}

	@Test
	public void testTypeGeneration() throws IOException {
		noTransaction();
		List<FileDescriptor> list = cut.getDefinitions(DataTypeDefinition.class);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		FileDescriptor descriptor = list.get(0);
		String string = IOUtils.toString(descriptor.getInputStream());
		assertNotNull(string);
		assertTrue("Should have at least one type", string.contains("<type>"));
		assertTrue("Resolved uris should be in full format", string.contains("<uri>http"));
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
