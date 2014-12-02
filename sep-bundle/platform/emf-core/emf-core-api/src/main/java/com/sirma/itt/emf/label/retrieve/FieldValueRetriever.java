package com.sirma.itt.emf.label.retrieve;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.plugin.Supportable;

/**
 * An interface for retrieving field values and labels. Every implementation should be annotated
 * with {@link FieldId} annotation.
 */
public interface FieldValueRetriever extends Plugin, Supportable<String> {

	String TARGET_NAME = "fieldValueRetriever";

	/**
	 * Gets the label for a given value
	 * 
	 * @param value
	 *            the value for the field
	 * @return the label corresponding to the given value
	 */
	public String getLabel(String... value);

	/**
	 * Gets the values, labels and total number of records for a given field
	 * 
	 * @param filter
	 *            the filter used to filter labels. "starts with" filter should be applied
	 * @param offset
	 *            returned results offset
	 * @param limit
	 *            number of returned results
	 * @return {@link RetrieveResponse} object containing total number of results and a list with
	 *         value-label pairs
	 */
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit);
}
