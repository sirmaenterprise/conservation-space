/**
 *
 */
package com.sirmaenterprise.sep.bpm.camunda.configuration;

/**
 * Defines configurations specific for workflow and task functionality.
 *
 * @author BBonev
 */
public interface WorkflowConfigurations {

	/**
	 * Gets the workflow priority low.
	 *
	 * @return the workflow priority low
	 */
	String getWorkflowPriorityLow();

	/**
	 * Gets the workflow priority normal.
	 *
	 * @return the workflow priority normal
	 */
	String getWorkflowPriorityNormal();

	/**
	 * Gets the workflow priority high.
	 *
	 * @return the workflow priority high
	 */
	String getWorkflowPriorityHigh();
}