/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.definition.Condition;

/**
 * @author BBonev
 */
public class StateTransitionMock implements StateTransition {

	private String identifier;
	private String transitionId;
	private String fromState;
	private String toState;
	private List<Condition> conditions = new ArrayList<>();

	@Override
	public List<Condition> getConditions() {
		return conditions;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getFromState() {
		return fromState;
	}

	@Override
	public String getTransitionId() {
		return transitionId;
	}

	@Override
	public String getToState() {
		return toState;
	}

	/**
	 * @param transitionId
	 *            the transitionId to set
	 */
	public void setTransitionId(String transitionId) {
		this.transitionId = transitionId;
	}

	/**
	 * @param fromState
	 *            the fromState to set
	 */
	public void setFromState(String fromState) {
		this.fromState = fromState;
	}

	/**
	 * @param toState
	 *            the toState to set
	 */
	public void setToState(String toState) {
		this.toState = toState;
	}

	/**
	 * @param conditions
	 *            the conditions to set
	 */
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
