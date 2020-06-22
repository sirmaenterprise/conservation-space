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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.activiti.ActivitiWorkflowManager;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for common Workflow functionality.
 * <p>
 * This class should be replaced with calls to a WorkflowService once it is
 * available.
 *
 * @author Kevin Roast
 */
public class WorkflowUtil {

	/** The logger. */
	private static Log logger = LogFactory.getLog(WorkflowUtil.class);

	/** The namespace service. */
	private static NamespaceService namespaceService;

	private static ActivitiWorkflowManager activitiWorkflowManager;

	/**
	 * Prepares the given node for persistence in the workflow engine.
	 *
	 * @param node
	 *            The node to package up for persistence
	 * @return The map of data representing the node
	 */
	@SuppressWarnings("unchecked")
	public static Map<QName, Serializable> prepareTaskParams(Node node) {
		Map<QName, Serializable> params = new HashMap<QName, Serializable>();

		// marshal the properties and associations captured by the property
		// sheet
		// back into a Map to pass to the workflow service

		// go through all the properties in the transient node and add them to
		// params map
		Map<String, Object> props = node.getProperties();
		for (String propName : props.keySet()) {
			QName propQName = resolveToQName(propName);
			params.put(propQName, (Serializable) props.get(propName));
		}

		// go through any associations that have been added to the start task
		// and build a list of NodeRefs representing the targets
		Map<String, Map<String, AssociationRef>> assocs = node.getAddedAssociations();
		for (String assocName : assocs.keySet()) {
			QName assocQName = resolveToQName(assocName);

			// get the associations added and create list of targets
			Map<String, AssociationRef> addedAssocs = assocs.get(assocName);
			List<AssociationRef> originalAssocRefs = (List<AssociationRef>) node.getAssociations()
					.get(assocName);
			List<NodeRef> targets = new ArrayList<NodeRef>(addedAssocs.size());

			if (originalAssocRefs != null) {
				for (AssociationRef assoc : originalAssocRefs) {
					targets.add(assoc.getTargetRef());
				}
			}

			for (AssociationRef assoc : addedAssocs.values()) {
				targets.add(assoc.getTargetRef());
			}

			params.put(assocQName, (Serializable) targets);
		}

		// go through the removed associations and either setup or adjust the
		// parameters map accordingly
		assocs = node.getRemovedAssociations();

		for (String assocName : assocs.keySet()) {
			QName assocQName = resolveToQName(assocName);

			// get the associations removed and create list of targets
			Map<String, AssociationRef> removedAssocs = assocs.get(assocName);
			List<NodeRef> targets = (List<NodeRef>) params.get(assocQName);

			if (targets == null) {
				// if there weren't any assocs of this type added get the
				// current
				// set of assocs from the node
				List<AssociationRef> originalAssocRefs = (List<AssociationRef>) node
						.getAssociations().get(assocName);
				targets = new ArrayList<NodeRef>(originalAssocRefs.size());

				for (AssociationRef assoc : originalAssocRefs) {
					targets.add(assoc.getTargetRef());
				}
			}

			// remove the assocs the user deleted
			for (AssociationRef assoc : removedAssocs.values()) {
				targets.remove(assoc.getTargetRef());
			}

			params.put(assocQName, (Serializable) targets);
		}

		// TODO: Deal with child associations if and when we need to support
		// them for workflow tasks, for now warn that they are being used
		Map<?, ?> childAssocs = node.getAddedChildAssociations();
		if (childAssocs.size() > 0) {
			if (logger.isWarnEnabled())
				logger.warn("Child associations are present but are not supported for workflow tasks, ignoring...");
		}

		return params;
	}

	/**
	 * Resolve to q name.
	 *
	 * @param localName
	 *            the local name
	 * @return the q name
	 */
	private static QName resolveToQName(String localName) {
		return QName.resolveToQName(namespaceService, localName);
	}

	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService
	 *            the new namespace service
	 */
	public static void setNamespaceService(NamespaceService namespaceService) {
		WorkflowUtil.namespaceService = namespaceService;

	}

	public static void setActivitiWorkflowManager(ActivitiWorkflowManager activitiWorkflowManager) {
		WorkflowUtil.activitiWorkflowManager = activitiWorkflowManager;
	}

	public static ActivitiWorkflowManager getActivitiWorkflowManager() {
		return activitiWorkflowManager;
	}
	// publiWc static boolean isTaskEditable(String taskId, ServletContext sc) {
	// if (taskId == null || taskId.isEmpty()) {
	// return false;
	// }
	// ServiceRegistry serviceRegistry = Repository.getServiceRegistry(sc);
	// String username =
	// serviceRegistry.getAuthenticationService().getCurrentUserName();
	// WorkflowService workflowService = serviceRegistry.getWorkflowService();
	// WorkflowTask task = workflowService.getTaskById(taskId);
	// return workflowService.isTaskEditable(task, username);
	// }
}
