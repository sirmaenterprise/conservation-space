package com.sirma.cmf.web.form.picklist;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.resources.ResourceType;

/**
 * SingleItemPreviewConverter used for the preview field in picklist component to convert between userId and
 * displayName.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class SingleUserPreviewConverter implements Converter {

	@Inject
	private PicklistUtil picklistUtil;

	/**
	 * Find out the hidden field in picklist wrapper and return its value insted of that passed for the preview field to
	 * which this convertor is registered.
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
		return picklistUtil.getValue(component);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object valueInput) {
		return picklistUtil.valueToString(valueInput, ResourceType.USER);
	}

}