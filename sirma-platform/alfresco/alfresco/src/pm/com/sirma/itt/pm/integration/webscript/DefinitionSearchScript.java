package com.sirma.itt.pm.integration.webscript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.webscript.SearchScript;
import com.sirma.itt.pm.integration.model.PMModel;
/**
 * Download the list of definitions currently stored in alfresco. Result depends
 * on query params.
 *
 * @author bbanchev
 *
 */
public class DefinitionSearchScript extends SearchScript {

	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("mode", "list");
		Pair<List<NodeRef>, Map<String, Object>> nodesData = null;
		List<NodeRef> nodeRefs = null;
		try {
			String content = req.getContent().getContent();
			JSONObject request = new JSONObject(content);
			// specific search for cases
			String servicePath = req.getServicePath();
			if (servicePath.contains("/pm/search/definitions/project")) {
				// specific search for definitions
				nodeRefs = searchByAspect(request, PMModel.ASPECT_PM_PROJECT_DEFINITION);
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
}
