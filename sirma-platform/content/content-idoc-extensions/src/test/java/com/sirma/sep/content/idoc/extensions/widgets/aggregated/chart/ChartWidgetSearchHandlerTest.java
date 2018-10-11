package com.sirma.sep.content.idoc.extensions.widgets.aggregated.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.sep.content.idoc.extensions.widgets.aggregated.chart.ChartWidgetSearchHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidget;

/**
 * Test class for {@link ChartWidgetSearchHandler}
 *
 * @author hlungov
 */
public class ChartWidgetSearchHandlerTest {

	private static final String CHART_WIDGET_CONFIG = "chart-widget-manually-select-config.txt";
	private static final String CHART_WIDGET_CONFIG_WITHOUT_GROUPBY = "chart-widget-manually-select-config-without-groupBy.txt";
	private static final String CURRENT_OBJECT = "current_object";

	@InjectMocks
	private ChartWidgetSearchHandler handler;

	@Mock
	private SearchService searchService;

	@Mock
	private JsonToConditionConverter jsonToConditionConverter;

	@Mock
	private ContentNodeHandler.HandlerContext handlerContext;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void accept_incorrectType() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctType() {
		boolean result = handler.accept(mock(ChartWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle_main_search_results_not_present() {
		Element node = new Element(Tag.valueOf("div"), "");
		ChartWidget widget = new ChartWidget(node);
		ContentNodeHandler.HandlerResult handlerResult = handler.handle(widget, new ContentNodeHandler.HandlerContext());
		assertFalse(handlerResult.getResult().isPresent());
	}

	@Test
	public void handle_without_groupBy_test() throws IOException {
		testHandle(CHART_WIDGET_CONFIG_WITHOUT_GROUPBY);
	}

	private void testHandle(String resource) throws IOException {
		Element node = new Element(Tag.valueOf("div"), "");
		when(jsonToConditionConverter.parseCondition(any(JsonObject.class))).thenReturn(
				SearchCriteriaBuilder.createConditionBuilder().build());
		SearchArguments arguments = new SearchArguments<>();
		arguments.setStringQuery("SELECT something with " + CURRENT_OBJECT);
		when(searchService.parseRequest(any(SearchRequest.class))).thenReturn(arguments);
		stubAggregatedSearch();
		try (InputStream stream = ChartWidgetSearchHandler.class
				.getClassLoader()
				.getResourceAsStream(resource)) {
			node.attr("config", IOUtils.toString(stream));
			ChartWidget widget = new ChartWidget(node);

			when(handlerContext.getCurrentInstanceId()).thenReturn("emf:something");
			ContentNodeHandler.HandlerResult result = handler.handle(widget, handlerContext);

			// The queries passed from the web have the string "current_object" there which we replace in order to
			// execute valid search. This assert validates this behavior.
			// TODO: this should be removed once this... solution is fixed.
			assertNotEquals(arguments.getStringQuery(), CURRENT_OBJECT);

			assertNotNull(result);
			Optional<Map<String, Object>> optional = result.getResult();
			assertTrue(optional.isPresent());

			Map<String, Object> handlerResultMap = optional.get();
			assertEquals(2, handlerResultMap.size());
			Map<String, Map<String, Serializable>> aggregatedResults = (Map<String, Map<String, Serializable>>) handlerResultMap
					.get("aggregatedData");
			assertEquals(4, aggregatedResults.get("aggregation-property").size());
		}
	}

	@Test
	public void handleTest() throws IOException {
		testHandle(CHART_WIDGET_CONFIG);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void stubAggregatedSearch() {
		doAnswer(a -> {
			SearchArguments arguments = a.getArgumentAt(1, SearchArguments.class);
			Map<String, Serializable> innerResults = new HashMap<>();
			innerResults.put("user-id-1", 2);
			innerResults.put("user-id-2", 4);
			innerResults.put("user-id-3", 1);
			innerResults.put("user-id-4", 1);
			Map<String, Map<String, Serializable>> aggregatedResults = new HashMap<>();
			aggregatedResults.put("aggregation-property", innerResults);
			arguments.setAggregated(aggregatedResults);
			return arguments;
		}).when(searchService).search(any(), any(SearchArguments.class));
	}
}
