package com.sirma.itt.cmf.integration.webscript;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * Download the list of definitions currently stored in alfresco. Result depends on query params.
 *
 * @author bbanchev
 *
 */
public class DefinitionSearchScript extends SearchScript {

	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(4);
		model.put(KEY_WORKING_MODE, "list");
		Pair<List<NodeRef>, Map<String, Object>> nodesData = null;
		List<NodeRef> nodeRefs = null;
		String servicePath = req.getServicePath();
		try {
			Map<String, Object> includedProperties = null;
			String content = req.getContent().getContent();
			JSONObject request = new JSONObject(content);
			// specific search for cases
			if (servicePath.contains("/cmf/search/definitions/case")) {
				// specific search for definitions
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_CASE_DEFINITION);
			} else if (servicePath.contains("/cmf/search/definitions/document")) {
				// specific search for definitions
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_DOCUMENT_DEFINITION);
			} else if (servicePath.contains("/cmf/search/definitions/workflow")) {
				// specific search for definitions
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_WORKFLOW_DEFINITION);
			} else if (servicePath.contains("/cmf/search/definitions/task")) {
				// specific search for definitions
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_TASK_DEFINITION);
			} else if (servicePath.contains("/cmf/search/definitions/generic")) {
				// specific search for definitions
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_GENERIC_DEFINITION);
			} else if (servicePath.contains("/cmf/search/definitions/permission")) {
				// specific search for definitions
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_PERMISSIONS_DEFINITION);
			} else if (servicePath.contains("/cmf/search/templates/notifications")) {
				// specific search for notifications
				nodeRefs = searchByAspect(request, ContentModel.ASPECT_TEMPLATABLE);
			} else if (servicePath.contains("/cmf/search/templates/doctemplate")) {
				// specific search for notifications
				nodeRefs = searchByAspect(request, CMFModel.ASPECT_CMF_TEMPLATE_DEFINITION);
				includedProperties = new HashMap<String, Object>(nodeRefs.size());
				for (NodeRef nodeRef : nodeRefs) {
					includedProperties.put(nodeRef.toString(),
							toPrefixedProperties(nodeService.getProperties(nodeRef)));
				}
			} else {
				nodeRefs = Collections.emptyList();
			}
			nodesData = new Pair<List<NodeRef>, Map<String, Object>>(nodeRefs,
					ModelUtil.buildPaging(nodeRefs.size(), -1, 0));
			model.put("propertiesMap", includedProperties);
			model.put("results", nodesData.getFirst());
			model.put("paging", nodesData.getSecond());
		} catch (Exception e) {
			throw new WebScriptException("Failed to search for definition at: " + servicePath, e);
		}
		return model;
	}
}
