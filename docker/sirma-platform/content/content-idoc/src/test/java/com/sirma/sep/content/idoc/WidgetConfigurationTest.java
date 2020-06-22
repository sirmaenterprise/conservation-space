package com.sirma.sep.content.idoc;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Test for {@link WidgetConfiguration}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class WidgetConfigurationTest {

	private WidgetConfiguration defaultTestConfig;

	@Before
	public void setup() {
		defaultTestConfig = new WidgetConfiguration(mock(Widget.class), new JsonObject());
	}

	@Test
	public void widgetConfigurationConstructor_stringConfig() throws IOException {
		WidgetConfiguration widgetConfiguration = buildConfigurationFromFile();
		assertNotNull(widgetConfiguration);
		assertNotNull(widgetConfiguration.getConfiguration());
	}

	@Test
	public void addNotNullProperty_nullKey() {
		defaultTestConfig.addNotNullProperty(null, new JsonObject());
		JsonObject configuration = defaultTestConfig.getConfiguration();
		assertEquals(0, configuration.entrySet().size());
	}

	@Test
	public void addNotNullProperty_emptyKey() {
		defaultTestConfig.addNotNullProperty("", new JsonObject());
		JsonObject configuration = defaultTestConfig.getConfiguration();
		assertEquals(0, configuration.entrySet().size());
	}

	@Test
	public void addNotNullProperty_nullValue() {
		defaultTestConfig.addNotNullProperty("key", null);
		JsonObject configuration = defaultTestConfig.getConfiguration();
		assertEquals(0, configuration.entrySet().size());
	}

	@Test
	public void addNotNullProperty_propertyAdded() {
		defaultTestConfig.addNotNullProperty("key", new JsonObject());
		JsonObject configuration = defaultTestConfig.getConfiguration();
		assertEquals(1, configuration.entrySet().size());
	}

	@Test
	public void getSearchCriteria_nullCriteria() {
		Optional<javax.json.JsonObject> criteria = defaultTestConfig.getSearchCriteria();
		assertFalse(criteria.isPresent());
	}

	@Test
	public void getSearchCriteria_withEmptyCriteria() {
		defaultTestConfig.addNotNullProperty("criteria", new JsonObject());
		Optional<javax.json.JsonObject> criteria = defaultTestConfig.getSearchCriteria();
		assertFalse(criteria.isPresent());
	}

	@Test
	public void getSearchCriteria_withCriteria() {
		JsonObject json = new JsonObject();
		json.addProperty("id", "criteria-id");
		defaultTestConfig.addNotNullProperty("criteria", json);
		Optional<javax.json.JsonObject> criteria = defaultTestConfig.getSearchCriteria();
		assertTrue(criteria.isPresent());
	}

	@Test
	public void getSelectionMode_noSuchProperty() {
		WidgetSelectionMode selectionMode = defaultTestConfig.getSelectionMode();
		assertNull(selectionMode);
	}

	@Test
	public void getSelectionMode_withProperty() {
		defaultTestConfig.setSelectionMode(WidgetSelectionMode.MANUALLY);
		WidgetSelectionMode selectionMode = defaultTestConfig.getSelectionMode();
		assertEquals(WidgetSelectionMode.MANUALLY, selectionMode);
	}

	@Test
	public void getArrayPropertyAsList_noSuchArray() {
		List<Object> result = defaultTestConfig.getArrayPropertyAsList("test-key");
		assertTrue(result.isEmpty());
	}

	@Test
	public void getArrayPropertyAsList_emptyArray() {
		defaultTestConfig.addNotNullProperty("array-key", new JsonArray());
		List<Object> result = defaultTestConfig.getArrayPropertyAsList("array-key");
		assertTrue(result.isEmpty());
	}

	@Test
	public void getArrayPropertyAsList_notArray() {
		defaultTestConfig.addNotNullProperty("array-key", new JsonObject());
		List<Object> result = defaultTestConfig.getArrayPropertyAsList("array-key");
		assertTrue(result.isEmpty());
	}

	@Test
	public void getArrayPropertyAsList_nullJson() {
		defaultTestConfig.addNotNullProperty("array-key", JsonNull.INSTANCE);
		List<Object> result = defaultTestConfig.getArrayPropertyAsList("array-key");
		assertTrue(result.isEmpty());
	}

	@Test
	public void getArrayPropertyAsList_notEmptyArray() {
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(new JsonPrimitive("value-1"));
		jsonArray.add(new JsonPrimitive("value-2"));
		defaultTestConfig.addNotNullProperty("array-key", jsonArray);
		List<Object> result = defaultTestConfig.getArrayPropertyAsList("array-key");
		assertFalse(result.isEmpty());
		assertEquals(2, result.size());
	}

	@Test
	public void getProperty_nullPropery() {
		JsonObject property = defaultTestConfig.getProperty("test-key", JsonObject.class);
		assertNull(property);
	}

	@Test
	public void getProperty_notNullPropery() {
		defaultTestConfig.addNotNullProperty("test-key", new JsonObject());
		JsonObject property = defaultTestConfig.getProperty("test-key", JsonObject.class);
		assertNotNull(property);
	}

	@Test
	public void writeConfiguration() {
		Widget widget = mock(Widget.class);
		WidgetConfiguration configuration = new WidgetConfiguration(widget, new JsonObject());
		configuration.writeConfiguration(new JsonObject());
		verify(widget).setConfiguration(anyString());
	}

	@Test
	public void getAllSelectedObjects_withoutObjects() {
		Set<Serializable> selectedObjects = defaultTestConfig.getAllSelectedObjects();
		assertTrue(selectedObjects.isEmpty());
	}

	@Test
	public void getAllSelectedObjects_withObjects() {
		defaultTestConfig.addNotNullProperty("selectedObject", new JsonPrimitive("instance-id-1"));
		JsonArray array = new JsonArray();
		array.add(new JsonPrimitive("instance-id-2"));
		array.add(new JsonPrimitive("instance-id-3"));
		defaultTestConfig.addNotNullProperty("selectedObjects", array);

		Set<Serializable> selectedObjects = defaultTestConfig.getAllSelectedObjects();
		assertEquals(3, selectedObjects.size());
	}

	@Test
	public void setSelectedObjects_nullCollection() {
		defaultTestConfig.setSelectedObjects(null);
		assertNull(defaultTestConfig.getConfiguration().get("selectedObjects"));
	}

	@Test
	public void setSelectedObjects_emptyCollection() {
		defaultTestConfig.setSelectedObjects(emptyList());
		assertNull(defaultTestConfig.getConfiguration().get("selectedObjects"));
	}

	@Test
	public void setSelectedObjects_withCollection() {
		defaultTestConfig.setSelectedObjects(Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3"));
		JsonElement element = defaultTestConfig.getConfiguration().get("selectedObjects");
		assertNotNull(element);
		assertEquals(3, element.getAsJsonArray().size());
	}

	@Test
	public void getSelectedObjects() {
		JsonArray array = new JsonArray();
		array.add(new JsonPrimitive("instance-id-1"));
		array.add(new JsonPrimitive("instance-id-2"));
		defaultTestConfig.getConfiguration().add("selectedObjects", array);

		Collection<String> selectedObjects = defaultTestConfig.getSelectedObjects();
		assertNotNull(selectedObjects);
		assertEquals(2, selectedObjects.size());
	}

	@Test
	public void setSelectedObject_nullId() {
		defaultTestConfig.setSelectedObject(null);
		assertNull(defaultTestConfig.getConfiguration().get("selectedObject"));
	}

	@Test
	public void setSelectedObjects_blankId() {
		defaultTestConfig.setSelectedObject("");
		assertNull(defaultTestConfig.getConfiguration().get("selectedObject"));
	}

	@Test
	public void setSelectedObjects_correctId() {
		defaultTestConfig.setSelectedObject("instance-id");
		JsonElement element = defaultTestConfig.getConfiguration().get("selectedObject");
		assertNotNull(element);
		assertEquals("instance-id", element.getAsString());
	}

	@Test
	public void getSelectedObject() {
		defaultTestConfig.getConfiguration().addProperty("selectedObject", "instance-id");
		String selectedObject = defaultTestConfig.getSelectedObject();
		assertNotNull(selectedObject);
		assertEquals("instance-id", selectedObject);
	}

	@Test
	public void cleanUpAllSelectedObjects_withSelectedObject_cleaned() {
		defaultTestConfig.setSelectedObject("Fast InvSqrt");
		assertFalse(defaultTestConfig.getSelectedObject().isEmpty());

		defaultTestConfig.cleanUpAllSelectedObjects();
		assertTrue(defaultTestConfig.getSelectedObject().isEmpty());
	}

	@Test
	public void cleanUpAllSelectedObjects_withSelectedObjects_cleaned() {
		defaultTestConfig.setSelectedObjects(Arrays.asList("Fast InvSqrt"));
		assertFalse(defaultTestConfig.getSelectedObjects().isEmpty());

		defaultTestConfig.cleanUpAllSelectedObjects();
		assertTrue(defaultTestConfig.getSelectedObjects().isEmpty());
	}

	@Test
	public void should_ProvideSelectedProperties() {
		WidgetConfiguration config = buildConfigurationFromFile();

		assertEquals("prop1", config.getSelectedProperties().get("test").getAsString());
	}

	private WidgetConfiguration buildConfigurationFromFile() {
		try {
			String config = IOUtils.toString(
					WidgetConfiguration.class.getClassLoader().getResourceAsStream("encoded-widget-configuration.txt"));
			return new WidgetConfiguration(mock(Widget.class), config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
