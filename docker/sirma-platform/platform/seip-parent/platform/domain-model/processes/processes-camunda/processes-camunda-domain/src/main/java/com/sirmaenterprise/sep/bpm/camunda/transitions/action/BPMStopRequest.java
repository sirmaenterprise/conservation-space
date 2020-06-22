package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

/**
 *
 * @author hlungov
 */
public class BPMStopRequest extends BPMTransitionRequest {

	private static final long serialVersionUID = -3841852788494565638L;

	/** The bpm stop action name */
	protected static final String STOP_OPERATION = "bpmStop";

	@Override
	public String getOperation() {
		return STOP_OPERATION;
	}
}
