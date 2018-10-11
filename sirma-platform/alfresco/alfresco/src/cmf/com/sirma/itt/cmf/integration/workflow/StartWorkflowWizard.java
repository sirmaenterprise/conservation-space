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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;
import com.sun.star.uno.RuntimeException;

/**
 * Bean implementation for the Start Workflow Wizard.
 * 
 * @author gavinc
 */
public class StartWorkflowWizard {

	/** The selected workflow. */
	protected String selectedWorkflow;

	/** The workflow service. */
	transient private WorkflowService workflowService;

	/** The invitation service. */
	transient private InvitationService invitationService;

	/** The start task node. */
	protected Node startTaskNode;

	/** The resources. */
	protected List<Node> resources;

	/** The is item being added. */
	protected boolean isItemBeingAdded = false;

	/** The next button disabled. */
	protected boolean nextButtonDisabled = false;

	/** The unprotected node service. */
	transient private NodeService unprotectedNodeService;

	/** The Constant LOGGER. */
	private static final Log LOGGER = LogFactory.getLog(StartWorkflowWizard.class);

	private static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();

	/** The registry. */
	private ServiceRegistry registry;

	/** The item to workflow. */
	private NodeRef itemToWorkflow;

	/** The case service. */
	private CMFService caseService;

	/** The workflow definition. */
	private WorkflowDefinition workflowDefinition;

	/**
	 * Sets the unprotected node service.
	 * 
	 * @param unprotectedNodeService
	 *            the new unprotected node service
	 */
	public void setUnprotectedNodeService(NodeService unprotectedNodeService) {
		this.unprotectedNodeService = unprotectedNodeService;
	}

	/**
	 * Instantiates a new start workflow wizard.
	 * 
	 * @param registry
	 *            the registry
	 * @param caseService
	 *            the case service
	 */
	public StartWorkflowWizard(ServiceRegistry registry, CMFService caseService) {
		this.registry = registry;
		this.caseService = caseService;
		com.sirma.itt.cmf.integration.workflow.WorkflowUtil.setNamespaceService(registry
				.getNamespaceService());
		try {
			Field field = Repository.class.getDeclaredField("namespaceService");
			field.setAccessible(true);
			field.set(null, registry.getNamespaceService());
			field.setAccessible(false);

			field = Repository.class.getDeclaredField("serviceRegistry");
			field.setAccessible(true);
			field.set(null, registry);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the unprotected node service.
	 * 
	 * @return the unprotected node service
	 */
	protected NodeService getUnprotectedNodeService() {
		if (this.unprotectedNodeService == null) {
			this.unprotectedNodeService = registry.getNodeService();
		}
		return this.unprotectedNodeService;
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

		this.startTaskNode = null;
		this.resources = null;
		this.isItemBeingAdded = false;
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
		QName type = this.getNodeService().getType(itemToWorkflow);
		if (!this.getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER)) {
			throw new WebScriptException(404, "Case to attach to workflow is not required type!");
		}
		createModel();

	}

	/**
	 * Gets the dictionary service.
	 * 
	 * @return the dictionary service
	 */
	private DictionaryService getDictionaryService() {
		return registry.getDictionaryService();
	}

	/**
	 * Process.
	 * 
	 * @param properties
	 *            the properties
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public Pair<WorkflowTask, WorkflowInstance> start(Map<QName, Serializable> properties)
			throws Exception {
		// TODO: Deal with workflows that don't require any data

		if (DEBUG_ENABLED)
			LOGGER.debug("Starting workflow: " + this.selectedWorkflow);

		// prepare the parameters from the current state of the property sheet
		Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(this.startTaskNode);
		// first set initial properties and then continue set them
		params.putAll(properties);
		if (DEBUG_ENABLED)
			LOGGER.debug("Starting workflow with parameters: " + params);

		// create a workflow package for the attached items and add them
		NodeRef workflowPackage = this.getWorkflowService().createPackage(null);
		params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);

		this.getUnprotectedNodeService().addChild(
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
			params.put(WorkflowModel.PROP_CONTEXT, (Serializable) workflowContext.getNodeRef());
		}

		params.putAll(properties);
		if (DEBUG_ENABLED) {
			LOGGER.debug("Workflow Params " + params);
		}
		WorkflowTask endedTask = null;
		// start the workflow to get access to the start task
		WorkflowPath path = this.getWorkflowService().startWorkflow(workflowDefinition.getId(),
				params);
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
			List<WorkflowTask> tasks = this.getWorkflowService().getTasksForWorkflowPath(
					path.getId());
			if (tasks.size() == 1) {
				WorkflowTask startTask = tasks.get(0);
				if (DEBUG_ENABLED)
					LOGGER.debug("Found start task:" + startTask);

				endedTask = this.getWorkflowService().endTask(startTask.getId(), null);

				// remove start task properties from workflow
				getNodeService().removeProperty(workflowPackage,
						CMFModel.PROP_SRART_TRANSITION_OUTCOME);
				getNodeService().removeProperty(workflowPackage, CMFModel.PROP_WF_TASK_STATUS);

				getNodeService().removeProperty(workflowPackage, CMFModel.PROP_TASK_MODIFIED);
				getNodeService().removeAspect(workflowPackage, CMFModel.ASPECT_TASK_NODE);
				getNodeService().removeAspect(workflowPackage, CMFModel.ASPECT_AUDITABLE);
			}

			if (DEBUG_ENABLED)
				LOGGER.debug("Started workflow: " + this.selectedWorkflow);
			return new Pair<WorkflowTask, WorkflowInstance>(endedTask, path.getInstance());
		}
		return null;
	}

	/**
	 * Gets the workflow service.
	 * 
	 * @return the workflow service
	 */
	private WorkflowService getWorkflowService() {
		if (workflowService == null) {
			workflowService = registry.getWorkflowService();
		}
		return workflowService;
	}

	/**
	 * Creates the model.
	 */
	public void createModel() {

		if (DEBUG_ENABLED)
			LOGGER.debug("Selected workflow: " + selectedWorkflow);

		WorkflowTaskDefinition taskDef = workflowDefinition.getStartTaskDefinition();
		if (taskDef != null) {
			if (DEBUG_ENABLED)
				LOGGER.debug("Start task definition: " + taskDef);

			// create an instance of a task from the data dictionary
			this.startTaskNode = TransientNode.createNew(registry, taskDef.getMetadata(), "task_"
					+ System.currentTimeMillis(), null);
		} else {
			throw new WebScriptException(404, "No start task found for: "
					+ workflowDefinition.getName());
		}

	}

	// ------------------------------------------------------------------------------
	// Event Handlers

	/**
	 * Prepares the dialog to allow the user to add an item to the workflow
	 * package.
	 * 
	 * @param event
	 *            The event
	 */
	public void prepareForAdd(ActionEvent event) {
		this.isItemBeingAdded = true;
	}

	/**
	 * Cancels the adding of an item to the workflow package.
	 * 
	 * @param event
	 *            The event
	 */
	public void cancelAddPackageItems(ActionEvent event) {
		this.isItemBeingAdded = false;
	}

	/**
	 * Determines whether an item is currently being added to the workflow
	 * package.
	 * 
	 * @return true if an item is being added
	 */
	public boolean isItemBeingAdded() {
		return this.isItemBeingAdded;
	}

	/**
	 * Returns the workflow selected by the user.
	 * 
	 * @return The selected workflow
	 */
	public String getSelectedWorkflow() {
		return selectedWorkflow;
	}

	/**
	 * Sets the selected workflow.
	 * 
	 * @param selectedWorkflow
	 *            The workflow selected
	 */
	public void setSelectedWorkflow(String selectedWorkflow) {
		this.selectedWorkflow = selectedWorkflow;
	}

	/**
	 * Returns the Node representing the start task metadata required.
	 * 
	 * @return The Node for the start task
	 */
	public Node getTaskMetadataNode() {
		return this.startTaskNode;
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

	/**
	 * Gets the node service.
	 * 
	 * @return the node service
	 */
	private NodeService getNodeService() {
		return registry.getNodeService();
	}

	/**
	 * Sets the workflow service to use.
	 * 
	 * @param workflowService
	 *            WorkflowService instance
	 */
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * Sets the invitation service.
	 * 
	 * @param invitationService
	 *            the new invitation service
	 */
	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	/**
	 * Gets the invitation service.
	 * 
	 * @return the invitation service
	 */
	public InvitationService getInvitationService() {
		return invitationService;
	}

}
