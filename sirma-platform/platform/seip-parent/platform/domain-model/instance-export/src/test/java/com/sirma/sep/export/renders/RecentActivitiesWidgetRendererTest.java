package com.sirma.sep.export.renders;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for RecentActivitiesWidget.
 *
 * @author Hristo Lungov
 */
public class RecentActivitiesWidgetRendererTest {

	private static final String TEST_IDOC = "Test Idoc";
	private static final String TEST_USER = "Test User";
	private static final String TEST_FILE_SELECTED_MODE_CURRENT = "recent-activities-widget-current-mode.json";
	private static final String TEST_FILE_SELECTED_MODE_AUTOMATICALLY = "recent-activities-widget-automatic-mode.json";
	private static final String TEST_FILE_UNDEFINED_SEARCH_CRITERIA = "recent-activities-undefined-search-scenario.json";
	private static final String TEST_FILE_SELECTED_MODE_MANUALLY = "recent-activities-widget-manual-mode.json";
	private static final String WIDGET_TITLE = "Recently modified widget";
	private static final String INSTANCE_TEST_ID = "instanceTestId";
	private static final String TEST_SENTENCE = "exported a tab to Word from <span><img src=\"/images/instance-icons/documentinstance-icon-16.png\" /></span><span><a href=\"#/idoc/emf:23f8b7a6-60dd-4453-a951-e6cddfae7178\"><span>"
			+ TEST_IDOC + "</span></a></span>";
	private static final String TEST_USER_HEADER = "<span><img src=\"/images/instance-icons/userinstance-icon-16.png\" /></span><span><a href=\"#/idoc/emf:testUser\"><span>"
			+ TEST_USER + "</span></a></span>";

	@InjectMocks
	private RecentActivitiesWidgetRenderer recentActivitiesWidget;

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

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Mock
	private LabelProvider labelProvider;

	@Spy
	private JsonToConditionConverter converter = new JsonToConditionConverter();

	/**
	 * Runs before each method and setup mockito.
	 *
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
		when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock("ui2Url"));
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA)).thenReturn(
				WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
	}

	@Test
	public void should_BuildUndefinedCriteriaTable_When_WidgetConfigurationHasNotSearchCriteria()
			throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_UNDEFINED_SEARCH_CRITERIA).build();

		Element table = recentActivitiesWidget.render("instance-id", widget);

		org.junit.Assert.assertEquals(WidgetNodeBuilder.LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA, table.text());
	}

	/**
	 * Accept method test.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void acceptTest() {
		WidgetNode widget = new WidgetNodeBuilder().setIsWidget(false).setName("").build();
		Assert.assertFalse(recentActivitiesWidget.accept(widget));

		widget = new WidgetNodeBuilder().setIsWidget(true).setName("").build();
		Assert.assertFalse(recentActivitiesWidget.accept(widget));

		widget = new WidgetNodeBuilder()
				.setIsWidget(false)
					.setName(RecentActivitiesWidgetRenderer.RECENT_ACTIVITIES_WIDGET_NAME)
					.build();
		Assert.assertFalse(recentActivitiesWidget.accept(widget));

		widget = new WidgetNodeBuilder()
				.setIsWidget(true)
					.setName(RecentActivitiesWidgetRenderer.RECENT_ACTIVITIES_WIDGET_NAME)
					.build();
		Assert.assertTrue(recentActivitiesWidget.accept(widget));
	}

	@Test
	public void renderSelectedObjectAutomaticallyWithIncludeCurrent() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_AUTOMATICALLY).build();
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
		when(generator.generateSentences(Matchers.anyList()))
				.thenReturn(Arrays.asList(firstRecentActivity, secondRecentActivity));

		Element table = recentActivitiesWidget.render(INSTANCE_TEST_ID, widget);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0) > p");
		Assert.assertEquals(WIDGET_TITLE, tableTitleRow.text());
		for (int row = 1; row < 2; row++) {
			Elements userHref = table.select("tr:eq(" + row + ") > td:eq(0) a");
			Assert.assertEquals(TEST_USER, userHref.text());

			Elements dateElement = table.select("tr:eq(" + row + ") > td:eq(1)");
			Assert.assertEquals(formattedDateTime.getFormatted(), dateElement.text());

			Elements actionInstanceTitle = table.select("tr:eq(" + row + ") > td:eq(2) a");
			Assert.assertEquals(TEST_IDOC, actionInstanceTitle.text());
		}
	}

	@Test
	public void renderSelectedObjectCurrent() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_CURRENT).build();
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

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Assert.assertEquals(WIDGET_TITLE, table.select("tr:eq(0) td:eq(0) p").text());
		Assert.assertEquals(TEST_USER, table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) a").text());
		Assert.assertEquals(formattedDateTime.getFormatted(), table.select("tr:eq(1) td:eq(1) p").text());
		Assert.assertEquals(TEST_IDOC, table.select("tr:eq(1) td:eq(2) a").text());
	}

	@Test
	public void renderSelectedObjectManually() throws URISyntaxException, IOException {
		WidgetNode widget = new WidgetNodeBuilder().setConfiguration(TEST_FILE_SELECTED_MODE_MANUALLY).build();
		InstanceReference reference = Mockito.mock(InstanceReference.class);
		Mockito.when(reference.getId()).thenReturn(INSTANCE_TEST_ID);
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

		Assert.assertEquals(JsoupUtil.TAG_TABLE, table.tagName());
		Assert.assertEquals(WIDGET_TITLE, table.select("tr:eq(0) td:eq(0) p").text());
		Assert.assertEquals(TEST_USER, table.select("tr:eq(1) td:eq(0) table tr:eq(0) td:eq(1) a").text());
		Assert.assertEquals(formattedDateTime.getFormatted(), table.select("tr:eq(1) td:eq(1) p").text());
		Assert.assertEquals(TEST_IDOC, table.select("tr:eq(1) td:eq(2) a").text());
	}
}
