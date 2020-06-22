package com.sirma.itt.seip.runtime;

import java.util.Collection;

import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Event fired before the processing the component instances for a phase. An observer could add or remove components
 * from the list so that they can be executed in the processed phase. Observer could remove elements using the iterator
 * returned from {@link #getComponents()} and add via the method {@link #addComponent(StartupComponent)}. Note that cannot add
 * component for a phase different of {@link #getPhase()}.
 *
 * @author BBonev
 */
public class BeforePhaseStartEvent {

	/** The components. */
	private final Collection<StartupComponent> components;
	private final StartupPhase phase;

	/**
	 * Instantiates a new before phase start event.
	 *
	 * @param phase
	 *            the phase
	 * @param components
	 *            the components
	 */
	public BeforePhaseStartEvent(StartupPhase phase, Collection<StartupComponent> components) {
		this.phase = phase;
		this.components = components;
	}

	/**
	 * Adds the component.
	 *
	 * @param component
	 *            the component
	 */
	public void addComponent(StartupComponent component) {
		if (component != null) {
			if (component.getPhase() != getPhase()) {
				throw new ComponentValidationException("Cannot add component " + component.getName() + " with phase "
						+ component.getPhase() + " while processing " + getPhase());
			}
			components.add(component);
		}
	}

	/**
	 * Provides access to the list of components that will be executed in the current phase.
	 *
	 * @return the components
	 */
	public Collection<StartupComponent> getComponents() {
		return components;
	}

	/**
	 * Getter method for phase.
	 *
	 * @return the phase
	 */
	public StartupPhase getPhase() {
		return phase;
	}

}