package com.sirma.itt.seip.domain.search.facet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.domain.search.SearchablePropertyProperties;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Represents a search facet with {@link FacetValue} and selected values.
 *
 * @author Mihail Radkov
 */
public class Facet extends SearchableProperty {

	private List<FacetValue> values;

	/** A set with ids of all selected values for this facet */
	private Set<String> selectedValues;

	private FacetConfiguration facetConfiguration;

	private String text;

	/**
	 * Getter method for values.
	 *
	 * @return the values
	 */
	public List<FacetValue> getValues() {
		return values;
	}

	/**
	 * Setter method for values.
	 *
	 * @param values
	 *            the values to set
	 */
	public void setValues(List<FacetValue> values) {
		this.values = values;
	}

	/**
	 * @return the selectedValues
	 */
	public Set<String> getSelectedValues() {
		return selectedValues;
	}

	/**
	 * @param selectedValues
	 *            the selectedValues to set
	 */
	public void setSelectedValues(Set<String> selectedValues) {
		this.selectedValues = selectedValues;
	}

	/**
	 * Gets the facet configuration.
	 *
	 * @return the facet configuration
	 */
	public FacetConfiguration getFacetConfiguration() {
		return facetConfiguration;
	}

	/**
	 * Sets the facet configuration.
	 *
	 * @param facetConfiguration
	 *            the new facet configuration
	 */
	public void setFacetConfiguration(FacetConfiguration facetConfiguration) {
		this.facetConfiguration = facetConfiguration;
	}

	@Override
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject facetItem = super.toJSONObject();

		JsonUtil.addToJson(facetItem, FacetProperties.SELECTED_VALUES, getSelectedValues());

		if (facetConfiguration != null) {
			JsonUtil.addToJson(facetItem, FacetProperties.ORDER, facetConfiguration.getOrder());
			JsonUtil.addToJson(facetItem, FacetProperties.HIDDEN, facetConfiguration.isDefault());
			JsonUtil.addToJson(facetItem, FacetProperties.SORT, facetConfiguration.getSort());
			JsonUtil.addToJson(facetItem, FacetProperties.SORT_ORDER, facetConfiguration.getSortOrder());
			JsonUtil.addToJson(facetItem, FacetProperties.PAGE_SIZE, facetConfiguration.getPageSize());
		}
		JSONArray facetValueArray = new JSONArray();
		if (getValues() != null) {
			for (FacetValue value : getValues()) {
				facetValueArray.put(value.toJSONObject());
			}
		}

		JsonUtil.addToJson(facetItem, FacetProperties.VALUES, facetValueArray);
		return facetItem;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		super.fromJSONObject(jsonObject);
		setText(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.TEXT));
		JSONArray selectedValuesJson = JsonUtil.getJsonArray(jsonObject, FacetProperties.SELECTED_VALUES);
		Set<String> selectedValuesSet = new HashSet<>();
		if (!JsonUtil.isNullOrEmpty(selectedValuesJson)) {
			for (int i = 0; i < selectedValuesJson.length(); i++) {
				selectedValuesSet.add(JsonUtil.getStringFromArray(selectedValuesJson, i));
			}
			setSelectedValues(selectedValuesSet);
		}

		facetConfiguration = new FacetConfiguration();
		getFacetConfiguration().setOrder(JsonUtil.getIntegerValue(jsonObject, FacetProperties.ORDER));
		getFacetConfiguration().setSort(JsonUtil.getStringValue(jsonObject, FacetProperties.SORT));
		getFacetConfiguration().setDefault(JsonUtil.getBooleanValue(jsonObject, FacetProperties.HIDDEN));
		getFacetConfiguration().setSortOrder(JsonUtil.getStringValue(jsonObject, FacetProperties.SORT_ORDER));
		getFacetConfiguration().setPageSize(JsonUtil.getIntegerValue(jsonObject, FacetProperties.PAGE_SIZE));
		getFacetConfiguration().setState(JsonUtil.getStringValue(jsonObject, FacetProperties.STATE));
		// Facet values are not added to the constructed facet.
	}

}
