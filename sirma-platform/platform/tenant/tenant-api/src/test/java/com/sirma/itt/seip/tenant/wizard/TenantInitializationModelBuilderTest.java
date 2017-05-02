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
	 * Test the builder's build. The test file should be copied to the models.
	 *
	 * @throws URISyntaxException
	 *             the uri syntax exception
	 */
	@Test
	public void testBuild() throws URISyntaxException {
		TenantInitializationModelBuilder.Builder builder = modelBuilder.getBuilder();
		InputStream stream = new ByteArrayInputStream("testFile".getBytes(StandardCharsets.UTF_8));
		builder.setModel("{'data':[{'id':'step'}]}");
		builder.appendModelFile("stepId_attachment_propertyId", "fileName.zip", stream);

		File file = new File(this.getClass().getResource("/test").toURI());
		Mockito.when(fileProvider.createTempDir(Matchers.anyString())).thenReturn(file);
		Mockito.when(fileProvider.createTempFile(Matchers.anyString(), Matchers.anyString())).thenReturn(file);
		TenantInitializationModel model = builder.build();
		Assert.assertEquals(file, model.get("stepId").getModels().get(0));
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
