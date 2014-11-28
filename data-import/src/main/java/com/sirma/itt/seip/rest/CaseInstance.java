package com.sirma.itt.seip.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data of a case instance.
 * 
 * @author Adrian Mitev
 */
public class CaseInstance extends Instance {

	private Map<String, String> sections = new HashMap<>();

	/**
	 * Getter method for sections.
	 * 
	 * @return the sections
	 */
	public Map<String, String> getSections() {
		return sections;
	}

	/**
	 * Setter method for sections.
	 * 
	 * @param sections
	 *            the sections to set
	 */
	public void setSections(Map<String, String> sections) {
		this.sections = sections;
	}

}
