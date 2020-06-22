package com.sirma.itt.seip.tenant.wizard;

import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantInitializationContext is holder of context data during step execution.
 *
 * @author bbanchev
 */
public class TenantInitializationContext {

	/** The tenant info. */
	private TenantInfo tenantInfo;

	private Mode mode;

	/** The admin password. */
	private String adminUser;
	/** The new tenant admin password. */
	private String newTenantAdminPassword;

	private String idpProvider;

	private Map<String, Object> provisions = new HashMap<>();

	/**
	 * Getter method for tenantInfo.
	 *
	 * @return the tenantInfo
	 */
	public TenantInfo getTenantInfo() {
		return tenantInfo;
	}

	/**
	 * Setter method for tenantInfo.
	 *
	 * @param tenantInfo
	 *            the tenantInfo to set
	 */
	public void setTenantInfo(TenantInfo tenantInfo) {
		if (this.tenantInfo != null) {
			throw new TenantCreationException("Already initialized tenant information: " + this.tenantInfo);
		}
		this.tenantInfo = tenantInfo;
	}

	/**
	 * Getter method for adminUser.
	 *
	 * @return the adminUser
	 */
	public String getAdminUser() {
		return adminUser;
	}

	/**
	 * Setter method for adminUser.
	 *
	 * @param adminUser
	 *            the adminUser to set
	 */
	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}

	/**
	 * Gets the new tenant admin password.
	 *
	 * @return the newTenantAdminPassword
	 */
	public String getNewTenantAdminPassword() {
		return newTenantAdminPassword;
	}

	/**
	 * Sets the new tenant admin password.
	 *
	 * @param newTenantAdminPassword
	 *            the newTenantAdminPassword to set
	 */
	public void setNewTenantAdminPassword(String newTenantAdminPassword) {
		this.newTenantAdminPassword = newTenantAdminPassword;
	}

	/**
	 * Adds the provision context.
	 *
	 * @param stepId
	 *            the step id
	 * @param data
	 *            the data
	 */
	public void addProvisionContext(String stepId, Object data) {
		provisions.put(stepId, data);
	}

	/**
	 * Gets the provision context stored under the specified key.
	 *
	 * @param <T>
	 *            the generic type
	 * @param stepId
	 *            the step id
	 * @return the provision context
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProvisionContext(String stepId) {
		return (T) provisions.get(stepId);
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String getIdpProvider() {
		return idpProvider;
	}

	public void setIdpProvider(String idpProvider) {
		this.idpProvider = idpProvider;
	}

	/**
	 * Defines current tenant mode, whether its creating new tenant, updating existing one or deleting.
	 */
	public enum Mode {
		CREATE, UPDATE, DELETE
	}

}
