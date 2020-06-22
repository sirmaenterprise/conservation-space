package com.sirma.itt.seip.tenant.step;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.tenant.context.Tenant;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.context.TenantStatus;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantInitializationStep is the initial step that register base information and register the new tenant in core
 * db
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 18)
public class TenantInitializationStep extends AbstractTenantStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String TENANTADMIN_USERNAME = "admin";
	private static final String KEY_MODEL_TENANTID = "tenantid";

	@Inject
	private TenantManager tenantManager;

	@Override
	public String getIdentifier() {
		return "TenantInitialization";
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		// force tenant domains to be with lower case because we will ignore the case
		String tenantId = data.getPropertyValue(KEY_MODEL_TENANTID, true).toLowerCase();
		if (tenantId.isEmpty()) {
			throw new TenantCreationException("Tenant Id is not in required format for domain name!");
		}
		LOGGER.info("Going to create/activate tenant {}", tenantId);
		Optional<Tenant> existingTenant = tenantManager.getTenant(tenantId);
		if (existingTenant.isPresent()) {
			if (TenantStatus.ACTIVE.equals(existingTenant.get().getStatus())) {
				throw new TenantCreationException(
						"Tenant with id '" + tenantId + "' is already registered and active!");
			} else if (TenantStatus.DELETED.equals(existingTenant.get().getStatus())) {
				throw new TenantCreationException("Tenant with id '" + tenantId
						+ "' has been deleted and that id will be available for reuse on next server restart!");
			}
		}

		String tenantDescription = data.getPropertyValue("tenantdescription", false);
		String tenantDisplayName = data.getPropertyValue("tenantname", true);

		TenantInfo infoBean = new TenantInfo(tenantId, tenantDisplayName, tenantDescription);
		context.setTenantInfo(infoBean);
		if (SecurityContext.isDefaultTenant(tenantId)) {
			context.setAdminUser(TENANTADMIN_USERNAME);
		} else {
			context.setAdminUser(TENANTADMIN_USERNAME + SecurityUtil.TENANT_ID_SEPARATOR + tenantId);
		}
		try {
			Tenant tenant = existingTenant.orElseGet(Tenant::new);
			tenant.setTenantId(tenantId);
			tenant.setStatus(TenantStatus.ACTIVE);
			tenant.setTenantAdmin(context.getAdminUser());
			tenant.setDisplayName(tenantDisplayName);
			tenant.setDescription(tenantDescription);
			addOrActivateTenant(existingTenant.orElse(null), tenant);
		} catch (TenantValidationException e) {
			throw new TenantCreationException("Tenant initialization failed!", e);
		}
		return true;
	}

	private void addOrActivateTenant(Tenant existingTenant, Tenant newTenant)
			throws TenantValidationException {
		if (existingTenant != null) {
			LOGGER.debug("Updating existing tenant {}", existingTenant.getTenantId());
			tenantManager.updateTenant(newTenant);
			if (TenantStatus.INACTIVE.equals(existingTenant.getStatus())) {
				LOGGER.debug("Activating existing tenant {}", existingTenant.getTenantId());
				tenantManager.activeTenant(newTenant.getTenantId());
			}
		} else {
			LOGGER.info("Adding new tenant {}", newTenant.getTenantId());
			// mark the new tenant as not active, if its active breaks some of the other tenant creation steps, the
			// final step will try to activate the tenant anyway
			newTenant.setStatus(TenantStatus.INACTIVE);
			tenantManager.addNewTenant(newTenant);
		}
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		TenantInfo tenantInfo = context.getTenantInfo();
		if (tenantInfo == null) {
			return true;
		}
		String tenantId = tenantInfo.getTenantId();
		Optional<Tenant> existingTenant = tenantManager.getTenant(tenantId);
		if (existingTenant.isPresent()) {
			try {
				tenantManager.deactivateTenant(tenantId);
			} catch (TenantValidationException e) {
				LOGGER.warn("Failed to deactivate tenant '" + tenantId + "' during rollback due to {}!",
						e.getMessage());
				LOGGER.trace("Failed to deactivate tenant '" + tenantId + "' during rollback due to!", e);
				return false;
			}
		}
		return true;
	}

}
