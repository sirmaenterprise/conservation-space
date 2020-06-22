/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * The AdministrationScript holds some admin operation requests handlers.
 *
 * @author bbanchev
 */
public class AdministrationScript extends BaseFormScript {

	/** The dictionary dao. */
	private DictionaryDAO dictionaryDAO;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.sirma.itt.cmf.integration.webscript.BaseAlfrescoScript#
	 * executeInternal
	 * (org.springframework.extensions.webscripts.WebScriptRequest)
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		ArrayList<NodeRef> results = new ArrayList<NodeRef>(1);
		Map<String, Object> model = new HashMap<String, Object>();
		String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();
		if (req.getServicePath().endsWith("case/instance/obsolete/delete")) {
			try {
				String content = req.getContent().getContent();
				JSONObject request = new JSONObject(content);
				Boolean all = request.has("all") ? Boolean.valueOf(request.getString("all")) : Boolean.FALSE;

				// if site/s are provided
				if (request.has(KEY_SITES_IDS)) {
					String string = request.getString(KEY_SITES_IDS);
					String[] sites = string.split(",");

					for (String site : sites) {
						SiteInfo siteInfo = getServiceRegistry().getSiteService().getSite(site);
						if (siteInfo != null) {
							if (request.has("containers")) {
								JSONArray jsonArray = request.getJSONArray("containers");
								for (int i = 0; i < jsonArray.length(); i++) {
									deleteContainer(currentUser, all, siteInfo, jsonArray.getString(i));
								}
							}
						}
					}
				} else // if node is provided
					if (request.has(KEY_NODEID) || request.has(KEY_PARENT_NODEID)) {
					NodeRef nodeId = cmfService.getNodeRef(request.has(KEY_NODEID) ? request.getString(KEY_NODEID)
							: request.getString(KEY_PARENT_NODEID));
					if (nodeId != null) {
						List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeId);
						for (ChildAssociationRef childAssociationRef : childAssocs) {
							final NodeRef childRef = childAssociationRef.getChildRef();
							deleteNode(all, childRef);
						}
						// if node s provided - delete it as well.
						// otherwise, all children
						if (request.has(KEY_NODEID)) {
							deleteNode(all, nodeId);
						}
					}

				}

				model.put("results", results);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (req.getServicePath().endsWith("dictionarymodel/update")) {
			FormData formData = null;
			try {
				formData = extractFormData(req);
				for (FormField field : formData.getFields()) {
					if ("filedata".equals(field.getName())) {
						if (field.getIsFile()) {
							Content content = field.getContent();
							InputStream modelStream = content.getInputStream();
							M2Model dataModel = M2Model.createModel(modelStream);
							getDictionaryDAO().putModel(dataModel);
						}
					}
				}
				model.put("results", results);
			} catch (Exception e) {
				throw new WebScriptException(e.getMessage(), e);
			} finally {
				getServiceProxy().cleanFormData(formData);
			}
		}
		return model;
	}

	/**
	 * Delete container for provided type
	 *
	 * @param currentUser
	 *            the current user
	 * @param all
	 *            the all
	 * @param siteInfo
	 *            the site info
	 * @param container
	 *            the container type qname
	 */
	private void deleteContainer(String currentUser, Boolean all, SiteInfo siteInfo, String container) {
		NodeRef cmfSpace = cmfService.getCMFSpace(QName.resolveToQName(getNamespaceService(), container),
				siteInfo.getNodeRef());
		if (cmfSpace == null) {
			return;
		}
		List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(cmfSpace);

		for (ChildAssociationRef childAssociationRef : childAssocs) {
			final NodeRef childRef = childAssociationRef.getChildRef();
			deleteNode(all, childRef);
		}

	}

	/**
	 * Delete node.
	 *
	 * @param all
	 *            if all cases should be deleted, or only in state DELETED
	 * @param childRef
	 *            the child ref
	 */
	private void deleteNode(Boolean all, final NodeRef childRef) {
		// FIXME state is configurable
		if (all || "DELETED".equals(nodeService.getProperty(childRef, CMFModel.PROP_STATUS))) {
			cmfLockService.unlockNode(childRef);

			nodeService.deleteNode(childRef);

		}
	}

	/**
	 * Gets the dictionary dao.
	 *
	 * @return the dictionaryDAO
	 */
	public DictionaryDAO getDictionaryDAO() {
		return dictionaryDAO;
	}

	/**
	 * Sets the dictionary dao.
	 *
	 * @param dictionaryDAO
	 *            the dictionaryDAO to set
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

}
