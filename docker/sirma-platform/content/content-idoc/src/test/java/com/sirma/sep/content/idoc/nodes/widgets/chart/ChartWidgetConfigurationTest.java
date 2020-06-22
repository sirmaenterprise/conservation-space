package com.sirma.sep.content.idoc.nodes.widgets.chart;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidgetConfiguration;

/**
 * Test class for {@link ChartWidgetConfiguration}
 *
 * @author hlungov
 */
public class ChartWidgetConfigurationTest {

	private ChartWidgetConfiguration chartWidgetConfiguration;

	@Before
	public void setup() {
		chartWidgetConfiguration = new ChartWidgetConfiguration(null, new JsonObject());
	}

	@Test
	public void should_be_null_groupBy() {
		JsonElement groupBy = chartWidgetConfiguration.getGroupBy();
		Assert.assertNotNull(groupBy);
		Assert.assertTrue(groupBy.isJsonNull());
	}

	@Test
	public void should_be_not_null_groupBy() {
		chartWidgetConfiguration.getConfiguration().addProperty(ChartWidgetConfiguration.GROUP_BY_KEY, "test");
		Assert.assertEquals("test",chartWidgetConfiguration.getGroupBy().getAsString());
	}

	@Test
	public void should_create_empty_config() {
		chartWidgetConfiguration = new ChartWidgetConfiguration(null, "e30=");
		Assert.assertEquals("{}",chartWidgetConfiguration.getConfiguration().toString());
	}

}
