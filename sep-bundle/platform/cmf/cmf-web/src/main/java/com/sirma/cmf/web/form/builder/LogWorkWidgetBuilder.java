package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for widget that allows logging work on task.
 * 
 * @iskrenborisov
 */
public class LogWorkWidgetBuilder extends ControlBuilder {

	/** The ui params. */
	private Map<String, ControlParam> controlParams;

	/**
	 * Instantiates a new log work widget builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public LogWorkWidgetBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build() {
		String displayStatusKey = getRenderedStatusKey(propertyDefinition, formViewMode);
		boolean displayStatus = renderStatusMap.get(displayStatusKey);

		if (trace) {
			String msg = MessageFormat.format(
					"CMFWeb: building property [{0}] with display status key [{1} = {2}]",
					propertyDefinition.getName(), displayStatusKey, displayStatus);
			log.trace(msg);
		}

		controlParams = getAsMap(propertyDefinition.getControlDefinition().getControlParams());

		// if display status is true, then go ahead and build the field
		if (displayStatus) {

			// build a wrapper for the label and field
			UIComponent wrapper = buildFieldWrapper();
			addStyleClass(wrapper, "log-work-widget-wrapper");
			List<UIComponent> children = wrapper.getChildren();

			HtmlPanelGroup widgetWrapper = (HtmlPanelGroup) builderHelper
					.getComponent(ComponentType.OUTPUT_PANEL);
			addStyleClass(widgetWrapper, "log-work-widget");
			children.add(widgetWrapper);

			List<UIComponent> containerChildren = container.getChildren();
			containerChildren.add(wrapper);

			boolean previewMode = false;
			if (controlParams != null) {
				ControlParam previewParam = controlParams.get("PREVIEW_MODE");
				if (previewParam != null) {
					previewMode = Boolean.valueOf(previewParam.getValue());
				}
			}

			containerChildren.add(createPluginScript());
			containerChildren.add(createInitScript(previewMode));
		}
	}

	/**
	 * Injects plugin script into the page.
	 * 
	 * @return the html output text
	 */
	private HtmlOutputText createPluginScript() {
		return getScriptFileInject("extjs.emf.log-work-on-task.js");
	}

	/**
	 * Creates the init script.
	 * 
	 * @param previewMode
	 *            boolean flag for setting preview mode
	 * @return the html output text
	 */
	private HtmlOutputText createInitScript(boolean previewMode) {
		String script = "Ext.onReady(function() {" + "$('.log-work-widget').extjsLogWorkOnTask({"
				+ "contextPath		: SF.config.contextPath,"
				+ "taskId		: EMF.documentContext.currentInstanceId," + "preview		: " + previewMode
				+ "});});";
		HtmlOutputText scriptOutput = getScriptOutput(script);
		return scriptOutput;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent buildField() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		return null;
	}
}
