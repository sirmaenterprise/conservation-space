package com.sirma.cmf.web.form.builder;

import java.util.ArrayList;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Builder for checkbox field.
 *
 * @author svelikov
 */
public class CheckboxFieldBuilder extends FormBuilder {

	/**
	 * Instantiates a new checkbox field builder.
	 *
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public CheckboxFieldBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		return createCheckbox();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UIComponent> buildOutputField() {
		List<UIComponent> componentsList = new ArrayList<>();
		HtmlSelectBooleanCheckbox checkbox = createCheckbox();

		String propertyName = propertyDefinition.getName();
		checkbox.setId(getIdForField(propertyName));

		String valueExpressionString = getValueExpressionString(getInstanceName(), propertyName);

		ValueExpression ve = createValueExpression(valueExpressionString, getValueResultType());

		checkbox.setValueExpression("value", ve);

		checkbox.setDisabled(true);

		addStyleClass(checkbox, BuilderCssConstants.CMF_PREVIEW_FIELD);

		componentsList.add(checkbox);

		return componentsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getValueResultType() {
		return Boolean.class;
	}

	/**
	 * Creates the checkbox.
	 *
	 * @return the html select boolean checkbox
	 */
	private HtmlSelectBooleanCheckbox createCheckbox() {
		HtmlSelectBooleanCheckbox uiComponent = (HtmlSelectBooleanCheckbox) builderHelper
				.getComponent(ComponentType.SELECT_BOOLEAN_CHECKBOX);
		return uiComponent;
	}
}
