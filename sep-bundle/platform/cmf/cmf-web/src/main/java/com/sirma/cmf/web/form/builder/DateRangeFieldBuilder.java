package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.web.util.DateUtil;

/**
 * Builder for date range fields.
 * 
 * @author svelikov
 */
public class DateRangeFieldBuilder extends ControlBuilder {

	private static final String END_DATE_FIELD = "END_DATE_FIELD";

	private Boolean isFormEditMode;

	private final DateUtil dateUtil;

	/**
	 * Instantiates a new date field builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 * @param dateUtil
	 *            the date util
	 */
	public DateRangeFieldBuilder(LabelProvider labelProvider, CodelistService codelistService,
			DateUtil dateUtil) {
		super(labelProvider, codelistService);
		this.dateUtil = dateUtil;
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
			// build a wrapper for the label and field
			UIComponent fieldWrapper = buildFieldWrapper();
			setRequired(fieldWrapper);

			// build label and put it in the wrapper
			fieldWrapper.getChildren().add(buildLabel());

			String previewStatusKey = getPreviewStatusKey(propertyDefinition, formViewMode);
			boolean previewStatus = previewStatusMap.get(previewStatusKey);

			if (trace) {
				String msg = MessageFormat.format("CMFWeb: previewStatusKey [{0} = {1}]",
						previewStatusKey, previewStatus);
				log.debug(msg);
			}

			isFormEditMode = previewStatusMap.get(previewStatusKey);
			if (isFormEditMode) {
				UIComponent uiComponent = buildField();
				fieldWrapper.getChildren().add(uiComponent);
				addAfterFieldContent(fieldWrapper);
				addTooltip(fieldWrapper, uiComponent);
			} else {
				fieldWrapper.getChildren().add(buildOutputField());
			}

			List<UIComponent> containerChildren = container.getChildren();
			containerChildren.add(fieldWrapper);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		HtmlInputText uiComponent = (HtmlInputText) createComponentInstance(
				"javax.faces.HtmlInputText", "javax.faces.Text");
		addStyleClass(uiComponent, "date-range");
		addStyleClass(uiComponent, propertyDefinition.getIdentifier());
		addStyleClass(uiComponent, "form-control");
		return uiComponent;
	}

	@Override
	public Class<?> getValueResultType() {
		return Date.class;
	}

	@Override
	public void updateField(UIComponent uiComponent) {
		String type = propertyDefinition.getDataType().getName();
		if (DataTypeDefinition.DATE.equals(type)) {
			addStyleClass(uiComponent, BuilderCssConstants.CMF_DATE_FIELD);
			// ((UIInput) uiComponent).setConverter(new EmfDateConverter());
			String converterEl = "#{emfDateConverter}";
			ValueExpression valueExpression = createValueExpression(converterEl, Object.class);
			uiComponent.setValueExpression("converter", valueExpression);
		} else if (DataTypeDefinition.DATETIME.equals(type)) {
			addStyleClass(uiComponent, BuilderCssConstants.CMF_DATETIME_FIELD);
			// ((UIInput) uiComponent).setConverter(new EmfDateWithTimeConverter());
			String converterEl = "#{emfDateWithTimeConverter}";
			ValueExpression valueExpression = createValueExpression(converterEl, Object.class);
			uiComponent.setValueExpression("converter", valueExpression);
		} else {
			log.warn("CMFWeb: Wrong datatype for field:" + propertyDefinition);
		}
		((UIInput) uiComponent).setConverterMessage(getDateConverterMessage());
		uiComponent.getAttributes().put("autocomplete", "off");
	}

	/**
	 * Gets the date converter message.
	 * 
	 * @return the date converter message
	 */
	protected String getDateConverterMessage() {
		return labelProvider.getValue(LabelConstants.MSG_ERROR_WRONG_DATE_FORMAT);
	}

	@Override
	public void addAfterFieldContent(UIComponent wrapper) {
		HtmlPanelGroup calendarImageContainer = new HtmlPanelGroup();
		String dateFormatHintLabel = null;
		String type = propertyDefinition.getDataType().getName();

		// apply style classes for calendar icon
		addStyleClass(calendarImageContainer, BuilderCssConstants.CMF_CALENDAR_ICON_CLASSES);

		if (DataTypeDefinition.DATE.equals(type)) {
			addStyleClass(calendarImageContainer, "cmf-date-field-icon");
			dateFormatHintLabel = getDateFormatHintLabel(dateUtil.getConverterDateFormatPattern());
		} else if (DataTypeDefinition.DATETIME.equals(type)) {
			addStyleClass(calendarImageContainer, "cmf-datetime-field-icon");
			dateFormatHintLabel = getDateFormatHintLabel(dateUtil
					.getConverterDatetimeFormatPattern());
		}
		wrapper.getChildren().add(calendarImageContainer);

		// add init script
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		List<ControlParam> controlParams = controlDefinition.getControlParams();
		ControlParam endDateFieldParam = getControlParameter(END_DATE_FIELD, controlParams);
		if ((endDateFieldParam != null)
				&& StringUtils.isNotNullOrEmpty(endDateFieldParam.getName())) {
			String endDateFieldId = endDateFieldParam.getValue();
			String startDateFieldId = propertyDefinition.getIdentifier();

			String scriptPattern = "$(function() '{' CMF.utilityFunctions.initDateRange(''.{0}'', ''.{1}''); '}');";
			String script = MessageFormat.format(scriptPattern, startDateFieldId, endDateFieldId);
			HtmlOutputText outputScript = getScriptOutput(script);
			wrapper.getChildren().add(outputScript);
		}
		HtmlPanelGroup innerWrapper = (HtmlPanelGroup) builderHelper
				.getComponent(ComponentType.OUTPUT_PANEL);
		addStyleClass(innerWrapper, BuilderCssConstants.CMF_RELATIVE_WRAPPER);
		List<UIComponent> innerWrapperChildren = innerWrapper.getChildren();
		List<UIComponent> wrapperChildren = wrapper.getChildren();
		UIComponent inputField = wrapperChildren.remove(1);
		UIComponent message = wrapperChildren.remove(1);

		// container that will holds hint message for date format
		HtmlPanelGroup dateFormatHintContainer = new HtmlPanelGroup();
		HtmlOutputText dateFormatHint = (HtmlOutputText) builderHelper
				.getComponent(ComponentType.OUTPUT_TEXT);
		dateFormatHint.setValue(dateFormatHintLabel);
		dateFormatHintContainer.getChildren().add(dateFormatHint);
		addStyleClass(dateFormatHintContainer, BuilderCssConstants.CMF_DATEPICKER_FORMAT_HINT);

		innerWrapperChildren.add(inputField);
		innerWrapperChildren.add(calendarImageContainer);
		wrapperChildren.add(innerWrapper);
		wrapperChildren.add(dateFormatHintContainer);
		wrapperChildren.add(message);
	}
}
