package com.sirma.sep.export.renders;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for AggregateTableWidgetRenderer.
 *
 * @author Boyan Tonchev
 */
public class AggregateTableWidgetRendererTest {

	private static final int CODELIST_106 = 106;
	private static final int CODELIST_1 = 1;
	private static final String CURRENT_INSTANCE_ID = "current-instance-id=of-test-AggregateTableWidgetRendererTest";
	private static final String UI2_URL = "https://ses.sirmaplatform.com/#/idoc/";
	private static final String KEY_LABEL_SELECT_OBJECT_RESULTS_NONE_LABEL = "No object could be found with the selected search criteria.";
	private static final String KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL_LABEL = "Total Results: ";

	private static final String CODELIST_1_STATUS_OPEN = "open";
	private static final String CODELIST_1_STATUS_OPEN_LABEL = "Open";

	private static final String CODELIST_1_STATUS_CLOSED = "closed";
	private static final String CODELIST_1_STATUS_CLOSED_LABEL = "Closed label from 1";

	private static final String CODELIST_106_STATUS_CLOSED = "closed";
	private static final String CODELIST_106_STATUS_CLOSED_LABEL = "Closed label from 106";

	private static final String MANUALY_SELECTED_OBJECTS_CODELIST = "aggregate-widget-manualy-selected-objects-codelist.json";
	private static final String MANUALY_SELECTED_OBJECTS_CODELIST_WITH_TOTAL_RESULT_ROW = "aggregate-widget-manualy-selected-objects-codelist-with-total-row.json";
	private static final String AUTOMATICALLY_SELECTED_OBJECTS_CODELIST = "aggregate-widget-automatically-selected-object-codelist.json";
	private static final String AUTOMATICALLY_SELECTED_OBJECTS = "aggregate-widget-automatically-selected-object.json";
	private static final String AUTOMATICALLY_SELECTED_OBJECTS_NUMBER_FIRST = "aggregate-widget-automatically-selected-object-codelist-column-order-number-first.json";
	private static final String AUTOMATICALLY_SELECTED_OBJECTS_VALUE_FIRST = "aggregate-widget-automatically-selected-object-codelist-column-order-value-first.json";

	@Mock
	private CodelistService codelistService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private ConfigurationProperty<String> ui2Url;

	@Mock
	private JsonToConditionConverter convertor;

	@Mock
	private SearchService searchService;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Mock
	private UserPreferences userPreferences;

	@InjectMocks
	private AggregateTableWidgetRenderer aggregateTableWidgetRenderer;

	/**
	 * Runs Before method init.
	 */
	@BeforeMethod
	public void beforeClass() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>(UI2_URL));
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");

		CodeValue codelistOneStatusOpen = Mockito.mock(CodeValue.class);
		Mockito.when(codelistOneStatusOpen.getDescription(Matchers.any())).thenReturn(CODELIST_1_STATUS_OPEN_LABEL);
		Mockito.when(codelistService.getCodeValue(CODELIST_1, CODELIST_1_STATUS_OPEN)).thenReturn(
				codelistOneStatusOpen);

		CodeValue codelistOneStatusClosed = Mockito.mock(CodeValue.class);
		Mockito.when(codelistOneStatusClosed.getDescription(Matchers.any())).thenReturn(CODELIST_1_STATUS_CLOSED_LABEL);
		Mockito.when(codelistService.getCodeValue(CODELIST_1, CODELIST_1_STATUS_CLOSED)).thenReturn(
				codelistOneStatusClosed);

		CodeValue codelistOneHundredAndSixStatusClosed = Mockito.mock(CodeValue.class);
		Mockito.when(codelistOneHundredAndSixStatusClosed.getDescription(Matchers.any())).thenReturn(
				CODELIST_106_STATUS_CLOSED_LABEL);
		Mockito.when(codelistService.getCodeValue(CODELIST_106, CODELIST_106_STATUS_CLOSED)).thenReturn(
				codelistOneHundredAndSixStatusClosed);

		Map<String, CodeValue> codelistOne = new HashMap<>(2);
		codelistOne.put(CODELIST_1_STATUS_OPEN, codelistOneStatusOpen);
		codelistOne.put(CODELIST_1_STATUS_CLOSED, codelistOneStatusClosed);
		Mockito.when(codelistService.getCodeValues(CODELIST_1)).thenReturn(codelistOne);

		Map<String, CodeValue> codelistOneHundredAndSix = new HashMap<>(1);
		codelistOneHundredAndSix.put(CODELIST_106_STATUS_CLOSED, codelistOneHundredAndSixStatusClosed);
		Mockito.when(codelistService.getCodeValues(CODELIST_106)).thenReturn(codelistOneHundredAndSix);

		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECT_OBJECT_RESULTS_NONE)).thenReturn(
				KEY_LABEL_SELECT_OBJECT_RESULTS_NONE_LABEL);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL)).thenReturn(
				KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL_LABEL);
		Mockito.when(labelProvider.getLabel(AggregateTableWidgetRenderer.KEY_LABEL_NUMBER)).thenReturn(
				"Number");
	}

	/**
	 * Tests method render scenario column value first.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderValueFirstTest() throws URISyntaxException, IOException {

		String instanceIdOne = "instance-id-one";
		String instanceIdTwo = "instance-id-two";
		String srcOne = "image src one";
		String labelOne = "Instance one label";
		String hrefOne = "instance-one-href";
		String srcTwo = "image src two";
		String labelTwo = "Instance two label";
		String hrefTwo = "instance-two-href";

		Instance instanceOne = Mockito.mock(Instance.class);
		Mockito.when(instanceOne.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcOne
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefOne + "\"><span>" + labelOne + "</span></a></span>");
		Mockito.when(instanceOne.getId()).thenReturn(instanceIdOne);

		Instance instanceTwo = Mockito.mock(Instance.class);
		Mockito.when(instanceTwo.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcTwo
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefTwo + "\"><span>" + labelTwo + "</span></a></span>");
		Mockito.when(instanceTwo.getId()).thenReturn(instanceIdTwo);

		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(AUTOMATICALLY_SELECTED_OBJECTS_VALUE_FIRST);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(1);
		Map<String, Serializable> groupByResult = new HashMap<>();
		groupByResult.put(instanceIdOne, 2);
		groupByResult.put(instanceIdTwo, 3);

		result.put("emf:createdBy", groupByResult);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Mockito.when(instanceResolver.resolveInstances(Matchers.anyList())).thenReturn(
				Arrays.asList(instanceOne, instanceTwo));

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").text(), "Created by");

		Assert.assertEquals(render.text(),
				"Created by Number Instance two label 3 Instance one label 2 Total Results: 5");
		Assert.assertEquals(render.select("tr").size(), 6);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").text(), "Number");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) table span").size(), 3);
		Assert.assertEquals(
				render.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC),
				UI2_URL + srcTwo);
		Assert.assertEquals(
				render.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF),
				UI2_URL + hrefTwo);
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) span a").text(), labelTwo);

		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) p").text(), "3");

		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) table span").size(), 3);
		Assert.assertEquals(
				render.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC),
				UI2_URL + srcOne);
		Assert.assertEquals(
				render.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF),
				UI2_URL + hrefOne);
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").text(), labelOne);

		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) p").text(), "2");

		Assert.assertEquals(render.select("tr:eq(3) td").size(), 1);
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 15px;");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").text(),
				KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL_LABEL + "5");

	}

	/**
	 * Tests method render scenario column number first.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderNumberFirstTest() throws URISyntaxException, IOException {

		String instanceIdOne = "instance-id-one";
		String instanceIdTwo = "instance-id-two";
		String srcOne = "image src one";
		String labelOne = "Instance one label";
		String hrefOne = "instance-one-href";
		String srcTwo = "image src two";
		String labelTwo = "Instance two label";
		String hrefTwo = "instance-two-href";

		Instance instanceOne = Mockito.mock(Instance.class);
		Mockito.when(instanceOne.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcOne
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefOne + "\"><span>" + labelOne + "</span></a></span>");
		Mockito.when(instanceOne.getId()).thenReturn(instanceIdOne);

		Instance instanceTwo = Mockito.mock(Instance.class);
		Mockito.when(instanceTwo.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcTwo
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefTwo + "\"><span>" + labelTwo + "</span></a></span>");
		Mockito.when(instanceTwo.getId()).thenReturn(instanceIdTwo);

		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(AUTOMATICALLY_SELECTED_OBJECTS_NUMBER_FIRST);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(1);
		Map<String, Serializable> groupByResult = new HashMap<>();
		groupByResult.put(instanceIdOne, 2);
		groupByResult.put(instanceIdTwo, 3);

		result.put("emf:createdBy", groupByResult);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Mockito.when(instanceResolver.resolveInstances(Matchers.anyList())).thenReturn(
				Arrays.asList(instanceOne, instanceTwo));

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.text(),
				"Number Created by 3 Instance two label 2 Instance one label Total Results: 5");
		Assert.assertEquals(render.select("tr").size(), 6);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p strong").text(), "Number");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p strong").text(), "Created by");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").text(), "3");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) table span").size(), 3);
		Assert.assertEquals(
				render.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC),
				UI2_URL + srcTwo);
		Assert.assertEquals(
				render.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF),
				UI2_URL + hrefTwo);
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").text(), labelTwo);

		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) p").text(), "2");

		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) table span").size(), 3);
		Assert.assertEquals(
				render.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC),
				UI2_URL + srcOne);
		Assert.assertEquals(
				render.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF),
				UI2_URL + hrefOne);
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text(), labelOne);

		Assert.assertEquals(render.select("tr:eq(3) td").size(), 1);
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 15px;");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").text(),
				KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL_LABEL + "5");

	}

	/**
	 * Tests method render scenario with instance and total row.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderResultInstanceWithTotalRowTest() throws URISyntaxException, IOException {

		String instanceIdOne = "instance-id-one";
		String instanceIdTwo = "instance-id-two";
		String srcOne = "image src one";
		String labelOne = "Instance one label";
		String hrefOne = "instance-one-href";
		String srcTwo = "image src two";
		String labelTwo = "Instance two label";
		String hrefTwo = "instance-two-href";

		Instance instanceOne = Mockito.mock(Instance.class);
		Mockito.when(instanceOne.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcOne
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefOne + "\"><span>" + labelOne + "</span></a></span>");
		Mockito.when(instanceOne.getId()).thenReturn(instanceIdOne);

		Instance instanceTwo = Mockito.mock(Instance.class);
		Mockito.when(instanceTwo.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcTwo
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefTwo + "\"><span>" + labelTwo + "</span></a></span>");
		Mockito.when(instanceTwo.getId()).thenReturn(instanceIdTwo);

		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(AUTOMATICALLY_SELECTED_OBJECTS);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(1);
		Map<String, Serializable> groupByResult = new HashMap<>();
		groupByResult.put(instanceIdOne, 2);
		groupByResult.put(instanceIdTwo, 3);

		result.put("emf:createdBy", groupByResult);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Mockito.when(instanceResolver.resolveInstances(Matchers.anyList())).thenReturn(
				Arrays.asList(instanceOne, instanceTwo));

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.text(),
				"Number Created by 3 Instance two label 2 Instance one label Total Results: 5");
		Assert.assertEquals(render.select("tr").size(), 6);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").text(), "Number");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").text(), "Created by");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").text(), "3");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) table span").size(), 3);
		Assert.assertEquals(
				render.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC),
				UI2_URL + srcTwo);
		Assert.assertEquals(
				render.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF),
				UI2_URL + hrefTwo);
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").text(), labelTwo);

		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) p").text(), "2");

		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) table span").size(), 3);
		Assert.assertEquals(
				render.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC),
				UI2_URL + srcOne);
		Assert.assertEquals(
				render.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF),
				UI2_URL + hrefOne);
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text(), labelOne);

		Assert.assertEquals(render.select("tr:eq(3) td").size(), 1);
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 15px;");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").text(),
				KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL_LABEL + "5");

	}

	/**
	 * Tests method render scenario with non codelist property simple text and total row.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderResultWithTotalRowTest() throws URISyntaxException, IOException {

		String simpleTextOne = "simple text one";
		String simpleTextTwo = "simple text two";

		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(AUTOMATICALLY_SELECTED_OBJECTS);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(1);
		Map<String, Serializable> groupByResult = new HashMap<>();
		groupByResult.put(simpleTextOne, 2);
		groupByResult.put(simpleTextTwo, 3);

		result.put("emf:createdBy", groupByResult);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.text(), "Number Created by 2 simple text one 3 simple text two Total Results: 5");

		Assert.assertEquals(render.select("tr").size(), 4);

		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").text(), "Number");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").text(), "Created by");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").text(), "2");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").text(), simpleTextOne);

		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").text(), "3");

		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").text(), simpleTextTwo);

		Assert.assertEquals(render.select("tr:eq(3) td").size(), 1);
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 15px;");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").text(),
				KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL_LABEL + "5");

	}

	/**
	 * Tests method render scenario with codelist property empty result without total row.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderCodelistResultWithoutTotalRowTest() throws URISyntaxException, IOException {
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(AUTOMATICALLY_SELECTED_OBJECTS_CODELIST);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(1);
		Map<String, Serializable> groupByResult = new HashMap<>();
		groupByResult.put(CODELIST_1_STATUS_OPEN, 2);
		groupByResult.put(CODELIST_106_STATUS_CLOSED, 3);

		result.put("emf:status", groupByResult);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);
		Assert.assertEquals(render.text(), "Number State 3 Closed label from 106, Closed label from 1 2 Open");

		Assert.assertEquals(render.select("tr").size(), 3);

		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").text(), "Number");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").text(), "State");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").text(), "3");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").text(),
				CODELIST_106_STATUS_CLOSED_LABEL + ", " + CODELIST_1_STATUS_CLOSED_LABEL);

		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").text(), "2");

		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").text(), CODELIST_1_STATUS_OPEN_LABEL);

		Assert.assertEquals(render.select("tr:eq(3) td").size(), 0);
	}

	/**
	 * Tests method render scenario with codelist property empty result and total row.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderCodelistResultWithTotalRowTest() throws URISyntaxException, IOException {
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(MANUALY_SELECTED_OBJECTS_CODELIST_WITH_TOTAL_RESULT_ROW);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(1);
		Map<String, Serializable> groupByResult = new HashMap<>();
		groupByResult.put(CODELIST_1_STATUS_OPEN, 2);
		groupByResult.put(CODELIST_106_STATUS_CLOSED, 3);

		result.put("emf:status", groupByResult);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);
		Assert.assertEquals(render.text(),
				"Number State 3 Closed label from 106, Closed label from 1 2 Open Total Results: 5");

		Assert.assertEquals(render.select("tr").size(), 4);

		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").text(), "Number");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1) p strong").size(), 1);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").text(), "State");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(0)").text(), "3");

		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(1) td:eq(1)").text(),
				CODELIST_106_STATUS_CLOSED_LABEL + ", " + CODELIST_1_STATUS_CLOSED_LABEL);

		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(0)").text(), "2");

		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 12px;");
		Assert.assertEquals(render.select("tr:eq(2) td:eq(1)").text(), CODELIST_1_STATUS_OPEN_LABEL);

		Assert.assertEquals(render.select("tr:eq(3) td").size(), 1);
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE), "margin-left: 15px;");
		Assert.assertEquals(render.select("tr:eq(3) td:eq(0)").text(),
				KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL_LABEL + "5");
	}

	/**
	 * Tests method render scenario with codelist property empty result.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderCodelistEmptyResultTest() throws URISyntaxException, IOException {
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(AUTOMATICALLY_SELECTED_OBJECTS_CODELIST);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(1);
		result.put("emf:status", new HashMap<>());
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.text(), KEY_LABEL_SELECT_OBJECT_RESULTS_NONE_LABEL);
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0) p").attr(JsoupUtil.ATTRIBUTE_STYLE),
				"text-align: center; color: #a94442;");
		Assert.assertEquals(render.select("tr").size(), 1);

		Mockito.verify(searchArgs).setStringQuery(Matchers.eq(CURRENT_INSTANCE_ID + " query"));
		ArgumentCaptor<SearchRequest> searchArguments = ArgumentCaptor.forClass(SearchRequest.class);
		Mockito.verify(searchService).parseRequest(searchArguments.capture());
		SearchRequest actualSearchRequest = searchArguments.getValue();

		Assert.assertEquals(actualSearchRequest.getSearchTree(), tree);
		Assert.assertEquals(actualSearchRequest.getRequest().get(IdocRenderer.GROUP_BY), Arrays.asList("emf:status"));
		Assert.assertEquals(actualSearchRequest.getRequest().get(IdocRenderer.SELECTED_OBJECTS),
				Collections.emptyList());
	}

	/**
	 * Tests method render scenario with codelist property null result.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderCodelistNullResultTest() throws URISyntaxException, IOException {
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(AUTOMATICALLY_SELECTED_OBJECTS_CODELIST);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(0);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.html(), "");

		Mockito.verify(searchArgs).setStringQuery(Matchers.eq(CURRENT_INSTANCE_ID + " query"));
		ArgumentCaptor<SearchRequest> searchArguments = ArgumentCaptor.forClass(SearchRequest.class);
		Mockito.verify(searchService).parseRequest(searchArguments.capture());
		SearchRequest actualSearchRequest = searchArguments.getValue();

		Assert.assertEquals(actualSearchRequest.getSearchTree(), tree);
		Assert.assertEquals(actualSearchRequest.getRequest().get(IdocRenderer.GROUP_BY), Arrays.asList("emf:status"));
		Assert.assertEquals(actualSearchRequest.getRequest().get(IdocRenderer.SELECTED_OBJECTS),
				Collections.emptyList());
	}

	/**
	 * Tests method render scenario manually selected with codelist property null result.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void renderCodelistManuallySelectedNullResultTest() throws URISyntaxException, IOException {
		WidgetConfiguration widgetConfiguration = Mockito.mock(WidgetConfiguration.class);
		JsonObject configuration = loadTestResource(MANUALY_SELECTED_OBJECTS_CODELIST);
		Mockito.when(widgetConfiguration.getConfiguration()).thenReturn(configuration);
		javax.json.JsonObject jsonConfiguration = IdocRenderer.toJson(widgetConfiguration);
		Condition tree = Mockito.mock(Condition.class);
		Mockito
				.when(convertor.parseCondition(jsonConfiguration.getJsonObject(IdocRenderer.SEARCH_CRITERIA)))
					.thenReturn(tree);
		ContentNode node = Mockito.mock(Widget.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(node.getElement()).thenReturn(element);

		Mockito.when(((Widget) node).getConfiguration()).thenReturn(widgetConfiguration);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);

		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		Mockito.when(searchArgs.getStringQuery()).thenReturn(IdocRenderer.CURRENT_OBJECT + " query");
		Map<String, Map<String, Serializable>> result = new HashMap<>(0);
		Mockito.when(searchArgs.getAggregatedData()).thenReturn(result);

		Element render = aggregateTableWidgetRenderer.render(CURRENT_INSTANCE_ID, node);

		Assert.assertEquals(render.html(), "");

		Mockito.verify(searchArgs).setStringQuery(Matchers.eq(CURRENT_INSTANCE_ID + " query"));
		ArgumentCaptor<SearchRequest> searchArguments = ArgumentCaptor.forClass(SearchRequest.class);
		Mockito.verify(searchService).parseRequest(searchArguments.capture());
		SearchRequest actualSearchRequest = searchArguments.getValue();

		Assert.assertEquals(actualSearchRequest.getSearchTree(), tree);
		Assert.assertEquals(actualSearchRequest.getRequest().get(IdocRenderer.GROUP_BY), Arrays.asList("emf:status"));
		Assert.assertEquals(actualSearchRequest.getRequest().get(IdocRenderer.SELECTED_OBJECTS),
				Arrays.asList("emf:c189f3a4-83e1-4a22-b0f2-bcc8c3137365", "emf:c1e158b1-878e-4908-94df-914314b03edb"));
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
		Assert.assertEquals(aggregateTableWidgetRenderer.accept(node), expectedResult, errorMessage);

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

		Widget nonAggregatedWidget = Mockito.mock(Widget.class);
		Mockito.when(nonAggregatedWidget.isWidget()).thenReturn(Boolean.TRUE);
		Mockito.when(nonAggregatedWidget.getName()).thenReturn("non aggregated widget");

		Widget aggregatedWidget = Mockito.mock(Widget.class);
		Mockito.when(aggregatedWidget.isWidget()).thenReturn(Boolean.TRUE);
		Mockito.when(aggregatedWidget.getName()).thenReturn(AggregateTableWidgetRenderer.AGGREGATE_TABLE_WIDGET_NAME);

		return new Object[][] { { null, Boolean.FALSE, "test with null node" },
				{ nonWidget, Boolean.FALSE, "test with node which is not witdget" },
				{ nonAggregatedWidget, Boolean.FALSE, "test with node which is not aggregated witdget" },
				{ aggregatedWidget, Boolean.TRUE, "test with node which is aggregated witdget" } };
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
	public JsonObject loadTestResource(String resource) throws URISyntaxException, IOException {
		URL testJsonURL = getClass().getClassLoader().getResource(resource);
		File jsonConfiguration = new File(testJsonURL.toURI());
		try (FileReader fileReader = new FileReader(jsonConfiguration)) {
			return new JsonParser().parse(fileReader).getAsJsonObject();
		}
	}
}