/**
 *
 */
package com.sirma.itt.cmf.services.adapter;

import java.util.Set;

import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Service responsible to provide actions for documents. <br>
 *
 * @author Borislav Banchev
 */
public interface CMFDocumentAdapterService {

	/**
	 * Upload mode to use
	 *
	 * @author bbanchev
	 */
	enum UploadMode {

		/** Dms custom mode. */
		CUSTOM, /** Direct upload. */
		DIRECT;
	}

	/**
	 * Upload new version.
	 *
	 * @param document
	 *            the document
	 * @param descriptor
	 *            the descriptor for uploaded file including the properties to be updated
	 * @return map with the newest properties as descriptor. Properties are not all document properties but reduced set.
	 * @throws DMSException
	 *             on client exception
	 */
	FileAndPropertiesDescriptor uploadNewVersion(DMSInstance document, UploadWrapperDescriptor descriptor)
			throws DMSException;

	/**
	 * Upload to dms a file and add specific properties and aspects. Document is uploaded the its owningInstance.
	 *
	 * @param documentInstance
	 *            the document instance to upload to
	 * @param descriptor
	 *            the descriptor the descriptor for uploaded file including the properties and the correct container.
	 * @param uploadMode
	 * @param aspectsProp
	 *            the aspects (dms specific)
	 * @return map of metadata or null on error
	 * @throws DMSException
	 *             if some occurs
	 */
	FileAndPropertiesDescriptor uploadContent(DMSInstance documentInstance, UploadWrapperDescriptor descriptor,
			Set<String> aspectsProp) throws DMSException;

	/**
	 * Gets the document preview. Document is transformed from alfresco to the desired mimetype, or if mimetypes of
	 * source and target matches, returns the document itself
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param targetMimetype
	 *            is the desired resulting document
	 * @return the document preview
	 * @throws DMSException
	 *             the dMS exception
	 */
	FileDescriptor getDocumentPreview(DmsAware documentInstance, String targetMimetype) throws DMSException;

	/**
	 * Gets the library dms id.
	 *
	 * @return the library dms id
	 */
	String getLibraryDMSId();
}
