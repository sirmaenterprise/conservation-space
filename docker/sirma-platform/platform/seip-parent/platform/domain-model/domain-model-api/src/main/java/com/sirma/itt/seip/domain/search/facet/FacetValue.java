package com.sirma.itt.seip.domain.search.facet;

import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Represents a facet value.
 *
 * @author Mihail Radkov
 */
public class FacetValue implements JsonRepresentable {

	private String id;

	private long count;

	private String text;

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
	 * Getter method for count.
	 *
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Setter method for count.
	 *
	 * @param count
	 *            the count to set
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 *
	 * @param text
	 *            the new text
	 */
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject value = new JSONObject();
		JsonUtil.addToJson(value, "id", getId());
		JsonUtil.addToJson(value, "count", getCount());
		JsonUtil.addToJson(value, "text", getText());
		return value;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// TODO implement
	}

}
