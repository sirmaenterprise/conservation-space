package com.sirma.itt.seip.script;

import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.provider.Provider;

/**
 * Script engine manager provider. The provider is responsible for initializing all global bindings and preloading the
 * provided scripts using the extension {@link GlobalBindingsExtension}.
 *
 * @author BBonev
 */
@ApplicationScoped
class ScriptEngineManagerProvider implements Provider<ScriptEngineManager> {
	/** The load script source */
	public static final String LOAD_JS = "function loadScript(name) { _e_ngine.eval(name); }"
			+ " function toJson(json) { return eval('(' + json + ')'); } ";

	/** The global bindings. */
	@Inject
	@ExtensionPoint(GlobalBindingsExtension.TARGET_NAME)
	private Iterable<GlobalBindingsExtension> globalBindings;

	/**
	 * Produces script engine instance initialized with the global bindings if any
	 *
	 * @return the script engine manager
	 */
	@Override
	@Produces
	public ScriptEngineManager provide() {
		return new ScriptEngineManager() {
			@Override
			public ScriptEngine getEngineByName(String shortName) {
				ScriptEngine engine = super.getEngineByName(shortName);
				// add custom logic to the created engine like global bindings
				loadExternalScripts(engine);
				copyEngineScopeToGlobal(engine);
				return engine;
			}
		};
	}

	/**
	 * Load external scripts.
	 *
	 * @param engine
	 *            the engine
	 */
	protected void loadExternalScripts(ScriptEngine engine) {
		Bindings b = engine.createBindings();
		// store the engine in the global bindings
		b.put("_e_ngine", engine);
		// add other global bindings if any
		setGlobalBindings(b);
		engine.setBindings(b, ScriptContext.GLOBAL_SCOPE);
		evalScript(LOAD_JS, engine);
		if (globalBindings == null) {
			return;
		}
		for (GlobalBindingsExtension extension : globalBindings) {
			Collection<String> scripts = extension.getScripts();
			if (scripts == null) {
				continue;
			}
			for (String script : scripts) {
				evalScript(script, engine);
			}
		}
	}

	/**
	 * Copy engine scope to global scope. This is required because all loaded scripts up until now are loaded into the
	 * engine scope
	 *
	 * @param engine
	 *            the engine
	 */
	private static void copyEngineScopeToGlobal(ScriptEngine engine) {
		ScriptContext context = engine.getContext();
		Bindings global = context.getBindings(ScriptContext.GLOBAL_SCOPE);
		global.putAll(context.getBindings(ScriptContext.ENGINE_SCOPE));
		context.setBindings(global, ScriptContext.GLOBAL_SCOPE);
		// reset the engine scope
		context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
	}

	/**
	 * Evaluate a passed script into the given engine.
	 *
	 * @param script
	 *            the script to evaluate
	 * @param engine
	 *            the engine to use
	 */
	private static void evalScript(String script, ScriptEngine engine) {
		try {
			engine.eval(script);
		} catch (javax.script.ScriptException e) {
			throw new ScriptException(e);
		}
	}

	/**
	 * Gets the global bindings to be set in the {@link ScriptEngineManager} used to produce the {@link ScriptEngine}s.
	 *
	 * @param bindings
	 *            the binding instance to populate
	 */
	protected void setGlobalBindings(Bindings bindings) {
		if (globalBindings == null) {
			return;
		}
		for (GlobalBindingsExtension extension : globalBindings) {
			Map<String, Object> map = extension.getBindings();
			if (map != null) {
				bindings.putAll(map);
			}
		}
	}

}
