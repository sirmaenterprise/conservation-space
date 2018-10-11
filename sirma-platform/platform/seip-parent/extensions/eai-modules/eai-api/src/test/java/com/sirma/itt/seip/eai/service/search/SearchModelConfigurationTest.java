package com.sirma.itt.seip.eai.service.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchFormCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchOrderCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchType;

@RunWith(MockitoJUnitRunner.class)
public class SearchModelConfigurationTest {
	@InjectMocks
	private SearchModelConfiguration searchModelConfiguration;

	@Test
	public void testaddCriterion() throws Exception {
		EntitySearchFormCriterion title = buildFormModel("title");
		searchModelConfiguration.addCriterion(buildTypeModel("name1"), title, Mockito.mock(EntityProperty.class));
		assertNotNull(searchModelConfiguration.getPropertyByCriteration(title));
		assertNotNull(searchModelConfiguration.getCriterionByInternalName("title"));
		EntitySearchFormCriterion title2 = buildFormModel("title2");
		searchModelConfiguration.addCriterion(buildTypeModel("name1"), title2, Mockito.mock(EntityProperty.class));
		assertNotNull(searchModelConfiguration.getCriterionByInternalName("title2"));
		assertNotNull(searchModelConfiguration.getPropertyByCriteration(title2));
	}

	@Test
	public void testGetFormData() throws Exception {
		searchModelConfiguration.addCriterion(buildTypeModel("name1"), buildFormModel("title"),
				Mockito.mock(EntityProperty.class));
		searchModelConfiguration.addCriterion(buildTypeModel("name1"), buildFormModel("title2"),
				Mockito.mock(EntityProperty.class));
		searchModelConfiguration.addCriterion(buildTypeModel("name2"), buildOrderModel("title"),
				Mockito.mock(EntityProperty.class));
		assertEquals(0, searchModelConfiguration.getFormData().size());
		searchModelConfiguration.seal();
		assertEquals(2, searchModelConfiguration.getFormData().size());
	}

	@Test
	public void testGetOrderData() throws Exception {
		searchModelConfiguration.addCriterion(buildTypeModel("name1"), buildOrderModel("title"),
				Mockito.mock(EntityProperty.class));
		searchModelConfiguration.addCriterion(buildTypeModel("name1"), buildOrderModel("title2"),
				Mockito.mock(EntityProperty.class));
		searchModelConfiguration.addCriterion(buildTypeModel("name2"), buildFormModel("title"),
				Mockito.mock(EntityProperty.class));
		assertEquals(0, searchModelConfiguration.getOrderData().size());
		searchModelConfiguration.seal();
		assertEquals(2, searchModelConfiguration.getOrderData().size());
	}

	@Test
	public void testSeal() throws Exception {
		searchModelConfiguration.seal();
		assertTrue(searchModelConfiguration.isSealed());
	}

	private EntitySearchFormCriterion buildFormModel(String name) {
		EntitySearchFormCriterion formField = new EntitySearchFormCriterion();
		formField.setMapping("ext_" + name);
		formField.setOperator("equals");
		formField.setPropertyId(name);
		return formField;
	}

	private EntitySearchOrderCriterion buildOrderModel(String name) {
		EntitySearchOrderCriterion orderByField = new EntitySearchOrderCriterion();
		orderByField.setOrderPosition(1);
		orderByField.setPropertyId(name);
		return orderByField;
	}

	private EntitySearchType buildTypeModel(String name) {
		EntitySearchType type = new EntitySearchType();
		type.setIdentifier(name);
		type.setUri(name);
		type.setTitle(name);
		type.setMapping("ext_name");
		type.setType("type");
		return type;
	}

}
