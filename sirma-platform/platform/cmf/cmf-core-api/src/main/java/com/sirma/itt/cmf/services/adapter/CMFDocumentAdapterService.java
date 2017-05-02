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
	 * The Enum DocumentOperation.
	 */
	enum DocumentOperation {

		/** The lock. */
		LOCK, /** The unlock. */
		UNLOCK, /** The checkout. */
		CHECKOUT, /** The checkin. */
		CHECKIN, /** The cancel checkout. */
		CANCEL_CHECKOUT;
	}

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
	 * The Enum DocumentInfoOperation provides access to current supported info for node.Could be used in conjuction
	 */
	enum DocumentInfoOperation {
		/** The document info. */
		DOCUMENT_INFO, /** The document versions. */
		DOCUMENT_VERSIONS, /** The document workflows. */
		DOCUMENT_WORKFLOWS;
	}

	/**
	 * Checks out the document using the given user. If the user id is <code>null</code> then the currently logged in
	 * user will be used. The method on success must return the descriptor of the working copy.
	 *
	 * @param document
	 *            the document
	 * @param userId
	 *            the user id
	 * @return the map of result properties as descriptor.
	 * @throws DMSException
	 *             the dMS exception
	 */
	FileAndPropertiesDescriptor checkOut(DMSInstance document, String userId) throws DMSException;

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
	 * Deletes the specified file from the dms system.
	 *
	 * @param documentInstance
	 *            the document instance to delete
	 * @return the dms id of the document on success
	 * @throws DMSException
	 *             if some occurs during delete
	 */
	String deleteAttachment(DMSInstance documentInstance) throws DMSException;

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
	 * Moves document in DMS to the new target.On error exception is thrown <br>
	 *
	 * @param src
	 *            the source document to move
	 * @param target
	 *            the target instance to move document in
	 * @return updated node properties as descriptor
	 * @throws DMSException
	 *             on any error
	 */
	FileAndPropertiesDescriptor moveDocument(DMSInstance src, DMSInstance target) throws DMSException;

	/**
	 * Gets the full uri to the given resource for direct access. It is backended in dms by a service secured with user
	 * creditentials, so user should be logged in.
	 *
	 * @param instance
	 *            is the instance to get link to
	 * @return the full uri to a resource
	 * @throws DMSException
	 *             on build uri error
	 */
	String getDocumentDirectAccessURI(DMSInstance instance) throws DMSException;

	/**
	 * Gets the library dms id.
	 *
	 * @return the library dms id
	 */
	String getLibraryDMSId();
}
