package com.sirma.itt.seip.permissions.action;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.Filterable;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * The RoleEvaluatorRuntimeSettings is wrapper for settings used in role evaluators to provide fine grained evaluation.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class RoleActionEvaluatorServiceImpl implements RoleActionFilterService {
	@Inject
	@ExtensionPoint(value = RoleActionFilterProvider.TARGET_NAME)
	private Plugins<RoleActionFilterProvider> filterProviders;

	@Inject
	private ContextualMap<String, Predicate<RoleActionEvaluatorContext>> predefinedFilters;

	/**
	 * Initialize the service with predefined predicates from providers.
	 */
	@PostConstruct
	public void initialize() {
		predefinedFilters.initializeWith(() -> provideFilters());
	}

	private Map<String, Predicate<RoleActionEvaluatorContext>> provideFilters() {
		Map<String, Predicate<RoleActionEvaluatorContext>> filters = new HashMap<>();
		for (RoleActionFilterProvider nextProvider : filterProviders) {
			filters.putAll(nextProvider.provideFilters());
		}
		return filters;
	}

	/**
	 * On definition reload clear the filter cache so that on next use any new filters could be loaded
	 *
	 * @param definitionsLoaded
	 *            the definitions loaded event
	 */
	void onDefinitionReload(@Observes DefinitionsChangedEvent definitionsLoaded) {
		predefinedFilters.clearContextValue();
	}

	/**
	 * Filter the actions using the context.
	 *
	 * @param actions
	 *            the set
	 * @param context
	 *            is the context
	 * @return the sets the
	 */
	@Override
	public Set<Action> filter(Set<Action> actions, RoleActionEvaluatorContext context) {
		Set<Action> filteredActions = createHashSet(actions.size());
		for (Action action : actions) {
			if (isAllowed(action, context)) {
				filteredActions.add(action);
			}
		}
		return filteredActions;

	}

	@Override
	public Set<String> getFilters() {
		return Collections.unmodifiableSet(predefinedFilters.keySet());
	}

	private boolean isAllowed(Action action, RoleActionEvaluatorContext context) {
		if (action instanceof Filterable) {
			Predicate<RoleActionEvaluatorContext> predicate = getFilters(((Filterable) action).getFilters())
					.reduce(Predicate::or).orElse(x -> true);
			return predicate.test(context);
		}
		return true;
	}

	/**
	 * Gets the predicate filters for given list of filter ids/expressions.
	 *
	 * @param list
	 *            the list of filters
	 * @return the filters for given set of filter ids
	 */
	private Stream<Predicate<RoleActionEvaluatorContext>> getFilters(List<String> list) {
		if (list == null) {
			return Stream.empty();
		}
		return list.stream().map(this::filterToPredicate);
	}

	private Predicate<RoleActionEvaluatorContext> filterToPredicate(String filter) {
		// return default predicate that does not affect the result when all filters are combined in a single 'or'
		// statement
		return predefinedFilters.getOrDefault(filter, ctx -> false);
	}
}
