/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
 * software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

/**
 * Extension of {@link org.alfresco.repo.workflow.jbpm.QviJBPMEngine} to support
 * reporting of workflow tasks. When task is created/ended/updated corresponding
 * method from this class should be invoked to register the change. All tasks
 * are kept as nodes in 'system' container of workspace store. This subsystem
 * could be disabled by setting <code>enabled = false</code> in context xml
 *
 * @author Borislav Banchev
 */
@SuppressWarnings("synthetic-access")
public interface WorkflowReportService {

	/**
	 * Adds new task to the system in specific container, so the task to be
	 * stored as node.<br>
	 * <strong> This method should be invoked for new workflows only with one
	 * task</strong><br>
	 * Code is executed as tenant system user to prevent update problems
	 *
	 * @param path
	 *            the path
	 */
	void addTask(final WorkflowPath path);

	/**
	 * Adds single instance task to the system in specific container, so the
	 * task to be stored as node.<br>
	 * <strong> This method should be invoked for standalone tasks</strong><br>
	 * Code is executed as tenant system user to prevent update problems
	 *
	 * @param task
	 *            the task created
	 * @return the created reporting node
	 */
	NodeRef addTask(final WorkflowTask task);

	/**
	 * Adds single instance task to the system in specific container, so the
	 * task to be stored as node.<br>
	 * <strong> This method should be invoked for standalone tasks</strong><br>
	 * Code is executed as tenant system user to prevent update problems
	 *
	 * @param taskId
	 *            the task id of created task
	 * @param props
	 *            are the standalone tasks properties
	 * @return the created reporting node
	 */
	NodeRef addStandaloneTask(final String taskId, final Map<QName, Serializable> props);

	/**
	 * Converts the standard task data to an optimized format that is also
	 * searchable.
	 *
	 * @param task
	 *            is the task - workflow or standalone task
	 * @param props
	 *            are the task properties
	 * @return the properties that should be stored and are the reporing
	 *         expected format.
	 */
	Map<QName, Serializable> prepareTaskProperties(final WorkflowTask task, final Map<QName, Serializable> props);

	/**
	 * Updates tasks by getting all information for it and setting the node
	 * properties for this task to the new ones.<br>
	 * Code is executed as tenant system user to prevent update problems
	 *
	 * @param task
	 *            is the task to update
	 */
	void updateTask(final WorkflowTask task);

	/**
	 * End task.
	 *
	 * @param endTask
	 *            the end task
	 */
	void endTask(final WorkflowTask endTask);

	/**
	 * Updates active tasks by getting all information for it and setting the
	 * node properties for this task to the new ones.<br>
	 * Code is executed as tenant system user to prevent update problems
	 *
	 * @param instance
	 *            is instance to cancel
	 */
	void cancelWorkflow(final WorkflowInstance instance);

	/**
	 * Delete workflow.
	 *
	 * @param workflowId
	 *            the workflow id
	 */
	void deleteWorkflow(final String workflowId);

	/**
	 * Creates the if not exists.
	 *
	 * @param createdTask
	 *            the created task
	 */
	void createIfNotExists(WorkflowTask createdTask);

	/**
	 * Gets the corresponding node from cache or by lucene query. This could
	 * return null if cache is just emptied or node is not indexed yet.
	 *
	 * @param task
	 *            is the task to search for
	 * @return the node or null if not found
	 */
	NodeRef getTaskNode(WorkflowTask task);

}
