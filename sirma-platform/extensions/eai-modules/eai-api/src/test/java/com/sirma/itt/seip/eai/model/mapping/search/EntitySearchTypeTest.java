package com.sirma.itt.seip.eai.model.mapping.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.junit.Test;

import com.sirma.itt.seip.eai.model.mapping.MappingUtil;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * @author bbanchev
 */
public class EntitySearchTypeTest {
	private static final String SERIALIZED_TYPE = "{\"identifier\":\"class:id\",\"uri\":\"uri\",\"title\":\"titleval\",\"type\":\"class\"}";

	@Test
	public void testSeal() throws Exception {
		EntitySearchType type = provideType();
		type.seal();
		assertTrue(type.isSealed());
		type.setTitle("new title");
		assertEquals("titleval", type.getTitle());
	}

	@Test
	public void testSerialziation() throws Exception {
		EntitySearchType type = provideType();
		String serialized = MappingUtil.writeValue(type);
		JsonAssert.assertJsonEquals(SERIALIZED_TYPE, serialized);
	}

	private EntitySearchType provideType() {
		EntitySearchType type = new EntitySearchType();
		type.setUri("uri");
		type.setType("class");
		type.setTitle("titleval");
		type.setIdentifier("class:id");
		return type;
	}

	@Test
	public void testDeserialziation() throws Exception {
		String val = SERIALIZED_TYPE;
		EntitySearchType expected = provideType();
		EntitySearchType read = MappingUtil.readValue(val, EntitySearchType.class);
		assertEquals(expected.getIdentifier(), read.getIdentifier());
		assertEquals(expected.getUri(), read.getUri());
		assertEquals(expected.getTitle(), read.getTitle());
		assertEquals(expected.getType(), read.getType());
	}

	@Test
	public void testEquals() throws Exception {
		EntitySearchType type1 = provideType();
		EntitySearchType type2 = provideType();
		assertTrue(type1.equals(type2));
		assertEquals(type1.hashCode(), type2.hashCode());
		type1.setType("def");
		assertFalse(type1.equals(type2));
	}

}
