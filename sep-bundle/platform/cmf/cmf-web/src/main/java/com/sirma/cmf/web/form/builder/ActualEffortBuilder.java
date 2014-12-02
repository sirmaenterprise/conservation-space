package com.sirma.cmf.web.form.builder;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder class for actual effort field. Used to convert field value from minutes to user readable string of type d h m (example: 2d 4h 30m)
 */
public class ActualEffortBuilder extends FormBuilder {

	/**
	 * Instantiates a new actual effort builder.
	 *
	 * @param labelProvider the label provider
	 * @param codelistService the codelist service
	 */
	public ActualEffortBuilder(LabelProvider labelProvider,
			CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		HtmlOutputText output = (HtmlOutputText) builderHelper
				.getComponent(ComponentType.OUTPUT_TEXT);

		String fieldId = getIdForField(propertyDefinition.getName());
		output.setId(fieldId);

		String valueExpressionForUser = getValueExpressionForActualEffort(propertyValue);
		output.setValueExpression("value",
				createValueExpression(valueExpressionForUser, String.class));

		addStyleClass(output, BuilderCssConstants.CMF_PREVIEW_FIELD);

		return output;
	}

	/**
	 * Gets the value expression for actual effort.
	 * 
	 * @param value
	 *            actual effort in minutes
	 * @return the value expression for actual effort in time string
	 */
	protected String getValueExpressionForActualEffort(Object value) {
		String expression = "";
		if (value instanceof Number) {
			expression = "#{dateUtil.convertMinutesToTimeString(" + value + ")}";
		}
		return expression;
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
	public UIComponent buildOutputField() {
		UIComponent uiComponent = getComponentInstance();
		return uiComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueResultType() {
		return String.class;
	}
	
}
