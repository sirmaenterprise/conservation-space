package com.sirma.cmf.web.form.builder;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputTextarea;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.CMFFieldValidator;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Builder for textarea field.
 *
 * @author svelikov
 */
public class MultyLineFieldBuilder extends FormBuilder {

	/**
	 * Instantiates a new multy line field builder.
	 *
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public MultyLineFieldBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		HtmlInputTextarea uiComponent = (HtmlInputTextarea) builderHelper.getComponent(ComponentType.INPUT_TEXT_AREA);
		addStyleClass(uiComponent, "form-control text-field");
		return uiComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getValueResultType() {
		return String.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValidator(UIComponent component, Pair<String, String> validatorData) {
		HtmlInputTextarea inputText = (HtmlInputTextarea) component;
		inputText.getAttributes().put(VALIDATION_PATTERN, validatorData);
		CMFFieldValidator fieldValidator = new CMFFieldValidator();
		inputText.addValidator(fieldValidator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateWrapper(UIComponent wrapper) {
		addStyleClass(wrapper, BuilderCssConstants.CMF_TEXTAREA_WRAPPER);
	}

}
