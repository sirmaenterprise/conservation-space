package com.sirma.itt.seip.tenant.step;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller;
import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller.InitConfiguration;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.adapters.remote.AlfrescoRESTClient;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.threads.ThreadSleeper;

/**
 * Tenant initialization in alfresco - initialize new tenant and site and optionally may apply definitions to it
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 5)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 10)
public class TenantAlfresco4Step extends AbstractTenantStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String DEFAULT_SITE_NAME = "seip";
	private static final String KEY_DOMAIN_ID = "tenantDomain";
	private static final String KEY_ADMIN_PASS = "tenantAdminPassword";
	private static final String SEIP_TENANT_CREATE_SERVICE = "/seip/tenant/create";
	private static final String SEIP_DELETE_SERVICE = "/case/instance/obsolete/delete";
	private static final String SEIP_SEARCH_SERVICE = "/cmf/search";
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.dms.alfresco4.deleteTenantOnRollback", defaultValue = "false",
			type = Boolean.class, system = true, label = "Enables/disables deletion of created tenant in Alfresco4 on"
			+ " tenant creation rollback.")
	private ConfigurationProperty<Boolean> deleteTenantOnRollback;
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
	private ThreadSleeper threadSleeper;

	@Override
	public String getIdentifier() {
		return "DMSInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		String siteId = extractSiteId(data, tenantId);
		URI addressForNewTenant = provideAddressForNewTenant(context.getTenantInfo());
		Collection<Configuration> configurations = transactionSupport
				.invokeInNewTx(
						() -> setupServerAddressConfigurations(context.getTenantInfo(), addressForNewTenant, siteId));
		if (configurations.isEmpty()) {
			throw new TenantCreationException("Could not add DMS configuration");
		}

		if (SecurityContext.isDefaultTenant(tenantId)) {
			return true;
		}
		try {
			if (SecurityConfiguration.WSO_IDP.equals(context.getIdpProvider())) {
				// create tenant using the system admin - required by Alfresco
				createTenant(addressForNewTenant, tenantId, context.getNewTenantAdminPassword());

				// now login with tenant admin
				securityContextManager.initializeTenantContext(tenantId);
				InitConfiguration configuration = new InitConfiguration();
				configuration.setSiteId(siteId);
				configuration.setFailOnMissing(false);

				tryToInitialize(configuration);
			} else {
				securityContextManager.initializeTenantContext(tenantId);
				disableAlfresco(tenantId);
			}
		} catch (Exception e) {
			throw new TenantCreationException("DMS tenant creation failed!", e);
		} finally {
			securityContextManager.endContextExecution();
		}
		data.completedSuccessfully();
		return true;
	}

	private void tryToInitialize(InitConfiguration configuration) throws Exception {
		for (int tries = 2; tries != 0; tries--) {
			try {
				initController.initialize(configuration);
				return;
			} catch (DMSClientException e) {
				LOGGER.debug("Failed to initialize Alfresco. The process will be retried after 5 sec."
						+ " Remaining retries: {}", tries);
				LOGGER.trace("", e);
				threadSleeper.sleepFor(5);
			}
		}

		// final try, don't catch the exception (if any) in order to propagate it
		initController.initialize(configuration);
	}

	private void disableAlfresco(String tenantId) {
		Configuration alfrescoStoreConfig = new Configuration(adaptersConfiguration.getAlfrescoStoreEnabled().getName(),
				Boolean.FALSE.toString(), tenantId);
		Configuration alfrescoViewStoreConfig = new Configuration(adaptersConfiguration.getAlfrescoViewStoreEnabled().getName(),
				Boolean.FALSE.toString(), tenantId);
		configurationManagement.addConfigurations(Arrays.asList(alfrescoStoreConfig, alfrescoViewStoreConfig));
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		if (data.isCompleted()) {
			try {
				securityContextManager.initializeTenantContext(tenantId);
				if (!context.shouldRollback() || deleteTenantOnRollback.get()) {
					deleteTenant(tenantId);
				}
			} catch (Exception e) {
				LOGGER.warn("DMS couldn't be cleared due to {}!", e.getMessage());
				LOGGER.trace("Error during DMS rollback!", e);
				return false;
			} finally {
				securityContextManager.endContextExecution();
			}
		}
		removeConfigurations(tenantId);
		return true;
	}

	private String extractSiteId(TenantStepData data, String tenantId) {
		Function<String, String> computeDmsContainerName = securityContextManager.executeAsTenant(tenantId).toWrapper()
				.function(rawConfigurationAccessor::getRawConfigurationValue);
		String configKey = adaptersConfiguration.getDmsContainerId().getName();

		Serializable siteName = data.getProperties().computeIfAbsent("siteId",
				key -> computeDmsContainerName.apply(configKey));
		return Objects.toString(siteName, DEFAULT_SITE_NAME);
	}

	private Collection<Configuration> setupServerAddressConfigurations(TenantInfo tenantInfo, URI address,
			String siteId) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			LOGGER.info("Using DMS-Alfresco4 server at {}", address);
			return configurationManagement
					.addConfigurations(createDmsConfigurations(tenantInfo.getTenantId(), address, siteId));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private URI provideAddressForNewTenant(TenantInfo tenantInfo) {
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
		return address;
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

	private String createTenant(URI addressForNewTenant, String tenantId, String adminPassword) {
		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, KEY_DOMAIN_ID, tenantId);
		JsonUtil.addToJson(request, KEY_ADMIN_PASS, adminPassword);
		String uri = addressForNewTenant + AlfrescoRESTClient.SERVICE_BASE_URI + SEIP_TENANT_CREATE_SERVICE;
		return JsonUtil.getStringValue(executeTenantRequest(request, uri, false), KEY_DOMAIN_ID);
	}

	/**
	 * Delete the tenant from alfresco. This doesn't actually delete the tenant. It just clears out
	 * all child nodes of the dms container.
	 */
	private void deleteTenant(String tenantId) {
		// Find the tenant's site id node ref. Needed when retrieving it's children.
		String nodeRef = getSiteIdNodeRef(tenantId);

		// Retrieve all subfolders from the tenant's site (definitions folders and documentLibrary).
		List<String> childNodeRefs = getChildNodeRefs(tenantId, nodeRef);

		// Clear all child nodes, effectively leaving the nodes empty. This will leave all folders
		// intact but clear out any old data from them like definitions and documents.
		childNodeRefs.forEach(this::clearNode);
	}

	private String clearNode(String nodeRef) {
		JSONObject deleteRequest = new JSONObject();
		JsonUtil.addToJson(deleteRequest, "parentNode", nodeRef);
		JsonUtil.addToJson(deleteRequest, "all", Boolean.TRUE);
		return JsonUtil.getStringValue(
				executeTenantRequest(deleteRequest, SEIP_DELETE_SERVICE, true),
				KEY_DOMAIN_ID);
	}

	private List<String> getChildNodeRefs(String tenantId, String nodeRef) {
		// The query that returns nodeRefs by their parent's nodeRef.
		String query = "PARENT:\"" + nodeRef + "\"";

		JSONObject childNodeRefsRequest = new JSONObject();
		JsonUtil.addToJson(childNodeRefsRequest, KEY_DOMAIN_ID, tenantId);
		JsonUtil.addToJson(childNodeRefsRequest, "query", query);
		JSONObject searchResponse = executeTenantRequest(childNodeRefsRequest, SEIP_SEARCH_SERVICE, true);

		// Iterate the response and retrieve all child nodeRefs.
		JSONObject data = JsonUtil.getJsonObject(searchResponse, "data");
		JSONArray items = JsonUtil.getJsonArray(data, "items");
		return JsonUtil.jsonArrayToList(items).stream()
				.map(item -> JsonUtil.getStringValue((JSONObject) item, "nodeRef")).collect(Collectors.toList());
	}

	private String getSiteIdNodeRef(String tenantId) {
		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, KEY_DOMAIN_ID, tenantId);
		JsonUtil.addToJson(request, "query", "@cm:name:'" + adaptersConfiguration.getDmsContainerId().get() + "'");
		JSONObject searchResponse = executeTenantRequest(request, SEIP_SEARCH_SERVICE, true);
		JSONObject data = JsonUtil.getJsonObject(searchResponse, "data");
		JSONObject item = JsonUtil.getFromArray(JsonUtil.getJsonArray(data, "items"), 0, JSONObject.class);
		return JsonUtil.getStringValue(item, "nodeRef");
	}

	private JSONObject executeTenantRequest(JSONObject request, String uri, boolean isRelativeUri) {
		try {
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String response = null;
			if (isRelativeUri) {
				response = restClient.request(uri, createMethod);
			} else {
				HttpMethod rawRequest = restClient.rawRequest(createMethod,
						new org.apache.commons.httpclient.URI(uri, false));
				response = AlfrescoRESTClient.getResponse(rawRequest);
			}

			if (response != null) {
				return new JSONObject(response);
			}
		} catch (DMSClientException e) {
			throw new TenantCreationException("Failure during tenant creation: ", e);
		} catch (Exception e) {
			throw new TenantCreationException(
					"Failure during request for tenant creation: " + AlfrescoErrorReader.parse(e), e);
		}
		throw new TenantCreationException("Tenant is not created!");
	}
}