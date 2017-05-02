package com.sirma.itt.seip.adapters.iiif;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.json.JsonObject;

/**
 * Wraps json object representing the ratio between physical dimensions and canvas dimensions
 * 
 * @author radoslav
 *
 */
public class PhysicalScale {

	private final JsonObject scale;

	/**
	 * Creates wrapper
	 * 
	 * @param scale
	 *            json object
	 */
	public PhysicalScale(JsonObject scale) {
		this.scale = scale;
	}

	/**
	 * Gets the json object
	 * 
	 * @return the object
	 */
	public JsonObject getScale() {
		return scale;
	}

	/**
	 * Converts the object to input stream
	 * 
	 * @return the input stream
	 */
	public InputStream getAsInputStream() {
		return new ByteArrayInputStream(scale.toString().getBytes(StandardCharsets.UTF_8));
	}

}
