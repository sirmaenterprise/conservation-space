package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PropertyInstance}.
 *
 * @author A. Kunchev
 */
public class PropertyInstanceTest {

	private PropertyInstance instance;

	@Before
	public void setup() {
		instance = new PropertyInstance();
	}

	@Test
	public void getInverseRelation_null() {
		assertNull(instance.getInverseRelation());
	}

	@Test
	public void getInverseRelation() {
		instance.add("inverseRelation", "invRel");
		assertEquals("invRel", instance.getInverseRelation());
	}

	@Test
	public void getLabel() {
		instance.setId("property");
		instance.setLabel("en", "Property");
		assertEquals("Property", instance.getLabelProvider().apply("en"));
	}

	@Test
	public void getDefaultLabel() {
		instance.setId("property");
		assertEquals("property", instance.getLabelProvider().apply("en"));
	}
}
