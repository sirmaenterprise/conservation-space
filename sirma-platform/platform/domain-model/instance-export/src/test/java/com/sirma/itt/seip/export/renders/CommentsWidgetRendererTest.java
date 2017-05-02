package com.sirma.itt.seip.export.renders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.time.DateRange;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirmaenterprise.sep.export.services.HtmlTableAnnotationService;

/**
 * Tests for CommentsWidgetRenderer.
 *
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
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

	/**
	 * Runs Before method init.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_COMMENTS)).thenReturn(
				"There are no comments for the selected objects and filters");
	}

	/**
	 * Tests scenario selectedObjects null
	 */
	@Test
	public void renderWithoutselectedObjectsTest() {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";
		String currentInstanceId = "current-instance-id";
		Integer limit = new Integer(10);
		Integer offset = new Integer(1);
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = new JsonParser()
				.parse("{\"title\": \"" + tableTitle
						+ "\",\"expanded\":true,\"selectObjectMode\":\"automatically\",\"criteria\":{\"condition\":\"OR\",\"rules\":[{\"id\":\"6618ca4e-3424-4481-d11e-b59a2a4655b5\",\"condition\":\"AND\",\"rules\":[{\"id\":\"41f4511a-1d14-447b-8d4c-b5a06632f811\",\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"anyObject\"]},{\"id\":\"95ce3a9c-0906-471a-b203-554dc979c81d\",\"condition\":\"AND\",\"rules\":[{\"id\":\"124e4e56-263d-486f-a43a-84df499a95b7\",\"field\":\"anyRelation\",\"type\":\"object\",\"operator\":\"set_to\",\"value\":[\"current_object\"]}]}]}],\"id\":\"749693c8-97e1-4f34-955e-d81eb7058659\"},\"size\":\""
						+ limit.intValue()
						+ "\",\"filterConfig\":{\"disabled\":false},\"filterCriteria\":{\"field\":\"emf:createdOn\",\"operator\":\"after\",\"type\":\"dateTime\",\"value\":\"2016-12-07T22:00:00.000Z\"},\"filterProperties\":[{\"id\":\"emf:createdOn\",\"text\":\"Created on\",\"type\":\"dateTime\"}],\"searchMode\":\"basic\",\"selectedUsers\":null,\"selectCurrentObject\":true}")
					.getAsJsonObject();
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);
		javax.json.JsonObject json = IdocRenderer.toJson(widgetConfiguration);
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		Mockito.doReturn(Arrays.asList()).when(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(
				currentInstanceId, json, false);

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(tableTitle, Arrays.asList(annotation)))
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
		Assert.assertEquals(actualArgument.getDateRange(), dateRange);
		Assert.assertEquals(actualArgument.getInstanceIds(), Arrays.asList("current-instance-id"));
		Assert.assertEquals(actualArgument.getUserIds(), Collections.emptyList());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Mockito.verify(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(currentInstanceId, json, false);
	}

	/**
	 * Tests scenario no annotations found
	 */
	@Test
	public void renderNoAnnotationsFoundTest() {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";
		String currentInstanceId = "current-instance-id";
		Integer limit = new Integer(10);
		Integer offset = new Integer(1);
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = new JsonParser()
				.parse("{\"title\": \"" + tableTitle
						+ "\",\"expanded\":true,\"selectObjectMode\":\"manually\",\"criteria\":{\"condition\":\"OR\",\"rules\":[{\"id\":\"6618ca4e-3424-4481-d11e-b59a2a4655b5\",\"condition\":\"AND\",\"rules\":[{\"id\":\"41f4511a-1d14-447b-8d4c-b5a06632f811\",\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"anyObject\"]},{\"id\":\"95ce3a9c-0906-471a-b203-554dc979c81d\",\"condition\":\"AND\",\"rules\":[{\"id\":\"124e4e56-263d-486f-a43a-84df499a95b7\",\"field\":\"anyRelation\",\"type\":\"object\",\"operator\":\"set_to\",\"value\":[\"current_object\"]}]}]}],\"id\":\"749693c8-97e1-4f34-955e-d81eb7058659\"},\"size\":\""
						+ limit.intValue()
						+ "\",\"filterConfig\":{\"disabled\":false},\"filterCriteria\":{\"field\":\"emf:createdOn\",\"operator\":\"after\",\"type\":\"dateTime\",\"value\":\"2016-12-07T22:00:00.000Z\"},\"filterProperties\":[{\"id\":\"emf:createdOn\",\"text\":\"Created on\",\"type\":\"dateTime\"}],\"searchMode\":\"basic\",\"selectedUsers\":null,\"selectCurrentObject\":true}")
					.getAsJsonObject();
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);
		javax.json.JsonObject json = IdocRenderer.toJson(widgetConfiguration);
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
		Assert.assertEquals(actualArgument.getDateRange(), dateRange);
		Assert.assertEquals(actualArgument.getInstanceIds(), Arrays.asList("current-instance-id"));
		Assert.assertEquals(actualArgument.getUserIds(), Collections.emptyList());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Assert.assertEquals(commentsWidgetTable.select("tr:eq(0) td:eq(0) p").text(), tableTitle);
		Assert.assertEquals(commentsWidgetTable.select("tr:eq(1) td:eq(0) p").text(),
				IdocRenderer.KEY_LABEL_NO_COMMENTS);
	}

	/**
	 * Tests scenario selectObjectMode -> blank manual
	 */
	@Test
	public void renderBlankManualSelectObjectModeTest() {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";
		String currentInstanceId = "current-instance-id";
		Integer limit = new Integer(10);
		Integer offset = new Integer(1);
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = new JsonParser()
				.parse("{\"title\": \"" + tableTitle
						+ "\",\"expanded\":true,\"selectObjectMode\":\"\",\"criteria\":{\"condition\":\"OR\",\"rules\":[{\"id\":\"6618ca4e-3424-4481-d11e-b59a2a4655b5\",\"condition\":\"AND\",\"rules\":[{\"id\":\"41f4511a-1d14-447b-8d4c-b5a06632f811\",\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"anyObject\"]},{\"id\":\"95ce3a9c-0906-471a-b203-554dc979c81d\",\"condition\":\"AND\",\"rules\":[{\"id\":\"124e4e56-263d-486f-a43a-84df499a95b7\",\"field\":\"anyRelation\",\"type\":\"object\",\"operator\":\"set_to\",\"value\":[\"current_object\"]}]}]}],\"id\":\"749693c8-97e1-4f34-955e-d81eb7058659\"},\"size\":\""
						+ limit.intValue()
						+ "\",\"filterConfig\":{\"disabled\":false},\"filterCriteria\":{\"field\":\"emf:createdOn\",\"operator\":\"after\",\"type\":\"dateTime\",\"value\":\"2016-12-07T22:00:00.000Z\"},\"filterProperties\":[{\"id\":\"emf:createdOn\",\"text\":\"Created on\",\"type\":\"dateTime\"}],\"searchMode\":\"basic\",\"selectedUsers\":null,\"selectCurrentObject\":true,\"selectedObjects\":[\"emf:admin-t4.ui2\"]}")
					.getAsJsonObject();
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);
		javax.json.JsonObject json = IdocRenderer.toJson(widgetConfiguration);
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		Mockito.doReturn(Arrays.asList()).when(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(
				currentInstanceId, json, false);

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(tableTitle, Arrays.asList(annotation)))
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
		Assert.assertEquals(actualArgument.getDateRange(), dateRange);
		Assert.assertEquals(actualArgument.getInstanceIds(), Arrays.asList("current-instance-id"));
		Assert.assertEquals(actualArgument.getUserIds(), Collections.emptyList());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Mockito.verify(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(currentInstanceId, json, false);
	}

	/**
	 * Tests scenario selectObjectMode -> not manual
	 */
	@Test
	public void renderNotManualSelectObjectModeTest() {

		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";
		String currentInstanceId = "current-instance-id";
		Integer limit = new Integer(10);
		Integer offset = new Integer(1);
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = new JsonParser()
				.parse("{\"title\": \"" + tableTitle
						+ "\",\"expanded\":true,\"selectObjectMode\":\"not-manually\",\"criteria\":{\"condition\":\"OR\",\"rules\":[{\"id\":\"6618ca4e-3424-4481-d11e-b59a2a4655b5\",\"condition\":\"AND\",\"rules\":[{\"id\":\"41f4511a-1d14-447b-8d4c-b5a06632f811\",\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"anyObject\"]},{\"id\":\"95ce3a9c-0906-471a-b203-554dc979c81d\",\"condition\":\"AND\",\"rules\":[{\"id\":\"124e4e56-263d-486f-a43a-84df499a95b7\",\"field\":\"anyRelation\",\"type\":\"object\",\"operator\":\"set_to\",\"value\":[\"current_object\"]}]}]}],\"id\":\"749693c8-97e1-4f34-955e-d81eb7058659\"},\"size\":\""
						+ limit.intValue()
						+ "\",\"filterConfig\":{\"disabled\":false},\"filterCriteria\":{\"field\":\"emf:createdOn\",\"operator\":\"after\",\"type\":\"dateTime\",\"value\":\"2016-12-07T22:00:00.000Z\"},\"filterProperties\":[{\"id\":\"emf:createdOn\",\"text\":\"Created on\",\"type\":\"dateTime\"}],\"searchMode\":\"basic\",\"selectedUsers\":null,\"selectCurrentObject\":true,\"selectedObjects\":[\"emf:admin-t4.ui2\"]}")
					.getAsJsonObject();
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);
		javax.json.JsonObject json = IdocRenderer.toJson(widgetConfiguration);
		HtmlTableBuilder htmlTableBuilder = Mockito.mock(HtmlTableBuilder.class);
		DateRange dateRange = new DateRange(null, null);
		javax.json.JsonObject filterCriteria = json.getJsonObject("filterCriteria");
		Mockito.when(jsonToDateRangeConverter.convertDateRange(filterCriteria)).thenReturn(dateRange);
		Mockito.doReturn(Arrays.asList()).when(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(
				currentInstanceId, json, false);

		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito
				.when(htmlTableAnnotationService.createAnnotationTable(tableTitle, Arrays.asList(annotation)))
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
		Assert.assertEquals(actualArgument.getDateRange(), dateRange);
		Assert.assertEquals(actualArgument.getInstanceIds(), Arrays.asList("current-instance-id"));
		Assert.assertEquals(actualArgument.getUserIds(), Collections.emptyList());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());

		Mockito.verify(spyOfcommentsWidgetRenderer).getSelectedInstancesFromCriteria(currentInstanceId, json, false);
	}

	/**
	 * Tests scenario selectObjectMode -> manual
	 */
	@Test
	public void renderManualSelectObjectModeTest() {
		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";
		Integer limit = new Integer(10);
		Integer offset = new Integer(1);
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = new JsonParser()
				.parse("{\"title\": \"" + tableTitle
						+ "\",\"expanded\":true,\"selectObjectMode\":\"manually\",\"criteria\":{\"condition\":\"OR\",\"rules\":[{\"id\":\"6618ca4e-3424-4481-d11e-b59a2a4655b5\",\"condition\":\"AND\",\"rules\":[{\"id\":\"41f4511a-1d14-447b-8d4c-b5a06632f811\",\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"anyObject\"]},{\"id\":\"95ce3a9c-0906-471a-b203-554dc979c81d\",\"condition\":\"AND\",\"rules\":[{\"id\":\"124e4e56-263d-486f-a43a-84df499a95b7\",\"field\":\"anyRelation\",\"type\":\"object\",\"operator\":\"set_to\",\"value\":[\"current_object\"]}]}]}],\"id\":\"749693c8-97e1-4f34-955e-d81eb7058659\"},\"size\":\""
						+ limit.intValue()
						+ "\",\"filterConfig\":{\"disabled\":false},\"filterCriteria\":{\"field\":\"emf:createdOn\",\"operator\":\"after\",\"type\":\"dateTime\",\"value\":\"2016-12-07T22:00:00.000Z\"},\"filterProperties\":[{\"id\":\"emf:createdOn\",\"text\":\"Created on\",\"type\":\"dateTime\"}],\"searchMode\":\"basic\",\"selectedUsers\":null,\"selectCurrentObject\":true,\"selectedObjects\":[\"emf:admin-t4.ui2\"]}")
					.getAsJsonObject();
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);
		javax.json.JsonObject json = IdocRenderer.toJson(widgetConfiguration);
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
		spyOfcommentsWidgetRenderer.render("current-instance-id", node);

		Mockito.verify(instanceResolver).resolveReferences(jsonToList);
		Mockito.verify(jsonToDateRangeConverter).convertDateRange(filterCriteria);
		Mockito.verify(htmlTableBuilder).build();
		ArgumentCaptor<AnnotationSearchRequest> annotationSearchRequest = ArgumentCaptor
				.forClass(AnnotationSearchRequest.class);
		Mockito.verify(annotationService).searchAnnotations(annotationSearchRequest.capture());
		AnnotationSearchRequest actualArgument = annotationSearchRequest.getValue();
		Assert.assertEquals(actualArgument.getDateRange(), dateRange);
		Assert.assertEquals(actualArgument.getInstanceIds(), Arrays.asList("current-instance-id"));
		Assert.assertEquals(actualArgument.getUserIds(), Collections.emptyList());
		Assert.assertNull(actualArgument.getStatus());
		Assert.assertNull(actualArgument.getText());
		Mockito.verify(instanceResolver).resolveReferences(Arrays.asList("emf:admin-t4.ui2"));
	}

	/**
	 * Tests scenario without selectCurrentObject.
	 */
	@Test
	public void renderWithoutSelectCurrentObjectTest() {
		CommentsWidgetRenderer spyOfcommentsWidgetRenderer = Mockito.spy(commentsWidgetRenderer);
		String tableTitle = "title of table";
		Integer limit = new Integer(10);
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = new JsonParser()
				.parse("{\"title\": \"" + tableTitle
						+ "\",\"expanded\":true,\"selectObjectMode\":\"manually\",\"criteria\":{\"condition\":\"OR\",\"rules\":[{\"id\":\"6618ca4e-3424-4481-d11e-b59a2a4655b5\",\"condition\":\"AND\",\"rules\":[{\"id\":\"41f4511a-1d14-447b-8d4c-b5a06632f811\",\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"anyObject\"]},{\"id\":\"95ce3a9c-0906-471a-b203-554dc979c81d\",\"condition\":\"AND\",\"rules\":[{\"id\":\"124e4e56-263d-486f-a43a-84df499a95b7\",\"field\":\"anyRelation\",\"type\":\"object\",\"operator\":\"set_to\",\"value\":[\"current_object\"]}]}]}],\"id\":\"749693c8-97e1-4f34-955e-d81eb7058659\"},\"size\":\""
						+ limit.intValue()
						+ "\",\"filterConfig\":{\"disabled\":false},\"filterCriteria\":{\"field\":\"emf:createdOn\",\"operator\":\"after\",\"type\":\"dateTime\",\"value\":\"2016-12-07T22:00:00.000Z\"},\"filterProperties\":[{\"id\":\"emf:createdOn\",\"text\":\"Created on\",\"type\":\"dateTime\"}],\"searchMode\":\"basic\",\"selectedUsers\":null,\"selectedObjects\":[\"emf:admin-t4.ui2\"]}")
					.getAsJsonObject();
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);
		javax.json.JsonObject json = IdocRenderer.toJson(widgetConfiguration);
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

		Element render = spyOfcommentsWidgetRenderer.render("current-instance-id", node);

		Assert.assertEquals(render.text(), "title of table There are no comments for the selected objects and filters");

		Assert.assertEquals(render.select("tr").size(), 2);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").text(), "title of table");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE),
				"text-align: center; color: #a94442;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").text(),
				"There are no comments for the selected objects and filters");

	}

	/**
	 * Test method accept.
	 *
	 * @param node
	 *            the node parameter.
	 * @param expectedResult
	 *            expected result.
	 * @param errorMessage
	 *            message to be shown in log if test fail.
	 */
	@Test(dataProvider = "acceptDP")
	public void acceptTest(ContentNode node, boolean expectedResult, String errorMessage) {
		Assert.assertEquals(commentsWidgetRenderer.accept(node), expectedResult, errorMessage);

	}

	/**
	 * Data provider for accepTest;.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public Object[][] acceptDP() {
		Widget nonWidget = Mockito.mock(Widget.class);
		Mockito.when(nonWidget.isWidget()).thenReturn(Boolean.FALSE);

		Widget nonCommentsWidget = Mockito.mock(Widget.class);
		Mockito.when(nonCommentsWidget.isWidget()).thenReturn(Boolean.TRUE);
		Mockito.when(nonCommentsWidget.getName()).thenReturn("non comments widget");

		Widget commentsWidget = Mockito.mock(Widget.class);
		Mockito.when(commentsWidget.isWidget()).thenReturn(Boolean.TRUE);
		Mockito.when(commentsWidget.getName()).thenReturn(CommentsWidgetRenderer.COMMENTS_WIDGET_NAME);

		return new Object[][] { { null, Boolean.FALSE, "test with null node" },
				{ nonWidget, Boolean.FALSE, "test with node which is not witdget" },
				{ nonCommentsWidget, Boolean.FALSE, "test with node which is not comments witdget" },
				{ commentsWidget, Boolean.TRUE, "test with node which is comments witdget" } };
	}
}