package com.sirma.itt.emf.cls.retriever;

/**
 * Class containing search criteria for code lists. Extends {@link SearchCriteria} that contains common criteria.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
public class CodeListSearchCriteria extends SearchCriteria {

	/** Boolean flag for excluding code values. */
	private boolean excludeValues;

	/**
	 * Checks if is excluding code values.
	 *
	 * @return true, if is excluding code values
	 */
	public boolean isExcludeValues() {
		return excludeValues;
	}

	/**
	 * Sets the excluding code values.
	 *
	 * @param excludeValues
	 *            the new excluding code values
	 */
	public void setExcludeValues(boolean excludeValues) {
		this.excludeValues = excludeValues;
	}

}
