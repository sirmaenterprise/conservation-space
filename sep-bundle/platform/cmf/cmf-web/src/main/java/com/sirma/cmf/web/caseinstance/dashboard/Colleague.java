package com.sirma.cmf.web.caseinstance.dashboard;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.User;

/**
 * Colleague data holder. Wrapps the resource and updates the compare field
 *
 * @author svelikov
 */
public class Colleague implements Comparable<Colleague> {

	private String displayName;
	// default is empty
	private String sortingField = "";
	/** The role. */
	private String role;
	/** The jobtitle. */
	private String jobtitle;
	/** The avatar path. */
	private String avatarPath;
	/** the colleague resource. */
	private final Resource resource;

	/**
	 * Wraps a resource using the parameter
	 *
	 * @param resource
	 *            is the resource to wrap
	 */
	public Colleague(Resource resource) {
		this.resource = resource;
		if (resource instanceof User) {
			sortingField = (String) resource.getProperties().get(ResourceProperties.LAST_NAME);
		}
		if (StringUtils.isBlank(sortingField)) {
			sortingField = resource.getIdentifier();
		}
	}

	/**
	 * Getter method for role.
	 *
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Setter method for role.
	 *
	 * @param role
	 *            the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Getter method for jobtitle.
	 *
	 * @return the jobtitle
	 */
	public String getJobtitle() {
		return jobtitle;
	}

	/**
	 * Setter method for jobtitle.
	 *
	 * @param jobtitle
	 *            the jobtitle to set
	 */
	public void setJobtitle(String jobtitle) {
		this.jobtitle = jobtitle;
	}

	/**
	 * Getter method for avatar.
	 *
	 * @return avatar path
	 */
	public String getAvatarPath() {
		return avatarPath;
	}

	/**
	 * Setter method for avatar.
	 *
	 * @param avatarPath
	 *            avathar path
	 */
	public void setAvatarPath(String avatarPath) {
		this.avatarPath = avatarPath;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		Colleague other = (Colleague) obj;
		if (resource == null) {
			if (other.resource != null) {
				return false;
			}
		} else if (!resource.equals(other.resource)) {
			return false;
		}
		return true;
	}

	/**
	 * Getter method for displayName.
	 *
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Setter method for displayName.
	 *
	 * @param displayName
	 *            the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public int compareTo(Colleague o) {
		return sortingField.compareTo(o.sortingField);
	}

}