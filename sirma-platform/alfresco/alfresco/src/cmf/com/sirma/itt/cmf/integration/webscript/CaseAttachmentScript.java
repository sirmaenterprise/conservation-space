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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
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

/**
 * Script for woriking with cases' attachments.
 *
 * @author bbanchev
 *
 */
public class CaseAttachmentScript extends BaseFormScript {

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
		ArrayList<NodeRef> value = new ArrayList<NodeRef>(1);
		model.put("mode", "unknown");
		Boolean securityEnabled = Boolean.valueOf(req.getHeader("security.enabled"));
		model.put(SECURITY_ENABLED, securityEnabled);
		try {
			String serverPath = req.getServicePath();
			// TODO enum
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
			// lock if it it case instnace
		} catch (ContentQuotaException e) {
			throw createStatus(413, "org.alfresco.service.cmr.usage.ContentQuotaException");
		} catch (WebScriptException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			// capture exception, annotate it accordingly and re-throw
			if (e.getMessage() != null) {
				throw new WebScriptException(500, e.getMessage(), e);
			} else {
				throw createStatus(500, "Unexpected error occurred during upload of new content.");

			}
		} finally {
			if (model.containsKey(PARENT_REF_LOCK_OWNER)) {
				cmfLockService.lockNode((NodeRef) model.get(PARENT_REF),
						model.get(PARENT_REF_LOCK_OWNER).toString());
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
	private void checkinRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		// delete a case attachment
		model.put("mode", "unlock");

		String lockUser = null;
		boolean authChanged = false;
		try {
			JSONObject request = new JSONObject(req.getContent().getContent());
			// if (request.has(KEY_LOCK_OWNER)) {
			// lockUser = request.getString(KEY_LOCK_OWNER);
			// }
			if (lockUser != null) {
				AuthenticationUtil.pushAuthentication();
				authChanged = true;
				AuthenticationUtil.setFullyAuthenticatedUser(lockUser);
			}
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
	private void unlockRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		// delete a case attachment
		model.put("mode", "unlock");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			String lockUser = AuthenticationUtil.getSystemUserName();
			// if (request.has(KEY_LOCK_OWNER)) {
			// lockUser = request.getString(KEY_LOCK_OWNER);
			// }
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
	private Map<String, Object> checkoutRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		// delete a case attachment
		model.put("mode", "lock");
		String lockUser = null;
		boolean authChanged = false;
		try {
			JSONObject request = new JSONObject(req.getContent().getContent());
			// TODO should be changed when perm are ready
			// if (request.has(KEY_LOCK_OWNER)) {
			// lockUser = request.getString(KEY_LOCK_OWNER);
			// }
			if (lockUser != null) {
				AuthenticationUtil.pushAuthentication();
				authChanged = true;
				AuthenticationUtil.setFullyAuthenticatedUser(lockUser);
			}
			if (request.has(KEY_ATTACHMENT_ID)) {
				NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
				if (updateable != null) {
					prepareParent(model, nodeService.getPrimaryParent(updateable).getParentRef());
					updateable = serviceRegistry.getCheckOutCheckInService().checkout(updateable);
					value.add(updateable);
				}
			}

		} finally {
			if (authChanged) {
				AuthenticationUtil.popAuthentication();
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
	private void cancelCheckoutRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		model.put("mode", "unlock");

		String lockUser = null;
		boolean authChanged = false;
		try {
			JSONObject request = new JSONObject(req.getContent().getContent());
			// if (request.has(KEY_LOCK_OWNER)) {
			// lockUser = request.getString(KEY_LOCK_OWNER);
			// }
			if (lockUser != null) {
				AuthenticationUtil.pushAuthentication();
				authChanged = true;
				AuthenticationUtil.setFullyAuthenticatedUser(lockUser);
			}
			if (request.has(KEY_ATTACHMENT_ID)) {
				NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
				if (updateable != null) {
					NodeRef cancelCheckout = getServiceRegistry().getCheckOutCheckInService()
							.cancelCheckout(updateable);
					value.add(cancelCheckout);
				}
			}

		} finally {
			if (authChanged) {
				AuthenticationUtil.popAuthentication();
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
	private Map<String, Object> lockRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		// delete a case attachment
		model.put("mode", "lock");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			String lockUser = AuthenticationUtil.getSystemUserName();
			// if (request.has(KEY_LOCK_OWNER)) {
			// lockUser = request.getString(KEY_LOCK_OWNER);
			// }
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
	private Map<String, Object> detachRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		model.put("mode", "dettach");
		JSONObject request = new JSONObject(req.getContent().getContent());
		NodeRef caseNode = null;
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				caseNode = caseService.getNodeRef(request.getString(KEY_CASE_ID));
				if (caseNode == null) {
					throw createStatus(404, "Document is not attached to specific case!");
				}
				prepareParent(model, caseNode);
				nodeService.deleteNode(updateable);
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
	private Map<String, Object> attachRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws Exception {
		model.put("mode", "attach");
		FormData data = extractFormData(req);
		if (data != null) {
			// process the attach
			attachFile(data, model);

			NodeRef attachFile = (NodeRef) model.get(DOCUMENT);
			if (attachFile != null) {
				// cmfLockService.lockNode(attachFile);
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
	private Map<String, Object> copyRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws Exception {
		model.put("mode", "copy");
		JSONObject request = new JSONObject(req.getContent().getContent());
		NodeRef moveable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
		if (moveable != null) {
			NodeRef newParent = caseService.getNodeRef(request.getString(KEY_DESTINATION));
			String filename = request.getString("name");
			NodeRef existingFile = caseService.childByNamePath(newParent, filename);
			if (existingFile != null) {
				// update the name if duplicate found
				filename = getNameForDuplicateChild(filename, newParent, existingFile);
			}
			// unlock the destination first
			prepareParent(model, newParent);

			// for read permission unlock is needed only in dest node.

			// ChildAssociationRef parentAssocs =
			// getNodeService().getPrimaryParent(moveable);
			// NodeRef parentRefOld = parentAssocs.getParentRef();
			// String lockOwner = cmfLockService.getLockedOwner(parentRefOld);
			// // unlock the original parent before move
			// prepareParent(model, parentRefOld, false);
			FileInfo attachFile = getServiceRegistry().getFileFolderService().copy(moveable,
					newParent, filename);
			if (attachFile != null) {
				model.put(DOCUMENT, attachFile);
				// set the versioning aspect
				ensureVersioningEnabled(attachFile.getNodeRef(), true, false);
				model.put("versionProperties", populateProperties(attachFile.getNodeRef(), true));
				// cmfLockService.lockNode(attachFile);
				value.add(attachFile.getNodeRef());
			}
			// // lock the original parent again
			// cmfLockService.lockNode(parentRefOld, lockOwner);
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
	 * @throws JSONException
	 *             on parse error
	 * @throws IOException
	 *             on io error
	 *             {@link #getNameForDuplicateChild(String, NodeRef, NodeRef)}
	 *             so no duplicate child exception occurs.
	 */
	private Map<String, Object> moveRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		model.put("mode", "move");
		JSONObject request = new JSONObject(req.getContent().getContent());
		NodeRef moveable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
		if (moveable != null) {
			NodeRef newParent = caseService.getNodeRef(request.getString(KEY_DESTINATION));
			String filename = getNodeService().getProperty(moveable, ContentModel.PROP_NAME)
					.toString();
			NodeRef existingFile = caseService.childByNamePath(newParent, filename);
			if (existingFile != null) {
				filename = getNameForDuplicateChild(filename, newParent, existingFile);
				// update the name
				getNodeService().setProperty(moveable, ContentModel.PROP_NAME, filename);
			}
			// unlock the destination first
			prepareParent(model, newParent);
			ChildAssociationRef parentAssocs = getNodeService().getPrimaryParent(moveable);
			String localName = parentAssocs.getQName().getLocalName();
			NodeRef parentRefOld = parentAssocs.getParentRef();
			String lockOwner = cmfLockService.getLockedOwner(parentRefOld);
			// unlock the original parent before move
			prepareParent(model, parentRefOld, false);
			ChildAssociationRef moveNode = nodeService.moveNode(moveable, newParent,
					parentAssocs.getTypeQName(),
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localName));
			NodeRef attachFile = moveNode.getChildRef();
			if (attachFile != null) {
				model.put(DOCUMENT, attachFile);

				// cmfLockService.lockNode(attachFile);
				model.put("versionProperties", populateProperties(attachFile, false));
				value.add(attachFile);
			}
			// lock the original parent again
			cmfLockService.lockNode(parentRefOld, lockOwner);
		}
		return model;

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
		Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(
				properties.size());
		for (Entry<QName, Serializable> prop : properties.entrySet()) {
			versionProperties.put(prop.getKey().toPrefixString(getNamespaceService()),
					prop.getValue());
		}
		if (versionLabel != null) {
			versionProperties.put("version", versionLabel);
		} else if (setDefaultVersion) {
			versionProperties.put("version", "1.0");
		}
		ContentData content = (ContentData) properties.get(ContentModel.PROP_CONTENT);
		versionProperties.put("cm:content.mimetype", content.getMimetype());
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
	private Map<String, Object> historicVersionRequest(WebScriptRequest req,
			Map<String, Object> model, ArrayList<NodeRef> value) throws JSONException, IOException {
		model.put("mode", "version");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				if (request.has("version")) {
					try {

						Version version = serviceRegistry.getVersionService()
								.getVersionHistory(updateable)
								.getVersion(request.getString("version"));
						NodeRef frozenStateNodeRef = version.getFrozenStateNodeRef();
						value.add(frozenStateNodeRef);
						model.put("versionProperties",
								populateProperties(frozenStateNodeRef, false));
					} catch (VersionDoesNotExistException e) {
						throw createStatus(500, "Invalid request. Version does not exists!");
					}
				}
			}
		} else {
			throw createStatus(500, "Invalid request. Document is not provided!");
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
	private Map<String, Object> revertRequest(WebScriptRequest req, Map<String, Object> model,
			ArrayList<NodeRef> value) throws JSONException, IOException {
		model.put("mode", "revert");
		JSONObject request = new JSONObject(req.getContent().getContent());
		if (request.has(KEY_ATTACHMENT_ID)) {
			NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
			if (updateable != null) {
				if (request.has("version")) {
					try {
						System.out.println(nodeService.getProperties(updateable));
						Version version = serviceRegistry.getVersionService()
								.getVersionHistory(updateable)
								.getVersion(request.getString("version"));
						serviceRegistry.getVersionService().revert(updateable, version);
						// checkout and checkin to create a new version
						updateable = serviceRegistry.getCheckOutCheckInService().checkout(
								updateable);
						// model for update should exists - no has() check
						updateable = checkin(request.getBoolean(MAJORVERSION),
								request.getString(DESCRIPTION), updateable);
						value.add(updateable);
						model.put("versionProperties", populateProperties(updateable, false));
					} catch (VersionDoesNotExistException e) {
						throw createStatus(500, "Invalid request. Version does not exists!");
					}
				}
			}
		} else {
			throw createStatus(500, "Invalid request. Document is not provided!");
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
		NodeRef updateable = caseService.getNodeRef(request.getString(KEY_ATTACHMENT_ID));
		if (updateable != null) {
			String description = request.has(DESCRIPTION) ? request.getString(DESCRIPTION)
					: "Automatic checkin comment";
			Boolean majorVersion = request.has(MAJORVERSION) ? Boolean.valueOf(request
					.getString(MAJORVERSION)) : Boolean.TRUE;
			if (nodeService.hasAspect(updateable, ContentModel.ASPECT_WORKING_COPY)) {
				NodeRef checkin = checkin(majorVersion, description, updateable);
				// update with some props
				if (request.has(KEY_PROPERTIES)) {
					Map<QName, Serializable> properties = toMap(request
							.getJSONObject(KEY_PROPERTIES));
					nodeService.addProperties(checkin, properties);
				}
				return checkin;
			} else {
				// get the working copy
				NodeRef workingCopyNode = getServiceProxy().getWorkingCopy(updateable);
				updateable = checkin(majorVersion, description, workingCopyNode);
				// update with some props
				if (request.has(KEY_PROPERTIES)) {
					Map<QName, Serializable> properties = toMap(request
							.getJSONObject(KEY_PROPERTIES));
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

		/*
		 * (non-Javadoc)
		 *
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

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.springframework.extensions.surf.util.Content#getInputStream()
		 */
		@Override
		public InputStream getInputStream() {
			ContentReader reader = getContentService().getReader(nodeRef, property);
			return ((reader != null) && reader.exists()) ? reader.getContentInputStream() : null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.extensions.surf.util.Content#getReader()
		 */
		@Override
		public Reader getReader() {
			ContentReader reader = getContentService().getReader(nodeRef, property);

			if ((reader != null) && reader.exists()) {
				try {
					return (contentData.getEncoding() == null) ? new InputStreamReader(
							reader.getContentInputStream()) : new InputStreamReader(
									reader.getContentInputStream(), contentData.getEncoding());
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
			writer.setMimetype(getMimetype()); // use existing mimetype value
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
			// writer.setMimetype(content.getMimetype());
			// writer.setEncoding(content.getEncoding());
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

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.extensions.surf.util.Content#getSize()
		 */
		@Override
		public long getSize() {
			return contentData.getSize();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.extensions.surf.util.Content#getMimetype()
		 */
		@Override
		public String getMimetype() {
			return contentData.getMimetype();
		}

		/*
		 * (non-Javadoc)
		 *
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
			// return getProperties(nodeRef).get(property);
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
					Charset charset = serviceRegistry.getMimetypeService()
							.getContentCharsetFinder().getCharset(in, getMimetype());
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
	private void attachFile(FormData formdata, Map<String, Object> model) throws Exception {
		SiteService siteService = serviceRegistry.getSiteService();
		NodeService nodeService = serviceRegistry.getNodeService();
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
			// String thumbnailNames = null;

			// Upload specific
			String uploadDirectory = null;
			String contentType = null;
			Set<String> aspects = new TreeSet<String>();
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			boolean overwrite = true; // If a filename clashes for a versionable
			// file

			// Update specific
			String updateNodeRef = null;
			boolean majorVersion = false;
			String description = "";

			// // allow the locale to be set via an argument
			// if (args["lang"] != null)
			// {
			// utils.setLocale(args["lang"]);
			// }

			// Parse file attributes
			FormField[] fields = formdata.getFields();
			for (FormField field : fields) {
				String fieldValue = field.getValue();
				if ("filedata".equals(field.getName())) {
					if (field.getIsFile()) {
						filename = field.getFilename();
						content = field.getContent();
						mimetype = field.getMimetype();
					} else {
						filename = field.getFilename() == null ? GUID.generate() : field
								.getFilename();
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
				}
				// else if ("thumbnails".equals(field.getName())) {
				// thumbnailNames = field.getValue();
				// }
			}
			debug("[filename=", filename, ", content=", content, ", mimetype=", mimetype,
					", siteId=", siteId, ", containerId=", containerId, ", destination=",
					destination, ", updateNodeRef=", updateNodeRef, ", uploadDirectory=",
					uploadDirectory, ", description=", description, ", contentType=", contentType,
					", aspects=", aspects, ", properties=", properties, ", overwrite=", overwrite,
					", majorVersion=", majorVersion, ", description=", description, "]");
			// Ensure mandatory file attributes have been located. Need either
			// destination, or site + container or updateNodeRef
			if (((filename == null) || (content == null))
					|| ((destination == null) && ((siteId == null) || (containerId == null))
							&& (updateNodeRef == null) && (uploadDirectory == null))) {
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
				destNode = caseService.getNodeRef(destination);
				if (destNode == null) {
					throw createStatus(404, "Destination (" + destination + ") not found.");
				}
			}

			/**
			 * Update existing or Upload new?
			 */
			if (updateNodeRef != null) {

				NodeRef updatable = caseService.getNodeRef(updateNodeRef);

				// String lockOwner = cmfLockService.getLockedOwner(updatable);
				NodeRef parentRef = nodeService.getPrimaryParent(updatable).getParentRef();
				// unlock and fill model
				prepareParent(model, parentRef);

				// if (StringUtils.isNotEmpty(lockOwner)) {
				// cmfLockService.unlockNode(updatable);
				// }

				// set the version props so to be kept as version info
				if (properties != null) {
					nodeService.addProperties(updatable, properties);
				}
				updatable = updateExisting(updatable, filename, content, majorVersion, description);
				// Record the file details ready for generating the response
				model.put(DOCUMENT, updatable);

			} else {
				/**
				 * Upload new file to destNode (calculated earlier) + optional
				 * subdirectory
				 */
				if (uploadDirectory != null) {
					List<NodeRef> destNodeLocal = caseService.getNodesByXPath(uploadDirectory);
					if ((destNodeLocal == null) || (destNodeLocal.size() != 1)) {
						throw createStatus(404, "Cannot upload file since upload directory '"
								+ uploadDirectory + "' does not exist.");
					}
					destNode = destNodeLocal.get(0);
				}
				/**
				 * Existing file handling.
				 */
				prepareParent(model, destNode);

				NodeRef existingFile = caseService.childByNamePath(destNode, filename);
				if (existingFile != null) {
					// File already exists, decide what to do
					if (nodeService.hasAspect(existingFile, ContentModel.ASPECT_VERSIONABLE)
							&& overwrite) {

						existingFile = updateExisting(existingFile, filename, content,
								majorVersion, description);
						// Record the file details ready for generating the
						// response
						model.put(DOCUMENT, existingFile);

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
				NamespaceService namespaceService = serviceRegistry.getNamespaceService();
				if (contentType != null) {
					contenTypeQname = QName
							.resolveToQName(namespaceService, contentType.toString());

					// Ensure that we are performing a specialise
					DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
					if ((nodeType.equals(contenTypeQname) == false)
							&& (dictionaryService.isSubClass(contenTypeQname, nodeType) == true)) {
						contenTypeQname = nodeType;
					}
				}
				NodeRef newFile = serviceRegistry
						.getFileFolderService()
						.create(destNode,
								filename,
								contenTypeQname == null ? ContentModel.TYPE_CONTENT
										: contenTypeQname).getNodeRef();
				// newFile.properties = properties;
				// Use a the appropriate write() method so that the mimetype
				// already guessed from the original filename is
				// maintained - as upload may have been via Flash - which always
				// sends binary mimetype and would overwrite it.
				// Also perform the encoding guess step in the write() method to
				// save an additional Writer operation.
				// writeContent(node, content, null,null);

				MimetypeService mimetypeService = serviceRegistry.getMimetypeService();
				String mimetypeDetected = mimetypeService.guessMimetype(filename);
				ContentDataHelper data = new ContentDataHelper(newFile, new ContentData(null,
						mimetypeDetected, 0L, UTF_8));
				data.write(content, false, true);
				// // Create thumbnail?
				// if (thumbnailNames != null) {
				// String[] thumbnails = thumbnailNames.split(",");
				// String thumbnailName = "";
				//
				// for (i = 0; i < thumbnails.length; i++) {
				// thumbnailName = thumbnails[i];
				// if (thumbnailName != ""
				// && serviceRegistry.getThumbnailService()
				// .getThumbnailRegistry()
				// .getThumbnailDefinition(thumbnailName) != null) {
				// // newFile.createThumbnail(thumbnailName, true);
				// createThumbnail(thumbnailName, true, newFile,
				// mimetype, content.getSize());
				// }
				// }
				// }

				// Additional aspects?
				// custom - add properties
				if (properties != null) {
					nodeService.addProperties(newFile, properties);
				}
				if (aspects.size() > 0) {
					for (String aspect : aspects) {
						QName aspectQName = QName.resolveToQName(namespaceService, aspect);
						if (ContentModel.ASPECT_VERSIONABLE.equals(aspectQName)) {
							ensureVersioningEnabled(newFile, true, false);
						} else {
							nodeService.addAspect(newFile, aspectQName, null);
						}
					}
				}

				// Extract the metadata
				extractMetadata(newFile);
				// Record the file details ready for generating the response
				model.put(DOCUMENT, newFile);
			}
			if (model.containsKey(DOCUMENT)) {
				putVersionInModel(model, majorVersion, (NodeRef) model.get(DOCUMENT));
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
				tmpFilename = filename.substring(0, dotIndex) + indexPrefix + counter
						+ filename.substring(dotIndex);
			} else {
				// Filename didn't contain a dot at all, create
				// "filename-1"
				tmpFilename = filename + indexPrefix + counter;
			}
			existingFile = caseService.childByNamePath(destNode, tmpFilename);
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
	private Pair<String, String> getNameForDuplicateWorkingCopyChild(String filename,
			NodeRef destNode, NodeRef existingFile, String workingCopyLabel) {
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
			existingFile = caseService.childByNamePath(destNode, tmpFilename);
			existingWorkingFile = caseService.childByNamePath(destNode, tmpWorkFilename);
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
	private void prepareParent(Map<String, Object> model, NodeRef destNode, boolean populateModel) {
		if (Boolean.FALSE.equals(model.get(SECURITY_ENABLED))) {
			return;
		}
		String lockOwner = cmfLockService.getLockedOwner(destNode);
		if (StringUtils.isNotEmpty(lockOwner)) {
			String unlockNode = cmfLockService.unlockNode(destNode, lockOwner);
			if (populateModel && (unlockNode != null)) {
				model.put(PARENT_REF, destNode);
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
	private void putVersionInModel(Map<String, Object> model, boolean majorVersion,
			NodeRef existingFile) {
		Version currentVersion = serviceRegistry.getVersionService()
				.getCurrentVersion(existingFile);
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
	 * @return the node ref updated
	 */
	private NodeRef updateExisting(final NodeRef updateNode, String filename, Content content,
			boolean majorVersion, String description) {
		/**
		 * Update existing file specified in updateNodeRef
		 */
		NodeRef existingNode = updateNode;
		if (existingNode == null) {
			throw createStatus(404, "Node specified by updateNodeRef (" + existingNode
					+ ") not found.");
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
		ContentDataHelper data = new ContentDataHelper(existingNode, new ContentData(null,
				mimetypeDetected, 0L, UTF_8));
		data.write(content);
		data.guessMimetype(filename);
		data.guessEncoding();
		// check it in again, with supplied version history note
		existingNode = checkin(majorVersion, description, existingNode);

		// ---------------------------------------------------
		// Extract the metadata
		// (The overwrite policy controls which if any parts of
		// the document's properties are updated from this)
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
		boolean sameExtension = EqualsHelper.nullSafeEquals(current.getSecond(),
				newName.getSecond());
		if (!sameExtension && (newName.getSecond() != null)) {
			String name = current.getFirst().endsWith(".") ? current.getFirst() : current
					.getFirst() + ".";
			String workingCopyLabel = " " + CheckOutCheckInServiceImpl.getWorkingCopyLabel();
			if (!name.endsWith(workingCopyLabel + '.')) {
				throw new RuntimeException();
			}
			// String updateExtensionName = name + newName.getSecond();
			String afterCheckinName = name.substring(0, name.length()
					- (workingCopyLabel.length() + 1));
			Pair<String, String> nameForDuplicateWorkingCopyChild = getNameForDuplicateWorkingCopyChild(
					afterCheckinName + "." + newName.getSecond(),
					nodeService.getPrimaryParent(existingNode).getParentRef(), existingNode,
					workingCopyLabel);
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
	private void ensureVersioningEnabled(NodeRef updateNode, boolean autoVersion,
			boolean autoVersionProps) {
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
		props.put(VersionBaseModel.PROP_VERSION_TYPE, majorVersion ? VersionType.MAJOR
				: VersionType.MINOR);
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

	// /**
	// * Creates a thumbnail for the content property of the node.
	// *
	// * The thumbnail name correspionds to pre-set thumbnail details stored in
	// * the repository.
	// *
	// * If the thumbnail is created asynchronously then the result will be null
	// * and creation of the thumbnail will occure at some point in the
	// * background.
	// *
	// * @param thumbnailName
	// * the name of the thumbnail
	// * @param async
	// * indicates whether the thumbnail is create asynchronously or
	// * not
	// * @param nodeRef
	// * the node ref
	// * @param nodeMimeType
	// * the node mime type
	// * @param size
	// * the size
	// * @return ScriptThumbnail the newly create thumbnail node or null if
	// async
	// * creation occures
	// */
	// public NodeRef createThumbnail(String thumbnailName, boolean async,
	// NodeRef nodeRef,
	// String nodeMimeType, long size) {
	// NodeRef result = null;
	//
	// // Use the thumbnail registy to get the details of the thumbail
	// ThumbnailRegistry registry =
	// serviceRegistry.getThumbnailService().getThumbnailRegistry();
	// ThumbnailDefinition details =
	// registry.getThumbnailDefinition(thumbnailName);
	// if (details == null) {
	// // Throw exception
	// throw new ScriptException("The thumbnail name '" + thumbnailName
	// + "' is not registered");
	// }
	//
	// // If there's nothing currently registered to generate thumbnails for
	// // the
	// // specified mimetype, then log a message and bail out
	// Serializable value = this.nodeService.getProperty(nodeRef,
	// ContentModel.PROP_CONTENT);
	// ContentData contentData =
	// DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
	// if (!registry.isThumbnailDefinitionAvailable(contentData.getContentUrl(),
	// nodeMimeType,
	// size, details)) {
	//
	// return null;
	// }
	//
	// // Have the thumbnail created
	// if (async == false) {
	// // Create the thumbnail
	// return serviceRegistry.getThumbnailService().createThumbnail(nodeRef,
	// ContentModel.PROP_CONTENT, details.getMimetype(),
	// details.getTransformationOptions(), details.getName());
	//
	// } else {
	// Action action = ThumbnailHelper.createCreateThumbnailAction(details,
	// serviceRegistry);
	//
	// // Queue async creation of thumbnail
	// serviceRegistry.getActionService().executeAction(action, nodeRef, true,
	// true);
	// }
	//
	// return result;
	// }

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
	};

}
