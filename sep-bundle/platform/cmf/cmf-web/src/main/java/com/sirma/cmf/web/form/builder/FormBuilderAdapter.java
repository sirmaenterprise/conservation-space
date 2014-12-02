package com.sirma.cmf.web.form.builder;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLabel;

import com.sirma.itt.emf.domain.Pair;

/**
 * Allows extending classes to not implement all the methods but only those that is needed.
 * 
 * @author svelikov
 */
public class FormBuilderAdapter extends AbstractFormBuilder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateLabel(HtmlOutputLabel label) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateField(UIComponent uiComponent) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAfterFieldContent(UIComponent wrapper) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateWrapper(UIComponent wrapper) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getValueResultType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValidator(UIComponent component, Pair<String, String> validatorData) {

	}

}
