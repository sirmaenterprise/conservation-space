package com.sirma.cmf.web.form.builder;

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
import com.sirma.itt.emf.web.util.DateUtil;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Builder for date input text field.
 *
 * @author svelikov
 */
public class DateFieldBuilder extends FormBuilder {

	/** The date util. */
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
	public DateFieldBuilder(LabelProvider labelProvider, CodelistService codelistService, DateUtil dateUtil) {
		super(labelProvider, codelistService);
		this.dateUtil = dateUtil;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		HtmlInputText uiComponent = (HtmlInputText) builderHelper.getComponent(ComponentType.INPUT_TEXT);
		addStyleClass(uiComponent, propertyDefinition.getIdentifier());
		addStyleClass(uiComponent, "form-control");
		return uiComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getValueResultType() {
		return Date.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateField(UIComponent uiComponent) {
		String type = propertyDefinition.getDataType().getName();
		if (DataTypeDefinition.DATE.equals(type)) {
			addStyleClass(uiComponent, BuilderCssConstants.CMF_DATE_FIELD);
			// set the date field marker class
			addStyleClass(uiComponent, BuilderCssConstants.DATE_FIELD);
			// ((UIInput) uiComponent).setConverter(new EmfDateConverter());
			String converterEl = "#{emfDateConverter}";
			ValueExpression valueExpression = createValueExpression(converterEl, Object.class);
			uiComponent.setValueExpression("converter", valueExpression);
		} else if (DataTypeDefinition.DATETIME.equals(type)) {
			addStyleClass(uiComponent, BuilderCssConstants.CMF_DATETIME_FIELD);
			// set the datetime field marker class
			addStyleClass(uiComponent, BuilderCssConstants.DATETIME_FIELD);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAfterFieldContent(UIComponent wrapper) {
		HtmlPanelGroup calendarImageContainer = new HtmlPanelGroup();
		String type = propertyDefinition.getDataType().getName();
		String dateFormatHintLabel = null;

		// apply style classes for calendar icon
		addStyleClass(calendarImageContainer, BuilderCssConstants.CMF_CALENDAR_ICON_CLASSES);

		if (DataTypeDefinition.DATE.equals(type)) {
			addStyleClass(calendarImageContainer, "cmf-date-field-icon");
			dateFormatHintLabel = getDateFormatHintLabel(dateUtil.getConverterDateFormatPattern());
		} else if (DataTypeDefinition.DATETIME.equals(type)) {
			addStyleClass(calendarImageContainer, "cmf-datetime-field-icon");
			dateFormatHintLabel = getDateFormatHintLabel(dateUtil.getConverterDatetimeFormatPattern());
		}

		// group container that will holds label,
		// input and icon for picking date/date-time
		HtmlPanelGroup innerWrapper = (HtmlPanelGroup) builderHelper.getComponent(ComponentType.OUTPUT_PANEL);
		addStyleClass(innerWrapper, BuilderCssConstants.CMF_RELATIVE_WRAPPER);
		List<UIComponent> innerWrapperChildren = innerWrapper.getChildren();

		List<UIComponent> wrapperChildren = wrapper.getChildren();
		UIComponent inputField = wrapperChildren.remove(1);
		UIComponent message = wrapperChildren.remove(1);
		innerWrapperChildren.add(inputField);
		innerWrapperChildren.add(calendarImageContainer);
		// date container that will holds the hint message for
		// available date format.
		HtmlPanelGroup dateFormatHintContainer = new HtmlPanelGroup();
		HtmlOutputText dateFormatHint = (HtmlOutputText) builderHelper.getComponent(ComponentType.OUTPUT_TEXT);
		dateFormatHint.setValue(dateFormatHintLabel);
		dateFormatHintContainer.getChildren().add(dateFormatHint);
		addStyleClass(dateFormatHintContainer, BuilderCssConstants.CMF_DATEPICKER_FORMAT_HINT);
		wrapperChildren.add(innerWrapper);
		wrapperChildren.add(dateFormatHintContainer);
		wrapperChildren.add(message);
	}
}
