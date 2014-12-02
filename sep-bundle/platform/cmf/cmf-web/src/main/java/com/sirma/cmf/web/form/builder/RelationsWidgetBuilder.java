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
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * @author svelikov
 */
public class RelationsWidgetBuilder extends ControlBuilder {

	/** The ui params. */
	private Map<String, ControlParam> uiParams;

	/**
	 * Instantiates a new task tree builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public RelationsWidgetBuilder(LabelProvider labelProvider, CodelistService codelistService) {
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

		uiParams = getAsMap(propertyDefinition.getControlDefinition().getUiParams());

		// if display status is true, then go ahead and build the field
		if (displayStatus) {
			// build a wrapper for the label and field
			UIComponent wrapper = buildFieldWrapper();
			addStyleClass(wrapper, "relations-widget-wrapper");
			List<UIComponent> children = wrapper.getChildren();

			HtmlPanelGroup widgetWrapper = (HtmlPanelGroup) builderHelper
					.getComponent(ComponentType.OUTPUT_PANEL);
			addStyleClass(widgetWrapper, "task-relations-widget");
			children.add(widgetWrapper);

			List<UIComponent> containerChildren = container.getChildren();
			containerChildren.add(wrapper);

			HtmlOutputText initScript = createInitScript();
			containerChildren.add(initScript);
			HtmlOutputText injection = createPluginInjectScript();
			containerChildren.add(injection);
		}
	}

	/**
	 * Creates the plugin inject script.
	 * 
	 * @return the html output text
	 */
	private HtmlOutputText createPluginInjectScript() {
		HtmlOutputText scriptFileInject = getScriptFileInject("extjs.emf.object-relations.js");
		return scriptFileInject;
	}

	/**
	 * Creates the init script.
	 * 
	 * @return the html output text
	 */
	private HtmlOutputText createInitScript() {
		String script = "Ext.onReady(function() {"
				+ "$('.task-relations-widget').extjsObjectRelations({"//
				+ "contextPath		: SF.config.contextPath,"//
				+ "instanceId		: '" + ((Entity<?>) getInstance()).getId()
				+ "',"//
				+ "instanceType		: '" + getInstanceName().toLowerCase()
				+ "',"//
				+ "preview			: false,"//
				+ "labels			: {" + "propertySelector	: _emfLabels['cmf.relations.type']"
				+ "}});});";
		HtmlOutputText scriptOutput = getScriptOutput(script);
		return scriptOutput;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent buildField() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		// TODO Auto-generated method stub
		return null;
	}

}
