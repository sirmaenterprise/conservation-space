package com.sirma.sep.content.idoc.extensions.widgets.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.extensions.widgets.process.ProcessWidgetSearchHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget;

/**
 * Test class for {@link ProcessWidgetSearchHandler}.
 *
 * @author hlungov
 */
public class ProcessWidgetSearchHandlerTest {

	private ProcessWidgetSearchHandler processWidgetSearchHandler;

	@Before
	public void setup() {
		processWidgetSearchHandler = new ProcessWidgetSearchHandler();
	}

	@Test
	public void should_accept_incorrectType_false() {
		boolean result = processWidgetSearchHandler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void should_accept_correctType_true() {
		boolean result = processWidgetSearchHandler.accept(mock(ProcessWidget.class));
		assertTrue(result);
	}

}
