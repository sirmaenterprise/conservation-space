package com.sirma.cmf.web;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for serialized document context.
 * 
 * @author svelikov
 */
public class SerializedDocumentContext {

	/** The context. */
	private Map<String, Serializable> context = new LinkedHashMap<String, Serializable>();

	/**
	 * Getter method for context.
	 * 
	 * @return the context
	 */
	public Map<String, Serializable> getContext() {
		return context;
	}

	/**
	 * Setter method for context.
	 * 
	 * @param context
	 *            the context to set
	 */
	public void setContext(Map<String, Serializable> context) {
		this.context = context;
	}

}
