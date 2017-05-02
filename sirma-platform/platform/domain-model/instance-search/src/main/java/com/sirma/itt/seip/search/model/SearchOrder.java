package com.sirma.itt.seip.search.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Represents a bean containing information about possible sorting parameters in search form
 *
 * @author bbanchev
 */
@JsonTypeName(value = "order")
public class SearchOrder {

	private String defaultOrder;

	private List<Map<String, String>> sortingFields;

	/**
	 * Gets the default order.
	 *
	 * @return the default order
	 */
	@JsonProperty(value = "default")
	public String getDefaultOrder() {
		return defaultOrder;
	}

	/**
	 * Sets the default order field id
	 *
	 * @param defaultOrderField
	 *            the new default order
	 */
	@JsonSetter(value = "default")
	public void setDefaultOrder(String defaultOrderField) {
		this.defaultOrder = defaultOrderField;
	}

	/**
	 * Gets the sorting fields.
	 *
	 * @return the sorting fields
	 */
	@JsonProperty(value = "properties")
	public List<Map<String, String>> getSortingFields() {
		return sortingFields;
	}

	/**
	 * Adds the sorting field as map entry
	 * 
	 * <pre>
	 * [{
	      "id": "emf:modifiedOn",
	      "text": "Modified on"
	    }]
	 * </pre>
	 *
	 * @param field
	 *            the field is the sorting field id
	 * @param label
	 *            the label is the label for this field
	 */
	public void addSortingField(String field, String label) {
		if (sortingFields == null) {
			sortingFields = new LinkedList<>();
		}
		Map<String, String> sorting = CollectionUtils.createHashMap(2);
		sorting.put("id", field);
		sorting.put("text", label);
		this.sortingFields.add(sorting);
	}

	/**
	 * Sets the sorting fields as list
	 * 
	 * <pre>
	 * [{
	      "id": "emf:modifiedOn",
	      "text": "Modified on"
	    }]
	 * </pre>
	 *
	 * @param sortingFields
	 *            is list of maps containing two keys - id and label for sorting param
	 */
	@JsonSetter(value = "properties")
	public void setSortingFields(List<Map<String, String>> sortingFields) {
		this.sortingFields = sortingFields;
	}

}
