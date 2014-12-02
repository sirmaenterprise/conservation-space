package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;

/**
 * The Class WorkflowInfo.
 * 
 * @author bbanchev
 */
public class WorkflowInfo implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5954269292420976394L;

	/** The workflow definition. */
	private final String workflowDefinition;

	/** The workflow description. */
	private final String workflowDescription;

	/** The workflow link. */
	private final String workflowLink;

	/**
	 * Instantiates a new workflow info.
	 * 
	 * @param workflowDefinition
	 *            the workflow definition
	 * @param workflowDescription
	 *            the workflow description
	 * @param workflowLink
	 *            the workflow link
	 */
	public WorkflowInfo(String workflowDefinition, String workflowDescription, String workflowLink) {
		super();
		this.workflowDefinition = workflowDefinition;
		this.workflowDescription = workflowDescription;
		this.workflowLink = workflowLink;
	}

	/**
	 * @return the workflowDefinition
	 */
	public String getWorkflowDefinition() {
		return workflowDefinition;
	}

	/**
	 * @return the workflowDescription
	 */
	public String getWorkflowDescription() {
		return workflowDescription;
	}

	/**
	 * @return the workflowLink
	 */
	public String getWorkflowLink() {
		return workflowLink;
	}

}
