package com.sirma.itt.seip.script;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.FixedSizeMap;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Metric.Builder;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Default implementation for the {@link ScriptEvaluator}. The implementation caches the compiled scripts using a digest
 * of the input script.
 *
 * @author BBonev
 */
@ApplicationScoped
class ScriptEvaluatorImpl implements ScriptEvaluator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptEvaluator.class);
	private static final Metric SCRIPT_EVAL_DURATION_SECONDS = Builder
			.timer("script_eval_duration_seconds", "Script evaluation duration in seconds").build();
	private static final Metric SCRIPT_FN_EVAL_DURATION_SECONDS = Builder
			.timer("script_fn_eval_duration_seconds", "Script function evaluation duration in seconds").build();
	/**
	 * Script engine to be used for executing custom server side scripts. <b>Default value: javascript</b>
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "script.engine.language", defaultValue = DEFAULT_LANGUAGE, sensitive = true, system = true, label = "Script engine to be used for executing custom server side scripts. <b>Default value: javascript</b>")
	private ConfigurationProperty<String> scriptEngineName;

	private static ThreadLocal<EngineData> engineCache = new ThreadLocal<>();

	/** The script engine manager. Used to produce script engines */
	@Inject
	private ScriptEngineManager scriptEngineManager;

	@Inject
	private javax.enterprise.inject.Instance<SchedulerService> schedulerService;

	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private Statistics stats;

	private ScriptEngine functionalEngine;

	/**
	 * Initialize script engine
	 */
	@PostConstruct
	public void initialize() {
		initializeEngine(() -> new EngineData(scriptEngineManager.getEngineByName(scriptEngineName.get())));
	}

	private static void initializeEngine(Supplier<EngineData> supplier) {
		engineCache = ThreadLocal.withInitial(supplier);
	}

	@Override
	public Object eval(String engine, String script, Map<String, Object> bindings) {
		return eval(script, bindings);
	}

	@Override
	public Object eval(String script, Map<String, Object> bindings) {
		if (script == null) {
			throw new com.sirma.itt.seip.script.ScriptException("Cannot execute null script");
		}

		try {
			stats.track(SCRIPT_EVAL_DURATION_SECONDS);
			if (isCompilationEnabled()) {
				return evalCompilableScript(script, bindings);
			}

			return evalNonCompilableScript(script, bindings);
		} finally {
			stats.end(SCRIPT_EVAL_DURATION_SECONDS);
		}
	}

	@Override
	public void scheduleEval(String script, Map<String, Object> bindings, boolean persistent, long delay,
			TimeUnit timeUnit) {
		if (script == null) {
			LOGGER.warn("Tried to schedule script execution of a null script");
			return;
		}
		long delayInMilliseconds = timeUnit == null ? 0 : timeUnit.toMillis(Math.max(delay, 0));
		long delayInHours = TimeUnit.MILLISECONDS.toHours(delayInMilliseconds);
		boolean isDelayMoreThanHour = delayInHours > 1;
		if (isDelayMoreThanHour && !persistent) {
			LOGGER.warn("Requested non persistent script execution with delay of {} hours. This is not stable use of "
					+ "non persistent async scripts so it will be executed persistently", delayInHours);
		}
		if (persistent || isDelayMoreThanHour) {
			scheduleScriptExecution(script, bindings, delayInMilliseconds);
		} else {
			// make sure there is a transaction when calling the internal method.
			// If there is no transaction then the async operation will be executed immediately after the next line
			// if there is a transaction then the async op will be executed at the end of that tx
			transactionSupport.invokeInTx(() -> {
				executeAsync(script, bindings, delayInMilliseconds);
				return null;
			});
		}
	}

	@Override
	public Predicate<Map<String, Object>> createScriptedPredicate(String script) {
		ScriptEngine engine = getFunctionalEngine();
		CompiledScript compiledScript = compile(script, engine);
		// create local copies of the engine and the script so they can be cached in the lambda bellow without a
		// reference to the current class
		return (Map<String, Object> args) -> {
			try {
				stats.track(SCRIPT_FN_EVAL_DURATION_SECONDS);
				Object result = compiledScript.eval(createBindings(engine, args));
				if (result instanceof Boolean) {
					return (Boolean) result;
				}
				throw new com.sirma.itt.seip.script.ScriptException(
						"The script was expected to return boolean value but got " + result);
			} catch (ScriptException e) {
				throw new com.sirma.itt.seip.script.ScriptException("Failed script execution", e);
			} finally {
				stats.end(SCRIPT_FN_EVAL_DURATION_SECONDS);
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> Function<Map<String, Object>, R> createScriptedFunction(String script) {
		// create local copies of the engine and the script so they can be cached in the lambda bellow without a
		// reference to the current class
		ScriptEngine engine = getFunctionalEngine();
		CompiledScript compiledScript = compile(script, engine);
		return (Map<String, Object> args) -> {
			try {
				stats.track(SCRIPT_FN_EVAL_DURATION_SECONDS);
				return (R) compiledScript.eval(createBindings(engine, args));
			} catch (ScriptException e) {
				throw new com.sirma.itt.seip.script.ScriptException("Failed script execution", e);
			} finally {
				stats.end(SCRIPT_FN_EVAL_DURATION_SECONDS);
			}
		};
	}

	private static Bindings createBindings(ScriptEngine engine, Map<String, Object> args) {
		Bindings bindings = engine.createBindings();
		if (args != null) {
			bindings.putAll(args);
		}
		return bindings;
	}

	private static CompiledScript compile(String script, ScriptEngine engine) {
		try {
			return ((Compilable) engine).compile(script);
		} catch (ScriptException e) {
			throw new com.sirma.itt.seip.script.ScriptException("Failed parsing script", e);
		}
	}

	private synchronized ScriptEngine getFunctionalEngine() {
		if (functionalEngine == null) {
			functionalEngine = scriptEngineManager.getEngineByName(scriptEngineName.get());
		}
		return functionalEngine;
	}

	/**
	 * Execute async using event API.
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @param delayInMilliseconds
	 *            the delay in milliseconds
	 */
	private void executeAsync(final String script, final Map<String, Object> bindings, long delayInMilliseconds) {
		TaskExecutor executor = taskExecutor;
		transactionSupport.invokeOnSuccessfulTransaction(() ->
				executor.executeAsync(() -> {
					if (delayInMilliseconds > 0) {
						LOGGER.debug("Will wait {} ms before execution of async script", delayInMilliseconds);
						try {
							Thread.sleep(delayInMilliseconds);
						} catch (Exception e) {
							LOGGER.warn("Interrupted delay for script execution. Script will not be executed");
							LOGGER.trace("", e);
							return;
						}
					}
					LOGGER.info("Started async script execution");
					eval(script, bindings);
				})
		);
	}

	/**
	 * Schedule script execution.
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @param delayInMilliseconds
	 *            the delay in milliseconds
	 */
	private void scheduleScriptExecution(String script, Map<String, Object> bindings, long delayInMilliseconds) {

		InstanceReference target = getTargetInstance(bindings);
		String id = null;
		if (target != null) {
			id = target.getId();
		}
		SchedulerService service = schedulerService.get();
		SchedulerConfiguration configuration = buildConfiguration(script, id, delayInMilliseconds, service);

		SchedulerContext context = buildContext(script, bindings);

		service.schedule(ScriptSchedulerExecutor.NAME, configuration, context);
	}

	/**
	 * Builds schedule context.
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @return the scheduler context
	 */
	private static SchedulerContext buildContext(String script, Map<String, Object> bindings) {
		SchedulerContext context = new SchedulerContext(2);
		context.put(ScriptSchedulerExecutor.PARAM_SCRIPT, script);

		if (bindings != null) {
			context.put(ScriptSchedulerExecutor.PARAM_BINDINGS, convertInstanceToReference(bindings));
		}
		return context;
	}

	/**
	 * Convert instance bindings to references.
	 *
	 * @param bindings
	 *            the bindings
	 * @return the serializable
	 */
	private static Serializable convertInstanceToReference(Map<String, Object> bindings) {
		Map<String, Object> copy = CollectionUtils.createHashMap(bindings.size());
		for (Entry<String, Object> entry : bindings.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof ScriptInstance) {
				value = ((ScriptInstance) value).getTarget().toReference();
			} else if (value instanceof Instance) {
				value = ((Instance) value).toReference();
			}
			copy.put(entry.getKey(), value);
		}

		return (Serializable) copy;
	}

	/**
	 * Gets the target instance.
	 *
	 * @param bindings
	 *            the bindings
	 * @return the target instance
	 */
	private static InstanceReference getTargetInstance(Map<String, Object> bindings) {
		InstanceReference target = null;
		if (bindings != null) {
			Object object = bindings.get("root");
			if (object instanceof ScriptInstance) {
				target = ((ScriptInstance) object).getTarget().toReference();
			} else if (object instanceof Instance) {
				target = ((Instance) object).toReference();
			} else if (object instanceof InstanceReference) {
				target = (InstanceReference) object;
			}
		}
		return target;
	}

	/**
	 * Builds the configuration.
	 *
	 * @param script
	 *            the script
	 * @param id
	 *            the id
	 * @param delayInMilliseconds
	 *            the delay in milliseconds
	 * @return the scheduler configuration
	 */
	private static SchedulerConfiguration buildConfiguration(String script, Serializable id, long delayInMilliseconds,
			SchedulerService service) {
		SchedulerConfiguration configuration = service.buildEmptyConfiguration(SchedulerEntryType.TIMED);
		configuration
				.setScheduleTime(new Date(System.currentTimeMillis() + delayInMilliseconds))
					.setPersistent(true)
					.setRemoveOnSuccess(true)
					.setTransactionMode(TransactionMode.REQUIRED)
					.setIdentifier(DigestUtils.md5Hex(script + id) + "_asyncScript");
		return configuration;
	}

	/**
	 * Checks if is compilation enabled for the script engine.
	 *
	 * @return true, if is compilation enabled
	 */
	@SuppressWarnings("static-method")
	protected boolean isCompilationEnabled() {
		return getScriptEngine() instanceof Compilable;
	}

	/**
	 * Reset the engine
	 */
	@SuppressWarnings("static-method")
	public void reset() {
		engineCache.remove();
	}

	/**
	 * Eval non compilable script.
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @return the object
	 */
	private static Object evalNonCompilableScript(String script, Map<String, Object> bindings) {
		Object scriptResult;
		ScriptEngine engine = getScriptEngine();
		try {
			setBindingToEngine(engine, bindings);
			// we does not pass the binding to the eval method because the other method breaks the
			// dynamic script importing
			scriptResult = engine.eval(script);
		} catch (ScriptException e) {
			throw new com.sirma.itt.seip.script.ScriptException("Failed executing script", e);
		} finally {
			resetEngineScope(engine);
		}
		return scriptResult;
	}

	/**
	 * Eval compilable script.
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @return the object
	 */
	private static Object evalCompilableScript(String script, Map<String, Object> bindings) {
		// this should be called before setBindingToEngine() !
		CompiledScript compiledScript = getOrCreateCompiledScript(script);

		Object scriptResult = null;
		ScriptEngine engine = getScriptEngine();
		try {
			setBindingToEngine(engine, bindings);
			// we does not pass the binding to the eval method because the other method breaks the
			// dynamic script importing
			scriptResult = compiledScript.eval();
		} catch (ClassCastException e) {
			LOGGER.warn("Failed to execute script due to incompatible script arguments", e);
			LOGGER.debug("Failing script is \n{}", script);
		} catch (ScriptException e) {
			throw new com.sirma.itt.seip.script.ScriptException("Failed executing compiled script", e);
		} finally {
			resetEngineScope(engine);
		}
		return scriptResult;
	}

	/**
	 * Gets the or create compiled script.
	 *
	 * @param script
	 *            the script
	 * @return the or create compiled script
	 */
	private static CompiledScript getOrCreateCompiledScript(String script) {
		return engineCache.get().compileAndStore(script);
	}

	/**
	 * Reset engine scope.
	 *
	 * @param scriptEngine
	 *            the script engine
	 */
	private static void resetEngineScope(ScriptEngine scriptEngine) {
		scriptEngine.setBindings(scriptEngine.createBindings(), ScriptContext.ENGINE_SCOPE);
	}

	/**
	 * Builds the bindings from the given map of arguments.
	 *
	 * @param engine
	 *            the engine
	 * @param bindings
	 *            the bindings
	 */
	private static void setBindingToEngine(ScriptEngine engine, Map<String, Object> bindings) {
		if (bindings != null) {
			Bindings engineScope = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			// if some value conversion should happen it should be here before initializing the
			// bindings
			engineScope.putAll(bindings);
		}
	}

	/**
	 * Get or create script engine.
	 *
	 * @return the scriptEngine
	 */
	private static ScriptEngine getScriptEngine() {
		return engineCache.get().getEngine();
	}

	/**
	 * Holder object for the engine data and the compiled scripts associated with the engine. The object is used for
	 * thread local store.
	 *
	 * @author BBonev
	 */
	private static class EngineData {

		/** The engine. */
		private ScriptEngine engine;
		/** The script cache. Stores compiled scripts by sha1 hash */
		private Map<String, CompiledScript> scriptCache = new FixedSizeMap<>(1024);

		/**
		 * Instantiates a new engine data.
		 *
		 * @param engine
		 *            the engine
		 */
		EngineData(ScriptEngine engine) {
			this.engine = engine;
		}

		CompiledScript compileAndStore(String script) {
			ScriptEngine scriptEngine = getEngine();
			return scriptCache.computeIfAbsent(DigestUtils.md5Hex(script), s -> compile(script, scriptEngine));
		}

		/**
		 * Getter method for engine.
		 *
		 * @return the engine
		 */
		public ScriptEngine getEngine() {
			return engine;
		}
	}
}
