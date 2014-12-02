package com.sirma.itt.emf.web.rest;

import java.util.Map;

/**
 * The Class FreemarkerModel.
 * 
 * @author hlungov
 */
public class FreemarkerModel {

	/** The model. */
	private Map<String, Object> model;

	/** The view. */
	private String view;

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public Map<String, Object> getModel() {
		return model;
	}

	/**
	 * Sets the model.
	 * 
	 * @param value
	 *            the value
	 */
	public void setModel(Map<String, Object> value) {
		this.model = value;
	}

	/**
	 * Gets the view.
	 * 
	 * @return the view
	 */
	public String getView() {
		return view;
	}

	/**
	 * Sets the view.
	 * 
	 * @param value
	 *            the new view
	 */
	public void setView(String value) {
		this.view = value;
	}
}