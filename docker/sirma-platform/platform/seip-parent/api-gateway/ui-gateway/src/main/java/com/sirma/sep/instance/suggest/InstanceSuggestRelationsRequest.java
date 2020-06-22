package com.sirma.sep.instance.suggest;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Holds information needed for instance relation suggestion.
 *
 * @author Boyan Tonchev.
 */
public class InstanceSuggestRelationsRequest {

	/**
	 * Definition id of property which will be suggested.
	 *
	 */
	private String definitionId;

	/**
	 * the property which will be suggested.
	 */
	private String propertyName;

	/**
	 *  Keyword used to find suggested results from "altTitle".
	 */
	private String keywords;

	/**
	 * Setter for definition id.
	 *
	 * @param definitionId - the definition id.
	 */
	public void setDefinitionId(String definitionId) {
		validate("definitionId", definitionId);
		this.definitionId = definitionId;
	}

	/**
	 * Getter for definition id.
	 *
	 * @return the definition id.
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * Setter for property name.
	 *
	 * @param propertyName - the property name.
	 */
	public void setPropertyName(String propertyName) {
		validate("propertyName", propertyName);
		this.propertyName = propertyName;
	}

	/**
	 * Getter for property name.
	 *
	 * @return the property name.
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Setter for keywords.
	 *
	 * @param keywords - the keywords.
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * Getter for keywords.
	 *
	 * @return - the keywords
	 */
	public String getKeywords() {
		return keywords;
	}

	private void validate(String key, String value) {
		if (StringUtils.isBlank(value)) {
			throw new BadRequestException("Missing mandatory parameter: " + key);
		}
	}
}
