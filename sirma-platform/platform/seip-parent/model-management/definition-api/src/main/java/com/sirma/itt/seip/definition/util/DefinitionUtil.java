package com.sirma.itt.seip.definition.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.Transitional;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.util.SortableComparator;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Common definition utility methods.
 *
 * @author BBonev
 */
public class DefinitionUtil {

	private static final Pattern RNC_FIELD_PATTERN = Pattern.compile("(?<!\\d+)\\[([\\w:]+?)\\]", Pattern.CANON_EQ);

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
	 * Gets the default transitions. These are transitions that does not have a purpose.
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
			if (StringUtils.isNotBlank(transitionDefinition.getPurpose())) {
				result.add(transitionDefinition);
			}
		}
		return result;
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
