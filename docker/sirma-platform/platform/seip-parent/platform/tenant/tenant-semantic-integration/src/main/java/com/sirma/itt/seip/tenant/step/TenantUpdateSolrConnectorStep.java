package com.sirma.itt.seip.tenant.step;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.seip.semantic.management.ConnectorConfiguration;
import com.sirma.seip.semantic.management.ConnectorService;

/**
 * @author kirq4e
 */
@Extension(target = TenantStep.UPDATE_STEP_NAME, order = 10.5)
public class TenantUpdateSolrConnectorStep extends AbstractTenantStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String STEP_NAME = "TenantUpdateSolrConnectorStep";

	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private ConnectorService connectorService;
	@Inject
	private SolrConfiguration solrConfiguration;
	
	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		LOGGER.info("Starting SolrConnectorUpdateStep");
		String tenantId = context.getTenantInfo().getTenantId();

		securityContextManager.initializeTenantContext(tenantId);
		try {
			String solrAddress = solrConfiguration.getSolrAddress();

			List<ConnectorConfiguration> connectors = connectorService.listConnectors();
			if (connectors.isEmpty()) {
				// expected that when updating the tenant if there are no connectors then they are deleted on purpose
				// to allow automatic generation of connector configuration instead of updating the existing
				// configuration
				ConnectorConfiguration configuration = connectorService
						.createDefaultConnectorConfiguration(connectorService.createConnectorName(tenantId));

				configuration.setAddress(solrAddress);

				connectorService.saveConnectorConfiguration(configuration);
				connectorService.resetConnector(configuration.getConnectorName());
			} else {
				for (ConnectorConfiguration configuration : connectors) {
					if (configuration.getRecreate()) {
						if (StringUtils.isEmpty(configuration.getAddress())) {
							configuration.setAddress(solrAddress);
						}

						LOGGER.info("Recreating the connector: {}", configuration.getConnectorName());
						connectorService.resetConnector(configuration.getConnectorName());
					}
				}
			}
		} finally {
			securityContextManager.endContextExecution();
		}
		LOGGER.info("Finishing SolrConnectorUpdateStep");
		return true;
	}

	@Override
	public String getIdentifier() {
		return STEP_NAME;
	}

}
