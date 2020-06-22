package com.sirma.itt.emf.adapter;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The Interface DMSInstanceAdapterService is the adapter that provides general logic and could be used on any model if
 * applicable
 */
public interface DMSInstanceAdapterService {

	/**
	 * Updates given node with given properties
	 *
	 * @param dmsInstance
	 *            is the instance to be updated. If this is not instanceof
	 *            {@link com.sirma.itt.seip.domain.instance.EmfInstance} or dms id is not valid exception is thrown. New
	 *            properties should in the properties map
	 * @return the map with newest set properties converted back from DMS
	 * @throws DMSException
	 *             the dms exception with specific error
	 */
	Map<String, Serializable> updateNode(DMSInstance dmsInstance) throws DMSException;

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
	 *             the dMS exception {@link com.sirma.itt.seip.content.desciptor.beans.LocalFileDescriptor}
	 */
	FileAndPropertiesDescriptor attachDocumentToInstance(DMSInstance documentInstance, FileDescriptor descriptor,
			String customAspect) throws DMSException;

	/**
	 * Upload the given descriptor to DMS system and return specific properties from it. <br>
	 * The method does not upload the document to a specific parent folder but to the abstract document library in the
	 * DMS system.
	 *
	 * @param documentInstance
	 *            is the instance to attach document to
	 * @param descriptor
	 *            the descriptor for uploaded file.
	 * @param customAspect
	 *            is the type of file ( structured, common, view, etc...)
	 * @return the map of properties for this node as descriptor
	 * @throws DMSException
	 *             the dMS exception {@link com.sirma.itt.seip.content.desciptor.beans.LocalFileDescriptor}
	 */
	FileAndPropertiesDescriptor attachDocumenToLibrary(DMSInstance documentInstance, FileDescriptor descriptor,
			String customAspect) throws DMSException;

	/**
	 * Delete given node by instance
	 *
	 * @param dmsInstance
	 *            is the instance to be deleted. If this is not instanceof
	 *            {@link com.sirma.itt.seip.domain.instance.EmfInstance} or dms id is not valid exception is thrown. New
	 *            properties should in the properties map
	 * @return the map with newest set properties converted back from DMS
	 * @throws DMSException
	 *             the dms exception with specific error
	 */
	boolean deleteNode(DMSInstance dmsInstance) throws DMSException;
}
