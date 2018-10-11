package com.sirma.sep.content.idoc.extensions.widgets.image;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidget;

/**
 * Test for {@link ImageWidgetVersionHandler}.
 *
 * @author A. Kunchev
 */
public class ImageWidgetVersionHandlerTest {

	private ImageWidgetVersionHandler handler;

	@Before
	public void setup() {
		handler = new ImageWidgetVersionHandler();
	}

	@Test
	public void accept_incorrectType_false() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctType_true() {
		boolean result = handler.accept(mock(ImageWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle_widgetEditDisabled() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		ImageWidget widget = new ImageWidget(node);
		widget.getConfiguration().setSearchResults(WidgetResults.fromConfiguration(null));
		handler.handle(widget, new HandlerContext());
		assertTrue(widget.getConfiguration().getProperty("lockWidget", Boolean.class));
	}
}