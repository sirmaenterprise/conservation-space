/**
 *
 */
package com.sirma.itt.seip.tenant.step;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.semantic.SemanticRepositoryProvisioning;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Tenant creation step that creates semantic repository.
 *
 * @author BBonev
 */
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 10)
public class TenantCreationSemanticStep extends AbstractTenantCreationStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** Current step name. */
	public static final String STEP_NAME = "SemanticDbInitialization";

	@Inject
	private SemanticRepositoryProvisioning repositoryProvisioning;

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
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		try {
			repositoryProvisioning.rollback(context.getProvisionContext(getIdentifier()), context.getTenantInfo());
			return true;
		} catch (Exception e) {
			LOGGER.error("Error during semantic DB rollback!", e);
		}
		return false;
	}

}
