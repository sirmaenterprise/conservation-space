package com.sirma.itt.seip.tenant.step;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.db.RelationDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantCreationDbStep provides the model for datasource and executes the actual creation
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 6)
public class TenantCreationDbStep extends AbstractTenantCreationStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantCreationDbStep.class);
	/** Current step name. */
	public static final String STEP_NAME = "DbInitialization";

	@Inject
	private RelationDbProvisioning relationDbProvisioning;

	@Override
	public String getIdentifier() {
		return STEP_NAME;
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			TenantRelationalContext relationalCtx = new TenantRelationalContext();
			context.addProvisionContext(getIdentifier(), relationalCtx);
			relationDbProvisioning.provision(data.getProperties(), context.getTenantInfo(), relationalCtx);
		} catch (Exception e) {
			LOGGER.error("Error during DB init step!", e);
			throw new TenantCreationException(e);
		}
		return true;
	}

	@Override
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		try {
			relationDbProvisioning.rollback(context.getProvisionContext(getIdentifier()), context.getTenantInfo());
		} catch (Exception e) {
			LOGGER.error("Error during DB rollback step!", e);
		}
		return false;
	}

}
