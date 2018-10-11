package com.sirma.itt.seip.tenant.audit;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.audit.step.AuditSolrSubsystemAddressProvider;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Provisioning of audit solr for tenant. Creates two new cores and configure them accordingly. The first one will be
 * the normal core for the old audit log that contains all fields (excluding the ones needed for the relations in the
 * new one) indexed but not stored. The second one will index and store the new fields and will have no dataimport
 * handler configured. Data in the second core shouldn't be imported automatically, it should instead be persisted
 * through the new solr service.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class AuditSolrProvisioning {
	public static final String RECENT_ACTIVITIES_SUFFIX = "_recentActivities";
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditSolrProvisioning.class);

	@Inject
	private AuditConfiguration auditConfiguration;

	@Inject
	private AuditSolrSubsystemAddressProvider auditSolrAddressProvider;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	protected ConfigurationManagement configurationManagement;

	@Inject
	protected SecurityContextManager securityContextManager;

	/**
	 * Provision the model.
	 *
	 * @param model
	 *            the model
	 * @param tenantInfo
	 *            the tenant info
	 * @param relationalContext
	 *            the relational context
	 * @throws RollbackedException
	 *             on provision error
	 */
	public void provisionAuditModel(Map<String, Serializable> model, TenantInfo tenantInfo,
			TenantRelationalContext relationalContext) throws RollbackedException {
		try {
			URI solrAuditAddress = auditSolrAddressProvider
					.provideAddressForNewTenant(Objects.toString(model.get("address"), null));
			// now run in context
			securityContextManager.initializeTenantContext(tenantInfo.getTenantId());

			TenantRelationalContext currentContext = new TenantRelationalContext();
			currentContext.setServerAddress(solrAuditAddress);
			// reuse same name
			currentContext.setDatabaseName(relationalContext.getDatabaseName());

			// init the correct context values before server creation
			Collection<Configuration> addedConfigurations = transactionSupport
					.invokeInNewTx(() -> insertConfigurations(currentContext, tenantInfo));
			if (addedConfigurations.isEmpty()) {
				throw new RollbackedException("Could not add Audit solr configuration");
			}

			provisionAuditModelInternal(tenantInfo, relationalContext);
		} catch (RollbackedException e) {
			throw e;
		} catch (Exception e) {
			throw new RollbackedException(e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Provision the recent activities model.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @throws RollbackedException
	 *             on provision error
	 */
	public void provisionRecentActivitiesModel(TenantInfo tenantInfo) throws RollbackedException {
		try {
			securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
			// init the correct context values before server creation
			transactionSupport.invokeInNewTx(() -> insertRecentActivitiesConfigurations(tenantInfo));
			provisionRecentActivitiesModelInternal(tenantInfo);
		} catch (RollbackedException e) {
			throw e;
		} catch (Exception e) {
			throw new RollbackedException(e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Try to access the core and take note of the status code. If a status code of 404 is returned then the core
	 * doesn't exists. Otherwise the core has already been created.
	 *
	 * @param tenantName
	 *            the tenant name created from the tenand id with it's '.' replaced with '_'
	 * @param tenantInfo
	 *            the info of the current tenant
	 * @return true if the core has already been created, false otherwise
	 * @throws RollbackedException
	 *             when producing the http client
	 */
	public boolean coreExists(String tenantName, TenantInfo tenantInfo) throws RollbackedException {
		try (CloseableHttpClient client = produceHttpClient()) {
			securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
			HttpGet request = new HttpGet(auditConfiguration.getSolrAddress().get() + "/" + tenantName + "/select");
			try (CloseableHttpResponse execute = client.execute(request)) {
				if (execute.getStatusLine().getStatusCode() == Status.NOT_FOUND.getStatusCode()) {
					return false;
				}
			}
			return true;
		} catch (IOException e) {
			throw new RollbackedException(e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Unload the solr core.
	 *
	 * @param coreName
	 *            name of the core
	 * @throws RollbackedException
	 *             when producing the http client
	 */
	public void unloadCore(String coreName) throws RollbackedException {
		solrAdminRequest("UNLOAD", coreName, produceHttpClient());
	}

	private void provisionAuditModelInternal(TenantInfo tenantInfo, TenantRelationalContext relationalContext)
			throws RollbackedException {
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			try (CloseableHttpClient produceHttpClient = produceHttpClient()) {
				LOGGER.debug("Creating SOLR core {}", relationalContext.getDatabaseName());
				// Create the normal solr core for the old audit log.
				uploadSolrConfigSet(relationalContext, produceHttpClient);
				solrAdminRequest("CREATE", relationalContext.getDatabaseName(), produceHttpClient);
			} catch (Exception e) {
				throw new RollbackedException("Failure during audit solr provisioning!", e);
			}
		}
	}

	private void provisionRecentActivitiesModelInternal(TenantInfo tenant) throws RollbackedException {
		String coreName = getRecentActivitiesCoreName(tenant);
		try (CloseableHttpClient produceHttpClient = produceHttpClient()) {
			LOGGER.debug("Creating SOLR core {}", coreName);
			// Create the new solr core for the ui2 audit log.
			uploadSolrSchema(coreName, produceHttpClient);
			solrAdminRequest("CREATE", coreName, produceHttpClient);

		} catch (Exception e) {
			throw new RollbackedException("Failure during audit solr provisioning!", e);
		}
	}

	private void solrAdminRequest(String operation, String coreName, CloseableHttpClient produceHttpClient)
			throws RollbackedException {
		if ("CREATE".equals(operation)) {
			HttpGet request = new HttpGet(auditConfiguration.getSolrAdminAddress().get() + "/cores?action=CREATE&name="
					+ coreName + "&configSet=" + coreName);

			executeRequest(produceHttpClient, request, "Failed to create solr core! ");
		} else if ("UNLOAD".equals(operation)) {
			HttpGet request = new HttpGet(auditConfiguration.getSolrAdminAddress().get()
					+ "/cores?action=UNLOAD&deleteInstanceDir=true&core=" + coreName);

			executeRequest(produceHttpClient, request, "Failed to unload solr core! ");
		}
	}

	private static void executeRequest(CloseableHttpClient httpClient, HttpUriRequest request, String errorMsg)
			throws RollbackedException {
		LOGGER.debug("Executing HTTP request: {}:{}", request.getMethod(), request.getURI());
		try (CloseableHttpResponse execute = httpClient.execute(request)) {
			if (execute.getStatusLine().getStatusCode() != Status.OK.getStatusCode()) {
				throw new RollbackedException(errorMsg + " Code:" + execute.getStatusLine().getStatusCode()
						+ ", error: " + execute.getStatusLine().getReasonPhrase());
			}
		} catch (IOException e) {
			LOGGER.warn("Could not execute request to solr due to {}", e.getMessage());
			LOGGER.trace("Could not execute request to solr due to {}", e.getMessage(), e);
			throw new RollbackedException(errorMsg + " , IOException raised: " + e.getMessage());
		}
	}

	private void uploadSolrConfigSet(TenantRelationalContext relationalContext, CloseableHttpClient produceHttpClient)
			throws IOException, RollbackedException {
		String configRaw = ResourceLoadUtil.loadResources(getClass(), "data-config.xml").iterator().next();

		configRaw = configRaw.replace("{dbAddress}", relationalContext.getServerAddress().toString());
		configRaw = configRaw.replace("{dbName}", relationalContext.getDatabaseName());
		configRaw = configRaw.replace("{dbUser}", relationalContext.getAccessUser());
		configRaw = configRaw.replace("{dbPassword}", relationalContext.getAccessUserPassword());
		uploadConfig(relationalContext.getDatabaseName(), configRaw, "data-config.xml", produceHttpClient);
	}

	private void uploadSolrSchema(String dbName, CloseableHttpClient produceHttpClient)
			throws RollbackedException, IOException {
		String configRaw = ResourceLoadUtil.loadResources(getClass(), "schema.xml").iterator().next();
		uploadConfig(dbName, configRaw, "schema.xml", produceHttpClient);
	}

	private void uploadConfig(String dbName, String resource, String resourceName,
			CloseableHttpClient produceHttpClient) throws RollbackedException, IOException {
		HttpPost request = new HttpPost(auditConfiguration.getSolrExternalAdminAddress().get() + "/config/upload");
		LOGGER.debug("Upload audit core config for '" + dbName + "' on server: " + request.getURI());
		MultipartEntityBuilder body = MultipartEntityBuilder.create();

		body.addTextBody("configname", dbName);

		body.addBinaryBody("conf|" + resourceName, resource.getBytes(StandardCharsets.UTF_8));
		request.setEntity(body.build());

		executeRequest(produceHttpClient, request, "Failed to upload solr audit configuration! ");
	}

	private static CloseableHttpClient produceHttpClient() throws RollbackedException {
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
			// disabled automatic retries as for some reason sends multiple requests for recent activities core creation
			// that cause tenant creation failure. Issue: CMF-24974
			return HttpClients.custom().disableAutomaticRetries().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			throw new RollbackedException("Unable to create a http client!", e);
		}
	}

	private Collection<Configuration> insertConfigurations(TenantRelationalContext context, TenantInfo tenantInfo) {
		URI address = context.getServerAddress();
		String dbName = context.getDatabaseName();

		Configuration protocol = new Configuration(auditConfiguration.getSolrProtocolConfigurationName(),
				address.getScheme(), tenantInfo.getTenantId());
		Configuration host = new Configuration(auditConfiguration.getSolrHostConfigurationName(), address.getHost(),
				tenantInfo.getTenantId());
		Configuration port = new Configuration(auditConfiguration.getSolrPortConfigurationName(),
				Integer.toString(address.getPort()), tenantInfo.getTenantId());
		Configuration auditCoreName = new Configuration(auditConfiguration.getSolrCoreConfigurationName(), dbName,
				tenantInfo.getTenantId());

		return configurationManagement.addConfigurations(Arrays.asList(protocol, host, port, auditCoreName));
	}

	private void insertRecentActivitiesConfigurations(TenantInfo tenantInfo) {
		Configuration recentActivitiesCoreName = new Configuration(
				auditConfiguration.getRecentActivitiesSolrCoreConfigurationName(),
				getRecentActivitiesCoreName(tenantInfo), tenantInfo.getTenantId());
		if (configurationManagement.addConfigurations(Collections.singletonList(recentActivitiesCoreName)).isEmpty()){
			LOGGER.warn("Recent activities configuration already inserted");
		}
	}

	/**
	 * Rollback audit core creation.
	 *
	 * @param relationalContext
	 *            the relational context
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void rollbackAuditCoreCreation(TenantRelationalContext relationalContext, TenantInfo tenantInfo) {
		rollbackCoreCreation(relationalContext.getDatabaseName(), tenantInfo);
	}

	/**
	 * Rollback recent activities core creation.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void rollbackRecentActivitiesCoreCreation(TenantInfo tenantInfo) {
		String coreName = getRecentActivitiesCoreName(tenantInfo);
		rollbackCoreCreation(coreName, tenantInfo);

	}

	private void rollbackCoreCreation(String coreName, TenantInfo tenantInfo) {
		// rollback in the context
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
			try {
				solrAdminRequest("UNLOAD", coreName, produceHttpClient());
			} catch (Exception e) {
				LOGGER.warn("Could not rollback solr creation for tenant {} due to error {}", tenantInfo.getTenantId(), e.getMessage());
				LOGGER.trace("Could not rollback solr creation for tenant {} due to error", tenantInfo.getTenantId(), e);
			} finally {
				securityContextManager.endContextExecution();
			}
		}

		try {
			removeConfigurations(tenantInfo);
		} catch (Exception e) {
			LOGGER.warn("Could not rollback solr audit configurations for tenant {} due to error {}",
					tenantInfo.getTenantId(), e.getMessage());
			LOGGER.trace("Could not rollback solr audit configurations for tenant {} due to error",
					tenantInfo.getTenantId(), e);
		}
	}

	private void removeConfigurations(TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			configurationManagement.removeConfiguration(auditConfiguration.getSolrProtocolConfigurationName());
			configurationManagement.removeConfiguration(auditConfiguration.getSolrHostConfigurationName());
			configurationManagement.removeConfiguration(auditConfiguration.getSolrPortConfigurationName());
			configurationManagement.removeConfiguration(auditConfiguration.getSolrCoreConfigurationName());
			configurationManagement
					.removeConfiguration(auditConfiguration.getRecentActivitiesSolrCoreConfigurationName());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Get the recent activities core name from the provided {@link TenantInfo}. The core name is in
	 * the format {username}_{recent activities suffix}.
	 * 
	 * @param info
	 *            the tenant info
	 * @return the recent activities core name
	 */
	public static String getRecentActivitiesCoreName(TenantInfo info) {
		String escapedTenantId = DbProvisioning.createUserNameFromTenantId(info);
		return escapedTenantId + RECENT_ACTIVITIES_SUFFIX;
	}

}
