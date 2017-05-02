/**
 *
 */
package com.sirma.itt.cmf.integration.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.webscript.BaseAlfrescoScript;
import com.sirma.itt.cmf.integration.workflow.alfresco4.WorkflowModelBuilder;

/**
 * The Class WorkflowScript.
 *
 * @author bbanchev
 */
public class WorkflowScript extends BaseAlfrescoScript {

	/** The model builder. */
	private WorkflowModelBuilder modelBuilder;

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
		Map<String, Object> model = new HashMap<String, Object>(1);
		String servicePath = req.getServicePath();
		try {

			String content = req.getContent().getContent();

			if (servicePath.contains("/cmf/workflow/instances")) {

				JSONObject request = new JSONObject(content);
				NodeRef nodeRef = null;
				if (request.has(KEY_NODEID)) {
					nodeRef = caseService.getNodeRef(request.getString(KEY_NODEID));
				} else {
					throw new WebScriptException(500, "Invalid node is provided!");
				}
				// list all active workflows for nodeRef
				List<WorkflowInstance> workflows = serviceRegistry.getWorkflowService()
						.getWorkflowsForContent(nodeRef, true);
				List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(
						workflows.size());

				for (WorkflowInstance workflow : workflows) {
					results.add(modelBuilder.buildSimple(workflow));
				}

				// build the model for ftl
				model.put("workflowInstances", results);
			} else if (servicePath.contains("/cmf/workflow/definitions")) {

			}
		} catch (WebScriptException e) {
			throw e;
		} catch (Exception e) {
			throw new WebScriptException(500, "Error during request", e);
		}
		return model;
	}

	/**
	 * Gets the model builder.
	 *
	 * @return the model builder
	 */
	public WorkflowModelBuilder getModelBuilder() {
		if (modelBuilder == null) {
			modelBuilder = new WorkflowModelBuilder(serviceRegistry.getNamespaceService(),
					nodeService, serviceRegistry.getAuthenticationService(),
					serviceRegistry.getPersonService(), serviceRegistry.getWorkflowService());
		}
		return modelBuilder;
	}

}
