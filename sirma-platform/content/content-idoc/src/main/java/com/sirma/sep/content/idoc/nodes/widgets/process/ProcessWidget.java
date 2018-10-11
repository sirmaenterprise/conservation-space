package com.sirma.sep.content.idoc.nodes.widgets.process;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default Process widget implementation.
 *
 * @author hlungov
 */
public class ProcessWidget extends WidgetNode {

	public static final String NAME = "business-process-diagram-widget";
	public static final String BPMN = "bpmn";
	public static final String ACTIVITY = "activity";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public ProcessWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new ProcessWidgetConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new ProcessWidgetConfiguration(this, config);
	}

}