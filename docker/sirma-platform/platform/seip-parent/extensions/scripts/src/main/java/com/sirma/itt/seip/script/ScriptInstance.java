/**
 *
 */
package com.sirma.itt.seip.script;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Instance used in scripts. It should act as a proxy to actual represented instance.
 *
 * @author BBonev
 */
public interface ScriptInstance extends Instance {

	String SCRIPT_TYPE = "default";

	/**
	 * Gets the target instance
	 *
	 * @return the target
	 */
	Instance getTarget();

	/**
	 * Sets the target instance for this script instance.
	 *
	 * @param instance
	 *            the new target
	 * @return the script instance
	 */
	ScriptInstance setTarget(Instance instance);
}
