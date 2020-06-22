package com.sirma.sep.content.idoc.extensions.widgets.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Base64;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.sep.content.idoc.extensions.widgets.process.ProcessWidgetRevertHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget;

/**
 * Test class for {@link ProcessWidgetRevertHandler}.
 *
 * @author hlungov
 */
public class ProcessWidgetRevertHandlerTest {

	private ProcessWidgetRevertHandler processWidgetRevertHandler;

	@Before
	public void setup() {
		processWidgetRevertHandler = new ProcessWidgetRevertHandler();
	}

	@Test
	public void should_accept_incorrectType_false() {
		boolean result = processWidgetRevertHandler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void should_accept_correctType_true() {
		boolean result = processWidgetRevertHandler.accept(mock(ProcessWidget.class));
		assertTrue(result);
	}

	@Test
	public void should_remove_process_widget_data() {
		Element node = new Element(Tag.valueOf("div"), "");
		JsonObject object = new JsonObject();
		object.add(ProcessWidget.ACTIVITY, new JsonPrimitive("activityJsonObject"));
		object.add(ProcessWidget.BPMN, new JsonPrimitive("bpmnId"));
		node.attr("config", Base64.getEncoder().encodeToString(object.toString().getBytes()));
		ProcessWidget processWidget = new ProcessWidget(node);
		processWidgetRevertHandler.handle(processWidget, null);
		assertFalse(processWidget.getConfiguration().getConfiguration().has(ProcessWidget.ACTIVITY));
		assertFalse(processWidget.getConfiguration().getConfiguration().has(ProcessWidget.BPMN));
	}

}
