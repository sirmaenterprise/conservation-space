package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.List;

import javax.el.MethodExpression;
import javax.faces.component.UIComponent;

import org.richfaces.component.UICommandButton;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.form.control.ActionEventButtonControlParameter;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Builder for action event button..
 * 
 * @author svelikov
 */
public class ActionEventButtonBuilder extends ControlBuilder {

	private static final String DEFAULT_RENDER_ELEMENT = "@none";

	private static final String DEFAULT_EXECUTE_ELEMENT = "@this";

	private static final String ORG_RICHFACES_COMMAND_BUTTON_RENDERER = "org.richfaces.CommandButtonRenderer";

	private static final String ORG_RICHFACES_COMMAND_BUTTON = "org.richfaces.CommandButton";

	/**
	 * The Enum RenderElement.
	 */
	private enum RenderElement {

		/** The form. */
		FORM,
		/** The none. */
		NONE
	}

	/**
	 * The Enum ExecuteElement.
	 */
	private enum ExecuteElement {

		/** The form. */
		FORM,
		/** The this. */
		THIS
	}

	/**
	 * Instantiates a new action event button.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public ActionEventButtonBuilder(LabelProvider labelProvider, CodelistService codelistService) {
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

		// if display status is true, then go ahead and build the field
		if (displayStatus) {

			UIComponent uiComponent = buildField();

			List<UIComponent> containerChildren = container.getChildren();

			containerChildren.add(uiComponent);
		}
	}

	@Override
	public UIComponent buildField() {
		UIComponent uiComponent = getComponentInstance();

		return uiComponent;
	}

	@Override
	public UIComponent getComponentInstance() {

		UICommandButton commandButton = (UICommandButton) createComponentInstance(
				ORG_RICHFACES_COMMAND_BUTTON, ORG_RICHFACES_COMMAND_BUTTON_RENDERER);

		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		List<ControlParam> controlParams = controlDefinition.getControlParams();

		String fieldId = getIdForField(propertyDefinition.getIdentifier());
		commandButton.setId(fieldId);

		setLabel(commandButton);

		addStyleClass(commandButton, BuilderCssConstants.CMF_STANDARD_BUTTON + " "
				+ BuilderCssConstants.CMF_ACTION_EVENT_BUTTON + " " + "cmf-button");

		boolean executeOnce = isExecuteOnce(controlParams);

		setMethodExpression(commandButton, controlParams, fieldId,
				propertyDefinition.getIdentifier(), executeOnce);

		setRenderAttribute(commandButton, controlParams);

		setExecuteAttribute(commandButton, controlParams);

		// only enable button if edit
		if (formViewMode == FormViewMode.EDIT) {
			setOncompleteAttribute(commandButton, controlParams, executeOnce);
		} else {
			commandButton.setDisabled(true);
		}

		commandButton.setImmediate(true);

		return commandButton;
	}

	/**
	 * Checks if action event should be allowed anly once.
	 * 
	 * @param controlParams
	 *            the control params
	 * @return true, if is execute once
	 */
	private boolean isExecuteOnce(List<ControlParam> controlParams) {
		ControlParam executeOnceParam = getControlParameter(
				ActionEventButtonControlParameter.EXECUTE_ONCE.name(), controlParams);

		boolean isExecuteOnce = false;

		if (executeOnceParam == null) {
			isExecuteOnce = true;
		} else {
			String providedExecuteOnce = executeOnceParam.getValue();
			if (isValidExecuteOnce(providedExecuteOnce)
					&& Boolean.parseBoolean(providedExecuteOnce)) {
				isExecuteOnce = true;
			}
		}

		return isExecuteOnce;
	}

	/**
	 * Sets the execute attribute. Default is @this
	 * 
	 * @param commandButton
	 *            the command button
	 * @param controlParams
	 *            the control params
	 */
	private void setExecuteAttribute(UICommandButton commandButton, List<ControlParam> controlParams) {
		String executeElement = DEFAULT_EXECUTE_ELEMENT;
		ControlParam executeElementParam = getControlParameter(
				ActionEventButtonControlParameter.EXECUTE_ELEMENT.name(), controlParams);
		if (executeElementParam != null) {
			String providedExecuteElement = executeElementParam.getValue();
			if (isValidExecuteElement(providedExecuteElement)) {
				executeElement = "@" + providedExecuteElement;
			}
		}
		commandButton.setExecute(executeElement);
	}

	/**
	 * Sets the render attribute. Default value is @none
	 * 
	 * @param commandButton
	 *            the command button
	 * @param controlParams
	 *            the control params
	 */
	private void setRenderAttribute(UICommandButton commandButton, List<ControlParam> controlParams) {
		String renderElement = DEFAULT_RENDER_ELEMENT;
		ControlParam renderElementParam = getControlParameter(
				ActionEventButtonControlParameter.RENDER_ELEMENT.name(), controlParams);
		if (renderElementParam != null) {
			String providedRenderElement = renderElementParam.getValue();
			if (isValidRenderElement(providedRenderElement)) {
				renderElement = "@" + providedRenderElement;
			}
		}
		commandButton.setRender(renderElement);
	}

	/**
	 * Sets the method expression.
	 * 
	 * @param commandButton
	 *            the command button
	 * @param controlParams
	 *            the control params
	 * @param fieldId
	 *            the field id
	 * @param fieldName
	 *            the field name
	 * @param executeOnce
	 *            the execute once
	 */
	private void setMethodExpression(UICommandButton commandButton,
			List<ControlParam> controlParams, String fieldId, String fieldName, boolean executeOnce) {
		ControlParam eventIdParam = getControlParameter(
				ActionEventButtonControlParameter.EVENT_ID.name(), controlParams);
		if (eventIdParam == null) {
			throw new RuntimeException(
					"CMFWeb: Missing EVENT_ID parameter for ACTION_EVENT_BUTTON with id ["
							+ fieldId + "]. Please check definition!");
		}
		String methodExpressionString = "#{dynamicFormAction.fireEvent(" + getInstanceName()
				+ ", '" + fieldName + "', '" + eventIdParam.getValue() + "', " + executeOnce + ")}";
		MethodExpression actionMethodExpressionString = createMethodExpression(
				methodExpressionString, new Class<?>[] { String.class });
		commandButton.setActionExpression(actionMethodExpressionString);
	}

	/**
	 * Sets the label of this button.
	 * 
	 * @param commandButton
	 *            the new label
	 */
	private void setLabel(UICommandButton commandButton) {
		String label = propertyDefinition.getLabel();
		if (StringUtils.isNullOrEmpty(label)) {
			label = "Action Button";
		}
		commandButton.setValue(label);
	}

	/**
	 * Sets the oncomplete attribute according to definition. If oncomplete attribute is provided
	 * and has valid value, then use it. Otherwise set the default oncomplete attribute that means
	 * to disable the button after the first click.
	 * 
	 * @param commandButton
	 *            the command button
	 * @param controlParams
	 *            the control params
	 * @param executeOnce
	 *            the execute once
	 */
	private void setOncompleteAttribute(UICommandButton commandButton,
			List<ControlParam> controlParams, boolean executeOnce) {
		if (executeOnce) {
			String disabledExpressionString = getDisabledExpressionString(getInstanceName(),
					propertyDefinition.getIdentifier(), DefaultProperties.ACTION_BUTTON_EXECUTED);
			commandButton.setValueExpression("disabled",
					createValueExpression(disabledExpressionString, Boolean.class));
		}
	}

	/**
	 * Gets the property expression string.
	 * 
	 * @param instanceName
	 *            the instance name
	 * @param propertyName
	 *            the property name
	 * @param itemName
	 *            the item name
	 * @return the property expression string
	 */
	protected String getDisabledExpressionString(String instanceName, String propertyName,
			String itemName) {

		String expressionString = "#'{'{0}.properties[''{1}''].properties[''{2}'']'}'";
		return MessageFormat.format(expressionString, instanceName, propertyName, itemName);
	}

	/**
	 * Checks if is valid execute once.
	 * 
	 * @param providedExecuteOnce
	 *            the provided execute once
	 * @return true, if is valid execute once
	 */
	private boolean isValidExecuteOnce(String providedExecuteOnce) {
		if ("true".equals(providedExecuteOnce) || "false".equals(providedExecuteOnce)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is valid execute element.
	 * 
	 * @param executeElement
	 *            the execute element
	 * @return true, if is valid execute element
	 */
	private boolean isValidExecuteElement(String executeElement) {
		if (ExecuteElement.FORM.name().toLowerCase().equals(executeElement)
				|| ExecuteElement.THIS.name().toLowerCase().equals(executeElement)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is valid render element.
	 * 
	 * @param renderElement
	 *            the render element
	 * @return true, if is valid render element
	 */
	private boolean isValidRenderElement(String renderElement) {
		if (RenderElement.FORM.name().toLowerCase().equals(renderElement)
				|| RenderElement.NONE.name().toLowerCase().equals(renderElement)) {
			return true;
		}
		return false;
	}
}
