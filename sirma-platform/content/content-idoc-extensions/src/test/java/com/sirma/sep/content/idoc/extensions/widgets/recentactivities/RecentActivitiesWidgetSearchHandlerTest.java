package com.sirma.sep.content.idoc.extensions.widgets.recentactivities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesRetriever;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerResult;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidget;

/**
 * Test for {@link RecentActivitiesWidgetSearchHandler}.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetSearchHandlerTest {

	private static final String TEST_WIDGET_CONFIG = "recent-activities-widget-configuration-manually-mode.txt";

	@InjectMocks
	private RecentActivitiesWidgetSearchHandler handler;

	@Mock
	private RecentActivitiesRetriever retriever;

	@Mock
	private SearchService searchService;

	@Mock
	private JsonToConditionConverter conditionConverter;

	@Before
	public void setup() {
		handler = new RecentActivitiesWidgetSearchHandler();
		MockitoAnnotations.initMocks(this);
		ReflectionUtils.setFieldValue(handler, "searchService", searchService);
		ReflectionUtils.setFieldValue(handler, "jsonToConditionConverter", conditionConverter);
	}

	@Test
	public void accept_incorrectWidgetType() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctWidgetType() {
		boolean result = handler.accept(mock(RecentActivitiesWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle_mainSearchResultsNotPresent() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		RecentActivitiesWidget widget = new RecentActivitiesWidget(node);
		HandlerResult handlerResult = handler.handle(widget, new HandlerContext());
		assertFalse(handlerResult.getResult().isPresent());
	}

	@Test
	public void handle_withMainSearchResults() throws IOException {
		when(retriever.getActivities(anyList(), eq(0), anyInt())).thenReturn(new StoredAuditActivitiesWrapper());
		try (InputStream stream = RecentActivitiesWidgetSearchHandlerTest.class
				.getClassLoader()
					.getResourceAsStream(TEST_WIDGET_CONFIG)) {
			Element node = new Element(Tag.valueOf("div"), "");
			node.attr("config", IOUtils.toString(stream));
			RecentActivitiesWidget widget = new RecentActivitiesWidget(node);
			HandlerResult result = handler.handle(widget, new HandlerContext("current-instnace-id"));
			assertNotNull(result);
			Optional<Object> optional = result.getResult();
			assertTrue(optional.isPresent());
			assertTrue(optional.get() instanceof Map);
		}
	}

}
