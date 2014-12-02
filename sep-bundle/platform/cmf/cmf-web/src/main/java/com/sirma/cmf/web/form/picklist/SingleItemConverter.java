package com.sirma.cmf.web.form.picklist;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

/**
 * SingleItemConverter used for the hidden input value holding single username/group.
 * 
 * @author bbanchev
 */
@Named
@ApplicationScoped
public class SingleItemConverter implements Converter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object valueInput) {
		Object value = ItemsConverter.convertObjectToItems(valueInput, false);
		if (value instanceof String) {
			return value.toString();
		}
		final String errorMessage = "Resource conversion error!";
		throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage,
				errorMessage));

	}

}
