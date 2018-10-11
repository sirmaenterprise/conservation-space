package com.sirma.sep.content.idoc.extensions.widgets.objectlink;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.extensions.widgets.objectlink.ObjectLinkWidgetVersionHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.insertlink.ObjectLinkWidget;

/**
 * Test for {@link ObjectLinkWidgetVersionHandler}.
 *
 * @author A. Kunchev
 */
public class ObjectLinkWidgetVersionHandlerTest {

	private ObjectLinkWidgetVersionHandler handler;

	@Before
	public void setup() {
		handler = new ObjectLinkWidgetVersionHandler();
	}

	@Test
	public void accept_incorrectType_false() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctType_true() {
		boolean result = handler.accept(mock(ObjectLinkWidget.class));
		assertTrue(result);
	}
}
