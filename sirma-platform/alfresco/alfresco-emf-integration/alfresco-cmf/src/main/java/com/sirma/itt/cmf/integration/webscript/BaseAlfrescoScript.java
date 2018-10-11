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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowReportService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.ServiceProxy;
import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFLockService;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * The BaseAlfrescoScript has some common methods used by all scripts.
 *
 * @author borislav banchev
 */
public abstract class BaseAlfrescoScript extends DeclarativeWebScript {

	private static final Properties SYSTEM_PROPERTIES = readGlobalProps();
	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(BaseAlfrescoScript.class);
	/** The Constant debugEnabled. */
	protected boolean debugEnabled = getLogger().isDebugEnabled();
	/** Comment for UTF_8. */
	public static final String UTF_8 = "UTF-8";
	/** . */
	public static final String KEY_START_PATH = "startPath";
	/** . */
	public static final String KEY_NODEID = "node";
	/** . */
	public static final String KEY_TYPE = "type";
	/** . */
	public static final String KEY_DESCRIPTION = "description";
	/** . */
	public static final String KEY_PARENT_NODEID = "parentNode";
	/** . */
	public static final String KEY_SITES_IDS = "sites";
	/** the key definition, describing the noderef id for definition . */
	public static final String KEY_DEFINITION_ID = "definitionId";
	/** context dms id. */
	public static final String KEY_CONTEXT_ID = "contextId";
	/** project id. */
	public static final String KEY_PROJECT_ID = "projectId";
	/** reference id. */
	public static final String KEY_REFERENCE_ID = "referenceId";
	/** attachment id. */
	public static final String KEY_ATTACHMENT_ID = "attachmentId";
	/** . */
	public static final String KEY_SITE_ID = "site";
	/** . */
	public static final String KEY_CONTEXT = "context";
	/** . */
	public static final String KEY_CHILD_ASSOC_NAME = "childAssocName";
	/** . */
	public static final String KEY_SECTIONS = "sections";
	/** . */
	public static final String KEY_PROPERTIES = "properties";
	/** . */
	public static final String KEY_ASPECTS = "aspects";
	/** force the operation. */
	public static final String KEY_FORCE = "force";
	/** lock owner. */
	public static final String KEY_LOCK_OWNER = "lockOwner";
	/** the template processing mode. */
	public static final String KEY_WORKING_MODE = "mode";
	/** The Constant DOCUMENT. */
	public static final String KEY_DOCUMENT = "document";
	/** The Constant KEY_QUERY. */
	public static final String KEY_QUERY = "query";

	private static final String KEY_TENANT_ADMIN_BASENAME = "alfresco_user_store.tenant.adminusername";
	/** The search service. */
	protected SearchService searchService;
	/** the node service. */
	protected NodeService nodeService;
	/** the lock service. */
	protected CMFLockService cmfLockService;
	/** the service registry. */
	protected ServiceRegistry serviceRegistry;
	/** the case service. */
	protected CMFService cmfService;
	/** the proxy for services. */
	private ServiceProxy serviceProxy;
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
	private AuthorityService authorityService;

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		String runAsUser = AuthenticationUtil.getRunAsUser();
		try {
			String systemUser = CMFService.getSystemUser(runAsUser);
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setRunAsUser(systemUser);
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
					PropertyDefinition property = getDataDictionaryService().getProperty(resolvedToQName);
					debug("convert ", property == null ? "!missing!" : property.getName(), " for: ", resolvedToQName.toString());
					if (property != null) {
						if (Date.class.getName().equals(property.getDataType().getJavaClassName())) {
							map.put(resolvedToQName, (V) new Date(jsonObject.getLong(currentLKey)));
						} else {
							// now try convert - if fail return to current value
							Object value = null;
							try {
								value = checkJSONMultivalue(jsonObject.get(currentLKey));
								if (JSONObject.NULL.equals(value)) {
									warn("NULL value for: ", resolvedToQName);
									continue;
								}
								Object converted = null;
								if (property.isMultiValued()) {
									converted = DefaultTypeConverter.INSTANCE.convert(Collection.class, value);
								} else {
									converted = DefaultTypeConverter.INSTANCE.convert(property.getDataType(), value);
								}
								map.put(resolvedToQName, (V) converted);
							} catch (Exception e) {
								warn(e, "Error while processing. Skipping to default! ", currentLKey);
								map.put(resolvedToQName, (V) value);
							}
						}
					} else {
						// nth to do
						map.put(resolvedToQName, (V) jsonObject.getString(currentLKey));
					}

				} else {
					warn("SKIP convert as not resolved: ", currentLKey);
				}
			}
		} catch (JSONException e) {
			log(Level.ERROR, e, "Error for: ", currentLKey);
		}
		return map;
	}

	/**
	 * Converts full notation {@link QName} to prefixed string with the same
	 * value.
	 *
	 * @param properties
	 *            is the properties map
	 * @return the updated keys map
	 */
	protected Map<String, Serializable> toPrefixedProperties(Map<QName, Serializable> properties) {
		Map<String, Serializable> result = new HashMap<String, Serializable>(properties.size());
		for (Entry<QName, Serializable> nextProp : properties.entrySet()) {
			result.put(nextProp.getKey().toPrefixString(getNamespaceService()), nextProp.getValue());
		}
		return result;
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
	protected Object checkJSONMultivalue(Object value) throws JSONException {
		Object valueLocal = value;
		if (valueLocal instanceof JSONArray) {
			JSONArray arr = (JSONArray) valueLocal;
			List<Object> collection = new ArrayList<Object>(arr.length());
			for (int i = 0; i < arr.length(); i++) {
				collection.add(arr.get(i));
			}
			valueLocal = collection;
		}
		return valueLocal;
	}

	/**
	 * Updates node and sets owner.
	 *
	 * @param properties
	 *            the properties map
	 * @param updateable
	 *            is the node to update
	 * @return the updateable on success or null
	 */
	protected NodeRef updateNodeAndSetOwner(final Map<QName, Serializable> properties, final NodeRef updateable) {
		if (updateable != null) {
			RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>() {

				@Override
				public NodeRef execute() throws Throwable {
					String lockOwner = cmfLockService.unlockNode(updateable);
					nodeService.addProperties(updateable, properties);
					if (lockOwner != null) {
						getOwnableService().setOwner(updateable, lockOwner);
					} else {
						getOwnableService().setOwner(updateable, CMFService.getSystemUser());
					}
					if (lockOwner != null) {
						cmfLockService.lockNode(updateable, lockOwner);
					}
					return updateable;
				}
			};
			if (doInTransaction(callback)) {
				return updateable;
			}
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
	protected NodeRef updateNodeAndSetOwner(final Map<QName, Serializable> properties, final NodeRef updateable, final String lockUser) {
		if (updateable != null) {
			RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>() {

				@Override
				public NodeRef execute() throws Throwable {
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
			};
			if (doInTransaction(callback)) {
				return updateable;
			}
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
	protected NodeRef updateNode(final Map<QName, Serializable> properties, final NodeRef updateable) {
		if (updateable != null) {
			RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>() {

				@Override
				public NodeRef execute() throws Throwable {
					String lockOwner = null;
					try {
						lockOwner = cmfLockService.unlockNode(updateable);
						nodeService.addProperties(updateable, properties);
					} finally {
						cmfLockService.lockNode(updateable, lockOwner);
					}
					return updateable;
				}
			};
			if (doInTransaction(callback)) {
				return updateable;
			}
		}
		return null;
	}

	/**
	 * Do in transaction.
	 * 
	 * @param callback
	 *            the callback
	 */
	public boolean doInTransaction(RetryingTransactionCallback<?> callback) {
		try {
			RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
			transactionHelper.setMaxRetries(5);
			transactionHelper.setMaxRetryWaitMs(5000);
			transactionHelper.setMinRetryWaitMs(1000);
			transactionHelper.setRetryWaitIncrementMs(1000);
			transactionHelper.setForceWritable(true);
			transactionHelper.doInTransaction(callback, false, true);
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
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
	protected Object getObjectProperty(JSONObject jsonObject, String propertyName) throws JSONException {
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
	protected String getStringProperty(JSONObject jsonObject, String propertyName) throws JSONException {
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
		return (T) AlfrescoTransactionSupport.getResource(prefix + String.valueOf(nodeRef));
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
		AlfrescoTransactionSupport.bindResource(prefix + String.valueOf(nodeRef) + user, value);
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
		return (T) AlfrescoTransactionSupport.getResource(prefix + String.valueOf(nodeRef) + user);

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
		AlfrescoTransactionSupport.bindResource(prefix + String.valueOf(nodeRef), value);
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
		return setTransactionCacheObject("nodeAspects", nodeRef, getNodeService().getAspects(nodeRef));

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
	protected Map<QName, Serializable> updateProperties(NodeRef nodeRef, Map<QName, Serializable> properties) {
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
		return cmfService.getNodeRef(storeType + "/" + storeId + "/" + docId);
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
	 * Setter method for cmfLockService.
	 *
	 * @param cmfLockService
	 *            the cmfLockService to set
	 */
	public void setCmfLockService(CMFLockService cmfLockService) {
		this.cmfLockService = cmfLockService;
	}

	/**
	 * Get the authority service cached.
	 *
	 * @return the authority service
	 */
	protected AuthorityService getAuthorityService() {
		if (authorityService == null) {
			authorityService = serviceRegistry.getAuthorityService();
		}
		return authorityService;
	}

	/**
	 * Gets the case service.
	 *
	 * @return the caseService
	 */
	public CMFService getCaseService() {
		return cmfService;
	}

	/**
	 * Gets the service proxy.
	 *
	 * @return the serviceProxy
	 */
	public ServiceProxy getServiceProxy() {
		if (serviceProxy == null) {
			serviceProxy = (ServiceProxy) serviceRegistry.getService(QName.createQName(NamespaceService.ALFRESCO_URI, "ServiceProxy"));
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
	 * @param cmfService
	 *            the cmfService to set
	 */
	public void setCaseService(CMFService cmfService) {
		this.cmfService = cmfService;
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
			workflowReportService = (WorkflowReportService) serviceRegistry.getService(CMFModel.TASK_REPORT_SERVICE_URI);
		}
		return workflowReportService;
	}

	/**
	 * Debug.
	 *
	 * @param msgs
	 *            the messages
	 */
	protected void debug(Object... msgs) {
		if (debugEnabled) {
			log(Level.DEBUG, null, msgs);
		}

	}

	/**
	 * Debug.
	 *
	 * @param msgs
	 *            the messages
	 * @param error
	 *            the exception to log
	 */
	protected void debug(Throwable error, Object... msgs) {
		if (debugEnabled) {
			log(Level.DEBUG, error, msgs);
		}
	}

	/**
	 * Log using arbitrary provided logger.
	 *
	 * @param level
	 *            is the log level
	 * @param error
	 *            the exception to log
	 * @param message
	 *            the messages to print as message
	 */
	protected void log(Level level, Throwable error, Object... message) {
		if (getLogger().isEnabledFor(level)) {
			StringBuilder builder = new StringBuilder(1024);
			if (message == null) {
				builder.append(error.getMessage());
			} else {
				for (Object msgPart : message) {
					builder.append(msgPart);
				}
			}
			if (error != null) {
				getLogger().log(level, builder.toString(), error);
			} else {
				getLogger().log(level, builder.toString());
			}
			builder = null;
		}
	}

	/**
	 * Log using arbitrary provided logger.
	 *
	 * @param level
	 *            is the log level
	 * @param message
	 *            the messages to print as message
	 */
	protected void log(Level level, Object... message) {
		log(level, null, message);
	}

	/**
	 * Warn to logger.
	 *
	 * @param e
	 *            the exception
	 * @param message
	 *            the message
	 */
	protected void warn(Throwable e, Object... message) {
		log(Level.WARN, e, message);
	}

	/**
	 * Warn to logger.
	 *
	 * @param message
	 *            the message
	 */
	protected void warn(Object... message) {
		log(Level.WARN, null, message);
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	protected Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Reads the global properties and return them as {@link Properties}. On
	 * error empty {@link Properties} is returned and error is indicated.
	 *
	 * @return the loaded or empty properties
	 */
	private static synchronized Properties readGlobalProps() {

		ClassPathResource globalProps = new ClassPathResource("alfresco-global.properties");
		try {
			Properties globalProperties = new Properties();
			globalProperties.load(globalProps.getInputStream());
			return globalProperties;
		} catch (Exception e) {
			LOGGER.error("Properties could not be read!", e);
		}
		return new Properties();

	}

	/**
	 * Reads a config property dynamically from the properties file.
	 *
	 * @param key
	 *            is the key for property
	 * @param defaultValue
	 *            is the default property if property is not found
	 * @return the default or found property
	 */
	protected static synchronized String readProperty(String key, String defaultValue) {
		if (SYSTEM_PROPERTIES.containsKey(key)) {
			return SYSTEM_PROPERTIES.getProperty(key);
		} else {
			SYSTEM_PROPERTIES.put(key, defaultValue);
			return defaultValue;
		}
	}

	protected String getTenantAdmin() {
		String readProperty = readProperty(KEY_TENANT_ADMIN_BASENAME, AuthenticationUtil.getAdminUserName());
		if (readProperty != null) {
			String tenantId = CMFService.getTenantId();
			if (StringUtils.isNotBlank(tenantId)) {
				return readProperty + TenantService.SEPARATOR + tenantId;
			}
		}
		return readProperty;
	}
}
