/**
 * 
 */
package com.sirma.itt.emf.cls.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.hibernate.annotations.Index;

import com.sirma.itt.emf.entity.BaseEntity;

/**
 * Entity POJO representing a tenant. By extending {@link BaseEntity} the ID is
 * of type Long.<br>
 * <b>NOTE</b>: The DB ID is not serialized to JSON.<br>
 * <b>NOTE</b>: Attributes with null values are not serialized to JSON.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
@Entity
@Table(name = "CLS_TENANT")
@JsonIgnoreProperties(value = { "id" })
@JsonSerialize(include = Inclusion.NON_NULL)
public class Tenant extends BaseEntity {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = 8056069932790542112L;

	/** The tenant's id. */
	@Column(length = 255, name = "TENANT_ID")
	@Index(name = "tenantId")
	private String tenantId;

	/** The tenant's name. */
	@Column(length = 255, name = "NAME")
	@Index(name = "tenantName")
	private String name;

	/** The tenant's contact. */
	@Column(length = 255, name = "CONTACT")
	private String contact;

	/**
	 * Getter method for tenant's ID.
	 * 
	 * @return the tenant's ID
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Setter method for tenant's ID.
	 * 
	 * @param tenantId
	 *            the tenant's ID to set
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Gets the tenant's name.
	 * 
	 * @return the tenant's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the tenant's name.
	 * 
	 * @param name
	 *            the new tenant's name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the tenant's contact.
	 * 
	 * @return the tenant's contact
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * Sets the tenant's contact.
	 * 
	 * @param contact
	 *            the new tenant's contact
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

}
