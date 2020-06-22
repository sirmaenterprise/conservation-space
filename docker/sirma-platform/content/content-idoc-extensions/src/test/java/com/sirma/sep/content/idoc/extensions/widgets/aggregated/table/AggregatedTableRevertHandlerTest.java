package com.sirma.sep.content.idoc.extensions.widgets.aggregated.table;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Base64;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.sep.content.idoc.extensions.widgets.aggregated.table.AggregatedTableRevertHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

/**
 * Test for {@link AggregatedTableRevertHandler}.
 *
 * @author A. Kunchev
 */
public class AggregatedTableRevertHandlerTest {

	private AggregatedTableRevertHandler handler;

	@Before
	public void setup() {
		handler = new AggregatedTableRevertHandler();
	}

	@Test
	public void accept_incorrectType_false() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctType_false() {
		boolean result = handler.accept(mock(AggregatedTableWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle_versionPropertyRemoved() {
		Element node = new Element(Tag.valueOf("div"), "");
		JsonObject object = new JsonObject();
		object.add("versionData", new JsonPrimitive("versions, version, versions"));
		node.attr("config", Base64.getEncoder().encodeToString(object.toString().getBytes()));
		AggregatedTableWidget widget = new AggregatedTableWidget(node);
		handler.handle(widget, null);
		assertFalse(widget.getConfiguration().getConfiguration().has("versionData"));
	}

}
