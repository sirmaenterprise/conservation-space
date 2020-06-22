package com.sirma.sep.export.renders;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.export.renders.CommentsWidgetRenderer;
import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.utils.JsoupUtil;
import com.sirma.sep.export.services.HtmlTableAnnotationService;

/**
 * Tests for CommentsWidgetRenderer.
 *
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
@RunWith(DataProviderRunner.class)
public class CommentsWidgetRendererTest {

	@Mock
	private HtmlTableAnnotationService htmlTableAnnotationService;

	@Mock
	private AnnotationService annotationService;

	@Spy
	private JsonToConditionConverter jsonToConditionConverter = new JsonToConditionConverter();

	@Mock
	protected SearchService searchService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private CommentsWidgetRenderer commentsWidgetRenderer;

	@Spy
	private JsonToConditionConverter converter = new JsonToConditionConverter();

	private static final String CURRENT_INSTANCE_ID = "current-instance-id";
	private static final String TEST_FILE_UNDEFINED_SEARCH_CRITERIA = "comment-widget-undefined-search-criteria.json";
	private static final String TEST_FILE_WITHOUT_SELECTED_OBJECT = "comment-widget-without-selected-object.json";
	private static final String TEST_FILE_NOT_FOUND_ANNOTATION = "comment-widget-not-found-annotation.json";
	private static final String TEST_FILE_MANUAL_WITHOUT_SLECTION = "comment-widget-manual-without-selection.json";
	private static final String TEST_FILE_NOT_MANUAL_SELECTION_MODE = "comment-widget-not-manual-selection-mode.json";
	private static final String TEST_FILE_MANUAL_SELECTION_MODE = "comment-widget-manual-selection-mode.json";
	private static final String TEST_FILE_WITHOUT_SELECTED_CURRENT_OBJECT = "comment-widget-without-selected-current-object.json";

	/**
	 * Runs Before method init.
	 */
	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_COMMENTS)).thenReturn(
				"There are no comments for the selected objects and filters");
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA)).thenReturn(WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
	}

	@Test
	public void should_BuildUndefinedCriteriaTable_When_WidgetConfigurationHasNotSearchCriteria()
			throws URISyntaxException, IOException {
		WidgetNode widgetTest = new WidgetNodeBuilder().setConfiguration(TEST_FILE_UNDEFINED_SEARCH_CRITERIA).build();

		Element table = commentsWidgetRenderer.render("instance-id", widgetTest);

		org.junit.Assert.assertEquals(WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA, table.text());
	}

	/**
	 * Tests scenario selectedObjects null
	 */
	@Test
	public void should_BuildHtmlTable_When_ConfigurationIsWithoutSelectedObjects() throws URISyntaxException, IOException {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String currentInstanceId = CURRENT_INSTANCE_ID;

		WidgetNode node = new WidgetNodeBuilder().setConfiguration(TEST_FILE_WITHOUT_SELECTED_OBJECT).build();

		javax.json.JsonObject json = IdocRenderer.toJson(node.getConfiguration());
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");



		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		Mockito.doReturn(Arrays.asList()).when(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(
				currentInstanceId, json, false);

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(Matchers.any(HtmlTableBuilder.class), Matchers.eq(Arrays.asList(annotation))))
					.thenReturn(htmlTableBuilder);
		Mockito.doReturn(Arrays.asList(annotation)).when(annotationService).searchAnnotations(
				Matchers.any(AnnotationSearchRequest.class));

		spyOfcommentsWidgetRenderer.render(currentInstanceId, node);

		Mockito.verify(jsonToDateRangeConverter).convertDateRange(filterCriteria);
		Mockito.verify(htmlTableBuilder).build();
		ArgumentCaptor<AnnotationSearchRequest> annotationSearchRequest = ArgumentCaptor
				.forClass(AnnotationSearchRequest.class);
		Mockito.verify(annotationService).searchAnnotations(annotationSearchRequest.capture());
		AnnotationSearchRequest actualArgument = annotationSearchRequest.getValue();
		Assert.assertEquals(dateRange, actualArgument.getDateRange());
		Assert.assertEquals(Arrays.asList(CURRENT_INSTANCE_ID), actualArgument.getInstanceIds());
		Assert.assertEquals(Collections.emptyList(), actualArgument.getUserIds());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Mockito.verify(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(currentInstanceId, json, false);
	}

	/**
	 * Tests scenario no annotations found
	 */
	@Test
	public void should_buildHtmlTable_When_AnnotationAreNotFound() throws URISyntaxException, IOException {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";
		String currentInstanceId = CURRENT_INSTANCE_ID;

		WidgetNode node = new WidgetNodeBuilder().setConfiguration(TEST_FILE_NOT_FOUND_ANNOTATION).build();

		javax.json.JsonObject json = IdocRenderer.toJson(node.getConfiguration());
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_COMMENTS)).thenReturn(
				IdocRenderer.KEY_LABEL_NO_COMMENTS);

		Mockito.doReturn(Collections.emptyList()).when(annotationService).searchAnnotations(
				Matchers.any(AnnotationSearchRequest.class));

		Element commentsWidgetTable = spyOfcommentsWidgetRenderer.render(currentInstanceId, node);

		Mockito.verify(jsonToDateRangeConverter).convertDateRange(filterCriteria);
		ArgumentCaptor<AnnotationSearchRequest> annotationSearchRequest = ArgumentCaptor
				.forClass(AnnotationSearchRequest.class);
		Mockito.verify(annotationService).searchAnnotations(annotationSearchRequest.capture());
		AnnotationSearchRequest actualArgument = annotationSearchRequest.getValue();
		Assert.assertEquals(dateRange, actualArgument.getDateRange());
		Assert.assertEquals(Arrays.asList(CURRENT_INSTANCE_ID), actualArgument.getInstanceIds());
		Assert.assertEquals(Collections.emptyList(), actualArgument.getUserIds());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Assert.assertEquals(tableTitle, commentsWidgetTable.select("tr:eq(0) td:eq(0) p").text());
		Assert.assertEquals(IdocRenderer.KEY_LABEL_NO_COMMENTS, commentsWidgetTable.select("tr:eq(1) td:eq(0) p").text());
	}

	/**
	 * Tests scenario selectObjectMode -> blank manual
	 */
	@Test
	public void should_BuildHtmlTable_When_ManualSelectionWithoutSelection() throws URISyntaxException, IOException {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String currentInstanceId = CURRENT_INSTANCE_ID;

		WidgetNode node = new WidgetNodeBuilder().setConfiguration(TEST_FILE_MANUAL_WITHOUT_SLECTION).build();

		javax.json.JsonObject json = IdocRenderer.toJson(node.getConfiguration());
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		Mockito.doReturn(Arrays.asList()).when(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(
				currentInstanceId, json, false);

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(Matchers.any(HtmlTableBuilder.class), Matchers.eq(Arrays.asList(annotation))))
					.thenReturn(htmlTableBuilder);
		Mockito.doReturn(Arrays.asList(annotation)).when(annotationService).searchAnnotations(
				Matchers.any(AnnotationSearchRequest.class));

		spyOfcommentsWidgetRenderer.render(currentInstanceId, node);

		Mockito.verify(jsonToDateRangeConverter).convertDateRange(filterCriteria);
		Mockito.verify(htmlTableBuilder).build();
		ArgumentCaptor<AnnotationSearchRequest> annotationSearchRequest = ArgumentCaptor
				.forClass(AnnotationSearchRequest.class);
		Mockito.verify(annotationService).searchAnnotations(annotationSearchRequest.capture());
		AnnotationSearchRequest actualArgument = annotationSearchRequest.getValue();
		Assert.assertEquals(dateRange, actualArgument.getDateRange());
		Assert.assertEquals(Arrays.asList(CURRENT_INSTANCE_ID), actualArgument.getInstanceIds());
		Assert.assertEquals(Collections.emptyList(), actualArgument.getUserIds());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Mockito.verify(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(currentInstanceId, json, false);
	}

	/**
	 * Tests scenario selectObjectMode -> not manual
	 */
	@Test
	public void should_BuildHtmlTable_When_ConfigurationIsWitnNotManualSelectObjectMode() throws URISyntaxException, IOException {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String currentInstanceId = CURRENT_INSTANCE_ID;

		WidgetNode node = new WidgetNodeBuilder().setConfiguration(TEST_FILE_NOT_MANUAL_SELECTION_MODE).build();

		javax.json.JsonObject json = IdocRenderer.toJson(node.getConfiguration());
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		Mockito.doReturn(Arrays.asList()).when(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(
				currentInstanceId, json, false);

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(Matchers.any(HtmlTableBuilder.class), Matchers.eq(Arrays.asList(annotation))))
				.thenReturn(htmlTableBuilder);
		Mockito.doReturn(Arrays.asList(annotation)).when(annotationService).searchAnnotations(
				Matchers.any(AnnotationSearchRequest.class));

		spyOfcommentsWidgetRenderer.render(currentInstanceId, node);

		Mockito.verify(jsonToDateRangeConverter).convertDateRange(filterCriteria);
		Mockito.verify(htmlTableBuilder).build();
		ArgumentCaptor<AnnotationSearchRequest> annotationSearchRequest = ArgumentCaptor
				.forClass(AnnotationSearchRequest.class);
		Mockito.verify(annotationService).searchAnnotations(annotationSearchRequest.capture());
		AnnotationSearchRequest actualArgument = annotationSearchRequest.getValue();
		Assert.assertEquals(dateRange, actualArgument.getDateRange());
		Assert.assertEquals(Arrays.asList(CURRENT_INSTANCE_ID), actualArgument.getInstanceIds());
		Assert.assertEquals(Collections.emptyList(), actualArgument.getUserIds());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Mockito.verify(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(currentInstanceId, json, false);
	}

	/**
	 * Tests scenario selectObjectMode -> manual
	 */
	@Test
	public void should_BuildHtmlTableBuilder_When_ManualSelectObjectModeIsConfigured() throws URISyntaxException, IOException {
		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);

		WidgetNode node = new WidgetNodeBuilder().setConfiguration(TEST_FILE_MANUAL_SELECTION_MODE).build();

		javax.json.JsonObject json = IdocRenderer.toJson(node.getConfiguration());
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		List<Serializable> jsonToList = JSON.jsonToList(json.getJsonArray(IdocRenderer.SELECTED_OBJECTS));
		Mockito.when(instanceResolver.resolveReferences(jsonToList)).thenReturn(Collections.emptyList());

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(Matchers.any(HtmlTableBuilder.class), Matchers.eq(Arrays.asList(annotation))))
					.thenReturn(htmlTableBuilder);
		Mockito.doReturn(Arrays.asList(annotation)).when(annotationService).searchAnnotations(
				Matchers.any(AnnotationSearchRequest.class));
		spyOfcommentsWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Mockito.verify(instanceResolver).resolveReferences(jsonToList);
		Mockito.verify(jsonToDateRangeConverter).convertDateRange(filterCriteria);
		Mockito.verify(htmlTableBuilder).build();
		ArgumentCaptor<AnnotationSearchRequest> annotationSearchRequest = ArgumentCaptor
				.forClass(AnnotationSearchRequest.class);
		Mockito.verify(annotationService).searchAnnotations(annotationSearchRequest.capture());
		AnnotationSearchRequest actualArgument = annotationSearchRequest.getValue();
		Assert.assertEquals(dateRange, actualArgument.getDateRange());
		Assert.assertEquals(Arrays.asList(CURRENT_INSTANCE_ID), actualArgument.getInstanceIds());
		Assert.assertEquals(Collections.emptyList(), actualArgument.getUserIds());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());
		Mockito.verify(instanceResolver).resolveReferences(Arrays.asList("emf:admin-t4.ui2"));
	}

	/**
	 * Tests scenario without selectCurrentObject.
	 */
	@Test
	public void should_BuildHtmlTable_When_ConfigurationIsWithoutSelectCurrentObject() throws URISyntaxException, IOException {
		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";

		WidgetNode node = new WidgetNodeBuilder().setConfiguration(TEST_FILE_WITHOUT_SELECTED_CURRENT_OBJECT).build();

		javax.json.JsonObject json = IdocRenderer.toJson(node.getConfiguration());
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		List<Serializable> jsonToList = JSON.jsonToList(json.getJsonArray(IdocRenderer.SELECTED_OBJECTS));
		Mockito.when(instanceResolver.resolveReferences(jsonToList)).thenReturn(Collections.emptyList());

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(tableTitle, Arrays.asList(annotation)))
					.thenReturn(htmlTableBuilder);
		Mockito.doReturn(Arrays.asList(annotation)).when(annotationService).searchAnnotations(
				Matchers.any(AnnotationSearchRequest.class));

		Element render = spyOfcommentsWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.text(), "title of table There are no comments for the selected objects and filters");

		Assert.assertEquals(2, render.select("tr").size());
		Assert.assertEquals("1", render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, render.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals("title of table", render.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals("text-align: center; color: #a94442;", render.select("tr:eq(1) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE));
		Assert.assertEquals("There are no comments for the selected objects and filters", render.select("tr:eq(1) td:eq(0) p").text());

	}

	@Test
	public void should_ReturnTrue_WhenNodeIsAcceptedByCommentWidget() {
		WidgetNode node = new WidgetNodeBuilder().setIsWidget(true).setName(CommentsWidgetRenderer.COMMENTS_WIDGET_NAME).build();
		Assert.assertTrue(commentsWidgetRenderer.accept(node));

	}

	@Test
	@UseDataProvider("notAcceptDP")
	public void should_ReturnFalse_WhenNodeIsNotAcceptedByCommentWidget(ContentNode node, String errorMessage) {
		Assert.assertFalse(errorMessage, commentsWidgetRenderer.accept(node));

	}

	/**
	 * Data provider for accepTest;.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public static Object[][] notAcceptDP() {
		Widget nonWidget = new WidgetNodeBuilder().setIsWidget(false).build();
		Widget nonCommentsWidget = new WidgetNodeBuilder().setIsWidget(true).setName("non comments widget").build();
		return new Object[][] { { null, Boolean.FALSE, "test with null node" },
				{ nonWidget, Boolean.FALSE, "test with node which is not witdget" },
				{ nonCommentsWidget, Boolean.FALSE, "test with node which is not comments witdget" }
		};
	}
}