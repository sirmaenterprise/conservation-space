package com.sirma.itt.seip.permissions.role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a role based permission entry. Could contain information about inherited or special
 * permissions.
 *
 * @author BBonev
 * @author bbanchev
 */
@Entity
@Table(name = "emf_resourcerole", indexes = @javax.persistence.Index(name = "idx_prr_trr", columnList = "targetid") )
@NamedQueries({
		@NamedQuery(name = ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_IDS_KEY, query = ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_IDS),
		@NamedQuery(name = ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_PATH_KEY, query = ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_PATH),
		@NamedQuery(name = ResourceRoleEntity.DELETE_RESOURCE_ROLES_BY_IDS_KEY, query = ResourceRoleEntity.DELETE_RESOURCE_ROLES_BY_IDS),
		@NamedQuery(name = ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY, query = ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_TARGET_ID) })
public class ResourceRoleEntity extends BaseEntity {

	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS_KEY. */
	public static final String QUERY_RESOURCE_ROLES_BY_IDS_KEY = "QUERY_RESOURCE_ROLES_BY_IDS";
	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS. */
	static final String QUERY_RESOURCE_ROLES_BY_IDS = "select p from ResourceRoleEntity p where p.id in (:ids)";

	/** The QUERY_RESOURCE_ROLES_BY_PATH key. Looks for permissions paths with like ''. */
	public static final String QUERY_RESOURCE_ROLES_BY_PATH_KEY = "QUERY_RESOURCE_ROLES_BY_PATH";
	/** The query QUERY_RESOURCE_ROLES_BY_PATH. */
	static final String QUERY_RESOURCE_ROLES_BY_PATH = "select distinct p.targetReference from ResourceRoleEntity p where p.path like :path";

	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS_KEY. */
	public static final String DELETE_RESOURCE_ROLES_BY_IDS_KEY = "DELETE_RESOURCE_ROLES_BY_IDS";
	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS. */
	static final String DELETE_RESOURCE_ROLES_BY_IDS = "delete from ResourceRoleEntity where id in (:ids)";

	/** The Constant QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY. */
	public static final String QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY = "QUERY_RESOURCE_ROLES_BY_TARGET_ID";
	/** The Constant QUERY_RESOURCE_ROLES_BY_TARGET_ID. */
	static final String QUERY_RESOURCE_ROLES_BY_TARGET_ID = "select e from ResourceRoleEntity e where targetReference=:sourceId";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4024290114080051359L;

	/** The role. */
	@Column(name = "role", length = 100, nullable = true)
	private String role;

	/** The target reference. */
	@Column(name = "targetid", length = 100, nullable = false)
	private String targetReference;

	/** The inherited target reference. */
	@Column(name = "inherited_targetid", length = 100, nullable = true)
	private String inheritedTargetReference;

	@Column(name = "authority_id", length = 100, nullable = true)
	private String authorityId;

	@Column(name = "path", length = 500, nullable = false)
	private String path;

	@Column(name = "systeminfo", length = 500, nullable = true)
	private String systeminfo;

	@Column(name = "inherit_from_library", nullable = true)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private Boolean inheritFromLibrary;

	@Column(name = "inherit_from_parent", nullable = true)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private Boolean inheritFromParent;

	@Override
	public String toString() {
		return "ResourceRoleEntity [role=" + role + ", targetReference=" + targetReference
				+ ", inheritedTargetReference=" + inheritedTargetReference + ", inheritFromLibrary="
				+ inheritFromLibrary + ", authorityId=" + authorityId + ", path=" + path + ", systeminfo=" + systeminfo
				+ "]";
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
	 * Getter method for resourceId.
	 *
	 * @return the resourceId
	 */
	public String getAuthorityId() {
		return authorityId;
	}

	/**
	 * Setter method for resourceId.
	 *
	 * @param authorityId
	 *            the authority Id to set
	 */
	public void setAuthorityId(String authorityId) {
		this.authorityId = authorityId;
	}

	/**
	 * Getter method for targetReference.
	 *
	 * @return the targetReference
	 */
	public String getTargetReference() {
		return targetReference;
	}

	/**
	 * Setter method for targetReference.
	 *
	 * @param targetReference
	 *            the targetReference to set
	 */
	public void setTargetReference(String targetReference) {
		this.targetReference = targetReference;
	}

	/**
	 * Gets the inherited target reference.
	 *
	 * @return the inherited target reference
	 */
	public String getInheritedTargetReference() {
		return inheritedTargetReference;
	}

	/**
	 * Sets the inherited target reference.
	 *
	 * @param inheritedTargetReference
	 *            the new inherited target reference
	 */
	public void setInheritedTargetReference(String inheritedTargetReference) {
		this.inheritedTargetReference = inheritedTargetReference;
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 *
	 * @param path
	 *            the new path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Getter method for systeminfo.
	 *
	 * @return the systeminfo
	 */
	public String getSysteminfo() {
		return systeminfo;
	}

	/**
	 * Setter method for systeminfo.
	 *
	 * @param systeminfo
	 *            the systeminfo to set
	 */
	public void setSysteminfo(String systeminfo) {
		this.systeminfo = systeminfo;
	}

	/**
	 * Getter method for inheritFromLibrary.
	 *
	 * @return the inheritFromLibrary
	 */
	public Boolean getInheritFromLibrary() {
		return inheritFromLibrary;
	}

	/**
	 * Setter method for inheritFromLibrary.
	 *
	 * @param inheritFromLibrary
	 *            the inheritFromLibrary to set
	 */
	public void setInheritFromLibrary(Boolean inheritFromLibrary) {
		this.inheritFromLibrary = inheritFromLibrary;
	}

	/**
	 * Getter method for inheritFromParent.
	 *
	 * @return the inheritFromParent
	 */
	public Boolean getInheritFromParent() {
		return inheritFromParent;
	}

	/**
	 * Setter method for inheritFromParent.
	 *
	 * @param inheritFromParent
	 *            the inheritFromParent to set
	 */
	public void setInheritFromParent(Boolean inheritFromParent) {
		this.inheritFromParent = inheritFromParent;
	}

}
