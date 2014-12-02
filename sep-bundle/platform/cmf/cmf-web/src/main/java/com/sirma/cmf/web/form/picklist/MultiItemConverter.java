package com.sirma.cmf.web.form.picklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * MultiItemConverter used for the hidden input value holding multiple usernames/groups separated by
 * ','.
 * 
 * @author bbanchev
 */
@Named
@ApplicationScoped
public class MultiItemConverter implements Converter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Find out the hidden field in picklist wrapper and return its value insted of that passed for
	 * the preview field to which this convertor is registered.
	 * 
	 * @param context
	 *            the context
	 * @param component
	 *            the component
	 * @param value
	 *            the value
	 * @return the as object
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		List<String> children = Collections.emptyList();
		String styleClass = (String) component.getAttributes().get("styleClass");
		if (StringUtils.isNotNullOrEmpty(styleClass)
				&& styleClass.contains("picklist-hidden-field")) {
			children = getMultiValueList(value);
		} else {
			children = Collections.emptyList();
		}
		return children;
	}

	/**
	 * Gets the multi value list.
	 * 
	 * @param value
	 *            the value
	 * @return the multi value list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> getMultiValueList(Object value) {
		if (value instanceof String) {
			return ItemsConverter.convertObjectToItems(value, true);
		} else if (value instanceof Collection) {
			return new ArrayList<>((Collection) value);
		}
		return Collections.emptyList();
	}

	/**
	 * Interates over array of values and returns single string representing the user names.
	 * {@inheritDoc}
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object valueInput) {
		try {
			Object value = ItemsConverter.convertObjectToItems(valueInput, true);
			if (value instanceof Collection) {
				return ItemsConverter.convertItemsToString(value);
			}
			return valueInput.toString();
		} catch (final Exception e) {
			final String errorMessage = "Resource conversion error!";
			log.error(errorMessage);
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, errorMessage));
		}
	}

}
