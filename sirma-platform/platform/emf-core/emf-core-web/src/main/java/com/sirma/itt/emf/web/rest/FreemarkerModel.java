package com.sirma.itt.emf.web.rest;

import java.util.Map;

/**
 * The Class FreemarkerModel.
 *
 * @author hlungov
 */
public class FreemarkerModel {

	private Map<String, Object> model;

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
		model = value;
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
		view = value;
	}
}