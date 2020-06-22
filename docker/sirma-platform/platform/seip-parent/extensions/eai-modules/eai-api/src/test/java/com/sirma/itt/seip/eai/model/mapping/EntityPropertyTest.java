package com.sirma.itt.seip.eai.model.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;

/**
 * Test EntityProperty
 * 
 * @author gshevkedov
 */
public class EntityPropertyTest {

	@Test
	public void testGetDataMapping() {
		EntityProperty entityProperty = new EntityProperty();
		assertNull(entityProperty.getDataMapping());
	}

	@Test
	public void testGetMappingInEmptyMap() {
		EntityProperty entityProperty = new EntityProperty();
		assertNull(entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
	}

	@Test
	public void testGetMapping() {
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.getMappings().put(EntityPropertyMapping.AS_DATA, "criteria");
		assertEquals("criteria", entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
	}

	@Test
	public void testAddMapping() {
		EntityProperty entityProperty = new EntityProperty();
		assertNull(entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
		entityProperty.addMapping(EntityPropertyMapping.AS_DATA, "type");
		assertEquals(1, entityProperty.getMappings().size());
		entityProperty.addMapping(EntityPropertyMapping.AS_DATA, "type");
		assertEquals(1, entityProperty.getMappings().size());
		assertNotNull(entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
	}

	@Test
	public void testAddMappingAll() {
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.addMapping(EntityPropertyMapping.AS_DATA, "data");
		assertEquals(1, entityProperty.getMappings().size());
		assertEquals("data", entityProperty.getDataMapping());
		assertEquals("data", entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
		assertEquals("data", entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
	}

	@Test
	public void testAddMappingSealed() {
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.seal();
		entityProperty.addMapping(EntityPropertyMapping.AS_DATA, "type");
		assertEquals(0, entityProperty.getMappings().size());
	}

	@Test
	public void testEntityProperties() {
		// title
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.setTitle("title1");
		assertEquals("title1", entityProperty.getTitle());
		entityProperty.seal();
		entityProperty.setTitle("title2");
		assertEquals("title1", entityProperty.getTitle());

		// uri
		entityProperty = new EntityProperty();
		entityProperty.setUri("uri1");
		assertEquals("uri1", entityProperty.getUri());
		entityProperty.seal();
		entityProperty.setUri("uri2");
		assertEquals("uri1", entityProperty.getUri());

		// codelist
		Integer cl = Integer.valueOf(1);
		entityProperty = new EntityProperty();
		entityProperty.setCodelist(cl);
		assertEquals(cl, entityProperty.getCodelist());
		entityProperty.seal();
		entityProperty.setCodelist(Integer.valueOf(2));
		assertEquals(cl, entityProperty.getCodelist());

		// mandatory
		entityProperty = new EntityProperty();
		entityProperty.setMandatory(true);
		assertTrue(entityProperty.isMandatory());
		entityProperty.seal();
		entityProperty.setMandatory(false);
		assertTrue(entityProperty.isMandatory());

		// type
		entityProperty = new EntityProperty();
		entityProperty.setType("type1");
		assertEquals("type1", entityProperty.getType());
		entityProperty.seal();
		entityProperty.setType("type2");
		assertEquals("type1", entityProperty.getType());

		// propertyId
		entityProperty = new EntityProperty();
		entityProperty.setPropertyId("p1");
		assertEquals("p1", entityProperty.getPropertyId());
		entityProperty.seal();
		entityProperty.setPropertyId("p2");
		assertEquals("p1", entityProperty.getPropertyId());

	}

	@Test
	public void testEqualsHashCode() {
		EntityProperty entityProperty1 = new EntityProperty();
		entityProperty1.setUri("uri");

		EntityProperty entityProperty2 = new EntityProperty();
		assertNotEquals(entityProperty1, entityProperty2);

		entityProperty2.setUri("uri");
		assertEquals(entityProperty1, entityProperty2);

		entityProperty1.addMapping(EntityPropertyMapping.AS_DATA, "d1");
		assertNotEquals(entityProperty1, entityProperty2);

		entityProperty2.addMapping(EntityPropertyMapping.AS_DATA, "d1");
		assertEquals(entityProperty1, entityProperty2);

		assertEquals(entityProperty1.hashCode(), entityProperty2.hashCode());
	}

	@Test
	public void testSeal() {
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.seal();
		assertTrue(entityProperty.isSealed());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSealWithError() {
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.addMapping(EntityPropertyMapping.AS_DATA, "data1");
		entityProperty.seal();
		entityProperty.addMapping(EntityPropertyMapping.AS_DATA, "data2");
		assertEquals("data1", entityProperty.getDataMapping());

		entityProperty.getMappings().put(EntityPropertyMapping.AS_DATA, "data2");
	}

}
