package com.sirma.itt.seip.instance.script;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.Transitional;
import com.sirma.itt.seip.domain.BehaviorControl;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.script.ScriptException;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Script evaluator that can run scripts located in transition definitions
 *
 * @author BBonev
 */
@ApplicationScoped
public class TransitionScriptEvaluatorImpl implements TransitionScriptEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String SCRIPT = "script";

	@Inject
	private ScriptEvaluator scriptEvaluator;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private DefinitionService definitionService;

	@Override
	public void executeScriptsForTransition(Instance instance, String transition, boolean isBefore,
			Map<String, Object> context) {
		DefinitionModel model = definitionService.getInstanceDefinition(instance);
		TransitionDefinition transitionDefinition = Transitional.findTransitionByName(model, transition);
		if (transitionDefinition == null) {
			return;
		}
		executeScriptsForTransition(instance, transitionDefinition, isBefore, context);
	}


	@Override
	public void executeScriptsForTransition(Instance target, TransitionDefinition definition, boolean isBefore,
			Map<String, Object> context) {
		if (definition == null) {
			return;
		}
		List<PropertyDefinition> scriptDefinitions = findScripts(definition);
		for (PropertyDefinition propertyDefinition : scriptDefinitions) {
			runScript(target, propertyDefinition, isBefore, TransitionScriptType.TRANSITION_ACTION, true, context);
		}
	}


	@Override
	public void evaluateConditionsFor(BehaviorControl control, Instance target,
			TransitionDefinition transitionDefinition, Map<String, Object> context) {
		if (control == null || transitionDefinition == null) {
			return;
		}

		List<PropertyDefinition> scriptDefinitions = findScripts(transitionDefinition);
		for (PropertyDefinition propertyDefinition : scriptDefinitions) {
			Object result = runScript(target,
					propertyDefinition,
					true,
					TransitionScriptType.TRANSITION_CONDITION,
					false,
					context);
			if (result instanceof String) {
				control.addDisabledReason((String) result);
			} else if (result != null) {
				LOGGER.warn("Conditional script returned non text result: {}", result);
			}
		}
	}

	@Override
	public Object evaluateOnTransitionScript(Instance target, DefinitionModel transitionDefinition,
			Map<String, Object> context) {
		if (transitionDefinition == null) {
			return null;
		}

		List<PropertyDefinition> scriptDefinitions = findScripts(transitionDefinition);
		if (scriptDefinitions.isEmpty()) {
			return null;
		}
		// execute only the first script we cannot execute multiple scripts and return the results
		return runScript(target,
				scriptDefinitions.get(0),
				true,
				TransitionScriptType.SCRIPT_WITH_RESULT,
				false,
				context);
	}

	@Override
	public List<Object> executeScriptsForTransition(Instance target, TransitionDefinition transitionDefinition,
			Map<String, Object> context) {
		// evaluate and collect the result
		return findScripts(transitionDefinition).stream()
				.map(definition -> runScript(target,
						definition,
						true,
						TransitionScriptType.TRANSITION_CONDITION,
						false,
						context))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private Object runScript(Instance target, PropertyDefinition propertyDefinition, boolean isBefore,
			TransitionScriptType transitionScriptType, boolean allowAsync, Map<String, Object> context) {
		String script = propertyDefinition.getDefaultValue();

		ExecutionConfiguration configuration = ExecutionConfiguration.parse(propertyDefinition);
		if (!configuration.isConfigurationApplicable(isBefore, transitionScriptType)) {
			// the execution phase does not match should not execute it now
			return null;
		}

		try {
			LOGGER.trace("Executing script\n{}", script);

			ScriptNode node = typeConverter.convert(ScriptNode.class, target);
			Map<String, Object> bindings = configuration.getBindings();
			Map<String, Object> map = CollectionUtils.createHashMap(bindings.size() + 2);
			map.putAll(bindings);
			map.put("root", node);
			map.put("context", context == null ? new HashMap<>() : context);

			if (allowAsync && configuration.isAsync()) {
				LOGGER.info("Scheduled async script execution!");
				scriptEvaluator.scheduleEval(script, map, configuration.isPersistent());
				return null;
			}

			return scriptEvaluator.eval(configuration.getLanguage(), script, map);
		} catch (ScriptException e) {
			if (configuration.shouldFailOnError()) {
				throw e;
			} else {
				LOGGER.warn("Failed executing automatic script ", e);
			}
		}
		return null;
	}

	@Override
	public Object evaluateScriptInsideControl(Instance target, DefinitionModel definition, String fieldName,
			Map<String, Object> context) {
		if (definition == null) {
			return null;
		}
		List<PropertyDefinition> scriptDefinitions = findScriptsInsideControl(definition, fieldName);
		if (scriptDefinitions.isEmpty()) {
			return null;
		}
		return runScript(target,
				scriptDefinitions.get(0),
				true,
				TransitionScriptType.SCRIPT_WITH_RESULT,
				false,
				context);

	}


	private static List<PropertyDefinition> findScriptsInsideControl(DefinitionModel definition, String propertyName) {
		return findDefinitionScripts(definition,
				PropertyDefinition.hasName(propertyName).and(PropertyDefinition.hasControl(SCRIPT)));
	}

	/**
	 * Find fields that have script control definition and something in their value.
	 */
	private static List<PropertyDefinition> findScripts(DefinitionModel definition) {
		return findDefinitionScripts(definition, PropertyDefinition.hasControl(SCRIPT));
	}

	private static List<PropertyDefinition> findDefinitionScripts(DefinitionModel definition,
			Predicate<PropertyDefinition> fieldFilter) {
		return definition.fieldsStream().filter(fieldFilter.and(PropertyDefinition.hasValue())).collect(
				Collectors.toCollection(LinkedList::new));
	}

	/**
	 * Execution script configuration
	 *
	 * @author BBonev
	 */
	private static class ExecutionConfiguration {
		private String language = ScriptEvaluator.DEFAULT_LANGUAGE;
		private Boolean beforePhase = Boolean.FALSE;
		private Boolean failOnError = Boolean.TRUE;
		private Boolean async = Boolean.FALSE;
		private Boolean persistent = Boolean.TRUE;
		private TransitionScriptType type = TransitionScriptType.TRANSITION_ACTION;
		private Map<String, Object> bindings = Collections.emptyMap();

		/**
		 * Parses the given parameters to build configuration object.
		 *
		 * @param propertyDefinition
		 *            property definition to parse
		 * @return the execution configuration
		 */
		public static ExecutionConfiguration parse(PropertyDefinition propertyDefinition) {
			ExecutionConfiguration configuration = new ExecutionConfiguration();
			if (propertyDefinition.getControlDefinition() == null) {
				return configuration;
			}

			propertyDefinition.getControlDefinition().getControlParams().stream()
					.filter(param -> StringUtils.isNotBlank(param.getValue()))
					.forEach(param -> setConfigurationParameter(configuration, param));
			return configuration;
		}

		private static void setConfigurationParameter(ExecutionConfiguration configuration,
				ControlParam param) {
			if (EqualsHelper.nullSafeEquals(param.getName(), "language")) {
				configuration.language = param.getValue();
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "phase")) {
				configuration.beforePhase = EqualsHelper.nullSafeEquals("before", param.getValue(), true);
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "failOnError")) {
				configuration.failOnError = Boolean.valueOf(param.getValue());
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "async")) {
				configuration.async = Boolean.valueOf(param.getValue());
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "persistent")) {
				configuration.persistent = Boolean.valueOf(param.getValue());
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "type")) {
				configuration.type = TransitionScriptType.valueOf(param.getValue().toUpperCase());
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "binding")) {
				if (configuration.bindings.isEmpty()) {
					configuration.bindings = new HashMap<>(8);
				}
				configuration.bindings.put(param.getIdentifier(), param.getValue());
			}
		}

		/**
		 * Checks if is persistent.
		 *
		 * @param isBefore
		 *            the is before
		 * @param transitionScriptType
		 *            the script type
		 * @return true, if is persistent
		 */
		public boolean isConfigurationApplicable(boolean isBefore, TransitionScriptType transitionScriptType) {
			if (transitionScriptType == getType()) {
				if (transitionScriptType == TransitionScriptType.TRANSITION_CONDITION
						|| transitionScriptType == TransitionScriptType.SCRIPT_WITH_RESULT) {
					return true;
				}
				return beforePhase == isBefore;
			}
			return false;
		}

		/**
		 * Checks if is async.
		 *
		 * @return true, if is async
		 */
		public boolean isAsync() {
			return async;
		}

		/**
		 * Checks if is persistent.
		 *
		 * @return true, if is persistent
		 */
		public boolean isPersistent() {
			return persistent;
		}

		public String getLanguage() {
			return language;
		}

		public boolean shouldFailOnError() {
			return failOnError;
		}

		public TransitionScriptType getType() {
			return type;
		}

		public Map<String, Object> getBindings() {
			return bindings;
		}
	}
}
