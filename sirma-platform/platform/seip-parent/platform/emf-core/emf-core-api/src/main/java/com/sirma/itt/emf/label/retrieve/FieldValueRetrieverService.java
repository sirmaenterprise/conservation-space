package com.sirma.itt.emf.label.retrieve;

import java.util.Map;

import com.sirma.itt.seip.domain.search.SearchRequest;

/**
 * A service used to obtain field values and labels.
 *
 * TODO maybe remove?
 */
public interface FieldValueRetrieverService {

	/**
	 * Gets the label. If the label can not be found, the original value is returned.
	 *
	 * @param fieldId
	 *            the id of the field for which we obtain the label.
	 * @param value
	 *            the values
	 * @return the label
	 */
	String getLabel(String fieldId, String value);

	/**
	 * Gets the label. If the label can not be found, the original value is returned.
	 *
	 * @param fieldId
	 *            the id of the field for which we obtain the label.
	 * @param value
	 *            the values
	 * @param additionalParameters
	 *            additional parameters used for retrieving. They will vary per implementation
	 * @return the label
	 */
	String getLabel(String fieldId, String value, SearchRequest additionalParameters);

	/**
	 * Gets labels for multiple values. <b>IMPORTANT</b> In most implementations it just calls getLabel method multiple
	 * times. Check your desired implementation and implement custom logic if needed.
	 *
	 * @param fieldId
	 *            the id of the field for which we obtain the labels
	 * @param values
	 *            values for which labels must be obtained
	 * @return {@link Map} with values as keys and their corresponding labels as values
	 */
	Map<String, String> getLabels(String fieldId, String[] values);

	/**
	 * Gets labels for multiple values. <b>IMPORTANT</b> In most implementations it just calls getLabel method multiple
	 * times. Check your desired implementation and implement custom logic if needed.
	 *
	 * @param fieldId
	 *            the id of the field for which we obtain the labels
	 * @param values
	 *            values for which labels must be obtained
	 * @param additionalParameters
	 *            additional parameters used for retrieving. They will vary per implementation
	 * @return {@link Map} with values as keys and their corresponding labels as values
	 */
	Map<String, String> getLabels(String fieldId, String[] values, SearchRequest additionalParameters);

	/**
	 * Gets the value-label pairs and total number of records for a given field.
	 *
	 * @param fieldId
	 *            the {@link FieldId} for which we obtain the values
	 * @param filter
	 *            the filter used to filter labels. "starts with" filter should be applied
	 * @param offset
	 *            returned results offset
	 * @param limit
	 *            number of returned results
	 * @return {@link RetrieveResponse} object containing total number of results and a list with value-label pairs
	 */
	RetrieveResponse getValues(String fieldId, String filter, Integer offset, Integer limit);

	/**
	 * Gets the value-label pairs and total number of records for a given field.
	 *
	 * @param fieldId
	 *            the {@link FieldId} for which we obtain the values
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
	RetrieveResponse getValues(String fieldId, String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit);
}
