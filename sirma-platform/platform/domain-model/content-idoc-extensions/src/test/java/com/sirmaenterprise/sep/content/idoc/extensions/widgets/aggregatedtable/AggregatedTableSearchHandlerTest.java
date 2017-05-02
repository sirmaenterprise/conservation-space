package com.sirmaenterprise.sep.content.idoc.extensions.widgets.aggregatedtable;

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

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirmaenterprise.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirmaenterprise.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirmaenterprise.sep.content.idoc.handler.ContentNodeHandler.HandlerResult;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

/**
 * Test for {@link AggregatedTableSearchHandler}.
 *
 * @author A. Kunchev
 */
public class AggregatedTableSearchHandlerTest {

	private static final String AGGREGATED_TABLE_CONFIG = "aggregated-table-manually-select-config.txt";
	private static final String CURRENT_OBJECT = "current_object";

	@InjectMocks
	private AggregatedTableSearchHandler handler;

	@Mock
	private SearchService searchService;

	@Mock
	private JsonToConditionConverter jsonToConditionConverter;

	@Mock
	private HandlerContext handlerContext;

	@Before
	public void setup() {
		handler = new AggregatedTableSearchHandler();
		MockitoAnnotations.initMocks(this);
		ReflectionUtils.setField(handler, "searchService", searchService);
		ReflectionUtils.setField(handler, "jsonToConditionConverter", jsonToConditionConverter);
	}

	@Test
	public void accept_incorrectType() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctType() {
		boolean result = handler.accept(mock(AggregatedTableWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle_mainSearchResultsNotPresent() {
		Element node = new Element(Tag.valueOf("div"), "");
		AggregatedTableWidget widget = new AggregatedTableWidget(node);
		HandlerResult handlerResult = handler.handle(widget, new HandlerContext());
		assertFalse(handlerResult.getResult().isPresent());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void handleTest() throws IOException {
		Element node = new Element(Tag.valueOf("div"), "");
		when(jsonToConditionConverter.parseCondition(any(JsonObject.class))).thenReturn(new Condition());
		SearchArguments arguments = new SearchArguments<>();
		arguments.setStringQuery("SELECT something with " + CURRENT_OBJECT);
		when(searchService.parseRequest(any(SearchRequest.class))).thenReturn(arguments);
		stubAggregatedSearch();
		try (InputStream stream = AggregatedTableSearchHandler.class
				.getClassLoader()
					.getResourceAsStream(AGGREGATED_TABLE_CONFIG)) {
			node.attr("config", IOUtils.toString(stream));
			AggregatedTableWidget widget = new AggregatedTableWidget(node);

			when(handlerContext.getCurrentInstanceId()).thenReturn("emf:something");
			HandlerResult result = handler.handle(widget, handlerContext);

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
