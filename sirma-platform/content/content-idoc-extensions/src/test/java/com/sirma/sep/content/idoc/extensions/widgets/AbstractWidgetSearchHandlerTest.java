package com.sirma.sep.content.idoc.extensions.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.json.JsonObject;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerResult;
import com.sirma.sep.content.idoc.nodes.widgets.objectdata.ObjectDataWidget;

/**
 * Test for {@link AbstractWidgetSearchHandler}.
 *
 * @author A. Kunchev
 */
public class AbstractWidgetSearchHandlerTest {

	private static final String CONFIGURATION_WITH_MANUALLY_SELECTION = "widget-configuration-with-manually-selection.txt";
	private static final String CONFIGURATION_WITH_SEARCH_CRITERIA = "widget-configuration-with-search-criteria.txt";

	@InjectMocks
	private AbstractWidgetSearchHandler<ObjectDataWidget> handler;

	@Mock
	private SearchService searchService;

	@Mock
	private JsonToConditionConverter jsonToConditionConverter;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Before
	public void setup() {
		handler = mock(AbstractWidgetSearchHandler.class, CALLS_REAL_METHODS);
		MockitoAnnotations.initMocks(this);
		ReflectionUtils.setFieldValue(handler, "searchService", searchService);
		ReflectionUtils.setFieldValue(handler, "jsonToConditionConverter", jsonToConditionConverter);
		ReflectionUtils.setFieldValue(handler, "jsonToDateRangeConverter", jsonToDateRangeConverter);

		doNothing().when(jsonToDateRangeConverter)
				.populateConditionRuleWithDateRange(any(ConditionBuilder.class), any(Condition.class));
	}

	@Test
	public void handle_manuallySelected() throws IOException {
		try (InputStream stream = AbstractWidgetSearchHandlerTest.class
				.getClassLoader()
					.getResourceAsStream(CONFIGURATION_WITH_MANUALLY_SELECTION)) {
			Element node = new Element(Tag.valueOf("div"), "");
			node.attr("config", IOUtils.toString(stream));
			ObjectDataWidget dataWidget = new ObjectDataWidget(node);
			HandlerResult result = handler.handle(dataWidget, new HandlerContext());
			WidgetResults searchResults = dataWidget.getConfiguration().getSearchResults();
			Optional<Collection<String>> resultOpt = result.getResult();
			assertTrue(resultOpt.isPresent());
			assertTrue(searchResults.areAny());
			assertFalse(resultOpt.get().isEmpty());
			assertEquals(searchResults.getResultsAsCollection(), resultOpt.get());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void handle_withoutSearchArguments() throws IOException {
		when(jsonToConditionConverter.parseCondition(any(JsonObject.class))).thenReturn(
				SearchCriteriaBuilder.createConditionBuilder().build());
		when(searchService.parseRequest(any(SearchRequest.class))).thenReturn(null);
		try (InputStream stream = AbstractWidgetSearchHandlerTest.class
				.getClassLoader()
					.getResourceAsStream(CONFIGURATION_WITH_SEARCH_CRITERIA)) {
			Element node = new Element(Tag.valueOf("div"), "");
			node.attr("config", IOUtils.toString(stream));
			ObjectDataWidget dataWidget = new ObjectDataWidget(node);
			handler.handle(dataWidget, new HandlerContext());
		}
	}

	@Test
	public void handle_withSearch() throws IOException {
		when(jsonToConditionConverter.parseCondition(any(JsonObject.class))).thenReturn(
				SearchCriteriaBuilder.createConditionBuilder().build());
		SearchArguments<Object> arguments = new SearchArguments<>();
		arguments.setResult(Arrays.asList(new EmfInstance("instance-id-1"), new EmfInstance("instance-id-2")));
		arguments.setStringQuery("");
		when(searchService.parseRequest(any(SearchRequest.class))).thenReturn(arguments);
		doAnswer(a -> AdditionalAnswers.returnsSecondArg()).when(searchService).search(any(), any());

		try (InputStream stream = AbstractWidgetSearchHandlerTest.class
				.getClassLoader()
					.getResourceAsStream(CONFIGURATION_WITH_SEARCH_CRITERIA)) {
			Element node = new Element(Tag.valueOf("div"), "");
			node.attr("config", IOUtils.toString(stream));
			ObjectDataWidget dataWidget = new ObjectDataWidget(node);
			HandlerResult result = handler.handle(dataWidget, new HandlerContext());
			WidgetResults configStore = dataWidget.getConfiguration().getSearchResults();
			Optional<Collection<String>> resultOpt = result.getResult();
			assertTrue(resultOpt.isPresent());
			assertTrue(configStore.areAny());
			assertFalse(resultOpt.get().isEmpty());
			assertEquals(configStore.getResultsAsCollection(), resultOpt.get());
		}
	}

	@Test
	public void test_handleResults() {
		SearchArguments<Instance> mockArguments = mock(SearchArguments.class);
		when(mockArguments.getResult()).thenReturn(null);
		assertNull(handler.handleResults(mockArguments));
	}
}
