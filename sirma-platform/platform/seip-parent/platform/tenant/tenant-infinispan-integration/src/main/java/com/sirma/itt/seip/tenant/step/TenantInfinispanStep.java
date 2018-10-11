package com.sirma.itt.seip.tenant.step;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.infinispan.LocalInfinispanProvisioning;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 7)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 7)
public class TenantInfinispanStep extends AbstractTenantStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private LocalInfinispanProvisioning cacheProvisioning;

	@Override
	public String getIdentifier() {
		return "InfinispanInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			cacheProvisioning.provision(context.getTenantInfo());
		} catch (Exception e) {
			LOGGER.error("Error during cache init step!", e);
			throw new TenantCreationException("Cound not add caches for tenant", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantInfo tenantInfo, boolean rollback) {
		try {
			cacheProvisioning.deleteCaches(tenantInfo);
			return true;
		} catch (Exception e) {
			LOGGER.warn("Infinispan caches couldn't be deleted due to {}!", e.getMessage());
			LOGGER.trace("Error during Infinispan caches rollback!", e);
		}
		return false;
	}

}