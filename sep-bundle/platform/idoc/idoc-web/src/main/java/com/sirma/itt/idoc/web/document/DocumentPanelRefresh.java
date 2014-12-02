package com.sirma.itt.idoc.web.document;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.services.DocumentService;

/**
 * Handles refreshing of the tree location panel when working with idoc.
 * 
 * @author Adrian Mitev
 */
@Named
@RequestScoped
public class DocumentPanelRefresh {

	@Inject
	private DocumentContext documentContext;

	@Inject
	private DocumentService documentService;

	private String id;

	/**
	 * Loads a document instance by id and sets it in the context.
	 */
	public void refresh() {
		DocumentInstance instance = documentService.loadByDbId(id);
		documentContext.setDocumentInstance(instance);
	}

	/**
	 * Getter method for id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

}
