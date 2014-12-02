package com.sirma.cmf.web.form.picklist;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * Common functions used in picklist and picklist value converters.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class PicklistUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PicklistUtil.class);

	/** The people service. */
	@Inject
	private ResourceService resourceService;

	/**
	 * Gets the value from hidden field.
	 * 
	 * @param component
	 *            the component
	 * @return the value
	 */
	public String getValue(UIComponent component) {
		String value = null;
		List<UIComponent> children = component.getParent().getChildren();
		for (UIComponent uiComponent : children) {
			String styleClass = (String) uiComponent.getAttributes().get("styleClass");
			if (StringUtils.isNotNullOrEmpty(styleClass)
					&& styleClass.contains("picklist-hidden-field")) {
				value = (String) ((HtmlInputText) uiComponent).getSubmittedValue();
				break;
			}
		}
		return value;
	}

	/**
	 * Converts a value that can be a collection or single string to a csv list.
	 * 
	 * @param valueInput
	 *            the value input
	 * @param resourceType
	 *            the resource type
	 * @return the string
	 */
	@SuppressWarnings("rawtypes")
	public String valueToString(Object valueInput, ResourceType resourceType) {
		try {
			Object value = ItemsConverter.convertObjectToItems(valueInput, false);
			if (value instanceof Collection) {
				StringBuilder resulted = new StringBuilder();
				Iterator iterator = ((Collection) value).iterator();
				while (iterator.hasNext()) {
					Object object = iterator.next();
					resulted.append(getResourceDisplayValue(object, resourceType));
					if (iterator.hasNext()) {
						resulted.append(",");
					}
				}
				return resulted.toString();
			}
			return getResourceDisplayValue(value, resourceType);
		} catch (final Exception e) {
			final String errorMessage = resourceType.getName() + " conversion error!";
			LOG.error(errorMessage, e);
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, errorMessage));
		}
	}

	/**
	 * Gets the display value for given resource.
	 * 
	 * @param value
	 *            the value as resource id
	 * @param resourceType
	 *            the resource type
	 * @return the display value for resource.
	 */
	private String getResourceDisplayValue(Object value, ResourceType resourceType) {
		String passedValue = (String) value;
		String displayName = "";
		if (StringUtils.isNotNullOrEmpty(passedValue)) {
			Resource resource = null;
			if (resourceType == ResourceType.GROUP) {
				resource = resourceService.getResource(passedValue, ResourceType.GROUP);
			} else {
				resource = resourceService.getResource(passedValue, ResourceType.USER);
			}
			if (resource != null) {
				displayName = resource.getDisplayName();
			}
		}
		return displayName;
	}

}
