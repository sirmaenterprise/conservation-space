package com.sirma.itt.emf.cls.retriever;

import java.util.List;

/**
 * Class containing search criteria for code lists. Extends
 * {@link SearchCriteria} that contains common criteria.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
public class CodeListSearchCriteria extends SearchCriteria {

	/** Tenant names. */
	private List<String> tenantNames;

	/** Boolean flag for excluding code values. */
	private boolean excludeValues;

	/**
	 * Gets the tenant names.
	 * 
	 * @return the tenant names
	 */
	public List<String> getTenantNames() {
		return tenantNames;
	}

	/**
	 * Sets the tenant names.
	 * 
	 * @param tenantNames
	 *            the new tenant names
	 */
	public void setTenantNames(List<String> tenantNames) {
		this.tenantNames = tenantNames;
	}

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
