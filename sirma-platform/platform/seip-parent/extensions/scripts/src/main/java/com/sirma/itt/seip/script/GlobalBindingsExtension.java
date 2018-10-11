package com.sirma.itt.seip.script;

import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point used to initialize the {@link ScriptEvaluator} engines. The modules could provide some default
 * bindings of services on engine creation that will be available during the script executions.
 *
 * @author BBonev
 */
public interface GlobalBindingsExtension extends Plugin {

	/** The target name. */
	String TARGET_NAME = "globalBindingsExtension";

	/**
	 * Gets the bindings to be added to the global bindings. The bindings could also be a script and could be
	 * dynamically loaded using loadScript([scriptName]).
	 *
	 * @return the bindings
	 */
	Map<String, Object> getBindings();

	/**
	 * The extension could provide some scripts to be preloaded the script engine and available to all scripts for
	 * using.
	 *
	 * @return a collection of scripts to be preloaded
	 */
	Collection<String> getScripts();
}
