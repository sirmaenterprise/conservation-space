package com.sirma.itt.cmf.integration.webscript;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.SEIPTenantIntegration;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API - create tenant
 * https://forums.alfresco.com/forum/developer-discussions/alfresco-api/creating
 * -tenants-rest-api-01102013-0111
 *
 * @author janv
 * @author bbanchev
 */
public class TenantIntegrationScript extends BaseAlfrescoScript {

	/** The Constant TENANT_DOMAIN. */
	protected static final String TENANT_DOMAIN = "tenantDomain";

	/** The Constant TENANT_ADMIN_PASSWORD. */
	protected static final String TENANT_ADMIN_PASSWORD = "tenantAdminPassword";

	/** The Constant TENANT_CONTENT_STORE_ROOT. */
	protected static final String TENANT_CONTENT_STORE_ROOT = "tenantContentStoreRoot";

	/** The tenant admin service. */
	protected TenantAdminService tenantAdminService;

	private String contentRoot;

	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		String servicePath = req.getServicePath();
		Map<String, Object> model = new HashMap<String, Object>(1);
		if (!SEIPTenantIntegration.getTenantId().isEmpty()) {
			throw new WebScriptException(401, "Only standard administrator is allowed to perform tenant operations!");
		}
		if (servicePath.contains("/seip/tenant/create")) {
			createRequest(req, model);
		} else if (servicePath.contains("/seip/tenant/delete")) {
			deleteRequest(req, model);
		} else if (servicePath.contains("/seip/tenant/list")) {
			List<Tenant> allTenants = tenantAdminService.getAllTenants();
			model.put("tenants", allTenants);
		}
		return model;
	}

	/**
	 * Creates new tenant and optionally synch ldap. If tenant already exists it
	 * is simply returned
	 * 
	 * @param req
	 *            is the original request
	 * @param model
	 *            is the populated model
	 */
	private void createRequest(final WebScriptRequest req, final Map<String, Object> model) {

		try {
			final JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
			RetryingTransactionHelper retryingTransactionHelper = newTranscationHelper();
			final Tenant tenant = retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Tenant>() {
				@Override
				public Tenant execute() throws Throwable {
					return createTenantInternal(json, model);
				}
			}, false, true);
			model.put("tenant", tenant);
			if (json.has("ldapSkipReload")) {
				if (Boolean.parseBoolean(json.getString("ldapSkipReload"))) {
					return;
				}
			}
			retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {

				@Override
				public Void execute() throws Throwable {
					cmfService.reloadLDAP(tenant.getTenantDomain());
					return null;
				}
			}, false, true);
		} catch (IOException iox) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
		} catch (JSONException je) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
		} catch (Exception e) {
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Could not process tenant creation!", e);
		}
	}

	private RetryingTransactionHelper newTranscationHelper() {
		RetryingTransactionHelper retryingTransactionHelper = new RetryingTransactionHelper();
		retryingTransactionHelper.setMaxRetries(1);
		retryingTransactionHelper.setReadOnly(false);
		retryingTransactionHelper.setTransactionService(serviceRegistry.getTransactionService());
		return retryingTransactionHelper;
	}

	private Tenant createTenantInternal(JSONObject json, Map<String, Object> model) throws JSONException, IOException {
		String tenantDomain = null;
		String tenantAdminPassword = null;
		String contentStoreRoot = null;

		if (!json.has(TENANT_DOMAIN)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'tenantDomain' parameter");
		}
		tenantDomain = json.getString(TENANT_DOMAIN);
		if (!json.has(TENANT_ADMIN_PASSWORD)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"Could not find required 'tenantAdminPassword' parameter");
		}
		tenantAdminPassword = json.getString(TENANT_ADMIN_PASSWORD);
		// with correct regex for domain name folder name should not be
		// problem
		contentStoreRoot = tenantDomain;
		String pathSeparator = "/";
		if (contentRoot.endsWith(pathSeparator) || contentStoreRoot.startsWith(pathSeparator)) {
			contentStoreRoot = contentRoot + contentStoreRoot;
		} else {
			contentStoreRoot = contentRoot + pathSeparator + contentStoreRoot;
		}
		if (!tenantAdminService.existsTenant(tenantDomain)) {
			tenantAdminService.createTenant(tenantDomain, tenantAdminPassword.toCharArray(), contentStoreRoot);
		}
		return tenantAdminService.getTenant(tenantDomain);
	}

	/**
	 * Delete or deactivate request for tenant. If operation is forced than
	 * deletion of tenant is executed.
	 * 
	 * @param req
	 *            is the original request
	 * @param model
	 *            is the populated model
	 */
	private void deleteRequest(WebScriptRequest req, Map<String, Object> model) {
		String tenantDomain = null;
		Boolean force = Boolean.FALSE;
		try {
			JSONObject request = new JSONObject(new JSONTokener(req.getContent().getContent()));
			if (!request.has(TENANT_DOMAIN)) {
				throw new WebScriptException(Status.STATUS_BAD_REQUEST,
						"Could not find required 'tenantDomain' parameter");
			}
			tenantDomain = request.getString(TENANT_DOMAIN);
			if (request.has("force")) {
				force = Boolean.valueOf(req.getParameter("force"));
			}
			debug("Delete tenant '", tenantDomain, "': ", force.booleanValue());
			Tenant tenant = null;
			if (tenantAdminService.existsTenant(tenantDomain)) {
				tenant = tenantAdminService.getTenant(tenantDomain);
				if (force.booleanValue()) {
					tenantAdminService.deleteTenant(tenantDomain);
				} else {
					tenantAdminService.disableTenant(tenantDomain);
				}
			}
			model.put("tenant", tenant);
		} catch (IOException iox) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
		} catch (JSONException je) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
		} catch (Exception e) {
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Could not process tenant deletion!", e);
		}

	}

	/**
	 * Sets the tenant admin service.
	 *
	 * @param tenantAdminService
	 *            the new tenant admin service
	 */
	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	/**
	 * Getter method for contentRoot.
	 *
	 * @return the contentRoot
	 */
	public String getContentRoot() {
		return contentRoot;
	}

	/**
	 * Setter method for contentRoot.
	 *
	 * @param contentRoot
	 *            the contentRoot to set
	 */
	public void setContentRoot(String contentRoot) {
		this.contentRoot = contentRoot;
	}

}
