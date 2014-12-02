package com.sirma.itt.emf.adapter;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.instance.model.DMSInstance;

/**
 * The Interface DMSInstanceAdapterService is the adapter that provides general logic and could be
 * used on any model if applicable
 */
public interface DMSInstanceAdapterService extends CMFAdapterService {

	/**
	 * Updates given node with given properties
	 *
	 * @param dmsInstance
	 *            is the instance to be updated. If this is not instanceof
	 *            {@link com.sirma.itt.emf.instance.model.EmfInstance} or dms id is not valid
	 *            exception is thrown. New properties should in the properties map
	 * @return the map with newest set properties converted back from DMS
	 * @throws DMSException
	 *             the dms exception with specific error
	 */
	Map<String, Serializable> updateNode(DMSInstance dmsInstance) throws DMSException;

	/**
	 * Links a instance to another with the parent/child assoc type. The assocName could customize
	 * the association name. The method allows duplicate definitions and should be paid attention
	 * for association request leading to duplicates.
	 *
	 * @param parent
	 *            is instance with the parent role
	 * @param child
	 *            is instance with the child role
	 * @param parentToUnlink
	 *            is optional param to remove old association to this parent
	 * @param assocName
	 *            customized association name. could be null
	 * @return true on success
	 * @throws DMSException
	 *             on any error or if nodes are not linked.
	 */
	boolean linkAsChild(DMSInstance parent, DMSInstance child, DMSInstance parentToUnlink,
			String assocName) throws DMSException;

	/**
	 * Removes a link between instance to another with the parent/child association type. The method
	 * removes all associations with the provided name if any. If no name is provided all
	 * associations will be removed between the two instances.
	 *
	 * @param parent
	 *            is instance with the parent role
	 * @param child
	 *            is instance with the child role
	 * @param assocName
	 *            customized association name. could be null
	 * @return true on success
	 * @throws DMSException
	 *             on any error or if nodes are not linked.
	 */
	boolean removeLinkAsChild(DMSInstance parent, DMSInstance child, String assocName)
			throws DMSException;

	/**
	 * Creates the child as folder in dms for the gicen parent dms instance.
	 *
	 * @param parent
	 *            is the instance to attach/create folder to
	 * @param child
	 *            the descriptor for created folder
	 * @return the child with populated dmsId
	 * @throws DMSException
	 *             the dMS exception {@link com.sirma.itt.cmf.beans.LocalFileDescriptor}
	 */
	DMSInstance attachFolderToInstance(DMSInstance parent, DMSInstance child) throws DMSException;

	/**
	 * Upload the given descriptor to DMS system and return specific properties from it. <br>
	 *
	 * @param documentInstance
	 *            is the instance to attach document to
	 * @param descriptor
	 *            the descriptor for uploaded file.
	 * @param customAspect
	 *            is the type of file ( structured, common, view, etc...)
	 * @return the map of properties for this node as descriptor
	 * @throws DMSException
	 *             the dMS exception {@link com.sirma.itt.cmf.beans.LocalFileDescriptor}
	 */
	FileAndPropertiesDescriptor attachDocumentToInstance(DMSInstance documentInstance,
			FileDescriptor descriptor, String customAspect) throws DMSException;

	/**
	 * Delete a document from the parent instance
	 *
	 * @param documentInstance
	 *            is the document to delete
	 * @return the dms id of document on success
	 * @throws DMSException
	 *             the dMS exception of any kind
	 */
	String dettachDocumentFromInstance(DMSInstance documentInstance) throws DMSException;

	/**
	 * Upload the given descriptor to DMS system and return specific properties from it. <br>
	 * The method does not upload the document to a specific parent folder but to the abstract
	 * document library in the DMS system.
	 *
	 * @param documentInstance
	 *            is the instance to attach document to
	 * @param descriptor
	 *            the descriptor for uploaded file.
	 * @param customAspect
	 *            is the type of file ( structured, common, view, etc...)
	 * @return the map of properties for this node as descriptor
	 * @throws DMSException
	 *             the dMS exception {@link com.sirma.itt.cmf.beans.LocalFileDescriptor}
	 */
	FileAndPropertiesDescriptor attachDocumenToLibrary(DMSInstance documentInstance,
			FileDescriptor descriptor, String customAspect) throws DMSException;

	/**
	 * Delete given node by instance
	 * 
	 * @param dmsInstance
	 *            is the instance to be deleted. If this is not instanceof
	 *            {@link com.sirma.itt.emf.instance.model.EmfInstance} or dms id is not valid
	 *            exception is thrown. New properties should in the properties map
	 * @return the map with newest set properties converted back from DMS
	 * @throws DMSException
	 *             the dms exception with specific error
	 */
	boolean deleteNode(DMSInstance dmsInstance) throws DMSException;
}
