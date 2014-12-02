package com.sirma.itt.cmf.services;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.UploadUnit;
import com.sirma.itt.cmf.exceptions.DmsDocumentException;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.DocumentInfoOperation;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.concurrent.WorkRecoveryLevel;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Specifies operations on documents
 *
 * @author BBonev
 */
public interface DocumentService extends InstanceService<DocumentInstance, DocumentDefinitionRef> {

	/**
	 * Checks in the document contained in the given document instance by the currently logged in
	 * user.
	 *
	 * @param documentInstance
	 *            the document instance
	 * @return the updated properties
	 */
	Map<String, Serializable> checkIn(DocumentInstance documentInstance);

	/**
	 * Checks out and locks the document identified by the given document instance by the currently
	 * logged in user.
	 *
	 * @param documentInstance
	 *            the document instance
	 */
	void checkOut(DocumentInstance documentInstance);

	/**
	 * Checks in the document contained in the given document instance by the given user.
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param userId
	 *            the user id
	 * @return the updated properties
	 */
	Map<String, Serializable> checkIn(DocumentInstance documentInstance, String userId);

	/**
	 * Checks out and locks the document identified by the given document instance by the given
	 * user.
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param userId
	 *            the user id
	 */
	void checkOut(DocumentInstance documentInstance, String userId);

	/**
	 * Cancel check out on the given document instance.
	 *
	 * @param documentInstance
	 *            the document instance
	 */
	void cancelCheckOut(DocumentInstance documentInstance);

	/**
	 * Update document properties.
	 *
	 * @param documentInstance
	 *            the document instance
	 */
	void updateProperties(DocumentInstance documentInstance);

	/**
	 * Gets the document preview.
	 *
	 * @param instance
	 *            the instance
	 * @return the document preview
	 */
	FileDescriptor getDocumentPreview(DocumentInstance instance);

	/**
	 * Deletes the given document from DMS and from CMF. If the document is only a link to the link
	 * will be deleted and not the original document. In that case the document will not be deleted
	 * from DMS.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if successful
	 */
	boolean deleteDocument(DocumentInstance instance);

	/**
	 * Gets the document info.
	 *
	 * @param instance
	 *            the instance
	 * @param info
	 *            the requested info
	 * @return the document info, keyed by the requested operation
	 */
	Map<DocumentInfoOperation, Serializable> getDocumentInfo(DocumentInstance instance,
			Set<DocumentInfoOperation> info);

	/**
	 * Uploads the given list of document instances to DMS all documents are uploaded to the given
	 * container, no matter where they have been assigned to. If the target container is missing
	 * then all documents will be uploaded to their respective containers set in
	 * {@link DocumentInstance#setOwningInstance(Instance)}. The documents could be uploaded in
	 * parallel if desired. The argument parallelUpload does not have an effect if there is only one
	 * document to upload.
	 *
	 * @param targetContainer
	 *            the target container
	 * @param parallelUpload
	 *            the parallel upload
	 * @param instance
	 *            the document instance
	 * @return true, if successful
	 */
	boolean upload(Instance targetContainer, boolean parallelUpload, DocumentInstance... instance);

	/**
	 * Uploads the given list of document instances to DMS all documents are uploaded to the given
	 * container, no matter where they have been assigned to. If the target container is missing
	 * then all documents will be uploaded to their respective containers set in
	 * {@link DocumentInstance#setOwningInstance(Instance)}. The documents could be uploaded in
	 * parallel if desired. The argument parallelUpload does not have an effect if there is only one
	 * document to upload.
	 * 
	 * @param targetContainer
	 *            the target container
	 * @param parallelUpload
	 *            the parallel upload
	 * @param units
	 *            the document instance
	 * @param recoveryLevel
	 *            the recovery level
	 * @return <code>true</code> if all units completed successfully and <code>false</code> when
	 *         even one failed no matter of the recovery level
	 */
	boolean upload(Instance targetContainer, boolean parallelUpload, Collection<UploadUnit> units,
			WorkRecoveryLevel recoveryLevel);

	/**
	 * Locks the given document instance by the current user
	 *
	 * @param documentInstance
	 *            the document instance
	 */
	void lock(DocumentInstance documentInstance);

	/**
	 * Locks the given document instance by the given user
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param userId
	 *            the user id
	 */
	void lock(DocumentInstance documentInstance, String userId);

	/**
	 * Removes the lock on the given document instance
	 *
	 * @param documentInstance
	 *            the document instance
	 */
	void unlock(DocumentInstance documentInstance);

	/**
	 * Removes the lock on the given document instance and saves the changes if required
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param persistChanges
	 *            the persist changes
	 */
	void unlock(DocumentInstance documentInstance, boolean persistChanges);

	/**
	 * Gets a historic version for document instance. The dms location and other properties are
	 * stored as well in the properties map
	 *
	 * @param instance
	 *            is the original instance to get version for
	 * @param version
	 *            is the version requested
	 * @return the requested version or throws exception on error
	 * @throws DmsDocumentException
	 *             on DMS error
	 */
	DocumentInstance getDocumentVersion(DocumentInstance instance, String version)
			throws DmsDocumentException;

	/**
	 * Revert a historic version for document instance. The dms location and other properties are
	 * stored as well in the properties map
	 *
	 * @param instance
	 *            is the original instance to revert version for
	 * @param version
	 *            is the version requested
	 * @return the reverted version or throws exception on error
	 * @throws DmsDocumentException
	 *             on DMS error
	 */
	DocumentInstance revertVersion(DocumentInstance instance, String version)
			throws DmsDocumentException;

	/**
	 * Fetches the location content
	 *
	 * @param doc
	 *            the document to get
	 * @return the stored file
	 */
	File getContent(DocumentInstance doc);

	/**
	 * Fetches the location content
	 *
	 * @param doc
	 *            the document to get
	 * @return the streamed document
	 */
	InputStream getContentStream(DocumentInstance doc);

	@Override
	DocumentInstance save(DocumentInstance instance, Operation operation);

	/**
	 * Retrieve the content of document by directly streaming to the out param
	 *
	 * @param doc
	 *            the document to retrieve
	 * @param out
	 *            the stream to use to write directly
	 * @return the number of bytes directly copied. If {@link java.io.IOException} or any other -1L
	 *         is returned
	 */
	long getContent(DocumentInstance doc, OutputStream out);

	/**
	 * Checks if document can be uploaded into given section. Document can be uploaded if the
	 * section contains not filled document instance or if in the section definition is specified
	 * that the section can have multiple non specified files.
	 *
	 * @param sectionInstance
	 *            the section instance
	 * @return <code>true</code>, if upload into the section is allowed and <code>false</code> if
	 *         the user cannot upload more documents into the section
	 */
	boolean canUploadDocumentInSection(SectionInstance sectionInstance);

	/**
	 * Checks if document can be uploaded into given section. Document can be uploaded if the
	 * section contains not filled document instance or if in the section definition is specified
	 * that the section can have multiple non files of the given attachment type.
	 *
	 * @param sectionInstance
	 *            the section instance
	 * @param attachmentType
	 *            the concrete attachment type to check for
	 * @return <code>true</code>, if upload into the section is allowed and <code>false</code> if
	 *         the user cannot upload more documents into the section
	 */
	boolean canUploadDocumentInSection(SectionInstance sectionInstance, String attachmentType);

	/**
	 * Copy document to new section as link. The document link can be hard (fixed to the current
	 * version regardless further document updates) or soft (any changes to the document should be
	 * seen into the linked document)
	 *
	 * @param src
	 *            the source document to link from
	 * @param targetSection
	 *            the target section of the link
	 * @param isHardLink
	 *            if the link should be hard link
	 * @return true, if successful
	 */
	boolean copyDocumentAsLink(DocumentInstance src, SectionInstance targetSection,
			boolean isHardLink);

	/**
	 * Copy document as new into the given section. Creates a brand new copy of the document into
	 * the given section as it was uploaded by user.
	 *
	 * @param src
	 *            the source document to copy from
	 * @param targetSection
	 *            the target section to copy to
	 * @param newDocumentName
	 *            the new document name
	 * @return true, if successful
	 */
	boolean copyDocumentAsNew(DocumentInstance src, SectionInstance targetSection,
			String newDocumentName);

	/**
	 * Creates the document instance for the given section. The method returns newly created
	 * document instance only If the method {@link #canUploadDocumentInSection(SectionInstance)}
	 * returns <code>true</code>. The created document instance is <b>NOT</b> automatically added to
	 * the section and not persisted. If the result from
	 * {@link #canUploadDocumentInSection(SectionInstance)} is <code>false</code> the the method
	 * will return <code>null</code>. <br>
	 * The method will create instance for the first document definition that does not have a
	 * specific attachment type until his maximum number of copies are not over then will move to
	 * the next definition until no one is left.<br>
	 * <b>NOTE:</b> if in the section is defined a document definition with numberOfCopies equal to
	 * -1 then this method will always return instances for that definition when reached.
	 *
	 * @param sectionInstance
	 *            the target section instance
	 * @return the document instance or <code>null</code>
	 * @see CaseService#canUploadDocumentInSection(SectionInstance)
	 */
	DocumentInstance createDocumentInstance(SectionInstance sectionInstance);

	/**
	 * Creates the document instance for the given section. The method returns newly created
	 * document instance only If the method
	 * {@link #canUploadDocumentInSection(SectionInstance, String)} returns <code>true</code>. The
	 * created document instance is <b>NOT</b> automatically added to the section and not persisted.
	 * If the result from {@link #canUploadDocumentInSection(SectionInstance, String)} is
	 * <code>false</code> the the method will return <code>null</code>.
	 *
	 * @param sectionInstance
	 *            the target section instance
	 * @param attachmentType
	 *            the concrete attachment type to search for
	 * @return the document instance or <code>null</code>
	 * @see CaseService#canUploadDocumentInSection(SectionInstance)
	 */
	DocumentInstance createDocumentInstance(SectionInstance sectionInstance, String attachmentType);

	/**
	 * Gets the allowed documents for the given section instance. The map will contains the number
	 * of allowed remaining document that can be uploaded of the given type.
	 *
	 * @param sectionInstance
	 *            the section instance
	 * @return the allowed documents and their allowed number
	 */
	Map<String, Pair<DocumentDefinitionRef, Integer>> getAllowedDocuments(
			SectionInstance sectionInstance);

	/**
	 * Gets the allowed documents for the given case instance per section. The map will contains the
	 * number of allowed remaining document that can be uploaded of the given type.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @return the allowed documents
	 */
	Map<SectionInstance, Map<String, Pair<DocumentDefinitionRef, Integer>>> getAllowedDocuments(
			CaseInstance caseInstance);

	/**
	 * Gets the defined documents per section that are possible to be uploaded without checking if
	 * document is uploaded or not.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @return the defined documents
	 */
	Map<String, Map<String, Integer>> getDefinedDocuments(CaseInstance caseInstance);

	/**
	 * Move document to other section. The target section could be in other case as well.
	 * <p>
	 * <b>NOTE:BB: </b>In case of moving document to other case for some reason the source case
	 * instance in the move method cannot be updated/saved. so it need to be updated outside/after
	 * the method call.
	 *
	 * @param src
	 *            the source document to move
	 * @param targetSection
	 *            the target section to move the document into
	 * @return true, if successful
	 */
	boolean moveDocument(DocumentInstance src, SectionInstance targetSection);

	/**
	 * Select document instance that is located into the given case instance.
	 *
	 * @param caseDmsId
	 *            the case dms id
	 * @param documentDmsId
	 *            the document dms id
	 * @return the document instance
	 */
	DocumentInstance selectDocumentInstance(String caseDmsId, String documentDmsId);

	/**
	 * Provides access uri to the given document. Currently as this is backend by dms, dms id should
	 * be set for current or histric version.
	 *
	 * @param instance
	 *            is the document to get url for
	 * @return the builded url or empty string on error
	 */
	String getContentURI(DocumentInstance instance);

	/**
	 * Checks if the given document is attached to the given parent instance or not.
	 *
	 * @param parent
	 *            parent instance to check. If parent is passed as <code>null</code> the check will
	 *            be done against the owning instance reference/instance that is present in the
	 *            provided document instance if any
	 * @param child
	 *            the child instance to check
	 * @return true, if is attached. If the parent is resolved to <code>null</code> the method will
	 *         return <code>true</code> again because will consider the document for part of the
	 *         document library
	 */
	boolean isAttached(Instance parent, DocumentInstance child);
}
