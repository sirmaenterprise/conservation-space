package com.sirma.itt.service;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import com.sirma.itt.utils.ObjectUtil;

/**
 * Utility service that holds general actions. it is registered as service to
 * inject the dependent services.
 * 
 * @author bbanchev
 */
public class BaseService {

	private NodeService nodeService;
	private SiteService siteService;
	private Repository repository;
	private DictionaryService dictionaryService;
	/** The service registry. */
	private ServiceRegistry serviceRegistry;

	protected static final DateFormat US_MID_DATE_FORMAT = DateFormat
			.getDateInstance(DateFormat.MEDIUM, Locale.US);
	protected static final DateFormat US_MID_DATE_TIME_FORMAT = DateFormat
			.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
					Locale.US);
	public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
			"yyyyMMdd");
	public static final SimpleDateFormat QVI_DATE_TIME_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm:ss aaa");
	/** document library node name. */
	public static final String DOC_LIBRARY_ID = "documentLibrary";

	/**
	 * Initialization for the service.
	 */
	public void init() {
		// nothing
	}

	/**
	 * Check if node is valid.
	 * 
	 * @param node
	 *            is the node to check
	 * @return true if the
	 *         <code> node !=null && its status is valid && it is not deleted</code>
	 */
	public boolean checkNode(NodeRef node) {
		if (node != null) {
			Status nodeStatus = getNodeService().getNodeStatus(node);
			return (nodeStatus != null) && !nodeStatus.isDeleted();
		}
		return false;
	}

	/**
	 * Generates new node description from string representation.
	 * 
	 * @param nodeRef
	 *            is the string representing node
	 * @return the created node (retrieved).
	 */
	public NodeRef toNode(String nodeRef) {
		if (nodeRef == null) {
			throw new RuntimeException("'nodeRef' is mandatory parameter");
		}
		return getRepository().findNodeRef("node",
				nodeRef.replace("://", "/").split("/"));
	}

	/**
	 * childByNamePath returns the Node at the specified 'cm:name' based Path
	 * walking the children of this Node. So a valid call might be:
	 * <code>mynode.childByNamePath("/QA/Testing/Docs");</code> is a leading /
	 * required? No, but it can be specified. are wild-cards supported? Does not
	 * seem to be used anywhere
	 * 
	 * @param rootNode
	 *            is the node to begin search from
	 * @param path
	 *            is the path to search under root
	 * @return The ScriptNode or null if the node is not found.
	 */
	public NodeRef childByNamePath(NodeRef rootNode, String path) {

		/**
		 * The current node is a folder. optimized code path for cm:folder and
		 * sub-types supporting getChildrenByName() method
		 */
		NodeRef result = null;
		StringTokenizer t = new StringTokenizer(path, "/");
		if (t.hasMoreTokens()) {
			result = rootNode;
			while (t.hasMoreTokens() && (result != null)) {
				String name = t.nextToken();
				List<ChildAssociationRef> results = nodeService
						.getChildrenByName(result, ContentModel.ASSOC_CONTAINS,
								Collections.singletonList(name));
				result = (results.size() > 0 ? results.get(0).getChildRef()
						: null);
			}
		}

		return result;

	}

	/**
	 * gets the doc library folder for a site. <br>
	 * <code> 1.find by  {@link SiteModel#PROP_COMPONENT_ID}
	 * if not ->
	 * 2.by qname if not ->
	 * 3.first child
	 * </code>
	 * 
	 * @param siteInfo
	 *            is the site retrieved by the site service
	 * @return the doc library or null of not found
	 */
	public NodeRef getDocLibraryOfSite(SiteInfo siteInfo) {
		if (siteInfo == null) {
			return null;
		}
		NodeRef root = siteInfo.getNodeRef();
		// try by component - most proper way
		List<ChildAssociationRef> childAssocs = nodeService
				.getChildAssocsByPropertyValue(root,
						SiteModel.PROP_COMPONENT_ID,
						DOC_LIBRARY_ID);
		if ((childAssocs != null) && (childAssocs.size() == 1)) {
			return childAssocs.get(0).getChildRef();
		}
		// first we try to find the document library
		childAssocs = nodeService.getChildAssocs(root,
				ContentModel.ASSOC_CONTAINS, QName.createQName(
						NamespaceService.CONTENT_MODEL_1_0_URI,
						DOC_LIBRARY_ID));
		if ((childAssocs != null) && !childAssocs.isEmpty()) {
			return childAssocs.get(0).getChildRef();
		}
		childAssocs = nodeService.getChildAssocs(root);
		if (childAssocs.size() > 0) {
			return childAssocs.get(0).getChildRef();
		}
		return null;
	}

	/**
	 * Invokes {@link #getSite(String)} and with the result
	 * {@link #getDocLibraryOfSite(SiteInfo)}.
	 * 
	 * @param siteName
	 *            the name of the site as string
	 * @return the document library or null if nth is found
	 */
	public NodeRef getDocLibraryOfSite(String siteName) {
		if (siteName == null) {
			return null;
		}
		return getDocLibraryOfSite(getSite(siteName));
	}

	/**
	 * Gets a site using name.
	 * 
	 * @param siteName
	 *            is the name of the site as string
	 * @return the site info as specified in {@link SiteService#getSite(String)}
	 */
	public SiteInfo getSite(String siteName) {
		if (siteName == null) {
			return null;
		}
		return siteService.getSite(siteName);
	}

	/**
	 * Gets the first child of children association using the parent param as
	 * parent node. Filtering is done using the popertyName and its value -
	 * propertyValue
	 * 
	 * @param parent
	 *            is the root node for the fetched associations
	 * @param propertyValue
	 *            is the value for the property propertyName
	 * @param propertyName
	 *            is {@link QName} representing key for property
	 * @return first child or null if nothing is found
	 */
	public NodeRef getChildNode(NodeRef parent, String propertyValue,
			QName propertyName) {
		if (!checkNode(parent) || (propertyValue == null)
				|| (propertyName == null)) {
			return null;
		}
		List<ChildAssociationRef> childAssocsByPropertyValue = nodeService
				.getChildAssocsByPropertyValue(parent, propertyName,
						propertyValue);
		if (childAssocsByPropertyValue.size() > 0) {
			return childAssocsByPropertyValue.get(0).getChildRef();
		}
		return null;
	}

	/**
	 * Gets all children of children association using the parent param as
	 * parent node. Filtering is done using the popertyName and its value -
	 * propertyValue
	 * 
	 * @param parent
	 *            is the root node for the fetched associations
	 * @param propertyValue
	 *            is the value for the property propertyName
	 * @param propertyName
	 *            is {@link QName} representing key for property
	 * @return all children or null if nothing is found
	 */
	public List<NodeRef> getChildNodes(NodeRef parent,
			Serializable propertyValue, QName propertyName) {
		if (!checkNode(parent) || (propertyValue == null)
				|| (propertyName == null)) {
			return null;
		}
		List<ChildAssociationRef> childAssocsByPropertyValue = nodeService
				.getChildAssocsByPropertyValue(parent, propertyName,
						propertyValue);
		if (childAssocsByPropertyValue.size() > 0) {
			return convertChildAssocToChildNodes(childAssocsByPropertyValue);
		}
		return null;
	}

	/**
	 * Filters a list of nodes by a property searching for the same value.
	 * 
	 * @param listOfNode
	 *            is the list of nodes to filter
	 * @param propertyValue
	 *            is the value for the property propertyName
	 * @param propertyName
	 *            is {@link QName} representing key for property
	 * @return list of filtered nodes (possibly empty) or null if some of the
	 *         input arguments is null
	 */
	public List<NodeRef> filterNodes(List<NodeRef> listOfNode,
			final Serializable propertyValue, final QName propertyName) {
		if ((listOfNode == null) || (propertyValue == null)
				|| (propertyName == null)) {
			return null;
		}
		List<NodeRef> filteredNodes = new ArrayList<NodeRef>();
		for (NodeRef nodeRef : listOfNode) {
			Serializable propertyValForNode = nodeService.getProperty(nodeRef,
					propertyName);
			if (propertyValue.equals(propertyValForNode)) {
				filteredNodes.add(nodeRef);
			}
		}
		return filteredNodes;
	}

	/**
	 * internal method to convert assoc to nodes
	 * 
	 * @param assocList
	 *            is the list of assoc
	 * @return the nodes
	 */
	public List<NodeRef> convertChildAssocToChildNodes(
			List<ChildAssociationRef> assocList) {
		ArrayList<NodeRef> children = new ArrayList<NodeRef>(assocList.size());
		for (ChildAssociationRef childAssociationRef : assocList) {
			children.add(childAssociationRef.getChildRef());
		}
		return children;
	}

	/**
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * @param siteService
	 *            the siteService to set
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * @return the siteService
	 */
	public SiteService getSiteService() {
		return siteService;
	}

	/**
	 * Checks if is container.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return true, if is container
	 */
	public boolean isContainer(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		return getDictionaryService()
				.isSubClass(type, ContentModel.TYPE_FOLDER)
				&& !getDictionaryService().isSubClass(type,
						ContentModel.TYPE_SYSTEM_FOLDER);
	}

	/**
	 * Gets the q name path.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return the q name path
	 */
	public String getQNamePath(final NodeRef nodeRef) {

		return AuthenticationUtil.runAs(new RunAsWork<String>() {

			@Override
			public String doWork() throws Exception {
				return getNodeService().getPath(nodeRef).toPrefixString(
						getServiceRegistry().getNamespaceService());
			}
		}, AuthenticationUtil.getSystemUserName());
	}

	/**
	 * Get children of parent filtered by specified properties.
	 * 
	 * @param parent
	 *            {@link NodeRef}, the parent node
	 * @param properties
	 *            {@link Map} of {@link QName} to {@link Serializable},
	 *            properties by which will be filtered children
	 * @return {@link List} of {@link NodeRef}, list of child nodes
	 */
	public List<NodeRef> getChildNodes(NodeRef parent,
			Map<QName, Serializable> properties) {
		List<ChildAssociationRef> associations = nodeService
				.getChildAssocs(parent);
		List<NodeRef> result = convertChildAssocToChildNodes(associations);
		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			result = filterNodes(result, entry.getValue(), entry.getKey());
		}
		return result;
	}

	/**
	 * Generates full user description using first and last names.
	 * 
	 * @param person
	 *            is the person node
	 * @return the constructed name
	 */
	public String getPersonNames(NodeRef person) {
		Serializable name = nodeService.getProperty(person,
				ContentModel.PROP_FIRSTNAME);
		name = ObjectUtil.isValid(name) ? name : "";
		Serializable surname = nodeService.getProperty(person,
				ContentModel.PROP_LASTNAME);
		surname = ObjectUtil.isValid(surname) ? surname : "";
		return (name + " " + surname).trim();
	}

	/**
	 * Gets a formated date from the node using its property.
	 * 
	 * @param node
	 *            is the node to obtain properties for using
	 *            {@link NodeService#getProperties(NodeRef)}
	 * @param key
	 *            is the property key
	 * @return the formated date using {@link #US_MID_DATE_FORMAT} or empty
	 *         string
	 */
	public String getDateProperty(NodeRef node, QName key) {
		Map<QName, Serializable> properties = nodeService.getProperties(node);
		Serializable serializable = properties.get(key);
		if (serializable instanceof Date) {
			return ISO_DATE_FORMAT.format(serializable);
		}
		return "";
	}

	/**
	 * Formats a date using US mid date format.
	 * 
	 * @param dateToFormat
	 *            date to format
	 * @return formated date.
	 */
	public String formatDate(Date dateToFormat) {
		return US_MID_DATE_FORMAT.format(dateToFormat);
	}

	/**
	 * Formats a date using US mid date format.
	 * 
	 * @param dateToFormat
	 *            date to format
	 * @return formated date.
	 */
	public String formatDateTime(Date dateToFormat) {
		return US_MID_DATE_TIME_FORMAT.format(dateToFormat);
	}

	/**
	 * Get child nodes which specified property {@link QName} is in specified
	 * {@link List}.
	 * 
	 * @param parent
	 *            {@link NodeRef}, the parent node
	 * @param propertyQName
	 *            {@link QName}, property {@link QName}
	 * @param properties
	 *            {@link List} of ? extends {@link Serializable}, possible
	 *            properties
	 * @return {@link List} of {@link NodeRef}, list of children
	 */
	public List<NodeRef> getChildNodes(NodeRef parent, QName propertyQName,
			List<? extends Serializable> properties) {
		List<NodeRef> result = new ArrayList<NodeRef>();
		List<ChildAssociationRef> associations = nodeService
				.getChildAssocs(parent);
		for (ChildAssociationRef association : associations) {
			Serializable childProperty = nodeService.getProperty(
					association.getChildRef(), propertyQName);
			if (properties.contains(childProperty)) {
				result.add(association.getChildRef());
			}
		}
		return result;
	}

	/**
	 * @param repository
	 *            the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * Checks if is document.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return true, if is document
	 */
	public boolean isDocument(NodeRef nodeRef) {

		QName type = nodeService.getType(nodeRef);
		return getDictionaryService().isSubClass(type,
				ContentModel.TYPE_CONTENT);

	}

	/**
	 * Update imap properties.
	 * 
	 * @param nodeRef
	 *            is the target node reference
	 * @return <code>true</code>, if successfully updated and modified
	 *         properties and <code>false</code> if the node was not modified
	 */
	public boolean updateImapProperties(NodeRef nodeRef) {
		if (getNodeService().exists(nodeRef) && isNodeImapFlaggable(nodeRef)) {
			if (!getNodeService()
					.hasAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE)) {
				HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
				props.put(ImapModel.PROP_FLAG_SEEN, Boolean.TRUE);
				props.put(ImapModel.PROP_FLAG_ANSWERED, Boolean.FALSE);
				props.put(ImapModel.PROP_FLAG_DELETED, Boolean.FALSE);
				props.put(ImapModel.PROP_FLAG_DRAFT, Boolean.FALSE);
				props.put(ImapModel.PROP_FLAG_FLAGGED, Boolean.FALSE);
				props.put(ImapModel.PROP_FLAG_RECENT, Boolean.FALSE);
				getNodeService().addAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE,
						props);
				return true;
			} else {
				Map<QName, Serializable> properties = getNodeService()
						.getProperties(nodeRef);

				Serializable seen = properties.get(ImapModel.PROP_FLAG_SEEN);
				Serializable recent = properties
						.get(ImapModel.PROP_FLAG_RECENT);

				if ((seen == null) || Boolean.FALSE.equals(seen)
						|| (recent == null) || Boolean.FALSE.equals(recent)) {
					properties.put(ImapModel.PROP_FLAG_SEEN, Boolean.TRUE);
					properties.put(ImapModel.PROP_FLAG_ANSWERED, Boolean.FALSE);
					properties.put(ImapModel.PROP_FLAG_DELETED, Boolean.FALSE);
					properties.put(ImapModel.PROP_FLAG_DRAFT, Boolean.FALSE);
					properties.put(ImapModel.PROP_FLAG_FLAGGED, Boolean.FALSE);
					properties.put(ImapModel.PROP_FLAG_RECENT, Boolean.FALSE);
					getNodeService().setProperties(nodeRef, properties);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if is node imap flaggable.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return true, if is node imap flaggable
	 */
	private boolean isNodeImapFlaggable(NodeRef nodeRef) {
		QName type = getNodeService().getType(nodeRef);
		return type.isMatch(ContentModel.TYPE_CONTENT);
		// TODO: RM support
//				|| type.isMatch(QVIModel.TYPE_EISO_CONTENT)
//				|| type.isMatch(RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT);
	}

	/**
	 * Gets the dictionary service.
	 * 
	 * @return the dictionary service
	 */
	private DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	/**
	 * Setter method for dictionaryService.
	 * 
	 * @param dictionaryService
	 *            the dictionaryService to set
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * Getter method for serviceRegistry.
	 * 
	 * @return the serviceRegistry
	 */
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	/**
	 * Setter method for serviceRegistry.
	 * 
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
}
