package com.sirma.itt.seip.export.renders;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonParser;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.content.idoc.nodes.WidgetNode;

/**
 * Tests for ContentViewerWidget.
 * 
 * @author Hristo Lungov
 */
public class ContentViewerWidgetTest {

	private static final String TEST_FILE_SELECTED_MODE_CURRENT = "content-viewer-widget-current-mode.json";
	private static final String TEST_FILE_SELECTED_MODE_AUTOMATICALLY = "content-viewer-widget-automatic-mode.json";
	private static final String TEST_FILE_SELECTED_MODE_MANUALLY = "content-viewer-widget-manual-mode.json";
	private static final String TEST_FILE_PDF = "test.pdf";

	private static final String WARN_MESSAGE_NO_PREVIEW_AVAILABLE = "No preview available.";
	private static final String WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND = "No object could be found with the selected search criteria.";
	private static final String WARN_MESSAGE_MORE_THAN_ONE = "More than one objects are found which satisfy the search criteria. Please select \"Configure widget\" action and chose only one of the objects in the search result.";

	private static final String INSTANCE_TEST_ID = "instanceTestId";
	private static final String WIDGET_TITLE = "Content viewer widget";

	@InjectMocks
	private ContentViewerWidget contentViewerWidget;

	@Mock
	private SearchService searchService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private LabelProvider labelProvider;

	@Spy
	private JsonToConditionConverter convertor = new JsonToConditionConverter();

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	/**
	 * Runs before each method and setup mockito.
	 * 
	 * @throws URISyntaxException
	 */
	@BeforeMethod
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
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
		when(widget.getName()).thenReturn(ContentViewerWidget.CONTENT_VIEWER_WIDGET_NAME);
		Assert.assertFalse(contentViewerWidget.accept(widget));

		when(widget.isWidget()).thenReturn(true);
		when(widget.getName()).thenReturn(ContentViewerWidget.CONTENT_VIEWER_WIDGET_NAME);
		Assert.assertTrue(contentViewerWidget.accept(widget));
	}

	/**
	 * Load test resource.
	 *
	 * @param resource
	 *            the resource
	 * @return the com.google.gson. json object
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private com.google.gson.JsonObject loadTestResource(String resource) throws URISyntaxException, IOException {
		URL testJsonURL = getClass().getClassLoader().getResource(resource);
		File jsonConfiguration = new File(testJsonURL.toURI());
		try (FileReader fileReader = new FileReader(jsonConfiguration)) {
			return new JsonParser().parse(fileReader).getAsJsonObject();
		}
	}

	/**
	 * Test render where selected object mode is current with no preview available.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings({ "boxing", "unchecked" })
	private void renderSelectedObjectCurrentWithNoPreview() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_CURRENT);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

		Instance instance = Mockito.mock(Instance.class);
		when(instance.isUploaded()).thenReturn(Boolean.FALSE);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));

		when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_PREVIEW_AVAILABLE)).thenReturn(WARN_MESSAGE_NO_PREVIEW_AVAILABLE);

		// first render
		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Assert.assertEquals(tableMessageRow.text(), WARN_MESSAGE_NO_PREVIEW_AVAILABLE);
	}

	/**
	 * Test render where selected object mode is automatically with more than selected instances.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings({ "unchecked" })
	private void renderSelectedObjectAutomaticallyWithMoreThanOneSelected() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_AUTOMATICALLY);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

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

		// first render
		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Assert.assertEquals(tableMessageRow.text(), WARN_MESSAGE_MORE_THAN_ONE);
	}

	/**
	 * Test render where selected object mode is automatically but with no instances found.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings({ "unchecked" })
	private void renderSelectedObjectAutomaticallyButNoneFound() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_AUTOMATICALLY);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

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

		// first render
		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Assert.assertEquals(tableMessageRow.text(), WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND);
	}

	/**
	 * Test render where selected object mode is manually, but with no content available.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings({ "unchecked", "boxing" })
	private void renderSelectedObjectManuallyWithNoContentAvailable() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_MANUALLY);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

		Instance instance = Mockito.mock(Instance.class);
		when(instance.isUploaded()).thenReturn(Boolean.TRUE);
		when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		when(instanceContentService.getContentPreview(INSTANCE_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(ContentInfo.DO_NOT_EXIST);
		when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_PREVIEW_AVAILABLE)).thenReturn(WARN_MESSAGE_NO_PREVIEW_AVAILABLE);

		// first render
		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Assert.assertEquals(tableMessageRow.text(), WARN_MESSAGE_NO_PREVIEW_AVAILABLE);
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
	private void renderSelectedObjectManuallySuccessfull() throws URISyntaxException, IOException {
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		try (InputStream testIS = getClass().getClassLoader().getResourceAsStream(TEST_FILE_PDF);) {
			com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_MANUALLY);
			when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

			Instance instance = Mockito.mock(Instance.class);
			when(instance.isUploaded()).thenReturn(Boolean.TRUE);
			when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
			when(contentInfo.exists()).thenReturn(Boolean.TRUE);
			when(contentInfo.getInputStream()).thenReturn(testIS);
			when(contentInfo.getMimeType()).thenReturn(ContentViewerWidget.APPLICATION_PDF);
			when(contentInfo.getName()).thenReturn(TEST_FILE_PDF);

			when(instanceContentService.getContentPreview(INSTANCE_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
			// first render
			Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);
			Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
			Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
			Assert.assertEquals(Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
			Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

			Elements imageTag = table.select("tr:eq(1) > td:eq(0) img");
			Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILE_PDF);
			Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILE_PDF);

			String style = imageTag.attr(JsoupUtil.ATTRIBUTE_STYLE);
			Assert.assertTrue(style.contains("max-width:77% !important;"));
			Assert.assertTrue(style.contains("max-height:77% !important;"));

		}
		// second render for IO Exception
		Element table = contentViewerWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

		Elements imageTag = table.select("tr:eq(1) > td:eq(0) img");
		Assert.assertTrue(imageTag.size() == 0);
	}
}
