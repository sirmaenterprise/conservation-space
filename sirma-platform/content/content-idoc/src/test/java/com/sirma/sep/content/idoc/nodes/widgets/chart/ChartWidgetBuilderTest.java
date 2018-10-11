package com.sirma.sep.content.idoc.nodes.widgets.chart;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidget;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidgetBuilder;

/**
 * Test class for {@link ChartWidgetBuilder}
 *
 * @author hlungov
 */
public class ChartWidgetBuilderTest {

	private ChartWidgetBuilder chartWidgetBuilder = new ChartWidgetBuilder();
	private Element element;

	@Before
	public void setup() {
		element = new Element(Tag.valueOf("widget"),"");
	}

	@Test
	public void should_accept() {
		element.attr(Widget.WIDGET_NAME,ChartWidget.CHART_WIDGET_NAME);
		assertTrue(chartWidgetBuilder.accept(element));
	}

	@Test
	public void should_not_accept_missing_name_attr() {
		assertFalse(chartWidgetBuilder.accept(element));
	}

	@Test
	public void should_not_accept_wrong_name_attr() {
		element.attr(Widget.WIDGET_NAME, "test");
		assertFalse(chartWidgetBuilder.accept(element));
	}

	@Test
	public void should_build() {
		assertNotNull(chartWidgetBuilder.build(element));
	}
}
