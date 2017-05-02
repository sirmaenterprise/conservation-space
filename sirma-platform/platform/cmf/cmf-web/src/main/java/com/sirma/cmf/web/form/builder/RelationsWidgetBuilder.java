package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * @author svelikov
 */
public class RelationsWidgetBuilder extends ControlBuilder {

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

	@Override
	public void build() {
		String displayStatusKey = getRenderedStatusKey(propertyDefinition, formViewMode);
		boolean displayStatus = getDisplayStatus(displayStatusKey);

		if (trace) {
			String msg = MessageFormat.format("CMFWeb: building property [{0}] with display status key [{1} = {2}]",
					propertyDefinition.getName(), displayStatusKey, displayStatus);
			log.trace(msg);
		}

		// if display status is true, then go ahead and build the field
		if (displayStatus) {
			// build a wrapper for the label and field
			UIComponent wrapper = buildFieldWrapper();
			addStyleClass(wrapper, "relations-widget-wrapper");
			List<UIComponent> children = wrapper.getChildren();

			HtmlPanelGroup widgetWrapper = (HtmlPanelGroup) builderHelper.getComponent(ComponentType.OUTPUT_PANEL);
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
		return getScriptFileInject("extjs.emf.object-relations.js");
	}

	/**
	 * Creates the init script.
	 *
	 * @return the html output text
	 */
	private HtmlOutputText createInitScript() {
		String script = "Ext.onReady(function() {" + "$('.task-relations-widget').extjsObjectRelations({"//
				+ "contextPath		: SF.config.contextPath,"//
				+ "instanceId		: '" + ((Entity<?>) getInstance()).getId() + "',"//
				+ "instanceType		: '" + getInstanceName().toLowerCase() + "',"//
				+ "preview			: false,"//
				+ "labels			: {" + "propertySelector	: _emfLabels['cmf.relations.type']" + "}});});";
		return getScriptOutput(script);
	}

	@Override
	public UIComponent buildField() {
		// Not used
		return null;
	}

	@Override
	public UIComponent getComponentInstance() {
		// Not used
		return null;
	}

}
