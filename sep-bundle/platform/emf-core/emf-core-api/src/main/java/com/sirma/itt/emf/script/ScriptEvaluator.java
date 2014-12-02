package com.sirma.itt.emf.script;

import java.util.Map;


/**
 * Wrapper script evaluator for the JDK {@link javax.script.ScriptEngine}. For optimization purposes
 * the implementation could cache the compiled scripts if possible. The engines could be initialized
 * with some default global bindings using the extension point @{link GlobalBindingsExtension}.
 * 
 * @author BBonev
 */
public interface ScriptEvaluator {

	/**
	 * Evaluates the given script using the specified engine name.
	 * 
	 * @param engine
	 *            the engine name to use for the script evaluation, if <code>null</code> the default
	 *            engine will be used.
	 * @param script
	 *            the script to evaluate
	 * @param bindings
	 *            the bindings optional bindings to pass for the script evaluation.
	 * @return the result of the script execution or <code>null</code> if nothing is returned.
	 * @throws ScriptException
	 */
	Object eval(String engine, String script, Map<String, Object> bindings);

	/**
	 * Evaluates the given script against the default script engine (JavaScript).
	 * 
	 * @param script
	 *            the script to evaluate
	 * @param bindings
	 *            the bindings optional bindings to pass for the script evaluation.
	 * @return the result of the script execution or <code>null</code> if nothing is returned.
	 * @throws ScriptException
	 */
	Object eval(String script, Map<String, Object> bindings);

}