package com.sirma.itt.seip.idp.wso2;

import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.idp.exception.IDPClientException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.itt.seip.tenant.provision.IdpTenantProvisioning;
import com.sirma.itt.seip.tenant.step.TenantIdpStep;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * WSO2 IdP tenant provisioning implementation.
 *
 * @author smustafov
 */
@Extension(target = TenantIdpStep.PROVISION, order = 1)
public class WSO2IdpTenantProvisioning implements IdpTenantProvisioning {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String WSO2_IDP_VERSION = "5.0";

	public static final String NAME = "wso2Idp";

	@Inject
	private WSO2IdPStubManagement idpManager;
	@Inject
	private IDPConfiguration idpConfiguration;

	@Override
	public boolean isApplicable(String idpName) {
		return NAME.equals(idpName);
	}

	@Override
	public void provision(IdpTenantInfo tenantInfo) {
		try {
			TenantMgtAdminServiceStub tenantManager = idpManager.getIDPTenantManager();
			if (tenantManager == null) {
				throw new TenantCreationException(
						"Could not contact Identity Provider. Cannot continue with tenant creation!");
			}
			if (!isTenantAvailable(tenantManager, tenantInfo.getTenantId())) {
				initTenantData(tenantManager, tenantInfo);
			} else {
				LOGGER.info("Tenant {} already exists. Going to activate it if not active", tenantInfo.getTenantId());
				tenantManager.activateTenant(tenantInfo.getTenantId());
			}
		} catch (RemoteException | TenantMgtAdminServiceExceptionException | IDPClientException e) {
			throw new TenantCreationException("Error during tenant creation in Identity Provider!", e);
		}
	}

	private static boolean isTenantAvailable(TenantMgtAdminServiceStub tenantManager, String tenantId) {
		try {
			TenantInfoBean bean = tenantManager.getTenant(tenantId);
			return bean != null && bean.getTenantId() > 0;
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			LOGGER.trace("Tenant {} does not exists", tenantId, e);
			return false;
		}
	}

	private void initTenantData(TenantMgtAdminServiceStub tenantManager, IdpTenantInfo tenantInfo)
			throws RemoteException, TenantMgtAdminServiceExceptionException, IDPClientException {
		LOGGER.info("Going to create new tenant {} at {}", tenantInfo.getTenantId(),
				idpConfiguration.getIdpServerURL().get());

		tenantManager.addTenant(createModel(tenantInfo));

		if (!WSO2_IDP_VERSION.equals(tenantInfo.getIdpVersion())) {
			// just a tenant - it is enough
			return;
		}

		String request = MessageFormat.format(idpConfiguration.getIdpStoreTemplate().get(), tenantInfo.getTenantId(),
				idpConfiguration.getIdpServerUserPass().get());
		// add second store
		idpManager.addUserStore(request);
		// update admin role
		String roleId = tenantInfo.getTenantId().toUpperCase() + "/admin";
		idpManager.assignRolePermissions(roleId,
				Arrays.asList(idpConfiguration.getIdpAdminPermissions().get().split(";")));
	}

	private static TenantInfoBean createModel(IdpTenantInfo tenantInfo) {
		TenantInfoBean info = new TenantInfoBean();
		info.setActive(true);
		info.setAdminPassword(tenantInfo.getAdminPassword());
		info.setFirstname(tenantInfo.getAdminFirstName());
		info.setLastname(tenantInfo.getAdminLastName());
		info.setEmail(tenantInfo.getAdminMail());

		String idpVersion = tenantInfo.getIdpVersion();
		boolean useFullUsername = WSO2_IDP_VERSION.equals(idpVersion);
		String adminUsername = useFullUsername ? tenantInfo.getAdminUsername()
				: SecurityUtil.getUserAndTenant(tenantInfo.getAdminUsername()).getFirst();
		info.setAdmin(adminUsername);

		info.setTenantDomain(tenantInfo.getTenantId());
		info.setCreatedDate(Calendar.getInstance());
		return info;
	}

	@Override
	public void delete(String tenantId) {
		TenantMgtAdminServiceStub idpTenantManager = idpManager.getIDPTenantManager();
		try {
			idpTenantManager.deleteTenant(tenantId);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			// Tenant deletion doesn't work because of this -
			// https://wso2.org/jira/browse/CARBON-15208. Maybe some day this warning will stop
			// printing...
			LOGGER.warn("Could not delete tenant {} will try to deactivate it at least", tenantId);
			LOGGER.trace("Could not delete tenant {} will try to deactivate it at least", tenantId, e);

			try {
				idpTenantManager.deactivateTenant(tenantId);
			} catch (RemoteException | TenantMgtAdminServiceExceptionException e1) {
				LOGGER.warn("Could not deactivate tenant {} because of {}.", tenantId, e1.getMessage());
				LOGGER.trace("Could not deactivate tenant {}.", tenantId, e1);
			}
		}
	}

}
