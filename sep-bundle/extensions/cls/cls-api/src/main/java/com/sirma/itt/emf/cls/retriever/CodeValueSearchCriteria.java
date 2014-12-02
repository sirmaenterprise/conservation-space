package com.sirma.itt.emf.cls.retriever;

/**
 * Class containing search criteria for code values. Extends {@link SearchCriteria} that contains
 * common criteria.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
public class CodeValueSearchCriteria extends SearchCriteria {

	/** The code list ID. */
	private String codeListId;

	/**
	 * Gets the code list ID.
	 * 
	 * @return the code list ID
	 */
	public String getCodeListId() {
		return codeListId;
	}

	/**
	 * Sets the code list ID.
	 * 
	 * @param codeListId
	 *            the new code list ID
	 */
	public void setCodeListId(String codeListId) {
		this.codeListId = codeListId;
	}

}
