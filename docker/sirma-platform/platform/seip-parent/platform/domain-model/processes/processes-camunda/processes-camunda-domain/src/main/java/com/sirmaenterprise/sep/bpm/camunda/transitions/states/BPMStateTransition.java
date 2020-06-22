package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import java.util.List;

import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * {@link BPMStateTransition} is a dynamic {@link StateTransition} that is generated based on the current activity flow
 * and possible outcomes.
 * 
 * @author bbanchev
 */
public class BPMStateTransition implements StateTransition {

	private String identifier;
	private List<Condition> conditions;
	private String fromState;
	private String transitionId;
	private String toState;

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public List<Condition> getConditions() {
		return conditions;
	}

	void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public String getFromState() {
		return fromState;
	}

	void setFromState(String fromState) {
		this.fromState = fromState;
	}

	@Override
	public String getTransitionId() {
		return transitionId;
	}

	void setTransitionId(String transitionId) {
		this.transitionId = transitionId;
	}

	@Override
	public String getToState() {
		return toState;
	}

	void setToState(String toState) {
		this.toState = toState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StateTransition)) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(identifier, ((StateTransition) obj).getIdentifier());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[{");
		builder.append(fromState);
		builder.append(" >>> ");
		builder.append(transitionId);
		builder.append(" >>> ");
		builder.append(toState);
		builder.append("}, conditions=");
		builder.append(conditions);
		builder.append("]");
		return builder.toString();
	}

}
