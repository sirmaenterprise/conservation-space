package com.sirma.sep.export.renders.html.table;

import static org.junit.Assert.assertEquals;

import org.jsoup.nodes.Element;
import org.junit.Test;

public class HtmlTableBuilderTest {
	
	@Test
	public void shouldAddHeaderRowByDefault() {
		HtmlTableBuilder builder = new HtmlTableBuilder("Table title");
		Element tableElement = builder.build();
		assertEquals(1, tableElement.childNodeSize());
	}

	@Test
	public void shouldAddHeaderRow() {
		HtmlTableBuilder builder = new HtmlTableBuilder("Table title", true);
		Element tableElement = builder.build();
		assertEquals(1, tableElement.childNodeSize());
	}
	
	@Test
	public void shouldNotAddHeaderRow() {
		HtmlTableBuilder builder = new HtmlTableBuilder("Table title", false);
		Element tableElement = builder.build();
		assertEquals(0, tableElement.childNodeSize());
	}
}
