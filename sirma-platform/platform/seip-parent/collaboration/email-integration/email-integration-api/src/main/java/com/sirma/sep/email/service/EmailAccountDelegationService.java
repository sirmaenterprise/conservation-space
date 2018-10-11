package com.sirma.sep.email.service;

import java.util.Map;

import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Account Delegation service used to add or remove sendAs delegation rights to email accounts.
 * 
 * @author g.tsankov
 */
public interface EmailAccountDelegationService {

	/**
	 * Modifies target email account delegation rights.
	 * 
	 * @param target
	 *            target email.
	 * @param granteeId
	 *            object Id, used to get email address from.
	 * @param shouldAdd
	 *            true - delegation rights are given, false - delegation rights are removed.
	 * @throws EmailIntegrationException
	 *             If operation was unsuccessful
	 */
	public void modifyAccountDelegationPermission(String target, String granteeId, boolean shouldAdd)
			throws EmailIntegrationException;

	/**
	 * Get email address and display name for external account. If no external account is set default values are
	 * returned
	 * 
	 * @param target
	 *            target email account
	 * @return map containing external account email address and display name
	 * @throws EmailIntegrationException
	 *             thrown if attributes extraction fail
	 */
	public Map<String, String> getEmailAccountAttributes(String target) throws EmailIntegrationException;

}
