package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.control.TaskTreeControlParameter;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for task tree component used in standalone task forms.
 * 
 * @author svelikov
 */
public class TaskTreeBuilder extends ControlBuilder {

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
	public TaskTreeBuilder(LabelProvider labelProvider, CodelistService codelistService) {
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
			String wrapperStyleClass = "tasktree-wrapper";
			addStyleClass(wrapper, wrapperStyleClass);

			// build label for the radiobutton group control and put it in the
			// wrapper
			addGroupLabel(wrapper, BuilderCssConstants.CMF_DYNAMIC_FORM_LABEL + " "
					+ BuilderCssConstants.CMF_DYNAMIC_GROUP_LABEL);

			// add tasktree initializing script
			HtmlOutputText initScript = createInitScript(getInitParameters(uiParams),
					wrapperStyleClass);
			wrapper.getChildren().add(initScript);

			HtmlOutputText data = createInitScriptData();
			wrapper.getChildren().add(data);

			List<UIComponent> containerChildren = container.getChildren();

			containerChildren.add(wrapper);
		}
	}

	/**
	 * Creates the init script.
	 * 
	 * @param initParameters
	 *            the init parameters
	 * @param selector
	 *            the selector
	 * @return the html output text
	 */
	private HtmlOutputText createInitScript(String initParameters, String selector) {
		String script = "$(function(){$('." + selector + "').tasktree(" + initParameters + ");});";
		HtmlOutputText scriptOutput = getScriptOutput(script);
		return scriptOutput;
	}

	/**
	 * Creates the init script data.
	 * 
	 * @return the html output text
	 */
	private HtmlOutputText createInitScriptData() {
		HtmlOutputText outputScript = (HtmlOutputText) builderHelper
				.getComponent(ComponentType.OUTPUT_TEXT);
		addStyleClass(outputScript, "hide tasktree-data");
		ValueExpression valueExpression = createValueExpression(
				"#{taskTreeController.taskTreeData()}", String.class);
		outputScript.setValueExpression("value", valueExpression);

		return outputScript;
	}

	/**
	 * Gets the inits the parameters.
	 * 
	 * @param uiParams
	 *            the ui params
	 * @return the inits the parameters
	 */
	protected String getInitParameters(Map<String, ControlParam> uiParams) {
		// example result: { 'parameter1' : 'value1' , 'parameter2' : 'value2' }

		String pattern = "''{0}'' : ''{1}'', ";
		StringBuilder parameters = new StringBuilder();
		for (Entry<String, ControlParam> entry : uiParams.entrySet()) {
			ControlParam parameter = entry.getValue();
			String name = parameter.getName();
			String value = parameter.getValue();
			if (StringUtils.isNotNullOrEmpty(name) && StringUtils.isNotNullOrEmpty(value)) {
				TaskTreeControlParameter enumValue = TaskTreeControlParameter.getEnumValue(name);
				if (enumValue != null) {
					value = labelProvider.getLabel(name);
					TaskTreeControlParameter paramName = TaskTreeControlParameter
							.getParamEnum(name);
					if (paramName != null) {
						String nameParam = paramName.getParam();
						parameters.append(MessageFormat.format(pattern, nameParam, value));
					}
				} else {
					throw new RuntimeException(
							"CMFWeb: Wrong parameter for TASKTREE control is provided. Please check definition!!!");
				}
			}
		}

		// if there are parameters, then remove trailing comma and wrap in curly braces
		if (parameters.length() == 0) {
			parameters.insert(0, "{ data:$('.tasktree-data').text()");
		} else {
			parameters.insert(0, "{ data:$('.tasktree-data').text(),");
		}
		parameters.append(" }");
		return parameters.toString();
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
