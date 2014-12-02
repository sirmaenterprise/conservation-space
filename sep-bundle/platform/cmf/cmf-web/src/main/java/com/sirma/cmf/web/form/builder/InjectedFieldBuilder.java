package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlOutputText;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.control.InjectedFieldControlParameter;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for field that can be injected in the form.
 * 
 * @author svelikov
 */
public class InjectedFieldBuilder extends ControlBuilder {

	/**
	 * Instantiates a new injected field builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public InjectedFieldBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build() {

		if (log.isDebugEnabled()) {
			String msg = MessageFormat.format("CMFWeb: building hidden field for property [{0}]",
					propertyDefinition.getName());
			log.debug(msg);
		}

		UIComponent uiComponent = buildField();

		List<UIComponent> containerChildren = container.getChildren();
		containerChildren.add(uiComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent buildField() {

		UIComponent uiComponent = null;
		UIComponent wrapper = null;
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		List<ControlParam> controlParams = controlDefinition.getControlParams();

		// - if there is VISIBILITY parameter:
		// -- if VISIBILITY == visible, then render visible preview field only if the field has
		// value
		// -- if VISIBILITY == hidden, then render hidden field
		// - else render hidden field
		ControlParam sourceParameter = getControlParameter(
				InjectedFieldControlParameter.SOURCE.name(), controlParams);
		ControlParam visibilityParameter = getControlParameter(
				InjectedFieldControlParameter.VISIBILITY.name(), controlParams);
		String visibility = null;
		if (visibilityParameter != null) {
			visibility = visibilityParameter.getValue();
			String displayStatusKey = getRenderedStatusKey(propertyDefinition, formViewMode);
			boolean displayStatus = renderStatusMap.get(displayStatusKey);
			// make the field hidden if previewempty is set
			if ("visible".equals(visibility) && displayStatus) {
				wrapper = buildFieldWrapper();
				wrapper.getChildren().add(buildLabel());
				uiComponent = buildReadonlyField();
				wrapper.getChildren().add(uiComponent);
				// if field has codelist attached, then add a hidden field that holds the codevalue
				// that may be used in rnc conditions
				addHiddenValueField(wrapper, uiComponent, controlParams);
			} else {
				// for fields that has visibility=hidden or any other value we build hidden field
				uiComponent = buildHiddenField();
			}
		} else {
			uiComponent = buildHiddenField();
		}

		String propertyName = propertyDefinition.getName();
		uiComponent.setId(propertyName);

		// - if there is SOURCE parameter: set field's value using the parameter
		// value (mainly if the value should come from the case
		// - else: set field's value to point the control field value
		String veString = null;
		if ((sourceParameter != null) && StringUtils.isNullOrEmpty(propertyDefinition.getRnc())) {
			veString = sourceParameter.getValue();
		} else {
			veString = getValueExpressionString(getInstanceName(), propertyName);
		}
		ValueExpression ve = createValueExpression(veString, String.class);
		uiComponent.setValueExpression("value", ve);
		if (wrapper != null) {
			return wrapper;
		}
		return uiComponent;
	}

	/**
	 * Adds the hidden value field to the wrapper if there is a codelist attached to the property
	 * definition.
	 * 
	 * @param wrapper
	 *            the wrapper of the injected field
	 * @param output
	 *            the output field
	 * @param controlParams
	 *            the control params passed trough definition
	 */
	private void addHiddenValueField(UIComponent wrapper, UIComponent output,
			List<ControlParam> controlParams) {
		Integer codelist = propertyDefinition.getCodelist();
		String clValue = getControlParameterValue(InjectedFieldControlParameter.CLVALUE.name(),
				controlParams);
		if ((codelist != null) && (clValue != null)) {
			UIComponent hiddenCodelistValue = builderHelper
					.getComponent(ComponentType.INPUT_HIDDEN);
			String propertyName = propertyDefinition.getName();
			hiddenCodelistValue.setId(propertyName + "_hiddenValue");
			String veString = "#{" + clValue + "}";
			ValueExpression ve = createValueExpression(veString, String.class);
			hiddenCodelistValue.setValueExpression("value", ve);
			wrapper.getChildren().add(hiddenCodelistValue);
			// add specific css marker class to allow the field to be recognized by the rnc executor
			addStyleClass(output, "has-clvalue");
		}
	}

	/**
	 * Builds the hidden field.
	 * 
	 * @return the uI component
	 */
	private UIComponent buildHiddenField() {
		HtmlInputHidden hiddenField = (HtmlInputHidden) builderHelper
				.getComponent(ComponentType.INPUT_HIDDEN);

		return hiddenField;
	}

	/**
	 * Builds the readonly field.
	 * 
	 * @return the uI component
	 */
	private UIComponent buildReadonlyField() {
		HtmlOutputText output = (HtmlOutputText) builderHelper
				.getComponent(ComponentType.OUTPUT_TEXT);
		return output;
	}
}
