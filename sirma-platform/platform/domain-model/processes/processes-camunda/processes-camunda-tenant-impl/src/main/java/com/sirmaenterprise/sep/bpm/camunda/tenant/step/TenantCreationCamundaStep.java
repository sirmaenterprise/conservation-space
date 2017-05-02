package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import static com.sirma.itt.seip.tenant.db.BaseRelationalDbProvisioning.isDatabaseReused;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.step.TenantCreationDbStep;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirmaenterprise.sep.bpm.camunda.service.custom.TenantInitializationStep;

/**
 * The {@link TenantCreationCamundaStep} provides the model for datasource and executes the actual creation of tenant
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 19)
public class TenantCreationCamundaStep extends AbstractTenantCreationStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantCreationCamundaStep.class);
	/** Current step name. */
	public static final String STEP_NAME = "CamundaInitialization";

	@Inject
	private CamundaDbProvisioning camundaDbProvisioning;

	@Override
	public String getIdentifier() {
		return STEP_NAME;
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			// disable auto update and continue with provided data
			TenantInitializationStep.disableStepForTenant(context.getTenantInfo().getTenantId());
			TenantRelationalContext relationalContext = new TenantRelationalContext();
			context.addProvisionContext(getIdentifier(), relationalContext);
			TenantRelationalContext contextToUse = null;
			if (isDatabaseReused(data)) {
				contextToUse = context.getProvisionContext(TenantCreationDbStep.STEP_NAME);
			}
			camundaDbProvisioning.provision(data.getProperties(), context.getTenantInfo(), contextToUse,
					relationalContext);
		} catch (Exception e) {
			throw new TenantCreationException("Error during Camunda DB init step!", e);
		}
		return true;
	}

	@Override
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		TenantRelationalContext relationalContext = context.getProvisionContext(getIdentifier());
		try {
			// if database was not reused then remove it otherwise do not
			camundaDbProvisioning.rollback(relationalContext, context.getTenantInfo(), !isDatabaseReused(data));
		} catch (Exception e) {
			LOGGER.error("Error during Camunda DB rollback!", e);
		}
		return false;
	}

}
