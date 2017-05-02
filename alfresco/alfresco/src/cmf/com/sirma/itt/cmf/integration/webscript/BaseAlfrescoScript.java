/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.workflow.WorkflowReportService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.ServiceProxy;
import com.sirma.itt.cmf.integration.service.CMFLockService;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * The BaseAlfrescoScript has some common methods used by all scripts.
 *
 * @author borislav banchev
 */
public abstract class BaseAlfrescoScript extends DeclarativeWebScript {
	/** Comment for UTF_8. */
	public static final String UTF_8 = "UTF-8";
	/** . */
	public static final String KEY_START_PATH = "startPath";
	/** . */
	public static final String KEY_NODEID = "node";
	/** . */
	public static final String KEY_DESCRIPTION = "description";
	/** . */
	public static final String KEY_PARENT_NODEID = "parentNode";
	/** . */
	public static final String KEY_SITES_IDS = "sites";
	/** the key definition, describing the noderef id for definition . */
	public static final String KEY_DEFINITION_ID = "definitionId";
	/** çase id. */
	public static final String KEY_CASE_ID = "caseId";;
	/** attachment id. */
	public static final String KEY_ATTACHMENT_ID = "attachmentId";
	/** . */
	public static final String KEY_SITE_ID = "site";
	/** . */
	public static final String KEY_CHILD_ASSOC_NAME = "childAssocName";
	/** . */
	public static final String KEY_SECTIONS = "sections";
	/** . */
	public static final String KEY_PROPERTIES = "properties";
	/** force the operation. */
	public static final String KEY_FORCE = "force";
	/** lock owner. */
	public static final String KEY_LOCK_OWNER = "lockOwner";
	/** the template processing mode. */
	public static final String KEY_MODE = "mode";
	/** The Constant DOCUMENT. */
	public static final String DOCUMENT = "document";
	/** The Constant KEY_QUERY. */
	public static final String KEY_QUERY = "query";
	/** The search service. */
	protected SearchService searchService;
	/** the node service. */
	protected NodeService nodeService;
	/** the lock service. */
	protected CMFLockService cmfLockService;
	/** the service registry. */
	protected ServiceRegistry serviceRegistry;
	/** the case service. */
	protected CMFService caseService;
	/** the proxy for services. */
	private ServiceProxy serviceProxy;
	/** the logger. */
	protected static final Logger LOGGER = Logger.getLogger(BaseAlfrescoScript.class);

	/** The Constant debugEnabled. */
	protected static final boolean debugEnabled = LOGGER.isDebugEnabled();
	/** The ownable service. */
	private OwnableService ownableService;

	/** The namespace service. */
	private NamespaceService namespaceService;

	/** The person service. */
	private PersonService personService;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The authentication service. */
	private MutableAuthenticationService authenticationService;

	/** The workflow service. */
	private WorkflowService workflowService;
	private WorkflowReportService workflowReportService;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl
	 * (org.springframework.extensions.webscripts.WebScriptRequest,
	 * org.springframework.extensions.webscripts.Status,
	 * org.springframework.extensions.webscripts.Cache)
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setRunAsUserSystem();
			return executeInternal(req);
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}

	/**
	 * Executes as system user by default current request. Should be
	 * implemented!
	 *
	 * @param req
	 *            is the current request
	 * @return the updated model.
	 */
	protected abstract Map<String, Object> executeInternal(WebScriptRequest req);

	/**
	 * Converts json object to hashmap (keys should be valid qname).
	 *
	 * @param <V>
	 *            is the required value type
	 * @param jsonObject
	 *            is the json to convert
	 * @return the converted map
	 * @throws JSONException
	 *             on error
	 */
	@SuppressWarnings("unchecked")
	protected <V> Map<QName, V> toMap(JSONObject jsonObject) throws JSONException {
		Iterator<String> keys = jsonObject.keys();
		Map<QName, V> map = new HashMap<QName, V>(jsonObject.length());
		String currentLKey = null;
		try {
			while (keys.hasNext()) {
				currentLKey = keys.next().toString();
				QName resolvedToQName = QName.resolveToQName(getNamespaceService(), currentLKey);
				if (resolvedToQName != null) {
					PropertyDefinition property = getDataDictionaryService().getProperty(
							resolvedToQName);
					debug("toMap() ", property == null ? "" : property.getName(), " for: ",
							resolvedToQName.toString());
					if (property != null) {
						if (Date.class.getName().equals(property.getDataType().getJavaClassName())) {
							map.put(resolvedToQName, (V) new Date(jsonObject.getLong(currentLKey)));
						} else {
							// now try convert - if fail return to current value
							Object value = null;
							try {
								value = toArray(jsonObject.get(currentLKey));

								Object converted = null;
								if (property.isMultiValued()) {
									converted = DefaultTypeConverter.INSTANCE.convert(
											Collection.class, value);
								} else {
									converted = DefaultTypeConverter.INSTANCE.convert(
											property.getDataType(), value);
								}
								map.put(resolvedToQName, (V) converted);
							} catch (Exception e) {
								debug(e, "Error while processing. Skipping to default! ",
										currentLKey);
								map.put(resolvedToQName, (V) value);
							}
						}
					} else {
						// nth to do
						map.put(resolvedToQName, (V) jsonObject.getString(currentLKey));
					}

				} else {
					LOGGER.warn("BaseAlfrescoScript.toMap() SKIP " + currentLKey);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			debug(e, "Error for: ", currentLKey);
		}
		return map;
	}

	/**
	 * Check if this is array and if so convert it to list. If it not
	 * {@link JSONArray} simply return the same value
	 *
	 * @param value
	 *            is the value to covert
	 * @return {@link ArrayList} or the same
	 * @throws JSONException
	 *             on error
	 */
	private Object toArray(Object value) throws JSONException {
		if (value instanceof JSONArray) {
			JSONArray arr = (JSONArray) value;
			ArrayList<Object> collection = new ArrayList<Object>(arr.length());
			for (int i = 0; i < arr.length(); i++) {
				collection.add(arr.get(i));
			}
			value = collection;
		}
		return value;
	}

	/**
	 * Updates node and sets owner.
	 *
	 * @param properties
	 *            the properties map
	 * @param updateable
	 *            is the node to update
	 * @return the updateable on sucess or null
	 */
	protected NodeRef updateNodeAndSetOwner(Map<QName, Serializable> properties, NodeRef updateable) {
		if (updateable != null) {
			String lockOwner = cmfLockService.unlockNode(updateable);
			nodeService.addProperties(updateable, properties);
			if (lockOwner != null) {
				getOwnableService().setOwner(updateable, lockOwner);
			} else {
				getOwnableService().setOwner(updateable, AuthenticationUtil.getSystemUserName());
			}
			if (lockOwner != null) {
				cmfLockService.lockNode(updateable, lockOwner);
			}
			return updateable;
		}
		return null;
	}

	/**
	 * Update node and set owner.
	 *
	 * @param properties
	 *            the properties
	 * @param updateable
	 *            the updateable
	 * @param lockUser
	 *            the owner to set
	 * @return the node ref
	 */
	protected NodeRef updateNodeAndSetOwner(Map<QName, Serializable> properties,
			NodeRef updateable, String lockUser) {
		if (updateable != null) {
			String lockOwner = null;
			try {
				lockOwner = cmfLockService.unlockNode(updateable);
				nodeService.addProperties(updateable, properties);
				if (lockOwner != null) {
					getOwnableService().setOwner(updateable, lockOwner);
				} else {
					getOwnableService().setOwner(updateable, lockUser);
				}
			} finally {
				cmfLockService.lockNode(updateable, lockUser);
			}
			return updateable;
		}
		return null;
	}

	/**
	 * Update node.
	 *
	 * @param properties
	 *            the properties
	 * @param updateable
	 *            the updateable
	 * @return the node ref
	 */
	protected NodeRef updateNode(Map<QName, Serializable> properties, NodeRef updateable) {
		if (updateable != null) {
			String lockOwner = null;
			try {
				lockOwner = cmfLockService.unlockNode(updateable);
				nodeService.addProperties(updateable, properties);
				return updateable;
			} finally {
				cmfLockService.lockNode(updateable, lockOwner);
			}
		}
		return null;
	}

	/**
	 * Gets general property.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param propertyName
	 *            the property name
	 * @return the property value
	 * @throws JSONException
	 *             the jSON exception
	 */
	protected Object getObjectProperty(JSONObject jsonObject, String propertyName)
			throws JSONException {
		if (jsonObject.has(propertyName)) {
			return jsonObject.get(propertyName);
		}
		return null;
	}

	/**
	 * Gets the string property.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param propertyName
	 *            the property name
	 * @return the property value
	 * @throws JSONException
	 *             the jSON exception
	 */
	protected String getStringProperty(JSONObject jsonObject, String propertyName)
			throws JSONException {
		if (jsonObject.has(propertyName)) {
			return jsonObject.getString(propertyName);
		}
		return null;
	}

	/**
	 * Sets specific exit status.
	 *
	 * @param error
	 *            the http error number
	 * @param msg
	 *            the custom message
	 * @return the web script exception
	 */
	protected WebScriptException createStatus(int error, String msg) {
		// status.setCode(error);
		// status.setMessage(msg);
		// status.setRedirect(true);
		return new WebScriptException(error, msg);
	}

	/**
	 * Gets the transaction cache object.
	 *
	 * @param <T>
	 *            the generic type
	 * @param prefix
	 *            the prefix
	 * @param nodeRef
	 *            the node ref
	 * @return the transaction cache object
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTransactionCacheObject(String prefix, NodeRef nodeRef) {
		return (T) AlfrescoTransactionSupport.getResource(prefix + nodeRef.toString());
	}

	/**
	 * Sets the transaction cache object.
	 *
	 * @param <T>
	 *            the generic type
	 * @param prefix
	 *            the prefix
	 * @param nodeRef
	 *            the node ref
	 * @param value
	 *            the value
	 * @return the t
	 */
	public <T> T setTransactionCacheUserObject(String prefix, NodeRef nodeRef, T value) {
		String user = AuthenticationUtil.getRunAsUser();
		AlfrescoTransactionSupport.bindResource(prefix + nodeRef.toString() + user, value);
		return value;
	}

	/**
	 * Gets the transaction cache object specific for user.
	 *
	 * @param <T>
	 *            the generic type
	 * @param prefix
	 *            the prefix
	 * @param nodeRef
	 *            the node ref
	 * @return the transaction cache object
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTransactionCacheUserObject(String prefix, NodeRef nodeRef) {
		String user = AuthenticationUtil.getRunAsUser();
		return (T) AlfrescoTransactionSupport.getResource(prefix + nodeRef.toString() + user);

	}

	/**
	 * Sets the transaction cache object.
	 *
	 * @param <T>
	 *            the generic type
	 * @param prefix
	 *            the prefix
	 * @param nodeRef
	 *            the node ref
	 * @param value
	 *            the value
	 * @return the t
	 */
	public <T> T setTransactionCacheObject(String prefix, NodeRef nodeRef, T value) {
		AlfrescoTransactionSupport.bindResource(prefix + nodeRef.toString(), value);
		return value;
	}

	/**
	 * Gets all aspects for node and saves them in transaction cache.
	 *
	 * @param nodeRef
	 *            is the current node
	 * @return the set of aspects.
	 */
	public Set<QName> getAspects(NodeRef nodeRef) {
		Set<QName> result = getTransactionCacheObject("nodeAspects", nodeRef);
		if (result != null) {
			return result;
		}
		return setTransactionCacheObject("nodeAspects", nodeRef,
				getNodeService().getAspects(nodeRef));

	}

	/**
	 * Updates the transactional cached properties lazily.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @param properties
	 *            the properties to set for node
	 * @return the current cached properties for node
	 */
	protected Map<QName, Serializable> updateProperties(NodeRef nodeRef,
			Map<QName, Serializable> properties) {
		return setTransactionCacheObject("nodeProperties", nodeRef, properties);
	}

	/**
	 * Get node props from local cache.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @return the properties for node
	 */
	public Map<QName, Serializable> getProperties(NodeRef nodeRef) {
		Map<QName, Serializable> result = getTransactionCacheObject("nodeProperties", nodeRef);
		if (result != null) {
			return result;
		}
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		return setTransactionCacheObject("nodeProperties", nodeRef, properties);
	}

	/**
	 * finds the node reference using the httpRequest.
	 *
	 * @param req
	 *            is the received WebScriptRequest
	 * @return the found node or throws an Exception if not found
	 */
	protected NodeRef getNodeRef(WebScriptRequest req) {
		Map<String, String> parameters = req.getServiceMatch().getTemplateVars();
		String storeId = parameters.get("store_id");
		String storeType = parameters.get("store_type");
		String docId = parameters.get("id");
		return caseService.getNodeRef(storeType + "/" + storeId + "/" + docId);
	}

	/**
	 * Gets the search service.
	 *
	 * @return the searchService
	 */
	public SearchService getSearchService() {
		return searchService;
	}

	/**
	 * Sets the search service.
	 *
	 * @param searchService
	 *            the searchService to set
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * Gets the node service.
	 *
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * Sets the node service.
	 *
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Gets the service registry.
	 *
	 * @return the serviceRegistry
	 */
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	/**
	 * Sets the service registry.
	 *
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * Gets the cmf lock service.
	 *
	 * @return the cmfLockService
	 */
	public CMFLockService getCmfLockService() {
		return cmfLockService;
	}

	/**
	 * Sets the cmf lock service.
	 *
	 * @param cmfLockService
	 *            the cmfLockService to set
	 */
	public void setCmfLockService(CMFLockService cmfLockService) {
		this.cmfLockService = cmfLockService;
	}

	/**
	 * Gets the case service.
	 *
	 * @return the caseService
	 */
	public CMFService getCaseService() {
		return caseService;
	}

	/**
	 * Gets the service proxy.
	 *
	 * @return the serviceProxy
	 */
	public ServiceProxy getServiceProxy() {
		if (serviceProxy == null) {
			serviceProxy = (ServiceProxy) serviceRegistry.getService(QName.createQName(
					NamespaceService.ALFRESCO_URI, "ServiceProxy"));
		}
		return serviceProxy;
	}

	/**
	 * Sets the service proxy.
	 *
	 * @param serviceProxy
	 *            the serviceProxy to set
	 */
	public void setServiceProxy(ServiceProxy serviceProxy) {
		this.serviceProxy = serviceProxy;
	}

	/**
	 * Sets the case service.
	 *
	 * @param caseService
	 *            the caseService to set
	 */
	public void setCaseService(CMFService caseService) {
		this.caseService = caseService;
	}

	/**
	 * Gets the ownable service.
	 *
	 * @return the ownable service
	 */
	protected OwnableService getOwnableService() {
		if (ownableService == null) {
			ownableService = getServiceRegistry().getOwnableService();
		}
		return ownableService;
	}

	/**
	 * Gets the namespace service.
	 *
	 * @return the namespace service
	 */
	protected NamespaceService getNamespaceService() {
		if (namespaceService == null) {
			namespaceService = serviceRegistry.getNamespaceService();
		}
		return namespaceService;
	}

	/**
	 * Gets the person service.
	 *
	 * @return the person service
	 */
	protected PersonService getPersonService() {
		if (personService == null) {
			personService = getServiceRegistry().getPersonService();
		}
		return personService;
	}

	/**
	 * Gets the data dictionary service.
	 *
	 * @return the data dictionary service
	 */
	protected DictionaryService getDataDictionaryService() {
		if (dictionaryService == null) {
			dictionaryService = serviceRegistry.getDictionaryService();
		}
		return dictionaryService;
	}

	/**
	 * Gets the authentication service.
	 *
	 * @return the authentication service
	 */
	protected AuthenticationService getAuthenticationService() {
		if (authenticationService == null) {
			authenticationService = getServiceRegistry().getAuthenticationService();
		}
		return authenticationService;
	}

	/**
	 * Gets the workflow service.
	 *
	 * @return the workflow service
	 */
	protected WorkflowService getWorkflowService() {
		if (workflowService == null) {
			workflowService = serviceRegistry.getWorkflowService();
		}
		return workflowService;
	}

	/**
	 * Gets the workflow report service.
	 *
	 * @return the workflow report service
	 */
	public WorkflowReportService getWorkflowReportService() {
		if (workflowReportService == null) {
			workflowReportService = (WorkflowReportService) serviceRegistry.getService(QName
					.createQName(NamespaceService.ALFRESCO_URI, "taskReportService"));
		}
		return workflowReportService;
	}

	/**
	 * Debug.
	 *
	 * @param message
	 *            the message
	 */
	protected void debug(Object... message) {
		if (debugEnabled) {
			StringBuilder builder = new StringBuilder();
			for (Object string : message) {
				builder.append(string);
			}
			getLogger().debug(builder.toString());
		}

	}

	/**
	 * Debug.
	 *
	 * @param message
	 *            the message
	 * @param e
	 *            the exception
	 */
	protected void debug(Throwable e, Object... message) {
		if (debugEnabled) {
			StringBuilder builder = new StringBuilder();
			for (Object string : message) {
				builder.append(string.toString());
			}
			getLogger().debug(builder.toString());
		}
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	protected Logger getLogger() {
		return LOGGER;
	}
}
