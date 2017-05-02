/*
 *
 */
package com.sirma.itt.seip.script.extensions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.script.extensions.FolderScriptProvider;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for loading custom java scripts
 *
 * @author BBonev
 */
@Test
public class FolderScriptProviderTest {

	@InjectMocks
	FolderScriptProvider provider = new FolderScriptProvider();

	@Spy
	private ConfigurationProperty<File> locationConfig = new ConfigurationPropertyMock<>(new File("test.js"));

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test file loading.
	 *
	 * @throws IOException
	 */
	public void testFileLoading() throws IOException {

		File file = new File("test.js");
		try {
			createTestFile(file);
			Collection<String> scripts = provider.getScripts();
			Assert.assertNotNull(scripts);
			Assert.assertFalse(scripts.isEmpty());
			Assert.assertNotNull(scripts.iterator().next());
			Assert.assertEquals(scripts.iterator().next(), "test");
		} finally {
			file.delete();
		}
	}

	/**
	 * Test file loading.
	 *
	 * @throws IOException
	 */
	public void testFolderLoading() throws IOException {
		File dir = new File(".");
		File file = new File(dir, "test.js");
		try {
			createTestFile(file);

			Collection<String> scripts = provider.getScripts();
			Assert.assertNotNull(scripts);
			Assert.assertFalse(scripts.isEmpty());
			Assert.assertNotNull(scripts.iterator().next());
			Assert.assertEquals(scripts.iterator().next(), "test");
		} finally {
			file.delete();
		}
	}

	/**
	 * Creates the test file.
	 *
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void createTestFile(File file) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			IOUtils.write("test", writer);
		}
	}
}
