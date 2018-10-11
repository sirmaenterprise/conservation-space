package com.sirma.sep.export.renders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.util.LinkProviderService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerBuilder;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNodeBuilder;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Test class for DataTableWidget.
 *
 * @author Hristo Lungov
 */
public class DataTableWidgetRendererTest {

	private static final String UI2_URL = "https://ses.sirmaplatform.com/#/idoc/";
	private static final String RESULTS = "2Results";
	private static final int CL_210 = 210;
	private static final String INSTANCE_ONE_TITLE = "new configuration DTW";
	private static final String INSTANCE_TWO_TITLE = "tests";
	private static final String SUB_PROPERTY_TITLE = "Sub property title";
	private static final String CL210_TITLE_DESCRIPTION = "Common document";
	private static final String INSTANCE_TWO_ID = "emf:f3908df1-6160-49cd-ba70-840866019785";
	private static final String INSTANCE_ONE_ID = "emf:76dcea11-ee66-45ae-8d5a-f2c42afc67ce";
	private static final String TABLE_TITLE = "DTW Title";
	private static final String ENTITY = "Entity";
	private static final String srcOne = "image src one";
	private static final String hrefOne = "instance-one-href";
	private static final String labelOne = "Instance one label";
	private static final String srcTwo = "image src two";
	private static final String hrefTwo = "instance-two-href";
	private static final String labelTwo = "Instance two label";

	private static final String HYPERLINK_LABEL = "(Common document) barnat.pdf";
	private static final String TEST_TITLE = "Test Title";
	private static final String TEST_TITLE_HTML = "<b>Test</b> <i>Title</i>";
	private static final String INSTANCE_ID = "instanceId";
	private static final String TITLE = "Title";
	private static final String HAS_PARENT = "hasParent";
	private static final String TYPE = "Type";
	private static final String DATA_TABLE_WIDGET_HTML_FILE = "data-table-widget.html";
	private static final String DATA_TABLE_WIDGET_ID = "0dda955c-e9cd-47e7-be51-ec5ccf66de5a";
	private static final String DATA_TABLE_WIDGET_CURRENT_OBJECT_HTML_FILE = "data-table-widget-current-object.html";
	private static final String DATA_TABLE_WIDGET_CURRENT_OBJECT_ID = "8b987e70-7c6c-4806-d08c-6beb4f461275";
	private static final String WIDGET_TITLE = "Fourth Widget";
	private static final String DATA_TABLE_WIDGET_CURRENT_OBJECT_HEADING_TEXT = "Sixth Widget";
	private static final String INSTANCE_HEADER = "<span><img src=\"some image src\" /></span><span><a class=\"instance-link has-tooltip\" href=\"#/idoc/emf:a117c235-e5f7-4417-b45c-c637db3a09e4\">(<span data-property=\"type\">Common document</span>) <span data-property=\"title\">barnat.pdf</span></a></span>";
	private static final String WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND = "No object could be found with the selected search criteria.";
	private static final String WARN_MESSAGE_NO_SELECTION = "No Selection.";
	private static final String MULTIPLE_OBJECTS = "Multiple objects";
	private static final String TEST_FILE_AUTO_MODE_NO_OBJECTS_FOUND = "datatable-widget-automatically-mode-no-objects-found.json";
	private static final String TEST_FILE_MANUAL_MODE_NO_SELECTED_OBJECTS = "datatable-widget-manually-mode-no-objects-found.json";

	private static final String TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN = "dtw-with-columns-order-without-entity-column.json";
	private static final String TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_SELECTED_PROPERTIES_MAP_CONFIGURATION = "dtw-with-columns-order-without-entity-column-selected-properties-map-configuration.json";
	private static final String TEST_DTW_WITH_COLUMN_ORDER_WITH_ENTITY_COLUMN = "dtw-with-columns-order-with-entity-column.json";

	private static final String TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW = "dtw-with-columns-order-without-entity-column-without-header-row.json";
	private static final String TEST_DTW_WITH_COLUMN_ORDER_WITH_ENTITY_COLUMN_WITHOUT_HEADER_ROW = "dtw-with-columns-order-with-entity-column-without-header-row.json";

	private static final String DTW_WITH_COLUMNS_ORDER_WITHOUT_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON = "dtw-with-columns-order-without-entity-column-with-table-title.json";
	private static final String DTW_WITH_COLUMNS_ORDER_WITH_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON = "dtw-with-columns-order-with-entity-column-with-table-title.json";

	private static final String TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW_WITH_TITLE = "dtw-with-columns-order-without-entity-column-without-header-row-with-title.json";
	private static final String TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITH_HEADER_ROW_WITH_TITLE = "dtw-with-columns-order-with-entity-column-without-header-row-with-title.json";

	private static final String TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN = "dtw-without-columns-order-without-entity-column.json";
	private static final String TEST_DTW_WITHOUT_COLUMN_ORDER_WITH_ENTITY_COLUMN = "dtw-without-columns-order-with-entity-column.json";

	private static final String TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW = "dtw-without-columns-order-without-entity-column-without-header-row.json";
	private static final String TEST_DTW_WITHOUT_COLUMN_ORDER_WITH_ENTITY_COLUMN_WITHOUT_HEADER_ROW = "dtw-without-columns-order-with-entity-column-without-header-row.json";

	private static final String DTW_WITHOUT_COLUMNS_ORDER_WITHOUT_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON = "dtw-without-columns-order-without-entity-column-with-table-title.json";
	private static final String DTW_WITHOUT_COLUMNS_ORDER_WITH_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON = "dtw-without-columns-order-with-entity-column-with-table-title.json";

	private static final String TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW_WITH_TITLE = "dtw-without-columns-order-without-entity-column-without-header-row-with-title.json";
	private static final String TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITH_HEADER_ROW_WITH_TITLE = "dtw-without-columns-order-with-entity-column-without-header-row-with-title.json";

	private static final String TEST_FILE_UNDEFINED_SEARCH_CRITERIA = "dtw-with-undefined-search-criteria.json";
	
	private static final String TEST_DTW_WITH_SELECTED_SUB_PROPERTIES = "dtw-with-selected-sub-properties.json";

	@InjectMocks
	private DataTableWidgetRenderer dataTableWidget;

	@Mock
	private SearchService searchService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private LinkProviderService linkProviderService;

	@Mock
	private CodelistService codelistService;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Spy
	private JsonToConditionConverter converter = new JsonToConditionConverter();

	@Mock
	private DefinitionService definitionService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Mock
	private ConditionsManager conditionsManager;

	/**
	 * Runs Before class init.
	 */
	@BeforeClass
	public static void beforeClass() {
		ContentNodeFactory instance = ContentNodeFactory.getInstance();
		instance.registerBuilder(new LayoutNodeBuilder());
		instance.registerBuilder(new LayoutManagerBuilder());
	}

	/**
	 * Runs before each method and setup mockito.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>(UI2_URL));
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SEARCH_RESULTS)).thenReturn("Results");
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA)).thenReturn(WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECT_OBJECT_RESULTS_NONE)).thenReturn(WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECT_OBJECT_NONE)).thenReturn(WARN_MESSAGE_NO_SELECTION);
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_MULTIPLE_OBJECTS)).thenReturn(MULTIPLE_OBJECTS);
	}

	@Test
	public void should_BuildUndefinedCriteriaTable_When_WidgetConfigurationHasNotSearchCriteria()
			throws URISyntaxException, IOException {
		WidgetNode widgetTest = new WidgetNodeBuilder().setConfiguration(TEST_FILE_UNDEFINED_SEARCH_CRITERIA).build();

		Element table = dataTableWidget.render("instance-id", widgetTest);

		org.junit.Assert.assertEquals(WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA, table.text());
	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. With entity column. 3. With title. 4. With header row.
	 * 5. Inner table.
	 */
	@Test
	public void renderWithColumnsOrderWithEntityColumnWithoutHeaderRowWithTitleInnerTabelTest()
			throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder(JsoupUtil.TAG_TD).setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITH_HEADER_ROW_WITH_TITLE).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);


		Assert.assertEquals(TABLE_TITLE + " " + RESULTS + " " + TYPE + " " + ENTITY + " " + TITLE + " " + CL210_TITLE_DESCRIPTION + " " + labelOne
						+ " " + INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " " + labelTwo + " "
						+ INSTANCE_TWO_TITLE , table.text());
		Assert.assertEquals(6, table.select("tr").size());
		Assert.assertEquals("99%", table.attr(JsoupUtil.ATTRIBUTE_WIDTH));

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p strong").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(0) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p strong").size());
		Assert.assertEquals(ENTITY, table.select("tr:eq(1) td:eq(1) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(2) p strong").text());

		Assert.assertEquals(table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) td:eq(0) p").size(), 1);
		Assert.assertEquals(table.select("tr:eq(2) td:eq(0) p").text(), CL210_TITLE_DESCRIPTION);

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(2) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. With entity column. 3. With title. 4. With header row.
	 */
	@Test
	public void renderWithColumnsOrderWithEntityColumnWithoutHeaderRowWithTitleTest()
			throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITH_HEADER_ROW_WITH_TITLE).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(TABLE_TITLE  + " " + RESULTS + " " + TYPE + " " + ENTITY + " " + TITLE + " " + CL210_TITLE_DESCRIPTION + " " + labelOne
						+ " " + INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " " + labelTwo + " "
						+ INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(6, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p strong").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(0) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p strong").size());
		Assert.assertEquals(ENTITY, table.select("tr:eq(1) td:eq(1) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(2) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(2) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. With entity column. 3. With title. 4. Without header
	 * row.
	 */
	@Test
	public void renderWithColumnsOrderWithEntityColumnWithTitleTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(DTW_WITH_COLUMNS_ORDER_WITH_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(TABLE_TITLE + " " + RESULTS + " " + CL210_TITLE_DESCRIPTION + " " + labelOne + " " + INSTANCE_ONE_TITLE + " "
						+ CL210_TITLE_DESCRIPTION + " " + labelTwo + " " + INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(5, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(2) p").text());
	}

	/**
	 * Tests method render.
	 * Scenario:
	 * 1. With columns order.
	 * 2. With entity column.
	 * 3. Without title.
	 * 4. Without widget title.
	 */
	@Test
	public void renderWithColumnsOrderWithEntityColumnWithoutHeaderRowTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITH_ENTITY_COLUMN_WITHOUT_HEADER_ROW).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(RESULTS + " "+ CL210_TITLE_DESCRIPTION + " " + labelOne + " " + INSTANCE_ONE_TITLE + " "
				+ CL210_TITLE_DESCRIPTION + " " + labelTwo + " " + INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(5, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(1) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(2) p").text());

	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. With entity column. 3. Without title.
	 */
	@Test
	public void renderWithColumnsOrderWithEntityColumnTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITH_ENTITY_COLUMN).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(
				RESULTS + " " + TYPE + " " + ENTITY + " " + TITLE + " " + CL210_TITLE_DESCRIPTION + " " + labelOne + " "
						+ INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " " + labelTwo + " "
						+ INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(6, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p strong").size());
		Assert.assertEquals(ENTITY, table.select("tr:eq(1) td:eq(1) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p strong").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(2) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(3) td:eq(1) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(2) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(2) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. Without entity column. 3. With title. 4. With header
	 * row.
	 */
	@Test
	public void renderWithColumnsOrderWithoutEntityColumnWithoutHeaderRowWithTitleTest()
			throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW_WITH_TITLE).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);


		Assert.assertEquals(TABLE_TITLE + " " + RESULTS + " " + TYPE + " " + TITLE + " " + CL210_TITLE_DESCRIPTION + " "
				+ INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(4, table.select("tr").size());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p strong").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(0) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(1) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(1) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. Without entity column. 3. With title. 4. Without header
	 * row.
	 */
	@Test
	public void renderWithColumnsOrderWithoutEntityColumnWithTitleTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(DTW_WITH_COLUMNS_ORDER_WITHOUT_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(TABLE_TITLE + " " + RESULTS + " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_ONE_TITLE + " "
				+ CL210_TITLE_DESCRIPTION + " " + INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(3, table.select("tr").size());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(1) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. Without entity column. 3. Without title. 4. Without
	 * header row.
	 */
	@Test
	public void renderWithColumnsOrderWithoutEntityColumnWithoutHeaderRowTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(RESULTS + " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_ONE_TITLE + " "
				+ CL210_TITLE_DESCRIPTION + " " + INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(3, table.select("tr").size());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(1) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. With columns order. 2. Without entity column. 3. Without title.
	 */
	@Test
	public void renderWithColumnsOrderWithoutEntityColumnTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(RESULTS + " " + TYPE + " " + TITLE + " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_ONE_TITLE
				+ " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(4, table.select("tr").size());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p strong").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(0) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p strong").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(1) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(1) p").text());
	}

	@Test
	public void renderWithColumnsOrderWithoutEntityColumnSelectedPropertiesMapConfigurationTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_SELECTED_PROPERTIES_MAP_CONFIGURATION).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(RESULTS + " " + TYPE + " " + TITLE + " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_ONE_TITLE
				+ " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_TWO_TITLE, table.text());
		Assert.assertEquals(4, table.select("tr").size());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p strong").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(0) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p strong").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(1) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(1) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. With entity column. 3. With title. 4. With header
	 * row.
	 */
	@Test
	public void renderWithoutColumnsOrderWithEntityColumnWithoutHeaderRowWithTitleTest()
			throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITH_HEADER_ROW_WITH_TITLE).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);


		Assert.assertEquals(TABLE_TITLE + " " + RESULTS + " " + ENTITY + " " + TITLE + " " + TYPE + " " + labelOne + " " + INSTANCE_ONE_TITLE + " "
						+ CL210_TITLE_DESCRIPTION + " " + labelTwo + " " + INSTANCE_TWO_TITLE + " "
						+ CL210_TITLE_DESCRIPTION, table.text());
		Assert.assertEquals(6, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(ENTITY, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(3) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(3) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(3) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(2) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. With entity column. 3. With title. 4. Without header
	 * row.
	 */
	@Test
	public void renderWithoutColumnsOrderWithEntityColumnWithTitleTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(DTW_WITHOUT_COLUMNS_ORDER_WITH_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(TABLE_TITLE + " " + RESULTS + " " + labelOne + " " + INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " " + labelTwo
						+ " " + INSTANCE_TWO_TITLE + " " + CL210_TITLE_DESCRIPTION , table.text());
		Assert.assertEquals(5, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(2, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(2) p").text());

	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. With entity column. 3. Without title. 4. Without
	 * header row.
	 */
	@Test
	public void renderWithoutColumnsOrderWithEntityColumnWithoutHeaderRowTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITHOUT_COLUMN_ORDER_WITH_ENTITY_COLUMN_WITHOUT_HEADER_ROW).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);


		Assert.assertEquals(RESULTS + " " + labelOne + " " + INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " "
				+ labelTwo + " " + INSTANCE_TWO_TITLE + " " + CL210_TITLE_DESCRIPTION, table.text());
		Assert.assertEquals(5, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(2) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. With entity column. 3. Without title.
	 */
	@Test
	public void renderWithoutColumnsOrderWithEntityColumnTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITHOUT_COLUMN_ORDER_WITH_ENTITY_COLUMN).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(RESULTS + " " + ENTITY + " " + TITLE + " " + TYPE + " " + labelOne + " " + INSTANCE_ONE_TITLE + " "
						+ CL210_TITLE_DESCRIPTION + " " + labelTwo + " " + INSTANCE_TWO_TITLE + " "
						+ CL210_TITLE_DESCRIPTION, table.text());
		Assert.assertEquals(6, table.select("tr").size());

		Assert.assertEquals("3", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(ENTITY, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(2) p").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcOne,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefOne,
				table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelOne, table.select("tr:eq(2) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(2) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(UI2_URL + srcTwo,
				table.select("tr:eq(3) td:eq(0) table tr:eq(0) td:eq(0) span img").attr(JsoupUtil.ATTRIBUTE_SRC));
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(3) td:eq(0) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(3) td:eq(0) table tr:eq(0) td:eq(1) span a").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(1) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(2) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(2) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. Without entity column. 3. With title. 4. With header
	 * row.
	 */
	@Test
	public void renderWithoutColumnsOrderWithoutEntityColumnWithoutHeaderRowWithTitleTest()
			throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW_WITH_TITLE).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(TABLE_TITLE + " " + RESULTS + " " + TITLE + " " + TYPE + " " + INSTANCE_ONE_TITLE + " "
				+ CL210_TITLE_DESCRIPTION + " " + INSTANCE_TWO_TITLE + " " + CL210_TITLE_DESCRIPTION , table.text());
		Assert.assertEquals(4, table.select("tr").size());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(1) p").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());
	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. Without entity column. 3. With title. 4. Without
	 * header row.
	 */
	@Test
	public void renderWithoutColumnsOrderWithoutEntityColumnWithTitleTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(DTW_WITHOUT_COLUMNS_ORDER_WITHOUT_ENTITY_COLUMN_WITH_TABLE_TITLE_JSON).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);


		Assert.assertEquals(TABLE_TITLE  + " " + RESULTS + " " + INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " "
				+ INSTANCE_TWO_TITLE + " " + CL210_TITLE_DESCRIPTION, table.text());
		Assert.assertEquals(3, table.select("tr").size());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(2, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(TABLE_TITLE, table.select("tr:eq(0) td:eq(0) p:eq(0)").text());

		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p:eq(1)").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(1) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. Without entity column. 3. Without title. 4. Without
	 * header row.
	 */
	@Test
	public void renderWithoutColumnsOrderWithoutEntityColumnWithoutHeaderRowTest()
			throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN_WITHOUT_HEADER_ROW).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);


		Assert.assertEquals(RESULTS  + " " + INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION + " " + INSTANCE_TWO_TITLE
				+ " " + CL210_TITLE_DESCRIPTION , table.text());
		Assert.assertEquals(3, table.select("tr").size());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(1) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(1) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());
	}

	/**
	 * Tests method render. Scenario: 1. Without columns order. 2. Without entity column. 3. Without title.
	 */
	@Test
	public void renderWithoutColumnsOrderWithoutEntityColumnTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITHOUT_COLUMN_ORDER_WITHOUT_ENTITY_COLUMN).build();
		initInstancesForTest();

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(RESULTS + " " + TITLE + " " + TYPE + " " + INSTANCE_ONE_TITLE + " " + CL210_TITLE_DESCRIPTION
				+ " " + INSTANCE_TWO_TITLE + " " + CL210_TITLE_DESCRIPTION , table.text());
		Assert.assertEquals(4, table.select("tr").size());

		Assert.assertEquals("2", table.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(0) td:eq(0) p").size());
		Assert.assertEquals(RESULTS, table.select("tr:eq(0) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(0) p strong").size());
		Assert.assertEquals(TITLE, table.select("tr:eq(1) td:eq(0) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(1) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(1) td:eq(1) p strong").size());
		Assert.assertEquals(TYPE, table.select("tr:eq(1) td:eq(1) p strong").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_ONE_TITLE, table.select("tr:eq(2) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(2) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(2) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(2) td:eq(1) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(0) p").size());
		Assert.assertEquals(INSTANCE_TWO_TITLE, table.select("tr:eq(3) td:eq(0) p").text());

		Assert.assertEquals("1", table.select("tr:eq(3) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(1, table.select("tr:eq(3) td:eq(1) p").size());
		Assert.assertEquals(CL210_TITLE_DESCRIPTION, table.select("tr:eq(3) td:eq(1) p").text());
	}

	/**
	 * Test accept method of widget.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void testAccept() {
		WidgetNode widget = new WidgetNodeBuilder().setIsWidget(true).setName(DataTableWidgetRenderer.DATA_TABLE_WIDGET_NAME).build();
		Assert.assertTrue(dataTableWidget.accept(widget));

		widget = new WidgetNodeBuilder().setIsWidget(false).setName(DataTableWidgetRenderer.DATA_TABLE_WIDGET_NAME).build();
		Assert.assertFalse(dataTableWidget.accept(widget));

		widget = new WidgetNodeBuilder().setIsWidget(true).setName("").build();
		Assert.assertFalse(dataTableWidget.accept(widget));
	}

	/**
	 * No objects found test.
	 */
	@Test
	public void noObjectsFoundTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_AUTO_MODE_NO_OBJECTS_FOUND).build();
		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);
		when(searchArgs.getResult()).thenReturn(Collections.emptyList());
		when(searchArgs.getStringQuery()).thenReturn("");
		Sorter sorter = Mockito.mock(Sorter.class);
		List<Sorter> sorters = new ArrayList<>(1);
		sorters.add(sorter);
		when(searchArgs.getSorters()).thenReturn(sorters);
		when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Collections.emptyList());

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Assert.assertEquals("1", table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(WIDGET_TITLE, table.select("tr:eq(0) > td:eq(0) p").text());
		Assert.assertEquals(WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND, table.select("tr:eq(1) > td:eq(0)").text());
	}

	@Test
	public void noSelectedObjectsTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_MANUAL_MODE_NO_SELECTED_OBJECTS).build();
		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Collections.emptyList());

		Element table = dataTableWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Assert.assertEquals("1", table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
		Assert.assertEquals(WIDGET_TITLE, table.select("tr:eq(0) > td:eq(0) p").text());
		Assert.assertEquals(WARN_MESSAGE_NO_SELECTION, table.select("tr:eq(1) > td:eq(0)").text());
	}

	/**
	 * Test render method of widget.
	 */
	@Test
	public void testRender() throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(DATA_TABLE_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(is);
			Optional<Widget> selectedWidget = idoc.selectWidget(DATA_TABLE_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			when(instance.get(TITLE.toLowerCase())).thenReturn(TEST_TITLE);
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(INSTANCE_HEADER);
			DefinitionModel instanceDefinitionModel = Mockito.mock(DefinitionModel.class);
			PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
			propertyDefinition.setLabelId(TITLE);
			propertyDefinition.setIdentifier(TITLE.toLowerCase());
			DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
			propertyDefinition.setDataType(dataTypeDefinition);
			when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
			when(definitionService.getProperty(TITLE.toLowerCase(), instance)).thenReturn(propertyDefinition);
			when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinitionModel);
			when(definitionService.find(Matchers.anyString())).thenReturn(instanceDefinitionModel);
			when(instanceDefinitionModel.getField(Matchers.anyString())).thenReturn(Optional.of(propertyDefinition));
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			InstanceType type = Mockito.mock(InstanceType.class);
			when(type.getId()).thenReturn(null);
			when(instance.type()).thenReturn(type);
			Element table = dataTableWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
			Assert.assertEquals("2", table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
			Assert.assertEquals(WIDGET_TITLE, table.select("tr:eq(0) > td:eq(0) p:eq(0)").text());
			Assert.assertEquals(IdocRenderer.ENTITY_LABEL, table.select("tr:eq(1) > td:eq(0) p").text());
			Assert.assertEquals(TITLE, table.select("tr:eq(1) > td:eq(1) p").text());
			Assert.assertEquals(UI2_URL + "#/idoc/emf:a117c235-e5f7-4417-b45c-c637db3a09e4",
					table.select("tr:eq(2) > td:eq(0) table tr:eq(0) > td:eq(1) a").attr(JsoupUtil.ATTRIBUTE_HREF));
			Assert.assertEquals(HYPERLINK_LABEL, table.select("tr:eq(2) > td:eq(0) table tr:eq(0) > td:eq(1) a").text());
			Assert.assertEquals(TEST_TITLE, table.select("tr:eq(2) > td:eq(1) p").text());
		}
	}

	@Test
	public void testRenderAutomaticSearchWithCurrentObject() throws IOException {
		try (InputStream is = getClass()
				.getClassLoader()
					.getResourceAsStream(DATA_TABLE_WIDGET_CURRENT_OBJECT_HTML_FILE)) {
			Idoc idoc = Idoc.parse(is);
			Optional<Widget> selectedWidget = idoc.selectWidget(DATA_TABLE_WIDGET_CURRENT_OBJECT_ID);
			Instance instance = Mockito.mock(Instance.class);
			SearchArguments<Instance> searchArguments = Mockito.mock(SearchArguments.class);
			when(searchArguments.getStringQuery()).thenReturn("");
			Sorter sorter = Mockito.mock(Sorter.class);
			List<Sorter> sorters = new ArrayList<>(1);
			sorters.add(sorter);
			when(searchArguments.getSorters()).thenReturn(sorters);
			when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArguments);
			when(searchArguments.getResult()).thenReturn(Arrays.asList(instance));
			when(instance.getId()).thenReturn(INSTANCE_ID);
			when(instance.get(TITLE.toLowerCase())).thenReturn(TEST_TITLE);
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(INSTANCE_HEADER);
			InstanceType type = Mockito.mock(InstanceType.class);
			when(type.getId()).thenReturn("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
			when(instance.type()).thenReturn(type);
			DefinitionModel instanceDefinitionModel = Mockito.mock(DefinitionModel.class);
			PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
			propertyDefinition.setLabelId(TITLE);
			propertyDefinition.setIdentifier(TITLE.toLowerCase());
			DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
			propertyDefinition.setDataType(dataTypeDefinition);
			when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
			when(definitionService.getProperty(TITLE.toLowerCase(), instance)).thenReturn(propertyDefinition);
			when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinitionModel);
			when(definitionService.find(Matchers.anyString())).thenReturn(instanceDefinitionModel);
			when(instanceDefinitionModel.getField(Matchers.anyString())).thenReturn(Optional.of(propertyDefinition));
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			Element table = dataTableWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
			Assert.assertEquals("2", table.select("tr:eq(0) > td:eq(0)").attr("colspan"));
			Assert.assertEquals(DATA_TABLE_WIDGET_CURRENT_OBJECT_HEADING_TEXT, table.select("tr:eq(0) > td:eq(0) p:eq(0)").text());
			Assert.assertEquals(IdocRenderer.ENTITY_LABEL, table.select("tr:eq(1) > td:eq(0)").text());
			Assert.assertEquals(TITLE, table.select("tr:eq(1) > td:eq(1)").text());
			Assert.assertEquals(UI2_URL + "#/idoc/emf:a117c235-e5f7-4417-b45c-c637db3a09e4",
					table.select("tr:eq(2) > td:eq(0) table tr:eq(0) > td:eq(1) a").attr(JsoupUtil.ATTRIBUTE_HREF));
			Assert.assertEquals(HYPERLINK_LABEL, table.select("tr:eq(2) > td:eq(0)").text());
			Assert.assertEquals(TEST_TITLE, table.select("tr:eq(2) > td:eq(1)").text());
		}
	}
	
	@Test
	public void renderWithSelectedSubPropertiesTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_SELECTED_SUB_PROPERTIES).build();
		
		Instance instanceOne = Mockito.mock(Instance.class);
		Instance instanceTwo = Mockito.mock(Instance.class);
		initInstancesForTest(instanceOne, instanceTwo);
		Mockito.when(instanceResolver.resolveInstances(Arrays.asList(INSTANCE_TWO_ID))).thenReturn(
				Arrays.asList(instanceTwo));
		Mockito.when(instanceTwo.get(TITLE.toLowerCase())).thenReturn(SUB_PROPERTY_TITLE);
		Element table = dataTableWidget.render(INSTANCE_ID, widget);
		
		Assert.assertEquals("Has Parent", table.select("tr:eq(1) > td:eq(1)").text());
		Assert.assertEquals("Has Parent: Title", table.select("tr:eq(1) > td:eq(2)").text());
		Assert.assertEquals(UI2_URL + hrefTwo,
				table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").attr(JsoupUtil.ATTRIBUTE_HREF));
		Assert.assertEquals(labelTwo, table.select("tr:eq(2) td:eq(1) table tr:eq(0) td:eq(1) span a").text());		
		Assert.assertEquals(SUB_PROPERTY_TITLE, table.select("tr:eq(2) > td:eq(2)").text());
	}
	
	@Test
	public void renderWithSelectedSubPropertiesOfMultiplePropertyTest() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_DTW_WITH_SELECTED_SUB_PROPERTIES).build();
		initInstancesForTest();
		
		Element table = dataTableWidget.render(INSTANCE_ID, widget);
		
		Assert.assertEquals("Has Parent: Title", table.select("tr:eq(1) > td:eq(2)").text());
		Assert.assertEquals("Has Parent: Has Parent", table.select("tr:eq(1) > td:eq(3)").text());
		Assert.assertEquals(MULTIPLE_OBJECTS, table.select("tr:eq(2) > td:eq(2)").text());
		Assert.assertEquals(MULTIPLE_OBJECTS, table.select("tr:eq(2) > td:eq(3)").text());
	}
	
	@Test
	public void testRenderFieldContainingHtml() throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(DATA_TABLE_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(is);
			Optional<Widget> selectedWidget = idoc.selectWidget(DATA_TABLE_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			when(instance.get(TITLE.toLowerCase())).thenReturn(TEST_TITLE_HTML);
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(INSTANCE_HEADER);
			DefinitionModel instanceDefinitionModel = Mockito.mock(DefinitionModel.class);
			PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
			propertyDefinition.setLabelId(TITLE);
			propertyDefinition.setIdentifier(TITLE.toLowerCase());
			DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
			propertyDefinition.setDataType(dataTypeDefinition);

			ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
			when(controlDefinition.getField(Matchers.anyString())).thenReturn(Optional.empty());
			when(controlDefinition.getIdentifier()).thenReturn(IdocRenderer.RICHTEXT);
			propertyDefinition.setControlDefinition(controlDefinition);

			when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
			when(definitionService.getProperty(TITLE.toLowerCase(), instance)).thenReturn(propertyDefinition);
			when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinitionModel);
			when(definitionService.find(Matchers.anyString())).thenReturn(instanceDefinitionModel);
			when(instanceDefinitionModel.getField(Matchers.anyString())).thenReturn(Optional.of(propertyDefinition));
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			InstanceType type = Mockito.mock(InstanceType.class);
			when(type.getId()).thenReturn(null);
			when(instance.type()).thenReturn(type);

			Element table = dataTableWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
			Assert.assertEquals("2", table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN));
			Assert.assertEquals(WIDGET_TITLE, table.select("tr:eq(0) > td:eq(0) p:eq(0)").text());
			Assert.assertEquals(IdocRenderer.ENTITY_LABEL, table.select("tr:eq(1) > td:eq(0) p").text());
			Assert.assertEquals(TITLE, table.select("tr:eq(1) > td:eq(1) p").text());
			Assert.assertEquals(UI2_URL + "#/idoc/emf:a117c235-e5f7-4417-b45c-c637db3a09e4",
					table.select("tr:eq(2) > td:eq(0) table tr:eq(0) > td:eq(1) a").attr(JsoupUtil.ATTRIBUTE_HREF));
			Assert.assertEquals(HYPERLINK_LABEL,
					table.select("tr:eq(2) > td:eq(0) table tr:eq(0) > td:eq(1) a").text());
			Assert.assertEquals(TEST_TITLE, table.select("tr:eq(2) > td:eq(1)").text());
		}
	}
	
	private void initInstancesForTest() {
		Instance instanceOne = Mockito.mock(Instance.class);
		Instance instanceTwo = Mockito.mock(Instance.class);
		initInstancesForTest(instanceOne, instanceTwo);
	}
	
	private void initInstancesForTest(Instance instanceOne, Instance instanceTwo) {
		String typeId = "OT210027";
		DataTypeDefinition textDataTypeDefinition = Mockito.mock(DataTypeDefinition.class);
		Mockito.when(textDataTypeDefinition.getName()).thenReturn("text");
		PropertyDefinitionMock propertyDefinitionTitle = new PropertyDefinitionMock();
		propertyDefinitionTitle.setLabelId(TITLE);
		propertyDefinitionTitle.setIdentifier(TITLE.toLowerCase());
		propertyDefinitionTitle.setDataType(textDataTypeDefinition);
		PropertyDefinitionMock propertyDefinitionType = new PropertyDefinitionMock();
		propertyDefinitionType.setLabelId(TYPE);
		propertyDefinitionType.setIdentifier(TYPE.toLowerCase());
		propertyDefinitionType.setCodelist(210);
		propertyDefinitionType.setDataType(textDataTypeDefinition);
	
		DataTypeDefinition objectDataTypeDefinition = Mockito.mock(DataTypeDefinition.class);
		Mockito.when(objectDataTypeDefinition.getName()).thenReturn(DataTypeDefinition.URI);
		PropertyDefinitionMock propertyDefinitionHasParent = new PropertyDefinitionMock();
		propertyDefinitionHasParent.setLabelId("Has Parent");
		propertyDefinitionHasParent.setIdentifier(HAS_PARENT);
		propertyDefinitionHasParent.setDataType(objectDataTypeDefinition);
		
		Mockito.when(instanceOne.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcOne
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefOne + "\"><span>" + labelOne + "</span></a></span>");

		Mockito.when(instanceOne.getId()).thenReturn(INSTANCE_ONE_ID);
		DefinitionModel definitionModelInstanceOne = Mockito.mock(DefinitionModel.class);
		Mockito.when(definitionService.getInstanceDefinition(instanceOne)).thenReturn(definitionModelInstanceOne);
		Mockito.when(definitionModelInstanceOne.getField(TITLE.toLowerCase())).thenReturn(
				Optional.of(propertyDefinitionTitle));
		
		Mockito.when(definitionModelInstanceOne.getField(HAS_PARENT)).thenReturn(
				Optional.of(propertyDefinitionHasParent));
		
		Mockito.when(definitionModelInstanceOne.getField(TYPE.toLowerCase())).thenReturn(
				Optional.of(propertyDefinitionType));
		
		Mockito.when(definitionModelInstanceOne.getIdentifier()).thenReturn("OT210027");
		
		Mockito.when(definitionService.getProperty(TITLE.toLowerCase(), instanceOne)).thenReturn(
				propertyDefinitionTitle);
		Mockito.when(definitionService.getProperty(TYPE.toLowerCase(), instanceOne)).thenReturn(propertyDefinitionType);
		Mockito.when(instanceOne.get(TITLE.toLowerCase())).thenReturn(INSTANCE_ONE_TITLE);
		Mockito.when(instanceOne.get(TYPE.toLowerCase())).thenReturn(typeId);
		Mockito.when(instanceOne.get(HAS_PARENT)).thenReturn(INSTANCE_TWO_ID);

		
		Mockito.when(codelistService.getDescription(CL_210, typeId)).thenReturn(CL210_TITLE_DESCRIPTION);
		Mockito.when(instanceTwo.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("<span><img src=\"" + srcTwo
				+ "\" /></span><span><a class=\"instance-link\" href=\"" + hrefTwo + "\"><span>" + labelTwo + "</span></a></span>");

		Mockito.when(instanceTwo.getId()).thenReturn(INSTANCE_TWO_ID);
		DefinitionModel definitionModelInstanceTwo = Mockito.mock(DefinitionModel.class);
		Mockito.when(definitionService.getInstanceDefinition(instanceTwo)).thenReturn(definitionModelInstanceTwo);
		Mockito.when(definitionModelInstanceTwo.getField(TITLE.toLowerCase())).thenReturn(
				Optional.of(propertyDefinitionTitle));
		Mockito.when(definitionModelInstanceTwo.getField(HAS_PARENT)).thenReturn(
				Optional.of(propertyDefinitionHasParent));
		Mockito.when(definitionModelInstanceTwo.getField(TYPE.toLowerCase())).thenReturn(
				Optional.of(propertyDefinitionType));
		Mockito.when(definitionModelInstanceTwo.getIdentifier()).thenReturn("OT210027");
		Mockito.when(definitionService.getProperty(TITLE.toLowerCase(), instanceTwo)).thenReturn(
				propertyDefinitionTitle);
		Mockito.when(definitionService.getProperty(TYPE.toLowerCase(), instanceTwo)).thenReturn(propertyDefinitionType);
		Mockito.when(instanceTwo.get(TITLE.toLowerCase())).thenReturn(INSTANCE_TWO_TITLE);
		Mockito.when(instanceTwo.get(TYPE.toLowerCase())).thenReturn(typeId);
		Mockito.when(instanceResolver.resolveInstances(Arrays.asList(INSTANCE_ONE_ID, INSTANCE_TWO_ID))).thenReturn(
				Arrays.asList(instanceOne, instanceTwo));
	}
}