package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.event.document.AfterDocumentRevertEvent;
import com.sirma.itt.cmf.event.document.AfterDocumentUploadEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentRevertEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentUploadEvent;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.event.AbstractContextEvent;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;


/**
 * Observer that controls the {@link DocumentInstance} semantic classes based on mimetype or purpose
 * of the document.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DocumentClassSynchronizationObserver {

	private static final String CURRENT_TYPE = "currentType";
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentClassSynchronizationObserver.class);
	/** The Constant DOCUMENT_PURPOSE. */
	private static final String DOCUMENT_PURPOSE = "iDoc";

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The connection. */
	@Inject
	private Instance<RepositoryConnection> connection;

	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The rdf type property name. It's used to point where to write the semantic class */
	private String rdfType;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Initialize the properties.
	 */
	@PostConstruct
	public void init() {
		rdfType = namespaceRegistryService.getShortUri(RDF.TYPE);
	}

	/**
	 * Listens for document upload event and determines the current semantic class based on mimetype
	 * and purpose.
	 *
	 * @param event
	 *            the event
	 */
	public void onBeforeDocumentUploaded(@Observes BeforeDocumentUploadEvent event) {
		DocumentInstance instance = event.getInstance();
		handleBeforeOperation(event, instance);
	}

	/**
	 * Listens for document revert event and determines the current semantic class based on mimetype
	 * and purpose.
	 *
	 * @param event
	 *            the event
	 */
	public void onBeforeDocumentRevertEvent(@Observes BeforeDocumentRevertEvent event) {
		DocumentInstance instance = event.getInstance();
		handleBeforeOperation(event, instance);
	}

	/**
	 * Backups the current document type/class in the given context event for later use.
	 *
	 * @param event
	 *            the event
	 * @param instance
	 *            the instance
	 */
	private void handleBeforeOperation(AbstractContextEvent event, DocumentInstance instance) {
		Serializable currentType = instance.getProperties().get(rdfType);
		// we will store the current type if any in the event and will try to detect type changes
		if (currentType != null) {
			event.addToContext(CURRENT_TYPE, currentType);
		} else {
			URI type = detectDocumentClass(instance);
			if (type != null) {
				event.addToContext(CURRENT_TYPE, type);
			}
		}
	}

	/**
	 * Listens for document upload event and changes the semantic class based on mimetype and
	 * purpose if the document mimetype has changed.
	 *
	 * @param event
	 *            the event
	 */
	public void onDocumentUploaded(@Observes AfterDocumentUploadEvent event) {
		DocumentInstance instance = event.getInstance();
		changeSemanticClassIfNeeded(event, instance);
	}

	/**
	 * Listens for document revert event and changes the semantic class based on mimetype and
	 * purpose if the document mimetype has changed.
	 *
	 * @param event
	 *            the event
	 */
	public void onAfterDocumentRevertEvent(@Observes AfterDocumentRevertEvent event) {
		DocumentInstance instance = event.getInstance();
		changeSemanticClassIfNeeded(event, instance);
	}

	/**
	 * Changes the semantic class of the given document if there is a change detected between the
	 * before and after events using the cached class in the events context.
	 *
	 * @param event
	 *            the event
	 * @param instance
	 *            the instance
	 */
	private void changeSemanticClassIfNeeded(AbstractContextEvent event, DocumentInstance instance) {
		Object oldType = event.getContext().remove(CURRENT_TYPE);
		URI newType = detectDocumentClass(instance);
		// if types are different we should send queries to change them
		if ((newType != null) && (oldType != null)
				&& !oldType.toString().equals(newType.toString())) {
			URI subject = namespaceRegistryService.buildUri(instance.getId().toString());
			URI oldObject = null;
			if (oldType instanceof URI) {
				oldObject = (URI) oldType;
			} else {
				oldObject = namespaceRegistryService.buildUri(oldType.toString());
			}
			RepositoryConnection repositoryConnection = connection.get();
			try {
				URI dataGraph = namespaceRegistryService.getDataGraph();
				repositoryConnection.remove(subject, RDF.TYPE, oldObject, dataGraph);
				repositoryConnection.add(subject, RDF.TYPE, newType, dataGraph);
			} catch (RepositoryException e) {
				LOGGER.error("Failed to change document type from {} to {} due to ", oldType,
						newType, e);
				try {
					repositoryConnection.rollback();
				} catch (RepositoryException e1) {
					LOGGER.error("Failed to rollback transaction due to ", e1);
				}
			}
		}

		if (newType != null) {
			Serializable value = typeConverter.convert(Uri.class, newType);
			instance.getProperties().put(rdfType, value);
		}
	}

	/**
	 * Detects the document type class.
	 *
	 * @param instance
	 *            the instance
	 * @return the uri
	 */
	private URI detectDocumentClass(DocumentInstance instance) {
		URI type = Proton.DOCUMENT;
		String mimeType = (String) instance.getProperties().get(DocumentProperties.MIMETYPE);
		if (DOCUMENT_PURPOSE.equals(instance.getPurpose()) && "text/html".equals(mimeType)) {
			type = EMF.INTELLIGENT_DOCUMENT;
		} else {
			if (mimeType != null) {
				if (mimeType.startsWith("image")) {
					type = EMF.IMAGE;
				} else if (mimeType.startsWith("video")) {
					type = EMF.VIDEO;
				} else if (mimeType.startsWith("audio")) {
					// we does not have a audio class so will place it for audio
					type = EMF.MEDIA;
				}
			} else {
				// if we does not have a mimetype we does not have uploaded document
				type = null;
			}
		}
		return type;
	}
}
