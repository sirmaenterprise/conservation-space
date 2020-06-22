package com.sirma.itt.seip.runtime;

import java.util.Collection;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Component instance definition. This is common interface to defining startup component instances.
 * Each instance should point to a single unit that can be started. These components are loaded
 * during deployment and executed based on the order defined by {@link #getOrder()} and
 * {@link #getDependsOn()}.
 * <p>
 * Asynchronous components will be triggered for execution in order after all synchronous components
 * with lower order. This forces strong dependency between asynchronous and synchronous components
 * in a way that asynchronous can depend on synchronous components.
 * <p>
 * This is materialized {@link Startup} annotation.
 *
 * @author BBonev
 * @see Startup
 */
public interface StartupComponent extends Component {

	/**
	 * Defines the phase on which the component to be invoked.
	 *
	 * @return the phase
	 * @see StartupPhase
	 */
	StartupPhase getPhase();

	/**
	 * If component should be loaded asynchronously. Note that the loading will not advance to the
	 * next phase until all asynchronous components finish with their loading in the current phase.
	 *
	 * @return true, if is async
	 */
	boolean isAsync();

	/**
	 * Defines order in which to load the components. The order is per phase. Components with same
	 * order can be loaded as the implementation decides. The one with higher order will be executed
	 * after the one with lower.
	 *
	 * @return the order
	 */
	double getOrder();

	/**
	 * Should return a collection of component names that should be loaded before loading the
	 * current component.
	 *
	 * @return the dependent components.
	 */
	Collection<String> getDependsOn();
}