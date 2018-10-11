/**
 *
 */
package com.sirma.itt.seip.tenant.step;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.semantic.SolrConnectorProvisioning;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Tenant creation step that creates semantic solr connector in repository. This step should be
 * executed after DMS is created, definitions are uploaded and synchronized in order to generate
 * proper connector.
 *
 * @author BBonev
 */
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 11)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 5.5)
public class TenantSolrConnectorStep extends AbstractTenantStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SolrConnectorProvisioning solrConnectorProvisioning;

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			solrConnectorProvisioning.provisionSolrConnector(data.getProperties(),
					context.getProvisionContext(TenantSemanticStep.STEP_NAME), context.getTenantInfo());
		} catch (Exception e) {
			LOGGER.error("Error during Solr connector initialization!", e);
			throw new TenantCreationException("Cound not create Solr connector ", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantInfo tenantInfo, boolean rollback) {
		try {
			solrConnectorProvisioning.rollbackSolrConnector(tenantInfo);
			return true;
		} catch (Exception e) {
			LOGGER.warn("Solr connector couldn't be deleted due to {}!", e.getMessage());
			LOGGER.trace("Error during Solr connector rollback!", e);
		}
		return false;
	}

	@Override
	public String getIdentifier() {
		return "SemanticSolrConnectorInitialization";
	}
}