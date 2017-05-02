package com.sirma.itt.seip.domain.codelist.adapter;

import java.math.BigInteger;
import java.util.Map;

import com.sirma.itt.seip.MutationObservable;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;

/**
 * Adapter service for accessing codelist subsystem.
 *
 * @author BBonev
 */
public interface CodelistAdapter extends MutationObservable {

	/**
	 * Checks if is configured.
	 *
	 * @return true, if is configured
	 */
	boolean isConfigured();

	/**
	 * Gets the code values for the given codelist.
	 *
	 * @param codelist
	 *            the codelist
	 * @param locale
	 *            for instance BG, EN, FIN, etc.
	 * @return the code values
	 */
	Map<String, CodeValue> getCodeValues(Integer codelist, String locale);

	/**
	 * Reset codelist.
	 */
	void resetCodelist();

	/**
	 * Retrieves all CodeLists from database.
	 *
	 * @param locale
	 *            for instance BG, EN, FIN, etc.
	 *
	 * @return all found codelists
	 */
	Map<BigInteger, String> getAllCodelists(String locale);
}
