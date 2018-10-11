package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;

/**
 * Expression evaluator manager. Entry point for expression evaluation.
 */
public interface ExpressionsManager {

	/**
	 * Gets an evaluator that can handle the given expression. The evaluator is searched by the given expression id and
	 * expression. If no id is passed it will be searched in all evaluators.
	 *
	 * @param expressionId
	 *            the expression id
	 * @param expression
	 *            the expression
	 * @return the evaluator or <code>null</code> if no evaluators found or there is no evaluator that can handle the
	 *         request.
	 */
	ExpressionEvaluator getEvaluator(String expressionId, String expression);

	/**
	 * Gets an evaluator that can handle the given expression.
	 *
	 * @param expression
	 *            the expression
	 * @return the evaluator or <code>null</code> if no evaluators found or there is no evaluator that can handle the
	 *         request.
	 */
	ExpressionEvaluator getEvaluator(String expression);

	/**
	 * Evaluate the default value from the given property definition.
	 *
	 * @param definition
	 *            the definition
	 * @return the serializable or <code>null</code>
	 */
	Serializable evaluate(PropertyDefinition definition);

	/**
	 * Evaluate the default value from the given property definition.
	 *
	 * @param definition
	 *            the definition
	 * @param context
	 *            the context
	 * @return the serializable or <code>null</code>
	 */
	Serializable evaluate(PropertyDefinition definition, ExpressionContext context);

	/**
	 * Evaluates the given collection of property definitions for their default values.
	 *
	 * @param definitions
	 *            the definitions to evaluate
	 * @param context
	 *            the context to use
	 * @return a mapping of evaluated results
	 */
	Map<String, Serializable> evaluate(Collection<PropertyDefinition> definitions, ExpressionContext context);

	/**
	 * Evaluates the rules from the given list of properties using a common context. All additional objects that should
	 * be passes after the list.
	 *
	 * @param definitions
	 *            the definitions
	 * @param allowNull
	 *            allow null values in return map
	 * @param target
	 *            the target
	 * @return the map
	 */
	Map<String, Serializable> evaluateRules(Set<PropertyDefinition> definitions, boolean allowNull,
			Serializable... target);

	/**
	 * Evaluates the rules from the given list of properties using a common context. All additional objects that should
	 * be passes after the list.
	 *
	 * @param definitions
	 *            the definitions
	 * @param context
	 *            the context
	 * @param allowNull
	 *            allow null values in return map
	 * @param target
	 *            the target
	 * @return the map
	 */
	Map<String, Serializable> evaluateRules(Set<PropertyDefinition> definitions, ExpressionContext context,
			boolean allowNull, Serializable... target);

	/**
	 * Evaluates the rule from the given {@link PropertyDefinition} using a common context. All additional objects that
	 * should be passes after the definition.
	 *
	 * @param propertyDefinition
	 *            the property definition
	 * @param context
	 *            the context to use
	 * @param target
	 *            the target
	 * @return the map
	 */
	Serializable evaluateRule(PropertyDefinition propertyDefinition, ExpressionContext context, Serializable... target);

	/**
	 * Evaluates the rule from the given expression using a common context. All additional objects that should be passes
	 * after the return type class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param expression
	 *            the expression
	 * @param returnType
	 *            the return type
	 * @param target
	 *            the target
	 * @return the map
	 */
	<T extends Serializable> T evaluateRule(String expression, Class<T> returnType, Serializable... target);

	/**
	 * Evaluates the rule from the given expression using a common context. All additional objects that should be passes
	 * after the return type class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param expression
	 *            the expression
	 * @param returnType
	 *            the return type
	 * @param context
	 *            the context to use
	 * @param target
	 *            the target
	 * @return the map
	 */
	<T extends Serializable> T evaluateRule(String expression, Class<T> returnType, ExpressionContext context,
			Serializable... target);

	/**
	 * Evaluates the given expression and converts to the target class if possible.
	 *
	 * @param <T>
	 *            the expected result type
	 * @param expression
	 *            the source expression
	 * @param target
	 *            the class of the expected result type
	 * @return the evaluated object or <code>null</code>
	 */
	<T> T evaluate(String expression, Class<T> target);

	/**
	 * Rule convert using the 'to' rule if any. The rule is searched in the rnc field of the definition
	 *
	 * @param definition
	 *            the definition
	 * @param value
	 *            the value
	 * @return the serializable
	 */
	Serializable ruleConvertTo(PropertyDefinition definition, Serializable value);

	/**
	 * Rule convert using the 'from' rule if any. The rule is searched in the rnc field of the definition
	 *
	 * @param definition
	 *            the definition
	 * @param value
	 *            the value
	 * @return the serializable
	 */
	Serializable ruleConvertFrom(PropertyDefinition definition, Serializable value);

	/**
	 * Creates the default execution context. The context is populated with the given target/current instance if any and
	 * the current field that contains the expression if any. Also is populated the current user/language and the
	 * additional properties are added.
	 *
	 * @param target
	 *            the target
	 * @param definition
	 *            the definition
	 * @param additionalProperties
	 *            the additional properties
	 * @return the expression context
	 */
	ExpressionContext createDefaultContext(Instance target, PropertyDefinition definition,
			Map<String, Serializable> additionalProperties);

	/**
	 * Checks if is expression.
	 *
	 * @param value
	 *            the value
	 * @return true, if is expression
	 */
	boolean isExpression(String value);
}