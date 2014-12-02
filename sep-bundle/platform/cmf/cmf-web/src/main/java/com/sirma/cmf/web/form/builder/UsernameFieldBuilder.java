package com.sirma.cmf.web.form.builder;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * UsernameFieldBuilder is responsible for building of output field for displaying of user name in
 * generated form.
 * 
 * @author svelikov
 */
public class UsernameFieldBuilder extends FormBuilder {

	/**
	 * Instantiates a new username field builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public UsernameFieldBuilder(LabelProvider labelProvider, CodelistService codelistService) {
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

		String valueExpressionForUser = getValueExpressionForUser(propertyDefinition.getName());
		output.setValueExpression("value",
				createValueExpression(valueExpressionForUser, String.class));

		addStyleClass(output, BuilderCssConstants.CMF_PREVIEW_FIELD);

		return output;
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
	public Class<String> getValueResultType() {
		return String.class;
	}

}
