package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirmaenterprise.sep.bpm.camunda.service.SepProcessApplication;

/**
 * {@link TenantStep#UPDATE_STEP_NAME} plugin to initialize Camunda in existing tenants
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.UPDATE_STEP_NAME, order = 19)
public class TenantUpdateCamundaStep extends TenantCamundaStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantUpdateCamundaStep.class);

	@EJB
	private SepProcessApplication bpmApplication;

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			bpmApplication.deploy();
		} catch (Exception e) {
			LOGGER.error("Camunda update step failed due to an error!", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		return false;
	}

}