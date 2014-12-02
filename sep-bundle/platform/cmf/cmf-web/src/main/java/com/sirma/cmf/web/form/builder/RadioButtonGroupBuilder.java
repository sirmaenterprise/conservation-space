package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlSelectOneRadio;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.form.control.RadioButtonGroupControlParameter;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for radio button group component.
 * 
 * @author svelikov
 */
public class RadioButtonGroupBuilder extends ControlBuilder {

	/**
	 * Instantiates a new radio button group builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public RadioButtonGroupBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

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
			// build a wrapper for the label and field
			UIComponent wrapper = buildFieldWrapper();

			// build label for the radiobutton group control and put it in the
			// wrapper
			addGroupLabel(wrapper, calculateGroupLabelStyleClass());

			// build field and put it in the wrapper
			String previewStatusKey = getPreviewStatusKey(propertyDefinition, formViewMode);

			boolean previewStatus = previewStatusMap.get(previewStatusKey);

			if (log.isDebugEnabled()) {
				String msg = MessageFormat.format("CMFWeb: previewStatusKey [{0} = {1}]",
						previewStatusKey, previewStatus);
				log.debug(msg);
			}

			UIComponent uiComponent = buildField();
			wrapper.getChildren().add(uiComponent);

			//
			List<UIComponent> containerChildren = container.getChildren();
			containerChildren.add(wrapper);
		}
	}

	/**
	 * Calculate group label style class.
	 * 
	 * @return the css class string
	 */
	private String calculateGroupLabelStyleClass() {
		StringBuilder styleClass = new StringBuilder();

		String layoutAttribute = getLayoutAttribute(propertyDefinition.getControlDefinition());
		RadioButtonGroupLayout layoutType = RadioButtonGroupLayout.getLayoutType(layoutAttribute);
		if ((layoutType != null) && (layoutType == RadioButtonGroupLayout.PAGE_DIRECTION)) {
			styleClass.append(BuilderCssConstants.CMF_DYNAMIC_FORM_LABEL).append(" ");
		}

		styleClass.append(BuilderCssConstants.CMF_DYNAMIC_GROUP_LABEL);
		return styleClass.toString();
	}

	@Override
	public UIComponent buildField() {
		UIComponent uiComponent = getComponentInstance();

		String propertyName = propertyDefinition.getName();

		String valueExpressionString = getValueExpressionString(getInstanceName(), propertyName);
		ValueExpression ve = createValueExpression(valueExpressionString, getValueResultType());
		uiComponent.setValueExpression("value", ve);

		return uiComponent;
	}

	@Override
	public UIComponent getComponentInstance() {

		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();

		String groupPropertyName = propertyDefinition.getName();

		// create the group panel that will hold the radiobuttons
		HtmlSelectOneRadio radioButtonPanel = (HtmlSelectOneRadio) builderHelper
				.getComponent(ComponentType.RADIO_BUTTON_GROUP);

		// set id for the group
		radioButtonPanel.setId(getIdForField(groupPropertyName));

		// radioButtonPanel.getAttributes().put(SIMPLE_ID_ATTRIBUTE,
		// groupPropertyName);

		// set specific css class to the group
		radioButtonPanel.getAttributes().put(STYLE_CLASS,
				BuilderCssConstants.CMF_RADIOBUTTON_GROUP_PANEL);

		// apply group layout
		setControlLayout(controlDefinition, radioButtonPanel);

		// apply display mode
		if (formViewMode == FormViewMode.PREVIEW) {
			radioButtonPanel.setDisabled(true);
		}

		List<UIComponent> panelChildren = radioButtonPanel.getChildren();

		List<PropertyDefinition> fields = controlDefinition.getFields();

		for (PropertyDefinition radiobuttonItemPropertyDefinition : fields) {

			UISelectItem item = (UISelectItem) createComponentInstance("javax.faces.SelectItem");

			String itemName = radiobuttonItemPropertyDefinition.getName();
			item.getAttributes().put("itemValue", itemName);

			String labelValue = null;
			String propertyLabel = radiobuttonItemPropertyDefinition.getLabel();
			if (StringUtils.isNotNullOrEmpty(propertyLabel)) {
				labelValue = propertyLabel;
			} else {
				log.error("CMFWeb: there is no label for item in the radiobutton group for item ["
						+ itemName + "]");
				labelValue = "Missing label! Please check the definition!";
			}
			item.getAttributes().put("itemLabel", labelValue);

			panelChildren.add(item);
		}

		return radioButtonPanel;
	}

	/**
	 * Sets the control layout.
	 * 
	 * @param controlDefinition
	 *            the control definition
	 * @param radioButtonPanel
	 *            the radio button panel
	 */
	private void setControlLayout(ControlDefinition controlDefinition,
			HtmlSelectOneRadio radioButtonPanel) {
		String layoutParameterValue = getLayoutAttribute(controlDefinition);
		radioButtonPanel.getAttributes().put(RadioButtonGroupControlParameter.LAYOUT.getParam(),
				layoutParameterValue);
	}

	/**
	 * Gets the layout attribute.
	 * 
	 * @param controlDefinition
	 *            the control definition
	 * @return the layout attribute
	 */
	private String getLayoutAttribute(ControlDefinition controlDefinition) {
		ControlParam layoutParameter = getControlParameter(
				RadioButtonGroupControlParameter.LAYOUT.name(),
				controlDefinition.getControlParams());
		// this is the default layout
		String layoutParameterValue = RadioButtonGroupLayout.PAGE_DIRECTION.getLayout();
		if ((layoutParameter != null) && isValidLayoutParameter(layoutParameter.getValue())) {
			layoutParameterValue = layoutParameter.getValue();
		}
		return layoutParameterValue;
	}

	/**
	 * Checks if is valid layout parameter.
	 * 
	 * @param value
	 *            the value
	 * @return true, if is valid layout parameter
	 */
	private boolean isValidLayoutParameter(String value) {
		return RadioButtonGroupLayout.getLayoutType(value) != null;
		// return LAYOUT_LINE_DIRECTION.equals(value) || LAYOUT_PAGE_DIRECTION.equals(value);
	}

	@Override
	public Class<?> getValueResultType() {
		return String.class;
	}

}
