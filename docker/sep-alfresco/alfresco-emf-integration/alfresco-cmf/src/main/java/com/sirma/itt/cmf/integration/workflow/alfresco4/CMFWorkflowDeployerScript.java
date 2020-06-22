/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.webscript.BaseAlfrescoScript;

/**
 * Alfresco bootstrap Process deployment.
 *
 * @author davidc
 */
public class CMFWorkflowDeployerScript extends BaseAlfrescoScript {

	/** The cmf workflow deployer. */
	private CMFWorkflowDeployer cmfWorkflowDeployer;

	/**
	 * Gets the cmf workflow deployer.
	 *
	 * @return the cmfWorkflowDeployer
	 */
	public CMFWorkflowDeployer getCmfWorkflowDeployer() {
		return cmfWorkflowDeployer;
	}

	/**
	 * Sets the cmf workflow deployer.
	 *
	 * @param cmfWorkflowDeployer
	 *            the cmfWorkflowDeployer to set
	 */
	public void setCmfWorkflowDeployer(CMFWorkflowDeployer cmfWorkflowDeployer) {
		this.cmfWorkflowDeployer = cmfWorkflowDeployer;
	}

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
		Map<String, Object> model = new HashMap<String, Object>();
		String servicePath = req.getServicePath();
		try {
			WorkflowDefinition definition = null;
			if (servicePath.contains("/workflow/deploy")) {
				String node = req.getParameter("node");
				if ((node != null) && NodeRef.isNodeRef(node)) {
					Boolean redeploy = Boolean.valueOf(req.getParameter("redeployIfExists"));
					definition = cmfWorkflowDeployer.deploy(getCaseService().getNodeRef(node),
							redeploy);

				}
			} else {
				String workflowName = req.getParameter("name");
				if (StringUtils.isEmpty(workflowName)) {
					return model;
				}
				if (servicePath.contains("/workflow/redeploy")) {
					WorkflowDefinition definitionByName = getWorkflowService().getDefinitionByName(
							workflowName);
					// get
					if (definitionByName != null) {
						definition = cmfWorkflowDeployer.redeploy(definitionByName);

					}
				} else if (servicePath.contains("/workflow/undeploy")) {
					definition = getWorkflowService().getDefinitionByName(workflowName);
					if (definition != null) {
						cmfWorkflowDeployer.undeploy(definition);
					}
				}
			}
			if (definition != null) {
				model.put("id", definition.getId());
				model.put("version", definition.getVersion());
				model.put("name", definition.getName());
			}
			return model;
		} catch (WebScriptException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException(e.getLocalizedMessage());
		}
	}
}
