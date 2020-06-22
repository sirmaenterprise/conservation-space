package com.sirma.itt.seip.tenant.provision;

/**
 * Carries info needed for tenant in idp.
 *
 * @author smustafov
 */
public class IdpTenantInfo {

	private String tenantId;
	private String tenantDisplayName;
	private String tenantDescription;
	private String adminUsername;
	private String adminPassword;
	private String adminMail;
	private String adminFirstName;
	private String adminLastName;
	private String idpVersion;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantDisplayName() {
		return tenantDisplayName;
	}

	public void setTenantDisplayName(String tenantDisplayName) {
		this.tenantDisplayName = tenantDisplayName;
	}

	public String getTenantDescription() {
		return tenantDescription;
	}

	public void setTenantDescription(String tenantDescription) {
		this.tenantDescription = tenantDescription;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getAdminMail() {
		return adminMail;
	}

	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}

	public String getAdminFirstName() {
		return adminFirstName;
	}

	public void setAdminFirstName(String adminFirstName) {
		this.adminFirstName = adminFirstName;
	}

	public String getAdminLastName() {
		return adminLastName;
	}

	public void setAdminLastName(String adminLastName) {
		this.adminLastName = adminLastName;
	}

	public String getIdpVersion() {
		return idpVersion;
	}

	public void setIdpVersion(String idpVersion) {
		this.idpVersion = idpVersion;
	}

}
