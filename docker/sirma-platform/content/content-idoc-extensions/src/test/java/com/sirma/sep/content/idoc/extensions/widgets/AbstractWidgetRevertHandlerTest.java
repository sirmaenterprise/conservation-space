package com.sirma.sep.content.idoc.extensions.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.WidgetSelectionMode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetRevertHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;

/**
 * Test for {@link AbstractWidgetRevertHandler}.
 *
 * @author A. Kunchev
 */
public class AbstractWidgetRevertHandlerTest {

	private static final String TEST_JSON_SELECTED_OBJECT = "revert-widget-configuration-with-selected-object-diff.json";
	private static final String TEST_JSON_SELECTED_OBJECTS = "revert-widget-configuration-with-selected-objects-diff.json";

	private AbstractWidgetRevertHandler<Widget> handler;

	@Before
	public void setup() {
		handler = mock(AbstractWidgetRevertHandler.class, CALLS_REAL_METHODS);
	}

	@Test
	public void accept_alwaysShouldReturnFalse() {
		assertFalse(handler.accept(new WidgetMock()));
	}

	@Test
	public void handle_noConfigDiff_noChangesToConfiguration() {
		WidgetConfiguration configuration = mock(WidgetConfiguration.class);
		when(configuration.getSelectionMode()).thenReturn(WidgetSelectionMode.MANUALLY);
		when(configuration.getConfiguration()).thenReturn(new JsonObject());

		Widget node = new WidgetMock(configuration);
		handler.handle(node, new HandlerContext());
		verify(configuration, never()).getProperty(anyString(), any());
		verify(configuration, never()).addNotNullProperty(anyString(), any(JsonElement.class));
		verify(configuration, never()).cleanUpAllSelectedObjects();
	}

	@Test
	public void handle_automaticallySelectedObjct_changesReverted() throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(
				getClass().getClassLoader().getResourceAsStream(TEST_JSON_SELECTED_OBJECT), StandardCharsets.UTF_8))) {
			JsonObject jsonElement = new JsonParser().parse(reader).getAsJsonObject();
			Widget node = new WidgetMock(new WidgetConfiguration(null, jsonElement));
			handler.handle(node, new HandlerContext());

			WidgetConfiguration configuration = node.getConfiguration();
			assertEquals("instance-id", configuration.getSelectedObject());
			assertEquals(WidgetSelectionMode.AUTOMATICALLY, configuration.getSelectionMode());
		}
	}

	@Test
	public void handle_manuallySelectedObjects_changesReverted() throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(
				getClass().getClassLoader().getResourceAsStream(TEST_JSON_SELECTED_OBJECTS), StandardCharsets.UTF_8))) {
			JsonObject jsonElement = new JsonParser().parse(reader).getAsJsonObject();
			Widget node = new WidgetMock(new WidgetConfiguration(null, jsonElement));
			handler.handle(node, new HandlerContext());

			WidgetConfiguration configuration = node.getConfiguration();
			assertEquals(3, configuration.getSelectedObjects().size());
			assertEquals("instance-id-1", configuration.getSelectedObjects().iterator().next());
			assertEquals(WidgetSelectionMode.MANUALLY, configuration.getSelectionMode());
		}
	}

	@Test
	public void handle_manuallySelectedObjectsWithAdditionalChanges_changesReverted() throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(
				getClass().getClassLoader().getResourceAsStream(TEST_JSON_SELECTED_OBJECTS), StandardCharsets.UTF_8))) {
			JsonObject jsonElement = new JsonParser().parse(reader).getAsJsonObject();
			Widget node = new WidgetMock(new WidgetConfiguration(null, jsonElement));
			handler.handle(node, new HandlerContext());

			WidgetConfiguration configuration = node.getConfiguration();
			assertEquals(2, configuration.getConfiguration().entrySet().size());
			assertEquals(3, configuration.getSelectedObjects().size());
			assertEquals("instance-id-1", configuration.getSelectedObjects().iterator().next());
			assertEquals(WidgetSelectionMode.MANUALLY, configuration.getSelectionMode());
			assertNull(configuration.getProperty("addedByVersioning", String.class));
			assertNull(configuration.getProperty("originalConfigurationDiff", JsonObject.class));
		}
	}

}
