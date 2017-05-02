package com.sirma.itt.seip.domain.search;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonUtil;

/**
 * The Class JsonSearchRequestMap.
 */
public class JsonSearchRequestMap extends HashMap<String, List<String>> {
	private static final long serialVersionUID = -6171588168053755194L;

	private JSONObject json;

	/**
	 * Instantiates a new json search request map.
	 *
	 * @param json
	 *            the json
	 */
	public JsonSearchRequestMap(JSONObject json) {
		this.json = json;
	}

	@Override
	public List<String> get(Object key) {
		List<String> value = new LinkedList<>();
		String property = String.valueOf(key);
		if (property.endsWith("[]")) {
			property = property.substring(0, property.length() - 2);
			JSONArray array = JsonUtil.getJsonArray(json, property);
			if (array != null) {
				int length = array.length();
				for (int i = 0; i < length; i++) {
					value.add(JsonUtil.getStringFromArray(array, i));
				}
			}
		} else {
			Object temp = JsonUtil.getValueOrNull(json, property);
			if (temp instanceof JSONArray) {
				JSONArray array = (JSONArray) temp;
				int length = array.length();
				for (int i = 0; i < length; i++) {
					value.add(JsonUtil.getStringFromArray(array, i));
				}
			} else {
				String string = JsonUtil.getStringValue(json, property);
				if (StringUtils.isNotBlank(string)) {
					value.add(string);
				}
			}
		}
		return value;
	}

	@Override
	public boolean isEmpty() {
		return json == null || json.length() == 0;
	}
}
