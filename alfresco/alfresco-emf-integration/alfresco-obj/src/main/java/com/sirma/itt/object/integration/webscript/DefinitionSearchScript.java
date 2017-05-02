package com.sirma.itt.object.integration.webscript;

import java.util.Collections;
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
import com.sirma.itt.object.integration.model.DOMModel;

/**
 * Download the list of definitions currently stored in alfresco. Result depends on query params.
 *
 * @author bbanchev
 *
 */
public class DefinitionSearchScript extends SearchScript {

	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put(KEY_WORKING_MODE, "list");
		Pair<List<NodeRef>, Map<String, Object>> nodesData = null;
		List<NodeRef> nodeRefs = null;
		String servicePath = null;
		try {
			String content = req.getContent().getContent();
			servicePath = req.getServicePath();
			JSONObject request = new JSONObject(content);
			if (servicePath.contains("/dom/search/definitions/objects")) {
				// specific search for object definitions
				nodeRefs = searchByAspect(request, DOMModel.ASPECT_DOM_OBJECT_DEFINITION);
			} else {
				nodeRefs = Collections.emptyList();
			}
			nodesData = new Pair<List<NodeRef>, Map<String, Object>>(nodeRefs,
					ModelUtil.buildPaging(nodeRefs.size(), -1, 0));
			model.put("results", nodesData.getFirst());
			model.put("paging", nodesData.getSecond());
		} catch (Exception e) {
			throw new WebScriptException("Failed to search for definition at: " + servicePath, e);
		}
		return model;
	}
}
