package com.sirma.itt.seip.tenant.wizard;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Test the tenant initialization model retriever.
 *
 * @author nvelkov
 */
public class TenantInitializationModelRetrieverTest {

	@Mock
	private ConfigurationProperty<String> manifestPath;

	@InjectMocks
	private TenantInitializationModelRetriever modelRetriever;

	/**
	 * Init the mocks.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the model retrieval from a resource file.
	 */
	@Test
	public void testModelRetrievalResource() {
		Mockito.when(manifestPath.get()).thenReturn("testResource");
		List<TenantInitializationExternalModel> models = modelRetriever.getModels();
		Assert.assertEquals(2, models.size());
		Assert.assertEquals("myModel", models.get(0).getId());
	}

	/**
	 * Test the model retrieval when the manifest file is missing.
	 */
	@Test(expected = TenantCreationException.class)
	public void testModelRetrievalFileNotFound() {
		Mockito.when(manifestPath.get()).thenReturn("doesntExist");
		modelRetriever.getModels();
	}

	/**
	 * Test the model file when the manifest file is malformed.
	 */
	@Test(expected = TenantCreationException.class)
	public void testModelRetrievalMalformedFile() {
		Mockito.when(manifestPath.get()).thenReturn("src/test/resources/test");
		modelRetriever.getModels();
	}
}
