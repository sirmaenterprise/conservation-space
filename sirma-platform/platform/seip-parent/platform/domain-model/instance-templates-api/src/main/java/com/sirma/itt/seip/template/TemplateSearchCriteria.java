package com.sirma.itt.seip.template;

import java.io.Serializable;
import java.util.Map;

/**
 * Wraps the search criteria for finding and filtering templates.
 *
 * @author Vilizar Tsonev
 */
public class TemplateSearchCriteria {

	private final String group;

	private final String purpose;

	private final Map<String, Serializable> filter;

	/**
	 * Constructs the criteria.
	 *
	 * @param group
	 *            the groupId (forType) of the template
	 * @param purpose
	 *            the purpose of the template (see {@link TemplatePurposes})
	 * @param filter
	 *            the filter map containing simple key-values reperesnting a template rule
	 */
	public TemplateSearchCriteria(String group, String purpose, Map<String, Serializable> filter) {
		this.group = group;
		this.purpose = purpose;
		this.filter = filter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((purpose == null) ? 0 : purpose.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TemplateSearchCriteria other = (TemplateSearchCriteria) obj;
		if (filter == null) {
			if (other.filter != null) {
				return false;
			}
		} else if (!filter.equals(other.filter)) {
			return false;
		}
		if (group == null) {
			if (other.group != null) {
				return false;
			}
		} else if (!group.equals(other.group)) {
			return false;
		}
		if (purpose == null) {
			if (other.purpose != null) {
				return false;
			}
		} else if (!purpose.equals(other.purpose)) {
			return false;
		}
		return true;
	}

	public String getGroup() {
		return group;
	}

	public String getPurpose() {
		return purpose;
	}

	public Map<String, Serializable> getFilter() {
		return filter;
	}
}
