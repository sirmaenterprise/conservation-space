package com.sirma.itt.seip.definition.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Test for {@link DefinitionModelRestService}.
 *
 * @author A. Kunchev
 */
public class DefinitionModelRestServiceTest {

	@InjectMocks
	private DefinitionModelRestService service;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private MultivaluedMap<String, String> pathParamsMap;

	@Mock
	private MultivaluedMap<String, String> queryParamsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Before
	public void setup() {
		service = new DefinitionModelRestService();
		MockitoAnnotations.initMocks(this);
		when(uriInfo.getQueryParameters()).thenReturn(queryParamsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	// @Test(expected = ResourceException.class)
	// public void getDefinitionModel_nullModel() {
	// when(dictionaryService.find("definitionId")).thenReturn(null);
	// service.getDefinitionModel("definitionId");
	// }
	//
	// @Test
	// public void getDefinitionModel_notNullModel() {
	// DefinitionModel definitionModel = mock(DefinitionModel.class);
	// when(dictionaryService.find("definitionId")).thenReturn(definitionModel);
	//
	// DefinitionModelObject modelObject = service.getDefinitionModel("definitionId");
	// assertEquals(definitionModel, modelObject.getDefinitionModel());
	// }

	@Test(expected = BadRequestException.class)
	public void getDefinitionModels_noIds() {
		when(queryParamsMap.get("id")).thenReturn(Collections.emptyList());
		service.getDefinitionModels(request);
	}

	@Test
	public void getDefinitionModels_buildMap() {
		List<String> ids = Arrays.asList("modelId-1", "modelId-2");
		when(queryParamsMap.get("id")).thenReturn(ids);
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("modelId-1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("modelId-2");

		when(dictionaryService.find("modelId-1")).thenReturn(model1);
		when(dictionaryService.find("modleId-2")).thenReturn(null);

		Map<String, DefinitionModelObject> result = service.getDefinitionModels(request);

		assertEquals(1, result.size());
		assertTrue(result.containsKey("modelId-1"));

		assertEquals(model1, result.get("modelId-1").getDefinitionModel());
	}

}
