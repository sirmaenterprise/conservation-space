package com.sirma.itt.seip.tenant.semantic;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.model.vocabulary.Connectors;
import com.sirma.seip.semantic.management.ConnectorConfiguration;
import com.sirma.seip.semantic.management.ConnectorService;

/**
 * Provisioning for Solr connector
 * 
 * @author kirq4e
 */
@ApplicationScoped
public class SolrConnectorProvisioning {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "semantic.connectorProvider", defaultValue = "solr", sensitive = true, type = SubsystemTenantAddressProvider.class, system = true)
	private ConfigurationProperty<SubsystemTenantAddressProvider> connectorProvider;

	@Inject
	private SolrConfiguration solrConfiguration;
	@Inject
	private ConnectorService connectorService;
	@Inject
	private SemanticConfiguration semanticConfiguration;
	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private TransactionSupport transactionSupport;

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

		URI solrAddress = connectorProvider.get().provideAddressForNewTenant(
				Objects.toString(properties.get(Connectors.ADDRESS.getLocalName()), null));

		securityContextManager.initializeTenantContext(tenantId);
		try {
			List<ConnectorConfiguration> connectors = connectorService.listConnectors();
			if (connectors.isEmpty()) {
				ConnectorConfiguration configuration = connectorService
						.createDefaultConnectorConfiguration(connectorService.createConnectorName(tenantId));

				createConnector(semanticContext, solrAddress, tenantId, configuration);
			} else {
				for (ConnectorConfiguration configuration : connectors) {
					if (configuration.getRecreate()) {
						createConnector(semanticContext, solrAddress, tenantId, configuration);
					}
				}
			}
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private void createConnector(TenantSemanticContext semanticContext, URI solrAddress, String tenantId,
			ConnectorConfiguration configuration) {
		String connectorName = configuration.getConnectorName();
		if (connectorService.isConnectorPresent(connectorName)) {
			connectorService.deleteConnector(connectorName);
		}

		configuration.setAddress(solrAddress.toString());

		Collection<Configuration> addedConfigurations = transactionSupport
				.invokeInNewTx(() -> insertSolrConfigurations(tenantId, connectorName, solrAddress));
		if (addedConfigurations.isEmpty()) {
			throw new TenantCreationException("Could not insert solr configurations");
		}

		transactionSupport.invokeInNewTx(() -> connectorService.createConnector(configuration));

		semanticContext.setSolrCoreName(connectorName);
		LOGGER.info("Semantic connector {} created at {} for semantic database at {} in repo {}", connectorName,
				solrAddress, semanticContext.getSemanticAddress(), semanticContext.getRepoName());
	}

	private Collection<Configuration> insertSolrConfigurations(String tenantId, String connectorName, URI solrAddress) {
		Configuration protocol = new Configuration(solrConfiguration.getSolrProtocolConfiguration(),
				solrAddress.getScheme(), tenantId);
		Configuration host = new Configuration(solrConfiguration.getSolrHostConfiguration(), solrAddress.getHost(),
				tenantId);
		Configuration port = new Configuration(solrConfiguration.getSolrPortConfiguration(), "" + solrAddress.getPort(),
				tenantId);
		Configuration core = new Configuration(solrConfiguration.getSolrCoreConfiguration(), connectorName, tenantId);
		Configuration ftsIndex = new Configuration(semanticConfiguration.getFtsIndexName().getName(),
				"solr-inst:" + connectorName, tenantId);

		return configurationManagement.addConfigurations(Arrays.asList(protocol, host, port, core, ftsIndex));
	}

	/**
	 * Rollback solr connector.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void rollbackSolrConnector(TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			String ftsIndexIRI = semanticConfiguration.getFtsIndexName().get();
			final String ftsIndexName = ftsIndexIRI.substring(ftsIndexIRI.indexOf(SPARQLQueryHelper.URI_SEPARATOR) + 1);
			transactionSupport.invokeInNewTx(this::removeSolrConfigurations);
			transactionSupport.invokeInNewTx(() -> connectorService.deleteConnector(ftsIndexName));
		} finally {
			securityContextManager.endContextExecution();
		}

	}

	private void removeSolrConfigurations() {
		configurationManagement.removeConfiguration(solrConfiguration.getSolrProtocolConfiguration());
		configurationManagement.removeConfiguration(solrConfiguration.getSolrHostConfiguration());
		configurationManagement.removeConfiguration(solrConfiguration.getSolrPortConfiguration());
		configurationManagement.removeConfiguration(solrConfiguration.getSolrCoreConfiguration());
		configurationManagement.removeConfiguration(semanticConfiguration.getFtsIndexName().getName());
	}

}
