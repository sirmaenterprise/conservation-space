package com.sirma.sep.content.idoc.extensions.widgets.comments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sirma.itt.seip.instance.version.VersionProperties.WidgetsHandlerContextProperties;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidget;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidgetConfiguration;

/**
 * Test for {@link CommentsWidgetVersionHandler}.
 *
 * @author A. Kunchev
 */
public class CommentsWidgetVersionHandlerTest {

	private CommentsWidgetVersionHandler handler;

	@Before
	public void setup() {
		handler = new CommentsWidgetVersionHandler();
	}

	@Test
	public void accept_incorrectWidgetType_false() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctWidgetType_false() {
		boolean result = handler.accept(mock(CommentsWidget.class));
		assertTrue(result);
	}

	@Test
	public void processResults_withoutDateRagne() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		CommentsWidget widget = new CommentsWidget(node);
		HandlerContext context = new HandlerContext();
		context.put(WidgetsHandlerContextProperties.VERSION_DATE_KEY, new Date());
		handler.processResults(widget, WidgetResults.fromSearch(buildSearchResults(new Object())), context);
		CommentsWidgetConfiguration configuration = widget.getConfiguration();
		assertNotNull(configuration);
		assertEquals(4, configuration.getSelectedObjects().size());
		JsonObject filterCriteria = configuration.getFilterCriteria();
		assertNull(filterCriteria);
	}

	@Test
	public void processResults_withDateRagne_withoutAfterDate() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		CommentsWidget widget = new CommentsWidget(node);
		HandlerContext context = new HandlerContext();
		context.put(WidgetsHandlerContextProperties.VERSION_DATE_KEY, new Date());
		handler.processResults(widget,
				WidgetResults.fromSearch(buildSearchResults(new DateRange(null, new Date()))), context);
		CommentsWidgetConfiguration configuration = widget.getConfiguration();
		assertNotNull(configuration);
		assertEquals(4, configuration.getSelectedObjects().size());
		JsonObject filterCriteria = configuration.getFilterCriteria();
		assertNotNull(filterCriteria);
		JsonArray dateArray = filterCriteria.getAsJsonArray("value");
		assertEquals(2, dateArray.size());
		assertTrue(dateArray.get(0).getAsString().isEmpty());
		assertFalse(dateArray.get(1).getAsString().isEmpty());
	}

	@Test
	public void processResults_withDateRagne_withBothaDates() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		CommentsWidget widget = new CommentsWidget(node);
		JsonObject criteria = new JsonObject();
		criteria.addProperty("operator", "after");
		widget.getConfiguration().getConfiguration().add("filterCriteria", criteria);
		WidgetResults widgetResults = WidgetResults
				.fromSearch(buildSearchResults(new DateRange(new Date(), new Date())));
		HandlerContext context = new HandlerContext();
		context.put(WidgetsHandlerContextProperties.VERSION_DATE_KEY, new Date());
		handler.processResults(widget, widgetResults, context);
		CommentsWidgetConfiguration configuration = widget.getConfiguration();
		assertNotNull(configuration);
		assertEquals(4, configuration.getSelectedObjects().size());
		JsonObject filterCriteria = configuration.getFilterCriteria();
		assertNotNull(filterCriteria);
		assertEquals("version", filterCriteria.get("operator").getAsString());
		JsonArray dateArray = filterCriteria.getAsJsonArray("value");
		assertEquals(2, dateArray.size());
		assertFalse(dateArray.get(0).getAsString().isEmpty());
		assertFalse(dateArray.get(1).getAsString().isEmpty());
	}

	private static Object buildSearchResults(Object range) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("instanceIds", Arrays.asList("instnace-id-1", "instnace-id-2", "instnace-id-3", "instnace-id-4"));
		resultMap.put("dateRange", range);
		return resultMap;
	}
}