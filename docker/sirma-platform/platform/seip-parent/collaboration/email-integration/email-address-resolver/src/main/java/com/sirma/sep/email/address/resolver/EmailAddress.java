package com.sirma.sep.email.address.resolver;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a database table with information about tenantId, instanceId and emailAddress. The table is
 * used to store unique email address for each instance.
 *
 * @author S.Djulgerova
 */
@PersistenceUnitBinding(PersistenceUnits.CORE)
@Entity
@Table(name = "emf_emailaddress")
@NamedQueries(value = {
		@NamedQuery(name = EmailAddress.QUERY_EMAIL_ADDRESS_BY_EMAIL_KEY, query = EmailAddress.QUERY_EMAIL_ADDRESS_BY_EMAIL),
		@NamedQuery(name = EmailAddress.QUERY_EMAIL_ADDRESS_OF_INSTANCE_KEY, query = EmailAddress.QUERY_EMAIL_ADDRESS_OF_INSTANCE),
		@NamedQuery(name = EmailAddress.DELETE_EMAIL_ADDRESS_BY_EMAIL_KEY, query = EmailAddress.DELETE_EMAIL_ADDRESS_BY_EMAIL),
		@NamedQuery(name = EmailAddress.QUERY_TENANT_ID_BY_MAIL_DOMAIN_KEY, query = EmailAddress.QUERY_TENANT_ID_BY_MAIL_DOMAIN),
		@NamedQuery(name = EmailAddress.QUERY_EMAIL_ADDRESS_BY_MAIL_TENANT_ID_KEY, query = EmailAddress.QUERY_EMAIL_ADDRESS_BY_MAIL_TENANT_ID) })
public class EmailAddress extends BaseEntity {

	private static final long serialVersionUID = 106890152010872170L;

	/** Query {@link EmailAddress} by given email address. Param: emailAddress */
	public static final String QUERY_EMAIL_ADDRESS_BY_EMAIL_KEY = "QUERY_EMAIL_ADDRESS_BY_EMAIL";
	static final String QUERY_EMAIL_ADDRESS_BY_EMAIL = "select ea from EmailAddress ea where ea.emailAddress=:emailAddress";

	/** Query {@link EmailAddress} by given instance id and tenant id. Params: instanceId and tenantId */
	public static final String QUERY_EMAIL_ADDRESS_OF_INSTANCE_KEY = "QUERY_EMAI_ADDRESS_OF_INSTANCE";
	static final String QUERY_EMAIL_ADDRESS_OF_INSTANCE = "select ea from EmailAddress ea where ea.instanceId=:instanceId and ea.tenantId=:tenantId";

	/** Delete {@link EmailAddress} by given email address. Param: emailAddress */
	public static final String DELETE_EMAIL_ADDRESS_BY_EMAIL_KEY = "DELETE_EMAI_ADDRESS_BY_EMAIL";
	static final String DELETE_EMAIL_ADDRESS_BY_EMAIL = "delete from EmailAddress where emailAddress=:emailAddress";

	public static final String QUERY_TENANT_ID_BY_MAIL_DOMAIN_KEY = "QUERY_TENANT_ID_BY_MAIL_DOMAIN";
	static final String QUERY_TENANT_ID_BY_MAIL_DOMAIN = "select distinct tenantId FROM EmailAddress where mailDomain=:mailDomain";

	public static final String QUERY_EMAIL_ADDRESS_BY_MAIL_TENANT_ID_KEY = "QUERY_EMAIL_ADDRESS_BY_MAIL_TENANT_ID";
	static final String QUERY_EMAIL_ADDRESS_BY_MAIL_TENANT_ID = "select ea from EmailAddress ea where ea.tenantId=:tenantId";

	@Column(name = "emailaddress", nullable = false)
	private String emailAddress; // NOSONAR

	@Column(name = "tenantid", nullable = false)
	private String tenantId;

	@Column(name = "instanceid", nullable = false)
	private String instanceId;

	@Column(name = "maildomain", nullable = false)
	private String mailDomain;

	/**
	 * Instantiates a new entity.
	 */
	public EmailAddress() {
		// default constructor
	}

	/**
	 * Instantiates a new entity.
	 *
	 * @param tenantId
	 *            tenant id
	 * @param instanceId
	 *            instance id
	 * @param emailAddress
	 *            generated email address
	 * @param mailDomain
	 *            domain where the email address belongs to
	 */
	public EmailAddress(String tenantId, String instanceId, String emailAddress, String mailDomain) {
		setTenantId(tenantId);
		setInstanceId(instanceId);
		setEmailAddress(emailAddress);
		setMailDomain(mailDomain);
	}

	/**
	 * Instantiates a new entity.
	 *
	 * @param id
	 *            entity id
	 * @param tenantId
	 *            tenant id
	 * @param instanceId
	 *            instance id
	 * @param emailAddress
	 *            generated email address
	 * @param mailDomain
	 *            domain where the email address belongs to
	 */
	public EmailAddress(Long id, String tenantId, String instanceId, String emailAddress, String mailDomain) {
		setId(id);
		setTenantId(tenantId);
		setInstanceId(instanceId);
		setEmailAddress(emailAddress);
		setMailDomain(mailDomain);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 36;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EmailAddress) {
			return super.equals(obj);
		}
		return false;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getMailDomain() {
		return mailDomain;
	}

	public void setMailDomain(String mailDomain) {
		this.mailDomain = mailDomain;
	}
}
