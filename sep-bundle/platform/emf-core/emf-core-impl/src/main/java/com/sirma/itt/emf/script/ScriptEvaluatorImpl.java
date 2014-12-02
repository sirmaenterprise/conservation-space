package com.sirma.itt.emf.script;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.collections.FixedSizeMap;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.DigestUtils;

/**
 * Default implementation for the {@link ScriptEvaluator}. The implementation caches the compiled
 * scripts using a digest of the input script.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class ScriptEvaluatorImpl implements ScriptEvaluator {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptEvaluator.class);
	/** The global bindings. */
	@Inject
	@ExtensionPoint(GlobalBindingsExtension.TARGET_NAME)
	private Iterable<GlobalBindingsExtension> globalBindings;

	/** The script engine name. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_SCRIPT_ENGINE, defaultValue = "javascript")
	private String scriptEngineName;

	/** The script cache. */
	private Map<String, CompiledScript> scriptCache;

	/** The script engine. */
	private ScriptEngine scriptEngine;

	/** The script engine manager. */
	private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

	/** The compilation enabled. */
	boolean compilationEnabled;

	/**
	 * Initialize the default script engine and global bindings.
	 */
	@PostConstruct
	public void initialize() {
		scriptEngineManager.setBindings(getGlobalBindings());
		scriptEngine = scriptEngineManager.getEngineByName(scriptEngineName);
		compilationEnabled = scriptEngine instanceof Compilable;
		scriptCache = new FixedSizeMap<>(1024);
	}

	/**
	 * Gets the global bindings to be set in the {@link ScriptEngineManager} used to produce the
	 * {@link ScriptEngine}s.
	 * 
	 * @return the global bindings
	 */
	protected Bindings getGlobalBindings() {
		SimpleBindings bindings = new SimpleBindings();
		for (GlobalBindingsExtension extension : globalBindings) {
			Map<String, Object> map = extension.getBindings();
			if (map != null) {
				bindings.putAll(map);
			}
		}
		return bindings;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object eval(String engine, String script, Map<String, Object> bindings) {
		return eval(script, bindings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object eval(String script, Map<String, Object> bindings) {
		if (script == null) {
			throw new com.sirma.itt.emf.script.ScriptException("Cannot execute null script");
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		Object scriptResult = null;
		if (compilationEnabled) {
			scriptResult = evalCompiledScript(script, bindings, tracker);
		} else {
			scriptResult = evalNonCompiledScript(script, bindings, tracker);
		}
		LOGGER.trace("Script execution took {} s", tracker.stopInSeconds());
		return scriptResult;
	}

	/**
	 * Eval non compiled script.
	 * 
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @param tracker
	 *            the tracker
	 * @return the object
	 */
	private Object evalNonCompiledScript(String script, Map<String, Object> bindings,
			TimeTracker tracker) {
		Object scriptResult = null;
		try {
			tracker.begin();
			scriptResult = scriptEngine.eval(script, buildBindings(bindings));
			LOGGER.trace("Script execution of a not compiled script took {} s",
					tracker.stopInSeconds());
		} catch (ScriptException e) {
			throw new com.sirma.itt.emf.script.ScriptException("Failed executing script", e);
		}
		return scriptResult;
	}

	/**
	 * Eval compiled script.
	 * 
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @param tracker
	 *            the tracker
	 * @return the object
	 */
	private Object evalCompiledScript(String script, Map<String, Object> bindings,
			TimeTracker tracker) {
		String digest = calculateDigest(script);
		CompiledScript compiledScript = scriptCache.get(digest);
		if (compiledScript == null) {
			try {
				tracker.begin();
				compiledScript = ((Compilable) scriptEngine).compile(script);
				LOGGER.trace("Script compiling took {} s", tracker.stopInSeconds());
				addToScriptCache(digest, compiledScript);
			} catch (ScriptException e) {
				throw new com.sirma.itt.emf.script.ScriptException("Failed parsing script", e);
			}
		}

		Object scriptResult = null;
		try {
			tracker.begin();
			scriptResult = compiledScript.eval(buildBindings(bindings));
			LOGGER.trace("Script execution of a compiled script took {} s",
					tracker.stopInSeconds());
		} catch (ScriptException e) {
			throw new com.sirma.itt.emf.script.ScriptException(
					"Failed executing compiled script", e);
		}
		return scriptResult;
	}

	/**
	 * Adds the to script cache.
	 * 
	 * @param digest
	 *            the digest
	 * @param compiledScript
	 *            the compiled script
	 */
	private void addToScriptCache(String digest, CompiledScript compiledScript) {
		scriptCache.put(digest, compiledScript);
	}

	/**
	 * Builds the bindings from the given map of arguments.
	 * 
	 * @param bindings
	 *            the bindings
	 * @return the bindings
	 */
	private Bindings buildBindings(Map<String, Object> bindings) {
		if (bindings == null) {
			return null;
		}
		Bindings result = scriptEngine.createBindings();
		// if some value conversion should happen it should be here before initializing the bindings
		result.putAll(bindings);
		return result;
	}

	/**
	 * Calculate digest for the given content.
	 * 
	 * @param content
	 *            the content
	 * @return the digest or <code>null</code>
	 */
	private String calculateDigest(String content) {
		return DigestUtils.calculateDigest(content);
	}

}
