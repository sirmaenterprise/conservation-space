package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.User;

/**
 * Colleague data holder. Wraps the resource and updates the compare field
 *
 * @author svelikov
 */
public class Colleague implements Comparable<Colleague>, JsonRepresentable, Identity {

	private String identifier;

	private String displayName;

	private String sortingField;

	private String role;

	private String jobtitle;

	private String avatarPath;

	private final Resource resource;

	private String type;

	/**
	 * Wraps a resource using the parameter
	 *
	 * @param resource
	 *            is the resource to wrap
	 */
	public Colleague(Resource resource) {
		this.resource = resource;
		setIdentifier(resource.getName());
		sortingField = "";
		if (resource instanceof User) {
			sortingField = (String) resource.getProperties().get(ResourceProperties.LAST_NAME);
		}
		if (StringUtils.isBlank(sortingField)) {
			sortingField = resource.getName();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
		result = prime * result + (resource == null ? 0 : resource.hashCode());
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
		if (!(obj instanceof Colleague)) {
			return false;
		}
		Colleague other = (Colleague) obj;
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject member = new JSONObject();
		Map<String, Serializable> props = resource.getProperties();
		JsonUtil.addToJson(member, ResourceProperties.USER_ID, resource.getName());
		JsonUtil.addToJson(member, DefaultProperties.NAME, resource.getDisplayName());
		JsonUtil.addToJson(member, ResourceProperties.ROLE, role);
		JsonUtil.copyToJson(member, ResourceProperties.JOB_TITLE, props);
		JsonUtil.copyToJson(member, ResourceProperties.AVATAR, props);
		JsonUtil.addToJson(member, DefaultProperties.TYPE, resource.getType());
		return member;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// no initialization from JSON for now
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}