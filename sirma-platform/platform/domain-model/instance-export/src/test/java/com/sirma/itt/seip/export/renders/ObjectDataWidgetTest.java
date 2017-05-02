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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonParser;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.util.LinkProviderService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirmaenterprise.sep.content.idoc.ContentNodeFactory;
import com.sirmaenterprise.sep.content.idoc.Idoc;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.content.idoc.nodes.WidgetNode;
import com.sirmaenterprise.sep.content.idoc.nodes.layout.LayoutManagerBuilder;
import com.sirmaenterprise.sep.content.idoc.nodes.layout.LayoutNodeBuilder;

/**
 * ObjectDataWidget class with tests.
 * 
 * @author Hristo Lungov
 */
public class ObjectDataWidgetTest {

	private static final String WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND = "No object could be found with the selected search criteria.";
	private static final String WARN_MESSAGE_NO_SELECTION = "No Selection.";
	private static final String NAME = "name";
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String CREATED_BY = "createdBy";
	private static final String MODIFIED_BY = "modifiedBy";
	private static final String MODIFIED_ON = "modifiedOn";
	private static final String TYPE = "type";
	private static final String CREATED_ON = "createdOn";
	private static final String TENANT_ID_USER_ID = "(tenantId) userId";
	private static final String WIDGET_TITLE = "First Widget";
	private static final String OBJECT_DATA_WIDGET_HTML_FILE = "object-data-widget.html";
	private static final String OBJECT_DATA_WIDGET_ID = "45b79fd0-5c3c-4044-d700-e294b316317e";
	private static final String INSTANCE_ID = "instanceId";
	private static final String USER_HEADER = "<span><img src=\"some image src\" /></span><span><a class=\"instance-link has-tooltip\" href=\"#/idoc/emf:a117c235-e5f7-4417-b45c-c637db3a09e4\">(<span data-property=\"type\">tenantId</span>) <span data-property=\"title\">userId</span></a></span>";
	private static final List<String> SELECTED_PROPERTIES = Arrays.asList(TITLE, CREATED_ON, CREATED_BY, MODIFIED_ON, MODIFIED_BY, NAME, TYPE, DESCRIPTION);

	private static final String TEST_FILE_NO_OBJECTS_FOUND = "object-data-widget-automatically-mode-no-objects-found.json";
	private static final String TEST_FILE_NO_SELECTED_OBJECTS = "object-data-widget-manually-mode-no-objects-found.json";

	@InjectMocks
	private ObjectDataWidget objectDataWidget;

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
	private JsonToConditionConverter convertor = new JsonToConditionConverter();

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;
	
	@Mock
	private ConditionsManager conditionsManager;
	
	@Mock
	private DefinitionHelper definitionHelper;


	/**
	 * Runs before each method and setup mockito.
	 */
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>(""));
	}

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
	 * No objects found test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void noObjectsFoundTest() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_NO_OBJECTS_FOUND);
		WidgetNode widget = Mockito.mock(WidgetNode.class);
		
		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);
		when(searchArgs.getResult()).thenReturn(Collections.emptyList());
		when(searchArgs.getStringQuery()).thenReturn("");
		Sorter sorter = Mockito.mock(Sorter.class);
		List<Sorter> sorters = new ArrayList<>(1);
		sorters.add(sorter);
		when(searchArgs.getSorters()).thenReturn(sorters);
		when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Collections.emptyList());

		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECT_OBJECT_RESULTS_NONE)).thenReturn(WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND);

		Element table = objectDataWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Assert.assertEquals(tableMessageRow.text(), WARN_MESSAGE_NO_OBJECT_COULD_BE_FOUND);
	}

	/**
	 * No selected objects test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void noSelectedObjectsTest() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_NO_SELECTED_OBJECTS);
		WidgetNode widget = Mockito.mock(WidgetNode.class);
		
		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Collections.emptyList());

		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECT_OBJECT_NONE)).thenReturn(WARN_MESSAGE_NO_SELECTION);

		Element table = objectDataWidget.render(INSTANCE_ID, widget);

		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);

		Elements tableMessageRow = table.select("tr:eq(1) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(tableTitleRow.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Assert.assertEquals(tableMessageRow.text(), WARN_MESSAGE_NO_SELECTION);
	}

	/**
	 * Test accept method of widget.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void testAccept() {
		WidgetNode widget = Mockito.mock(WidgetNode.class);
		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn(ObjectDataWidget.OBJECT_DATA_WIDGET_NAME);
		Assert.assertTrue(objectDataWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(false);
		Assert.assertFalse(objectDataWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn("");
		Assert.assertFalse(objectDataWidget.accept(widget));
	}

	/**
	 * Test render method of widget scenario without regions.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRenderWithoutRegion() throws IOException {
		try (InputStream testIs = getClass().getClassLoader().getResourceAsStream(OBJECT_DATA_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(testIs);
			Optional<Widget> selectedWidget = idoc.selectWidget(OBJECT_DATA_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			Date date = new Date();
			
			DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
			when(dictionaryService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
			List<Ordinal> allFields = new LinkedList<>();
			
			for (String property : SELECTED_PROPERTIES) {
				PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
				propertyDefinition.setLabelId(property);
				propertyDefinition.setIdentifier(property);
				when(dictionaryService.getProperty(property, instance)).thenReturn(propertyDefinition);
				allFields.add(propertyDefinition);
				switch (property) {
					case DESCRIPTION:
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
						propertyDefinition.setControlDefinition(controlDefinition);
						PropertyDefinition controlPropertyDefinition = Mockito.mock(PropertyDefinition.class);
						when(controlPropertyDefinition.getLabel()).thenReturn(property.toUpperCase());
						when(controlDefinition.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinition));
						break;
					case TYPE:
						propertyDefinition.setCodelist(200);
						when(codelistService.getDescription(200, property.toUpperCase())).thenReturn(property.toUpperCase());
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						break;
					case CREATED_ON:
						propertyDefinition.setType(DataTypeDefinition.DATETIME);
						when(instance.get(property)).thenReturn(date);
						when(typeConverter.convert(FormattedDateTime.class, date)).thenReturn(new FormattedDateTime(date.toString()));
						break;
					case MODIFIED_ON:
						propertyDefinition.setType(DataTypeDefinition.DATE);
						when(instance.get(property)).thenReturn(date);
						when(typeConverter.convert(FormattedDate.class, date)).thenReturn(new FormattedDate(date.toString()));
						break;
					case MODIFIED_BY:
						propertyDefinition.setType(DataTypeDefinition.URI);
						when(instance.get(property)).thenReturn(property.toUpperCase());
						break;
					case CREATED_BY:
						propertyDefinition.setType(DataTypeDefinition.URI);
						when(instance.get(property)).thenReturn(property.toUpperCase());
						break;
					default:
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						break;
				}
			}
			when(definitionHelper.collectAllFields(instanceDefinition)).thenReturn(allFields);
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(USER_HEADER);
			when(instance.getString(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(USER_HEADER);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			Element table = objectDataWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0) p").text(), WIDGET_TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(0) p").text(), TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(1) p").text(), TITLE.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(2) > td:eq(0) p").text(), CREATED_ON);
			Assert.assertNotNull(table.select("tr:eq(2) > td:eq(1) p").text());
			Assert.assertEquals(table.select("tr:eq(3) > td:eq(0) p").text(), CREATED_BY);
			Assert.assertEquals(table.select("tr:eq(3) > td:eq(1) table tr:eq(0) td:eq(1) a").text(), TENANT_ID_USER_ID);
			Assert.assertEquals(table.select("tr:eq(4) > td:eq(0) p").text(), MODIFIED_ON);
			Assert.assertNotNull(table.select("tr:eq(4) > td:eq(1) p").text());
			Assert.assertEquals(table.select("tr:eq(5) > td:eq(0) p").text(), MODIFIED_BY);
			Assert.assertEquals(table.select("tr:eq(5) > td:eq(1) table tr:eq(0) td:eq(1) a").text(), TENANT_ID_USER_ID);
			Assert.assertEquals(table.select("tr:eq(6) > td:eq(0) p").text(), NAME);
			Assert.assertEquals(table.select("tr:eq(6) > td:eq(1) p").text(), NAME.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(7) > td:eq(0) p").text(), TYPE);
			Assert.assertEquals(table.select("tr:eq(7) > td:eq(1) p").text(), TYPE.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(8) > td:eq(0) p").text(), DESCRIPTION);
			Assert.assertEquals(table.select("tr:eq(8) > td:eq(1) p").text(), DESCRIPTION.toUpperCase());
		}
	}
	
	/**
	 * Test render method of widget scenario with region.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRenderWithRegion() throws IOException {
		try (InputStream testIs = getClass().getClassLoader().getResourceAsStream(OBJECT_DATA_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(testIs);
			Optional<Widget> selectedWidget = idoc.selectWidget(OBJECT_DATA_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			Date date = new Date();
			
			DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
			when(dictionaryService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
			List<PropertyDefinition> allFields = new LinkedList<>();
			for (String property : SELECTED_PROPERTIES) {
				PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
				propertyDefinition.setLabelId(property);
				propertyDefinition.setIdentifier(property);
				allFields.add(propertyDefinition);
				when(dictionaryService.getProperty(property, instance)).thenReturn(propertyDefinition);
				switch (property) {
					case DESCRIPTION:
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
						propertyDefinition.setControlDefinition(controlDefinition);
						PropertyDefinition controlPropertyDefinition = Mockito.mock(PropertyDefinition.class);
						when(controlPropertyDefinition.getLabel()).thenReturn(property.toUpperCase());
						when(controlDefinition.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinition));
						break;
					case TYPE:
						propertyDefinition.setCodelist(200);
						when(codelistService.getDescription(200, property.toUpperCase())).thenReturn(property.toUpperCase());
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						break;
					case CREATED_ON:
						propertyDefinition.setType(DataTypeDefinition.DATETIME);
						when(instance.get(property)).thenReturn(date);
						when(typeConverter.convert(FormattedDateTime.class, date)).thenReturn(new FormattedDateTime(date.toString()));
						break;
					case MODIFIED_ON:
						propertyDefinition.setType(DataTypeDefinition.DATE);
						when(instance.get(property)).thenReturn(date);
						when(typeConverter.convert(FormattedDate.class, date)).thenReturn(new FormattedDate(date.toString()));
						break;
					case MODIFIED_BY:
						propertyDefinition.setType(DataTypeDefinition.URI);
						when(instance.get(property)).thenReturn(property.toUpperCase());
						break;
					case CREATED_BY:
						propertyDefinition.setType(DataTypeDefinition.URI);
						when(instance.get(property)).thenReturn(property.toUpperCase());
						break;
					default:
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						break;
				}
			}
			
			
			RegionDefinition regionDefinition = Mockito.mock(RegionDefinition.class);
			when(regionDefinition.getDisplayType()).thenReturn(DisplayType.HIDDEN);
			when(conditionsManager.evalPropertyConditions(regionDefinition, ConditionType.HIDDEN, instance)).thenReturn(false);
			when(regionDefinition.getFields()).thenReturn(allFields);
			
			when(definitionHelper.collectAllFields(instanceDefinition)).thenReturn(Arrays.asList(regionDefinition));
			
			
			
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(USER_HEADER);
			when(instance.getString(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(USER_HEADER);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			Element table = objectDataWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0) p").text(), WIDGET_TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(0) p").text(), TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(1) p").text(), TITLE.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(2) > td:eq(0) p").text(), CREATED_ON);
			Assert.assertNotNull(table.select("tr:eq(2) > td:eq(1) p").text());
			Assert.assertEquals(table.select("tr:eq(3) > td:eq(0) p").text(), CREATED_BY);
			Assert.assertEquals(table.select("tr:eq(3) > td:eq(1) table tr:eq(0) td:eq(1) a").text(), TENANT_ID_USER_ID);
			Assert.assertEquals(table.select("tr:eq(4) > td:eq(0) p").text(), MODIFIED_ON);
			Assert.assertNotNull(table.select("tr:eq(4) > td:eq(1) p").text());
			Assert.assertEquals(table.select("tr:eq(5) > td:eq(0) p").text(), MODIFIED_BY);
			Assert.assertEquals(table.select("tr:eq(5) > td:eq(1) table tr:eq(0) td:eq(1) a").text(), TENANT_ID_USER_ID);
			Assert.assertEquals(table.select("tr:eq(6) > td:eq(0) p").text(), NAME);
			Assert.assertEquals(table.select("tr:eq(6) > td:eq(1) p").text(), NAME.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(7) > td:eq(0) p").text(), TYPE);
			Assert.assertEquals(table.select("tr:eq(7) > td:eq(1) p").text(), TYPE.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(8) > td:eq(0) p").text(), DESCRIPTION);
			Assert.assertEquals(table.select("tr:eq(8) > td:eq(1) p").text(), DESCRIPTION.toUpperCase());
		}
	}
	
	/**
	 * Test render method of widget scenario with hidden region and condition true.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRenderWithRegionTrueCondition() throws IOException {
		try (InputStream testIs = getClass().getClassLoader().getResourceAsStream(OBJECT_DATA_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(testIs);
			Optional<Widget> selectedWidget = idoc.selectWidget(OBJECT_DATA_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			Date date = new Date();
			
			DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
			when(dictionaryService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
			List<PropertyDefinition> allFields = new LinkedList<>();
			for (String property : SELECTED_PROPERTIES) {
				PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
				propertyDefinition.setLabelId(property);
				propertyDefinition.setIdentifier(property);
				allFields.add(propertyDefinition);
				when(dictionaryService.getProperty(property, instance)).thenReturn(propertyDefinition);
				switch (property) {
					case DESCRIPTION:
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
						propertyDefinition.setControlDefinition(controlDefinition);
						PropertyDefinition controlPropertyDefinition = Mockito.mock(PropertyDefinition.class);
						when(controlPropertyDefinition.getLabel()).thenReturn(property.toUpperCase());
						when(controlDefinition.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinition));
						break;
					case TYPE:
						propertyDefinition.setCodelist(200);
						when(codelistService.getDescription(200, property.toUpperCase())).thenReturn(property.toUpperCase());
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						break;
					case CREATED_ON:
						propertyDefinition.setType(DataTypeDefinition.DATETIME);
						when(instance.get(property)).thenReturn(date);
						when(typeConverter.convert(FormattedDateTime.class, date)).thenReturn(new FormattedDateTime(date.toString()));
						break;
					case MODIFIED_ON:
						propertyDefinition.setType(DataTypeDefinition.DATE);
						when(instance.get(property)).thenReturn(date);
						when(typeConverter.convert(FormattedDate.class, date)).thenReturn(new FormattedDate(date.toString()));
						break;
					case MODIFIED_BY:
						propertyDefinition.setType(DataTypeDefinition.URI);
						when(instance.get(property)).thenReturn(property.toUpperCase());
						break;
					case CREATED_BY:
						propertyDefinition.setType(DataTypeDefinition.URI);
						when(instance.get(property)).thenReturn(property.toUpperCase());
						break;
					default:
						when(instance.get(property)).thenReturn(property.toUpperCase());
						propertyDefinition.setType(DataTypeDefinition.TEXT);
						break;
				}
			}
			
			
			RegionDefinition regionDefinition = Mockito.mock(RegionDefinition.class);
			when(regionDefinition.getDisplayType()).thenReturn(DisplayType.HIDDEN);
			when(conditionsManager.evalPropertyConditions(regionDefinition, ConditionType.HIDDEN, instance)).thenReturn(false);
			when(regionDefinition.getFields()).thenReturn(allFields);
			
			when(definitionHelper.collectAllFields(instanceDefinition)).thenReturn(Arrays.asList(regionDefinition));
			
			
			
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(USER_HEADER);
			when(instance.getString(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(USER_HEADER);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			Element table = objectDataWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0) p").text(), WIDGET_TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(0) p").text(), TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(1) p").text(), TITLE.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(2) > td:eq(0) p").text(), CREATED_ON);
			Assert.assertNotNull(table.select("tr:eq(2) > td:eq(1) p").text());
			Assert.assertEquals(table.select("tr:eq(3) > td:eq(0) p").text(), CREATED_BY);
			Assert.assertEquals(table.select("tr:eq(3) > td:eq(1) table tr:eq(0) td:eq(1) a").text(), TENANT_ID_USER_ID);
			Assert.assertEquals(table.select("tr:eq(4) > td:eq(0) p").text(), MODIFIED_ON);
			Assert.assertNotNull(table.select("tr:eq(4) > td:eq(1) p").text());
			Assert.assertEquals(table.select("tr:eq(5) > td:eq(0) p").text(), MODIFIED_BY);
			Assert.assertEquals(table.select("tr:eq(5) > td:eq(1) table tr:eq(0) td:eq(1) a").text(), TENANT_ID_USER_ID);
			Assert.assertEquals(table.select("tr:eq(6) > td:eq(0) p").text(), NAME);
			Assert.assertEquals(table.select("tr:eq(6) > td:eq(1) p").text(), NAME.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(7) > td:eq(0) p").text(), TYPE);
			Assert.assertEquals(table.select("tr:eq(7) > td:eq(1) p").text(), TYPE.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(8) > td:eq(0) p").text(), DESCRIPTION);
			Assert.assertEquals(table.select("tr:eq(8) > td:eq(1) p").text(), DESCRIPTION.toUpperCase());
		}
	}
	
	/**
	 * Test render method of widget scenario with hidden region and condition false.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRenderWithRegionFalseCondition() throws IOException {
		try (InputStream testIs = getClass().getClassLoader().getResourceAsStream(OBJECT_DATA_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(testIs);
			Optional<Widget> selectedWidget = idoc.selectWidget(OBJECT_DATA_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			
			DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
			when(dictionaryService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
			String propertyType = TYPE;
			PropertyDefinitionMock propertyDefinitionType = new PropertyDefinitionMock();
			propertyDefinitionType.setLabelId(propertyType);
			propertyDefinitionType.setIdentifier(propertyType);
			when(dictionaryService.getProperty(propertyType, instance)).thenReturn(propertyDefinitionType);
			when(instance.get(propertyType)).thenReturn(propertyType.toUpperCase());
			propertyDefinitionType.setType(DataTypeDefinition.TEXT);
			ControlDefinition controlDefinitionType = Mockito.mock(ControlDefinition.class);
			propertyDefinitionType.setControlDefinition(controlDefinitionType);
			PropertyDefinition controlPropertyDefinitionType = Mockito.mock(PropertyDefinition.class);
			when(controlPropertyDefinitionType.getLabel()).thenReturn(propertyType.toUpperCase());
			when(controlDefinitionType.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinitionType));
			
			
			RegionDefinition regionDefinition = Mockito.mock(RegionDefinition.class);
			when(regionDefinition.getDisplayType()).thenReturn(DisplayType.HIDDEN);
			when(conditionsManager.evalPropertyConditions(regionDefinition, ConditionType.HIDDEN, instance)).thenReturn(true);
			
			when(definitionHelper.collectAllFields(instanceDefinition)).thenReturn(Arrays.asList(regionDefinition, propertyDefinitionType));
			
			String property = DESCRIPTION;
			PropertyDefinitionMock regionPropertyDefinition = new PropertyDefinitionMock();
			regionPropertyDefinition.setLabelId(property);
			regionPropertyDefinition.setIdentifier(property);
			when(dictionaryService.getProperty(property, instance)).thenReturn(regionPropertyDefinition);
			when(instance.get(property)).thenReturn(property.toUpperCase());
			regionPropertyDefinition.setType(DataTypeDefinition.TEXT);
			ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
			regionPropertyDefinition.setControlDefinition(controlDefinition);
			PropertyDefinition controlPropertyDefinition = Mockito.mock(PropertyDefinition.class);
			when(controlPropertyDefinition.getLabel()).thenReturn(property.toUpperCase());
			when(controlDefinition.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinition));
			
			when(regionDefinition.getFields()).thenReturn(Arrays.asList(regionPropertyDefinition));
			
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(USER_HEADER);
			when(instance.getString(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(USER_HEADER);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			Element table = objectDataWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
			Assert.assertEquals(table.select("tr").size(), 2);
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0) p").text(), WIDGET_TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(0) p").text(), TYPE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(1) p").text(), TYPE.toUpperCase());
		}
	}
	
	/**
	 * Test render method of widget scenario with system region and condition true.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRenderWithSystemRegionTrueCondition() throws IOException {
		try (InputStream testIs = getClass().getClassLoader().getResourceAsStream(OBJECT_DATA_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(testIs);
			Optional<Widget> selectedWidget = idoc.selectWidget(OBJECT_DATA_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			
			DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
			when(dictionaryService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
			String propertyType = TYPE;
			PropertyDefinitionMock propertyDefinitionType = new PropertyDefinitionMock();
			propertyDefinitionType.setLabelId(propertyType);
			propertyDefinitionType.setIdentifier(propertyType);
			when(dictionaryService.getProperty(propertyType, instance)).thenReturn(propertyDefinitionType);
			when(instance.get(propertyType)).thenReturn(propertyType.toUpperCase());
			propertyDefinitionType.setType(DataTypeDefinition.TEXT);
			ControlDefinition controlDefinitionType = Mockito.mock(ControlDefinition.class);
			propertyDefinitionType.setControlDefinition(controlDefinitionType);
			PropertyDefinition controlPropertyDefinitionType = Mockito.mock(PropertyDefinition.class);
			when(controlPropertyDefinitionType.getLabel()).thenReturn(propertyType.toUpperCase());
			when(controlDefinitionType.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinitionType));
			
			
			RegionDefinition regionDefinition = Mockito.mock(RegionDefinition.class);
			when(regionDefinition.getDisplayType()).thenReturn(DisplayType.SYSTEM);
			when(conditionsManager.evalPropertyConditions(regionDefinition, ConditionType.HIDDEN, instance)).thenReturn(true);
			
			when(definitionHelper.collectAllFields(instanceDefinition)).thenReturn(Arrays.asList(regionDefinition, propertyDefinitionType));
			
			String property = DESCRIPTION;
			PropertyDefinitionMock regionPropertyDefinition = new PropertyDefinitionMock();
			regionPropertyDefinition.setLabelId(property);
			regionPropertyDefinition.setIdentifier(property);
			when(dictionaryService.getProperty(property, instance)).thenReturn(regionPropertyDefinition);
			when(instance.get(property)).thenReturn(property.toUpperCase());
			regionPropertyDefinition.setType(DataTypeDefinition.TEXT);
			ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
			regionPropertyDefinition.setControlDefinition(controlDefinition);
			PropertyDefinition controlPropertyDefinition = Mockito.mock(PropertyDefinition.class);
			when(controlPropertyDefinition.getLabel()).thenReturn(property.toUpperCase());
			when(controlDefinition.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinition));
			
			when(regionDefinition.getFields()).thenReturn(Arrays.asList(regionPropertyDefinition));
			
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(USER_HEADER);
			when(instance.getString(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(USER_HEADER);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			Element table = objectDataWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
			Assert.assertEquals(table.select("tr").size(), 2);
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0) p").text(), WIDGET_TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(0) p").text(), TYPE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(1) p").text(), TYPE.toUpperCase());
		}
	}
	
	/**
	 * Test render method of widget scenario with system region and condition false.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRenderWithHiddenRegionFalseCondition() throws IOException {
		try (InputStream testIs = getClass().getClassLoader().getResourceAsStream(OBJECT_DATA_WIDGET_HTML_FILE)) {
			Idoc idoc = Idoc.parse(testIs);
			Optional<Widget> selectedWidget = idoc.selectWidget(OBJECT_DATA_WIDGET_ID);
			Instance instance = Mockito.mock(Instance.class);
			when(instance.getId()).thenReturn(INSTANCE_ID);
			
			DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
			when(dictionaryService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
			String propertyType = DESCRIPTION;
			PropertyDefinitionMock propertyDefinitionType = new PropertyDefinitionMock();
			propertyDefinitionType.setLabelId(propertyType);
			propertyDefinitionType.setIdentifier(propertyType);
			when(dictionaryService.getProperty(propertyType, instance)).thenReturn(propertyDefinitionType);
			when(instance.get(propertyType)).thenReturn(propertyType.toUpperCase());
			propertyDefinitionType.setType(DataTypeDefinition.TEXT);
			ControlDefinition controlDefinitionType = Mockito.mock(ControlDefinition.class);
			propertyDefinitionType.setControlDefinition(controlDefinitionType);
			PropertyDefinition controlPropertyDefinitionType = Mockito.mock(PropertyDefinition.class);
			when(controlPropertyDefinitionType.getLabel()).thenReturn(propertyType.toUpperCase());
			when(controlDefinitionType.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinitionType));
			
			
			RegionDefinition regionDefinition = Mockito.mock(RegionDefinition.class);
			when(regionDefinition.getDisplayType()).thenReturn(DisplayType.HIDDEN);
			when(conditionsManager.evalPropertyConditions(regionDefinition, ConditionType.HIDDEN, instance)).thenReturn(false);
			
			when(definitionHelper.collectAllFields(instanceDefinition)).thenReturn(Arrays.asList(regionDefinition, propertyDefinitionType));
			
			String property = TYPE;
			PropertyDefinitionMock regionPropertyDefinition = new PropertyDefinitionMock();
			regionPropertyDefinition.setLabelId(property);
			regionPropertyDefinition.setIdentifier(property);
			when(dictionaryService.getProperty(property, instance)).thenReturn(regionPropertyDefinition);
			when(instance.get(property)).thenReturn(property.toUpperCase());
			regionPropertyDefinition.setType(DataTypeDefinition.TEXT);
			ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
			regionPropertyDefinition.setControlDefinition(controlDefinition);
			PropertyDefinition controlPropertyDefinition = Mockito.mock(PropertyDefinition.class);
			when(controlPropertyDefinition.getLabel()).thenReturn(property.toUpperCase());
			when(controlDefinition.getField(Matchers.anyString())).thenReturn(Optional.of(controlPropertyDefinition));
			
			when(regionDefinition.getFields()).thenReturn(Arrays.asList(regionPropertyDefinition));
			
			when(instance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(USER_HEADER);
			when(instance.getString(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(USER_HEADER);
			when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
			Element table = objectDataWidget.render(INSTANCE_ID, selectedWidget.get());
			Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
			Assert.assertEquals(table.select("tr").size(), 3);
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "2");
			Assert.assertEquals(table.select("tr:eq(0) > td:eq(0) p").text(), WIDGET_TITLE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(0) p").text(), TYPE);
			Assert.assertEquals(table.select("tr:eq(1) > td:eq(1) p").text(), TYPE.toUpperCase());
			Assert.assertEquals(table.select("tr:eq(2) > td:eq(0) p").text(), DESCRIPTION);
			Assert.assertEquals(table.select("tr:eq(2) > td:eq(1) p").text(), DESCRIPTION.toUpperCase());
		}
	}
}
