package com.sirma.sep.content.idoc.extensions.widgets.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.sep.content.idoc.WidgetConfiguration;

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

	@Test
	public void setObjectIdsToConfiguration_withSelection() {
		JsonObject json = new JsonObject();
		json.addProperty("selection", "multi");
		WidgetConfiguration configuration = new WidgetConfiguration(null, json);
		WidgetHandlersUtil.setObjectIdsToConfiguration(Arrays.asList("instance-id-1", "instance-id-2"), configuration);
		assertEquals(2, configuration.getSelectedObjects().size());
	}

	@Test
	public void setObjectIdsToConfiguration_withSingleSelection() {
		JsonObject json = new JsonObject();
		json.addProperty("selection", "single");
		WidgetConfiguration configuration = new WidgetConfiguration(null, json);
		WidgetHandlersUtil.setObjectIdsToConfiguration(Arrays.asList("instance-id-2"), configuration);
		assertEquals("instance-id-2", configuration.getSelectedObject());
	}

	@Test
	public void getCollectionFromMap_noCollection() {
		Map<String, Object> map = new HashMap<>();
		map.put("test-key", "single-value");
		Collection<Serializable> collection = WidgetHandlersUtil.getCollectionFromMap("test-key", map);
		assertTrue(collection.isEmpty());
	}

	@Test
	public void getCollectionFromMap_collection_removedDuplications() {
		Map<String, Object> map = new HashMap<>();
		map.put("test-key", Arrays.asList("value-1", "value-2", "value-2"));
		Collection<Serializable> collection = WidgetHandlersUtil.getCollectionFromMap("test-key", map);
		assertFalse(collection.isEmpty());
		assertEquals(2, collection.size());
	}
}