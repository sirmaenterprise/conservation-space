/**
 * Copyright (c) 2014 26.06.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data of an Instance.
 * 
 * @author Adrian Mitev
 */
public class Instance {

	private String id;

	private Map<String, String> properties = new HashMap<>();

	/**
	 * Getter method for id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Getter method for properties.
	 * 
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * Setter method for properties.
	 * 
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}
