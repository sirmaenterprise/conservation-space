package com.sirma.sep.email.service;

import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Administration service used for creating/deleting share folder and managing user sharing of the folder between newly
 * created users or removal of users which will be deleted from the mail server.
 *
 * @author g.tsankov
 */
public interface ShareFolderAdministrationService {
	/**
	 * Creates tenant scoped contacts folder. Used for storing mail accounts for the tenant.
	 *
	 * @throws EmailIntegrationException
	 */
	public void createTenantShareFolder() throws EmailIntegrationException;

	/**
	 * Creates a new contact and stores him in the tenant scoped contacts folder. After that the folder is shared to the
	 * grantee.
	 *
	 * @param targetEmail
	 *            mail address of the newly created contact.
	 * @throws EmailIntegrationException
	 *             if a problem occurs with addition of the grantee.
	 */
	public void shareFolderWithUser(String targetEmail) throws EmailIntegrationException;

	/**
	 * Removes a target contact from the share folder.
	 *
	 * @param targetMail
	 *            mail address of the contact to be removed.
	 * @throws EmailIntegrationException
	 *             if a problem occured while removing the contact.
	 */
	public void removeContactFromShareFolder(String targetMail) throws EmailIntegrationException;

	/**
	 * Deletes the tenant scoped share folder. If the folder owner is not in "ACTIVE" status, an exception will be
	 * thrown.
	 *
	 * @throws EmailIntegrationException
	 *             thrown if exception folder owner status is not active status or a problem occured while deleting.
	 */
	public void deleteTenantShareFolder() throws EmailIntegrationException;

	/**
	 * Creates a new contact for the tenant scoped contacts folder, if eligible. This operation is fast because only the
	 * tenant admin is authenticated for each operations. mountShareFolderToUser is a slower operation, because the
	 * target needs to be authenticated.
	 *
	 * @param targetMail
	 *            mail address of the contact to be created.
	 * @throws EmailIntegrationException
	 *             if a problem occurs with contact creation or share rights given.
	 */
	void addContactToShareFolder(String targetMail) throws EmailIntegrationException;

	/**
	 * Grant share rights and mounts the share folder to the target email, if eligible. This is a slow operation because
	 * the target email must be authenticated.
	 *
	 * @param targetMail
	 *            email address of the contact that the folder will be mounted on.
	 *
	 * @throws EmailIntegrationException
	 *             if a problem occurs while mounting.
	 */
	void mountShareFolderToUser(String targetMail) throws EmailIntegrationException;

	/**
	 * Checks if the target mail account has the share folder mounted.
	 *
	 * @param targetMail
	 *            email address of the contact that the folder will be checked
	 * @return <b>true</b> if mounted.
	 * @throws EmailIntegrationException
	 *             if a problem occurs while querying the mail server
	 */
	boolean isShareFolderMounted(String targetMail) throws EmailIntegrationException;
}