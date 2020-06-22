package com.sirma.sep.export.renders;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for ContentViewerWidget.
 * 
 * @author Hristo Lungov
 */
public class ContentViewerWidgetRendererTest {

	private static final String TEST_FILE_SELECTED_MODE_CURRENT = "content-viewer-widget-current-mode.json";
	private static final String TEST_FILE_SELECTED_MODE_AUTOMATICALLY = "content-viewer-widget-automatic-mode.json";
	private static final String TEST_FILE_SELECTED_MODE_MANUALLY = "content-viewer-widget-manual-mode.json";
	private static final String TEST_FILE_UNDEFINED_SEARCH_CRITERIA = "content-viewer-undefined-search-criteria.json";
	private static final String TEST_FILE_PDF = "test.pdf";
	private static final String TEST_FILE_JPG = "test.jpg";

	private static final String WARN_MESSAGE_NO_PREVIEW_AVAILABLE = "No preview available.";
	private static final String WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND = "No object could be found with the selected search criteria.";
	private static final String WARN_MESSAGE_MORE_THAN_ONE = "More than one objects are found which satisfy the search criteria. Please select \"Configure widget\" action and chose only one of the objects in the search result.";

	private static final String INSTANCE_TEST_ID = "instanceTestId";
	private static final String WIDGET_TITLE = "Content viewer widget";

	@Mock
	private SearchService searchService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Spy
	private JsonToConditionConverter converter = new JsonToConditionConverter();

	@InjectMocks
	private ContentViewerWidgetRenderer contentViewerWidget;

	/**
	 * Runs before each method and setup mockito.
	 * 
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA)).thenReturn(WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
	}

	@Test
	public void should_BuildUndefinedCriteriaTable_When_WidgetConfigurationHasNotSearchCriteria()
			throws URISyntaxException, IOException {
		WidgetNode widgetTest = new WidgetNodeBuilder().setConfiguration(TEST_FILE_UNDEFINED_SEARCH_CRITERIA).build();

		Element table = contentViewerWidget.render("instance-id", widgetTest);

		org.junit.Assert.assertEquals(WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA, table.text());
	}

	@Test
	@SuppressWarnings({ "boxing", "unchecked" })
	public void should_ReturnHtmlTable_When_SelectedObjectModeIsCurrentWithNoPreviewAvailable() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_CURRENT).build();
		Instance instance = Mockito.mock(Instance.class);
		when(instance.isUploaded()).thenReturn(Boolean.FALSE);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_PREVIEW_AVAILABLE)).thenReturn(WARN_MESSAGE_NO_PREVIEW_AVAILABLE);

		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());
		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(1, Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue());
		Assert.assertEquals(WARN_MESSAGE_NO_PREVIEW_AVAILABLE, tableMessageRow.text());
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	public void should_ReturnHtmlTable_When_SelectedObjectIsAutomaticallyWithMoreThanOneSelectedInstances() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_AUTOMATICALLY).build();
		Instance instance1 = Mockito.mock(Instance.class);
		Instance instance2 = Mockito.mock(Instance.class);
		List<Instance> instances = Arrays.asList(instance1, instance2);
		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);
		when(searchArgs.getResult()).thenReturn(instances);
		when(searchArgs.getStringQuery()).thenReturn("");
		Sorter sorter = Mockito.mock(Sorter.class);
		List<Sorter> sorters = new ArrayList<>(1);
		sorters.add(sorter);
		when(searchArgs.getSorters()).thenReturn(sorters);
		when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(instances);
		when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_MORE_THAN_ONE)).thenReturn(WARN_MESSAGE_MORE_THAN_ONE);

		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());
		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(1, Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue());
		Assert.assertEquals(WARN_MESSAGE_MORE_THAN_ONE, tableMessageRow.text());
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	public void should_ReturnHtmlTable_When_SelectedObjectAutomaticallyButNoneFoundInstance() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_AUTOMATICALLY).build();
		List<Instance> instances = Collections.emptyList();
		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);
		when(searchArgs.getResult()).thenReturn(instances);
		when(searchArgs.getStringQuery()).thenReturn("");
		Sorter sorter = Mockito.mock(Sorter.class);
		List<Sorter> sorters = new ArrayList<>(1);
		sorters.add(sorter);
		when(searchArgs.getSorters()).thenReturn(sorters);
		when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(instances);
		when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECT_OBJECT_RESULTS_NONE)).thenReturn(WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND);

		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());
		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(1, Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue());
		Assert.assertEquals(WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND, tableMessageRow.text());
	}

	@Test
	@SuppressWarnings({ "unchecked", "boxing" })
	public void should_ReturnHtmlTable_When_SelectedObjectIsManuallyWithNoContentAvailable() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_MANUALLY).build();
		Instance instance = Mockito.mock(Instance.class);
		when(instance.isUploaded()).thenReturn(Boolean.TRUE);
		when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		when(instanceContentService.getContentPreview(INSTANCE_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(ContentInfo.DO_NOT_EXIST);
		when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_PREVIEW_AVAILABLE)).thenReturn(WARN_MESSAGE_NO_PREVIEW_AVAILABLE);

		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());
		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(1, Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue());
		Assert.assertEquals(WARN_MESSAGE_NO_PREVIEW_AVAILABLE, tableMessageRow.text());
	}

	/**
	 * Test render where selected object mode is manually successful test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings({ "unchecked", "boxing" })
	public void should_RenderSuccessfully_When_SelectedObjectIsPdf() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_MANUALLY).build();
		try (InputStream testIS = getClass().getClassLoader().getResourceAsStream(TEST_FILE_PDF)) {
			Instance instance = Mockito.mock(Instance.class);
			when(instance.isUploaded()).thenReturn(Boolean.TRUE);
			when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
			when(contentInfo.exists()).thenReturn(Boolean.TRUE);
			when(contentInfo.getInputStream()).thenReturn(testIS);
			when(contentInfo.getMimeType()).thenReturn("application/pdf");
			when(contentInfo.getName()).thenReturn(TEST_FILE_PDF);
			when(instanceContentService.getContentPreview(INSTANCE_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);

			// first render
			Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);

			Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
			Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
			Assert.assertEquals(1, Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue());
			Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());

			Elements imageTag = table.select("tr:eq(1) > td:eq(0) img");
			Assert.assertEquals(TEST_FILE_PDF, imageTag.attr(JsoupUtil.ATTRIBUTE_ALT));
			Assert.assertEquals(TEST_FILE_PDF, imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE));

			String style = imageTag.attr(JsoupUtil.ATTRIBUTE_STYLE);
			Assert.assertTrue(style.contains("max-width:75% !important;"));
			Assert.assertTrue(style.contains("max-height:75% !important;"));

		}
		// second render for IO Exception
		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());
		Elements imageTag = table.select("tr:eq(1) > td:eq(0) img");
		Assert.assertTrue(imageTag.size() == 0);
	}

	@Test
	public void should_RenderSuccessfully_When_SelectedObjectIsImage() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_MANUALLY).build();
		InputStream testIS = getClass().getClassLoader().getResourceAsStream(TEST_FILE_JPG);
		Instance instance = Mockito.mock(Instance.class);
		when(instance.isUploaded()).thenReturn(Boolean.TRUE);
		when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentInfo.getInputStream()).thenReturn(testIS);
		when(contentInfo.getMimeType()).thenReturn("image/jpeg");
		when(contentInfo.getName()).thenReturn(TEST_FILE_JPG);
		when(instanceContentService.getContentPreview(INSTANCE_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(
				contentInfo);

		// first render
		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(1, Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue());
		Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());

		Elements imageTag = table.select("tr:eq(1) > td:eq(0) img");
		Assert.assertEquals(TEST_FILE_JPG, imageTag.attr(JsoupUtil.ATTRIBUTE_ALT));
		Assert.assertEquals(TEST_FILE_JPG, imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE));

		String style = imageTag.attr(JsoupUtil.ATTRIBUTE_STYLE);
		Assert.assertTrue(style.contains("max-width:75% !important;"));
		Assert.assertTrue(style.contains("max-height:75% !important;"));

	}

	/**
	 * Accept method test.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void acceptTest() {
		WidgetNode widget = Mockito.mock(WidgetNode.class);
		when(widget.isWidget()).thenReturn(false);
		when(widget.getName()).thenReturn("");
		Assert.assertFalse(contentViewerWidget.accept(widget));

		when(widget.isWidget()).thenReturn(true);
		when(widget.getName()).thenReturn("");
		Assert.assertFalse(contentViewerWidget.accept(widget));

		when(widget.isWidget()).thenReturn(false);
		when(widget.getName()).thenReturn(ContentViewerWidgetRenderer.CONTENT_VIEWER_WIDGET_NAME);
		Assert.assertFalse(contentViewerWidget.accept(widget));

		when(widget.isWidget()).thenReturn(true);
		when(widget.getName()).thenReturn(ContentViewerWidgetRenderer.CONTENT_VIEWER_WIDGET_NAME);
		Assert.assertTrue(contentViewerWidget.accept(widget));
	}
}
