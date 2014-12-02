package com.sirma.itt.emf.script;

import java.util.Map;

import com.sirma.itt.emf.plugin.Plugin;

/**
 * Extension point used to initialize the {@link ScriptEvaluator} engines. The modules could provide
 * some default bindings of services on engine creation that will be available during the script
 * executions.
 * 
 * @author BBonev
 */
public interface GlobalBindingsExtension extends Plugin {

	/** The target name. */
	String TARGET_NAME = "globalBindingsExtension";

	/**
	 * Gets the bindings to be added to the global bindings.
	 * 
	 * @return the bindings
	 */
	public Map<String, Object> getBindings();
}
