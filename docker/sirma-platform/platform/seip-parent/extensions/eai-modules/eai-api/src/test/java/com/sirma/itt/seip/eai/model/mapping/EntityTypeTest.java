package com.sirma.itt.seip.eai.model.mapping;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;

import org.junit.Test;

/**
 * Tests {@link EntityType}
 * 
 * @author bbanchev
 */
public class EntityTypeTest {

	private static final String SERIALIZED_TYPE = "{\"identifier\":\"id\",\"uri\":\"uri\",\"title\":\"titleval\",\"mappings\":[],\"properties\":[{\"uri\":\"propuri\",\"title\":\"proptitle\",\"propertyId\":null,\"type\":\"an\",\"codelist\":null,\"mandatory\":false,\"externals\":{}}],\"relations\":[]}";

	@Test
	public void testTypeProperties() {
		// title
		EntityType entityType = new EntityType();
		entityType.setTitle("title1");
		assertEquals("title1", entityType.getTitle());
		entityType.seal();
		entityType.setTitle("title2");
		assertEquals("title1", entityType.getTitle());

		// uri
		entityType = new EntityType();
		entityType.setUri("uri1");
		assertEquals("uri1", entityType.getUri());
		entityType.seal();
		entityType.setUri("uri2");
		assertEquals("uri1", entityType.getUri());

		// identifier
		entityType = new EntityType();
		entityType.setIdentifier("id1");
		assertEquals("id1", entityType.getIdentifier());
		entityType.seal();
		entityType.setIdentifier("id2");
		assertEquals("id1", entityType.getIdentifier());

		// mapping
		entityType = new EntityType();
		entityType.setMapping("mapped1");
		assertEquals("mapped1", entityType.getMappings().iterator().next());
		entityType.seal();
		entityType.setMapping("mapped2");
		assertEquals("mapped1", entityType.getMappings().iterator().next());

		// properties
		entityType = new EntityType();
		LinkedList<EntityProperty> newProperties = new LinkedList<>();
		EntityProperty prop1 = new EntityProperty();
		prop1.setUri("u1");
		newProperties.add(prop1);
		entityType.addProperties(newProperties);
		assertEquals(1, entityType.getProperties().size());
		entityType.seal();
		EntityProperty prop2 = new EntityProperty();
		prop2.setUri("u2");
		newProperties.add(prop2);
		entityType.addProperties(newProperties);
		assertEquals(1, entityType.getProperties().size());
		assertEquals(prop1, entityType.getProperties().get(0));
		assertTrue(entityType.getProperties().get(0).isSealed());

		// relataions
		entityType = new EntityType();
		LinkedList<EntityRelation> newRelations = new LinkedList<>();
		EntityRelation r1 = new EntityRelation();
		r1.setUri("u1");
		newRelations.add(r1);
		entityType.addRelations(newRelations);
		assertEquals(1, entityType.getRelations().size());
		entityType.seal();
		EntityRelation r2 = new EntityRelation();
		r2.setUri("u2");
		newRelations.add(r2);
		entityType.addRelations(newRelations);
		assertEquals(1, entityType.getRelations().size());
		assertEquals(r1, entityType.getRelations().get(0));
		assertTrue(entityType.getRelations().get(0).isSealed());

		entityType.addRelation(r2);
		assertEquals(1, entityType.getRelations().size());
		assertEquals(r1, entityType.getRelations().get(0));
		assertTrue(entityType.getRelations().get(0).isSealed());

	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSealWithErrorProperties() throws Exception {
		EntityType entityType = new EntityType();
		LinkedList<EntityProperty> newProperties = new LinkedList<>();
		EntityProperty prop1 = new EntityProperty();
		prop1.setUri("u1");
		newProperties.add(prop1);
		entityType.addProperties(newProperties);
		entityType.seal();

		EntityProperty prop2 = new EntityProperty();
		prop2.setUri("u2");

		entityType.getProperties().add(prop2);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSealWithErrorRelation() throws Exception {
		EntityType entityType = new EntityType();
		LinkedList<EntityRelation> newRelations = new LinkedList<>();
		EntityRelation r1 = new EntityRelation();
		r1.setUri("u1");
		newRelations.add(r1);
		entityType.addRelations(newRelations);
		assertEquals(1, entityType.getRelations().size());
		entityType.seal();

		EntityRelation r2 = new EntityRelation();
		r2.setUri("u1");
		entityType.getRelations().add(r2);
	}

	@Test
	public void testEqualsHashCode() {
		EntityType entityType1 = new EntityType();
		entityType1.setUri("uri");
		entityType1.setIdentifier("id");

		EntityType entityType2 = new EntityType();
		entityType2.setUri("uri");
		assertNotEquals(entityType1, entityType2);

		entityType2.setIdentifier("id");
		assertEquals(entityType1, entityType2);

		assertEquals(entityType1.hashCode(), entityType2.hashCode());

		entityType2.setTitle("t2");
		assertEquals(entityType1.hashCode(), entityType2.hashCode());
	}

	@Test
	public void testSerialziation() throws Exception {
		EntityType entityType1 = new EntityType();
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.setTitle("proptitle");
		entityProperty.setType("an");
		entityProperty.setUri("propuri");
		entityType1.addProperties(Collections.singletonList(entityProperty));
		entityType1.setUri("uri");
		entityType1.setTitle("titleval");
		entityType1.setIdentifier("id");
		String serialized = MappingUtil.writeValue(entityType1);
		assertJsonEquals(SERIALIZED_TYPE, serialized);
	}

	@Test
	public void testDeserialziation() throws Exception {
		String val = SERIALIZED_TYPE;
		EntityType serialized = MappingUtil.readValue(val, EntityType.class);
		assertEquals("id", serialized.getIdentifier());
		assertEquals("uri", serialized.getUri());
		assertEquals("titleval", serialized.getTitle());
		assertEquals(1, serialized.getProperties().size());
	}

}
