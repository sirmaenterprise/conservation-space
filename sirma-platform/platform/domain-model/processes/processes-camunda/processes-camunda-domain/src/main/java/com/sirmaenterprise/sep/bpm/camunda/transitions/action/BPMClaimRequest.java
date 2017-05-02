package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

/**
 * Request for claiming a task as action
 * 
 * @author Hristo Lungov
 */
public class BPMClaimRequest extends BPMTransitionRequest {

	private static final long serialVersionUID = -1839011743934094859L;
	/** The bpm claim action name */
	protected static final String CLAIM_OPERATION = "bpmClaim";

	@Override
	public String getOperation() {
		return CLAIM_OPERATION;
	}

}