package com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableConfiguration;

/**
 * Test for {@link AggregatedTableConfiguration}.
 *
 * @author A. Kunchev
 */
public class AggregatedTableConfigurationTest {

	private AggregatedTableConfiguration configuration;

	@Before
	public void setup() {
		configuration = new AggregatedTableConfiguration(null, new JsonObject());
	}

	@Test
	public void getGroupBy_noProperty() {
		JsonObject groupBy = configuration.getGroupBy();
		assertNull(groupBy);
	}

	@Test
	public void getGroupBy_withProperty() {
		configuration.addNotNullProperty("groupBy", new JsonObject());
		JsonObject groupBy = configuration.getGroupBy();
		assertNotNull(groupBy);
	}

}
