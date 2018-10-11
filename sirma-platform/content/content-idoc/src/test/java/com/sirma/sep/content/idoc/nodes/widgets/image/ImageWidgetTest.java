package com.sirma.sep.content.idoc.nodes.widgets.image;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidget;

/**
 * Test for {@link ImageWidget}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class ImageWidgetTest {

	@Test
	public void setContentEditable() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("contenteditable", "true");
		ImageWidget widget = new ImageWidget(node);
		widget.setContentEditable(Boolean.FALSE);
		assertFalse(Boolean.valueOf(widget.getProperty("contenteditable")));
	}

	@Test
	public void setContentEditable_passedNull_propertyNotChanged() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("contenteditable", "true");
		ImageWidget widget = new ImageWidget(node);
		widget.setContentEditable(null);
		assertTrue(Boolean.valueOf(widget.getProperty("contenteditable")));
	}

}
