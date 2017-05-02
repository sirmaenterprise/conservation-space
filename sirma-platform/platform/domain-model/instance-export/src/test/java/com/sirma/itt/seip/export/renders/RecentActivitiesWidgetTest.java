package com.sirma.itt.seip.export.renders;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesRetriever;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesSentenceGenerator;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.content.idoc.nodes.WidgetNode;

/**
 * Tests for RecentActivitiesWidget.
 *
 * @author Hristo Lungov
 */
public class RecentActivitiesWidgetTest {

	private static final String TEST_IDOC = "Test Idoc";
	private static final String TEST_USER = "Test User";
	private static final String TEST_FILE_SELECTED_MODE_CURRENT = "recent-activities-widget-current-mode.json";
	private static final String TEST_FILE_SELECTED_MODE_AUTOMATICALLY = "recent-activities-widget-automatic-mode.json";
	private static final String TEST_FILE_SELECTED_MODE_MANUALLY = "recent-activities-widget-manual-mode.json";
	private static final String WIDGET_TITLE = "Recently modified widget";
	private static final String INSTANCE_TEST_ID = "instanceTestId";
	private static final String TEST_SENTENCE = "exported a tab to Word from <span><img src=\"/images/instance-icons/documentinstance-icon-16.png\" /></span><span><a href=\"#/idoc/emf:23f8b7a6-60dd-4453-a951-e6cddfae7178\"><span>"
			+ TEST_IDOC + "</span></a></span>";
	private static final String TEST_USER_HEADER = "<span><img src=\"/images/instance-icons/userinstance-icon-16.png\" /></span><span><a href=\"#/idoc/emf:testUser\"><span>" + TEST_USER
			+ "</span></a></span>";

	@InjectMocks
	private RecentActivitiesWidget recentActivitiesWidget;

	@Mock
	private RecentActivitiesRetriever retriever;

	@Mock
	private RecentActivitiesSentenceGenerator generator;

	@Mock
	private SearchService searchService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Spy
	private JsonToConditionConverter convertor = new JsonToConditionConverter();

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Mock
	private LabelProvider labelProvider;

	/**
	 * Runs before each method and setup mockito.
	 *
	 * @throws URISyntaxException
	 */
	@BeforeMethod
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
		when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock("ui2Url"));
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
		Assert.assertFalse(recentActivitiesWidget.accept(widget));

		when(widget.isWidget()).thenReturn(true);
		when(widget.getName()).thenReturn("");
		Assert.assertFalse(recentActivitiesWidget.accept(widget));

		when(widget.isWidget()).thenReturn(false);
		when(widget.getName()).thenReturn(RecentActivitiesWidget.RECENT_ACTIVITIES_WIDGET_NAME);
		Assert.assertFalse(recentActivitiesWidget.accept(widget));

		when(widget.isWidget()).thenReturn(true);
		when(widget.getName()).thenReturn(RecentActivitiesWidget.RECENT_ACTIVITIES_WIDGET_NAME);
		Assert.assertTrue(recentActivitiesWidget.accept(widget));
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

	@Test
	public void renderSelectedObjectAutomaticallyWithIncludeCurrent() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_AUTOMATICALLY);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);

		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_TEST_ID + 1);
		List<Instance> instances = Arrays.asList(instance);

		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);
		when(searchArgs.getResult()).thenReturn(instances);
		when(searchArgs.getStringQuery()).thenReturn("");
		Sorter sorter = Mockito.mock(Sorter.class);
		List<Sorter> sorters = new ArrayList<>(1);
		sorters.add(sorter);
		when(searchArgs.getSorters()).thenReturn(sorters);
		when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);

		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(instances);
		StoredAuditActivity firstStoredActivity = Mockito.mock(StoredAuditActivity.class);
		StoredAuditActivity secondStoredActivity = Mockito.mock(StoredAuditActivity.class);
		StoredAuditActivitiesWrapper wrapper = new StoredAuditActivitiesWrapper();
		wrapper.setActivities(Arrays.asList(firstStoredActivity, secondStoredActivity));
		when(retriever.getActivities(Matchers.anyList())).thenReturn(wrapper);

		Instance user = Mockito.mock(Instance.class);
		when(user.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(TEST_USER_HEADER);
		Date date = new Date();
		FormattedDateTime formattedDateTime = new FormattedDateTime(date.toString());
		when(typeConverter.convert(Matchers.any(), Matchers.any(Date.class))).thenReturn(formattedDateTime);

		RecentActivity firstRecentActivity = Mockito.mock(RecentActivity.class);
		when(firstRecentActivity.getUser()).thenReturn(user);
		when(firstRecentActivity.getTimestamp()).thenReturn(date);
		when(firstRecentActivity.getSentence()).thenReturn(TEST_SENTENCE);

		RecentActivity secondRecentActivity = Mockito.mock(RecentActivity.class);
		when(secondRecentActivity.getUser()).thenReturn(user);
		when(secondRecentActivity.getTimestamp()).thenReturn(date);
		when(secondRecentActivity.getSentence()).thenReturn(TEST_SENTENCE);

		when(generator.generateSentences(Matchers.anyList())).thenReturn(Arrays.asList(firstRecentActivity, secondRecentActivity));

		Element table = recentActivitiesWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0) > p");
		Assert.assertEquals(tableTitleRow.text(), WIDGET_TITLE);
		for (int row = 1; row < 2; row++) {
			Elements userHref = table.select("tr:eq(" + row + ") > td:eq(0) a");
			Assert.assertEquals(userHref.text(), TEST_USER);

			Elements dateElement = table.select("tr:eq(" + row + ") > td:eq(1)");
			Assert.assertEquals(dateElement.text(), formattedDateTime.getFormatted());

			Elements actionInstanceTitle = table.select("tr:eq(" + row + ") > td:eq(2) a");
			Assert.assertEquals(actionInstanceTitle.text(), TEST_IDOC);
		}
	}

	@Test
	public void renderSelectedObjectCurrent() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_CURRENT);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);

		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		List<Instance> instances = Arrays.asList(instance);

		when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(instances);
		StoredAuditActivity storedActivity = Mockito.mock(StoredAuditActivity.class);
		StoredAuditActivitiesWrapper wrapper = new StoredAuditActivitiesWrapper();
		wrapper.setActivities(Arrays.asList(storedActivity));
		when(retriever.getActivities(Matchers.anyList())).thenReturn(wrapper);

		Instance user = Mockito.mock(Instance.class);
		when(user.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(TEST_USER_HEADER);
		Date date = new Date();
		FormattedDateTime formattedDateTime = new FormattedDateTime(date.toString());
		when(typeConverter.convert(Matchers.any(), Matchers.any(Date.class))).thenReturn(formattedDateTime);

		RecentActivity recentActivity = Mockito.mock(RecentActivity.class);
		when(recentActivity.getUser()).thenReturn(user);
		when(recentActivity.getTimestamp()).thenReturn(date);
		when(recentActivity.getSentence()).thenReturn(TEST_SENTENCE);

		when(generator.generateSentences(Matchers.anyList())).thenReturn(Arrays.asList(recentActivity));

		Element table = recentActivitiesWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Assert.assertEquals(table.select("tr:eq(0) td:eq(0) p").text(), WIDGET_TITLE);
		Assert.assertEquals(table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) a").text(), TEST_USER);
		Assert.assertEquals(table.select("tr:eq(1) td:eq(1) p").text(), formattedDateTime.getFormatted());
		Assert.assertEquals(table.select("tr:eq(1) td:eq(2) a").text(), TEST_IDOC);
	}

	@Test
	public void renderSelectedObjectManually() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(TEST_FILE_SELECTED_MODE_MANUALLY);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);

		when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));

		InstanceReference reference = Mockito.mock(InstanceReference.class);
		Mockito.when(reference.getIdentifier()).thenReturn(INSTANCE_TEST_ID);
		List<InstanceReference> references = Arrays.asList(reference);
		when(instanceResolver.resolveReferences(Matchers.anyCollection())).thenReturn(references);
		StoredAuditActivity storedActivity = Mockito.mock(StoredAuditActivity.class);
		StoredAuditActivitiesWrapper wrapper = new StoredAuditActivitiesWrapper();
		wrapper.setActivities(Arrays.asList(storedActivity));
		when(retriever.getActivities(Matchers.anyList())).thenReturn(wrapper);

		Instance user = Mockito.mock(Instance.class);
		when(user.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(TEST_USER_HEADER);
		Date date = new Date();
		FormattedDateTime formattedDateTime = new FormattedDateTime(date.toString());
		when(typeConverter.convert(Matchers.any(), Matchers.any(Date.class))).thenReturn(formattedDateTime);

		RecentActivity recentActivity = Mockito.mock(RecentActivity.class);
		when(recentActivity.getUser()).thenReturn(user);
		when(recentActivity.getTimestamp()).thenReturn(date);
		when(recentActivity.getSentence()).thenReturn(TEST_SENTENCE);

		when(generator.generateSentences(Matchers.anyList())).thenReturn(Arrays.asList(recentActivity));

		Element table = recentActivitiesWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Assert.assertEquals(table.select("tr:eq(0) td:eq(0) p").text(), WIDGET_TITLE);
		Assert.assertEquals(table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) a").text(), TEST_USER);
		Assert.assertEquals(table.select("tr:eq(1) td:eq(1) p").text(), formattedDateTime.getFormatted());
		Assert.assertEquals(table.select("tr:eq(1) td:eq(2) a").text(), TEST_IDOC);
	}

}
