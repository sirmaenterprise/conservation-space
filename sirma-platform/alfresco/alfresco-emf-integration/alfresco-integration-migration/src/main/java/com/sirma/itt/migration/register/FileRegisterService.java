package com.sirma.itt.migration.register;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import com.sirma.itt.migration.constants.MigrationStatus;
import com.sirma.itt.migration.dto.FileRegisterSearchDTO;
import com.sirma.itt.migration.dto.FileRegistryEntry;

/**
 * Service representing the functions of the FileRegister used by Migration
 * Tool.
 *
 * @author BBonev
 */
public interface FileRegisterService {

	/**
	 * Saves the given object to the DB
	 *
	 * @param <E>
	 *            is the object type
	 * @param e
	 *            is the object to save or update
	 * @return the saved instance
	 */
	public <E> E save(E e);

	/**
	 * Finds a record by given CRC code
	 *
	 * @param crcString
	 *            is the code to look for
	 * @return is the found record or <code>null</code> if not found
	 */
	public FileRegistryEntry findByCrc(String crcString);

	/**
	 * Searches for records using the given arguments and optional paging.
	 *
	 * @param args
	 *            are the search argument
	 * @return the updated argument with the results
	 */
	public FileRegisterSearchDTO search(FileRegisterSearchDTO args);

	/**
	 * Changes a files target path with another (ex the file was moved in
	 * alfresco)
	 *
	 * @param src
	 *            is the old target path
	 * @param target
	 *            is the new target path
	 * @return if record in the register was found with the given initial path
	 *         and was updated successfully
	 */
	public boolean fileMoved(String src, String target);

	/**
	 * Changes the state of the given record identified by the CRC code.
	 *
	 * @param crc
	 *            is the target CRC code
	 * @param status
	 *            is the new status to set
	 * @return <code>true</code> if the record was found and successfully
	 *         updated.
	 */
	public boolean changeStatus(String crc, MigrationStatus status);

	/**
	 * Fetches entries identified by the given list of CRC codes
	 *
	 * @param crcs
	 *            is the list of CRC codes to fetch if available
	 * @return the list of found entries, cannot be <code>null</code> but can be
	 *         empty
	 */
	public List<FileRegistryEntry> fetchEntries(List<String> crcs);

	/**
	 * Performs an update on the register table when a file is deleted. If the
	 * file was found in the register the status is changed to
	 * {@link MigrationStatus#NOT_MIGRATED} and the destination path is deleted
	 *
	 * @param nodeRef
	 *            is the reference of the deleted file
	 * @return <code>true</code> if the file was found and updated and
	 *         <code>false</code> if no file found
	 */
	public boolean fileDeleted(NodeRef nodeRef);

	/**
	 * Changes the destination path or name of a file detonated by the given
	 * node reference
	 *
	 * @param oldRef
	 *            is the target node ref
	 * @param destPath
	 *            is the new path to update
	 * @param newName
	 *            is the new name of the file
	 * @return <code>true</code> if the file was found and updated and
	 *         <code>false</code> if no file found
	 */
	public boolean fileMoved(NodeRef oldRef, String destPath, String newName);

	/**
	 * Checks if the given node reference has an entry into the file register
	 * 
	 * @param nodeRef
	 *            is the node reference to check
	 * @return <code>true</code> if the node exists in the file register table
	 *         and <code>false</code> if not.
	 */
	public boolean existsNodeRefEntry(NodeRef nodeRef);
}
