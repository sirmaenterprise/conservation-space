package com.sirma.itt.seip.definition.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Test for {@link DefinitionModelRestService}.
 *
 * @author A. Kunchev
 */
public class DefinitionRestServiceTest {

	@InjectMocks
	private DefinitionRestService service;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void setup() {
		service = new DefinitionRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = BadRequestException.class)
	public void getDefinitionModels_noIds() {
		service.getDefinitionModels(Collections.emptyList());
	}

	@Test
	public void getDefinitionModels_buildMap() {
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("modelId-1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("modelId-2");

		when(definitionService.find("modelId-1")).thenReturn(model1);
		when(definitionService.find("modleId-2")).thenReturn(null);

		Map<String, DefinitionModelObject> result = service
				.getDefinitionModels(Arrays.asList("modelId-1", "modelId-2"));

		assertEquals(1, result.size());
		assertTrue(result.containsKey("modelId-1"));
		assertEquals(model1, result.get("modelId-1").getDefinitionModel());
	}
}
