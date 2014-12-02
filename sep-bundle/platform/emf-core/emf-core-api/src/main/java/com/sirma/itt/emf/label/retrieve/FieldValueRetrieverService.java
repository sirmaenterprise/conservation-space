package com.sirma.itt.emf.label.retrieve;

/**
 * A service used to obtain field values and labels.
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
	public String getLabel(String fieldId, String... value);

	/**
	 * Gets the value-label pairs and total number of records for a given field
	 * 
	 * @param fieldId
	 *            the {@link FieldId} for which we obtain the values
	 * @param filter
	 *            the filter used to filter labels. "starts with" filter should be applied
	 * @param offset
	 *            returned results offset
	 * @param limit
	 *            number of returned results
	 * @return {@link RetrieveResponse} object containing total number of results and a list with
	 *         value-label pairs
	 */
	public RetrieveResponse getValues(String fieldId, String filter, Integer offset, Integer limit);
}
