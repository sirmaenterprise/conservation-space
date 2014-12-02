package com.sirma.itt.cmf.search;

/**
 * Determines the search types.
 * 
 * @author BBonev
 */
public interface SearchType {

	/** When searching only for cases. */
	String CASE = "case";
	/** When searching for cases and documents. */
	String CASE_AND_DOCUMENT = "case_and_document";
	/** When searching documents only. */
	String DOCUMENT = "document";
	/** When searching for tasks. */
	String TASK = "task";

}
