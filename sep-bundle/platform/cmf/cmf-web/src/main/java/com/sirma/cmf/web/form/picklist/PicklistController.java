package com.sirma.cmf.web.form.picklist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * PicklistController is responsible for operations in picklist component.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class PicklistController extends Action implements Serializable {

	private static final long serialVersionUID = 6246028109995287580L;

	@Inject
	private ResourceService resourceService;

	/**
	 * Gets the list of items as display names. Ids are split by '|'.
	 * 
	 * @param value
	 *            the list/value for user/s
	 * @param itemType
	 *            is the current item type
	 * @return the list of items display names as string
	 */
	@SuppressWarnings("unchecked")
	public String getAsString(Object value, String itemType) {
		try {
			Collection<String> iterable = null;
			if (value instanceof String) {
				if (StringUtils.isNullOrEmpty((String) value)) {
					return "";
				}
				String[] users = ((String) value).split(ItemsConverter.DELIMITER);
				iterable = new ArrayList<>(users.length);
				for (String user : users) {
					iterable.add(user);
				}
			} else if (value instanceof Collection) {
				iterable = (Collection<String>) value;
			}
			StringBuilder resulted = new StringBuilder();
			if (iterable != null) {
				Iterator<String> iterator = iterable.iterator();
				while (iterator.hasNext()) {
					String object = iterator.next();
					resulted.append(getDisplayValue(object, itemType));
					if (iterator.hasNext()) {
						resulted.append(", ");
					}
				}
			}
			return resulted.toString();
		} catch (final Exception e) {
			final String errorMessage = "Item conversion error!";
			log.error(errorMessage, e);
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, errorMessage));
		}
	}

	/**
	 * Picklist retrieving of value for field.
	 * 
	 * @param value
	 *            is the value
	 * @param itemType
	 *            is the item type
	 * @param multivalued
	 *            whether is multivalue
	 * @return the value for current picker
	 */
	public Object getAsValue(Object value, String itemType, boolean multivalued) {

		return value;

	}

	/**
	 * Gets the display value for item.
	 * 
	 * @param value
	 *            the value
	 * @param itemType
	 *            current item type
	 * @return the display value for item
	 */
	private String getDisplayValue(String value, String itemType) {
		String passedValue = value;
		String displayName = "";
		ResourceType resourceType = ResourceType.getByType(itemType);
		Resource resource = null;
		if (resourceType == ResourceType.ALL) {
			resource = resourceService.getResource(passedValue, ResourceType.UNKNOWN);
		} else {
			resource = resourceService.getResource(passedValue, resourceType);
		}
		if (resource != null) {
			displayName = resource.getDisplayName();
		}
		return displayName;
	}

}
