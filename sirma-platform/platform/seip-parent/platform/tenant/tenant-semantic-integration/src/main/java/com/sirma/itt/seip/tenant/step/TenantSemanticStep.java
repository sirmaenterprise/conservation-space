package com.sirma.itt.seip.tenant.step;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.semantic.SemanticRepositoryProvisioning;
import com.sirma.itt.seip.tenant.semantic.TenantSemanticContext;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Tenant creation step that creates semantic repository.
 *
 * @author BBonev
 */
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 10)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 6)
public class TenantSemanticStep extends AbstractTenantStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** Current step name. */
	public static final String STEP_NAME = "SemanticDbInitialization";

	@Inject
	private SemanticRepositoryProvisioning repositoryProvisioning;

	@Inject
	private SemanticConfiguration semanticConfiguration;
	
	@Inject
	private SecurityContextManager securityContextManager;
	
	@Override
	public String getIdentifier() {
		return STEP_NAME;
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		Map<String, Serializable> stepData = data.getProperties();
		try {
			repositoryProvisioning.provision(stepData, data.getModels(), context.getTenantInfo(),
					ctx -> context.addProvisionContext(getIdentifier(), ctx));
		} catch (Exception e) {
			throw new TenantCreationException("Error during semantic DB init step!", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantInfo tenantInfo, boolean rollback) {
		try {
			securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
			TenantSemanticContext semanticContext = new TenantSemanticContext();
			semanticContext.setRepoName(semanticConfiguration.getRepositoryName().get());
			semanticContext.setSemanticAddress(URI.create(semanticConfiguration.getServerURL().get()));

			repositoryProvisioning.rollback(semanticContext, tenantInfo);
			return true;
		} catch (Exception e) {
			LOGGER.warn("Semantic repository couldn't be deleted due to {}!", e.getMessage());
			LOGGER.trace("Error during semantic repository rollback!", e);
		} finally {
			securityContextManager.endContextExecution();
		}
		return false;
	}

}