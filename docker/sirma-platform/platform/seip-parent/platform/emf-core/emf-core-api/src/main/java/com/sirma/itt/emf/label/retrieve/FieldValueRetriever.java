package com.sirma.itt.emf.label.retrieve;

import java.util.Map;

import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.plugin.Supportable;

/**
 * An interface for retrieving field values and labels. Every implementation should be annotated with {@link FieldId}
 * annotation.
 */
public interface FieldValueRetriever extends Plugin, Supportable<String> {

	String TARGET_NAME = "fieldValueRetriever";

	/**
	 * Gets the label for a given value. Use for convenience if there are no additional parameters.
	 *
	 * @param value
	 *            the value for the field
	 * @return the label corresponding to the given value
	 */
	String getLabel(String value);

	/**
	 * Gets the label for a given value.
	 *
	 * @param value
	 *            the value for the field
	 * @param additionalParameters
	 *            additional parameters used for retrieving. They will vary per implementation
	 * @return the label corresponding to the given value
	 */
	String getLabel(String value, SearchRequest additionalParameters);

	/**
	 * Gets labels for multiple values. Use for convenience if there are no additional parameters.
	 *
	 * @param values
	 *            for which labels must be retrieved
	 * @return {@link Map} with values as keys and their corresponding labels as values
	 */
	Map<String, String> getLabels(String[] values);

	/**
	 * Gets labels for multiple values.
	 *
	 * @param values
	 *            for which labels must be retrieved
	 * @param additionalParameters
	 *            additional parameters used for retrieving. They will vary per implementation
	 * @return {@link Map} with values as keys and their corresponding labels as values
	 */
	Map<String, String> getLabels(String[] values, SearchRequest additionalParameters);

	/**
	 * Gets the values, labels and total number of records for a given field. Use for convenience if there are no
	 * additional parameters.
	 *
	 * @param filter
	 *            the filter used to filter labels. "starts with" filter should be applied
	 * @param offset
	 *            returned results offset
	 * @param limit
	 *            number of returned results
	 * @return {@link RetrieveResponse} object containing total number of results and a list with value-label pairs
	 */
	RetrieveResponse getValues(String filter, Integer offset, Integer limit);

	/**
	 * Gets the values, labels and total number of records for a given field.
	 *
	 * @param filter
	 *            the filter used to filter labels. "starts with" filter should be applied
	 * @param additionalParameters
	 *            additional parameters used for retrieving. They will vary per implementation
	 * @param offset
	 *            returned results offset
	 * @param limit
	 *            number of returned results
	 * @return {@link RetrieveResponse} object containing total number of results and a list with value-label pairs
	 */
	RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset, Integer limit);
}
