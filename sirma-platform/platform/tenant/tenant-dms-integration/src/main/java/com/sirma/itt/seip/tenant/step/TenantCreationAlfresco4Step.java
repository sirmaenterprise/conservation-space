package com.sirma.itt.seip.tenant.step;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller;
import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller.DefinitionType;
import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller.InitConfiguration;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.adapters.remote.AlfrescoRESTClient;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tenant initialization in alfresco - initialize new tenant and site and optionally may apply definitions to it
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 5)
public class TenantCreationAlfresco4Step extends AbstractTenantCreationStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String DEFAULT_SITE_NAME = "seip";
	private static final String KEY_DOMAIN_ID = "tenantDomain";
	private static final String KEY_ADMIN_PASS = "tenantAdminPassword";
	private static final String SEIP_TENANT_CREATE_SERVICE = "/seip/tenant/create";
	private static final String SEIP_TENANT_DELETE_SERVICE = "/seip/tenant/delete";

	@Inject
	private RESTClient restClient;
	@Inject
	private InitAlfresco4Controller initController;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private AdaptersConfiguration adaptersConfiguration;
	@Inject
	private RawConfigurationAccessor rawConfigurationAccessor;
	@Inject
	@Named("DMSAlfresco4")
	protected SubsystemTenantAddressProvider addressProvider;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private TempFileProvider tempFileProvider;

	@Override
	public String getIdentifier() {
		return "DMSInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		String siteId = extractSiteId(data, tenantId);
		Collection<Configuration> configurations = transactionSupport
				.invokeInNewTx(() -> setupServerAddressConfigurations(context.getTenantInfo(), siteId));
		if (configurations.isEmpty()) {
			throw new TenantCreationException("Could not add DMS configuration");
		}

		if (SecurityContext.isDefaultTenant(tenantId)) {
			return true;
		}
		try {
			// now login with tenant admin
			securityContextManager.initializeTenantContext(tenantId);
			// create tenant using the system admin - required by Alfresco
			createTenant(tenantId, context.getNewTenantAdminPassword());
			InitConfiguration configuration = new InitConfiguration();
			configuration.setSiteId(siteId);
			String definitionsPath = provideDefinitions(data, context.getTenantInfo().getTenantId());
			configuration.setDefinitionsLocation(definitionsPath);
			if (configuration.getDefinitionsLocation() != null) {
				configuration.addDefinitionType(DefinitionType.values());
			}
			configuration.setFailOnMissing(false);
			initController.initialize(configuration, progreess ->
				{
					// add progress handling
				});
		} catch (Exception e) {
			throw new TenantCreationException("DMS tenant creation failed!", e);
		} finally {
			securityContextManager.endContextExecution();
		}
		data.completedSuccessfully();
		return true;
	}

	@Override
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		removeConfigurations(tenantId);
		if (data.isCompleted()) {
			try {
				securityContextManager.initializeTenantContext(tenantId);
				deleteTenant(context.getTenantInfo().getTenantId());
			} catch (Exception e) {
				LOGGER.error("Error during DMS rollback!", e);
			} finally {
				securityContextManager.endContextExecution();
			}
		}
		return false;
	}

	private String provideDefinitions(TenantStepData data, String tenantId) throws IOException {
		if (CollectionUtils.isNotEmpty(data.getModels())) {
			File tempDir = tempFileProvider
					.createTempDir(tenantId + "_definitions_" + Long.toString(System.currentTimeMillis()));
			List<File> models = data.getModels();
			for (File model : models) {
				FileUtils.copyDirectory(model, tempDir);
			}
			return tempDir.getAbsolutePath();
		}
		return null;
	}

	private String extractSiteId(TenantStepData data, String tenantId) {
		Function<String, String> computeDmsContainerName = securityContextManager.executeAsTenant(tenantId).toWrapper()
				.function(rawConfigurationAccessor::getRawConfigurationValue);
		String configKey = adaptersConfiguration.getDmsContainerId().getName();

		Serializable siteName = data.getProperties().computeIfAbsent("siteId",
				key -> computeDmsContainerName.apply(configKey));
		return Objects.toString(siteName, DEFAULT_SITE_NAME);
	}

	private User getSystemUser() {
		return securityContextManager.getSuperAdminUser();

	}

	static String createUserPasswordForTenantId(TenantInfo tenantInfo) {
		String uuid = UUID.randomUUID().toString();
		return tenantInfo.getTenantId() + "-" + uuid.substring(0, uuid.indexOf('-'));
	}

	private Collection<Configuration> setupServerAddressConfigurations(TenantInfo tenantInfo, String siteId) {
		URI address = null;
		if (SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			address = readAddressConfiguration();
		}
		if (address == null) {
			address = addressProvider.provideAddressForNewTenant();
		}
		if (address == null) {
			throw new TenantCreationException(
					"No valid address to create new DMS Alfresco4 tenant for " + tenantInfo.getTenantId());
		}
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			LOGGER.info("Using DMS-Alfresco4 server at {}", address);
			return configurationManagement
					.addConfigurations(createDmsConfigurations(tenantInfo.getTenantId(), address, siteId));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private URI readAddressConfiguration() {
		String protocol = rawConfigurationAccessor
				.getRawConfigurationValue(adaptersConfiguration.getDmsProtocolConfiguration());
		String host = rawConfigurationAccessor
				.getRawConfigurationValue(adaptersConfiguration.getDmsHostConfiguration());
		String port = rawConfigurationAccessor
				.getRawConfigurationValue(adaptersConfiguration.getDmsPortConfiguration());
		Objects.requireNonNull(protocol, "DMS protocol for default tenant is not configured!");
		Objects.requireNonNull(host, "DMS host for default tenant is not configured!");
		Objects.requireNonNull(port, "DMS port for default tenant is not configured!");
		return URI.create(protocol + "://" + host + ":" + port);
	}

	private List<Configuration> createDmsConfigurations(String info, URI uri, String siteId) {
		return Arrays.asList(new Configuration(adaptersConfiguration.getDmsContainerId().getName(), siteId, info),
				new Configuration(adaptersConfiguration.getDmsProtocolConfiguration(), uri.getScheme(), info),
				new Configuration(adaptersConfiguration.getDmsHostConfiguration(), uri.getHost(), info),
				new Configuration(adaptersConfiguration.getDmsPortConfiguration(), String.valueOf(uri.getPort()),
						info));
	}

	private void removeConfigurations(String tenantId) {
		securityContextManager.initializeTenantContext(tenantId);
		try {
			configurationManagement.removeConfiguration(adaptersConfiguration.getDmsProtocolConfiguration());
			configurationManagement.removeConfiguration(adaptersConfiguration.getDmsHostConfiguration());
			configurationManagement.removeConfiguration(adaptersConfiguration.getDmsPortConfiguration());
			configurationManagement.removeConfiguration(adaptersConfiguration.getDmsContainerId().getName());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Creates the tenant.
	 *
	 * @param systemAdmin
	 *            the request user
	 * @param tenantId
	 *            the tenant id
	 * @param adminPassword
	 *            the admin password
	 * @return the string
	 */
	public String createTenant(String tenantId, String adminPassword) {
		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, KEY_DOMAIN_ID, tenantId);
		JsonUtil.addToJson(request, KEY_ADMIN_PASS, adminPassword);
		return executeTenantRequest(request, SEIP_TENANT_CREATE_SERVICE);
	}

	/**
	 * Delete tenant.
	 *
	 * @param systemAdmin
	 *            the request user
	 * @param tenantId
	 *            the tenant id
	 * @return the string
	 */
	public String deleteTenant(String tenantId) {
		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, KEY_DOMAIN_ID, tenantId);
		return executeTenantRequest(request, SEIP_TENANT_DELETE_SERVICE + "?force=true");
	}

	/**
	 * Execute tenant request.
	 *
	 * @param request
	 *            the request
	 * @param uri
	 *            the uri
	 * @return the string
	 */
	private String executeTenantRequest(JSONObject request, String uri) {
		String tenantId = null;
		try {
			tenantId = request.getString(KEY_DOMAIN_ID);

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String ticket = StringUtils.trimToNull(getSystemUser().getTicket());
			if (ticket != null) {
				createMethod.addRequestHeader(AlfrescoRESTClient.SAML_TOKEN, ticket);
			}
			String restResult = restClient.request(uri, createMethod);
			if (restResult != null) {
				JSONObject result = new JSONObject(restResult);
				return result.getString(KEY_DOMAIN_ID);
			}
		} catch (DMSClientException e) {
			throw new TenantCreationException("Failure during tenant creation: " + tenantId, e);
		} catch (Exception e) {
			throw new TenantCreationException(
					"Failure during request for tenant creation: '" + tenantId + "': " + AlfrescoErrorReader.parse(e),
					e);
		}
		throw new TenantCreationException("Tenant '" + tenantId + "' is not created!");
	}

}
