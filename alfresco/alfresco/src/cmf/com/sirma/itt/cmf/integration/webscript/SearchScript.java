/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;

// TODO: Auto-generated Javadoc
/**
 * Script that will executes custom searches.
 *
 * @author borislav banchev
 */
public class SearchScript extends BaseAlfrescoScript {


	/**
	 * Execute internal. Wrapper for system user action.
	 *
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected  Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("mode", "list");
		Pair<List<NodeRef>, Map<String, Object>> nodesData = null;
		List<NodeRef> nodeRefs = null;
		try {
			String content = req.getContent().getContent();
			JSONObject request = new JSONObject(content);
			// specific search for cases
			String servicePath = req.getServicePath();
			if (servicePath.endsWith("/cmf/search")) {
				// general search
				JSONObject paging = null;
				debug("Request: ", request);
				if (request.has("paging")) {
					paging = request.getJSONObject("paging");
				}
				JSONObject additional = new JSONObject();
				if (request.has("keywords")) {
					additional.put("keywords", request.get("keywords"));
				}
				JSONArray sort = null;
				if (request.has("sort")) {
					sort = request.getJSONArray("sort");
				}
				nodesData = caseService.search(request.getString(KEY_QUERY), paging, sort,
						additional);
			} else if (servicePath.contains("/cmf/search/instances")) {
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_CASE_INSTANCE);
			} else if (servicePath.contains("/cmf/search/containers/cmf")) {
				// specific search for definitions
				List<NodeRef> nodeRefsSpaces = caseService
						.search("PATH:\"/app:company_home/st:sites//*\" AND TYPE:\""
								+ CMFModel.TYPE_CMF_INSTANCE_SPACE.toString() + "\"");
				nodeRefs =new ArrayList<NodeRef>(nodeRefsSpaces.size());
				SiteService siteService = getServiceRegistry().getSiteService();
				for (NodeRef nodeRef : nodeRefsSpaces) {
					nodeRefs.add(siteService.getSite(nodeRef).getNodeRef());
				}
			} else if (servicePath.contains("/cmf/search/case/documents")) {
				// // case document search
				// JSONObject paging = null;
				// // if (request.has("paging")) {
				// // paging = request.getJSONObject("paging");
				// // }
				// JSONArray sort = null;
				// if (request.has("sort")) {
				// sort = request.getJSONArray("sort");
				// }
				// // get first pass nodes
				// Pair<List<NodeRef>, Integer> nodesData = caseService.search(
				// request.getString(KEY_QUERY), paging, sort, null);
				// String parentQuery = nodeRefs.toString().replaceAll(", ",
				// "\" OR PARENT:\"");
				// parentQuery = parentQuery.replace("[",
				// "PARENT:\"").replace("]", "\"");
				// // if (request.has("keywords")) {
				// // model.put("mode", "map");
				// // JSONObject additional = new JSONObject();
				// // additional.put("keywords", request.get("keywords"));
				// // nodeRefs = caseService.search(parentQuery +
				// // " AND TYPE:\"cmf:\" ", paging,
				// // sort, additional);
				// // } else {
				// Pair<List<NodeRef>, Integer> nodesData = caseService.search(
				// request.getString(KEY_QUERY), paging, sort, null);
				// // }
				//
				// model.put("results", nodeRefs);
			} else {
				// general search
				JSONObject paging = null;
				debug("Request: ", request);
				if (request.has("paging")) {
					paging = request.getJSONObject("paging");
				}
				JSONObject additional = new JSONObject();
				if (request.has("keywords")) {
					additional.put("keywords", request.get("keywords"));
				}
				JSONArray sort = null;
				if (request.has("sort")) {
					sort = request.getJSONArray("sort");
				}
				nodesData = caseService.search(request.getString(KEY_QUERY), paging, sort,
						additional);
			}
			if (nodesData == null) {
				nodesData = new Pair<List<NodeRef>, Map<String, Object>>(nodeRefs,
						ModelUtil.buildPaging(nodeRefs.size(), -1, 0));
			}
			model.put("results", nodesData.getFirst());
			model.put("paging", nodesData.getSecond());
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException(e.getMessage());
		}
		return model;
	}

	/**
	 * Search by aspect.
	 *
	 * @param request
	 *            the request
	 * @param aspect
	 *            the aspect
	 * @return the list
	 * @throws JSONException
	 *             the jSON exception
	 */
	protected List<NodeRef> searchByAspect(JSONObject request, QName aspect) throws JSONException {
		List<NodeRef> nodeRefs;
		Set<String> paths = new HashSet<String>();
		if (request.has(KEY_START_PATH)) {
			paths.add(request.getString(KEY_START_PATH));
		} else if (request.has(KEY_SITES_IDS)) {
			String string = request.getString(KEY_SITES_IDS);
			String[] sites = string.split(",");
			for (String site : sites) {
				SiteInfo siteInfo = getServiceRegistry().getSiteService().getSite(site);
				if (siteInfo != null) {
					// add each site
					paths.add(nodeService.getPath(siteInfo.getNodeRef()).toPrefixString(
							serviceRegistry.getNamespaceService())
							+ "/*/*");// append one dir deeper
				}
			}
		}
		String query = "";
		if (request.has(KEY_QUERY)) {
			query = " ( " + request.getString(KEY_QUERY) + " ) AND ";
		}
		if (!paths.isEmpty()) {
			nodeRefs = getCaseService().searchNodes(query + "ASPECT:\"" + aspect.toString() + "\"",
					paths);
		} else {
			nodeRefs = getCaseService().searchNodes(query + "ASPECT:\"" + aspect.toString() + "\"",
					null);
		}
		return nodeRefs;
	}

}
