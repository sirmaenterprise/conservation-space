package com.sirma.itt.cmf.integration;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.servlet.FormData;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * The Interface ServiceProxy will provide facade for alfresco api.
 */
public interface ServiceProxy {

	/** The web preview. */
	QName WEB_PREVIEW = QName.createQName(CMFModel.CMF_MODEL_1_0_URI, "previewFor");

	/**
	 * Gets the default query language.
	 * 
	 * @return the default query language
	 */
	String getDefaultQueryLanguage();

	/**
	 * Gets the transformer.
	 * 
	 * @param sourceMimetype
	 *            the source mimetype
	 * @param targetMimetype
	 *            the target mimetype
	 * @param options
	 *            the options
	 * @return the transformer
	 */
	ContentTransformer getTransformer(String sourceMimetype, String targetMimetype,
			TransformationOptions options);

	/**
	 * Gets the working copy.
	 * 
	 * @param updateable
	 *            the updateable
	 * @return the working copy
	 */
	NodeRef getWorkingCopy(NodeRef updateable);

	/**
	 * Guess mimetype.
	 * 
	 * @param filename
	 *            the filename
	 * @param reader
	 *            the reader
	 * @return the string
	 */
	String guessMimetype(String filename, ContentReader reader);

	/**
	 * Checks if is checked out.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return true, if is checked out
	 */
	boolean isCheckedOut(NodeRef nodeRef);

	/**
	 * Clean form data.
	 * 
	 * @param formdata
	 *            the formdata
	 */
	void cleanFormData(FormData formdata);

	/**
	 * Ensure versioning enabled.
	 * 
	 * @param updateNode
	 *            the update node
	 * @param props
	 *            the props
	 */
	void ensureVersioningEnabled(NodeRef updateNode, Map<QName, Serializable> props);

	/**
	 * Gets the service registry.
	 * 
	 * @return the serviceRegistry
	 */
	public ServiceRegistry getServiceRegistry();

	/**
	 * Sets the service registry.
	 * 
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry);

	/**
	 * Creates the pdf preview if none is existing (it is not checked)
	 * 
	 * @param node
	 *            the node is the node
	 * @return the node ref is the node for preview
	 */
	NodeRef createPdfPreview(NodeRef node);

	/**
	 * Find pdf preview for specifix alfresco system.
	 * 
	 * @param transformable
	 *            the transformable
	 * @return the node ref
	 */
	NodeRef findPdfPreview(NodeRef transformable);
}
