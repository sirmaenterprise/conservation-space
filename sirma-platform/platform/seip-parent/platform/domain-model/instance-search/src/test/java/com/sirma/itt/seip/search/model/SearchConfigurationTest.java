package com.sirma.itt.seip.search.model;

import java.io.StringWriter;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.javacrumbs.jsonunit.JsonAssert;

public class SearchConfigurationTest {
	@Test
	public void testSerialize() throws Exception {
		SearchConfiguration config = new SearchConfiguration();
		SearchOrder searchOrder = new SearchOrder();
		searchOrder.setDefaultOrder("test1");
		searchOrder.addSortingField("test1", "test1 label");
		searchOrder.addSortingField("test2", "test2 label");
		StringWriter w = new StringWriter();
		config.setOrder(searchOrder);
		new ObjectMapper().writeValue(w, config);
		JsonAssert.assertJsonEquals(
				"{\"order\":{\"default\":\"test1\",\"properties\":[{\"id\":\"test1\",\"text\":\"test1 label\"},{\"id\":\"test2\",\"text\":\"test2 label\"}]}}",
				w.toString());
	}

}
