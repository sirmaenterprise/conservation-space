package com.sirma.cmf.web.form.picklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * UserConverter used for the preview field in picklist component to convert between userId and displayName.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class MultiItemPreviewConverter implements Converter {

	private static final Logger LOG = LoggerFactory.getLogger(MultiItemPreviewConverter.class);

	/** The people service. */
	@Inject
	private ResourceService resourceService;

	@Inject
	private PicklistUtil picklistUtil;

	/**
	 * Find out the hidden field in picklist wrapper and return its value instead of that passed for the preview field
	 * to which this convertor is registered.
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
		Object item = picklistUtil.getValue(component);
		item = ItemsConverter.convertObjectToItems(item, true);
		return item;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object valueInput) {
		try {
			Object value = ItemsConverter.convertObjectToItems(valueInput, true);
			if (value instanceof Collection) {
				ArrayList<String> resulted = new ArrayList<String>(((Collection) value).size());
				Iterator iterator = ((Collection) value).iterator();
				while (iterator.hasNext()) {
					Object object = iterator.next();
					resulted.add(getDisplayValueForUser(object));
				}
				return ItemsConverter.convertItemsToString(resulted, ", ");
			}
			return getDisplayValueForUser(value);
		} catch (final Exception e) {
			final String errorMessage = "User conversion error!";
			LOG.error(errorMessage, e);
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, errorMessage));
		}
	}

	/**
	 * Gets the display value for user.
	 *
	 * @param value
	 *            the value
	 * @return the display value for user
	 */
	private String getDisplayValueForUser(Object value) {
		String passedValue = (String) value;
		String displayName = "";
		if (StringUtils.isNotNullOrEmpty(passedValue)) {
			Resource resource = resourceService.findResource(passedValue);
			if (resource != null) {
				displayName = resource.getDisplayName();
			}
		}
		return displayName;
	}

}