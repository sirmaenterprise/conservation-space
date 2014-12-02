package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.context.FacesContext;
import javax.faces.event.BehaviorEvent;

import org.ajax4jsf.component.behavior.AjaxBehavior;
import org.ajax4jsf.component.behavior.MethodExpressionAjaxBehaviorListener;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.form.control.ChecklistControl.ChecklistParmeter;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.SortableComparator;

/**
 * Builder for checklist component.
 * 
 * @author svelikov
 */
public class ChecklistBuilder extends ControlBuilder {

	private static final String CHECKLIST_ITEM_CONTROL_ID = "CHECKLIST_ITEM";

	/**
	 * Instantiates a new checklist builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public ChecklistBuilder(LabelProvider labelProvider, CodelistService codelistService) {
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

			addGroupLabel(wrapper, BuilderCssConstants.CMF_DYNAMIC_FORM_LABEL + " "
					+ BuilderCssConstants.CMF_DYNAMIC_GROUP_LABEL);

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

	@Override
	public UIComponent buildField() {
		UIComponent uiComponent = getComponentInstance();

		return uiComponent;
	}

	@Override
	public UIComponent getComponentInstance() {

		String checklistPropertyName = propertyDefinition.getName();

		// create the checklist panel that will hold the checkboxes
		HtmlPanelGroup checklistPanel = (HtmlPanelGroup) builderHelper
				.getComponent(ComponentType.OUTPUT_PANEL);

		checklistPanel.setId(getIdForField(checklistPropertyName));

		Map<String, Object> attributes = checklistPanel.getAttributes();

		// attributes.put(SIMPLE_ID_ATTRIBUTE, checklistPropertyName);

		attributes.put("styleClass", BuilderCssConstants.CMF_CHECKLIST_PANEL);

		List<UIComponent> panelChildren = checklistPanel.getChildren();

		// get the defined fields from the control definition and create
		// a list with h:selectBooleanCheckboxes backed by the
		// CommonInstance.properties Map
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();

		List<PropertyDefinition> fields = controlDefinition.getFields();
		Collections.sort(fields, new SortableComparator());

		// calculate disabled status on all checkboxes based on ALLOW_CHANGE parameter
		Boolean allDisabled = Boolean.FALSE;
		List<ControlParam> controlParams = propertyDefinition.getControlDefinition()
				.getControlParams();
		String allowChangeAll = getControlParameterValue(ChecklistParmeter.ALLOW_CHANGE.name(),
				controlParams);
		if ((allowChangeAll != null)
				&& (Boolean.valueOf(allowChangeAll).compareTo(Boolean.FALSE) == 0)) {
			allDisabled = Boolean.TRUE;
		}

		// get all fields and create checkboxes
		for (PropertyDefinition checklistItemPropertyDefinition : fields) {
			HtmlPanelGroup checkboxWrapper = createCheckbox(checklistPropertyName,
					checklistItemPropertyDefinition, allDisabled);
			panelChildren.add(checkboxWrapper);
		}

		return checklistPanel;
	}

	/**
	 * Creates the checkbox group.
	 * 
	 * @param propertyName
	 *            the property name
	 * @param checkboxFieldPropertyDefinition
	 *            the property definition
	 * @param allDisabled
	 *            the all disabled
	 * @return the html panel group
	 */
	private HtmlPanelGroup createCheckbox(String propertyName,
			PropertyDefinition checkboxFieldPropertyDefinition, Boolean allDisabled) {
		String itemName = checkboxFieldPropertyDefinition.getName();

		ControlDefinition controlDefinition = checkboxFieldPropertyDefinition
				.getControlDefinition();
		// added check for explicit check for instance type. If not the field will not be complex at
		// all
		boolean complexLabel = (controlDefinition != null)
				&& EqualsHelper.nullSafeEquals(controlDefinition.getIdentifier(),
						CHECKLIST_ITEM_CONTROL_ID, true)
				&& checkboxFieldPropertyDefinition.getDataType().getName()
						.equals(DataTypeDefinition.INSTANCE);

		// create the wrapper for the checkbox and its label
		HtmlPanelGroup checkboxWrapper = (HtmlPanelGroup) builderHelper
				.getComponent(ComponentType.OUTPUT_PANEL);

		checkboxWrapper.getAttributes().put(STYLE_CLASS,
				BuilderCssConstants.CMF_CHECKLIST_ITEM_WRAPPER);

		List<UIComponent> wrapperChildren = checkboxWrapper.getChildren();

		// create the checkbox
		HtmlSelectBooleanCheckbox checkbox = (HtmlSelectBooleanCheckbox) builderHelper
				.getComponent(ComponentType.SELECT_BOOLEAN_CHECKBOX);
		String checkboxId = getIdForField(checkboxFieldPropertyDefinition, itemName);
		checkbox.setId(checkboxId);

		checkbox.getAttributes().put(STYLE_CLASS, BuilderCssConstants.CMF_CHECKLIST_ITEM);

		// #{taskInstance.properties[checklist].properties[itemname]}
		String valueExpressionString = getValueExpressionString(getInstanceName(), propertyName,
				itemName, complexLabel);
		checkbox.setValueExpression("value",
				createValueExpression(valueExpressionString, Boolean.class));

		// calculate disabled attribute for the checkbox
		// - allDisabled is calculated for the whole checklist and can be overriden for particular
		// checkbox
		// - if there is ALLOW_CHANGE=false control parameter in definition, then disable the
		// checkbox
		// - if form view mode is set to be preview, then disable the checkbox
		Boolean disabled = allDisabled;
		if (controlDefinition != null) {
			List<ControlParam> controlParams = controlDefinition.getControlParams();
			String allowChange = getControlParameterValue(ChecklistParmeter.ALLOW_CHANGE.name(),
					controlParams);
			if (allowChange != null) {
				// allowChange=true means that we should have disabled=false
				disabled = !Boolean.valueOf(allowChange);
			}
		}
		if (formViewMode == FormViewMode.PREVIEW) {
			disabled = Boolean.TRUE;
		}
		checkbox.setDisabled(disabled.booleanValue());

		wrapperChildren.add(checkbox);

		// create the checkbox label
		HtmlOutputLabel label = (HtmlOutputLabel) builderHelper
				.getComponent(ComponentType.OUTPUT_LABEL);
		label.setFor(checkboxId);
		String labelId = checkboxId + "_" + "label";
		label.setId(labelId);

		// disable html escape so the label to allow html tags inside
		label.setEscape(false);

		// we have complex field so we will have complex label
		String labelValue = getCheckboxLabel(itemName, checkboxFieldPropertyDefinition);
		if (complexLabel) {
			String labelChangeExpression = getLabelChangeExpressionString(getInstanceName(),
					propertyName, itemName);
			// add ajax behavior for on click of the check box and we also refresh the label
			addAjaxBehavior(checkbox, labelChangeExpression, labelId);
			String labelExpression = getLabelExpressionString(getInstanceName(), propertyName,
					itemName);
			labelValue += " " + labelExpression;

			label.setValueExpression("value", createValueExpression(labelValue, String.class));
		} else {
			// if this control has codelist attached, then the labels are taken from
			// codelist and otherwise labels are taken from the definition
			label.setValue(labelValue);
		}

		label.getAttributes().put(STYLE_CLASS, "checklist-item-label");
		wrapperChildren.add(label);
		return checkboxWrapper;
	}

	/**
	 * Adds the ajax behavior for on click of the check box and we also refresh the label.
	 * 
	 * @param checkbox
	 *            the target checkbox
	 * @param expression
	 *            the expression to execute
	 * @param labelId
	 *            the label id to refresh on change
	 */
	protected void addAjaxBehavior(HtmlSelectBooleanCheckbox checkbox, String expression,
			String labelId) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Application application = facesContext.getApplication();

		AjaxBehavior ajaxBehavior = (AjaxBehavior) application
				.createBehavior(AjaxBehavior.BEHAVIOR_ID);

		MethodExpression listener = application.getExpressionFactory().createMethodExpression(
				facesContext.getELContext(), expression, null, new Class[] { BehaviorEvent.class });
		ajaxBehavior.setRender(Arrays.asList(labelId));

		ajaxBehavior.addAjaxBehaviorListener(new MethodExpressionAjaxBehaviorListener(listener));
		checkbox.addClientBehavior("change", ajaxBehavior);
	}

	/**
	 * Gets the checkbox label according to that whether the checklist control has a codelist
	 * attached.
	 * 
	 * @param itemName
	 *            the item name
	 * @param checkboxFieldPropertyDefinition
	 *            the checkbox field property definition
	 * @return the checkbox label - will not be null in any case
	 */
	private String getCheckboxLabel(String itemName,
			PropertyDefinition checkboxFieldPropertyDefinition) {

		String labelValue = null;

		Integer checklistCL = propertyDefinition.getCodelist();

		if (checklistCL == null) {
			String propertyLabel = checkboxFieldPropertyDefinition.getLabel();
			if (StringUtils.isNotNullOrEmpty(propertyLabel)) {
				labelValue = propertyLabel;
			} else {
				log.error("CMFWeb: there is no label for item in the checklist for item ["
						+ itemName + "]");
				labelValue = "Missing label! Please check the definition!";
			}
		} else {
			labelValue = codelistService.getDescription(checklistCL, itemName);
		}
		return labelValue;
	}

	/**
	 * Gets the value expression string.
	 * 
	 * @param instanceName
	 *            the instance name
	 * @param propertyName
	 *            the property name
	 * @param itemName
	 *            the item name
	 * @param subValue
	 *            the sub value
	 * @return the value expression string
	 */
	protected String getValueExpressionString(String instanceName, String propertyName,
			String itemName, boolean subValue) {
		return getPropertyExpressionString(instanceName, propertyName, itemName, subValue,
				DefaultProperties.CHECK_BOX_VALUE);
	}

	/**
	 * Gets the label change expression string.
	 * 
	 * @param instanceName
	 *            the instance name
	 * @param propertyName
	 *            the property name
	 * @param itemName
	 *            the item name
	 * @return the label change expression string
	 */
	protected String getLabelChangeExpressionString(String instanceName, String propertyName,
			String itemName) {
		String expressionString = "#'{'checkboxListener.checkboxChanged({0}, ''{1}'', ''{2}'')'}'";
		return MessageFormat.format(expressionString, instanceName, propertyName, itemName);
	}

	/**
	 * Gets the label expression string.
	 * 
	 * @param instanceName
	 *            the instance name
	 * @param propertyName
	 *            the property name
	 * @param itemName
	 *            the item name
	 * @return the label expression string
	 */
	protected String getLabelExpressionString(String instanceName, String propertyName,
			String itemName) {
		String expressionString = "#'{'labelBuilder.getCheckBoxModifier({0}, ''{1}'', ''{2}'')'}'";
		return MessageFormat.format(expressionString, instanceName, propertyName, itemName);
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
	 * @param subValue
	 *            the sub value
	 * @param subProperty
	 *            the sub property
	 * @return the property expression string
	 */
	protected String getPropertyExpressionString(String instanceName, String propertyName,
			String itemName, boolean subValue, String subProperty) {

		String expressionString = "#'{'{0}.properties[''{1}''].properties[''{2}'']{3}'}'";
		return MessageFormat.format(expressionString, instanceName, propertyName, itemName,
				subValue ? ".properties['" + subProperty + "']" : "");
	}

}
