package com.sirma.itt.seip.instance.script;

import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.BehaviorControl;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Script evaluator that can run scripts located in transition definitions.
 *
 * @author BBonev
 */
// TODO Rename me and fix my comments. Also take care of my implementations.
public interface TransitionScriptEvaluator {

	/**
	 * Execute on workflow end. The method finds a transition definition based on the given operation id and if so try
	 * to execute the script attached to it if any.
	 *
	 * @param instance
	 *            the instance
	 * @param transition
	 *            the transition
	 * @param isBefore
	 *            the is before
	 * @param context
	 *            the context parameters valid for this transaction.
	 */
	void executeScriptsForTransition(Instance instance, String transition, boolean isBefore, Map<String, Object> context);

	/**
	 * Execute script that are defined in the given {@link TransitionDefinition} object.
	 *
	 * @param target
	 *            the target
	 * @param definition
	 *            the definition
	 * @param isBefore
	 *            if the event is before the operation
	 * @param context
	 *            the context parameters valid for this transaction.
	 */
	void executeScriptsForTransition(Instance target, TransitionDefinition definition, boolean isBefore, Map<String, Object> context);

	/**
	 * Evaluate conditions for the given instance
	 *
	 * @param control
	 *            the control
	 * @param target
	 *            the target
	 * @param transitionDefinition
	 *            the transition definition
	 * @param context
	 *            the context parameters valid for this transaction.
	 */
	void evaluateConditionsFor(BehaviorControl control, Instance target, TransitionDefinition transitionDefinition, Map<String, Object> context);

	/**
	 * Evaluate script that is defined in the given transition for execution on.
	 *
	 * @param target
	 *            the target
	 * @param transitionDefinition
	 *            the transition definition
	 * @param context
	 *            the context parameters valid for this transaction.
	 * @return the result from the script evaluation or <code>null</code>
	 */
	Object evaluateOnTransitionScript(Instance target, DefinitionModel transitionDefinition, Map<String, Object> context);

	/**
	 * Find filed by name and evaluate the script inside.
	 *
	 * @param target
	 *            target instance
	 * @param definition
	 *            definition model
	 * @param fieldName
	 *            field name for inspection
	 * @param context
	 *            the context parameters valid for this transaction.
	 * @return result from executed script
	 */
	Object evaluateScriptInsideControl(Instance target, DefinitionModel definition, String fieldName, Map<String, Object> context);

	/**
	 * Evaluate the scripts for given transition. The method will find the fields inside transition with identifier
	 * {@link TransitionScriptType#TRANSITION_CONDITION} and execute the script.
	 *
	 * @param target
	 *            the target instance
	 * @param transitionDefinition
	 *            the transition definition
	 * @param context
	 *            the context parameters valid for this transaction.
	 * @return results from executed scripts
	 */
	List<Object> executeScriptsForTransition(Instance target, TransitionDefinition transitionDefinition, Map<String, Object> context);

}