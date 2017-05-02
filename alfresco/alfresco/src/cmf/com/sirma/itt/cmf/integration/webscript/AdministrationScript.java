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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * The Class AdministrationScript.
 *
 * @author bbanchev
 */
public class AdministrationScript extends BaseFormScript {
	private DictionaryDAO dictionaryDAO;

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
		return executeInternal(req);

	}

	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		ArrayList<NodeRef> results = new ArrayList<NodeRef>(1);
		Map<String, Object> model = new HashMap<String, Object>();
		String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();
		if (req.getServicePath().endsWith("case/instance/obsolete/delete")) {
			try {
				String content = req.getContent().getContent();
				JSONObject request = new JSONObject(content);
				Boolean all = request.has("all") ? Boolean.valueOf(request.getString("all"))
						: Boolean.FALSE;

				// if site/s are provided
				if (request.has(KEY_SITES_IDS)) {
					String string = request.getString(KEY_SITES_IDS);
					String[] sites = string.split(",");

					for (String site : sites) {
						SiteInfo siteInfo = getServiceRegistry().getSiteService().getSite(site);
						if (siteInfo != null) {
							NodeRef cmfSpace = caseService.getCMFSpace(
									CMFModel.TYPE_CMF_INSTANCE_SPACE, siteInfo.getNodeRef());
							List<ChildAssociationRef> childAssocs = nodeService
									.getChildAssocs(cmfSpace);

							try {
								AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil
										.getSystemUserName());
								for (ChildAssociationRef childAssociationRef : childAssocs) {
									final NodeRef childRef = childAssociationRef.getChildRef();
									deleteNode(all, childRef);
								}
							} finally {
								AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
							}
							// LOG warn
						}
					}
				} else // if node is provided
					if (request.has(KEY_NODEID) || request.has(KEY_PARENT_NODEID)) {
						try {
							AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil
									.getSystemUserName());
							NodeRef nodeId = caseService.getNodeRef(request.getString(KEY_NODEID));
							if (nodeId != null) {
								List<ChildAssociationRef> childAssocs = nodeService
										.getChildAssocs(nodeId);
								for (ChildAssociationRef childAssociationRef : childAssocs) {
									final NodeRef childRef = childAssociationRef.getChildRef();
									deleteNode(true, childRef);
								}
								// if node s provided - delete it as well.
								// otherwise, all children
								if (request.has(KEY_NODEID)) {
									deleteNode(true, nodeId);
								}
							}
						} finally {
							AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
						}

					}

				model.put("results", results);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (req.getServicePath().endsWith("dictionarymodel/update")) {

			InputStream modelStream = null;
			M2Model dataModel = M2Model.createModel(modelStream);
			getDictionaryDAO().putModel(dataModel);
		}
		return model;
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
		if (all || "DELETED".equals(nodeService.getProperty(childRef, CMFModel.PROP_PRIMARY_STATE))) {
			cmfLockService.unlockNode(childRef);

			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {
					nodeService.deleteNode(childRef);
					return null;
				}
			}, AuthenticationUtil.getSystemUserName());

		}
	}

	/**
	 * @return the dictionaryDAO
	 */
	public DictionaryDAO getDictionaryDAO() {
		return dictionaryDAO;
	}

	/**
	 * @param dictionaryDAO
	 *            the dictionaryDAO to set
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

}
