package com.sirma.itt.seip.eai.model.mapping.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EntitySearchOrderCriterionTest {

	@Test
	public void testSeal() throws Exception {
		EntitySearchOrderCriterion entitySearchOrderCriterion = new EntitySearchOrderCriterion();
		assertFalse(entitySearchOrderCriterion.isSealed());
		entitySearchOrderCriterion.setOrderPosition(1);
		entitySearchOrderCriterion.setPropertyId("1");
		entitySearchOrderCriterion.seal();
		entitySearchOrderCriterion.setOrderPosition(2);
		entitySearchOrderCriterion.setPropertyId("2");
		assertTrue(entitySearchOrderCriterion.isSealed());
		assertEquals((Integer) 1, entitySearchOrderCriterion.getOrderPosition());
		assertEquals("1", entitySearchOrderCriterion.getPropertyId());
	}

}
