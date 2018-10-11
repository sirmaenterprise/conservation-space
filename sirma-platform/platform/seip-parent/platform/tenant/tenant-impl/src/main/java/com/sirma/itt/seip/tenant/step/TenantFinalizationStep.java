package com.sirma.itt.seip.tenant.step;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * The final step during tenant creation process - all final logic should be placed here or relevant events to be fired
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 100)
public class TenantFinalizationStep extends AbstractTenantStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private TenantManager tenantManager;

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		TenantInfo tenantInfo = context.getTenantInfo();
		try {
			// make sure the tenant is active at the end of creation process
			// this is because for some reason (probably transactions) the tenant is not enabled after failed to
			// initialize before and need to be activated explicitly
			tenantManager.activeTenant(tenantInfo.getTenantId());

			if (SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
				return true;
			}

			// notify for tenant creation - this will trigger calling all methods
			// annotated with @Startup and @OnTenantAdd
			// this is not needed to be called for default tenant initialization
			tenantManager.finishTenantActivation(tenantInfo.getTenantId());
		} catch (Exception e) {
			LOGGER.warn("Problems with tenant finalization. Some components may not be initialized.", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantInfo tenantInfo, boolean rollback) {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "TenantFinialization";
	}

}