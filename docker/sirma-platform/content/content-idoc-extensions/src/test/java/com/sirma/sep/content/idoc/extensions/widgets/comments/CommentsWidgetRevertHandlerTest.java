package com.sirma.sep.content.idoc.extensions.widgets.comments;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.extensions.widgets.comments.CommentsWidgetRevertHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidget;

/**
 * Test for {@link CommentsWidgetRevertHandler}.
 *
 * @author A. Kunchev
 */
public class CommentsWidgetRevertHandlerTest {

	private CommentsWidgetRevertHandler handler;

	@Before
	public void setup() {
		handler = new CommentsWidgetRevertHandler();
	}

	@Test
	public void accept_incorrectWidgetType_false() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctWidgetType_false() {
		boolean result = handler.accept(mock(CommentsWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle() {
		// TBD
	}

}
