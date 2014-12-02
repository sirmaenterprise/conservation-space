/**
 * 
 */
package com.sirma.itt.emf.audit.activity;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.emf.time.DateRange;

/**
 * Class for constructing a criteria for retrieving BAM activities.
 * 
 * @author Mihail Radkov
 */
public class AuditActivityCriteria {

	/** The date range. */
	private DateRange dateRange;

	/** The ids. */
	private List<String> ids;

	/** The included username. */
	private String includedUsername;

	/** The excluded username. */
	private String excludedUsername;

	/** The criteria type. */
	private CriteriaType criteriaType;

	/**
	 * Sets the date range.
	 * 
	 * @param dateRange
	 *            the date range
	 * @return the current criteria
	 */
	public AuditActivityCriteria setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
		return this;
	}

	/**
	 * Gets the date range.
	 * 
	 * @return the date range
	 */
	public DateRange getDateRange() {
		return dateRange;
	}

	/**
	 * Sets the ids.
	 * 
	 * @param ids
	 *            the ids
	 * @return the current criteria
	 */
	public AuditActivityCriteria setIds(List<String> ids) {
		this.ids = ids;
		return this;
	}

	/**
	 * Gets the ids.
	 * 
	 * @return the ids
	 */
	public List<String> getIds() {
		return ids;
	}

	/**
	 * Adds an id.
	 * 
	 * @param id
	 *            the id
	 * @return the current criteria
	 */
	public AuditActivityCriteria addId(String id) {
		if (ids == null) {
			this.ids = new ArrayList<String>();
		}
		this.ids.add(id);
		return this;
	}

	/**
	 * Sets the included username.
	 * 
	 * @param username
	 *            the username
	 * @return the current criteria
	 */
	public AuditActivityCriteria setIncludedUsername(String username) {
		this.includedUsername = username;
		return this;
	}

	/**
	 * Gets the included username.
	 * 
	 * @return the included username
	 */
	public String getIncludedUsername() {
		return includedUsername;
	}

	/**
	 * Sets the excluded username.
	 * 
	 * @param username
	 *            the username
	 * @return the current criteria
	 */
	public AuditActivityCriteria setExcludedUsername(String username) {
		this.excludedUsername = username;
		return this;
	}

	/**
	 * Gets the excluded username.
	 * 
	 * @return the excluded username
	 */
	public String getExcludedUsername() {
		return excludedUsername;
	}

	/**
	 * Sets the criteria type.
	 * 
	 * @param criteriaType
	 *            the new criteria type
	 * @return the current criteria
	 */
	public AuditActivityCriteria setCriteriaType(CriteriaType criteriaType) {
		this.criteriaType = criteriaType;
		return this;
	}

	/**
	 * Gets the criteria type.
	 * 
	 * @return the criteria type
	 */
	public CriteriaType getCriteriaType() {
		return criteriaType;
	}
}
