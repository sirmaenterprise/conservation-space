package com.sirma.sep.content.idoc.extensions.widgets.recentactivities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.extensions.widgets.recentactivities.RecentActivitiesWidgetRevertHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidget;

/**
 * Test for {@link RecentActivitiesWidgetRevertHandler}.
 * 
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetRevertHandlerTest {

	private RecentActivitiesWidgetRevertHandler handler;

	@Before
	public void setup() {
		handler = new RecentActivitiesWidgetRevertHandler();
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

}
