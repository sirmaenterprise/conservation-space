package com.sirma.itt.seip.definition.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.Transitional;
import com.sirma.itt.seip.domain.util.SortableComparator;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Common definition utility methods.
 *
 * @author BBonev
 */
public class DefinitionUtil {

	private static final Pattern RNC_FIELD_PATTERN = Pattern.compile("(?<!\\d+)\\[([\\w:]+?)\\]", Pattern.CANON_EQ);

	private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionUtil.class);

	private static final SortableComparator EXTENDED_SORTABLE_COMPARATOR = new SortableComparator(true);
	private static final SortableComparator BASE_SORTABLE_COMPARATOR = new SortableComparator(false);

	/**
	 * Instantiates a new definition util.
	 */
	private DefinitionUtil() {
		// utility class
	}

	/** The purpose of the transition definitions that is for actions. */
	public static final String TRANSITION_PERPOSE_ACTION = "action";

	/**
	 * Gets the default transitions. These are transitions that does not have a purpose. The method is equal to calling
	 * {@link #filterTransitionsByPurpose(Transitional, String)} with <code>null</code> second argument
	 *
	 * @param transitional
	 *            the transitional
	 * @return the default transitions, or empty list
	 */
	public static List<TransitionDefinition> getDefaultTransitions(Transitional transitional) {
		if (transitional == null || transitional.getTransitions().isEmpty()) {
			return CollectionUtils.emptyList();
		}
		return getDefaultTransitions(transitional.getTransitions());
	}

	/**
	 * Gets the default transitions.These are transitions that does not have a purpose. The method is equal to calling
	 * {@link #filterByPurpose(List, String)} with <code>null</code> second argument
	 *
	 * @param transitions
	 *            the transitions
	 * @return the default transitions, or empty list
	 */
	public static List<TransitionDefinition> getDefaultTransitions(List<TransitionDefinition> transitions) {
		if (transitions == null || transitions.isEmpty()) {
			return CollectionUtils.emptyList();
		}
		return filterByPurpose(transitions, null);
	}

	/**
	 * Checks if the given {@link Purposable} represents a standard action.
	 *
	 * @param purposable
	 *            the purposable to check
	 * @return <code>true</code> for non <code>null</code> instance and the purpose is equal to
	 *         {@link #TRANSITION_PERPOSE_ACTION}.
	 */
	public static boolean isStandardAction(Purposable purposable) {
		return purposable != null
				&& EqualsHelper.nullSafeEquals(purposable.getPurpose(), TRANSITION_PERPOSE_ACTION, true);
	}

	/**
	 * Gets the filter all actions from the given transitional object. An actions is any transition object with purpose
	 * different that <code>null</code> or empty string.
	 *
	 * @param transitional
	 *            the transitional
	 * @return the actions found in the given {@link Transitional} object or empty list if <code>null</code> or non are
	 *         found.
	 */
	public static List<TransitionDefinition> filterAction(Transitional transitional) {
		if (transitional == null || transitional.getTransitions().isEmpty()) {
			return CollectionUtils.emptyList();
		}
		List<TransitionDefinition> transitions = transitional.getTransitions();
		List<TransitionDefinition> result = new ArrayList<>(transitions.size());
		for (TransitionDefinition transitionDefinition : transitions) {
			if (StringUtils.isNotNullOrEmpty(transitionDefinition.getPurpose())) {
				result.add(transitionDefinition);
			}
		}
		return result;
	}

	/**
	 * Gets the filter transitions by purpose.
	 *
	 * @param transitional
	 *            the transitional
	 * @param purpose
	 *            the purpose
	 * @return the filtered definitions from the given {@link Transitional} object or empty list.
	 */
	public static List<TransitionDefinition> filterTransitionsByPurpose(Transitional transitional, String purpose) {
		if (transitional == null || transitional.getTransitions().isEmpty()) {
			return CollectionUtils.emptyList();
		}
		return filterByPurpose(transitional.getTransitions(), purpose);
	}

	/**
	 * Filter objects/definitions by their purpose.
	 *
	 * @param <E>
	 *            the concrete purpose type
	 * @param transitions
	 *            the transitions
	 * @param purpose
	 *            the purpose
	 * @return the filter transitions by purpose, or empty list
	 */
	public static <E extends Purposable> List<E> filterByPurpose(List<E> transitions, String purpose) {
		if (transitions == null || transitions.isEmpty()) {
			return CollectionUtils.emptyList();
		}
		List<E> result = new ArrayList<>(transitions.size());
		for (E transitionDefinition : transitions) {
			if (EqualsHelper.nullSafeEquals(purpose, transitionDefinition.getPurpose(), false)) {
				result.add(transitionDefinition);
			}
		}
		return result;
	}

	/**
	 * Collect definition fields.
	 *
	 * @param model
	 *            the model
	 * @return the collection
	 */
	public static Collection<PropertyDefinition> collectFields(DefinitionModel model) {
		return model.fieldsStream().collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * Sort definitions.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the definitions
	 * @param sorter
	 *            the sorter to use
	 * @return the map
	 */
	public static <E> Map<String, String> sortDefinitionsUsingSorter(Map<String, E> definitions,
			DefinitionSorterCallback<E> sorter) {
		LOGGER.debug("Started sorting using {} of items: {}", sorter, definitions.keySet());
		Map<String, String> unresolved = new HashMap<>();

		Map<String, E> result = new LinkedHashMap<>();
		for (Entry<String, E> entry : definitions.entrySet()) {
			LOGGER.trace("Processing item {}", entry.getKey());
			if (sorter.getParentDefinitionId(entry.getValue()) == null) {
				LOGGER.trace("Item {} does not have parent. Added to result.", entry.getKey());
				result.put(entry.getKey(), entry.getValue());
			} else {
				sortWithParent(definitions, sorter, unresolved, result, entry);
			}
		}
		// updating result
		definitions.clear();
		definitions.putAll(result);
		return unresolved;
	}

	/**
	 * Sort with parent.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the definitions
	 * @param sorter
	 *            the sorter
	 * @param unresolved
	 *            the unresolved
	 * @param result
	 *            the result
	 * @param entry
	 *            the entry
	 */
	private static <E> void sortWithParent(Map<String, E> definitions, DefinitionSorterCallback<E> sorter,
			Map<String, String> unresolved, Map<String, E> result, Entry<String, E> entry) {
		LOGGER.trace("Item {} have parent {}", entry.getKey(), sorter.createParentDefinitionId(entry.getValue()));
		if (EqualsHelper.nullSafeEquals(sorter.getId(entry.getValue()), sorter.getParentDefinitionId(entry.getValue()),
				true)) {
			ValidationLoggingUtil.addErrorMessage("An item cannot reference to itself via parentId. Item ID="
					+ sorter.createParentDefinitionId(entry.getValue()));
			unresolved.put(sorter.getId(entry.getValue()), sorter.getId(entry.getValue()));
			return;
		}
		if (!result.containsKey(entry.getKey())) {
			Map<String, E> defTree = createTreeHierarchy(definitions, sorter, unresolved, entry);

			// here if we have more then 2 elements we need to
			// prepare them for the second pass
			if (!defTree.isEmpty()) {
				List<String> keys = new ArrayList<>(defTree.keySet());
				LOGGER.trace("Hierarchy need sorting and second time processing. Updating second pass list with {}",
						keys);
				// reverse the order and add them for second pass
				Collections.reverse(keys);
				for (String key : keys) {
					result.put(key, defTree.get(key));
				}
			} else {
				LOGGER.trace("No valid parents found for item {}", entry.getKey());
			}
		} else {
			LOGGER.trace("Item {} already processed.", entry.getKey());
		}
	}

	/**
	 * Creates the tree hierarchy.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the definitions
	 * @param sorter
	 *            the sorter
	 * @param unresolved
	 *            the unresolved
	 * @param entry
	 *            the entry
	 * @return the hierarchy map
	 */
	private static <E> Map<String, E> createTreeHierarchy(Map<String, E> definitions,
			DefinitionSorterCallback<E> sorter, Map<String, String> unresolved, Entry<String, E> entry) {
		E e = entry.getValue();

		Map<String, E> defTree = new LinkedHashMap<>();
		defTree.put(sorter.createDefinitionId(e), e);

		while (sorter.getParentDefinitionId(e) != null) {
			String uniqueParentDef = sorter.createParentDefinitionId(e);
			LOGGER.trace("Checking parent {}", uniqueParentDef);
			if (!definitions.containsKey(uniqueParentDef)) {
				// error - no parent definition for definition 'e'
				unresolved.put(sorter.createDefinitionId(e), uniqueParentDef);
				LOGGER.trace("Parent item not found {} added to unresoled list.", uniqueParentDef);
				break;
			}
			E parent = definitions.get(uniqueParentDef);
			LOGGER.trace("Added parent for sorting {}", sorter.createDefinitionId(parent));
			defTree.put(sorter.createDefinitionId(parent), parent);
			e = parent;
		}
		return defTree;
	}

	/**
	 * Sorter for definition fields and regions.
	 *
	 * @param fields
	 *            definition fields
	 * @param regions
	 *            definition regions
	 * @return sorted fields and regions
	 */
	public static List<Ordinal> sortRegionsAndFields(List<PropertyDefinition> fields, List<RegionDefinition> regions) {
		List<Ordinal> sortables = new ArrayList<>(fields.size() + regions.size());
		sortables.addAll(fields);
		sortables.addAll(regions);
		sort(sortables);
		return sortables;
	}

	/**
	 * Sorts the list of the given ordinal elements. The sorting is more stable if the elements provide additional means
	 * of sorting. Note that the list is modified and is not applicable for immutable definition objects
	 *
	 * @param sortables
	 *            the sortables
	 * @see SortableComparator
	 */
	public static void sortExtended(List<? extends Ordinal> sortables) {
		Collections.sort(sortables, EXTENDED_SORTABLE_COMPARATOR);
	}

	/**
	 * Sorts the list of the given ordinal elements. Note that the list is modified and is not applicable for immutable
	 * definition objects
	 *
	 * @param sortables
	 *            the sortables
	 */
	public static void sort(List<? extends Ordinal> sortables) {
		Collections.sort(sortables, BASE_SORTABLE_COMPARATOR);
	}

	/**
	 * Sorts the list of the given ordinal elements by first creating a copy and then returning the copy. Note that the
	 * list is NOT modified.
	 *
	 * @param <E>
	 *            the element type
	 * @param sortables
	 *            the sortables
	 * @return the sorted
	 */
	public static <E extends Ordinal> List<E> getSorted(List<E> sortables) {
		List<E> copy = new ArrayList<>(sortables);
		sort(copy);
		return copy;
	}

	/**
	 * Gets the field names that are used in the provided expression
	 *
	 * @param expression
	 *            the expression
	 * @return the rnc fields
	 */
	public static Set<String> getRncFields(String expression) {
		if (org.apache.commons.lang3.StringUtils.isBlank(expression)) {
			return Collections.emptySet();
		}
		Set<String> fields = new LinkedHashSet<>();
		Matcher matcher = RNC_FIELD_PATTERN.matcher(expression);
		while (matcher.find()) {
			fields.add(matcher.group(1));
		}
		return fields;
	}

}
