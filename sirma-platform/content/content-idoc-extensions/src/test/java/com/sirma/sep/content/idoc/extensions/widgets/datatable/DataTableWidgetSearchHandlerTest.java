package com.sirma.sep.content.idoc.extensions.widgets.datatable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.content.idoc.extensions.widgets.datatable.DataTableWidgetSearchHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.datatable.DataTableWidget;

/**
 * Test for {@link DataTableWidgetSearchHandler}.
 *
 * @author A. Kunchev
 */
public class DataTableWidgetSearchHandlerTest {

	private DataTableWidgetSearchHandler handler;

	@Before
	public void setup() {
		handler = new DataTableWidgetSearchHandler();
	}

	@Test
	public void accept_incorrectNodeType() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctNodeType() {
		boolean result = handler.accept(mock(DataTableWidget.class));
		assertTrue(result);
	}

}
