package com.sirma.itt.seip.eai.rest;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.eai.rest.EAIModelRestService;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;

/**
 * Test {@link EAIModelRestService}
 * 
 * @author gshevkedov
 */
public class EAIModelRestServiceTest {

	@Mock
	private ModelService modelService;

	@InjectMocks
	private EAIModelRestService eiaModelRestService;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = RestServiceException.class)
	public void testProvideModelEx() {
		new EAIModelRestService().provideModel("id");
	}

	@Test
	public void testProvideModel() {
		ModelConfiguration modelConfiguration = new ModelConfiguration();
		String systemId = "id";

		Mockito.when(modelService.getModelConfiguration(systemId.toUpperCase())).thenReturn(modelConfiguration);
		assertNotNull(eiaModelRestService.provideModel(systemId));
	}

}
