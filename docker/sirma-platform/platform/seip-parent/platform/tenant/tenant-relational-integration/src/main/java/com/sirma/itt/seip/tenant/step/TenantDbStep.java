package com.sirma.itt.seip.tenant.step;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.RelationDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantCreationDbStep provides the model for the relational database and datasource creation
 * and deletion and executes the actual creation.
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 6)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 16)
public class TenantDbStep extends AbstractTenantRelationalStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantDbStep.class);
	/** Current step name. */
	public static final String STEP_NAME = "DbInitialization";

	@Inject
	private RelationDbProvisioning relationDbProvisioning;

	@Inject
	private DatabaseConfiguration databaseConfiguration;

	@Override
	public String getIdentifier() {
		return STEP_NAME;
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			TenantRelationalContext relationalCtx = new TenantRelationalContext();
			context.addProvisionContext(getIdentifier(), relationalCtx);
			relationDbProvisioning.provision(data.getProperties(), context.getTenantInfo(), null, relationalCtx);
		} catch (Exception e) {
			LOGGER.error("Error during DB init step!", e);
			throw new TenantCreationException(e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		try {
			TenantInfo tenantInfo = context.getTenantInfo();
			relationDbProvisioning.rollback(getRelationalContext(databaseConfiguration,
					relationDbProvisioning.getDatasourceName(tenantInfo), tenantInfo), null, tenantInfo, true);
		} catch (Exception e) {
			LOGGER.warn("Error during DB rollback step {}!", e.getMessage());
			LOGGER.trace("Error during DB rollback step!", e);
		}
		return false;
	}

}