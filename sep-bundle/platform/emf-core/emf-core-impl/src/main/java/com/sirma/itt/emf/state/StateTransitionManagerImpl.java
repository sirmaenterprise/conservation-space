package com.sirma.itt.emf.state;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Implementation for automatic state transition management defined in.
 * {@link StateTransitionManager}
 * 
 * @author BBonev
 */
@ApplicationScoped
public class StateTransitionManagerImpl implements StateTransitionManager, Serializable {

	private static final String REQUIRED = "REQUIRED";

	private static final DefaultPrimaryStateTypeImpl INITIAL_STATE = new DefaultPrimaryStateTypeImpl(
			PrimaryStateType.INITIAL);

	private static final long serialVersionUID = -1733380793348830471L;

	/** Finds fields definitions within expression. */
	private static final Pattern FIELD_PATTERN = Pattern.compile("\\[(\\w+?)\\]", Pattern.CANON_EQ);

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private javax.enterprise.inject.Instance<StateService> stateServiceInstance;

	@Override
	public boolean isStateManagementSupported(Instance instance) {
		StateTransitionalModel model = getModel(instance);
		return (model != null) && !model.getStateTransitions().isEmpty();
	}

	@Override
	public String getNextState(Instance instance, String currentState, String operation) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			// automatic state management is not supported for this instance
			return null;
		}

		String stateLocal = getNonNullState(instance, currentState);
		// if the current state is null then we will return the initial state
		if (StringUtils.isNullOrEmpty(currentState)) {
			return stateLocal;
		}
		// not to clear the state if transition is not found
		String nextState = stateLocal;
		// wildcard search as well
		StateTransition stateTransition = findTransition(model, stateLocal, operation, true);
		if (stateTransition != null) {
			nextState = stateTransition.getToState();
		}
		return nextState;
	}

	@Override
	public Set<String> getRequiredFields(Instance instance, String currentState, String operation) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			// automatic state management is not supported for this instance
			return Collections.emptySet();
		}

		Set<String> requiredFields = Collections.emptySet();

		String stateLocal = getNonNullState(instance, currentState);

		StateTransition stateTransition = findTransition(model, stateLocal, operation, false);
		if (stateTransition != null) {
			requiredFields = new LinkedHashSet<>();
			for (Condition condition : stateTransition.getConditions()) {
				if (EqualsHelper.nullSafeEquals(REQUIRED, condition.getRenderAs())) {
					collectRequiredFields(requiredFields, condition);
				}
			}
		}
		return requiredFields;
	}

	/**
	 * Collect required fields.
	 * 
	 * @param requiredFields
	 *            the required fields
	 * @param condition
	 *            the condition
	 */
	private void collectRequiredFields(Set<String> requiredFields, Condition condition) {
		String expression = condition.getExpression();
		if (expression != null) {
			Matcher matcher = FIELD_PATTERN.matcher(expression);
			while (matcher.find()) {
				requiredFields.add(matcher.group(1));
			}
		}
	}

	@Override
	public String getRequiredFieldsExpression(Instance instance, String currentState,
			String operation) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			// automatic state management is not supported for this instance
			return null;
		}

		String stateLocal = getNonNullState(instance, currentState);
		String expression = null;
		StateTransition stateTransition = findTransition(model, stateLocal, operation, false);
		if (stateTransition != null) {
			for (Condition condition : stateTransition.getConditions()) {
				if (EqualsHelper.nullSafeEquals("DISABLE_SAVE", condition.getRenderAs())) {
					expression = condition.getExpression();
					break;
				}
			}
		}
		return expression;
	}

	@Override
	public Set<String> getAllowedOperations(Instance instance, String currentState) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			return Collections.emptySet();
		}
		String stateLocal = getNonNullState(instance, currentState);
		List<StateTransition> stateTransitions = model.getStateTransitions();
		Set<String> transitions = new LinkedHashSet<>();
		for (StateTransition stateTransition : stateTransitions) {
			if (EqualsHelper.nullSafeEquals(stateLocal, stateTransition.getFromState())) {
				transitions.add(stateTransition.getTransitionId());
			}
		}
		return transitions;
	}

	@Override
	public <A extends Action> Set<A> getAllowedActions(Instance instance, String currentState) {
		return getAllowedActions(instance, currentState, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends Action> Set<A> getAllowedActions(Instance instance, String currentState,
			Set<A> set) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			return Collections.emptySet();
		}
		String stateLocal = getNonNullState(instance, currentState);
		List<StateTransition> stateTransitions = model.getStateTransitions();
		Set<String> transitions = CollectionUtils.createLinkedHashSet(stateTransitions.size());
		for (StateTransition stateTransition : stateTransitions) {
			if (EqualsHelper.nullSafeEquals(stateLocal, stateTransition.getFromState())
					|| ((set == null) && (stateLocal == null))) {
				transitions.add(stateTransition.getTransitionId());
			}
		}

		Set<A> actions = CollectionUtils.createLinkedHashSet(transitions.size());
		List<TransitionDefinition> actionDefinitions = DefinitionUtil.filterTransitionsByPurpose(
				model, DefinitionUtil.TRANSITION_PERPOSE_ACTION);
		for (TransitionDefinition transition : actionDefinitions) {
			if (((set == null) && stateTransitions.isEmpty())
					|| ((stateLocal == null) && (set != null) && set.contains(transition)) // documents
					|| (transitions.contains(transition.getIdentifier()) && ((set == null) || set
							.contains(transition)))) {
				// prevent modification of the transition object
				transition.seal();
				actions.add((A) transition);
			}
		}
		return actions;
	}

	@Override
	public Set<String> getAllowedStates(Instance instance) {
		if (instance == null) {
			return Collections.emptySet();
		}
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			return Collections.emptySet();
		}
		Set<String> result = new LinkedHashSet<>();
		// collects the end states of all transitions
		// the initial state is not interested due to the fact that the user could not work with it
		for (StateTransition transition : model.getStateTransitions()) {
			result.add(transition.getToState());
		}
		return result;
	}

	/**
	 * Gets the state transitional model for the given instance. If the definition of the instance
	 * supports such functions
	 * 
	 * @param instance
	 *            the instance
	 * @return the model if supported or <code>null</code> if not
	 */
	protected StateTransitionalModel getModel(Instance instance) {
		DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);
		if (definition instanceof StateTransitionalModel) {
			return (StateTransitionalModel) definition;
		}
		return null;
	}

	/**
	 * Finds a transition from the given model that satisfy the given current state and operation.
	 * 
	 * @param model
	 *            the model
	 * @param currentState
	 *            the current state
	 * @param operation
	 *            the operation
	 * @param wildcardTransition
	 *            whether to check from any
	 * @return the state transition is found or <code>null</code> if no valid transition is found
	 */
	protected StateTransition findTransition(StateTransitionalModel model, String currentState,
			String operation, boolean wildcardTransition) {
		List<StateTransition> stateTransitions = model.getStateTransitions();
		// first check for explicit transition
		for (StateTransition stateTransition : stateTransitions) {
			if (EqualsHelper.nullSafeEquals(currentState, stateTransition.getFromState(), true)
					&& EqualsHelper.nullSafeEquals(operation, stateTransition.getTransitionId(),
							true)) {
				// found valid transition
				return stateTransition;
			}
		}
		if (wildcardTransition) {
			for (StateTransition stateTransition : stateTransitions) {
				if (EqualsHelper.nullSafeEquals("*", stateTransition.getFromState(), false)
						&& EqualsHelper.nullSafeEquals(operation,
								stateTransition.getTransitionId(), true)) {
					return stateTransition;
				}
			}
		}
		// no valid transition found
		return null;
	}

	/**
	 * Gets the non null state. checks if the state is null if so uses the state service to
	 * determine the initial state of the given instance
	 * 
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @return the new non null state
	 */
	protected String getNonNullState(Instance instance, String currentState) {
		if (StringUtils.isNullOrEmpty(currentState)) {
			return stateServiceInstance.get().getState(INITIAL_STATE, instance.getClass());
		}
		return currentState;
	}

}
