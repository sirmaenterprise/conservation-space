package com.sirma.cmf.web.form.picklist;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.resources.ResourceType;

/**
 * SingleItemPreviewConverter used for the preview field in picklist component to convert between
 * userId and displayName.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class SingleUserPreviewConverter implements Converter {

	@Inject
	private PicklistUtil picklistUtil;

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
		String userId = picklistUtil.getValue(component);
		return userId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object valueInput) {
		String stringValue = picklistUtil.valueToString(valueInput, ResourceType.USER);
		return stringValue;
	}

}