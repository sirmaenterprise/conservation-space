package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.RelationDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.step.AbstractTenantRelationalStep;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.service.custom.TenantInitializationStep;
import com.sirmaenterprise.sep.bpm.camunda.tenant.service.SepRuntimeCamundaContainerDelegate;

/**
 * The {@link TenantCamundaStep} provides the model for camunda database and datasource creation and
 * deletion.
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 19)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 13)
public class TenantCamundaStep extends AbstractTenantRelationalStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantCamundaStep.class);
	/** Current step name. */
	public static final String STEP_NAME = "CamundaInitialization";

	@Inject
	private CamundaDbProvisioning camundaDbProvisioning;

	@Inject
	private DatabaseConfiguration databaseConfigurations;

	@Inject
	private RelationDbProvisioning relationDbProvisioning;

	@Inject
	private CamundaConfiguration camundaDatabaseConfigurations;

	@Inject
	private SepRuntimeCamundaContainerDelegate camundaRuntimeContainerDelegate;

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
			TenantRelationalContext contextToUse = getRelationalContextIfReused(data, databaseConfigurations,
					relationDbProvisioning.getDatasourceName(context.getTenantInfo()), context.getTenantInfo());
			camundaDbProvisioning.provision(data.getProperties(), context.getTenantInfo(), contextToUse,
					relationalContext);

			camundaRuntimeContainerDelegate.deployEngine(context.getTenantInfo().getTenantId());
		} catch (Exception e) {
			throw new TenantCreationException("Error during Camunda DB init step!", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		TenantInfo tenantInfo = context.getTenantInfo();
		TenantRelationalContext relationalContext = getRelationalContext(camundaDatabaseConfigurations,
				camundaDbProvisioning.getDatasourceName(tenantInfo), tenantInfo);
		try {
			// if database was not reused then remove it otherwise do not
			TenantRelationalContext contextToUse = getRelationalContextIfReused(data, databaseConfigurations,
					relationDbProvisioning.getDatasourceName(tenantInfo), tenantInfo);
			camundaDbProvisioning.rollback(relationalContext, contextToUse, tenantInfo, !isDatabaseReused(data));

			camundaRuntimeContainerDelegate.undeployEngine(tenantInfo.getTenantId());

			return true;
		} catch (Exception e) {
			LOGGER.warn("Error during Camunda DB rollback {}!", e.getMessage());
			LOGGER.trace("Error during Camunda DB rollback!", e);
			return false;
		}
	}

}