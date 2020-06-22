package com.sirma.itt.seip.tenant.step;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.itt.seip.tenant.provision.IdpTenantProvisioning;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Idp integration for tenant creation and deletion.
 * <p>
 * Depending on the chosen idp provider and deployed implementations of {@link IdpTenantProvisioning}, the tenants are
 * provisioned accordingly.
 *
 * @author bbanchev
 * @author smustafov
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 3)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 17.6)
public class TenantIdpStep extends AbstractTenantStep {

	public static final String PROVISION = "idpTenantProvisioning";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String KEY_MODEL_ADMINPASS = "tenantadminpass";
	private static final String KEY_MODEL_ADMIN_FIRSTNAME = "tenantadminfn";
	private static final String KEY_MODEL_ADMIN_LASTNAME = "tenantadminln";
	private static final String KEY_MODEL_ADMIN_MAIL = "tenantadminemail";
	private static final String KEY_MODEL_IDP_PROVIDER = "tenantidpprovider";

	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private SecurityConfiguration securityConfiguration;
	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	@ExtensionPoint(PROVISION)
	private Plugins<IdpTenantProvisioning> availableIdpProvisioners;

	@Override
	public String getIdentifier() {
		return "IdpInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		context.setNewTenantAdminPassword(data.getPropertyValue(KEY_MODEL_ADMINPASS, true));
		String tenantId = context.getTenantInfo().getTenantId();
		String idpProvider = data.getPropertyValue(KEY_MODEL_IDP_PROVIDER, true);
		context.setIdpProvider(idpProvider);

		if (!SecurityContext.isDefaultTenant(tenantId)) {
			IdpTenantProvisioning idpTenantProvisioning = retrieveProvisioning(idpProvider);

			try {
				idpTenantProvisioning.provision(buildIdpTenantInfo(data, context));
			} catch (Exception e) {
				idpTenantProvisioning.delete(tenantId);
				throw e;
			}

			data.completedSuccessfully();
		}

		transactionSupport.invokeBiConsumerInNewTx(this::insertConfigurations, context, idpProvider);
		return true;
	}

	private static IdpTenantInfo buildIdpTenantInfo(TenantStepData data, TenantInitializationContext context) {
		IdpTenantInfo tenantInfo = new IdpTenantInfo();
		tenantInfo.setTenantId(context.getTenantInfo().getTenantId());
		tenantInfo.setTenantDisplayName(context.getTenantInfo().getTenantDisplayName());
		tenantInfo.setTenantDescription(context.getTenantInfo().getTenantDescription());
		tenantInfo.setAdminUsername(context.getAdminUser());
		tenantInfo.setAdminPassword(context.getNewTenantAdminPassword());
		// if email is missing set full username - admin@tenantId
		tenantInfo.setAdminMail(data.getPropertyValue(KEY_MODEL_ADMIN_MAIL, context.getAdminUser()));
		tenantInfo.setAdminFirstName(data.getPropertyValue(KEY_MODEL_ADMIN_FIRSTNAME, "System"));
		tenantInfo.setAdminLastName(data.getPropertyValue(KEY_MODEL_ADMIN_LASTNAME, "Administrator"));
		tenantInfo.setIdpVersion(data.getPropertyValue("idpVersion", false));
		return tenantInfo;
	}

	private IdpTenantProvisioning retrieveProvisioning(String idpProvider) {
		return availableIdpProvisioners.select(provisioner -> provisioner.isApplicable(idpProvider)).orElseThrow(
				() -> new EmfRuntimeException(
						"No applicable " + IdpTenantProvisioning.class + " implementation could be found with name "
								+ idpProvider));
	}

	private void insertConfigurations(TenantInitializationContext context, String idpProvider) {
		String tenantId = context.getTenantInfo().getTenantId();
		securityContextManager.initializeTenantContext(tenantId);

		try {
			Configuration adminName = new Configuration(securityConfiguration.getAdminUserName().getName(),
					context.getAdminUser(), tenantId);
			Configuration adminPass = new Configuration(securityConfiguration.getAdminPasswordConfiguration(),
					context.getNewTenantAdminPassword(), tenantId);
			Configuration adminGroup = new Configuration(securityConfiguration.getAdminGroup().getName(),
					"GROUP_" + ResourceService.SYSTEM_ADMIN_GROUP_ID, tenantId);
			Configuration idpProviderConfig = new Configuration(securityConfiguration.getIdpProviderName().getName(),
					idpProvider, tenantId);

			configurationManagement
					.addConfigurations(Arrays.asList(adminName, adminPass, adminGroup, idpProviderConfig));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		TenantInfo tenantInfo = context.getTenantInfo();
		String idpProvider = context.getConfigValue(securityConfiguration.getIdpProviderName().getName());

		if (context.shouldRollback()) {
			try {
				deleteConfigurations(tenantInfo);
			} catch (Exception e) {
				LOGGER.error("Failed to remove configurations", e);
			}
		}

		if (data.isCompleted()) {
			retrieveProvisioning(idpProvider).delete(tenantInfo.getTenantId());
		}
		return true;
	}

	private void deleteConfigurations(TenantInfo tenantInfo) {
		String tenantId = tenantInfo.getTenantId();
		securityContextManager.initializeTenantContext(tenantId);

		try {
			configurationManagement.removeConfiguration(securityConfiguration.getAdminUserName().getName());
			configurationManagement.removeConfiguration(securityConfiguration.getAdminPasswordConfiguration());
			configurationManagement.removeConfiguration(securityConfiguration.getAdminGroup().getName());
			configurationManagement.removeConfiguration(securityConfiguration.getIdpProviderName().getName());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

}
