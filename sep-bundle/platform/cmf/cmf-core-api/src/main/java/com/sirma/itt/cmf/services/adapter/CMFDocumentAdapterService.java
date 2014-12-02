/**
 *
 */
package com.sirma.itt.cmf.services.adapter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.DMSInstance;

/**
 * Service responsible to provide actions for documents. <br>
 *
 * @author Borislav Banchev
 */
public interface CMFDocumentAdapterService {

	/**
	 * The Enum DocumentOperation.
	 */
	public enum DocumentOperation {

		/** The lock. */
		LOCK,
		/** The unlock. */
		UNLOCK,
		/** The checkout. */
		CHECKOUT,
		/** The checkin. */
		CHECKIN,
		/** The cancel checkout. */
		CANCEL_CHECKOUT;
	}

	/**
	 * The Enum DocumentInfoOperation provides access to current supported info for node.Could be
	 * used in conjuction
	 */
	public enum DocumentInfoOperation {
		/** The document info. */
		DOCUMENT_INFO,
		/** The document versions. */
		DOCUMENT_VERSIONS,
		/** The document workflows. */
		DOCUMENT_WORKFLOWS;
	}

	/**
	 * Perform document info operation.
	 *
	 * @param document
	 *            the document
	 * @param operations
	 *            which info operations are required
	 * @return the map of properties for node
	 * @throws DMSException
	 *             the dMS exception
	 */
	public Map<DocumentInfoOperation, Serializable> performDocumentInfoOperation(
			DocumentInstance document, Set<DocumentInfoOperation> operations) throws DMSException;

	/**
	 * Perform document operation. Each operation might need specific metadata.
	 *
	 * @param document
	 *            the document
	 * @param operation
	 *            the operation
	 * @return the map of properties for node as descriptor.
	 * @throws DMSException
	 *             the dMS exception
	 */
	public FileAndPropertiesDescriptor performDocumentOperation(DocumentInstance document,
			DocumentOperation operation) throws DMSException;

	/**
	 * Perform document operation using the given user ID. Each operation might need specific
	 * metadata.
	 *
	 * @param document
	 *            the document
	 * @param userId
	 *            the user id
	 * @param operation
	 *            the operation
	 * @return the map of properties for node as descriptor.
	 * @throws DMSException
	 *             the dMS exception
	 */
	public FileAndPropertiesDescriptor performDocumentOperation(DocumentInstance document,
			String userId, DocumentOperation operation) throws DMSException;

	/**
	 * Checks out the document using the given user. If the user id is <code>null</code> then the
	 * currently logged in user will be used. The method on success must return the descriptor of
	 * the working copy.
	 *
	 * @param document
	 *            the document
	 * @param userId
	 *            the user id
	 * @return the map of result properties as descriptor.
	 * @throws DMSException
	 *             the dMS exception
	 */
	public FileAndPropertiesDescriptor checkOut(DocumentInstance document, String userId)
			throws DMSException;

	/**
	 * Upload new version.
	 *
	 * @param document
	 *            the document
	 * @param descriptor
	 *            the descriptor for uploaded file including the properties to be updated
	 * @return map with the newest properties as descriptor. Properties are not all document
	 *         properties but reduced set.
	 * @throws DMSException
	 *             on client exception
	 */
	public FileAndPropertiesDescriptor uploadNewVersion(DocumentInstance document,
			FileAndPropertiesDescriptor descriptor) throws DMSException;

	/**
	 * Upload to dms a file and add specific properties and aspects. Document is uploaded the its
	 * owningInstance.
	 *
	 * @param documentInstance
	 *            the document instance to upload to
	 * @param descriptor
	 *            the descriptor the descriptor for uploaded file including the properties and the
	 *            correct container.
	 * @param aspectsProp
	 *            the aspects (dms specific)
	 * @return map of metadata or null on error
	 * @throws DMSException
	 *             if some occurs
	 */
	public FileAndPropertiesDescriptor uploadContent(DocumentInstance documentInstance,
			FileAndPropertiesDescriptor descriptor, Set<String> aspectsProp) throws DMSException;

	/**
	 * Deletes the specified file from the dms system.
	 *
	 * @param documentInstance
	 *            the document instance to delete
	 * @return the dms id of the document on success
	 * @throws DMSException
	 *             if some occurs during delete
	 */
	public String deleteAttachment(DocumentInstance documentInstance) throws DMSException;

	/**
	 * Gets the document preview. Document is transformed from alfresco to the desired mimetype, or
	 * if mimetypes of source and target matches, returns the document itself
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param targetMimetype
	 *            is the desired resulting document
	 * @return the document preview
	 * @throws DMSException
	 *             the dMS exception
	 */
	public FileDescriptor getDocumentPreview(DocumentInstance documentInstance,
			String targetMimetype) throws DMSException;

	/**
	 * Gets a historic version for document instance. The dms location and other properties are
	 * stored as well in the properties map
	 *
	 * @param instance
	 *            is the original instance to get version for
	 * @param version
	 *            is the version requested
	 * @return the requested version or throws exception on error
	 */
	public DocumentInstance getDocumentVersion(DocumentInstance instance, String version)
			throws DMSException;

	/**
	 * Revert a historic version for document instance. The dms location and other properties are
	 * stored as well in the properties map
	 *
	 * @param instance
	 *            is the original instance to revert version for
	 * @param version
	 *            is the version requested
	 * @return the reverted version or throws exception on error
	 */
	public DocumentInstance revertVersion(DocumentInstance instance, String version)
			throws DMSException;

	/**
	 * Moves document in DMS to the new section.On error exception is thrown <br>
	 *
	 * @param src
	 *            the source document to move
	 * @param targetSection
	 *            the target section to move document in
	 * @return updated node properties as descriptor
	 * @throws DMSException
	 *             on any error
	 */
	public FileAndPropertiesDescriptor moveDocument(DocumentInstance src,
			DMSInstance targetSection) throws DMSException;

	/**
	 * Copies document in DMS to the new section.On error exception is thrown <br>
	 *
	 * @param src
	 *            the source document to move
	 * @param targetSection
	 *            the target instance to copy document in
	 * @param newDocumentName
	 *            the new document name
	 * @return updated node properties as descriptor
	 * @throws DMSException
	 *             on any error
	 */
	public FileAndPropertiesDescriptor copyDocument(DocumentInstance src,
			DMSInstance targetSection, String newDocumentName) throws DMSException;

	/**
	 * Gets the full uri to the given resource for direct access. It is backended in dms by a
	 * service secured with user creditentials, so user should be logged in.
	 *
	 * @param instance
	 *            is the instance to get link to
	 * @return the full uri to a resource
	 * @throws DMSException
	 *             on build uri error
	 */
	public String getDocumentDirectAccessURI(DocumentInstance instance) throws DMSException;
}
