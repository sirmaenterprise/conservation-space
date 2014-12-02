package com.sirma.itt.idoc.web.document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.VersionInfo;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Specifies operations on intelligent documents.
 * 
 * @author Adrian Mitev
 */
public interface IntelligentDocumentService {

	/**
	 * Creates a DocumentInstance inside a section of a case but does NOT persist it.
	 * 
	 * @param caseId
	 *            id of the case where the document will be saved.
	 * @param sectionId
	 *            id of the section where the document will be saved.
	 * @param definitionId
	 *            definition of the document.
	 * @return created document instance.
	 */
	DocumentInstance create(Serializable caseId, Serializable sectionId, String definitionId);

	/**
	 * Updates a document content.
	 * 
	 * @param id
	 *            id of the document to save.
	 * @param content
	 *            document content.
	 * @param title
	 *            title of the document
	 * @return the saved document.
	 */
	DocumentInstance save(Serializable id, String content, String title);

	/**
	 * Save.
	 * 
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @param documentId
	 *            the document id
	 * @param content
	 *            the content
	 * @param title
	 *            the title
	 * @return the instance
	 */
	Instance save(Class<? extends Instance> type, Serializable id, Serializable documentId,
			String content,
			String title);

	/**
	 * Saves a document and its content.
	 * 
	 * @param documentInstance
	 *            document instance to save.
	 * @param content
	 *            document content.
	 * @return the saved document.
	 */
	DocumentInstance save(DocumentInstance documentInstance, String content);

	/**
	 * Updates properties of an intelligent document.
	 * 
	 * @param id
	 *            document id.
	 * @param properties
	 *            properties to update.
	 */
	void updateProperties(Serializable id, Map<String, Serializable> properties);

	/**
	 * Loads an intelligent document as DocumentInstance.
	 * 
	 * @param id
	 *            of the section where the document is stored.
	 * @param caseId
	 *            id of the case where the document is located.
	 * @param sectionId
	 *            id of the section where the document is located.
	 * @return fetched {@link DocumentInstance}.
	 */
	public DocumentInstance load(Serializable id, Serializable caseId, Serializable sectionId);

	/**
	 * Loads a document content as String..
	 * 
	 * @param documentInstance
	 *            document instance to save.
	 * @return document content.
	 */
	public String loadContent(DocumentInstance documentInstance);

	/**
	 * Gets the versions of a document.
	 * 
	 * @param documentInstance
	 *            document for which the
	 * @return list with all versions of the document.
	 */
	List<VersionInfo> getVersions(DocumentInstance documentInstance);

	/**
	 * Loads a previous version of the document.
	 * 
	 * @param documentId
	 *            Id of the document for which we are retrieving a previous version.
	 * @param version
	 *            Previous version label/key/number.
	 * @return {@link DocumentInstance} representing the previous verison, or {@code null} if a
	 *         version with this key is not found.
	 */
	DocumentInstance loadVersion(Serializable documentId, String version);

}
