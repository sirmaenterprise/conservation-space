package com.sirma.sep.email.service;

import java.util.List;
import java.util.Map;

import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.ContactInformation;

/**
 * Service used to create/delete/manage zimbra contact entities. Contacts need to be attached to a folder entity.
 *
 * @author g.tsankov
 *
 */
public interface ContactsAdministrationService {

	/**
	 * Creates a contact entity and adds it to the specified folder.
	 *
	 * @param contactAttributes
	 *            attributes which are going to be used for contact creation.
	 * @param folderId
	 *            folderId, where the contact is going to be listed in.
	 * @param folderOwnerEmail
	 *            mail address of the folder owner.
	 * @throws EmailIntegrationException
	 *             if problem with contact creation occurs.
	 */
	void createContact(Map<String, String> contactAttributes, String folderId, String folderOwnerEmail)
			throws EmailIntegrationException;

	/**
	 * Removes a contact entity from the target user account.
	 *
	 * @param contactId
	 *            id of the contact to be removed.
	 * @param contactOwnerMail
	 *            email address of the user who owns the contact to be removed.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with contact removal.
	 */
	void removeContactFromFolder(String contactId, String contactOwnerMail) throws EmailIntegrationException;

	/**
	 * Executes a specific operation(specified within the zimbra docs) on a contact. Operations that may be executed
	 * are: move|delete|flag|trash|tag|update
	 *
	 * Note: trash is soft deletion, meaning moving the contact to the trash.
	 *
	 * @param contactId
	 *            id of the contact that the operation will be executed on.
	 * @param operation
	 *            string representation of the operation.
	 * @param contactOwnerMail
	 *            mail address of the contact owner.
	 * @throws EmailIntegrationException
	 *             thrown if operation is unsuccessful.
	 */
	void executeContactOperation(String contactId, String operation, String contactOwnerMail)
			throws EmailIntegrationException;

	/**
	 * Gets {@link ContactInformation} based on user email in the contact, name of the folder that is searched and name
	 * of the folder that is searched in.
	 *
	 * @param folderOwnerEmail
	 *            owner of the contacts folder.
	 * @param searchedEmail
	 *            contact mail address that will be searched for.
	 * @param folderId
	 *            id of the folder that will be searched.
	 * @return {@link ContactInformation } of the searched contact or empty if none is found.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with contact quering.
	 */
	ContactInformation getContact(String folderOwnerEmail, String searchedEmail, String folderId)
			throws EmailIntegrationException;

	/**
	 * Returns a list of {@link ContactInformation} of all the contacts within the searched folder.
	 *
	 * @param folderOwnerEmail
	 *            email of the owner of the searched folder
	 * @param folderId
	 *            id of the folder that will be searched
	 * @return list of all contacts
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with contacts retrieval
	 */
	List<ContactInformation> getAllFolderContacts(String folderOwnerEmail, String folderId)
			throws EmailIntegrationException;

}
