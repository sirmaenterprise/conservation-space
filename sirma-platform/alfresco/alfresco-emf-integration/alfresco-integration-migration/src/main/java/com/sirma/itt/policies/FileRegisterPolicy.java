package com.sirma.itt.policies;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sirma.itt.migration.register.FileRegisterService;
import com.sirma.itt.service.BaseService;

/**
 * This class would be responsible to provide correct updates of nodes that
 * should behave in different than the default way. All related to 'eiso' site
 * changes should be added here.
 *
 * @author BBonev
 */
public class FileRegisterPolicy implements 
		NodeServicePolicies.OnMoveNodePolicy,
		NodeServicePolicies.OnDeleteNodePolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy {
	private static final Log LOGGER = LogFactory.getLog(FileRegisterPolicy.class);
	/** The on move node policy. */
	private JavaBehaviour onMoveNodePolicy;
	/** The on delete node policy. */
	private JavaBehaviour onDeleteNodePolicy;
	/** The on update node properties policy. */
	private JavaBehaviour onUpdateNodePropertiesPolicy;
	/** The on move folder policy. */
	private JavaBehaviour onMoveFolderPolicy;
	/** Policy component */
	private PolicyComponent policyComponent;
	/** baseService service */
	private BaseService baseService;

	/** The file register service. */
	private FileRegisterService fileRegisterService;

	/** The permission service. */
	private PermissionService permissionService;

	/** The node service. */
	private NodeService nodeService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/**
	 * Initialize the policies.
	 */
	public void init() {
		/* for move */
		onMoveNodePolicy = new JavaBehaviour(this, "onMoveNode",
				NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnMoveNodePolicy.QNAME,
				ContentModel.TYPE_CONTENT, onMoveNodePolicy);
		onMoveFolderPolicy = new JavaBehaviour(this, "onMoveNode",
				NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnMoveNodePolicy.QNAME,
				ContentModel.TYPE_FOLDER, onMoveFolderPolicy);

		/* for deleting */
		onDeleteNodePolicy = new JavaBehaviour(this, "onDeleteNode",
				NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnDeleteNodePolicy.QNAME,
				ContentModel.TYPE_CONTENT, onDeleteNodePolicy);

		/* for file renaming */
		onUpdateNodePropertiesPolicy = new JavaBehaviour(this,
				"onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ContentModel.TYPE_CONTENT, onUpdateNodePropertiesPolicy);
	}

	/**
	 * {@inheritDoc}<br>
	 * Actions for moving a node.
	 */
	@Override
	public void onMoveNode(ChildAssociationRef oldChildAssocRef,
			ChildAssociationRef newChildAssocRef) {
		boolean isFolder = getDictionaryService().isSubClass(
				getNodeService().getType(newChildAssocRef.getChildRef()),
				ContentModel.TYPE_FOLDER);
		// if we have folder to move we use the other method
		if (isFolder) {
			String destpath = constructDisplayPath(newChildAssocRef
					.getChildRef());
			if (destpath != null) {
				String srcPath = constructDisplayPath(oldChildAssocRef
						.getParentRef());
				if (srcPath != null) {
					srcPath += "/"
							+ (String) getNodeService().getProperty(
									oldChildAssocRef.getParentRef(),
									ContentModel.PROP_NAME);
					String name = (String) getNodeService().getProperty(
							newChildAssocRef.getChildRef(),
							ContentModel.PROP_NAME);
					destpath += "/" + name;
					srcPath += "/" + name;
					fileRegisterService.fileMoved(srcPath, destpath);
				}
			}
		} else {
			String destpath = constructDisplayPath(newChildAssocRef
					.getChildRef());
			if (destpath != null) {
				String name = (String) getNodeService().getProperty(
						newChildAssocRef.getChildRef(), ContentModel.PROP_NAME);
				fileRegisterService.fileMoved(oldChildAssocRef.getChildRef(),
						destpath, name);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef,
			boolean isNodeArchived) {
		NodeRef childRef = childAssocRef.getChildRef();
		fileRegisterService.fileDeleted(childRef);
	}

	/**
	 * {@inheritDoc} <br>
	 * On update properties policy to invoke open office processor that should
	 * update the properties.
	 */
	@Override
	public void onUpdateProperties(NodeRef nodeRef,
			Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (!getBaseService().checkNode(nodeRef)) {
			return;
		}

		// if we have modified the name of the file
		if (checkProperty(before, after, ContentModel.PROP_NAME)) {
			String destpath = constructDisplayPath(nodeRef);
			if (destpath != null) {
				Serializable nameAfter = after.get(ContentModel.PROP_NAME);
				// we update the new name
				// everything else should be the same
				fileRegisterService.fileMoved(nodeRef, destpath,
						(String) nameAfter);
			}
		}
	}

	/**
	 * Gets the policy component.
	 *
	 * @return the policyComponent
	 */
	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent
	 *            the policyComponent to set
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	/**
	 * Gets the node service.
	 *
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Getter method for baseService.
	 *
	 * @return the baseService
	 */
	public BaseService getBaseService() {
		return baseService;
	}

	/**
	 * Setter method for baseService.
	 *
	 * @param baseService
	 *            the baseService to set
	 */
	public void setBaseService(BaseService baseService) {
		this.baseService = baseService;
	}

	/**
	 * Getter method for fileRegisterService.
	 *
	 * @return the fileRegisterService
	 */
	public FileRegisterService getFileRegisterService() {
		return fileRegisterService;
	}

	/**
	 * Setter method for fileRegisterService.
	 *
	 * @param fileRegisterService
	 *            the fileRegisterService to set
	 */
	public void setFileRegisterService(FileRegisterService fileRegisterService) {
		this.fileRegisterService = fileRegisterService;
	}

	/**
	 * Getter method for permissionService.
	 *
	 * @return the permissionService
	 */
	public PermissionService getPermissionService() {
		return permissionService;
	}

	/**
	 * Setter method for permissionService.
	 *
	 * @param permissionService
	 *            the permissionService to set
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * Construct display path of the given node. The path is useful for
	 * FileRegister API
	 *
	 * @param nodeRef
	 *            the node ref
	 * @return the display path or <code>null</code> if cannot be retrieved
	 */
	private String constructDisplayPath(NodeRef nodeRef) {
		Path childPath = getNodeService().getPath(nodeRef);
		if (childPath != null) {
			String destpath = childPath.toDisplayPath(getNodeService(),
					getPermissionService());
			destpath = destpath.replace("/Company Home/Sites/", "").replace(
					"/documentLibrary/", "://");
			return destpath;
		}
		return null;
	}

	/**
	 * Check property whether should be processed.
	 *
	 * @param before
	 *            the before props
	 * @param after
	 *            the after props
	 * @param prop
	 *            the prop to check
	 * @return true, if successful
	 */
	private boolean checkProperty(Map<QName, Serializable> before,
			Map<QName, Serializable> after, QName prop) {
		Serializable propAfter = after.get(prop);
		return ((propAfter != null) && !propAfter.equals(before.get(prop)));
	}

}
