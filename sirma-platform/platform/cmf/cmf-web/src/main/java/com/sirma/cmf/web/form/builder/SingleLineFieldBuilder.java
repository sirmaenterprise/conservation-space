package com.sirma.cmf.web.form.builder;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;

import com.sirma.cmf.web.form.CMFFieldValidator;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Builder for input text field.
 *
 * @author svelikov
 */
public class SingleLineFieldBuilder extends FormBuilder {

	/**
	 * Instantiates a new single line field builder.
	 *
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public SingleLineFieldBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		HtmlInputText uiComponent = (HtmlInputText) builderHelper.getComponent(ComponentType.INPUT_TEXT);
		addStyleClass(uiComponent, "form-control text-field");
		return uiComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	// TODO: svelikov This couldn't be tested easily
	@Override
	public void setFieldValidator(UIComponent component, Pair<String, String> validatorData) {
		HtmlInputText inputText = (HtmlInputText) component;
		inputText.getAttributes().put(VALIDATION_PATTERN, validatorData);
		CMFFieldValidator fieldValidator = new CMFFieldValidator();
		inputText.addValidator(fieldValidator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueResultType() {
		return String.class;
	}

}
