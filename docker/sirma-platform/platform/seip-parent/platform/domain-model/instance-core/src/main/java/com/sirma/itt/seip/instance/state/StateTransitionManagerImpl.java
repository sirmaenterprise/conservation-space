package com.sirma.itt.seip.instance.state;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.definition.StateTransitionalModel;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Implementation for automatic state transition management defined in. {@link StateTransitionManager}
 *
 * @author BBonev
 */
@ApplicationScoped
public class StateTransitionManagerImpl implements StateTransitionManager, Serializable {

	static final String REQUIRED_FIELDS = "requiredFields";

	static final String PROPERTY = "PROPERTY";

	static final String DISABLE_SAVE = "DISABLE_SAVE";

	static final String REQUIRED = "REQUIRED";

	private static final DefaultPrimaryStateTypeImpl INITIAL_STATE = new DefaultPrimaryStateTypeImpl(
			PrimaryStates.INITIAL_KEY);

	private static final long serialVersionUID = -1733380793348830471L;

	/** Finds fields definitions within expression. */
	private static final Pattern FIELD_PATTERN = Pattern.compile("\\[([\\w:]+?)\\]", Pattern.CANON_EQ);
	private static final Logger LOGGER = LoggerFactory.getLogger(StateTransitionManagerImpl.class);

	@Inject
	private DefinitionService definitionService;

	@Inject
	private javax.enterprise.inject.Instance<StateService> stateServiceInstance;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private ResourceService resourceService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	@ExtensionPoint(DynamicStateTransitionProvider.TARGET_NAME)
	private Plugins<DynamicStateTransitionProvider> transitionProviders;

	@Override
	public boolean isStateManagementSupported(Instance instance) {
		StateTransitionalModel model = getModel(instance);
		return model != null && !model.getStateTransitions().isEmpty();
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
		if (StringUtils.isBlank(operation)) {
			return stateLocal;
		}
		// not to clear the state if transition is not found
		String nextState = stateLocal;
		// wildcard search as well
		StateTransition stateTransition = findTransition(instance, model, stateLocal, operation, true);
		if (stateTransition != null) {
			nextState = stateTransition.getToState();
		}
		return nextState;
	}

	@Override
	public Set<String> getRequiredFields(Instance instance, String currentState, String operation) {
		return collectRequiredEntries(instance, currentState, operation, REQUIRED_FIELDS);
	}

	private Set<String> collectRequiredEntries(Instance instance, String currentState, String operation,
			String conditionId) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			// automatic state management is not supported for this instance
			return Collections.emptySet();
		}

		Set<String> requiredFields = null;

		String stateLocal = getNonNullState(instance, currentState);

		StateTransition stateTransition = findTransition(instance, model, stateLocal, operation, false);
		if (stateTransition != null) {
			requiredFields = new LinkedHashSet<>();
			collectRequired(conditionId, requiredFields, stateTransition.getConditions());
		}
		if (operation != null) {
			List<TransitionDefinition> transitions = model.getTransitions();
			for (TransitionDefinition transitionDefinition : transitions) {
				if (operation.equals(transitionDefinition.getIdentifier())) {
					requiredFields = requiredFields == null ? new LinkedHashSet<>() : requiredFields;
					collectRequired(conditionId, requiredFields, transitionDefinition.getConditions());
				}
			}
		}
		return requiredFields == null ? Collections.emptySet() : requiredFields;
	}

	private static void collectRequired(String checkedConditionId, Set<String> requiredFields,
			List<Condition> conditions) {
		for (Condition condition : conditions) {
			String expression = condition.getExpression();
			if (!EqualsHelper.nullSafeEquals(REQUIRED, condition.getRenderAs())
					|| !checkedConditionId.equals(condition.getIdentifier()) || expression == null) {
				// skip non required and non matching conditions
				continue;
			}
			if (REQUIRED_FIELDS.equals(condition.getIdentifier())) {
				Matcher matcher = FIELD_PATTERN.matcher(expression);
				while (matcher.find()) {
					requiredFields.add(matcher.group(1));
				}
			}
		}
	}

	@Override
	public String getRequiredFieldsExpression(Instance instance, String currentState, String operation) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			// automatic state management is not supported for this instance
			return null;
		}

		String stateLocal = getNonNullState(instance, currentState);
		String expression = null;
		StateTransition stateTransition = findTransition(instance, model, stateLocal, operation, false);
		if (stateTransition != null) {
			for (Condition condition : stateTransition.getConditions()) {
				if (EqualsHelper.nullSafeEquals(DISABLE_SAVE, condition.getRenderAs())) {
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
			if (EqualsHelper.nullSafeEquals(stateLocal, stateTransition.getFromState())
					&& checkConditions(instance, stateTransition)) {
				transitions.add(stateTransition.getTransitionId());
			}
		}
		return transitions;
	}

	private boolean checkConditions(Instance instance, StateTransition stateTransition) {
		// if no additional conditions are specified then we are good to go
		if (instance == null || stateTransition.getConditions().isEmpty()) {
			return true;
		}
		int properties = 0;
		for (Condition condition : stateTransition.getConditions()) {
			if (EqualsHelper.nullSafeEquals(PROPERTY, condition.getRenderAs(), true)) {
				properties++;
				Serializable currentValue = instance.getProperties().get(condition.getIdentifier());
				if (areValuesMatch(currentValue, condition.getExpression())) {
					return true;
				}
			}
		}
		// if there is at least one property that got invalid match then we should return false
		return !(properties > 0);
	}

	/**
	 * Checks if the current value is the same as the one passed in a property condition expression. The method will try
	 * to convert the string values to the current value type if any. The method can handle collections and resource
	 * values.
	 *
	 * @return <code>true</code>, if both parameters are <code>null</code> or match and <code>false</code> otherwise.
	 */
	private boolean areValuesMatch(Object currentValue, String expression) {
		// if both values are null we have a match
		if (currentValue == null) {
			return StringUtils.isBlank(expression);
		} else if (StringUtils.isBlank(expression)) {
			// if we have a value but expression is empty no need to continue
			return false;
		}

		// if the value is collection we should check the collection elements for match
		// we can't convert the expression to collection
		if (currentValue instanceof Collection) {
			Collection<?> collection = (Collection<?>) currentValue;
			for (Object object : collection) {
				if (areValuesMatch(object, expression)) {
					return true;
				}
			}
			return false;
		} else if (currentValue instanceof Resource) {
			// if we have to match against user. probably not going to happen but just in case
			return resourceService.areEqual(currentValue, expression);
		}
		Class<?> currentValueType = currentValue.getClass();
		try {
			return EqualsHelper.nullSafeEquals(currentValue, typeConverter.convert(currentValueType, expression));
		} catch (TypeConversionException e) {
			LOGGER.trace("Cannot convert expression value {} to instance value type {}", expression, currentValueType,
					e);
		}
		return false;
	}

	@Override
	public <A extends Action> Set<A> getAllowedActions(Instance instance, String currentState) {
		return getAllowedActions(instance, currentState, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends Action> Set<A> getAllowedActions(Instance instance, String currentState, Set<A> set) {
		StateTransitionalModel model = getModel(instance);
		if (model == null) {
			return Collections.emptySet();
		}
		String stateLocal = getNonNullState(instance, currentState);
		Collection<StateTransition> stateTransitions = extractTransitions(instance, model);

		Set<String> allowedTransitionsForState = stateTransitions
				.stream()
					.filter(tr -> EqualsHelper.nullSafeEquals(stateLocal, tr.getFromState())
							&& checkConditions(instance, tr) || set == null && stateLocal == null)
					.map(StateTransition::getTransitionId)
					.collect(Collectors.toCollection(LinkedHashSet::new));

		List<TransitionDefinition> actionDefinitions = DefinitionUtil.filterAction(model);

		boolean allowAll = set == null && stateTransitions.isEmpty();

		// force action checking by equals method
		// yes is not optimal but the EmfAction and transition has different hashes
		// if tried to unify them everything stopped working so this is the result of that
		List<A> filteredActions = null;
		if (set != null) {
			filteredActions = new ArrayList<>(set);
		}

		List<A> filteredActionsList = filteredActions;
		return (Set<A>) actionDefinitions
				.stream()
					.filter(tr -> allowAll || isPartOfAllowedOperations(filteredActionsList, stateLocal, tr)
							|| isAllowedInModel(filteredActionsList, allowedTransitionsForState, tr))
					.peek(tr -> tr.seal()) // prevent modification of the transition object
					.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static <A extends Action> boolean isAllowedInModel(Collection<A> set,
			Set<String> allowedTransitionsForState, TransitionDefinition transition) {
		Action transitionToTest = transition;
		return allowedTransitionsForState.contains(transition.getIdentifier())
				&& (set == null || set.contains(transitionToTest));
	}

	private static <A extends Action> boolean isPartOfAllowedOperations(Collection<A> set, String stateLocal,
			TransitionDefinition transition) {
		Action transitionToTest = transition;
		return stateLocal == null && set != null && set.contains(transitionToTest);
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

	@Override
	public List<TransitionGroupDefinition> getActionGroups(Instance instance) {
		Objects.requireNonNull(instance, "Instance should be provided");

		StateTransitionalModel model = getModel(instance);
		return new ArrayList<>(model.getTransitionGroups());
	}

	/**
	 * Gets the state transitional model for the given instance. If the definition of the instance supports such
	 * functions
	 *
	 * @param instance
	 *            the instance
	 * @return the model if supported or <code>null</code> if not
	 */
	protected StateTransitionalModel getModel(Instance instance) {
		DefinitionModel definition = definitionService.getInstanceDefinition(instance);
		if (definition instanceof StateTransitionalModel) {
			return (StateTransitionalModel) definition;
		}
		return null;
	}

	/**
	 * Finds a transition from the given model that satisfy the given current state and operation.
	 *
	 * @param instance
	 *            the target instance used for conditions checks
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
	protected StateTransition findTransition(Instance instance, StateTransitionalModel model, String currentState,
			String operation, boolean wildcardTransition) {
		Collection<StateTransition> stateTransitions = extractTransitions(instance, model);

		// first check for explicit transition
		Optional<StateTransition> foundTransition = stateTransitions
				.stream()
					.filter(transition -> isStateMatch(currentState, transition)
							&& isOperationMatch(operation, transition) && checkConditions(instance, transition))
					.findFirst();

		if (foundTransition.isPresent()) {
			return foundTransition.get();
		}
		if (wildcardTransition) {
			return stateTransitions
					.stream()
						.filter(transition -> isWildcardTransition(transition)
								&& isOperationMatch(operation, transition) && checkConditions(instance, transition))
						.findFirst()
						.orElse(null);
		}
		return null;
	}

	private Collection<StateTransition> extractTransitions(Instance instance, StateTransitionalModel model) {
		Collection<StateTransition> stateTransitions = model.getStateTransitions();

		if (transitionProviders.count() > 0) {
			stateTransitions = new LinkedHashSet<>(stateTransitions);
			for (DynamicStateTransitionProvider provider : transitionProviders) {
				stateTransitions.addAll(provider.provide(instance));
			}
		}
		return stateTransitions;
	}

	private static boolean isStateMatch(String currentState, StateTransition transition) {
		return EqualsHelper.nullSafeEquals(currentState, transition.getFromState(), true);
	}

	private static boolean isWildcardTransition(StateTransition transition) {
		return EqualsHelper.nullSafeEquals("*", transition.getFromState(), false);
	}

	private static boolean isOperationMatch(String operation, StateTransition transition) {
		return EqualsHelper.nullSafeEquals(operation, transition.getTransitionId(), true);
	}

	/**
	 * Gets the non null state. checks if the state is null if so uses the state service to determine the initial state
	 * of the given instance
	 *
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @return the new non null state
	 */
	protected String getNonNullState(Instance instance, String currentState) {
		if (StringUtils.isBlank(currentState)) {
			return stateServiceInstance.get().getState(INITIAL_STATE, instance.type().getCategory());
		}
		return currentState;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends Action> Set<A> getActions(Serializable identifier, Set<String> actionIds) {
		Optional<Instance> instance = instanceTypeResolver
				.resolveReference(identifier)
					.map(InstanceReference::toInstance);
		if (!instance.isPresent()) {
			return emptySet();
		}

		StateTransitionalModel transitionalModel = getModel(instance.get());
		if (transitionalModel == null) {
			return emptySet();
		}

		List<TransitionDefinition> transitions = DefinitionUtil.filterAction(transitionalModel);
		if (isEmpty(actionIds)) {
			transitions.forEach(tr -> tr.seal());
			return (Set<A>) new LinkedHashSet<>(transitions);
		}

		return (Set<A>) transitions
				.stream()
					.filter(transition -> actionIds.contains(transition.getIdentifier()))
					.peek(tr -> tr.seal())
					.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
