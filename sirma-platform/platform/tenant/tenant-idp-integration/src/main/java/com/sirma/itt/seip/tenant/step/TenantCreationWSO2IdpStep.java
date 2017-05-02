package com.sirma.itt.seip.tenant.step;

import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.idp.exception.IDPClientException;
import com.sirma.itt.seip.idp.wso2.WSO2IdPStubManagment;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * WSO2 Idp integration for tenant creation. The tenant admin data and base inforamtion is sent.
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 3)
public class TenantCreationWSO2IdpStep extends AbstractTenantCreationStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String KEY_MODEL_ADMINPASS = "tenantadminpass";
	private static final String KEY_MODEL_ADMIN_FIRSTNAME = "tenantadminfn";
	private static final String KEY_MODEL_ADMIN_LASTNAME = "tenantadminln";
	private static final String KEY_MODEL_ADMIN_MAIL = "tenantadminemail";

	@Inject
	private WSO2IdPStubManagment idpManager;
	@Inject
	private IDPConfiguration idpConfiguration;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private SecurityConfiguration securityConfiguration;
	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public String getIdentifier() {
		return "IdpInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		context.setNewTenantAdminPassword(data.getPropertyValue(KEY_MODEL_ADMINPASS, true));
		String tenantId = context.getTenantInfo().getTenantId();
		if (!SecurityContext.isDefaultTenant(tenantId)) {
			try {
				TenantMgtAdminServiceStub tenantManager = idpManager.getIDPTenantManager();
				if (tenantManager == null) {
					throw new TenantCreationException(
							"Could not contact Identity Provider. Cannot continue with tenant creation!");
				}
				if (!isTenantAvailable(tenantManager, context.getTenantInfo())) {
					initTenantData(data, context, tenantId, tenantManager);
				} else {
					LOGGER.info("Tenant {} already exists. Going to activate it if not active", tenantId);
					tenantManager.activateTenant(tenantId);
				}
				data.completedSuccessfully();
			} catch (RemoteException | TenantMgtAdminServiceExceptionException | IDPClientException e) {
				throw new TenantCreationException("Error during tenant creation in Identity Provider!", e);
			}
		}
		transactionSupport.invokeConsumerInNewTx(this::insertConfigurations, context);
		return true;
	}

	private void initTenantData(TenantStepData data, TenantInitializationContext context, String tenantId,
			TenantMgtAdminServiceStub tenantManager)
					throws RemoteException, TenantMgtAdminServiceExceptionException, IDPClientException {
		String idpVersion = data.getPropertyValue("idpVersion", false);
		LOGGER.info("Going to create new tenant {} at {}", tenantId, idpConfiguration.getIdpServerURL().get());
		tenantManager.addTenant(createModel(data, context));
		if (!"5.0".equals(idpVersion)) {
			// just a tenant - it is enough
			return;
		}
		String request = MessageFormat.format(idpConfiguration.getIdpStoreTemplate().get(), tenantId,
				idpConfiguration.getIdpServerUserPass().get());
		// add second store
		idpManager.addUserStore(request);
		// update admin role
		String roleId = tenantId.toUpperCase() + "/admin";
		idpManager.assignRolePermissions(roleId,
				Arrays.asList(idpConfiguration.getIdpAdminPermissions().get().split(";")));
	}

	private static boolean isTenantAvailable(TenantMgtAdminServiceStub tenantManager, TenantInfo tenantInfo) {
		try {
			TenantInfoBean bean = tenantManager.getTenant(tenantInfo.getTenantId());
			return bean != null && bean.getTenantId() > 0;
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			LOGGER.trace("Tenant {} does not exists", tenantInfo, e);
			return false;
		}
	}

	private static TenantInfoBean createModel(TenantStepData data, TenantInitializationContext context) {
		TenantInfoBean info = new TenantInfoBean();
		info.setActive(true);
		info.setAdminPassword(data.getPropertyValue(KEY_MODEL_ADMINPASS, true));
		info.setFirstname(data.getPropertyValue(KEY_MODEL_ADMIN_FIRSTNAME, false));

		info.setLastname(data.getPropertyValue(KEY_MODEL_ADMIN_LASTNAME, false));
		if (info.getFirstname() == null) {
			info.setFirstname(info.getLastname() == null ? context.getTenantInfo().getTenantId() : "Administrator");
		}
		if (info.getLastname() == null) {
			info.setLastname("Administrator");
		}

		String adminMail = data.getPropertyValue(KEY_MODEL_ADMIN_MAIL, false);
		adminMail = adminMail == null ? context.getAdminUser() : adminMail;
		info.setEmail(adminMail);
		// define whether to use admin@tenant or admin - this is used for idp
		// compatibility
		String idpVersion = data.getPropertyValue("idpVersion", false);
		boolean useFullUsername = "5.0".equals(idpVersion);
		String adminUsername = useFullUsername ? context.getAdminUser()
				: SecurityUtil.getUserAndTenant(context.getAdminUser()).getFirst();
		info.setAdmin(adminUsername);
		info.setTenantDomain(context.getTenantInfo().getTenantId());
		info.setCreatedDate(Calendar.getInstance());
		return info;
	}

	void insertConfigurations(TenantInitializationContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		securityContextManager.initializeTenantContext(tenantId);
		try {
			Configuration adminName = new Configuration(securityConfiguration.getAdminUserName().getName(),
					context.getAdminUser(), tenantId);
			Configuration adminPass = new Configuration(securityConfiguration.getAdminPasswordConfiguration(),
					context.getNewTenantAdminPassword(), tenantId);
			Configuration adminGroup = new Configuration(securityConfiguration.getAdminGroup().getName(),
					"GROUP_ALFRESCO_ADMINISTRATORS", tenantId);
			configurationManagement.addConfigurations(Arrays.asList(adminName, adminPass, adminGroup));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	@Override
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		try {
			deleteConfigurations(context);
		} catch (Exception e) {
			LOGGER.warn("Failed to remove configurations", e);
		}
		if (data.isCompleted()) {
			TenantMgtAdminServiceStub idpTenantManager = idpManager.getIDPTenantManager();
			try {
				idpTenantManager.deleteTenant(context.getTenantInfo().getTenantId());
			} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
				LOGGER.error("Could not delete tenant {} will try to deactivate it at least",
						context.getTenantInfo().getTenantId(), e);
				try {
					idpTenantManager.deactivateTenant(context.getTenantInfo().getTenantId());
				} catch (RemoteException | TenantMgtAdminServiceExceptionException e1) {
					LOGGER.warn("Could not deactivate tenant {}", context.getTenantInfo().getTenantId(), e1);
				}
			}
		}
		return true;
	}

	private void deleteConfigurations(TenantInitializationContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		securityContextManager.initializeTenantContext(tenantId);
		try {
			configurationManagement.removeConfiguration(securityConfiguration.getAdminUserName().getName());
			configurationManagement.removeConfiguration(securityConfiguration.getAdminPasswordConfiguration());
			configurationManagement.removeConfiguration(securityConfiguration.getAdminGroup().getName());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

}
