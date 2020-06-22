package com.sirma.itt.seip.domain.codelist;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.domain.codelist.model.CodeValue;

/**
 * Service for accessing codelist information.
 *
 * @author BBonev
 */
public interface CodelistService {

	/**
	 * Gets the code values for the given codelist number.
	 *
	 * @param codelist
	 *            the codelist number
	 * @return the code values
	 */
	Map<String, CodeValue> getCodeValues(Integer codelist);

	/**
	 * Gets the code value for a codelist and value
	 *
	 * @param codelist
	 *            the codelist
	 * @param value
	 *            the value
	 * @return the code value
	 */
	CodeValue getCodeValue(Integer codelist, String value);

	/**
	 * Gets the code values for the given codelist number. The method can optionally sort the results by their
	 * description.
	 *
	 * @param codelist
	 *            the codelist
	 * @param sorted
	 *            the result should be sorted
	 * @return the code values
	 */
	Map<String, CodeValue> getCodeValues(Integer codelist, boolean sorted);

	/**
	 * Gets the description for the given codelist and code value based on the current default locale.
	 *
	 * @param codelist
	 *            the codelist
	 * @param value
	 *            the value
	 * @return The description according to locale. This may return null in cases where the codelist is null or there is
	 *         not such codelist in cache or there is no value found in the codelist for the key.
	 */
	String getDescription(Integer codelist, String value);

	/**
	 * Gets the description for the given codelist and code value based on the provided locale id.
	 *
	 * @param codelist
	 *            the codelist
	 * @param value
	 *            the value
	 * @param language
	 *            the language to use for fetching the description
	 * @return The description according to locale. This may return null in cases where the codelist is null or there is
	 *         not such codelist in cache or there is no value found in the codelist for the key.
	 */
	String getDescription(Integer codelist, String value, String language);

	/**
	 * Gets the filtered code values.
	 *
	 * @param codelist
	 *            the codelist
	 * @param filterId
	 *            the filter id
	 * @return the filtered code values
	 */
	Map<String, CodeValue> getFilteredCodeValues(Integer codelist, String... filterId);

	/**
	 * Gets the filtered code values and also sort the results if needed
	 *
	 * @param codelist
	 *            the codelist
	 * @param sorted
	 *            the sorted
	 * @param filterId
	 *            the filter id
	 * @return the filtered code values
	 */
	Map<String, CodeValue> getFilteredCodeValues(Integer codelist, boolean sorted, String... filterId);

	/**
	 * Gets the filtered code values
	 *
	 * @param codelist
	 *            the codelist
	 * @param inclusive
	 *            the inclusive
	 * @param field
	 *            the filter id
	 * @param filterValues
	 *            the filter values
	 * @return the filtered code values
	 */
	Map<String, CodeValue> filterCodeValues(Integer codelist, boolean inclusive, String field, String... filterValues);

	/**
	 * Filters codelist using custom filter
	 *
	 * @param codelist
	 *            the codelist number
	 * @param inclusive
	 *            true if inclusive operation should be executed. False otherwise
	 * @param values
	 *            the supplied custom filter values
	 * @return the filtered code values
	 */
	Map<String, CodeValue> filterCodeValues(Integer codelist, boolean inclusive, List<String> values);

	/**
	 * Gets the description from the given {@link CodeValue}. The description is determined based on the current user
	 * locale
	 *
	 * @param value
	 *            the value
	 * @return the description
	 */
	String getDescription(CodeValue value);

	/**
	 * Retrieves all CodeLists from database.
	 *
	 * @return all found codelists
	 */
	Map<BigInteger, String> getAllCodelists();

	/**
	 * Force reload internal cache (if any).
	 */
	void refreshCache();
}
