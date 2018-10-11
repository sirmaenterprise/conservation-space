package com.sirma.itt.seip.eai.service.model;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test {@link ModelService}
 * 
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelServiceTest {
	private static final String SYSTEM_ID = "CMS";

	@Mock
	private EAIConfigurationService integrationService;

	@Mock
	private EAIConfigurationProvider integrationConfigurationProvider;

	@InjectMocks
	private ModelService modelService;

	@Mock
	@ExtensionPoint(value = EAIModelConverter.PLUGIN_ID)
	private Plugins<EAIModelConverter> converters;

	@Test
	public void testGetModelConfiguration() {
		Mockito.when(integrationService.getIntegrationConfiguration(SYSTEM_ID)).thenReturn(
				integrationConfigurationProvider);
		ConfigurationProperty<ModelConfiguration> configProperty = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(integrationService.getIntegrationConfiguration(SYSTEM_ID).getModelConfiguration()).thenReturn(
				configProperty);
		ModelConfiguration modelConfiguration = new ModelConfiguration();
		Mockito
				.when(integrationService.getIntegrationConfiguration(SYSTEM_ID).getModelConfiguration().get())
					.thenReturn(modelConfiguration);
		assertNotNull(modelService.getModelConfiguration(SYSTEM_ID));
	}

	@Test
	public void testProvideModelConverter() {
		EAIModelConverter modelConverter = Mockito.mock(EAIModelConverter.class);
		Optional<EAIModelConverter> optionalValue = Optional.of((EAIModelConverter) modelConverter);
		Mockito.when(converters.get(SYSTEM_ID)).thenReturn(optionalValue);
		assertNotNull(modelService.provideModelConverter(SYSTEM_ID));
	}

	@Test(expected = EAIRuntimeException.class)
	public void testGetModelConfigurationByNamespaceMissing() {
		String namespace = "mynamespace";
		modelService.getModelConfigurationByNamespace(namespace);
	}

	@Test
	public void testGetSearchConfiguration() throws Exception {
		Mockito.when(integrationService.getIntegrationConfiguration(SYSTEM_ID)).thenReturn(
				integrationConfigurationProvider);
		SearchModelConfiguration searchConfig = new SearchModelConfiguration();
		ConfigurationProperty<SearchModelConfiguration> configProperty = new ConfigurationPropertyMock<>(searchConfig);
		Mockito.when(integrationService.getIntegrationConfiguration(SYSTEM_ID).getSearchConfiguration()).thenReturn(
				configProperty);
		SearchModelConfiguration loaded = modelService.getSearchConfiguration(SYSTEM_ID);
		assertEquals(searchConfig, loaded);
	}

}
