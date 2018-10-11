package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

/**
 * Request for starting a workflow as action.
 *
 * @author bbanchev
 */
public class BPMStartRequest extends BPMTransitionRequest {
	private static final long serialVersionUID = -6009656633029566404L;
	/** The bpm start action name */
	protected static final String START_OPERATION = "bpmStart";

	@Override
	public String getOperation() {
		return START_OPERATION;
	}

}
