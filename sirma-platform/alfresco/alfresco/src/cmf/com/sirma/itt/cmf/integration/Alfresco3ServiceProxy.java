package com.sirma.itt.cmf.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * The Class Alfresco3ServiceProxy is proxy for services in QVII alfresco.
 */
public class Alfresco3ServiceProxy implements ServiceProxy {
	private static final String RENDERED_PDF_NAME = "webpreview";
	/** The service registry. */
	private ServiceRegistry serviceRegistry;

	/** The content service. */
	private ContentService contentService;

	/** The node service. */
	private NodeService nodeService;

	/**
	 * Gets the content service.
	 * 
	 * @return the content service
	 */
	private ContentService getContentService() {
		if (contentService == null) {
			contentService = serviceRegistry.getContentService();
		}
		return contentService;
	}

	/**
	 * Gets the node service.
	 * 
	 * @return the node service
	 */
	private NodeService getNodeService() {
		if (nodeService == null) {
			nodeService = serviceRegistry.getNodeService();
		}
		return nodeService;
	}

	/**
	 * Instantiates a new alfresco3 service proxy.
	 */
	public Alfresco3ServiceProxy() {
		System.out.println("AlfrescoQVIIServiceProxy.AlfrescoQVIIServiceProxy()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.integration.ServiceProxy#getDefaultQueryLanguage()
	 */
	@Override
	public String getDefaultQueryLanguage() {
		return SearchService.LANGUAGE_FTS_ALFRESCO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.ServiceProxy#getTransformer(java.lang.String
	 * , java.lang.String,
	 * org.alfresco.service.cmr.repository.TransformationOptions)
	 */
	@Override
	public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype,
			TransformationOptions options) {
		return getContentService().getTransformer(sourceMimetype, targetMimetype, options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.ServiceProxy#getWorkingCopy(org.alfresco
	 * .service.cmr.repository.NodeRef)
	 */
	@Override
	public NodeRef getWorkingCopy(NodeRef updateable) {
		return serviceRegistry.getCheckOutCheckInService().getWorkingCopy(updateable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.ServiceProxy#guessMimetype(java.lang.String
	 * , org.alfresco.service.cmr.repository.ContentReader)
	 */
	@Override
	public String guessMimetype(String filename, ContentReader reader) {
		return serviceRegistry.getMimetypeService().guessMimetype(filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.ServiceProxy#isCheckedOut(org.alfresco.
	 * service.cmr.repository.NodeRef)
	 */
	@Override
	public boolean isCheckedOut(NodeRef nodeRef) {

		LockType lockType = getServiceRegistry().getLockService().getLockType(nodeRef);
		if (LockType.READ_ONLY_LOCK.equals(lockType) == true || getWorkingCopy(nodeRef) != null) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.ServiceProxy#cleanFormData(org.springframework
	 * .extensions.webscripts.servlet.FormData)
	 */
	@Override
	public void cleanFormData(FormData formdata) {
		// nth to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.ServiceProxy#ensureVersioningEnabled(org
	 * .alfresco.service.cmr.repository.NodeRef, java.util.Map)
	 */
	@Override
	public void ensureVersioningEnabled(NodeRef updateNode, Map<QName, Serializable> props) {
		if (!getNodeService().hasAspect(updateNode, ContentModel.ASPECT_VERSIONABLE)) {
			getNodeService().addAspect(updateNode, ContentModel.ASPECT_VERSIONABLE, props);

		}
		if (serviceRegistry.getVersionService().getVersionHistory(updateNode) == null) {
			Map<String, Serializable> propsVersion = new HashMap<String, Serializable>(2, 1.0f);
			propsVersion.put(Version.PROP_DESCRIPTION, "");
			propsVersion.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			serviceRegistry.getVersionService().createVersion(updateNode, propsVersion);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.integration.ServiceProxy#getServiceRegistry()
	 */
	@Override
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.ServiceProxy#setServiceRegistry(org.alfresco
	 * .service.ServiceRegistry)
	 */
	@Override
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	// -------------get the services cached------------ //

	@Override
	public NodeRef createPdfPreview(NodeRef node) {
		Map<QName, Serializable> props = new HashMap<QName, Serializable>();
		props.put(ContentModel.PROP_NAME, RENDERED_PDF_NAME);
		return nodeService.createNode(node, RenditionModel.ASSOC_RENDITION,
				QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, RENDERED_PDF_NAME),
				ContentModel.TYPE_CONTENT, props).getChildRef();
	}

	@Override
	public NodeRef findPdfPreview(NodeRef transformable) {
		return serviceRegistry.getThumbnailService().getThumbnailByName(transformable,
				ContentModel.PROP_CONTENT, RENDERED_PDF_NAME);
	}

}
