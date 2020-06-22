package com.sirma.sep.content.idoc.nodes.widgets.image;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidget;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidgetConfiguration;

/**
 * Test for {@link ImageWidgetConfiguration}.
 *
 * @author A. Kunchev
 */
public class ImageWidgetConfigurationTest {

	private ImageWidgetConfiguration configuration;

	@Before
	public void setup() {
		configuration = new ImageWidgetConfiguration(mock(ImageWidget.class), new JsonObject());
	}

	@Test
	public void lockWidget() {
		configuration.lockWidget();
		assertTrue(configuration.getProperty("lockWidget", Boolean.class).booleanValue());
	}

	@Test
	public void unlockWidget() {
		configuration.unlockWidget();
		assertFalse(configuration.getProperty("lockWidget", Boolean.class).booleanValue());
	}

}
