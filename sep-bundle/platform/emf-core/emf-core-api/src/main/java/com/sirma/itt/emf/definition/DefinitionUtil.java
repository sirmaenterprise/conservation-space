package com.sirma.itt.emf.definition;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Common definition utility methods.
 *
 * @author BBonev
 */
public class DefinitionUtil {

	/** The purpose of the transition definitions that is for actions. */
	public static final String TRANSITION_PERPOSE_ACTION = "action";

	/**
	 * Gets the default transitions. These are transitions that does not have a purpose. The method
	 * is equal to calling {@link #filterTransitionsByPurpose(Transitional, String)} with
	 * <code>null</code> second argument
	 *
	 * @param transitional
	 *            the transitional
	 * @return the default transitions, or empty list
	 */
	public static List<TransitionDefinition> getDefaultTransitions(Transitional transitional) {
		if ((transitional == null) || transitional.getTransitions().isEmpty()) {
			return CollectionUtils.emptyList();
		}
		return getDefaultTransitions(transitional.getTransitions());
	}

	/**
	 * Gets the default transitions.These are transitions that does not have a purpose. The method
	 * is equal to calling {@link #filterByPurpose(List, String)} with <code>null</code> second
	 * argument
	 *
	 * @param transitions
	 *            the transitions
	 * @return the default transitions, or empty list
	 */
	public static List<TransitionDefinition> getDefaultTransitions(
			List<TransitionDefinition> transitions) {
		if ((transitions == null) || transitions.isEmpty()) {
			return CollectionUtils.emptyList();
		}
		return filterByPurpose(transitions, null);
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
	public static List<TransitionDefinition> filterTransitionsByPurpose(Transitional transitional,
			String purpose) {
		if ((transitional == null) || transitional.getTransitions().isEmpty()) {
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
	public static <E extends Purposable> List<E> filterByPurpose(List<E> transitions,
			String purpose) {
		if ((transitions == null) || transitions.isEmpty()) {
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

}
