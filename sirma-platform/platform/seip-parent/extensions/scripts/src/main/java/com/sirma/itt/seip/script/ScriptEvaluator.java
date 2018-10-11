package com.sirma.itt.seip.script;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Wrapper script evaluator for the JDK {@link javax.script.ScriptEngine}. For optimization purposes the implementation
 * could cache the compiled scripts if possible. The engines could be initialized with some default global bindings
 * using the extension point @{link GlobalBindingsExtension}.
 *
 * @author BBonev
 */
public interface ScriptEvaluator {
	String DEFAULT_LANGUAGE = "javascript";

	/**
	 * Evaluates the given script using the specified engine name.
	 *
	 * @param engine
	 *            the engine name to use for the script evaluation, if <code>null</code> the default engine will be
	 *            used.
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

	/**
	 * Schedule asynchronous script evaluation using the given bindings. Note that if the bindings contain an instance
	 * the instance will be converted to instance reference before scheduling and will be loaded again upon execution
	 * and converted to {@link ScriptInstance}.
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @param persistent
	 *            <code>true</code> if the execution should be persistent with retries
	 */
	default void scheduleEval(String script, Map<String, Object> bindings, boolean persistent) {
		scheduleEval(script, bindings, persistent, 0, TimeUnit.MILLISECONDS);
	}

	/**
	 * Schedule asynchronous script evaluation using the given bindings. Note that if the bindings contain an instance
	 * the instance will be converted to instance reference before scheduling and will be loaded again upon execution
	 * and converted to {@link ScriptInstance}.
	 * <p>
	 * The method allows the asynchronous execution to be delayed by the given duration. Note that there is a limitation
	 * of ONE DAY to the delay that can be passed for non persistent executions. If requested with delay of more than
	 * that a {@link ScriptException} will be thrown.
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @param persistent
	 *            <code>true</code> if the execution should be persistent with retries
	 * @param delay
	 *            the delay of the async execution
	 * @param timeUnit
	 *            the time unit of the delay
	 */
	void scheduleEval(String script, Map<String, Object> bindings, boolean persistent, long delay, TimeUnit timeUnit);

	/**
	 * Creates the predicate that executes the given script when passed a bindings context. Note that the script should
	 * return boolean value.
	 * <p>
	 * A hint for the implementation is to cache the compiled script instance in the created predicate.
	 *
	 * @param script
	 *            the script that need to be executed when the function is called
	 * @return the predicate that is backed by the given script.
	 */
	Predicate<Map<String, Object>> createScriptedPredicate(String script);

	/**
	 * Creates the function that executes the given script when passed a bindings context.
	 * <p>
	 * A hint for the implementation is to cache the compiled script instance in the created function.
	 *
	 * @param <R>
	 *            the expected result type from the script
	 * @param script
	 *            the script that need to be executed when the function is called
	 * @return the function that is backed by the given script
	 */
	<R> Function<Map<String, Object>, R> createScriptedFunction(String script);

}