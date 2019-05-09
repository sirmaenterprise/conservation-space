package com.sirma.sep.content.idoc.extensions.widgets;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.itt.seip.instance.dao.InstanceExistResult;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.VersionDao;
import com.sirma.itt.seip.instance.version.VersionIdsCache;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.WidgetSelectionMode;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerResult;
import com.sirma.sep.content.idoc.nodes.widgets.datatable.DataTableWidget;

/**
 * Test for {@link AbstractWidgetVersionHandler}.
 *
 * @author A. Kunchev
 */
public class AbstractWidgetVersionHandlerTest {

	@InjectMocks
	private AbstractWidgetVersionHandler<DataTableWidget> handler;

	@Mock
	private VersionDao versionDao;

	@Mock
	private InstanceService instanceService;

	@Before
	public void setup() {
		handler = mock(AbstractWidgetVersionHandler.class, CALLS_REAL_METHODS);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void handle_noSearchResults() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		DataTableWidget widget = new DataTableWidget(node);
		widget.getConfiguration().setSelectionMode(WidgetSelectionMode.AUTOMATICALLY);
		widget.getConfiguration().setSearchResults(WidgetResults.fromSearch(null));
		HandlerResult handlerResult = handler.handle(widget, prepareHandlerContext());
		assertFalse(handlerResult.getResult().isPresent());
	}

	@Test
	public void handle_noVersionDate() {
		Element node = new Element(Tag.valueOf("section"), "");
		node.attr("config", "e30=");
		node.attr("id", "widget-id");
		DataTableWidget widget = new DataTableWidget(node);
		WidgetConfiguration configuration = widget.getConfiguration();
		configuration.setSearchResults(WidgetResults.fromSearch(Arrays.asList("result-1", "result-2")));
		configuration.setSelectionMode(WidgetSelectionMode.AUTOMATICALLY);
		HandlerResult handlerResult = handler.handle(widget, new HandlerContext());
		assertFalse(handlerResult.getResult().isPresent());
	}

	@Test
	public void handle_selectionModeSetToCurrent() {
		Element node = new Element(Tag.valueOf("section"), "");
		node.attr("config", "e30=");
		node.attr("id", "widget-id");
		DataTableWidget widget = new DataTableWidget(node);
		widget.getConfiguration().setSearchResults(
				WidgetResults.fromSearch(Arrays.asList("result-1", "result-2")));
		widget.getConfiguration().setSelectionMode(WidgetSelectionMode.CURRENT);
		HandlerResult handlerResult = handler.handle(widget, prepareHandlerContext());
		assertFalse(handlerResult.getResult().isPresent());
	}

	@Test
	public void handle_selectionModeManually_selectedObjects() {
		DataTableWidget widget = prepareMultipleInstancesAsResult(WidgetSelectionMode.MANUALLY);
		handler.handle(widget, prepareHandlerContext());
		WidgetConfiguration widgetConfiguration = widget.getConfiguration();
		assertEquals(WidgetSelectionMode.MANUALLY, widgetConfiguration.getSelectionMode());
		assertEquals(2, widgetConfiguration.getSelectedObjects().size());
	}

	@Test
	public void handle_selectionModeManually_selectedObject() {
		DataTableWidget widget = prepareSingleInstanecAsResult(WidgetSelectionMode.MANUALLY);
		handler.handle(widget, prepareHandlerContext());
		WidgetConfiguration widgetConfiguration = widget.getConfiguration();
		assertEquals(WidgetSelectionMode.MANUALLY, widgetConfiguration.getSelectionMode());
		assertEquals("version-instance-id-1", widgetConfiguration.getSelectedObject());
	}

	@Test
	public void handle_selectionModeAutomatically_selectedObjects() {
		DataTableWidget widget = prepareMultipleInstancesAsResult(WidgetSelectionMode.AUTOMATICALLY);
		handler.handle(widget, prepareHandlerContext());
		WidgetConfiguration widgetConfiguration = widget.getConfiguration();
		assertEquals(WidgetSelectionMode.MANUALLY, widgetConfiguration.getSelectionMode());
		assertEquals(2, widgetConfiguration.getSelectedObjects().size());
	}

	@Test
	public void handle_selectionModeAutomatically_noSelectedObject_withSingleSelection() {
		DataTableWidget widget = prepareSingleInstanecAsResult(WidgetSelectionMode.AUTOMATICALLY);
		widget.getConfiguration().getConfiguration().addProperty("selection", "single");
		handler.handle(widget, prepareHandlerContext());
		WidgetConfiguration widgetConfiguration = widget.getConfiguration();
		assertEquals(WidgetSelectionMode.MANUALLY, widgetConfiguration.getSelectionMode());
		assertEquals("version-instance-id-1", widgetConfiguration.getSelectedObject());
	}

	@Test
	public void handle_selectionModeAutomatically_noSelectedObjects_withMultipleSelection() {
		DataTableWidget widget = prepareMultipleInstancesAsResult(WidgetSelectionMode.AUTOMATICALLY);
		widget.getConfiguration().getConfiguration().addProperty("selection", "multiple");
		handler.handle(widget, prepareHandlerContext());
		WidgetConfiguration widgetConfiguration = widget.getConfiguration();
		assertEquals(WidgetSelectionMode.MANUALLY, widgetConfiguration.getSelectionMode());
		assertEquals(2, widgetConfiguration.getSelectedObjects().size());
	}

	@Test
	public void handle_selectionModeAutomatically_selectedObject() {
		DataTableWidget widget = prepareSingleInstanecAsResult(WidgetSelectionMode.AUTOMATICALLY);
		handler.handle(widget, prepareHandlerContext());
		WidgetConfiguration widgetConfiguration = widget.getConfiguration();
		assertEquals(WidgetSelectionMode.MANUALLY, widgetConfiguration.getSelectionMode());
		assertEquals("version-instance-id-1", widgetConfiguration.getSelectedObject());
	}

	private DataTableWidget prepareSingleInstanecAsResult(WidgetSelectionMode selectionMode) {
		Map<Serializable, Serializable> daoResults = new HashMap<>();
		daoResults.put("instance-id-1", "version-instance-id-1");
		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollection(), any(Date.class))).thenReturn(daoResults);

		Element node = new Element(Tag.valueOf("section"), "");
		node.attr("config", "e30=");
		node.attr("id", "widget-id");
		DataTableWidget widget = new DataTableWidget(node);
		WidgetConfiguration configuration = widget.getConfiguration();
		List<String> ids = Arrays.asList("instance-id-1");
		configuration.setSearchResults(WidgetResults.fromSearch(ids));
		configuration.setSelectionMode(selectionMode);
		configuration.setSelectedObject("instance-id-1");
		return widget;
	}

	private DataTableWidget prepareMultipleInstancesAsResult(WidgetSelectionMode selectionMode) {
		Map<Serializable, Serializable> daoResults = new HashMap<>();
		daoResults.put("instance-id-1", "version-instance-id-1");
		daoResults.put("instance-id-2", "version-instance-id-2");
		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollection(), any(Date.class))).thenReturn(daoResults);

		Element node = new Element(Tag.valueOf("section"), "");
		node.attr("config", "e30=");
		node.attr("id", "widget-id");
		DataTableWidget widget = new DataTableWidget(node);
		WidgetConfiguration configuration = widget.getConfiguration();
		List<String> ids = Arrays.asList("instance-id-1", "instance-id-2");
		configuration.setSearchResults(WidgetResults.fromSearch(ids));
		configuration.setSelectionMode(selectionMode);
		configuration.setSelectedObjects(new ArrayList<>(ids));
		return widget;
	}

	private HandlerContext prepareHandlerContext() {
		HandlerContext context = new HandlerContext();
		Date date = new Date();
		context.put("versionCreationDate", date);
		context.put("versionedInstancesCache", new VersionIdsCache(date, versionDao::findVersionIdsByTargetIdAndDate));
		return context;
	}

	@Test
	public void storeConfigurationDiff_withAdditionalHandlerProperty() {
		WidgetMock widget = new WidgetMock(new WidgetConfiguration(null, new JsonObject()));
		WidgetConfiguration configuration = widget.getConfiguration();
		configuration.addNotNullProperty("addedByHandler", new JsonPrimitive("value"));
		HashMap<String, JsonElement> originalConfigurationMap = new HashMap<>();
		handler.storeConfigurationDiff(originalConfigurationMap, configuration);

		assertFalse(originalConfigurationMap.isEmpty());
		assertTrue(originalConfigurationMap.containsKey("additionalChanges"));

		JsonElement element = originalConfigurationMap.get("additionalChanges");
		assertTrue(element.isJsonArray());
		assertEquals("addedByHandler", element.getAsJsonArray().get(0).getAsString());
	}

	@Test
	public void extractResults_resultAreFoundBySearch_searchServiceNotCalled() {
		Set<String> ids = Collections.singleton("instance-id");
		Collection<String> extractedResults = handler.extractResults(true, () -> ids);
		assertEquals(ids, extractedResults);
		verifyZeroInteractions(instanceService);
	}

	@Test
	public void extractResults_noResults_searchServiceNotCalled() {
		Collection<Serializable> extractedResults = handler.extractResults(false, () -> emptySet());
		assertTrue(extractedResults.isEmpty());
		verifyZeroInteractions(instanceService);
	}

	@Test
	public void extractResults_resultAreNotFoundBySearch_searchServiceCalled() {
		List<String> ids = Arrays.asList("instance-id-1", "instance-id-2");
		when(instanceService.exist(anyCollectionOf(Serializable.class)))
				.thenReturn(new InstanceExistResult<>(emptyMap()));
		handler.extractResults(false, () -> ids);
		verify(instanceService).exist(anyCollectionOf(Serializable.class));
	}
}