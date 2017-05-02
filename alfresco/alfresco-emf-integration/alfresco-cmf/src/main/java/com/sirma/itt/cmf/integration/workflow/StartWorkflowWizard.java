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
package com.sirma.itt.cmf.integration.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.bean.repository.Node;
import org.springframework.extensions.webscripts.WebScriptException;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * Bean implementation for the Start Workflow Wizard.
 *
 * @author gavinc
 */
public class StartWorkflowWizard extends ActivitiWizards {

	/** The selected workflow. */
	protected String selectedWorkflow;

	/** The start task node. */
	protected Node startTaskNode;

	/** The item to workflow. */
	private NodeRef itemToWorkflow;

	/** The workflow definition. */
	private WorkflowDefinition workflowDefinition;

	/**
	 * Instantiates a new start workflow wizard.
	 *
	 * @param registry
	 *            the registry
	 * @param caseService
	 *            the case service
	 */
	public StartWorkflowWizard(ServiceRegistry registry, CMFService caseService) {
		super(registry, caseService);
	}

	// ------------------------------------------------------------------------------
	// Wizard implementation

	/**
	 * Inits the.
	 *
	 * @param selectedWorkflow
	 *            the selected workflow
	 * @param itemToWorkflowId
	 *            the item to workflow id
	 */
	public void init(String selectedWorkflow, String itemToWorkflowId) {

		startTaskNode = null;
		this.selectedWorkflow = selectedWorkflow;
		workflowDefinition = getWorkflowService().getDefinitionByName(selectedWorkflow);
		if (workflowDefinition == null) {
			throw new WebScriptException(404, "No workflow definition found for: "
					+ selectedWorkflow);
		}
		if (itemToWorkflowId == null) {
			throw new RuntimeException("Workflow could not be started empty!");
		}
		itemToWorkflow = caseService.getNodeRef(itemToWorkflowId);
		if (itemToWorkflow == null) {
			throw new WebScriptException(404, "Case to attach to workflow is not found!");
		}
		QName type = getNodeService().getType(itemToWorkflow);
		if (!getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER)) {
			throw new WebScriptException(404,
					"Object to attach to workflow is not the required type!");
		}
		createModel();

	}

	/**
	 * Process the start of workflow.
	 *
	 * @param properties
	 *            the properties
	 * @return the string
	 * @throws Exception
	 *             on any error
	 */
	public Pair<WorkflowTask, WorkflowInstance> start(Map<QName, Serializable> properties)
			throws Exception {

		debug("Starting workflow: ", selectedWorkflow);

		// prepare the parameters from the current state of the property sheet
		Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(startTaskNode);
		// first set initial properties and then continue set them
		params.putAll(properties);
		debug("Starting workflow with parameters: ", params);

		// create a workflow package for the attached items and add them
		NodeRef workflowPackage = getWorkflowService().createPackage(null);
		params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);

		getUnprotectedNodeService().addChild(
				workflowPackage,
				itemToWorkflow,
				WorkflowModel.ASSOC_PACKAGE_CONTAINS,
				QName.createQName(
						NamespaceService.CONTENT_MODEL_1_0_URI,
						QName.createValidLocalName((String) getNodeService().getProperty(
								itemToWorkflow, ContentModel.PROP_NAME))));

		// setup the context for the workflow (this is the space the workflow
		// was launched from)
		// TODO
		SiteInfo workflowContext = registry.getSiteService().getSite(itemToWorkflow);
		if (workflowContext != null) {
			params.put(WorkflowModel.PROP_CONTEXT, workflowContext.getNodeRef());
		}

		params.putAll(properties);
		debug("Workflow Params ", params);
		WorkflowTask endedTask = null;
		// start the workflow to get access to the start task
		WorkflowPath path = getWorkflowService().startWorkflow(workflowDefinition.getId(), params);
		Set<QName> keySet = params.keySet();
		Map<QName, Serializable> paramsCopy = new HashMap<QName, Serializable>();
		for (QName qName : keySet) {
			if (CMFModel.CMF_WORKFLOW_MODEL_1_0_URI.equals(qName.getNamespaceURI())) {
				paramsCopy.put(qName, params.get(qName));
			}
		}
		// add the custom props here
		getNodeService().addProperties(workflowPackage, paramsCopy);

		if (path != null) {
			// extract the start task
			List<WorkflowTask> tasks = getWorkflowService().getTasksForWorkflowPath(path.getId());
			if (tasks.size() == 1) {
				WorkflowTask startTask = tasks.get(0);
				debug("Found start task:", startTask);

				endedTask = getWorkflowService().endTask(startTask.getId(), null);

				// remove start task properties from workflow
				getNodeService().removeProperty(workflowPackage,
						CMFModel.PROP_SRART_TRANSITION_OUTCOME);
				getNodeService().removeProperty(workflowPackage, CMFModel.PROP_STATUS);

				getNodeService().removeProperty(workflowPackage, CMFModel.PROP_TASK_MODIFIED);
				getNodeService().removeAspect(workflowPackage, CMFModel.ASPECT_TASK_NODE);
				getNodeService().removeAspect(workflowPackage, CMFModel.ASPECT_AUDITABLE);
			}

			debug("Started workflow: ", selectedWorkflow);
			return new Pair<WorkflowTask, WorkflowInstance>(endedTask, path.getInstance());
		}
		return null;
	}

	/**
	 * Creates the model.
	 */
	public void createModel() {

		debug("Selected workflow: ", selectedWorkflow);

		WorkflowTaskDefinition taskDef = workflowDefinition.getStartTaskDefinition();
		if (taskDef != null) {
			debug("Start task definition: ", taskDef);

			// create an instance of a task from the data dictionary
			startTaskNode = TransientNode.createNew(registry, taskDef.getMetadata(), "task_"
					+ System.currentTimeMillis(), null);
		} else {
			throw new WebScriptException(404, "No start task found for: "
					+ workflowDefinition.getName());
		}

	}

	/**
	 * Returns the Node representing the start task metadata required.
	 *
	 * @return The Node for the start task
	 */
	public Node getTaskMetadataNode() {
		return startTaskNode;
	}

	//
	// /**
	// * Returns a list of resources associated with this task i.e. the children
	// * of the workflow package
	// *
	// * @return The list of nodes
	// */
	// public List<Node> getResources() {
	// this.resources = new ArrayList<Node>(4);
	//
	// UserTransaction tx = null;
	// try {
	// FacesContext context = FacesContext.getCurrentInstance();
	// tx = Repository.getUserTransaction(context, true);
	// tx.begin();
	//
	// if (this.getNodeService().exists(itemToWorkflow)) {
	// // create our Node representation
	// MapNode node = new MapNode(itemToWorkflow, this.getNodeService(), true);
	// this.browseBean.setupCommonBindingProperties(node);
	//
	// // add property resolvers to show path information
	// node.addPropertyResolver("path", this.browseBean.resolverPath);
	// node.addPropertyResolver("displayPath",
	// this.browseBean.resolverDisplayPath);
	//
	// this.resources.add(node);
	// } else {
	// if (logger.isDebugEnabled())
	// logger.debug("Ignoring " + itemToWorkflow
	// + " as it has been removed from the repository");
	// }
	//
	// // commit the transaction
	// tx.commit();
	// } catch (Throwable err) {
	// Utils.addErrorMessage(
	// MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
	// Repository.ERROR_GENERIC), err.getMessage()), err);
	// this.resources = Collections.<Node> emptyList();
	// try {
	// if (tx != null) {
	// tx.rollback();
	// }
	// } catch (Exception tex) {
	// }
	// }
	//
	// return this.resources;
	// }

}
