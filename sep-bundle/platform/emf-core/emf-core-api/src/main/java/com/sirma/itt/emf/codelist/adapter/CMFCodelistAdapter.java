package com.sirma.itt.emf.codelist.adapter;

import java.math.BigInteger;
import java.util.Map;

import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.codelist.model.CodeValue;

/**
 * Adapter service for accessing codelist subsystem.
 *
 * @author BBonev
 */
public interface CMFCodelistAdapter extends CMFAdapterService {

	/**
	 * Gets the code values for the given codelist.
	 *
	 * @param codelist the codelist
	 * @return the code values
	 */
	Map<String, CodeValue> getCodeValues(Integer codelist);

	/**
	 * Reset codelist. 
	 */
	void resetCodelist();

	/**
	 * Retrieves all CodeLists from database.
	 * 
	 * @return all found codelists
	 */
	Map<BigInteger, String> getAllCodelists();
}
