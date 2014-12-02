package com.sirma.itt.emf.cls.service;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;

/**
 * Interface for retrieving code lists and values based on certain search
 * criteria.
 * 
 * @author Mihail Radkov
 */
public interface CodeListService {

	/**
	 * Retrieves code lists based on provided search criteria.
	 * 
	 * @param criteria
	 *            the provided search criteria
	 * @return {@link SearchResult} containing total number of results, returned
	 *         results and pagination parameters
	 */
	SearchResult getCodeLists(CodeListSearchCriteria criteria);

	/**
	 * Retrieves code values based on provided search criteria.
	 * 
	 * @param criteria
	 *            the provided search criteria
	 * @return {@link SearchResult} containing total number of results, returned
	 *         results and pagination parameters
	 */
	SearchResult getCodeValues(CodeValueSearchCriteria criteria);

	/**
	 * Saves or updates a code list depending on the boolean flag. If the code
	 * list information is invalid an exception is thrown.
	 * 
	 * @param codeList
	 *            the provided code list
	 * @param update
	 *            boolean flag specifying if the code list should be updated or
	 *            new to be saved
	 * @throws CodeListException
	 *             if the information in the code list is invalid
	 */
	void saveOrUpdateCodeList(CodeList codeList, boolean update)
			throws CodeListException;

	/**
	 * Saves a new version of the given codeValue by saving the codevalue in the
	 * database with the applied changes and updating the last (latest validTo
	 * date) codevalues validTo date to match the new versions validFrom date.
	 * 
	 * @param codeValue
	 *            the provided code value
	 * @throws CodeListException
	 *             if the information in the code list is invalid
	 */
	void updateCodeValue(CodeValue codeValue) throws CodeListException;

	/**
	 * Persists a new code value to the database.
	 * 
	 * @param codeValue
	 *            the code value to be persisted
	 * @throws CodeListException
	 *             the exception thrown if something went wrong
	 */
	void saveCodeValue(CodeValue codeValue) throws CodeListException;

}
