package com.sirma.itt.seip.eai.model.response.composed;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a related item with the type of relations and its id
 * 
 * @author bbanchev
 */
public class RelatedItem {
	@JsonProperty(value = "type", required = true)
	private String type;
	@JsonProperty(value = "uid", required = true)
	private String uid;

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for related item uid.
	 *
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Setter method for related item uid.
	 *
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

}
