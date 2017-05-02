package com.sirmaenterprise.sep.content.idoc.extensions.widgets.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;

/**
 * Test for {@link WidgetHandlersUtil}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class WidgetHandlersUtilTest {

	@Test
	public void setObjectIdsToConfiguration_withSelectedObjectConfiguration() {
		JsonObject json = new JsonObject();
		json.add("selectedObject", new JsonPrimitive("banan"));
		WidgetConfiguration configuration = new WidgetConfiguration(null, json);
		WidgetHandlersUtil.setObjectIdsToConfiguration(Arrays.asList("instance-id"), configuration);
		assertEquals("instance-id", configuration.getSelectedObject());
	}

	@Test
	public void setObjectIdsToConfiguration_withSelectedObjectsConfiguration() {
		JsonObject json = new JsonObject();
		JsonArray array = new JsonArray();
		array.add(new JsonPrimitive("f"));
		array.add(new JsonPrimitive("u"));
		array.add(new JsonPrimitive("n"));
		json.add("selectedObjects", array);
		WidgetConfiguration configuration = new WidgetConfiguration(null, json);
		WidgetHandlersUtil.setObjectIdsToConfiguration(Arrays.asList("instance-id-1", "instance-id-2"), configuration);
		assertEquals(2, configuration.getSelectedObjects().size());
	}

}
