package com.sirma.itt.emf.label.retrieve;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.domain.Pair;

/**
 * An object holding information for retrieving field's value-label list
 */
public class RetrieveResponse {
	
	private Long total;
	private List<Pair<String, String>> results;
	
	
	/**
	 * Instantiates a new retrieve response.
	 *
	 * @param total the total number of results
	 * @param results value-label pairs with the results page
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
	 * @param total the total to set
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
	 * @param results the results to set
	 */
	public void setResults(List<Pair<String, String>> results) {
		this.results = results;
	}
	
	/**
	 * Converts {@link RetrieveResponse} to an appropriate jSON string
	 *
	 * @return the string
	 * @throws JSONException the jSON exception
	 */
	public String toJSONString() throws JSONException {
		JSONArray results = new JSONArray();
		if (this.results != null) {
			for (Pair<String, String> value : this.results) {
				JSONObject obj = new JSONObject();
				obj.put("id", value.getFirst());
				obj.put("text", value.getSecond());
				results.put(obj);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("total", this.total);
		result.put("results", results);
		return result.toString();
	}

}
