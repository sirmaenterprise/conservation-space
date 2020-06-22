package com.sirma.itt.seip.eai.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchFormCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchOrderCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchType;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class EAISearchModelRestServiceTest {
	private static final String SYSTEM_ID = "CMS";
	@Mock
	private ModelService modelService;
	@InjectMocks
	private EAISearchModelRestService eaiSearchModelRestService;

	@Test
	public void testProvideSearchModel() throws Exception {
		SearchModelConfiguration value = new SearchModelConfiguration();
		when(modelService.getSearchConfiguration(eq(SYSTEM_ID))).thenReturn(value);
		SearchModelConfiguration provideSearchModel = eaiSearchModelRestService.provideSearchModel(SYSTEM_ID);
		assertEquals(value, provideSearchModel);
	}

	@Test(expected = RestServiceException.class)
	public void testProvideSearchModelMissing() throws Exception {
		SearchModelConfiguration value = new SearchModelConfiguration();
		when(modelService.getSearchConfiguration(eq(SYSTEM_ID))).thenThrow(new EAIRuntimeException("Missing service"));
		eaiSearchModelRestService.provideSearchModel(SYSTEM_ID);
	}

	@Test
	public void testProvideModelTypes() throws Exception {
		SearchModelConfiguration config = new SearchModelConfiguration();
		EntitySearchType type = new EntitySearchType();
		type.setIdentifier("id");
		type.setTitle("title");
		type.setUri("uri");
		type.setType("type");
		config.addCriterion(type, new EntitySearchFormCriterion(), new EntityProperty());
		config.seal();
		when(modelService.getSearchConfiguration(eq(SYSTEM_ID))).thenReturn(config);
		Response provideSearchModel = eaiSearchModelRestService.provideModelTypes(SYSTEM_ID);
		Object entity = provideSearchModel.getEntity();
		JsonAssert.assertJsonEquals("[{\"id\":\"id\",\"label\":\"title\",\"type\":\"type\"}]", entity.toString());
	}

	@Test(expected = RestServiceException.class)
	public void testProvideModelTypesMissing() throws Exception {
		when(modelService.getSearchConfiguration(eq(SYSTEM_ID))).thenThrow(new EAIRuntimeException("Missing service"));
		eaiSearchModelRestService.provideModelTypes(SYSTEM_ID);
	}

	@Test
	public void testProvideModelProperties() throws Exception {
		SearchModelConfiguration config = new SearchModelConfiguration();
		EntitySearchType type = new EntitySearchType();
		type.setIdentifier("id");
		type.setTitle("title");
		type.setUri("uri");
		type.setType("type");

		buildFormCriterion(config, type, "equals", "1", null, "an..1");
		buildFormCriterion(config, type, "between", "2", null, "dateTime");
		buildFormCriterion(config, type, "equals", "3", 1, "uri");
		buildOrderCriterion(config, type, "1", null, "a..1");
		config.seal();

		when(modelService.getSearchConfiguration(eq(SYSTEM_ID))).thenReturn(config);
		Response provideSearchModel = eaiSearchModelRestService.provideModelProperties(SYSTEM_ID, null);
		Object entity = provideSearchModel.getEntity();
		JsonAssert.assertJsonEquals(
				"[{\"id\":\"1\",\"text\":\"title1\",\"type\":\"text\",\"operators\":[\"equals\"]},{\"id\":\"2\",\"text\":\"title2\",\"type\":\"datetime\",\"operators\":[\"between\"]},{\"id\":\"3\",\"text\":\"title3\",\"type\":\"codeList\",\"operators\":[\"equals\"]}]",
				entity.toString());
	}

	@Test(expected = RestServiceException.class)
	public void testProvideModelPropertiesMissing() throws Exception {
		when(modelService.getSearchConfiguration(eq(SYSTEM_ID))).thenThrow(new EAIRuntimeException("Missing service"));
		eaiSearchModelRestService.provideModelProperties(SYSTEM_ID, null);
	}

	private static EntitySearchFormCriterion buildFormCriterion(SearchModelConfiguration config, EntitySearchType type,
			String operator, String id, Integer codelist, String typeName) {
		EntitySearchFormCriterion criterion = new EntitySearchFormCriterion();
		criterion.setMapping("ext_" + id);
		criterion.setPropertyId(id);
		criterion.setOperator(operator);
		criterion.setVisible(true);
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.setTitle("title" + id);
		entityProperty.setType(typeName);
		entityProperty.setPropertyId("p" + id);
		entityProperty.setCodelist(codelist);
		config.addCriterion(type, criterion, entityProperty);
		return criterion;
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
