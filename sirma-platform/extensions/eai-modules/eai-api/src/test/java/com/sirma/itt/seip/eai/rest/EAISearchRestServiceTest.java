package com.sirma.itt.seip.eai.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchOrderCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchType;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.search.model.SearchConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class EAISearchRestServiceTest {
	private static final String SYSTEM_ID = "CMS";

	@Mock
	private EAIConfigurationService integrationService;

	@Mock
	private ModelService modelService;
	@InjectMocks
	private EAISearchRestService eaiSearchRestService;

	@Test(expected = RestServiceException.class)
	public void testConfigurationMissing() throws Exception {
		eaiSearchRestService.configuration(SYSTEM_ID);
	}

	@Test
	public void testConfiguration() throws Exception {
		SearchModelConfiguration model = new SearchModelConfiguration();
		EntitySearchType type = new EntitySearchType();
		type.setIdentifier("id");
		type.setTitle("title");
		type.setUri("uri");
		type.setType("type");
		buildOrderCriterion(model, type, "2", 2, "an..1");
		buildOrderCriterion(model, type, "1", 1, "an..1");
		model.seal();
		when(integrationService.getAllRegisteredSystems()).thenReturn(Collections.singleton(SYSTEM_ID));
		when(modelService.getSearchConfiguration(eq(SYSTEM_ID))).thenReturn(model);
		Response provideSearchModel = eaiSearchRestService.configuration(SYSTEM_ID);
		assertTrue(provideSearchModel.getEntity() instanceof SearchConfiguration);
		SearchConfiguration config = (SearchConfiguration) provideSearchModel.getEntity();
		assertEquals(2, config.getOrder().getSortingFields().size());
		assertEquals("2", config.getOrder().getDefaultOrder());
		assertEquals("2", config.getOrder().getSortingFields().get(0).get("id"));
		assertEquals("1", config.getOrder().getSortingFields().get(1).get("id"));
	}

	private static EntitySearchOrderCriterion buildOrderCriterion(SearchModelConfiguration config,
			EntitySearchType type, String id, Integer orderPosition, String typeName) {
		EntitySearchOrderCriterion criterion = new EntitySearchOrderCriterion();
		criterion.setPropertyId(id);
		criterion.setOrderPosition(orderPosition);
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.setTitle("title" + id);
		entityProperty.setPropertyId("p" + id);
		entityProperty.setType(typeName);
		config.addCriterion(type, criterion, entityProperty);
		return criterion;
	}

}
