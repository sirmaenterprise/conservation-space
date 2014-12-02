package com.sirma.itt.cmf.workflow.observers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.sirma.itt.cmf.event.task.standalone.AfterStandaloneTaskTransitionEvent;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskTransitionEvent;
import com.sirma.itt.cmf.event.workflow.AfterWorkflowTransitionEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowTransitionEvent;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.script.ScriptEvaluator;
import com.sirma.itt.emf.script.ScriptException;
import com.sirma.itt.emf.script.ScriptNode;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Observer for executing any workflow and standalone script actions defined in the executed
 * transitions.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class AutomaticTransitionScriptHandler {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = org.slf4j.LoggerFactory
			.getLogger(AutomaticTransitionScriptHandler.class);

	/** The script evaluator. */
	@Inject
	private ScriptEvaluator scriptEvaluator;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Before transition.
	 *
	 * @param event
	 *            the event
	 */
	public void beforeTransition(@Observes BeforeWorkflowTransitionEvent event) {
		executeScriptIfAny(event.getTaskInstance(), event.getTransitionDefinition(), true);
	}

	/**
	 * After transition.
	 *
	 * @param event
	 *            the event
	 */
	public void afterTransition(@Observes AfterWorkflowTransitionEvent event) {
		executeScriptIfAny(event.getTaskInstance(), event.getTransitionDefinition(), false);
	}

	/**
	 * Before standalone task transition.
	 * 
	 * @param event
	 *            the event
	 */
	public void beforeStandaloneTaskTransition(@Observes BeforeStandaloneTaskTransitionEvent event) {
		executeScriptIfAny(event.getInstance(), event.getTransitionDefinition(), true);
	}

	/**
	 * After standalone task transition.
	 * 
	 * @param event
	 *            the event
	 */
	public void afterStandaloneTaskTransition(@Observes AfterStandaloneTaskTransitionEvent event) {
		executeScriptIfAny(event.getInstance(), event.getTransitionDefinition(), false);
	}

	/**
	 * Execute script if any.
	 * 
	 * @param target
	 *            the target
	 * @param definition
	 *            the definition
	 * @param isBefore
	 *            if the event is before the operation
	 */
	private void executeScriptIfAny(Instance target, TransitionDefinition definition,
			boolean isBefore) {
		if (definition == null) {
			return;
		}
		List<PropertyDefinition> scriptDefinitions = findScripts(definition);
		for (PropertyDefinition propertyDefinition : scriptDefinitions) {
			runScript(target, propertyDefinition, isBefore);
		}
	}

	/**
	 * Run script.
	 * 
	 * @param target
	 *            the target
	 * @param propertyDefinition
	 *            the property definition
	 * @param isBefore
	 *            if the event is before the operation
	 */
	private void runScript(Instance target, PropertyDefinition propertyDefinition, boolean isBefore) {
		String script = propertyDefinition.getDefaultValue();

		List<ControlParam> params = propertyDefinition.getControlDefinition().getControlParams();
		String language = null;
		Boolean phase = Boolean.FALSE;
		Boolean failOnError = Boolean.TRUE;
		Boolean async = Boolean.FALSE;
		for (ControlParam param : params) {
			if (StringUtils.isNullOrEmpty(param.getValue())) {
				continue;
			}
			if (EqualsHelper.nullSafeEquals(param.getName(), "language")) {
				language = param.getValue();
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "phase")) {
				phase = EqualsHelper.nullSafeEquals("before", param.getValue(), true);
				if (phase != isBefore) {
					// the execution phase does not match should not execute it now
					return;
				}
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "failOnError")) {
				failOnError = Boolean.valueOf(param.getValue());
			} else if (EqualsHelper.nullSafeEquals(param.getName(), "async")) {
				async = Boolean.valueOf(param.getValue());
			}
		}

		// if the phase was not defined use the default value
		if (phase != isBefore) {
			// the execution phase does not match should not execute it now
			return;
		}

		try {
			// TODO: add asynchronous execution
			if (async) {
				LOGGER.warn("Requested async script execution, which is not implemented now. Will run the script on the same thread.");
			}

			ScriptNode node = typeConverter.convert(ScriptNode.class, target);
			Map<String, Object> map = CollectionUtils.createHashMap(2);
			map.put("root", node);

			LOGGER.trace("Executing script\n{}", script);

			scriptEvaluator.eval(language, script, map);
		} catch (ScriptException e) {
			LOGGER.warn("Failed executing automatic script ", e);
			if (failOnError) {
				throw e;
			}
		}
	}

	/**
	 * Find fields that have script control definition and something in their value.
	 * 
	 * @param definition
	 *            the definition
	 * @return the list
	 */
	private List<PropertyDefinition> findScripts(TransitionDefinition definition) {
		List<PropertyDefinition> list = new LinkedList<PropertyDefinition>();
		for (PropertyDefinition propertyDefinition : definition.getFields()) {
			if ((propertyDefinition.getControlDefinition() != null)
					&& EqualsHelper.nullSafeEquals(propertyDefinition.getControlDefinition()
							.getIdentifier(), "SCRIPT")
					&& StringUtils.isNotNullOrEmpty(propertyDefinition.getDefaultValue())) {
				list.add(propertyDefinition);
			}
		}
		return list;
	}

}
