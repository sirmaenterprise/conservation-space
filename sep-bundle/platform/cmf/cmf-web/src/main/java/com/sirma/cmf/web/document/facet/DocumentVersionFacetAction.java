package com.sirma.cmf.web.document.facet;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.DocumentInfoOperation;

/**
 * DocumentVersionFacetAction backing bean.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class DocumentVersionFacetAction extends Action implements Serializable {

	/** The serial version identifier. */
	private static final long serialVersionUID = 5605745101954523977L;

	/**  Document service for retrieving needed document information. */
	@Inject
	private DocumentService documentService;

	/** The list with document versions. */
	private List<Serializable> documentVersions;

	/**
	 * Gets versions for selected document.
	 * 
	 * @param documentInstance
	 *            the {@link DocumentInstance}
	 */
	public void loadDocumentVersions(DocumentInstance documentInstance) {

		log.debug("CMFWeb: Executing DocumentVersionFacetAction.loadDocumentVersions");

		if (documentInstance == null) {

			documentVersions = Collections.emptyList();

		} else {

			Set<DocumentInfoOperation> info = new HashSet<DocumentInfoOperation>();
			info.add(DocumentInfoOperation.DOCUMENT_VERSIONS);

			Map<DocumentInfoOperation, Serializable> infosMap = documentService.getDocumentInfo(
					documentInstance, info);

			List<Serializable> serializableInfoList = new LinkedList<Serializable>();
			for (Serializable serial : infosMap.values()) {
				if (serial instanceof List) {
					List<?> aList = (List<?>) serial;
					for (Object object : aList) {
						if (object instanceof Serializable) {
							serializableInfoList.add((Serializable) object);
						}
					}
				}
			}

			documentVersions = serializableInfoList;
		}

		log.debug("CMFWeb: found [" + documentVersions.size() + "] document versions");
	}

	/**
	 * Getter method for documentVersions.
	 * 
	 * @return the documentVersions
	 */
	public List<Serializable> getDocumentVersions() {
		DocumentInstance documentInstance = getDocumentContext().getDocumentInstance();
		if(documentInstance != null){
			loadDocumentVersions(documentInstance);
		}
		return documentVersions;
	}

	/**
	 * Setter method for documentVersions.
	 * 
	 * @param documentVersions
	 *            the documentVersions to set
	 */
	public void setDocumentVersions(List<Serializable> documentVersions) {
		this.documentVersions = documentVersions;
	}
}
