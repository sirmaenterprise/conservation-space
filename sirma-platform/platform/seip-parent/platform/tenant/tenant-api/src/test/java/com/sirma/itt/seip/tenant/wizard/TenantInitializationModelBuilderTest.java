package com.sirma.itt.seip.tenant.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Test the tenant initialization model builder.
 *
 * @author nvelkov
 */
public class TenantInitializationModelBuilderTest {

	@Mock
	private TempFileProvider fileProvider;

	@InjectMocks
	private TenantInitializationModelBuilder modelBuilder;

	/**
	 * Init the mocks.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the builder's build with a compressed file. The test file should be unzipped copied to the models.
	 *
	 * @throws URISyntaxException
	 *             the uri syntax exception
	 */
	@Test
	public void should_buildModel_withACompressedFile() throws URISyntaxException {
		File file = new File(this.getClass().getResource("/test").toURI());
		Mockito.when(fileProvider.createTempDir(Matchers.anyString())).thenReturn(file);
		Mockito.when(fileProvider.createTempFile(Matchers.anyString(), Matchers.anyString())).thenReturn(file);

		TenantInitializationModelBuilder.Builder builder = createBuilder("fileName.zip");
		TenantInitializationModel model = builder.build();
		Assert.assertEquals(file, model.get("stepId").getModels().get(0));
		// Ensure that a temp dir has been created for the extracted zip file.
		Mockito.verify(fileProvider, Mockito.times(1)).createTempDir(Matchers.anyString());

	}

	/**
	 * Test the builder's build with a single semantic file. The test file should be copied to the models.
	 *
	 * @throws URISyntaxException
	 *             the uri syntax exception
	 */
	@Test
	public void should_buildModel_withASinglePatchFile() throws URISyntaxException {
		File file = new File(this.getClass().getResource("/test").toURI());
		Mockito.when(fileProvider.createTempFile(Matchers.anyString(), Matchers.anyString())).thenReturn(file);

		TenantInitializationModelBuilder.Builder builder = createBuilder("fileName.ttl");
		TenantInitializationModel model = builder.build();
		Assert.assertEquals(file, model.get("stepId").getModels().get(0));
		// Ensure that no temp dir has been created because there is nothing to extract.
		Mockito.verify(fileProvider, Mockito.never()).createTempDir(Matchers.anyString());
	}

	private TenantInitializationModelBuilder.Builder createBuilder(String fileName) {
		TenantInitializationModelBuilder.Builder builder = modelBuilder.getBuilder();
		InputStream stream = new ByteArrayInputStream("testFile".getBytes(StandardCharsets.UTF_8));
		builder.setModel("{'data':[{'id':'step'}]}");
		builder.appendModelFile("stepId_attachment_propertyId", fileName, stream);
		return builder;
	}

	/**
	 * Test the builder's build with a malformed key.
	 */
	@Test(expected = TenantCreationException.class)
	public void testBuildInvalidKey() {
		TenantInitializationModelBuilder.Builder builder = modelBuilder.getBuilder();
		InputStream stream = new ByteArrayInputStream("testFile".getBytes(StandardCharsets.UTF_8));
		builder.setModel("{'data':[{'id':'step'}]}");
		builder.appendModelFile("stepId_attachment", "fileName.zip", stream);
		builder.build();
	}
}
