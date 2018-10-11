package com.sirma.sep.content.idoc.nodes.widgets.recentactivities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidgetConfiguration;

/**
 * Test for {@link RecentActivitiesWidgetConfiguration}.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetConfigurationTest {

	private RecentActivitiesWidgetConfiguration configuration;

	@Before
	public void setup() {
		configuration = new RecentActivitiesWidgetConfiguration(mock(Widget.class), new JsonObject());
	}

	@Test
	public void getLimit_nullProperty_defaultResult() {
		int limit = configuration.getLimit(25);
		assertEquals(25, limit);
	}

	@Test
	public void getLimit_valueAll_unlimited() {
		configuration.addNotNullProperty("pageSize", new JsonPrimitive("all"));
		int limit = configuration.getLimit(25);
		assertEquals(-1, limit);
	}

	@Test
	public void getLimit_withValue() {
		configuration.addNotNullProperty("pageSize", new JsonPrimitive(10));
		int limit = configuration.getLimit(25);
		assertEquals(10, limit);
	}

	@Test
	public void setSelectedObjects() {
		configuration.setSelectedObjects(Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3"));
		assertEquals(3, configuration.getArrayPropertyAsList("selectedItems").size());
	}

	@Test
	public void getSelectedObjects() {
		JsonArray items = new JsonArray();
		for (int i = 0; i < 3; i++) {
			JsonObject item = new JsonObject();
			item.addProperty("id", "instance-id-" + i);
			items.add(item);
		}
		configuration.addNotNullProperty("selectedItems", items);
		assertEquals(3, configuration.getSelectedObjects().size());
	}

	@Test
	public void cleanUpAllSelectedObjects_cleanedSelectedItems() {
		configuration.setSelectedObjects(Arrays.asList("selectedItem"));
		assertFalse(configuration.getSelectedObjects().isEmpty());

		configuration.cleanUpAllSelectedObjects();
		assertTrue(configuration.getSelectedObjects().isEmpty());
	}

}
