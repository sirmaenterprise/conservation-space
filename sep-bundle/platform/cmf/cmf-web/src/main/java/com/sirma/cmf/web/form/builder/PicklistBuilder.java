package com.sirma.cmf.web.form.builder;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutcomeTargetButton;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

import org.json.JSONObject;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.control.PicklistControlParameter;
import com.sirma.cmf.web.form.picklist.ItemsConverter;
import com.sirma.cmf.web.form.picklist.PicklistConstants;
import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Builds a picklist component.
 * 
 * @author svelikov
 */
public class PicklistBuilder extends ControlBuilder {

	/** The is form edit mode. */
	private boolean isFormEditMode;

	/** The field wrapper. */
	private UIComponent fieldWrapper;

	private Map<String, ControlParam> uiParams;

	private String pickerItemsType;

	private Object value;

	private boolean allowMultySelection;

	/**
	 * Instantiates a new picklist builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public PicklistBuilder(LabelProvider labelProvider, CodelistService codelistService) {
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
		ControlParam pickerItemsTypeParam = uiParams.get(PicklistControlParameter.ITEM_TYPE.name());
		pickerItemsType = pickerItemsTypeParam != null ? pickerItemsTypeParam.getValue()
				: ResourceType.USER.getName();

		ControlParam pklMode = uiParams.get(PicklistControlParameter.FUNCTIONAL_MODE.name());
		allowMultySelection = pklMode != null ? "multy".equals(pklMode.getValue()) : false;

		// if display status is true, then go ahead and build the field
		if (displayStatus) {
			// build a wrapper for the label and field
			fieldWrapper = buildFieldWrapper();
			addStyleClass(fieldWrapper, "cmf-picklist-wrapper");
			String specificStyleClassWrapper = propertyDefinition.getName() + "-picklist-wrapper";
			addStyleClass(fieldWrapper, specificStyleClassWrapper);
			setRequired(fieldWrapper);

			// build label and put it in the wrapper
			fieldWrapper.getChildren().add(buildLabel());

			String previewStatusKey = getPreviewStatusKey(propertyDefinition, formViewMode);
			Boolean previewStatus = previewStatusMap.get(previewStatusKey);

			if (trace) {
				String msg = MessageFormat.format("CMFWeb: previewStatusKey [{0} = {1}]",
						previewStatusKey, previewStatus);
				log.debug(msg);
			}

			isFormEditMode = previewStatusMap.get(previewStatusKey);
			if (isFormEditMode) {
				UIComponent uiComponent = buildField();
				fieldWrapper.getChildren().add(uiComponent);
				// add picklist initializing script
				HtmlOutputText initScript = createInitScript(getPicklistInitParameters(),
						specificStyleClassWrapper);
				fieldWrapper.getChildren().add(initScript);
			} else {
				fieldWrapper.getChildren().add(createPreviewField(true));
			}

			List<UIComponent> containerChildren = container.getChildren();
			containerChildren.add(fieldWrapper);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent buildField() {
		UIComponent uiComponent = getComponentInstance();

		return uiComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {

		String propertyName = propertyDefinition.getName();
		// create picklist wrapper panel
		UIComponent picklist = builderHelper.getComponent(ComponentType.OUTPUT_PANEL);
		String picklistPanelId = propertyName + "_picklistPanel";
		picklist.setId(picklistPanelId);
		addStyleClass(picklist, "picklist-field-wrapper clearfix");
		List<UIComponent> picklistPanelChildren = picklist.getChildren();

		List<ControlParam> controlParams = propertyDefinition.getControlDefinition()
				.getControlParams();
		String filterName = getFilterName(controlParams);
		Map<String, List<String>> keywords = getKeywords(controlParams);

		exportFilters(filterName, keywords);

		// create picklist trigger button
		UIComponent picklistTriggerButton = createPicklistTriggerButton(picklistPanelId,
				filterName, keywords);
		picklistPanelChildren.add(picklistTriggerButton);

		// create output field for preview
		UIComponent previewField = createPreviewField(false);
		picklistPanelChildren.add(previewField);

		// create a hidden field that holds the selected valuefinition.getName());
		UIComponent hiddenField = createHiddenField();
		// Set the field as required if necessary and add required message.
		if (isRequiredDynamically()) {
			((UIInput) hiddenField).setRequired(true);
			((UIInput) hiddenField).setRequiredMessage(labelProvider
					.getValue(LabelConstants.MSG_ERROR_REQUIRED_FIELD));
		}
		setHtmlMessage(fieldWrapper, hiddenField);
		picklistPanelChildren.add(hiddenField);

		return picklist;
	}

	/**
	 * Export filters to request map.
	 * 
	 * @param filterName
	 *            the filter name
	 * @param keywords
	 *            the keywords
	 */
	private void exportFilters(String filterName, Map<String, List<String>> keywords) {
		Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestMap();
		requestMap.put(PicklistConstants.FILTERNAME_ATTR, filterName);
		requestMap.put(PicklistConstants.KEYWORDS_ATTR, keywords);
	}

	/**
	 * Creates the javascript plugin init script.
	 * 
	 * @param initParameters
	 *            the init parameters
	 * @param specificStyleClassWrapper
	 *            the specific style class wrapper
	 * @return the html output text
	 */
	private HtmlOutputText createInitScript(String initParameters, String specificStyleClassWrapper) {
		String script = "$(function(){$('." + specificStyleClassWrapper + "').picklist("
				+ initParameters + ");});";
		script += "EMF.util.textareaResize('." + specificStyleClassWrapper
				+ " .picklist-preview-field');";
		HtmlOutputText scriptOutput = getScriptOutput(script);
		return scriptOutput;
	}

	/**
	 * Creates the picklist trigger button.
	 * 
	 * @param picklistPanelId
	 *            the picklist panel id
	 * @param filterName
	 *            the filter name
	 * @param keywords
	 *            the keywords
	 * @return the uI command button
	 */
	private UIComponent createPicklistTriggerButton(String picklistPanelId, String filterName,
			Map<String, List<String>> keywords) {

		String propertyName = propertyDefinition.getName();
		String id = propertyName + "_picklistTrigger";
		String btnSecificClass = id.replaceAll(":", "//:") + "-button";

		// pass false as argument that means the required parameters should be taken from the
		// current component attributes

		ValueExpression disabledExpression = null;
		List<ControlParam> controlParams = propertyDefinition.getControlDefinition()
				.getControlParams();
		ControlParam triggerButtonParam = getControlParameter(
				PicklistControlParameter.TRIGGER_BUTTON_TITLE.name(), controlParams);
		String triggerButtonValue = "Missing button value! Please check the definition!";
		if (triggerButtonParam != null) {
			if (StringUtils.isNotNullOrEmpty(triggerButtonParam.getValue())) {
				triggerButtonValue = labelProvider.getLabel(triggerButtonParam.getValue());
			}
		}

		String styleClass = BuilderCssConstants.CMF_STANDARD_BUTTON + " open-picklist "
				+ btnSecificClass;
		Boolean rendered = Boolean.TRUE;
		HtmlOutcomeTargetButton picklistTriggerButton = (HtmlOutcomeTargetButton) createComponentInstance(
				"javax.faces.HtmlOutcomeTargetButton", "javax.faces.Button");
		picklistTriggerButton.setId(id);
		picklistTriggerButton.setValueExpression("disabled", disabledExpression);
		picklistTriggerButton.setValue(triggerButtonValue);
		picklistTriggerButton.getAttributes().put("styleClass", styleClass);
		picklistTriggerButton.setRendered(rendered);

		JSONObject keywordsMap = JsonUtil.toJsonObject((Serializable) keywords);
		picklistTriggerButton.setOnclick("CMF.resourcePicker.open({'type': '" + pickerItemsType
				+ "', 'filtername': '" + filterName + "', 'keywords': " + keywordsMap
				+ ", triggerSelector: '." + btnSecificClass + "'}); return false;");

		return picklistTriggerButton;
	}

	/**
	 * Gets the picklist init parameters.
	 * 
	 * @return the picklist init parameters
	 */
	protected String getPicklistInitParameters() {
		// example result: { 'parameter1' : 'value1' , 'parameter2' : 'value2' }

		String pattern = "''{0}'' : ''{1}'', ";
		StringBuilder parameters = new StringBuilder();
		for (String key : uiParams.keySet()) {
			ControlParam parameter = uiParams.get(key);
			String name = parameter.getName();
			String value = parameter.getValue();
			if (StringUtils.isNotNullOrEmpty(name) && StringUtils.isNotNullOrEmpty(value)) {
				PicklistControlParameter enumValue = PicklistControlParameter.getEnumValue(name);
				if (enumValue != null) {

					value = getDefinitionLabel(value, enumValue);

					PicklistControlParameter picklistParamName = PicklistControlParameter
							.getPicklistParam(name);
					if (picklistParamName != null) {
						String nameParam = picklistParamName.getParam();
						parameters.append(MessageFormat.format(pattern, nameParam, value));
					}
				} else {
					throw new RuntimeException(
							"CMFWeb: Wrong parameter for picklist control is provided. Please check definition!!!");
				}
			}
		}

		parameters.append(MessageFormat.format(pattern, "applicationContext", FacesContext
				.getCurrentInstance().getExternalContext().getRequestContextPath()));
		parameters.append(MessageFormat
				.format(pattern, "imgResourceService", "/service/dms/proxy/"));
		// if there are parameters, then remove trailing comma and wrap in curly braces
		int len = parameters.length();
		if (len > 0) {
			parameters.setLength(len - 2);

			parameters.insert(0, "{ ");
			parameters.append(" }");
		}
		return parameters.toString();
	}

	/**
	 * Gets label from definition for given parameter. If matches one of enum constants, then get
	 * the value from bundle. Otherwise return same value.
	 * 
	 * @param value
	 *            the value
	 * @param enumValue
	 *            the enum value
	 * @return the bundle value
	 */
	private String getDefinitionLabel(String value, PicklistControlParameter enumValue) {
		switch (enumValue) {
			case TRIGGER_BUTTON_TITLE:
			case CANCEL_BUTTON_TITLE:
			case OK_BUTTON_TITLE:
			case HEADER_TITLE:
				value = labelProvider.getLabel(value);
				break;
			default:
				break;
		}
		return value;
	}

	/**
	 * Gets the name to be used for the event for loading of items for this listbox.
	 * 
	 * @param controlParams
	 *            the parameters
	 * @return the filter name
	 */
	private String getFilterName(List<ControlParam> controlParams) {
		String filterName = propertyDefinition.getName();
		ControlParam filterNameParam = getControlParameter(
				PicklistControlParameter.FILTER_NAME.name(), controlParams);

		if (filterNameParam != null) {
			String providedFiltername = filterNameParam.getValue();
			if (StringUtils.isNotNullOrEmpty(providedFiltername)) {
				filterName = providedFiltername;
			}
		}

		return filterName;
	}

	/**
	 * Assembles a map with keywords.
	 * 
	 * @param controlParams
	 *            the control params
	 * @return the keywords
	 */
	private Map<String, List<String>> getKeywords(List<ControlParam> controlParams) {

		List<ControlParam> keywordParams = getParametersByName(
				PicklistControlParameter.KEYWORD.name(), controlParams);

		Map<String, List<String>> keywordsMapping = new LinkedHashMap<String, List<String>>();

		if ((keywordParams != null) && !keywordParams.isEmpty()) {
			for (ControlParam keywordParam : keywordParams) {
				String keywordId = keywordParam.getIdentifier();
				String keywordsString = keywordParam.getValue();

				if (StringUtils.isNotNullOrEmpty(keywordsString)) {
					keywordsString = keywordsString.replaceAll(" ", "");
					List<String> keywords = Arrays.asList(keywordsString.split(","));
					keywordsMapping.put(keywordId, keywords);
				}
			}
		}

		return keywordsMapping;
	}

	/**
	 * Creates the hidden value field. We actually create regular text field taht is hidden with
	 * css.
	 * 
	 * @return the html input hidden
	 */
	private UIComponent createHiddenField() {
		HtmlInputText inputHidden = (HtmlInputText) builderHelper
				.getComponent(ComponentType.INPUT_TEXT);
		inputHidden.setId(getIdForField(propertyDefinition, propertyDefinition.getName()));
		String valueExpressionString = getValueExpressionString(getInstanceName(),
				propertyDefinition.getName());
		inputHidden.setValueExpression("value",
				createValueExpression(valueExpressionString, String.class));
		if (allowMultySelection) {
			inputHidden.setValueExpression("converter",
					createValueExpression("#{multiItemConverter}", Object.class));
		} else {
			inputHidden.setValueExpression("converter",
					createValueExpression("#{singleItemConverter}", Object.class));

		}
		// we actually can't set styleClass on input type=hidden fields and that's why we create
		// normal text field because we need a way to distinguish this field in the CMF.RNC module
		addStyleClass(inputHidden, "picklist-hidden-field");
		return inputHidden;
	}

	/**
	 * Creates the preview field. In previewMode=true we build an output text field. Otherwise we
	 * build input and apply a converter.
	 * 
	 * @param previewMode
	 *            the preview mode
	 * @return the uI component
	 */
	private UIComponent createPreviewField(boolean previewMode) {
		UIComponent previewField = null;
		String valueExpressionString = null;
		ValueExpression valueExpression = null;
		String itemsList = ItemsConverter.convertItemsToString(propertyValue);
		valueExpressionString = "#{picklistController.getAsString('" + itemsList + "','"
				+ pickerItemsType + "')}";
		valueExpression = createValueExpression(valueExpressionString, String.class);
		// createMethodExpression(valueExpressionString);
		if (previewMode) {
			previewField = builderHelper.getComponent(ComponentType.OUTPUT_TEXT);
		} else {
			// preview field is built as text area because:
			// - to have all values visible if many exists
			// - ?
			previewField = builderHelper.getComponent(ComponentType.INPUT_TEXT_AREA);
			if (allowMultySelection) {
				previewField.setValueExpression("converter",
						createValueExpression("#{multiItemPreviewConverter}", Object.class));
			} else {
				StringBuilder converter = new StringBuilder();
				converter.append("#{single");
				converter.append(Character.toUpperCase(pickerItemsType.charAt(0)));
				converter.append(pickerItemsType.substring(1));
				converter.append("PreviewConverter}");
				previewField.setValueExpression("converter",
						createValueExpression(converter.toString(), Object.class));
			}
			valueExpressionString = getValueExpressionString(getInstanceName(),
					propertyDefinition.getName());
			valueExpression = createValueExpression(valueExpressionString, String.class);
		}
		previewField.setValueExpression("value", valueExpression);

		setFieldId(previewField);
		addStyleClass(previewField, "picklist-preview-field");

		return previewField;
	}

	/**
	 * Sets the field id.
	 * 
	 * @param previewField
	 *            the new field id
	 */
	private void setFieldId(UIComponent previewField) {
		String path = PathHelper.getPath(propertyDefinition);
		String id = path + "_picklistPreviewField_" + propertyDefinition.getName();
		id = FIELD_PATH_REPLACEMENT_PATTERN.matcher(id).replaceAll("_");
		previewField.setId(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getValueResultType() {
		return String.class;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

}
