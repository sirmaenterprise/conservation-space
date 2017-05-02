package com.sirmaenterprise.sep.bpm.camunda.service.custom;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirmaenterprise.sep.bpm.camunda.tenant.step.CamundaDbProvisioning;

/**
 * Initialize prerequired by Camunda infrastructure. Use the provided {@link CamundaDbProvisioning} to do the actual
 * provisioning
 *
 * @author bbanchev
 */
public class TenantInitializationStep extends DeploymentOperationStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private SecurityContextManager securityContextManager;
	private CamundaDbProvisioning dbProvisioning;

	/**
	 * Initialize step with required services.
	 * 
	 * @param securityContextManager
	 *            the current {@link SecurityContextManager}
	 * @param dbProvisioning
	 *            the {@link CamundaDbProvisioning} instance
	 */
	public TenantInitializationStep(SecurityContextManager securityContextManager,
			CamundaDbProvisioning dbProvisioning) {
		this.securityContextManager = securityContextManager;
		this.dbProvisioning = dbProvisioning;
	}

	@Override
	public String getName() {
		return "Initialize required db and configurations.";
	}

	@Override
	public void performOperationStep(DeploymentOperation operationContext) {
		// if feature is disabled in general
		String globalInitDisabled = System.getProperty("system.camunda.global.init.disabled", "false");
		LOGGER.debug("Camunda automatic initialization is globally disabled={}", globalInitDisabled);
		if (Boolean.TRUE.equals(Boolean.valueOf(globalInitDisabled))) {
			return;
		}
		// now check the provided tenant
		TenantInfo tenantInfo = new TenantInfo(securityContextManager.getCurrentContext().getCurrentTenantId());
		String tenantId = tenantInfo.getTenantId();
		String forceInit = System.getProperty(getConfigurationKeyByTenantId(tenantId), "true");
		if (Boolean.FALSE.equals(Boolean.valueOf(forceInit))) {
			LOGGER.debug("Camunda automatic initialization is disabled for: {}", tenantId);
			return;
		}
		if (dbProvisioning.isProvisioned(tenantInfo)) {
			LOGGER.debug("Camunda datasource is already provided for tenant: {}", tenantId);
			return;
		}
		LOGGER.info("Going to initilize tenant: {}", tenantId);
		doInitialize(tenantInfo);
	}

	private void doInitialize(TenantInfo tenantInfo) {
		try {
			securityContextManager.initializeExecutionAsSystemAdmin();
			dbProvisioning.provision(new HashMap<>(0), tenantInfo, null, new TenantRelationalContext());
			LOGGER.info("Camunda tenant initilization completed for: {}", tenantInfo.getTenantId());
		} catch (Exception e) {
			LOGGER.error("Camunda initialization step failed due to an error!", e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Disable the initialization step for single tenant.
	 *
	 * @param tenant
	 *            the tenant is the tenant to disable for
	 */
	public static void disableStepForTenant(String tenant) {
		System.setProperty(getConfigurationKeyByTenantId(tenant), "false");
	}

	private static String getConfigurationKeyByTenantId(String tenant) {
		return "system.camunda." + tenant + ".init.forced";
	}

}
