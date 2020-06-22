package com.sirma.itt.seip.definition.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.domain.definition.Condition;

/**
 * Default implementation for {@link StateTransition}
 *
 * @author BBonev
 */
public class StateTransitionImpl extends MergeableBase<StateTransitionImpl>
		implements StateTransition, Serializable, Copyable<StateTransitionImpl>, Sealable {

	private static final long serialVersionUID = -3580596402314513192L;

	@Tag(1)
	private String transitionId;

	@Tag(2)
	private String toState;

	@Tag(3)
	private String fromState;

	@Tag(4)
	private List<Condition> conditions;

	private transient String generatedIdentifier = null;

	private boolean sealed = false;

	@Override
	public List<Condition> getConditions() {
		if (conditions == null) {
			conditions = new LinkedList<>();
		}
		return conditions;
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
	 * Setter method for transitionId.
	 *
	 * @param transitionId
	 *            the transitionId to set
	 */
	public void setTransitionId(String transitionId) {
		if (!isSealed()) {
			this.transitionId = transitionId;
		}
	}

	/**
	 * Setter method for toState.
	 *
	 * @param toState
	 *            the toState to set
	 */
	public void setToState(String toState) {
		if (!isSealed()) {
			this.toState = toState;
		}
	}

	/**
	 * Setter method for fromState.
	 *
	 * @param fromState
	 *            the fromState to set
	 */
	public void setFromState(String fromState) {
		if (!isSealed()) {
			this.fromState = fromState;
		}
	}

	/**
	 * Setter method for conditions.
	 *
	 * @param conditions
	 *            the conditions to set
	 */
	public void setConditions(List<Condition> conditions) {
		if (!isSealed()) {
			this.conditions = conditions;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public StateTransitionImpl mergeFrom(StateTransitionImpl source) {
		fromState = MergeHelper.replaceIfNull(fromState, source.fromState);
		transitionId = MergeHelper.replaceIfNull(transitionId, source.transitionId);
		toState = MergeHelper.replaceIfNull(toState, source.toState);

		conditions = MergeHelper.mergeLists(MergeHelper.convertToMergable(conditions),
				MergeHelper.convertToMergable(source.getConditions()), EmfMergeableFactory.CONDITION_DEFINITION);
		return this;
	}

	@Override
	public String getIdentifier() {
		if (getFromState() == null || getTransitionId() == null || getToState() == null) {
			return null;
		}
		if (generatedIdentifier == null) {
			generatedIdentifier = new StringBuilder(32)
					.append(getFromState())
						.append("|")
						.append(getTransitionId())
						.append("|")
						.append(getToState())
						.toString();
		}
		return generatedIdentifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		// nothing to set
	}

	@Override
	public int hashCode() {
		// cannot use it from the super class, because this method should be overridden, because #equals is overridden
		return Objects.hash(getIdentifier());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof StateTransition) {
			// no need to check the fields the method getIdentifier provides all the data needed for
			// the checks
			return super.equals(obj);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("transition [{");
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

	@Override
	public StateTransitionImpl createCopy() {
		StateTransitionImpl copy = new StateTransitionImpl();
		copy.fromState = fromState;
		copy.toState = toState;
		copy.transitionId = transitionId;

		for (Condition condition : conditions) {
			copy.getConditions().add(((ConditionDefinitionImpl) condition).createCopy());
		}
		return copy;
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void seal() {
		if (isSealed()) {
			return;
		}

		conditions = Collections.unmodifiableList(Sealable.seal(getConditions()));

		sealed = true;
	}
}
