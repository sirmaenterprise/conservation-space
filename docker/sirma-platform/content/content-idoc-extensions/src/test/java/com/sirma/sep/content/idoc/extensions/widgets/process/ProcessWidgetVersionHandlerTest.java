package com.sirma.sep.content.idoc.extensions.widgets.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.extensions.widgets.process.ProcessWidgetVersionHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget;

/**
 * Test class for {@link ProcessWidgetVersionHandler}.
 *
 * @author hlungov
 */
public class ProcessWidgetVersionHandlerTest {

	private ProcessWidgetVersionHandler processWidgetVersionHandler;

	@Before
	public void setup() {
		processWidgetVersionHandler = new ProcessWidgetVersionHandler();
	}

	@Test
	public void should_accept_incorrectType_false() {
		boolean result = processWidgetVersionHandler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void should_accept_correctType_true() {
		boolean result = processWidgetVersionHandler.accept(mock(ProcessWidget.class));
		assertTrue(result);
	}

}
