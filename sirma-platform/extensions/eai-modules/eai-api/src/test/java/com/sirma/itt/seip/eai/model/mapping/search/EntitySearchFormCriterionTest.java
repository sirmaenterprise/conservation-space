package com.sirma.itt.seip.eai.model.mapping.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EntitySearchFormCriterionTest {

	@Test
	public void testSeal() throws Exception {
		EntitySearchFormCriterion entitySearchOrderCriterion = new EntitySearchFormCriterion();
		assertFalse(entitySearchOrderCriterion.isSealed());
		entitySearchOrderCriterion.setMapping("1");
		entitySearchOrderCriterion.setPropertyId("1");
		entitySearchOrderCriterion.setOperator("1");
		entitySearchOrderCriterion.seal();
		entitySearchOrderCriterion.setMapping("2");
		entitySearchOrderCriterion.setPropertyId("2");
		entitySearchOrderCriterion.setOperator("2");
		assertTrue(entitySearchOrderCriterion.isSealed());
		assertEquals("1", entitySearchOrderCriterion.getMapping());
		assertEquals("1", entitySearchOrderCriterion.getPropertyId());
		assertEquals("1", entitySearchOrderCriterion.getOperator());
	}

}
