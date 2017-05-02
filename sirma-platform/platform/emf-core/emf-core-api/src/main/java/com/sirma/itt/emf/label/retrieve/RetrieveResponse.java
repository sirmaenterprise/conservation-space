package com.sirma.itt.emf.label.retrieve;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * An object holding information for retrieving field's value-label list
 */
public class RetrieveResponse implements JsonRepresentable {

	private Long total;
	private List<Pair<String, String>> results;

	/**
	 * Instantiates a new retrieve response.
	 *
	 * @param total
	 *            the total number of results
	 * @param results
	 *            value-label pairs with the results page
	 */
	public RetrieveResponse(Long total, List<Pair<String, String>> results) {
		this.total = total;
		this.results = results;
	}

	/**
	 * @return the total number of results
	 */
	public Long getTotal() {
		return total;
	}

	/**
	 * @param total
	 *            the total to set
	 */
	public void setTotal(Long total) {
		this.total = total;
	}

	/**
	 * @return the results
	 */
	public List<Pair<String, String>> getResults() {
		return results;
	}

	/**
	 * @param results
	 *            the results to set
	 */
	public void setResults(List<Pair<String, String>> results) {
		this.results = results;
	}

	/**
	 * Converts {@link RetrieveResponse} to an appropriate jSON string
	 *
	 * @return the string
	 */
	public String toJSONString() {
		return toJSONObject().toString();
	}

	@Override
	public JSONObject toJSONObject() {
		JSONArray array = new JSONArray();
		if (results != null) {
			for (Pair<String, String> value : results) {
				JSONObject obj = new JSONObject();
				JsonUtil.addToJson(obj, "id", value.getFirst());
				JsonUtil.addToJson(obj, "text", value.getSecond());
				array.put(obj);
			}
		}

		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, "total", total);
		JsonUtil.addToJson(result, "results", array);
		return result;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// read is not supported for now
	}

}
