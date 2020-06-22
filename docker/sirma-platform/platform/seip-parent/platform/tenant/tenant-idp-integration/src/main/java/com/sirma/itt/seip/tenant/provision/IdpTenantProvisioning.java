package com.sirma.itt.seip.tenant.provision;

import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.tenant.step.TenantIdpStep;

/**
 * Defines abstract non-idp dependent tenant provisioning actions for creation and deletion. This interface extends
 * {@link Plugin} and implementations should be extensions to the {@link TenantIdpStep#PROVISION} extension point.
 *
 * @author smustafov
 */
public interface IdpTenantProvisioning extends Plugin {

	/**
	 * Checks whether the underlying idp is the provided one.
	 *
	 * @param idpName the name of the idp that is chosen for provision
	 * @return true if the given idp name corresponds to the implementation
	 */
	boolean isApplicable(String idpName);

	/**
	 * Provisions tenant in the underlying idp.
	 *
	 * @param tenantInfo the idp specific tenant info
	 */
	void provision(IdpTenantInfo tenantInfo);

	/**
	 * Deletes tenant from the underlying idp.
	 *
	 * @param tenantId the id of tenant that will be deleted
	 */
	void delete(String tenantId);

}
