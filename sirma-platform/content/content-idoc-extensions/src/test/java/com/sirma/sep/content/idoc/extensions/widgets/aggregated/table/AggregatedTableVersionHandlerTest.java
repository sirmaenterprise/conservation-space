package com.sirma.sep.content.idoc.extensions.widgets.aggregated.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.instance.version.VersionDao;
import com.sirma.itt.seip.instance.version.VersionIdsCache;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

/**
 * Test for {@link AggregatedTableVersionHandler}.
 *
 * @author A. Kunchev
 */
public class AggregatedTableVersionHandlerTest {

	private static final String VERSION_TEST_PREFIX = "version-";

	@InjectMocks
	private AggregatedTableVersionHandler handler;

	@Mock
	private VersionDao versionDao;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
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
	public void handle_noSearchResults() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		AggregatedTableWidget widget = new AggregatedTableWidget(node);
		HandlerContext context = new HandlerContext();
		context.put("versionCreationDate", new Date());
		widget.getConfiguration().setSearchResults(WidgetResults.fromConfiguration(null));
		handler.handle(widget, context);

		WidgetConfiguration configuration = widget.getConfiguration();
		assertNotNull(configuration);
		JsonObject jsonObject = configuration.getProperty("versionData", JsonObject.class);
		assertTrue(jsonObject.entrySet().isEmpty());
	}

	@Test
	public void handle_noVersionDate() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		AggregatedTableWidget widget = new AggregatedTableWidget(node);
		widget.getConfiguration().setSearchResults(WidgetResults.fromSearch(new HashMap<>()));
		handler.handle(widget, new HandlerContext());

		WidgetConfiguration configuration = widget.getConfiguration();
		assertNotNull(configuration);
		JsonObject jsonObject = configuration.getProperty("versionData", JsonObject.class);
		assertTrue(jsonObject.entrySet().isEmpty());
	}

	@Test
	public void handle_successful() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		AggregatedTableWidget widget = new AggregatedTableWidget(node);
		Map<String, Object> results = new HashMap<>(2);
		results.put("aggregatedData", buildAggregatedSearchResult());
		Collection<Serializable> searchIds = Arrays.asList("user-id-1", "user-id-2", "user-id-3", "user-id-4");
		results.put("instanceIds", searchIds);
		widget.getConfiguration().setSearchResults(WidgetResults.fromSearch(results));
		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollection(), any(Date.class)))
				.thenReturn(buildDaoResults(searchIds));
		HandlerContext context = new HandlerContext();
		Date date = new Date();
		context.put("versionCreationDate", date);
		context.put("versionedInstancesCache", new VersionIdsCache(date, versionDao::findVersionIdsByTargetIdAndDate));

		handler.handle(widget, context);

		WidgetConfiguration configuration = widget.getConfiguration();
		assertNotNull(configuration);
		JsonObject jsonObject = configuration.getProperty("versionData", JsonObject.class);
		assertNotNull(jsonObject);
		assertEquals(2, jsonObject.entrySet().size());
	}

	private static Map<String, Map<String, Serializable>> buildAggregatedSearchResult() {
		Map<String, Serializable> innerResults = new HashMap<>();
		innerResults.put("user-id-1", 2);
		innerResults.put("user-id-2", 4);
		innerResults.put("user-id-3", 1);
		innerResults.put("user-id-4", 1);
		Map<String, Map<String, Serializable>> aggregatedResults = new HashMap<>();
		aggregatedResults.put("aggregation-property", innerResults);
		return aggregatedResults;
	}

	private static Map<Serializable, Serializable> buildDaoResults(Collection<Serializable> searchIds) {
		return searchIds.stream().collect(Collectors.toMap(Function.identity(), id -> VERSION_TEST_PREFIX + id));
	}

}
