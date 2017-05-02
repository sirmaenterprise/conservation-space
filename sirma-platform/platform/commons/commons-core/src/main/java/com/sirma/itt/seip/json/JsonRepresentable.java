package com.sirma.itt.seip.json;

import org.json.JSONObject;

/**
 * The implementer of the interface should provide the JSON object representation of the implementing instance
 *
 * @author BBonev
 */
public interface JsonRepresentable {

	/**
	 * To JSON object representing the current instance.
	 *
	 * @return the JSON object
	 */
	JSONObject toJSONObject();

	/**
	 * Initialize the internal state of the implemented class from the given JSON object
	 *
	 * @param jsonObject
	 *            the source json object
	 */
	void fromJSONObject(JSONObject jsonObject);

	/**
	 * Static method to be used in stream api map function.
	 *
	 * @param representable
	 *            the representable
	 * @return the JSON object
	 */
	static JSONObject toJson(JsonRepresentable representable) {
		return representable.toJSONObject();
	}
}
