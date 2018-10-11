/**
 *
 */
package com.sirma.itt.seip.script.extensions;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Script extension provider to provide other script executions from scripts. Allow asynchronous script execution.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 215)
public class ScriptEvaluatorBindingProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private ScriptEvaluator scriptEvaluator;
	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.singletonMap("script", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Creates the configuration.
	 *
	 * @return the script configuration
	 */
	public ScriptConfiguration createConfig() {
		return new ScriptConfiguration(this);
	}

	/**
	 * Execute asynchronous script using the given configuration.
	 *
	 * @param config
	 *            the config
	 * @return the object script result only if the script is executed in the current transaction synchronously
	 */
	public Object execute(ScriptConfiguration config) {
		if (config == null) {
			LOGGER.warn("Passed null config for async script execution! Request ignored!");
			return null;
		}
		if (StringUtils.isBlank(config.script)) {
			LOGGER.warn("No script is set for async execution! Request ignored!");
			return null;
		}

		if (config.async) {
			if (config.inCurrentTransaction) {
				executeAsyncInternal(scriptEvaluator, config);
			} else {
				ScriptEvaluator evaluator = scriptEvaluator;
				transactionSupport.invokeAfterTransactionCompletionInTx(() -> executeAsyncInternal(evaluator, config));
			}
			return null;
		}
		if (config.inCurrentTransaction) {
			return executeSyncInternal(scriptEvaluator, config);
		}
		ScriptEvaluator evaluator = scriptEvaluator;
		transactionSupport.invokeAfterTransactionCompletionInTx(() -> executeSyncInternal(evaluator, config));
		return null;
	}

	private static void executeAsyncInternal(ScriptEvaluator evaluator, ScriptConfiguration config) {
		evaluator.scheduleEval(config.script, config.bindings, config.persisted, config.delay, config.timeUnit);
	}

	private static Object executeSyncInternal(ScriptEvaluator evaluator, ScriptConfiguration config) {
		return evaluator.eval(config.script, config.bindings);
	}

	/**
	 * Configuration object for asynchronous script executions
	 *
	 * @author BBonev
	 */
	public static class ScriptConfiguration {

		boolean persisted = false;
		long delay = 0;
		TimeUnit timeUnit = TimeUnit.MILLISECONDS;
		Map<String, Object> bindings = new HashMap<>();
		boolean inCurrentTransaction = true;
		String script;
		boolean async = false;
		private final ScriptEvaluatorBindingProvider evaluator;

		/**
		 * Instantiates a new script configuration.
		 *
		 * @param evaluator
		 *            the evaluator
		 */
		ScriptConfiguration(ScriptEvaluatorBindingProvider evaluator) {
			this.evaluator = evaluator;
		}

		/**
		 * Async.
		 *
		 * @return the script configuration
		 */
		public ScriptConfiguration async() {
			async = true;
			return this;
		}

		/**
		 * Sync.
		 *
		 * @return the script configuration
		 */
		public ScriptConfiguration sync() {
			async = false;
			return this;
		}

		/**
		 * Persisted.
		 *
		 * @return the script configuration
		 */
		public ScriptConfiguration persisted() {
			persisted = true;
			return this;
		}

		/**
		 * Non persisted.
		 *
		 * @return the script configuration
		 */
		public ScriptConfiguration nonPersisted() {
			persisted = false;
			return this;
		}

		/**
		 * After.
		 *
		 * @param timeInMillies
		 *            the time in millies
		 * @return the script configuration
		 */
		public ScriptConfiguration after(long timeInMillies) {
			delay = timeInMillies;
			return this;
		}

		/**
		 * After.
		 *
		 * @param time
		 *            the time
		 * @param unit
		 *            the unit
		 * @return the script configuration
		 */
		public ScriptConfiguration after(long time, TimeUnit unit) {
			delay = time;
			timeUnit = unit;
			return this;
		}

		/**
		 * After.
		 *
		 * @param time
		 *            the delay time.
		 * @param unit
		 *            the time unit as string
		 * @return the script configuration
		 */
		public ScriptConfiguration after(long time, String unit) {
			delay = time;
			timeUnit = TimeUnit.valueOf(unit.toUpperCase());
			return this;
		}

		/**
		 * In current transaction.
		 *
		 * @return the script configuration
		 */
		public ScriptConfiguration inCurrentTransaction() {
			inCurrentTransaction = true;
			return this;
		}

		/**
		 * After current transaction.
		 *
		 * @return the script configuration
		 */
		public ScriptConfiguration afterCurrentTransaction() {
			inCurrentTransaction = false;
			return this;
		}

		/**
		 * Adds the binding.
		 *
		 * @param name
		 *            the name
		 * @param value
		 *            the value
		 * @return the script configuration
		 */
		public ScriptConfiguration addBinding(String name, Object value) {
			if (name != null) {
				bindings.put(name, value);
			}
			return this;
		}

		/**
		 * Adds the bindings.
		 *
		 * @param mapping
		 *            the mapping
		 * @return the script configuration
		 */
		public ScriptConfiguration addBindings(Map<String, Object> mapping) {
			if (mapping != null) {
				bindings.putAll(mapping);
			}
			return this;
		}

		/**
		 * Script.
		 *
		 * @param toExecute
		 *            the to execute
		 * @return the script configuration
		 */
		public ScriptConfiguration script(String toExecute) {
			script = toExecute;
			return this;
		}

		/**
		 * Run the configured script
		 */
		public void run() {
			evaluator.execute(this);
		}
	}
}
