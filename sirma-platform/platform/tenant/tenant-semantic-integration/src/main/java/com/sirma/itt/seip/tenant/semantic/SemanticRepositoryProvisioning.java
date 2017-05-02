package com.sirma.itt.seip.tenant.semantic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.patch.SemanticPatchService;
import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.patch.exception.PatchFailureException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.semantic.patch.BackingPatchService;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.seip.semantic.management.RepositoryConfiguration;
import com.sirma.seip.semantic.management.RepositoryInfo;
import com.sirma.seip.semantic.management.RepositoryManagement;
import com.sirma.seip.semantic.management.SolrConnectorConfiguration;

/**
 * Provisioning for semantic database.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SemanticRepositoryProvisioning {
	private static final String REPOSITORY_NAME = "repositoryName";
	private static final String PASSWORD = "$pass$";
	private static final String USER_NAME = "$user$";
	private static final String ADDRESS = "$address$";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "semantic.applicationName", defaultValue = "graphdb-workbench", sensitive = true, type = SubsystemTenantAddressProvider.class, system = true)
	private ConfigurationProperty<SubsystemTenantAddressProvider> databaseProvider;

	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "semantic.connectorProvider", defaultValue = "solr", sensitive = true, type = SubsystemTenantAddressProvider.class, system = true)
	private ConfigurationProperty<SubsystemTenantAddressProvider> connectorProvider;

	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "semantic.adminName", defaultValue = "admin", sensitive = true, system = true)
	private ConfigurationProperty<String> adminName;

	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "semantic.adminPassword", defaultValue = "root", sensitive = true, system = true)
	private ConfigurationProperty<String> adminPassword;

	@Inject
	private RepositoryManagement repositoryManagement;
	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private SemanticConfiguration semanticConfiguration;
	@Inject
	private SolrConfiguration solrConfiguration;
	@Inject
	private SemanticPatchService patchService;
	@Inject
	private RawConfigurationAccessor rawConfigurationAccessor;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private BackingPatchService patchUtilService;

	/**
	 * Lookup bean.
	 *
	 * @param context
	 *            the context
	 * @param beanManager
	 *            the bean manager
	 * @return the subsystem tenant address provider
	 */
	@ConfigurationConverter
	static SubsystemTenantAddressProvider lookupBean(ConverterContext context, BeanManager beanManager) {
		return CDI.instantiateBean(context.getRawValue(), SubsystemTenantAddressProvider.class, beanManager);
	}

	/**
	 * Provision a new semantic repository - creates and executes the patches.
	 *
	 * @param properties
	 *            the properties
	 * @param models
	 *            the models
	 * @param tenantInfo
	 *            the context
	 * @param contextConsumer
	 *            the context consumer
	 */
	public void provision(Map<String, Serializable> properties, List<File> models, TenantInfo tenantInfo,
			Consumer<TenantSemanticContext> contextConsumer) {
		databaseProvider.requireConfigured("Cound not find semantic address provider");
		boolean isDefaultTenant = SecurityContext.isDefaultTenant(tenantInfo.getTenantId());
		// clear context data
		if (!isDefaultTenant) {
			createRepository(properties, tenantInfo);
			LOGGER.info("Repository creation complete.");
		} else {
			initializeDefaultTenantProperties(properties, tenantInfo);
		}
		// now new repo is created - update the context
		TenantSemanticContext provisionContext = new TenantSemanticContext();
		provisionContext.setSemanticAddress((URI) properties.get(ADDRESS));
		// use the name of already created repository
		provisionContext.setRepoName(properties.get(REPOSITORY_NAME).toString());
		contextConsumer.accept(provisionContext);

		if (isDefaultTenant
				&& !isRepositoryExists(provisionContext.getRepoName(), provisionContext.getSemanticAddress())) {
			// eventually we could create the repo
			throw new TenantCreationException("Could not find a repository " + provisionContext.getRepoName());
		}

		Collection<Configuration> insertedConfigurations = transactionSupport
				.invokeInNewTx(() -> insertConfigurations(properties, tenantInfo));

		if (insertedConfigurations.isEmpty() && !isDefaultTenant) {
			throw new TenantCreationException("Could not add tenant configurations");
		}

		if (!isDefaultTenant) {
			patchRepository(models, tenantInfo);
		}
	}

	private void initializeDefaultTenantProperties(Map<String, Serializable> properties, TenantInfo tenantInfo) {
		String tenantId = tenantInfo.getTenantId();
		// if repository name is not set then we will read the one from configurations for the
		// default tenant
		Serializable defaultTenantRepoName = properties.computeIfAbsent(REPOSITORY_NAME,
				key -> getRawConfig(tenantId, semanticConfiguration.getRepositoryNameConfiguration()));
		if (defaultTenantRepoName == null) {
			throw new TenantCreationException("No semantic repository name is configured for default tenant");
		}

		// if server address is not set then we will read it from configurations for the default
		// tenant
		properties.computeIfAbsent(ADDRESS, key ->
			{
				String address = getRawConfig(tenantId, semanticConfiguration.getServerURLConfiguration());
				if (StringUtils.isNullOrEmpty(address)) {
					throw new TenantCreationException("No semantic repository address configured for default tenant");
				}
				return URI.create(address);
			});
	}

	private String getRawConfig(String tenant, String configName) {
		return securityContextManager.executeAsTenant(tenant)
				.function(rawConfigurationAccessor::getRawConfigurationValue, configName);
	}

	private boolean isRepositoryExists(String repositoryName, Serializable address) {
		URI repoAddress;
		if (address instanceof URI) {
			repoAddress = (URI) address;
		} else {
			repoAddress = URI.create(address.toString());
		}
		return repositoryManagement
				.isRepositoryExists(fillRepositoryInfo(new RepositoryInfo(), repositoryName, repoAddress));
	}

	private void createRepository(Map<String, Serializable> properties, TenantInfo tenantInfo) {
		String tenantId = tenantInfo.getTenantId();

		URI tenantAddress = databaseProvider.get()
				.provideAddressForNewTenant(Objects.toString(properties.get("address"), null));

		// the repository name cannot contain dot
		String repositoryName = properties.getOrDefault(REPOSITORY_NAME, tenantId).toString();
		repositoryName = escape(repositoryName);

		if (isRepositoryExists(repositoryName, tenantAddress)) {
			throw new TenantCreationException(
					"Repository with name " + tenantId + " already exists at " + tenantAddress);
		}
		// update properties
		properties.put(ADDRESS, tenantAddress);
		properties.put(REPOSITORY_NAME, repositoryName);
		RepositoryConfiguration configuration = createRepositoryConfiguration(tenantId, properties);

		repositoryManagement.createRepository(configuration);

		// this should be changed to use single access user to all databases
		String userName = escape(tenantId);
		String password = createUserPasswordForTenantId(tenantInfo);

		try {
			repositoryManagement.createAccessUserForRepo(configuration.getInfo(), userName, password);
		} catch (UnsupportedOperationException e) {
			LOGGER.warn("Semantic user management is not implemented. Using admin user repository access!");
			LOGGER.trace("", e);
			userName = adminName.get();
			password = adminPassword.get();
		}

		properties.put(USER_NAME, userName);
		properties.put(PASSWORD, password);
	}

	private static String escape(String text) {
		return text.replaceAll("[\\.-]+", "_");
	}

	/**
	 * Creates the user password for tenant id.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @return the string
	 */
	public static String createUserPasswordForTenantId(TenantInfo tenantInfo) {
		String uuid = UUID.randomUUID().toString();
		return tenantInfo.getTenantId() + "-" + uuid.substring(0, uuid.indexOf('-'));
	}

	private Collection<Configuration> insertConfigurations(Map<String, Serializable> properties,
			TenantInfo tenantInfo) {
		String tenantId = tenantInfo.getTenantId();
		securityContextManager.initializeTenantContext(tenantId);
		try {
			// this data is already computed and used - just store the config
			Serializable address = properties.get(ADDRESS);
			String userName = (String) properties.get(USER_NAME);
			String password = (String) properties.get(PASSWORD);
			String repositoryName = (String) properties.get(REPOSITORY_NAME);

			Configuration addressConfig = new Configuration(semanticConfiguration.getServerURLConfiguration(),
					address.toString(), tenantId);
			Configuration accessUserName = new Configuration(
					semanticConfiguration.getRepositoryAccessUserNameConfiguration(), userName, tenantId);
			Configuration accessUserPass = new Configuration(
					semanticConfiguration.getRepositoryAccessUserPasswordConfiguration(), password, tenantId);
			Configuration repoNameConfig = new Configuration(semanticConfiguration.getRepositoryNameConfiguration(),
					repositoryName, tenantId);
			return configurationManagement
					.addConfigurations(Arrays.asList(addressConfig, accessUserName, accessUserPass, repoNameConfig));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	@SuppressWarnings("boxing")
	private RepositoryConfiguration createRepositoryConfiguration(String tenantId,
			Map<String, Serializable> properties) {
		RepositoryConfiguration configuration = new RepositoryConfiguration();
		configuration.setLabel("Repository for tenant " + tenantId);
		fillRepositoryInfo(configuration.getInfo(), (String) properties.get(REPOSITORY_NAME),
				(URI) properties.get(ADDRESS));
		configuration
				.setEntityIndexSize(Long.valueOf(properties.getOrDefault("entity-index-size", 2000000L).toString()));
		configuration.setTupleIndexMemory(properties.getOrDefault("tuple-index-memory", "512m").toString());
		configuration.setCacheMemory(properties.getOrDefault("cache-memory", "512m").toString());
		return configuration;
	}

	private RepositoryInfo fillRepositoryInfo(RepositoryInfo info, String repoName, URI tenantAddress) {
		info.setAddress(tenantAddress);
		info.setRepositoryName(repoName);
		info.setUserName(adminName.get());
		info.setPassword(adminPassword.get());
		return info;
	}

	private void patchRepository(List<File> models, TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			patchService.runPatches();
			for (File model : models) {
				patchUtilService.runPatchAndBackup(model, tenantInfo.getTenantId());
			}
		} catch (RollbackedException | PatchFailureException e) {
			throw new TenantCreationException("Cound not patch database due to ", e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Rollback repository creation.
	 *
	 * @param context
	 *            the context
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void rollback(TenantSemanticContext context, TenantInfo tenantInfo) {
		try {
			if (context != null && !SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
				deleteRepository(context);
			}
		} catch (Exception e) {
			LOGGER.warn("Cound not delete repository {} on rollback", tenantInfo.getTenantId(), e);
		}

		try {
			deleteConfigurations(tenantInfo);
		} catch (Exception e) {
			LOGGER.warn("Cound not delete semantic configurations for tenant {} on rollback", tenantInfo.getTenantId(),
					e);
		}
	}

	private void deleteRepository(TenantSemanticContext context) {
		URI tenantAddress = context.getSemanticAddress();
		if (tenantAddress == null) {
			return;
		}
		repositoryManagement
				.deleteRepository(fillRepositoryInfo(new RepositoryInfo(), context.getRepoName(), tenantAddress));
	}

	private void deleteConfigurations(TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			configurationManagement.removeConfiguration(semanticConfiguration.getServerURLConfiguration());
			configurationManagement
					.removeConfiguration(semanticConfiguration.getRepositoryAccessUserNameConfiguration());
			configurationManagement
					.removeConfiguration(semanticConfiguration.getRepositoryAccessUserPasswordConfiguration());
			configurationManagement.removeConfiguration(semanticConfiguration.getRepositoryNameConfiguration());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Provision solr connector and solr configurations.
	 *
	 * @param properties
	 *            the properties
	 * @param semanticContext
	 *            the semantic context
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void provisionSolrConnector(Map<String, Serializable> properties, TenantSemanticContext semanticContext,
			TenantInfo tenantInfo) {
		connectorProvider.requireConfigured("Cound not find connector address provider");

		if (semanticContext == null) {
			throw new TenantCreationException("Cannot initialize Solr connector without semantic repository info!");
		}

		String tenantId = tenantInfo.getTenantId();
		if (SecurityContext.isDefaultTenant(tenantId)) {
			return;
		}

		RepositoryInfo info = fillRepositoryInfo(new RepositoryInfo(), escape(tenantId),
				semanticContext.getSemanticAddress());

		String connectorName = "fts_" + escape(tenantId);
		if (repositoryManagement.isSolrConnectorPresent(info, connectorName)) {
			throw new TenantCreationException("Semantic connector " + connectorName + " found at address "
					+ semanticContext.getSemanticAddress());
		}

		SolrConnectorConfiguration configuration = new SolrConnectorConfiguration();
		configuration.setRepositoryInfo(info);
		configuration.setConnectorName(connectorName);

		URI solrAddress = connectorProvider.get()
				.provideAddressForNewTenant(Objects.toString(properties.get("address"), null));
		configuration.setSolrConnector(buildConnector(connectorName, solrAddress));
		configuration.setInitialSolrImport(readResource("init_solr_schema.trig"));

		repositoryManagement.createSolrConnector(configuration);

		Collection<Configuration> addedConfigurations = transactionSupport
				.invokeInNewTx(() -> insertSolrConfigurations(tenantId, connectorName, solrAddress));
		if (addedConfigurations.isEmpty()) {
			throw new TenantCreationException("Could not insert solr configurations");
		}

		semanticContext.setSolrCoreName(connectorName);
		LOGGER.info("Semantic connector {} created at {} for semantic database at {} in repo {}", connectorName,
				solrAddress, semanticContext.getSemanticAddress(), semanticContext.getRepoName());
	}

	private Collection<Configuration> insertSolrConfigurations(String tenantId, String connectorName, URI solrAddress) {
		securityContextManager.initializeTenantContext(tenantId);
		try {
			Configuration protocol = new Configuration(solrConfiguration.getSolrProtocolConfiguration(),
					solrAddress.getScheme(), tenantId);
			Configuration host = new Configuration(solrConfiguration.getSolrHostConfiguration(), solrAddress.getHost(),
					tenantId);
			Configuration port = new Configuration(solrConfiguration.getSolrPortConfiguration(),
					"" + solrAddress.getPort(), tenantId);
			Configuration core = new Configuration(solrConfiguration.getSolrCoreConfiguration(), connectorName,
					tenantId);
			Configuration ftsIndex = new Configuration(semanticConfiguration.getFtsIndexName().getName(),
					"solr-inst:" + connectorName, tenantId);

			return configurationManagement.addConfigurations(Arrays.asList(protocol, host, port, core, ftsIndex));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private String buildConnector(String connectorName, URI provideAddressForNewTenant) {
		// this should be generated at some point
		String readFile = readResource("graphdb_solr_connector.ttl");
		return readFile.replaceAll("\\{coreName\\}", connectorName).replaceAll("\\{solrUrl\\}",
				provideAddressForNewTenant.toString());
	}

	/**
	 * Rollback solr connector.
	 *
	 * @param semanticContext
	 *            the semantic context
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void rollbackSolrConnector(TenantSemanticContext semanticContext, TenantInfo tenantInfo) {
		String tenantId = tenantInfo.getTenantId();
		removeSolrConfigurations(tenantId);

		if (semanticContext.getSolrCoreName() != null) {
			RepositoryInfo info = fillRepositoryInfo(new RepositoryInfo(), tenantId,
					semanticContext.getSemanticAddress());
			repositoryManagement.deleteSolrConnector(info, semanticContext.getSolrCoreName());
		}
	}

	private void removeSolrConfigurations(String tenantId) {
		securityContextManager.initializeTenantContext(tenantId);
		try {
			configurationManagement.removeConfiguration(solrConfiguration.getSolrProtocolConfiguration());
			configurationManagement.removeConfiguration(solrConfiguration.getSolrHostConfiguration());
			configurationManagement.removeConfiguration(solrConfiguration.getSolrPortConfiguration());
			configurationManagement.removeConfiguration(solrConfiguration.getSolrCoreConfiguration());
			configurationManagement.removeConfiguration(semanticConfiguration.getFtsIndexName().getName());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Read resource.
	 *
	 * @param fileName
	 *            the file name
	 * @return the string
	 */
	protected String readResource(String fileName) {
		try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
			Objects.requireNonNull(inputStream, "File " + fileName + " not found!");
			return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Could not load default configuration", e);
		}
	}

}
