package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

/**
 * Request for releasing a task as action.
 * 
 * @author Hristo Lungov
 */
public class BPMReleaseRequest extends BPMTransitionRequest {

	private static final long serialVersionUID = -1839011743934094859L;
	/** The bpm claim action name */
	protected static final String RELEASE_OPERATION = "bpmRelease";

	@Override
	public String getOperation() {
		return RELEASE_OPERATION;
	}

}