package com.sirma.cmf.web.digitalsign;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * The DocumentSignAction is backend bean to populate model and handle requests.
 */
@Named
@ViewAccessScoped
public class DocumentSignAction extends EntityAction implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSignAction.class);
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4528618475350663238L;

	/** The source token id. */
	private String sourceTokenId;

	/** The signed token id. */
	private String signedTokenId;

	/** The ues button id. */
	private String uesButtonId;

	/** The document url. */
	private String documentURL;

	/**
	 * Getter method for sourceTokenId.
	 *
	 * @return the sourceTokenId
	 */
	public String getSourceTokenId() {
		return sourceTokenId;
	}

	/**
	 * Inits the.
	 *
	 * @param instance
	 *            the instance
	 * @return the document sign action
	 */
	public DocumentSignAction init(Serializable instance) {

		if (instance instanceof DocumentInstance) {
			sourceTokenId = ((DocumentInstance) instance).getDmsId();
		}
		return this;
	}

	/**
	 * Download document action observer.
	 *
	 * @param event
	 *            Event payload object.
	 */
	public void sign(
			@Observes @EMFAction(value = ActionTypeConstants.SIGN, target = DocumentInstance.class) final EMFActionEvent event) {
		log.debug("CMFWeb: Executing observer DocumentSignAction.sign");
	}

	/**
	 * Refresh case instance and navigate to same page with reloaded context.
	 *
	 * @param instance
	 *            is the current instance to reload
	 * @return the desired navigation page
	 */
	public String reload(DocumentInstance instance) {
		reloadCaseInstance();
		if (instance != null) {
			List<SectionInstance> sections = getDocumentContext().getInstance(CaseInstance.class)
					.getSections();
			for (SectionInstance sectionInstance : sections) {
				List<com.sirma.itt.emf.instance.model.Instance> documents = sectionInstance
						.getContent();
				for (com.sirma.itt.emf.instance.model.Instance documentInstance : documents) {
					if ((documentInstance instanceof DocumentInstance)
							&& EqualsHelper.entityEquals(instance, documentInstance)) {
						getDocumentContext().setDocumentInstance(
								(DocumentInstance) documentInstance);
						return NavigationConstants.RELOAD_PAGE;
					}
				}
			}
			LOGGER.error("Document: {} is not found in current case: ", instance.getId(),
					getDocumentContext().getInstance(CaseInstance.class));
		} else {
			LOGGER.error("Document provided is invalid for current case: "
					+ getDocumentContext().getInstance(CaseInstance.class));
		}
		return NavigationConstants.NAVIGATE_TAB_CASE_DETAILS;
	}

	/**
	 * Setter method for sourceTokenId.
	 *
	 * @param sourceTokenId
	 *            the sourceTokenId to set
	 */
	public void setSourceTokenId(String sourceTokenId) {

		this.sourceTokenId = sourceTokenId;
	}

	/**
	 * Getter method for signedTokenId.
	 *
	 * @return the signedTokenId
	 */
	public String getSignedTokenId() {
		return signedTokenId;
	}

	/**
	 * Gets the source token id.
	 *
	 * @param instance
	 *            the instance
	 * @return the source token id
	 */
	public String getSourceTokenId(Serializable instance) {
		if (instance instanceof DocumentInstance) {
			sourceTokenId = ((DocumentInstance) instance).getDmsId();
			return sourceTokenId;
		}
		return null;
	}

	/**
	 * Setter method for signedTokenId.
	 *
	 * @param signedTokenId
	 *            the signedTokenId to set
	 */
	public void setSignedTokenId(String signedTokenId) {
		this.signedTokenId = signedTokenId;
	}

	/**
	 * Getter method for uesButtonId.
	 *
	 * @return the uesButtonId
	 */
	public String getUesButtonId() {
		return uesButtonId;
	}

	/**
	 * Setter method for uesButtonId.
	 *
	 * @param uesButtonId
	 *            the uesButtonId to set
	 */
	public void setUesButtonId(String uesButtonId) {
		this.uesButtonId = uesButtonId;
	}

	/**
	 * Gets the mimetype for given instances using its properties data.
	 * {@link DocumentProperties#FILE_MIMETYPE} is expected to be in the map as required.
	 * 
	 * @param instance
	 *            the instance to get from
	 * @return the mimetype or null if this is not document
	 */
	public String getMimetype(Serializable instance) {
		if (instance instanceof DocumentInstance) {
			DocumentInstance doc = (DocumentInstance) instance;
			// CMF-9265
			LOGGER.trace("CMFWeb: DocumentSignAction.getMimetype beforeload documentInstance = {}",
					doc);
			doc = documentService.loadByDbId(doc.getId());
			// CMF-9265
			LOGGER.trace("CMFWeb: DocumentSignAction.getMimetype after documentInstance = {}", doc);
			Serializable serializable = null;
			if ((doc != null) && (doc.getProperties() != null)) {
				serializable = doc.getProperties().get(DocumentProperties.MIMETYPE);
			}
			return (String) serializable;
		}
		return null;
	}

	/**
	 * Gets the name for given instances using its properties data. {@link DocumentProperties#NAME}
	 * is expected to be in the map as required.
	 * 
	 * @param instance
	 *            the instance to get from
	 * @return the mimetype or "" if this is not document
	 */
	public String getName(Serializable instance) {
		if (instance instanceof DocumentInstance) {
			DocumentInstance doc = (DocumentInstance) instance;
			String name = (String) doc.getProperties().get(DocumentProperties.NAME);
			if (name != null) {
				return name;
			}
			// in case of null return unknown.pdf, uknown.xml
		}
		return "";
	}

	/**
	 * Gets the document url.
	 *
	 * @return the document url
	 */
	public String getDocumentURL() {
		return documentURL;
	}

}
