package com.sirma.sep.content.idoc.nodes.widgets.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidgetBuilder;

/**
 * Test class for {@link ProcessWidgetBuilder}.
 *
 * @author hlungov
 */
public class ProcessWidgetBuilderTest {

	private ProcessWidgetBuilder processWidgetBuilder = new ProcessWidgetBuilder();
	private Element element;

	@Before
	public void setup() {
		element = new Element(Tag.valueOf("widget"), "");
	}

	@Test
	public void should_accept() {
		element.attr(Widget.WIDGET_NAME, ProcessWidget.NAME);
		assertTrue(processWidgetBuilder.accept(element));
	}

	@Test
	public void should_not_accept_missing_name_attr() {
		assertFalse(processWidgetBuilder.accept(element));
	}

	@Test
	public void should_not_accept_wrong_name_attr() {
		element.attr(Widget.WIDGET_NAME, "test");
		assertFalse(processWidgetBuilder.accept(element));
	}

	@Test
	public void should_build() {
		assertNotNull(processWidgetBuilder.build(element));
	}

}
