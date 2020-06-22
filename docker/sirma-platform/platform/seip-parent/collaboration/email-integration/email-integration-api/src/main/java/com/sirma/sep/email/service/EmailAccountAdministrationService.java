package com.sirma.sep.email.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.sep.email.exception.EmailAccountCreationException;
import com.sirma.sep.email.exception.EmailAccountDeleteException;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.GenericAttribute;

/**
 * Email account administration service interface used to common administration purposes.
 *
 * @author g.tsankov
 */
public interface EmailAccountAdministrationService extends Serializable {

	/**
	 * Creates an account with the desired accountName and password.
	 *
	 * @param accountName
	 *            full account name that will be used to log in into the service. (e.g: testAccount@testDomain.com)
	 * @param password
	 *            password that will be used to log in to the account
	 * @param attributes
	 *            specific account creation attributes (like displayName, sn, etc.)
	 * @return returned {@link EmailAccountInformation} populated with the created account info.
	 * @throws EmailAccountCreationException
	 *             thrown if account creation fails.
	 */
	EmailAccountInformation createAccount(String accountName, String password, Map<String, String> attributes)
			throws EmailAccountCreationException;

	/**
	 * Creates an account with admin rights. This account is used only from the system. Such account is needed for
	 * execution of all SOAP methods which needs authentication. Like create, delete and modify account.
	 *
	 * @param accountName
	 *            full account name that will be used to log in into the service. (e.g: testAccount@testDomain.com)
	 * @param password
	 *            password that will be used to log in to the account
	 * @return returned {@link EmailAccountInformation} populated with the created account info.
	 * @throws EmailAccountCreationException
	 *             thrown if account creation fails.
	 */
	EmailAccountInformation createTenantAdminAccount(String accountName, String password)
			throws EmailAccountCreationException;

	/**
	 * Deletes an account from the mail server.
	 *
	 * @param accountId
	 *            id of the account.
	 * @throws EmailAccountDeleteException
	 *             thrown if deletion fails.
	 */
	void deleteAccount(String accountId) throws EmailAccountDeleteException;

	/**
	 * Renames the account.
	 *
	 * @param accountId
	 *            id of the account to be renamed.
	 * @param newName
	 *            new name to be set.
	 * @throws EmailIntegrationException
	 *             thrown if rename fails.
	 */
	void renameAccount(String accountId, String newName) throws EmailIntegrationException;

	/**
	 * Grants or revokes delegate "SendAs" permission to target email.
	 *
	 * @param target
	 *            target email account that will be modified.
	 * @param grantee
	 *            email account that will be added or removed as SendAs delegate.
	 * @param modifyFlag
	 *            adds grantee if true, removes otherwise.
	 * @throws EmailIntegrationException
	 *             thrown if delegate permission fails
	 */
	void modifyDelegatePermission(String target, String grantee, boolean modifyFlag) throws EmailIntegrationException;

	/**
	 * Modifies an account with a list of attribute objects.
	 *
	 * @param accountId
	 *            account Id to be modified.
	 * @param attributes
	 *            list of attributes to modify the account.
	 * @return {@link EmailAccountInformation} object containing the updated information for the account.
	 * @throws EmailIntegrationException
	 *             if failed to communicate with the remote server to modify the account
	 */
	EmailAccountInformation modifyAccount(String accountId, List<GenericAttribute> attributes)
			throws EmailIntegrationException;

	/**
	 * Disables email account, by making it inactive and unable to receive new e-mails.
	 *
	 * @param accountId
	 *            id of the email account to be disabled.
	 * @throws EmailIntegrationException
	 *             thrown if disable fails.
	 */
	void disableAccount(String accountId) throws EmailIntegrationException;

	/**
	 * Gets a specific account information by id. Gets a full list of the account attributes.
	 *
	 * @param accountName
	 *            account name.
	 * @return account information.
	 * @throws EmailIntegrationException
	 *             thrown if account retrieval fails
	 */
	EmailAccountInformation getAccount(String accountName) throws EmailIntegrationException;

	/**
	 * Gets a specific account information by id. Gets a set list of the account attributes. If null, gets full list of
	 * account attributes.
	 *
	 * @param accountName
	 *            account name.
	 * @param accountAttributes
	 *            list of attributes to be queried. If null, gets all of the account attributes.
	 * @return account information.
	 * @throws EmailIntegrationException
	 *             thrown if account retrieval fails
	 */
	EmailAccountInformation getAccount(String accountName, List<String> accountAttributes)
			throws EmailIntegrationException;

	/**
	 * Gets a list of all accounts on the server.
	 *
	 * @return list of accounts.
	 * @throws EmailIntegrationException
	 *             thrown if account retrieval fails.
	 */
	List<EmailAccountInformation> getAllAccounts() throws EmailIntegrationException;

	/**
	 * Gets the email account white list.
	 *
	 * @param emailName
	 *            email account name
	 * @param emailPassword
	 *            email account password.
	 * @return list of white listed email account names.
	 * @throws EmailIntegrationException
	 *             thrown if whitelist retrieval fails.
	 */
	List<String> getWhiteList(String emailName, String emailPassword) throws EmailIntegrationException;

	/**
	 * Modifies email account white list by adding or removing a list of email addresses. Operation must be set (+ for
	 * addition, - for removal).
	 *
	 * @param emailName
	 *            email account name whose whitelist will be modified.
	 * @param password
	 *            email account password whose whitelist will be modified
	 * @param emailAdresses
	 *            list of email addresses to be added or removed.
	 * @param operation
	 *            operation that will be executed on the list.
	 * @throws EmailIntegrationException
	 *             thrown if notification fails
	 */
	void modifyWhiteList(String emailName, String password, List<String> emailAdresses, String operation)
			throws EmailIntegrationException;

	/**
	 * Modifies admin account giving access to some admin views
	 *
	 * @param target
	 *            admin email account
	 * @throws EmailIntegrationException
	 *             thrown if account modification fails
	 */
	void modifyAdminAccount(String target) throws EmailIntegrationException;

	/**
	 * Grants admin rights to manage domains
	 *
	 * @param grantee
	 *            admin email account
	 * @throws EmailIntegrationException
	 *             thrown if admin domain rights can not be granted
	 */
	void grantAdminDomainRights(String grantee) throws EmailIntegrationException;

	/**
	 * Grants admin rights to manage accounts
	 *
	 * @param target
	 *            admin email account
	 * @throws EmailIntegrationException
	 *             thrown if admin account rights can not be granted
	 */
	void grantAdminAccountRights(String target) throws EmailIntegrationException;

}
