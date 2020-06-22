package com.sirma.itt.seip.definition;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Common interface for definitions with transitions.
 *
 * @author BBonev
 */
public interface Transitional extends Identity {

	/**
	 * Getter method for transitions.
	 *
	 * @return the transitions
	 */
	List<TransitionDefinition> getTransitions();

	/**
	 * Getter method for transition groups.
	 *
	 * @return list of transition groups
	 */
	List<TransitionGroupDefinition> getTransitionGroups();

	/**
	 * Gets the transition by name.
	 *
	 * @param name
	 *            the name
	 * @return the transition by name
	 */
	default Optional<TransitionDefinition> getTransitionByName(String name) {
		return asStream(this).filter(t -> nullSafeEquals(t.getIdentifier(), name)).findFirst();
	}

	/**
	 * Returns a stream of the transitions for the given definition model if the model implements the
	 * {@link Transitional} interface otherwise empty stream will be returned.
	 *
	 * @param model
	 *            the model
	 * @return the transitions stream
	 */
	static Stream<TransitionDefinition> asStream(Object model) {
		if (model instanceof Transitional) {
			return ((Transitional) model).getTransitions().stream();
		}
		return Stream.empty();
	}

	/**
	 * Find transition in the given model using the given filter to tests the transitions in the model. The method does
	 * anything only if the given model implements {@link Transitional}.
	 *
	 * @param model
	 *            the model
	 * @param filter
	 *            the filter
	 * @return the transition definition or <code>null</code> if not found
	 */
	static TransitionDefinition findTransition(DefinitionModel model, Predicate<TransitionDefinition> filter) {
		return asStream(model).filter(filter).findAny().orElse(null);
	}

	/**
	 * Find transition in the given model using the given filter to tests the transitions in the model. The method does
	 * anything only if the given model implements {@link Transitional}.
	 *
	 * @param model
	 *            the model
	 * @param name
	 *            the name of the transition
	 * @return the transition definition or <code>null</code> if not found
	 */
	static TransitionDefinition findTransitionByName(DefinitionModel model, String name) {
		return findTransition(model, t -> nullSafeEquals(t.getIdentifier(), name, true));
	}
}
