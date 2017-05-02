package com.sirma.itt.cmf.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * The Class Alfresco4ServiceProxy is proxy for services in alfresco 4.
 */
public class Alfresco4ServiceProxy implements ServiceProxy {

	private static final String RENDERED_PDF_NAME = "pdf_document";

	/** The service registry. */
	private ServiceRegistry serviceRegistry;

	/** The node service. */
	private NodeService nodeService;

	private BehaviourFilter behaviourFilter;

	/**
	 * Instantiates a new alfresco4 service proxy.
	 */
	public Alfresco4ServiceProxy() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.integration.ServiceProxy#getDefaultQueryLanguage()
	 */
	@Override
	public String getDefaultQueryLanguage() {
		return SearchService.LANGUAGE_SOLR_FTS_ALFRESCO;
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
		return getServiceRegistry().getContentService().getTransformer(null, sourceMimetype, -1L,
				targetMimetype, options);
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
		return getServiceRegistry().getCheckOutCheckInService().getWorkingCopy(updateable);
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
		return getServiceRegistry().getMimetypeService().guessMimetype(filename);
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
		return serviceRegistry.getCheckOutCheckInService().isCheckedOut(nodeRef);
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
		getServiceRegistry().getVersionService().ensureVersioningEnabled(updateNode, props);
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
	 * Gets the service registry.
	 * 
	 * @return the serviceRegistry
	 */
	@Override
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	/**
	 * Sets the service registry.
	 * 
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	@Override
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public NodeRef findPdfPreview(NodeRef nodeToCopy) {
		List<ChildAssociationRef> childAssocs = getNodeService().getChildAssocs(nodeToCopy,
				RegexQNamePattern.MATCH_ALL,
				new RegexQNamePattern(NamespaceService.RENDITION_MODEL_1_0_URI, RENDERED_PDF_NAME));
		if (childAssocs.size() == 1) {
			Version currentVersion = serviceRegistry.getVersionService().getCurrentVersion(
					nodeToCopy);
			String currentNodeVersion = null;
			if (currentVersion != null) {
				currentNodeVersion = currentVersion.getVersionLabel();
			} else {
				currentNodeVersion = "1.0";
			}
			NodeRef pdf = childAssocs.get(0).getChildRef();
			Serializable property = nodeService.getProperty(pdf, WEB_PREVIEW);
			if (currentNodeVersion.equals(property)) {
				return pdf;
			} else {
				// remove obsolete
				getNodeService().removeChildAssociation(childAssocs.get(0));
			}
		} else {
			// remove the obsolete data.
			for (ChildAssociationRef childAssociationRef : childAssocs) {
				getNodeService().removeChildAssociation(childAssociationRef);
			}
		}
		return null;
	}

	@Override
	public NodeRef createPdfPreview(NodeRef nodeToCopy) {
		Map<QName, Serializable> props = new HashMap<QName, Serializable>();
		props.put(ContentModel.PROP_NAME, RENDERED_PDF_NAME);
		Version currentVersion = serviceRegistry.getVersionService().getCurrentVersion(nodeToCopy);
		if (currentVersion != null) {
			props.put(WEB_PREVIEW, currentVersion.getVersionLabel());
		} else {
			props.put(WEB_PREVIEW, "1.0");
		}
		getBehaviourFilter().disableBehaviour(nodeToCopy);
		NodeRef childRef = null;
		try {
			childRef = nodeService.createNode(nodeToCopy, RenditionModel.ASSOC_RENDITION,
					QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, RENDERED_PDF_NAME),
					ContentModel.TYPE_THUMBNAIL, props).getChildRef();
		} finally {
			getBehaviourFilter().enableBehaviour(nodeToCopy);
		}
		return childRef;
	}

	/**
	 * Gets the behaviour filter.
	 * 
	 * @return the behaviour filter
	 */
	public BehaviourFilter getBehaviourFilter() {
		if (behaviourFilter == null) {
			behaviourFilter = (BehaviourFilter) serviceRegistry.getService(QName.createQName(
					NamespaceService.ALFRESCO_URI, "policyBehaviourFilter"));
		}
		return behaviourFilter;
	}
}
