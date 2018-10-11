package com.sirma.sep.content.idoc.extensions.widgets.objectlink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.nodes.widgets.insertlink.ObjectLinkWidget;

/**
 * Test for {@link ObjectLinkWidgetSearcHandler}.
 *
 * @author A. Kunchev
 */
public class ObjectLinkWidgetSearcHandlerTest {

	private static final String WIDGET_CONFIG = "eyJzZWxlY3RlZE9iamVjdCI6IlRoZSBPbmUgUmluZyJ9";

	private ObjectLinkWidgetSearcHandler handler;

	@Before
	public void setUp() {
		handler = new ObjectLinkWidgetSearcHandler();
	}

	@Test
	public void accept_incorrectType_false() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctType_true() {
		boolean result = handler.accept(mock(ObjectLinkWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle_searchResultsStored() {
		Element element = new Element(Tag.valueOf("div"), "");
		element.attr("config", WIDGET_CONFIG);
		ObjectLinkWidget widget = new ObjectLinkWidget(element);
		handler.handle(widget, new HandlerContext());
		assertTrue(widget.getConfiguration().getSearchResults().areAny());
		Collection<String> actual = widget.getConfiguration().getSearchResults().getResultsAsCollection();
		assertEquals("The One Ring", actual.iterator().next());
	}
}