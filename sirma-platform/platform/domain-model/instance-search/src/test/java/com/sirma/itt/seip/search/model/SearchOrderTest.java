package com.sirma.itt.seip.search.model;

import static org.testng.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.javacrumbs.jsonunit.JsonAssert;

public class SearchOrderTest {
	private static String SERIALIZED = "{\"default\":\"id1\",\"properties\":[{\"id\":\"id1\",\"text\":\"label1\"},{\"id\":\"id2\",\"text\":\"label2\"}]}";

	@Test
	public void testSerialize() throws Exception {
		SearchOrder searchOrder = new SearchOrder();
		searchOrder.addSortingField("id1", "label1");
		searchOrder.addSortingField("id2", "label2");
		searchOrder.setDefaultOrder("id1");
		StringWriter writer = new StringWriter();
		new ObjectMapper().writeValue(writer, searchOrder);
		JsonAssert.assertJsonEquals(SERIALIZED, writer.toString());
	}

	@Test
	public void testDeserialize() throws Exception {
		SearchOrder searchOrder = new ObjectMapper().readValue(SERIALIZED, SearchOrder.class);
		assertEquals("id1", searchOrder.getDefaultOrder());
		assertEquals(2, searchOrder.getSortingFields().size());
		assertEquals("id1", searchOrder.getSortingFields().get(0).get("id"));
		assertEquals("label1", searchOrder.getSortingFields().get(0).get("text"));
	}

}
