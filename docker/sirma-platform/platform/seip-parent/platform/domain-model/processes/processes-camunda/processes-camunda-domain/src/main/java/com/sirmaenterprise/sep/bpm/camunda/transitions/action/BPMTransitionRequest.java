package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionRequest;

/**
 * Request action for bpm transition.
 * 
 * @author bbanchev
 */
public class BPMTransitionRequest extends BPMActionRequest {
	private static final long serialVersionUID = -2106372192452160218L;

	static final String OPERATION_NAME = "bpmTransition";

	private Map<String, Instance> transitionData;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	/**
	 * Provides the transition data binded as a map of instances ids and related properties as instance
	 * 
	 * @param transitionData
	 *            the data to set
	 */
	public void setTransitionData(Map<String, Instance> transitionData) {
		this.transitionData = transitionData;
	}

	/**
	 * Gets the transition data - binded as a map of instances ids and related properties
	 * 
	 * @return the transition data
	 */
	public Map<String, Instance> getTransitionData() {
		return transitionData;
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append(this.getClass().getSimpleName());
		toString.append(" (");
		toString.append(getTargetId());
		toString.append(") = ");
		toString.append(transitionData);
		return toString.toString();
	}

}
