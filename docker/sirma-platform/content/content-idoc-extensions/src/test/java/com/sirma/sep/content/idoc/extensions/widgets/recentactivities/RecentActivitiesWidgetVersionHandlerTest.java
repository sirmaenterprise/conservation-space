package com.sirma.sep.content.idoc.extensions.widgets.recentactivities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.seip.instance.version.VersionProperties.WidgetsHandlerContextProperties;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.WidgetSelectionMode;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidget;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidgetConfiguration;

/**
 * Test for {@link RecentActivitiesWidgetVersionHandler}.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetVersionHandlerTest {

	private RecentActivitiesWidgetVersionHandler handler;

	@Before
	public void setup() {
		handler = new RecentActivitiesWidgetVersionHandler();
	}

	@Test
	public void accept_incorrectWidgetType() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctWidgetType() {
		boolean result = handler.accept(mock(RecentActivitiesWidget.class));
		assertTrue(result);
	}

	@Test
	public void processResults() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		RecentActivitiesWidget widget = new RecentActivitiesWidget(node);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("storedActivities", buildSearchResults());
		resultMap.put("instanceIds", Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3"));
		HandlerContext context = new HandlerContext();
		context.put(WidgetsHandlerContextProperties.VERSION_DATE_KEY, new Date());
		handler.processResults(widget, WidgetResults.fromSearch(resultMap), context);
		RecentActivitiesWidgetConfiguration configuration = widget.getConfiguration();
		assertNotNull(configuration);
		assertEquals(WidgetSelectionMode.MANUALLY, configuration.getSelectionMode());
		assertEquals(3, configuration.getAllSelectedObjects().size());
	}

	private static StoredAuditActivitiesWrapper buildSearchResults() {
		List<StoredAuditActivity> activities = buildActivities(4);
		return new StoredAuditActivitiesWrapper().setActivities(activities).setTotal(activities.size());
	}

	private static List<StoredAuditActivity> buildActivities(int numberOfActivities) {
		List<StoredAuditActivity> activities = new ArrayList<>(numberOfActivities);
		for (int i = 0; i < numberOfActivities; i++) {
			StoredAuditActivity activity = new StoredAuditActivity();
			activity.setInstanceId("activity-id-" + i);
			activity.setUserId("user-id-" + i);
			activity.setTimestamp(new Date());
			activities.add(activity);
		}

		return activities;
	}
}