package com.sirma.itt.cmf.integration.webscript;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluator;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.workflow.MemoryCache;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * Script for woriking with dms attachments.
 * 
 * @author bbanchev
 */
public class AttachmentScript extends BaseFormScript {

	/** The Constant SECURITY_ENABLED. */
	private static final String SECURITY_ENABLED = "securityEnabled";
	/** The Constant KEY_DESTINATION. */
	private static final String KEY_DESTINATION = "destination";
	/** The Constant DESCRIPTION. */
	private static final String DESCRIPTION = "description";
	/** The Constant MAJORVERSION. */
	private static final String MAJORVERSION = "majorversion";
	/** The Constant PARENT_REF. */
	private static final String PARENT_REF = "parentRef";
	/** The Constant PARENT_REF_LOCK_OWNER. */
	private static final String PARENT_REF_LOCK_OWNER = "parentRefLockOwner";

	/** The policy behaviour filter. */
	private BehaviourFilter policyBehaviourFilter;

	/** The action service. */
	private ActionService actionService;

	/** The site service. */
	private static SiteService siteService;

	/** The thumbnail service. */
	private ThumbnailService thumbnailService;

	/** The timout time. */
	private long timoutTime = Long.valueOf(readProperty("preview.thumbnail.asynch.startafter", "10000"));

	/** semaphore lock. */
	private static final ReentrantReadWriteLock ADD_CHILD_LOCK = new ReentrantReadWriteLock(true);

	/** The temporary synch storage. */
	private MemoryCache<String, NodeRef> temporarySynchStorage = new MemoryCache<String, NodeRef>(1000);

	/**
	 * Enum for allowed types for thubmnail generation of documents.
	 *
	 * @author bbanchev
	 */
	private enum ThumbnailGenerationMode {
		/** asynch after time. */
		ASYNCH("asynch"), /** synch during upload. */
		SYNCH("synch"), /** none. */
		NONE("none");

		/** The id. */
		private String id;

		/**
		 * Constructs new enum.
		 * 
		 * @param id
		 *            the string key for this enum
		 */
		private ThumbnailGenerationMode(String id) {
			this.id = id;

		}

		/**
		 * Get the mode for this id.
		 *
		 * @param id
		 *            is the toString of the enum or the mode id
		 * @return the found mode or asynch as default
		 */
		static ThumbnailGenerationMode getMode(String id) {
			if (id == null || id.trim().isEmpty()) {
				return ASYNCH;
			}
			ThumbnailGenerationMode directType = valueOf(id);
			if (directType != null) {
				return directType;
			} else if (ASYNCH.id.equals(id.toLowerCase())) {
				return ASYNCH;
			} else if (SYNCH.id.equals(id.toLowerCase())) {
				return SYNCH;
			} else if (NONE.id.equals(id.toLowerCase())) {
				return NONE;
			}
			return ASYNCH;
		}

	}

	/**
	 * Upload mode to use.
	 *
	 * @author bbanchev
	 */
	enum DocumentUploadMode {

		/** Dms custom mode. */
		CUSTOM, /** Direct upload. */
		DIRECT;

		/**
		 * Gets the mode.
		 *
		 * @param id
		 *            the id
		 * @return the mode
		 */
		public static DocumentUploadMode getMode(String id) {
			if (id == null || id.trim().isEmpty()) {
				return DIRECT;
			}
			DocumentUploadMode directType = valueOf(id);
			if (directType != null) {
				return directType;
			}
			return DIRECT;
		}
	}

	/**
	 * Execute internal. Wrapper for system user action.
	 * 
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(2);
		List<NodeRef> value = new ArrayList<NodeRef>(1);
		model.put(KEY_WORKING_MODE, "unknown");
		Boolean securityEnabled = Boolean.valueOf(req.getHeader("security.enabled"));
		model.put(SECURITY_ENABLED, securityEnabled);
		try {
			String serverPath = req.getServicePath();
			if (serverPath.endsWith("/cmf/instance/attach")) {
				attachRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/instance/dettach")) {
				detachRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/lock")) {
				lockRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/checkout")) {
				checkoutRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/unlock")) {
				unlockRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/checkin")) {
				checkinRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/cancelcheckout")) {
				cancelCheckoutRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/move")) {
				moveRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/copy")) {
				copyRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/version")) {
				historicVersionRequest(req, model, value);
			} else if (serverPath.endsWith("/cmf/document/revert")) {
				revertRequest(req, model, value);
			}
			model.put("results", value);
		} catch (ContentQuotaException e) {
			throw createStatus(413, "org.alfresco.service.cmr.usage.ContentQuotaException");
		} catch (WebScriptException e) {
			throw e;
		} catch (Exception e) {
			// capture exception, annotate it accordingly and re-throw
			if (e.getMessage() != null) {
				throw new WebScriptException(500,
						"Unexpected error occurred during document operation: " + e.getMessage(), e);
			} else {
				throw createStatus(500, "Unexpected error occurred during document operation!");

			}
		} finally {
			if (model.containsKey(PARENT_REF_LOCK_OWNER)) {
				@SuppressWarnings("unchecked")
				Collection<NodeRef> unlockedNodes = (Collection<NodeRef>) model.get(PARENT_REF);
				for (NodeRef nodeRef : unlockedNodes) {
					cmfLockService.lockNode(nodeRef, model.get(PARENT_REF_LOCK_OWNER).toString());
				}
			}
		}

		return model;
	}

	// ---------------process requests code-----------------------------
	/**
	 * Checkin request.
	 * 
	 * @param req
	 *            the req
	 * @param model
	 *            the model
	 * @param value
	 *            the value
	 * @throws JSONException
	 *             the jSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void checkinRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws JSONException, IOException {
		// delete a case attachment
		model.put(KEY_WORKING_MODE, "unlock");
		boolean authChanged = false;
		try {
			JSONObject request = new JSONObject(req.getContent().getContent());
			if (request.has(KEY_ATTACHMENT_ID)) {
				NodeRef updateable = checkinRequest(request);
				if (updateable != null) {
					value.add(updateable);
				}
			}
		} finally {
			if (authChanged) {
				AuthenticationUtil.popAuthentication();
			}
		}
	}

	/**
	 * Unlock request.
	 * 
	 * @param req
	 *            the req
	 * @param model
	 *            the model
	 * @param value
	 *            the value
	 * @throws JSONException
	 *             the jSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void unlockRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws JSONException, IOException {
		// delete a case attachment
		model.put(KEY_WORKING_MODE, "unlock");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			String lockUser = CMFService.getSystemUser();
			if (updateable != null) {
				cmfLockService.unlockNode(updateable, lockUser);
				value.add(updateable);
			}
		}
	}

	/**
	 * Checkout request. Node is processed using
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws JSONException
	 *             on some parse error
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 *             {@link org.alfresco.service.cmr.coci.CheckOutCheckInService#checkout(NodeRef)}
	 *             .
	 */
	private Map<String, Object> checkoutRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws JSONException, IOException {
		// delete a case attachment
		model.put(KEY_WORKING_MODE, "lock");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				prepareParent(model, nodeService.getPrimaryParent(updateable).getParentRef());
				updateable = serviceRegistry.getCheckOutCheckInService().checkout(updateable);
				value.add(updateable);
			}
		}
		return model;
	}

	/**
	 * Cancel checkout request. Invoke is processed by
	 * 
	 * @param req
	 *            the req
	 * @param model
	 *            the model
	 * @param value
	 *            the value
	 * @throws JSONException
	 *             the jSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 *             {@link org.alfresco.service.cmr.coci.CheckOutCheckInService#cancelCheckout(NodeRef)}
	 */
	private void cancelCheckoutRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws JSONException, IOException {
		model.put(KEY_WORKING_MODE, "unlock");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				NodeRef cancelCheckout = getServiceRegistry().getCheckOutCheckInService().cancelCheckout(updateable);
				value.add(cancelCheckout);
			}
		}
	}

	/**
	 * Lock request. Node is locked using
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws JSONException
	 *             on some parse error
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 *             {@link com.sirma.itt.cmf.integration.service.CMFLockService#lockNode(NodeRef, String)}
	 */
	private Map<String, Object> lockRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws JSONException, IOException {
		// delete a case attachment
		model.put(KEY_WORKING_MODE, "lock");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			String lockUser = CMFService.getSystemUser();
			if (updateable != null) {
				cmfLockService.lockNode(updateable, lockUser);
				value.add(updateable);
			}
		}
		return model;
	}

	/**
	 * Detach request. Node is deleted.
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws JSONException
	 *             on some parse error
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private Map<String, Object> detachRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws JSONException, IOException {
		model.put(KEY_WORKING_MODE, "dettach");
		JSONObject request = new JSONObject(req.getContent().getContent());
		NodeRef caseNode = null;
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				caseNode = cmfService.getNodeRef(request.getString(KEY_NODEID));
				if (caseNode == null) {
					throw createStatus(404, "document is not attached to specific case!");
				}
				prepareParent(model, caseNode);
				Pair<NodeRef, NodeRef> contentSubContainer = findContentSubContainer(updateable);
				nodeService.deleteNode(updateable);
				if (contentSubContainer.getSecond() != null) {
					prepareParent(model, contentSubContainer.getFirst());
					// delete the whole container
					nodeService.deleteNode(contentSubContainer.getSecond());
				}
				model.put("deleted", updateable.toString());
				value.add(caseNode);
			}
		} else {
			throw createStatus(500, "Invalid request. Document is not provided!");
		}
		return model;
	}

	/**
	 * Attach request. Invocation is processed by
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws Exception
	 *             on some error {@link #attachFile(FormData, Map)}
	 */
	private Map<String, Object> attachRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws Exception {
		model.put(KEY_WORKING_MODE, "attach");
		FormData data = extractFormData(req);
		if (data != null) {
			// process the attach
			attachFile(data, model);

			NodeRef attachFile = (NodeRef) model.get(KEY_DOCUMENT);
			if (attachFile != null) {
				value.add(attachFile);
				debug(attachFile, " version:", model.get("version"));
			}
		}
		return model;
	}

	/**
	 * Moving of document method. Source and target destinations are
	 * locked/unlocked during operation and new name is calculated using
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws Exception
	 *             on any error
	 */
	private Map<String, Object> copyRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws Exception {
		model.put(KEY_WORKING_MODE, "copy");
		JSONObject request = new JSONObject(req.getContent().getContent());
		NodeRef moveable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
		if (moveable != null) {
			NodeRef newParent = cmfService.getNodeRef(request.getString(KEY_DESTINATION));
			String filename = request.getString("name");
			NodeRef existingFile = cmfService.childByNamePath(newParent, filename);
			if (existingFile != null) {
				// update the name if duplicate found
				filename = getNameForDuplicateChild(filename, newParent, existingFile);
			}
			// unlock the destination first
			prepareParent(model, newParent);
			Pair<NodeRef, NodeRef> contentSubContainer = findContentSubContainer(moveable);
			// for read permission unlock is needed only in dest node.
			FileInfo attachFile = getServiceRegistry().getFileFolderService().copy(moveable, newParent, filename);
			if (attachFile != null) {
				model.put(KEY_DOCUMENT, attachFile);
				// set the versioning aspect
				ensureVersioningEnabled(attachFile.getNodeRef(), true, false);
				model.put("versionProperties", populateProperties(attachFile.getNodeRef(), true));
				// cmfLockService.lockNode(attachFile);
				value.add(attachFile.getNodeRef());
				// TODO on param set
				if (contentSubContainer.getSecond() != null) {
					// delete the whole container
					getServiceRegistry().getFileFolderService().copy(contentSubContainer.getSecond(), newParent, null);
				}

			}
		}
		return model;

	}

	/**
	 * Moving of document method. Source and target destinations are
	 * locked/unlocked during operation and new name is calculated using
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws Exception
	 *             on any error
	 */
	private Map<String, Object> moveRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws Exception {
		model.put(KEY_WORKING_MODE, "move");
		JSONObject request = new JSONObject(req.getContent().getContent());
		NodeRef moveable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
		if (moveable != null) {
			NodeRef newParent = cmfService.getNodeRef(request.getString(KEY_DESTINATION));
			String filename = getNodeService().getProperty(moveable, ContentModel.PROP_NAME).toString();
			NodeRef existingFile = cmfService.childByNamePath(newParent, filename);
			if (existingFile != null) {
				filename = getNameForDuplicateChild(filename, newParent, existingFile);
				// update the name
				getNodeService().setProperty(moveable, ContentModel.PROP_NAME, filename);
			}
			// unlock the destination first
			prepareParent(model, newParent);
			Pair<NodeRef, NodeRef> contentSubContainer = findContentSubContainer(moveable);
			ChildAssociationRef parentAssocs = getNodeService().getPrimaryParent(moveable);
			String localName = parentAssocs.getQName().getLocalName();
			NodeRef parentRefOld = parentAssocs.getParentRef();
			String lockOwner = cmfLockService.getLockedOwner(parentRefOld);
			// unlock the original parent before move
			prepareParent(model, parentRefOld, false);

			newParent = getParentForNode(model, newParent);

			ChildAssociationRef moveNode = nodeService.moveNode(moveable, newParent, parentAssocs.getTypeQName(),
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localName));
			NodeRef attachFile = moveNode.getChildRef();
			if (attachFile != null) {
				model.put(KEY_DOCUMENT, attachFile);

				// cmfLockService.lockNode(attachFile);
				model.put("versionProperties", populateProperties(attachFile, false));
				value.add(attachFile);
				if (contentSubContainer.getSecond() != null) {
					// delete the whole container
					getServiceRegistry().getFileFolderService().move(contentSubContainer.getSecond(), newParent, null);
				}
			}
			// lock the original parent again
			cmfLockService.lockNode(parentRefOld, lockOwner);
		}
		return model;

	}

	private NodeRef getParentForNode(Map<String, Object> model, NodeRef newParent) {
		QName type = getNodeService().getType(newParent);
		boolean isParentContent = getDataDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT);
		if (isParentContent) {
			newParent = findFileToFileContainer(model, newParent);
		} else {
			newParent = findDestinitionContainer(newParent, DocumentUploadMode.DIRECT);
		}
		return newParent;
	}

	/**
	 * Populate properties in the model for the provided node. Properties are
	 * keyed by shorted qname as string
	 * 
	 * @param nodeRef
	 *            the node ref to get properties for
	 * @param setDefaultVersion
	 *            whether to set version 1.0 as current if none is available.
	 * @return the map of properties for node.
	 */
	private Map<String, Serializable> populateProperties(NodeRef nodeRef, boolean setDefaultVersion) {
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		Serializable versionLabel = properties.remove(ContentModel.PROP_VERSION_LABEL);
		Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(properties.size());
		for (Entry<QName, Serializable> prop : properties.entrySet()) {
			versionProperties.put(prop.getKey().toPrefixString(getNamespaceService()), prop.getValue());
		}
		if (versionLabel != null) {
			versionProperties.put("version", versionLabel);
		} else if (setDefaultVersion) {
			versionProperties.put("version", "1.0");
		}
		ContentData content = (ContentData) properties.get(ContentModel.PROP_CONTENT);
		if (content != null) {
			versionProperties.put("cm:content.mimetype", content.getMimetype());
			versionProperties.put("cm:content.size", content.getSize());
			versionProperties.put("cm:content.encoding", content.getEncoding());
		}
		return versionProperties;
	}

	/**
	 * Retrieve a historic version for node. Properties are populated as well.
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws JSONException
	 *             on parse error
	 * @throws IOException
	 *             on io error
	 *             {@link #getNameForDuplicateChild(String, NodeRef, NodeRef)}
	 *             so no duplicate child exception occurs.
	 */
	private Map<String, Object> historicVersionRequest(WebScriptRequest req, Map<String, Object> model,
			List<NodeRef> value) throws JSONException, IOException {
		model.put(KEY_WORKING_MODE, "version");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				if (request.has("version")) {
					try {
						Version version = serviceRegistry.getVersionService().getVersionHistory(updateable)
								.getVersion(request.getString("version"));
						NodeRef frozenStateNodeRef = version.getFrozenStateNodeRef();
						value.add(frozenStateNodeRef);
						model.put("versionProperties", populateProperties(frozenStateNodeRef, false));
					} catch (VersionDoesNotExistException e) {
						throw createStatus(500, "Invalid request. Version does not exists!");
					}
				}
			}
		} else {
			throw createStatus(500, "Invalid request. document is not provided!");
		}
		return model;

	}

	/**
	 * Retrieve a historic version for node. Properties are populated as well.
	 * 
	 * @param req
	 *            the req to process
	 * @param model
	 *            the final model to update
	 * @param value
	 *            the list of updated nodes
	 * @return the map of updated model
	 * @throws JSONException
	 *             on parse error
	 * @throws IOException
	 *             on io error
	 *             {@link #getNameForDuplicateChild(String, NodeRef, NodeRef)}
	 *             so no duplicate child exception occurs.
	 */
	private Map<String, Object> revertRequest(WebScriptRequest req, Map<String, Object> model, List<NodeRef> value)
			throws JSONException, IOException {
		model.put(KEY_WORKING_MODE, "revert");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				if (request.has("version")) {
					try {
						System.out.println(nodeService.getProperties(updateable));
						Version version = serviceRegistry.getVersionService().getVersionHistory(updateable)
								.getVersion(request.getString("version"));
						serviceRegistry.getVersionService().revert(updateable, version);
						// checkout and checkin to create a new version
						updateable = serviceRegistry.getCheckOutCheckInService().checkout(updateable);
						// model for update should exists - no has() check
						updateable = checkin(request.getBoolean(MAJORVERSION), request.getString(DESCRIPTION),
								updateable);
						value.add(updateable);
						model.put("versionProperties", populateProperties(updateable, false));
					} catch (VersionDoesNotExistException e) {
						throw createStatus(500, "Invalid request. Version does not exists!");
					}
				}
			}
		} else {
			throw createStatus(500, "Invalid request. document is not provided!");
		}
		return model;

	}

	/**
	 * Dispatch Checkin request.
	 * 
	 * @param request
	 *            the req to process
	 * @return the node ref that is checkin on success, null otherwise
	 * @throws JSONException
	 *             on parse error
	 */
	private NodeRef checkinRequest(JSONObject request) throws JSONException {
		NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
		if (updateable != null) {
			String description = request.has(DESCRIPTION) ? request.getString(DESCRIPTION)
					: "Automatic checkin comment";
			Boolean majorVersion = request.has(MAJORVERSION) ? Boolean.valueOf(request.getString(MAJORVERSION))
					: Boolean.TRUE;
			if (nodeService.hasAspect(updateable, ContentModel.ASPECT_WORKING_COPY)) {
				NodeRef checkin = checkin(majorVersion, description, updateable);
				// update with some props
				if (request.has(KEY_PROPERTIES)) {
					Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
					nodeService.addProperties(checkin, properties);
				}
				return checkin;
			} else {
				// get the working copy
				NodeRef workingCopyNode = getServiceProxy().getWorkingCopy(updateable);
				updateable = checkin(majorVersion, description, workingCopyNode);
				// update with some props
				if (request.has(KEY_PROPERTIES)) {
					Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
					nodeService.addProperties(updateable, properties);
				}
				return updateable;
			}
		}
		return null;
	}

	// ------------------end process requests
	// code--------------------------------

	/**
	 * Inner class wrapping and providing access to a ContentData property.
	 */
	public class ContentDataHelper implements Content, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -7819328543933312278L;

		/** The node ref. */
		private NodeRef nodeRef = null;

		/** The content service. */
		private ContentService contentService;

		/** The content data. */
		private ContentData contentData;

		/** The property. */
		private QName property;

		/**
		 * Constructor.
		 * 
		 * @param nodeRef
		 *            the node ref
		 * @param data
		 *            the data
		 */
		public ContentDataHelper(NodeRef nodeRef, ContentData data) {
			contentData = data;
			property = ContentModel.PROP_CONTENT;
			this.nodeRef = nodeRef;
		}

		/**
		 * Gets the content.
		 *
		 * @return the content
		 * @see org.alfresco.repo.jscript.ScriptNode.ScriptContent#getContent()
		 */
		@Override
		public String getContent() {
			ContentReader reader = getContentService().getReader(nodeRef, property);
			return ((reader != null) && reader.exists()) ? reader.getContentString() : "";
		}

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
		 * Gets the input stream.
		 *
		 * @return the input stream
		 * @see org.springframework.extensions.surf.util.Content#getInputStream()
		 */
		@Override
		public InputStream getInputStream() {
			ContentReader reader = getContentService().getReader(nodeRef, property);
			return ((reader != null) && reader.exists()) ? reader.getContentInputStream() : null;
		}

		/**
		 * Gets the reader.
		 *
		 * @return the reader
		 * @see org.springframework.extensions.surf.util.Content#getReader()
		 */
		@Override
		public Reader getReader() {
			ContentReader reader = getContentService().getReader(nodeRef, property);

			if ((reader != null) && reader.exists()) {
				try {
					return (contentData.getEncoding() == null) ? new InputStreamReader(reader.getContentInputStream())
							: new InputStreamReader(reader.getContentInputStream(), contentData.getEncoding());
				} catch (IOException e) {
					// NOTE: fall-through
				}
			}
			return null;
		}

		/**
		 * Set the content stream.
		 * 
		 * @param content
		 *            Content string to set
		 */
		public void setContent(String content) {
			ContentWriter writer = getContentService().getWriter(nodeRef, property, true);
			// use existing mimetype value
			writer.setMimetype(getMimetype());
			writer.putContent(content);

			// update cached variables after putContent()
			contentData = (ContentData) getProperty(property);
		}

		/**
		 * Set the content stream from another content object.
		 * 
		 * @param content
		 *            ScriptContent to set
		 */
		public void write(Content content) {
			ContentWriter writer = getContentService().getWriter(nodeRef, property, true);
			writer.putContent(content.getInputStream());
			// update cached variables after putContent()
			contentData = (ContentData) getProperty(property);
		}

		/**
		 * Set the content stream from another content object.
		 * 
		 * @param content
		 *            ScriptContent to set
		 * @param applyMimetype
		 *            If true, apply the mimetype from the Content object, else
		 *            leave the original mimetype
		 * @param guessEncoding
		 *            If true, guess the encoding from the underlying input
		 *            stream, else use encoding set in the Content object as
		 *            supplied.
		 */
		public void write(Content content, boolean applyMimetype, boolean guessEncoding) {
			ContentWriter writer = getContentService().getWriter(nodeRef, property, true);
			InputStream is = null;
			if (applyMimetype) {
				writer.setMimetype(content.getMimetype());
			} else {
				writer.setMimetype(getMimetype());
			}
			if (guessEncoding) {
				is = new BufferedInputStream(content.getInputStream());
				is.mark(1024);
				writer.setEncoding(guessEncoding(is, false));
				try {
					is.reset();
				} catch (IOException e) {
				}
			} else {
				writer.setEncoding(content.getEncoding());
				is = content.getInputStream();
			}
			writer.putContent(is);

			// update cached variables after putContent()
			contentData = (ContentData) getProperty(property);
		}

		/**
		 * Set the content stream from another input stream.
		 * 
		 * @param inputStream
		 *            the input stream
		 */
		public void write(InputStream inputStream) {
			ContentService contentService = getContentService();
			ContentWriter writer = contentService.getWriter(nodeRef, property, true);
			writer.putContent(inputStream);

			// update cached variables after putContent()
			contentData = (ContentData) getProperty(property);
		}

		/**
		 * Delete the content stream.
		 */
		public void delete() {
			ContentService contentService = getContentService();
			ContentWriter writer = contentService.getWriter(nodeRef, property, true);
			OutputStream output = writer.getContentOutputStream();
			try {
				output.close();
			} catch (IOException e) {
				// NOTE: fall-through
			}
			writer.setMimetype(null);
			writer.setEncoding(null);

			// update cached variables after putContent()
			contentData = (ContentData) getProperty(property);
		}

		/**
		 * Gets the size.
		 *
		 * @return the size
		 * @see org.springframework.extensions.surf.util.Content#getSize()
		 */
		@Override
		public long getSize() {
			return contentData.getSize();
		}

		/**
		 * Gets the mimetype.
		 *
		 * @return the mimetype
		 * @see org.springframework.extensions.surf.util.Content#getMimetype()
		 */
		@Override
		public String getMimetype() {
			return contentData.getMimetype();
		}

		/**
		 * Gets the encoding.
		 *
		 * @return the encoding
		 * @see org.springframework.extensions.surf.util.Content#getEncoding()
		 */
		@Override
		public String getEncoding() {
			return contentData.getEncoding();
		}

		/**
		 * Sets the encoding.
		 * 
		 * @param encoding
		 *            the new encoding
		 */
		public void setEncoding(String encoding) {
			contentData = ContentData.setEncoding(contentData, encoding);
			setProperty(property, contentData);

			// update cached variables after putContent()
			contentData = (ContentData) getProperty(property);
		}

		/**
		 * Gets the property.
		 * 
		 * @param property
		 *            the property
		 * @return the property
		 */
		private Serializable getProperty(QName property) {
			return getNodeService().getProperty(nodeRef, property);
		}

		/**
		 * Sets the property.
		 * 
		 * @param property
		 *            the property
		 * @param data
		 *            the data
		 */
		private void setProperty(QName property, Serializable data) {
			getNodeService().setProperty(nodeRef, property, data);
		}

		/**
		 * Sets the mimetype.
		 * 
		 * @param mimetype
		 *            the new mimetype
		 */
		public void setMimetype(String mimetype) {
			contentData = ContentData.setMimetype(contentData, mimetype);
			setProperty(property, contentData);

			// update cached variables after putContent()
			contentData = (ContentData) getProperty(property);
		}

		/**
		 * Guess the mimetype for the given filename - uses the extension to
		 * match on system mimetype map.
		 * 
		 * @param filename
		 *            the filename
		 */
		public void guessMimetype(String filename) {
			ContentService contentService = getContentService();
			ContentReader reader = contentService.getReader(nodeRef, property);
			setMimetype(getServiceProxy().guessMimetype(filename, reader));
		}

		/**
		 * Guess the character encoding of a file. For non-text files UTF-8
		 * default is applied, otherwise the appropriate encoding (such as
		 * UTF-16 or similar) will be appiled if detected.
		 */
		public void guessEncoding() {
			setEncoding(guessEncoding(getInputStream(), true));
		}

		/**
		 * Guess encoding.
		 * 
		 * @param in
		 *            the in
		 * @param close
		 *            the close
		 * @return the string
		 */
		private String guessEncoding(InputStream in, boolean close) {
			String encoding = UTF_8;
			try {
				if (in != null) {
					Charset charset = serviceRegistry.getMimetypeService().getContentCharsetFinder().getCharset(in,
							getMimetype());
					encoding = charset.name();
				}
			} finally {
				if (close) {
					IOUtils.closeQuietly(in);
				}
			}
			return encoding;
		}

	}

	/**
	 * Attach file.
	 * 
	 * @param formdata
	 *            the formdata
	 * @param model
	 *            the model
	 * @throws Exception
	 *             the exception
	 */
	private void attachFile(final FormData formdata, final Map<String, Object> model) throws Exception {
		final SiteService siteService = getSiteService();
		final NodeService nodeService = getNodeService();
		try {
			String filename = null;
			Content content = null;
			String mimetype = null;
			String siteId = null;
			SiteInfo site = null;
			String containerId = null;
			NodeRef container = null;
			String destination = null;
			NodeRef destNode = null;

			// Upload specific
			String uploadDirectory = null;
			String contentType = null;
			Set<String> aspects = new TreeSet<String>();
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			// If a filename clashes for a versionable
			boolean overwrite = true;

			// Update specific
			String updateNodeRef = null;
			boolean majorVersion = false;
			String description = "";

			// Parse file attributes
			ThumbnailGenerationMode thumbGenerationMode = ThumbnailGenerationMode.ASYNCH;

			DocumentUploadMode uploadMode = DocumentUploadMode.DIRECT;
			FormField[] fields = formdata.getFields();
			for (FormField field : fields) {
				String fieldValue = field.getValue();
				if ("filedata".equals(field.getName())) {
					if (field.getIsFile()) {
						filename = field.getFilename();
						content = field.getContent();
						mimetype = field.getMimetype();
					} else {
						filename = field.getFilename() == null ? GUID.generate() : field.getFilename();
						content = field.getContent();
						mimetype = field.getMimetype();
					}
				} else if ("siteid".equals(field.getName())) {
					siteId = field.getValue();
				} else if ("containerid".equals(field.getName())) {
					containerId = field.getValue();
				} else if (KEY_DESTINATION.equals(field.getName())) {
					destination = field.getValue();
				} else if ("updatenoderef".equals(field.getName())) {
					updateNodeRef = field.getValue();
				} else if ("uploaddirectory".equals(field.getName())) {
					uploadDirectory = field.getValue();
				} else if (DESCRIPTION.equals(field.getName())) {
					description = field.getValue();
				} else if ("contenttype".equals(field.getName())) {
					contentType = field.getValue();
				} else if ("aspects".equals(field.getName())) {
					String[] split = fieldValue.replaceAll("\\s+", "").split(",");
					aspects = new TreeSet<String>(Arrays.asList(split));
					split = null;
				} else if ("properties".equals(field.getName())) {
					properties = toMap(new JSONObject(field.getValue()));
				} else if (MAJORVERSION.equals(field.getName())) {
					majorVersion = Boolean.valueOf(field.getValue()).booleanValue();
				} else if ("overwrite".equals(field.getName())) {
					overwrite = Boolean.valueOf(field.getValue()).booleanValue();
				} else if ("thumbnail".equals(field.getName())) {
					thumbGenerationMode = ThumbnailGenerationMode.getMode(field.getValue());
				} else if ("uploadMode".equals(field.getName())) {
					uploadMode = DocumentUploadMode.getMode(field.getValue());
				}
			}
			debug("[filename=", filename, ", content=", content, ", mimetype=", mimetype, ", siteId=", siteId,
					", containerId=", containerId, ", destination=", destination, ", updateNodeRef=", updateNodeRef,
					", uploadDirectory=", uploadDirectory, ", description=", description, ", contentType=", contentType,
					", aspects=", aspects, ", properties=", properties, ", overwrite=", overwrite, ", majorVersion=",
					majorVersion, ", description=", description, ", thumbnail=", thumbGenerationMode, ", mode=",
					uploadMode, "]");
			// Ensure mandatory file attributes have been located. Need either
			// destination, or site + container or updateNodeRef
			if (((filename == null) || (content == null))
					|| ((destination == null) && ((siteId == null) || (containerId == null)) && (updateNodeRef == null)
							&& (uploadDirectory == null))) {
				throw createStatus(400, "Required parameters are missing");
			}

			/**
			 * Site or Non-site?
			 */
			if ((siteId != null) && (siteId.length() > 0)) {
				/**
				 * Site mode. Need valid site and container. Try to create
				 * container if it doesn't exist.
				 */
				site = siteService.getSite(siteId);
				if (site == null) {
					throw createStatus(404, "Site (" + siteId + ") not found.");
				}

				container = siteService.getContainer(siteId, containerId);
				if (container == null) {
					try {
						// Create container since it didn't exist
						container = siteService.createContainer(siteId, containerId, null, null);
					} catch (Exception e) {
						// Error could be that it already exists (was created
						// exactly after our previous check) but also something
						// else
						container = siteService.getContainer(siteId, containerId);
						if (container == null) {
							// Container still doesn't exist, then re-throw
							// error
							throw createStatus(500, e.toString());
						}
						// Since the container now exists we can proceed as
						// usual
					}
				}

				if (container == null) {
					throw createStatus(404, "Component container (" + containerId + ") not found.");
				}

				destNode = container;
			} else if (destination != null) {
				/**
				 * Non-Site mode. Need valid destination nodeRef.
				 */
				destNode = cmfService.getNodeRef(destination);
				if (destNode == null) {
					throw createStatus(404, "Destination (" + destination + ") not found.");
				}

				destNode = getParentForNode(model, destNode);
			}
			/**
			 * Update existing or Upload new?
			 */
			if (updateNodeRef != null) {
				NodeRef updatable = cmfService.getNodeRef(updateNodeRef);
				NodeRef parentRef = nodeService.getPrimaryParent(updatable).getParentRef();
				// unlock and fill model
				prepareParent(model, parentRef);

				// set the version props so to be kept as version info
				if (properties != null) {
					nodeService.addProperties(updatable, properties);
				}
				updatable = updateExisting(updatable, filename, content, majorVersion, description,
						thumbGenerationMode);
				// Record the file details ready for generating the response
				model.put(KEY_DOCUMENT, updatable);
				model.put("properties", convertPropeties(nodeService.getProperties(updatable)));
			} else {
				/**
				 * Upload new file to destNode (calculated earlier) + optional
				 * subdirectory
				 */
				if (uploadDirectory != null) {
					List<NodeRef> destNodeLocal = cmfService.getNodesByXPath(uploadDirectory);
					if ((destNodeLocal == null) || (destNodeLocal.size() != 1)) {
						throw createStatus(404,
								"Cannot upload file since upload directory '" + uploadDirectory + "' does not exist.");
					}
					destNode = destNodeLocal.get(0);
				}
				/**
				 * Existing file handling.
				 */
				prepareParent(model, destNode);

				NodeRef existingFile = cmfService.childByNamePath(destNode, filename);
				if (existingFile != null) {
					// File already exists, decide what to do
					if (nodeService.hasAspect(existingFile, ContentModel.ASPECT_VERSIONABLE) && overwrite) {

						existingFile = updateExisting(existingFile, filename, content, majorVersion, description,
								thumbGenerationMode);
						// Record the file details ready for generating the
						// response
						model.put(KEY_DOCUMENT, existingFile);
						model.put(KEY_PROPERTIES, convertPropeties(nodeService.getProperties(existingFile)));
						putVersionInModel(model, majorVersion, existingFile);
						return;
					} else {
						filename = getNameForDuplicateChild(filename, destNode, existingFile);
						if (properties.containsKey(ContentModel.PROP_NAME)) {
							properties.put(ContentModel.PROP_NAME, filename);
						}
					}
				}

				/**
				 * Create a new file.
				 */

				QName nodeType = ContentModel.TYPE_CONTENT;
				QName contenTypeQname = null;
				final NamespaceService namespaceService = serviceRegistry.getNamespaceService();
				if (contentType != null) {
					contenTypeQname = QName.resolveToQName(namespaceService, contentType.toString());

					// Ensure that we are performing a specialise
					DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
					if ((nodeType.equals(contenTypeQname) == false)
							&& (dictionaryService.isSubClass(contenTypeQname, nodeType) == true)) {
						contenTypeQname = nodeType;
					}
				}

				RetryingTransactionHelper retryingTransactionHelper = new RetryingTransactionHelper();
				retryingTransactionHelper.setMaxRetries(1);
				retryingTransactionHelper.setReadOnly(false);
				retryingTransactionHelper.setTransactionService(serviceRegistry.getTransactionService());
				final NodeRef destNodeFinal = destNode;
				final String filenameFinal = filename;
				final QName contenTypeQnameFinal = contenTypeQname;
				final Content contentFinal = content;
				final Map<QName, Serializable> propertiesFinal = properties;
				final Set<String> aspectsFinal = aspects;
				ContentDataHelper data = retryingTransactionHelper
						.doInTransaction(new RetryingTransactionCallback<ContentDataHelper>() {

							@Override
							public ContentDataHelper execute() throws Throwable {
								NodeRef newFile = serviceRegistry.getFileFolderService().create(destNodeFinal,
										filenameFinal,
										contenTypeQnameFinal == null ? ContentModel.TYPE_CONTENT : contenTypeQnameFinal)
										.getNodeRef();
								MimetypeService mimetypeService = serviceRegistry.getMimetypeService();
								String mimetypeDetected = mimetypeService.guessMimetype(filenameFinal);
								ContentDataHelper data = new ContentDataHelper(newFile,
										new ContentData(null, mimetypeDetected, 0L, UTF_8));
								data.write(contentFinal, false, true);
								// Additional aspects?
								// custom - add properties
								if (propertiesFinal != null) {
									nodeService.addProperties(newFile, propertiesFinal);
								}
								for (String aspect : aspectsFinal) {
									QName aspectQName = QName.resolveToQName(namespaceService, aspect);
									if (ContentModel.ASPECT_VERSIONABLE.equals(aspectQName)) {
										ensureVersioningEnabled(newFile, true, false);
									} else {
										nodeService.addAspect(newFile, aspectQName, null);
									}
								}

								// Extract the metadata
								extractMetadata(newFile);
								return data;
							}
						}, false, true);
				NodeRef newFile = data.nodeRef;

				createThumbnail(data, "doclib", thumbGenerationMode);
				// Record the file details ready for generating the response
				model.put(KEY_DOCUMENT, newFile);
				model.put(KEY_PROPERTIES, convertPropeties(nodeService.getProperties(newFile)));
			}
			if (model.containsKey(KEY_DOCUMENT)) {
				putVersionInModel(model, majorVersion, (NodeRef) model.get(KEY_DOCUMENT));
			}
			// final cleanup of temporary resources created during request
			// processing
			getServiceProxy().cleanFormData(formdata);
		} finally {
			try {
				getServiceProxy().cleanFormData(formdata);
			} catch (Exception ce) {
				// NOTE: ignore
			}
		}
		return;
	}

	/**
	 * Find destinition container.
	 *
	 * @param destNode
	 *            the dest node
	 * @param uploadMode
	 *            the upload mode
	 * @return the node ref
	 */
	private NodeRef findDestinitionContainer(final NodeRef destNode, final DocumentUploadMode uploadMode) {
		return serviceRegistry.getRetryingTransactionHelper()
				.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

					@Override
					public NodeRef execute() throws Throwable {
						if (uploadMode == DocumentUploadMode.CUSTOM) {
							return cmfService.getWorkingDir(destNode);
						} else if (isDestinationdocumentLibrary(destNode)) {
							return cmfService.getWorkingDir(destNode);
						}
						return destNode;
					}
				}, false, true);
	}

	/**
	 * Convert propeties.
	 *
	 * @param props
	 *            the props
	 * @return the map
	 */
	private Map<String, String> convertPropeties(Map<QName, Serializable> props) {
		Map<String, String> newProps = new HashMap<String, String>();
		for (Entry<QName, Serializable> prop : props.entrySet()) {
			Serializable propValue = prop.getValue();
			String strPropValue = null;
			if (propValue == null) {
				propValue = "";
			}
			if (propValue instanceof Collection || propValue instanceof Map) {
				if (propValue instanceof MLText) {
					strPropValue = ((MLText) propValue).getDefaultValue();
				}
			} else {
				strPropValue = DefaultTypeConverter.INSTANCE.convert(String.class, propValue);
			}
			newProps.put(prop.getKey().toPrefixString(serviceRegistry.getNamespaceService()), strPropValue);
		}
		return newProps;
	}

	/**
	 * Check if the destination is the document library for this site and
	 * generates the hierarchy structure.
	 *
	 * @param destNode
	 *            is the node to check
	 * @return true if this is the document library
	 */
	private boolean isDestinationdocumentLibrary(NodeRef destNode) {
		SiteInfo site = getSiteService().getSite(destNode);
		if (site != null) {
			NodeRef library = getSiteService().getContainer(site.getShortName(), "documentLibrary");
			if (destNode.equals(library)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a file as e child of another file. The code actually is creating a
	 * new folder with the id of the parent file and upload the child file into
	 * it.
	 * 
	 * @param model
	 *            current model for the response
	 * @param destNode
	 *            is the destination node - the parent
	 * @return the destination node to create node into
	 */
	private NodeRef findFileToFileContainer(final Map<String, Object> model, NodeRef destNode) {
		NodeRef destinationParent = destNode;
		try {
			ADD_CHILD_LOCK.writeLock().lock();
			Pair<NodeRef, NodeRef> findContentSubContainer = findContentSubContainer(destinationParent);
			final NodeRef parentRef = findContentSubContainer.getFirst();
			NodeRef childByName = findContentSubContainer.getSecond();
			prepareParent(model, parentRef);
			final String nodeId = destinationParent.getId();
			if (childByName == null) {
				if (!temporarySynchStorage.contains(nodeId)) {
					destinationParent = serviceRegistry.getRetryingTransactionHelper()
							.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

								@Override
								public NodeRef execute() throws Throwable {
									final Map<QName, Serializable> props = Collections
											.singletonMap(ContentModel.PROP_NAME, (Serializable) nodeId);
									return nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS,
											QName.createQName(CMFModel.EMF_MODEL_1_0_URI, nodeId),
											ContentModel.TYPE_FOLDER, props).getChildRef();
								}
							}, false, true);
					temporarySynchStorage.put(nodeId, destNode);
				} else {
					destinationParent = temporarySynchStorage.get(nodeId);
				}
			} else {
				destinationParent = childByName;
				// now is visible in the new transaction so remove it
				temporarySynchStorage.remove(nodeId);
			}

		} finally {
			ADD_CHILD_LOCK.writeLock().unlock();
		}
		return destinationParent;
	}

	/**
	 * Find content sub container.
	 *
	 * @param destNode
	 *            is the content node
	 * @return Pair(destnode parent, destnode files container)
	 */
	protected Pair<NodeRef, NodeRef> findContentSubContainer(NodeRef destNode) {
		ChildAssociationRef primaryParent = nodeService.getPrimaryParent(destNode);
		NodeRef parentRef = primaryParent.getParentRef();
		String nodeId = destNode.getId();
		NodeRef childByName = getNodeService().getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, nodeId);
		return new Pair<NodeRef, NodeRef>(parentRef, childByName);
	}

	/**
	 * Creates the thumbnail based on the content data provided.
	 *
	 * @param contentData
	 *            the content data used as wrapper for needed params
	 * @param thumbnailName
	 *            the thumbnail name is the thumbnail data to create
	 * @param thumbnailMode
	 *            the mode for generating thumbnail
	 * @return the script thumbnail
	 */
	private NodeRef createThumbnail(ContentDataHelper contentData, String thumbnailName,
			ThumbnailGenerationMode thumbnailMode) {
		if (thumbnailMode == ThumbnailGenerationMode.NONE) {
			return null;
		}
		final NodeRef nodeRef = contentData.nodeRef;
		// Use the thumbnail registy to get the details of the thumbail
		ThumbnailRegistry registry = getThumbnailService().getThumbnailRegistry();
		final ThumbnailDefinition details = registry.getThumbnailDefinition(thumbnailName);
		if (details == null) {
			// Throw exception
			throw new ScriptException("The thumbnail name '" + thumbnailName + "' is not registered");
		}

		if (!registry.isThumbnailDefinitionAvailable(contentData.contentData.getContentUrl(), contentData.getMimetype(),
				contentData.getSize(), nodeRef, details)) {
			debug("Unable to create thumbnail '", details.getName(), "' for ", contentData.getMimetype(),
					" as no transformer is currently available");
			return null;
		}

		// Have the thumbnail created
		if (thumbnailMode == ThumbnailGenerationMode.SYNCH) {
			try {
				return getThumbnailService().createThumbnail(nodeRef, ContentModel.PROP_CONTENT, details.getMimetype(),
						details.getTransformationOptions(), details.getName());
			} catch (Exception e) {
				getLogger().error(e);
			}
		}
		final Action action = ThumbnailHelper.createCreateThumbnailAction(details, serviceRegistry);
		action.getActionCondition(0).getParameterValues().put(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_PERIOD,
				10L);
		action.getActionCondition(0).getParameterValues().put(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_COUNT,
				10);
		// action.getActionCondition(0).setInvertCondition(true);
		final String runAsUser = AuthenticationUtil.getRunAsUser();

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				AuthenticationUtil.runAs(new RunAsWork<Void>() {

					@Override
					public Void doWork() throws Exception {
						debug("Going to generate preview for ", nodeRef);
						getActionService().executeAction(action, nodeRef, true, true);
						return null;
					}
				}, runAsUser);
			}
		};
		timer.schedule(task, timoutTime);
		return null;
	}

	/**
	 * Gets the action service.
	 * 
	 * @return the action service
	 */
	private ActionService getActionService() {
		if (actionService == null) {
			actionService = getServiceRegistry().getActionService();
		}
		return actionService;
	}

	/**
	 * Gets the thumbnail service.
	 * 
	 * @return the thumbnail service
	 */
	private ThumbnailService getThumbnailService() {
		if (thumbnailService == null) {
			thumbnailService = getServiceRegistry().getThumbnailService();
		}
		return thumbnailService;
	}

	/**
	 * Gets the name for duplicate child if child already exists. _{d} suffix
	 * counter is incremented untile empty slot is found.
	 * 
	 * @param filename
	 *            the filename
	 * @param destNode
	 *            the target node parent
	 * @param existingFile
	 *            the initial child with this name.
	 * @return the unique name for duplicate child
	 */
	private String getNameForDuplicateChild(String filename, NodeRef destNode, NodeRef existingFile) {
		// Upload component was configured to find a new unique
		// name for clashing filenames
		int counter = 1;
		String tmpFilename = null;
		int dotIndex = 0;

		char indexPrefix = '_';
		while (existingFile != null) {
			dotIndex = filename.lastIndexOf(".");
			if (dotIndex == 0) {
				// File didn't have a proper 'name' instead it
				// had just a suffix and started with a ".",
				// create "1.txt"
				tmpFilename = counter + filename;
			} else if (dotIndex > 0) {
				// Filename contained ".", create
				// "filename-1.txt"
				tmpFilename = filename.substring(0, dotIndex) + indexPrefix + counter + filename.substring(dotIndex);
			} else {
				// Filename didn't contain a dot at all, create
				// "filename-1"
				tmpFilename = filename + indexPrefix + counter;
			}
			existingFile = cmfService.childByNamePath(destNode, tmpFilename);
			counter++;
		}
		filename = tmpFilename;
		return filename;
	}

	/**
	 * Simultaneous check for duplicate name of working copy and original node,
	 * based on new node name.
	 * 
	 * @param filename
	 *            is the initial filename
	 * @param destNode
	 *            is the parent node
	 * @param existingFile
	 *            is the current node, under this name
	 * @param workingCopyLabel
	 *            is the label for working copy to look for.
	 * @return pair of the new calculated names (name,working copy name)
	 */
	private Pair<String, String> getNameForDuplicateWorkingCopyChild(String filename, NodeRef destNode,
			NodeRef existingFile, String workingCopyLabel) {
		// Upload component was configured to find a new unique
		// name for clashing filenames
		int counter = 0;
		String tmpFilename = null;
		String tmpWorkFilename = null;
		int dotIndex = 0;
		NodeRef existingWorkingFile = existingFile;
		String indexPrefix = "_";
		while ((existingFile != null) || (existingWorkingFile != null)) {
			dotIndex = filename.lastIndexOf(".");
			if (dotIndex == 0) {
				// File didn't have a proper 'name' instead it
				// had just a suffix and started with a ".",
				// create "1.txt"
				tmpFilename = counter + filename;
				tmpWorkFilename = counter + workingCopyLabel + filename;
			} else {
				String newNamePrefix = null;
				if (counter > 0) {
					newNamePrefix = indexPrefix + counter;
				} else {
					newNamePrefix = "";
				}
				if (dotIndex > 0) {
					// Filename contained ".", create
					// "filename-1.txt"
					String startText = filename.substring(0, dotIndex) + newNamePrefix;
					tmpFilename = startText + filename.substring(dotIndex);

					tmpWorkFilename = startText + workingCopyLabel + filename.substring(dotIndex);
				} else {
					// Filename didn't contain a dot at all, create
					// "filename-1"
					tmpFilename = filename + newNamePrefix;
					tmpWorkFilename = tmpFilename + workingCopyLabel;
				}
			}
			existingFile = cmfService.childByNamePath(destNode, tmpFilename);
			existingWorkingFile = cmfService.childByNamePath(destNode, tmpWorkFilename);
			counter++;
		}
		filename = tmpFilename;
		return new Pair<String, String>(tmpFilename, tmpWorkFilename);
	}

	/**
	 * Prepare parent by unlocking if needed and adding in model for later re
	 * lock.
	 * 
	 * @param model
	 *            the model
	 * @param destNode
	 *            the dest node
	 */
	private void prepareParent(Map<String, Object> model, NodeRef destNode) {
		prepareParent(model, destNode, true);
	}

	/**
	 * Prepare parent by unlocking if needed and adding in model for later re
	 * lock.
	 * 
	 * @param model
	 *            the model
	 * @param destNode
	 *            the dest node
	 * @param populateModel
	 *            is the model to populate
	 */
	@SuppressWarnings("unchecked")
	private void prepareParent(Map<String, Object> model, NodeRef destNode, boolean populateModel) {
		if (Boolean.FALSE.equals(model.get(SECURITY_ENABLED))) {
			return;
		}
		String lockOwner = cmfLockService.getLockedOwner(destNode);
		if (StringUtils.isNotEmpty(lockOwner)) {
			String unlockNode = cmfLockService.unlockNode(destNode, lockOwner);
			if (populateModel && (unlockNode != null)) {
				if (model.get(PARENT_REF) == null) {
					model.put(PARENT_REF, new ArrayList<NodeRef>());
				}
				((Collection<NodeRef>) model.get(PARENT_REF)).add(destNode);
				model.put(PARENT_REF_LOCK_OWNER, lockOwner);
			}
		}
	}

	/**
	 * Gets the version and fill the model with it.
	 * 
	 * @param model
	 *            the model
	 * @param majorVersion
	 *            the major version
	 * @param existingFile
	 *            the existing file
	 */
	private void putVersionInModel(Map<String, Object> model, boolean majorVersion, NodeRef existingFile) {
		Version currentVersion = serviceRegistry.getVersionService().getCurrentVersion(existingFile);
		if (currentVersion != null) {
			model.put("version", currentVersion.getVersionLabel());
		} else {
			model.put("version", majorVersion ? "1.0" : "0.1");
		}
	}

	/**
	 * Update existing node common operation.
	 * 
	 * @param updateNode
	 *            the update node
	 * @param filename
	 *            the filename
	 * @param content
	 *            the content
	 * @param majorVersion
	 *            the major version
	 * @param description
	 *            the description
	 * @param thumbGenerationMode
	 *            is the thumbnail mode to generate
	 * @return the node ref updated
	 */
	private NodeRef updateExisting(final NodeRef updateNode, String filename, Content content, boolean majorVersion,
			String description, ThumbnailGenerationMode thumbGenerationMode) {
		/**
		 * Update existing file specified in updateNodeRef
		 */
		NodeRef existingNode = updateNode;
		if (existingNode == null) {
			throw createStatus(404, "Node specified by updateNodeRef (" + existingNode + ") not found.");
		}
		if (cmfLockService.isLocked(existingNode)) {
			// We cannot update a locked document
			throw createStatus(404, "Cannot update locked document '" + existingNode
					+ "', supply a reference to its working copy instead.");
		}

		if (!nodeService.hasAspect(existingNode, ContentModel.ASPECT_WORKING_COPY)) {
			// Ensure the file is versionable (autoVersion = true,
			// autoVersionProps = false)
			ensureVersioningEnabled(existingNode, true, false);

			existingNode = checkoutForUpload(existingNode);
			// --------------------------------------------------
		}
		String mimetypeDetected = serviceRegistry.getMimetypeService().guessMimetype(filename);
		fixExtension(existingNode, filename);
		ContentDataHelper data = new ContentDataHelper(existingNode,
				new ContentData(null, mimetypeDetected, 0L, UTF_8));
		data.write(content);
		data.guessMimetype(filename);
		data.guessEncoding();
		// check it in again, with supplied version history note
		existingNode = checkin(majorVersion, description, existingNode);
		createThumbnail(data, "doclib", thumbGenerationMode);
		// ---------------------------------------------------
		// Extract the metadata
		// (The overwrite policy controls which if any parts of
		// the DOCUMENT's properties are updated from this)
		extractMetadata(existingNode);
		return existingNode;

	}

	/**
	 * Fix extension if changed with new version. This helps change mimetype.
	 * 
	 * @param existingNode
	 *            the existing node
	 * @param filename
	 *            the filename
	 */
	private void fixExtension(NodeRef existingNode, String filename) {
		Pair<String, String> newName = splitNameAndExtension(filename);
		Serializable property = getNodeService().getProperty(existingNode, ContentModel.PROP_NAME);
		Pair<String, String> current = splitNameAndExtension(property);
		boolean sameExtension = EqualsHelper.nullSafeEquals(current.getSecond(), newName.getSecond());
		if (!sameExtension && (newName.getSecond() != null)) {
			String name = current.getFirst().endsWith(".") ? current.getFirst() : current.getFirst() + ".";
			String workingCopyLabel = " " + CheckOutCheckInServiceImpl.getWorkingCopyLabel();
			if (!name.endsWith(workingCopyLabel + '.')) {
				throw new RuntimeException();
			}
			// String updateExtensionName = name + newName.getSecond();
			String afterCheckinName = name.substring(0, name.length() - (workingCopyLabel.length() + 1));
			Pair<String, String> nameForDuplicateWorkingCopyChild = getNameForDuplicateWorkingCopyChild(
					afterCheckinName + "." + newName.getSecond(),
					nodeService.getPrimaryParent(existingNode).getParentRef(), existingNode, workingCopyLabel);
			getNodeService().setProperty(existingNode, ContentModel.PROP_NAME,
					nameForDuplicateWorkingCopyChild.getSecond());
		}
	}

	/**
	 * Gets the file name and extension.
	 * 
	 * @param filenameProperty
	 *            the filename property
	 * @return the extension and the file name split
	 */
	private Pair<String, String> splitNameAndExtension(Serializable filenameProperty) {
		@SuppressWarnings("unchecked")
		Pair<String, String> result = Pair.NULL_PAIR;
		// Extract the extension
		if (filenameProperty != null) {
			String filename = filenameProperty.toString();
			result = new Pair<String, String>(filename, null);
			if (filename.length() > 0) {
				int index = filename.lastIndexOf('.');
				if ((index > -1) && (index < (filename.length() - 1))) {
					result.setFirst(filename.substring(0, index));
					result.setSecond(filename.substring(index + 1).toLowerCase());
				} else {
					result.setFirst(filename);
				}
			}
		}
		return result;
	}

	/**
	 * Ensure versioning enabled.
	 * 
	 * @param updateNode
	 *            the update node
	 * @param autoVersion
	 *            if auto version
	 * @param autoVersionProps
	 *            if auto version props
	 */
	private void ensureVersioningEnabled(NodeRef updateNode, boolean autoVersion, boolean autoVersionProps) {
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
		props.put(ContentModel.PROP_AUTO_VERSION, autoVersion);
		props.put(ContentModel.PROP_AUTO_VERSION_PROPS, autoVersionProps);
		// checkout--------------------------------------
		getServiceProxy().ensureVersioningEnabled(updateNode, props);
	}

	/**
	 * Checkin node using.
	 * 
	 * @param majorVersion
	 *            the major version
	 * @param description
	 *            the description
	 * @param updateNode
	 *            the update node
	 * @return the working copy node
	 *         {@link org.alfresco.service.cmr.coci.CheckOutCheckInService}
	 */
	private NodeRef checkin(boolean majorVersion, String description, final NodeRef updateNode) {
		// checkin--------------------------------------
		Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
		props.put(Version.PROP_DESCRIPTION, description);
		props.put(VersionBaseModel.PROP_VERSION_TYPE, majorVersion ? VersionType.MAJOR : VersionType.MINOR);
		return serviceRegistry.getCheckOutCheckInService().checkin(updateNode, props);
	}

	/**
	 * Checkout for upload.
	 * 
	 * @param updateNode
	 *            the update node
	 * @return the node ref
	 */
	private NodeRef checkoutForUpload(NodeRef updateNode) {
		// It's not a working copy, do a check out to get the actual
		// working copy
		AlfrescoTransactionSupport.bindResource("checkoutforupload", Boolean.TRUE.toString());
		serviceRegistry.getRuleService().disableRules();
		try {
			return serviceRegistry.getCheckOutCheckInService().checkout(updateNode);

		} finally {
			serviceRegistry.getRuleService().enableRules();
		}
	}

	/**
	 * Extract metadata.
	 * 
	 * @param file
	 *            the file
	 */
	public void extractMetadata(NodeRef file) {
		// Extract metadata - via repository action for now.
		// This should use the MetadataExtracter API to fetch properties,
		// allowing for possible failures.

		Action emAction = serviceRegistry.getActionService().createAction("extract-metadata");
		if (emAction != null) {
			// Call using readOnly = false, newTransaction = false
			serviceRegistry.getActionService().executeAction(emAction, file, false, false);
		}
	}

	// Prevents Flash- and IE8-sourced "null" values being set for those
	// parameters where they are invalid.
	// Note: DON'T use a "!=" comparison for "null" here.
	/**
	 * Fn field value.
	 * 
	 * @param p_field
	 *            the p_field
	 * @return the string
	 */
	public String fnFieldValue(FieldData p_field) {
		return p_field.getValue().toString();
	}

	/**
	 * Gets the policy behaviour filter.
	 * 
	 * @return the policy behaviour filter
	 */
	public BehaviourFilter getPolicyBehaviourFilter() {
		return policyBehaviourFilter;
	}

	/**
	 * Sets the policy behaviour filter.
	 * 
	 * @param policyBehaviourFilter
	 *            the new policy behaviour filter
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * Gets the site service cached.
	 *
	 * @return the site service instance
	 */
	private SiteService getSiteService() {
		if (siteService == null) {
			siteService = serviceRegistry.getSiteService();
		}
		return siteService;
	}
}
