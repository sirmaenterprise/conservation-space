package com.sirma.sep.content.idoc.nodes.widgets.process;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidgetConfiguration;

/**
 * Test class for {@link ProcessWidgetConfiguration}.
 *
 * @author hlungov
 */
public class ProcessWidgetConfigurationTest {

	private ProcessWidgetConfiguration processWidgetConfiguration;

	@Test
	public void should_have_empty_config_json() {
		processWidgetConfiguration = new ProcessWidgetConfiguration(null, new JsonObject());
		Assert.assertEquals("{}",processWidgetConfiguration.getConfiguration().toString());
	}

	@Test
	public void should_have_empty_config_base64() {
		processWidgetConfiguration = new ProcessWidgetConfiguration(null, "e30=");
		Assert.assertEquals("{}",processWidgetConfiguration.getConfiguration().toString());
	}
}
