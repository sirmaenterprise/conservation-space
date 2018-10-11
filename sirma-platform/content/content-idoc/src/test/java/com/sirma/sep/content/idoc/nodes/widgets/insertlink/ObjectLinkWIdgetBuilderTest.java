package com.sirma.sep.content.idoc.nodes.widgets.insertlink;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.nodes.widgets.insertlink.ObjectLinkWIdgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.insertlink.ObjectLinkWidget;

/**
 * Test for {@link ObjectLinkWIdgetBuilder}.
 *
 * @author A. Kunchev
 */
public class ObjectLinkWIdgetBuilderTest {

	private ObjectLinkWIdgetBuilder builder;

	@Before
	public void setup() {
		builder = new ObjectLinkWIdgetBuilder();
	}

	@Test
	public void accept_notWidget_false() {
		Element element = new Element(Tag.valueOf("div"), "");
		assertFalse(builder.accept(element));
	}

	@Test
	public void accept_notObjectLinkWidget_false() {
		Element element = new Element(Tag.valueOf("div"), "");
		element.attr("widget", "in-the-end");
		assertFalse(builder.accept(element));
	}

	@Test
	public void accept_objectLinkWidget_true() {
		Element element = new Element(Tag.valueOf("div"), "");
		element.attr("widget", "object-link");
		assertTrue(builder.accept(element));
	}

	@Test
	public void build() {
		Element element = new Element(Tag.valueOf("div"), "");
		element.attr("widget", "object-link");
		assertTrue(builder.build(element) instanceof ObjectLinkWidget);
	}

}
