package com.sirma.itt.seip.instance.state;

import java.util.List;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The {@link DynamicStateTransitionProvider} is extension to provide additional state transition based on runtime
 * calculations.
 *
 * @author bbanchev
 */
public interface DynamicStateTransitionProvider extends Named, Plugin {
	/** The plugin target. */
	String TARGET_NAME = "DynamicStateTransitionProvider";

	/**
	 * Provide a list of dynamic transition to be applied to a single instance.
	 * 
	 * @param instance
	 *            the instance to get transitions for
	 * @return a list of {@link StateTransition} or empty list of none are available;
	 */
	List<StateTransition> provide(Instance instance);
}
