package com.sirma.itt.seip.definition;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Definition model that defines a possible automatic state transitions
 *
 * @author BBonev
 */
public interface StateTransitionalModel extends Transitional {
	/**
	 * Gets the state transitions.
	 *
	 * @return the state transitions
	 */
	List<StateTransition> getStateTransitions();

	/**
	 * Returns a stream of the state transitions for the given definition model if the model implements the
	 * {@link StateTransitionalModel} interface otherwise empty stream will be returned.
	 *
	 * @param model
	 *            the model
	 * @return the state transitions stream
	 */
	static Stream<StateTransition> asStream(DefinitionModel model) {
		if (model instanceof StateTransitionalModel) {
			return ((StateTransitionalModel) model).getStateTransitions().stream();
		}
		return Stream.empty();
	}

	/**
	 * Find state transition in the given definition model. If the given model does not implement the
	 * {@link StateTransitionalModel} then the method does nothing.
	 *
	 * @param model
	 *            the model to look into for state transitions.
	 * @param filter
	 *            the filter to apply while searching for state transition
	 * @return the state transition or <code>null</code>
	 */
	static StateTransition findStateTransition(DefinitionModel model, Predicate<StateTransition> filter) {
		return asStream(model).filter(filter).findAny().orElse(null);
	}
}
