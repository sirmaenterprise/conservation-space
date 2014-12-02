package com.sirma.itt.objects.web.menu;

import java.util.ArrayList;
import java.util.List;

import com.sirma.cmf.web.rest.SearchQueryParameters;
import com.sirma.itt.emf.web.rest.EmfQueryParameters;

/**
 * ObjectLibraryMenuItem.
 * 
 * @author svelikov
 */
public class ObjectLibraryMenuItem {

	/** The object library menu label */
	private String label;

	/** The short uri. */
	private String shortUri;

	/** Query parameters used to init the search that would load the data for this library. */
	private List<ObjectTypeParameter> parameters;

	/**
	 * Instantiates a new object library menu item.
	 * 
	 * @param label
	 *            the label
	 * @param shortUri
	 *            the short uri
	 */
	public ObjectLibraryMenuItem(String label, String shortUri) {
		this.label = label;
		this.shortUri = shortUri;
		this.parameters = new ArrayList<>();
	}

	/**
	 * Adds a new query parameter to the list.
	 * 
	 * @param param
	 *            the param
	 * @param value
	 *            the value
	 */
	public void addParameter(String param, String value) {
		if ((param != null) && (value != null)) {
			parameters.add(new ObjectTypeParameter(param, value));
		}
	}

	/**
	 * Converts the parameters list to query string.
	 * 
	 * @return the string
	 */
	public String joinParameters() {
		StringBuilder joined = new StringBuilder();
		joined.append(EmfQueryParameters.LIBRARY).append("=object&");
		joined.append(EmfQueryParameters.LIBRARY_TITLE).append("=").append(label).append("&");
		for (ObjectTypeParameter parameter : parameters) {
			joined.append(parameter.getParameter()).append("=").append(parameter.getValue())
					.append("&");
		}
		return joined.substring(0, joined.length() - 1);
	}

	/**
	 * Gets the object type from query parameters if any.
	 * 
	 * @return the object type
	 */
	public String getObjectType() {
		for (ObjectTypeParameter parameter : parameters) {
			if (SearchQueryParameters.OBJECT_TYPE.equals(parameter.getParameter())) {
				return parameter.getValue();
			}
		}
		return "";
	}

	/**
	 * Getter method for label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Setter method for label.
	 * 
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Getter method for parameters.
	 * 
	 * @return the parameters
	 */
	public List<ObjectTypeParameter> getParameters() {
		return parameters;
	}

	/**
	 * Setter method for parameters.
	 * 
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(List<ObjectTypeParameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Getter method for shortUri.
	 * 
	 * @return the shortUri
	 */
	public String getShortUri() {
		return shortUri;
	}

	/**
	 * Setter method for shortUri.
	 * 
	 * @param shortUri
	 *            the shortUri to set
	 */
	public void setShortUri(String shortUri) {
		this.shortUri = shortUri;
	}

}
