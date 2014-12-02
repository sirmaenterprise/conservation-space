package com.sirma.cmf.web.workflow;

import java.util.List;

import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * WorkflowDefinitionFilterEvent fired when workflow definitions are loaded before visualizing to
 * the start new worklfow page thus allowing additional filtering to be applied.
 * 
 * @author svelikov
 */
@Documentation("WorkflowDefinitionFilterEvent fired when workflow definitions are loaded before visualizing to the start new worklfow page thus allowing additional filtering to be applied.")
public class WorkflowDefinitionFilterEvent implements EmfEvent {

	/** The workflow definitions. */
	private List<WorkflowDefinition> workflowDefinitions;

	/**
	 * Instantiates a new workflow definition filter event.
	 * 
	 * @param workflowDefinitionsList
	 *            the workflow definitions
	 */
	public WorkflowDefinitionFilterEvent(List<WorkflowDefinition> workflowDefinitionsList) {
		this.workflowDefinitions = workflowDefinitionsList;
	}

	/**
	 * Getter method for workflowDefinitions.
	 * 
	 * @return the workflowDefinitions
	 */
	public List<WorkflowDefinition> getWorkflowDefinitions() {
		return workflowDefinitions;
	}

	/**
	 * Setter method for workflowDefinitions.
	 * 
	 * @param workflowDefinitionsList
	 *            the workflowDefinitions to set
	 */
	public void setWorkflowDefinitions(List<WorkflowDefinition> workflowDefinitionsList) {
		this.workflowDefinitions = workflowDefinitionsList;
	}

}
