package com.sirma.itt.seip.eai.model.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EntityRelationTest {
	@Test
	public void testRelationProperties() {
		// title
		EntityRelation entityRelation = new EntityRelation();
		entityRelation.setTitle("title1");
		assertEquals("title1", entityRelation.getTitle());
		entityRelation.seal();
		entityRelation.setTitle("title2");
		assertEquals("title1", entityRelation.getTitle());

		// uri
		entityRelation = new EntityRelation();
		entityRelation.setUri("uri1");
		assertEquals("uri1", entityRelation.getUri());
		entityRelation.seal();
		entityRelation.setUri("uri2");
		assertEquals("uri1", entityRelation.getUri());

		// domain
		entityRelation = new EntityRelation();
		entityRelation.setDomain("id1");
		assertEquals("id1", entityRelation.getDomain());
		entityRelation.seal();
		entityRelation.setDomain("id2");
		assertEquals("id1", entityRelation.getDomain());

		// range
		entityRelation = new EntityRelation();
		entityRelation.setRange("id1");
		assertEquals("id1", entityRelation.getRange());
		entityRelation.seal();
		entityRelation.setRange("id2");
		assertEquals("id1", entityRelation.getRange());

		// mappings
		entityRelation = new EntityRelation();
		entityRelation.addMappings("test1", "tEST2");
		assertEquals(2, entityRelation.getMappings().size());
		assertEquals("test1", entityRelation.getMappings().get(0));
		assertTrue(entityRelation.hasMapping("test1"));
		assertTrue(entityRelation.hasMapping("test2"));
		entityRelation.seal();
		entityRelation.addMappings("tEST2");
		assertEquals(2, entityRelation.getMappings().size());

	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSealWithError() throws Exception {
		EntityRelation entityRelation = new EntityRelation();
		entityRelation.addMappings("test1");
		entityRelation.seal();
		entityRelation.getMappings().add("test2");
	}

	@Test
	public void testEqualsHashCode() {
		EntityRelation entityRelation1 = new EntityRelation();
		entityRelation1.setUri("uri");

		EntityRelation entityRelation2 = new EntityRelation();
		entityRelation2.setUri("uri");
		assertEquals(entityRelation1, entityRelation2);

		entityRelation2.addMappings("id1");
		assertNotEquals(entityRelation1, entityRelation2);

		entityRelation1.addMappings("id1");
		assertEquals(entityRelation1, entityRelation2);

		assertEquals(entityRelation1.hashCode(), entityRelation2.hashCode());
	}
}
