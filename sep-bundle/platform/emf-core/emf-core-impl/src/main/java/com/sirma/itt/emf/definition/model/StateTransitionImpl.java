package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.MergeableBase;
import com.sirma.itt.emf.state.transition.StateTransition;

/**
 * Default implementation for {@link StateTransition}
 * 
 * @author BBonev
 */
public class StateTransitionImpl extends MergeableBase<StateTransitionImpl> implements
		StateTransition, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3580596402314513192L;
	/** The transition id. */
	@Tag(1)
	private String transitionId;
	/** The to state. */
	@Tag(2)
	private String toState;
	/** The from state. */
	@Tag(3)
	private String fromState;
	/** The conditions. */
	@Tag(4)
	private List<Condition> conditions;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Condition> getConditions() {
		if (conditions == null) {
			conditions = new LinkedList<Condition>();
		}
		return conditions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFromState() {
		return fromState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTransitionId() {
		return transitionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToState() {
		return toState;
	}

	/**
	 * Setter method for transitionId.
	 * 
	 * @param transitionId
	 *            the transitionId to set
	 */
	public void setTransitionId(String transitionId) {
		this.transitionId = transitionId;
	}

	/**
	 * Setter method for toState.
	 * 
	 * @param toState
	 *            the toState to set
	 */
	public void setToState(String toState) {
		this.toState = toState;
	}

	/**
	 * Setter method for fromState.
	 * 
	 * @param fromState
	 *            the fromState to set
	 */
	public void setFromState(String fromState) {
		this.fromState = fromState;
	}

	/**
	 * Setter method for conditions.
	 * 
	 * @param conditions
	 *            the conditions to set
	 */
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StateTransitionImpl mergeFrom(StateTransitionImpl source) {
		fromState = MergeHelper.replaceIfNull(fromState, source.fromState);
		transitionId = MergeHelper.replaceIfNull(transitionId, source.transitionId);
		toState = MergeHelper.replaceIfNull(toState, source.toState);

		conditions = MergeHelper.mergeLists(MergeHelper.convertToMergable(conditions),
				MergeHelper.convertToMergable(source.getConditions()),
				EmfMergeableFactory.CONDITION_DEFINITION);
		return this;
	}

	@Override
	public String getIdentifier() {
		if ((getFromState() == null) || (getTransitionId() == null) || (getToState() == null)) {
			return null;
		}
		return getFromState() + "|" + getTransitionId() + "|" + getToState();
	}

	@Override
	public void setIdentifier(String identifier) {
		// nothing to set
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((fromState == null) ? 0 : fromState.hashCode());
		result = (prime * result) + ((toState == null) ? 0 : toState.hashCode());
		result = (prime * result) + ((transitionId == null) ? 0 : transitionId.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StateTransitionImpl other = (StateTransitionImpl) obj;
		if (fromState == null) {
			if (other.fromState != null) {
				return false;
			}
		} else if (!fromState.equals(other.fromState)) {
			return false;
		}
		if (toState == null) {
			if (other.toState != null) {
				return false;
			}
		} else if (!toState.equals(other.toState)) {
			return false;
		}
		if (transitionId == null) {
			if (other.transitionId != null) {
				return false;
			}
		} else if (!transitionId.equals(other.transitionId)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StateTransitionImpl [fromState=");
		builder.append(fromState);
		builder.append(", transitionId=");
		builder.append(transitionId);
		builder.append(", toState=");
		builder.append(toState);
		builder.append(", conditions=");
		builder.append(conditions);
		builder.append("]");
		return builder.toString();
	}
}
