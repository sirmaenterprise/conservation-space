package com.sirma.sep.email.service;

import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.FolderInformation;

/**
 * Administration service for zimbra email account folders. Folders are specified by a view, and contain Contacts or
 * Links. Folders can be shared and mounted on between users.
 *
 * @author g.tsankov
 */
public interface FolderAdministrationService {

	/**
	 * Creates a new folder specified by its view and name.
	 *
	 * @param ownerEmail
	 *            folder owner email.
	 * @param folderName
	 *            name of the created folder.
	 * @param view
	 *            view to which the folder will be created.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with folder creation.
	 */
	void createFolder(String ownerEmail, String folderName, String view) throws EmailIntegrationException;

	/**
	 * Gets specified folder by using the folder owner mail and folder name. If no folder is found, empty
	 * {@link FolderInformation} object is returned. Finds first level depth folder by its name.
	 *
	 * @param folderOwnerEmail
	 *            folder owner.
	 * @param folderName
	 *            folder name.
	 * @return {@link FolderInformation} of the retrieved folder.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with folder retrieval.
	 */
	FolderInformation getFolder(String folderOwnerEmail, String folderName) throws EmailIntegrationException;

	/**
	 * Deletes a folder from target mail user.
	 *
	 * @param folderOwnerEmail
	 *            mail address of the folder owner.
	 * @param folderId
	 *            folder id.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with folder deletion.
	 */
	void deleteFolder(String folderOwnerEmail, String folderId) throws EmailIntegrationException;

	/**
	 * Gives share rights to folder from one user to another. A manual mount point must be made
	 *
	 * @param folderId
	 *            id of the folder to be shared.
	 * @param granteeEmail
	 *            email of user which share rights will be granted.
	 * @param folderOwnerEmail
	 *            email folder owner.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with rights delegation.
	 */
	void giveShareRightsToFolder(String folderId, String granteeEmail, String folderOwnerEmail)
			throws EmailIntegrationException;

	/**
	 * Creates a mount point to a shared folder between users, making it visible in both target and grantee users.
	 *
	 * @param granteeEmail
	 *            mail address of the user where the folder will be mounted on.
	 * @param folderOwnerId
	 *            id of the user which is the folder owner.
	 * @param folderName
	 *            name of the folder that is shared.
	 * @param view
	 *            specifies on which view the folder will be mounted on.
	 * @param folderId
	 *            id of the shared folder.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs with mount point creation.
	 */
	void createMountPointForFolder(String granteeEmail, String folderOwnerId, String folderName, String view,
			String folderId) throws EmailIntegrationException;
}
