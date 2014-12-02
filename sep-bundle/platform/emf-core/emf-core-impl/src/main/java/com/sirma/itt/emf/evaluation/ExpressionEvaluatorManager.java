package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;

/**
 * Provides easy access to all evaluators.
 *
 * @author BBonev
 */
public class ExpressionEvaluatorManager implements ExpressionsManager {

	/**
	 * The expression prefix used to mark the beginning of expression string.
	 * The prefix should be the first character in the expression string
	 */
	public static final String EXPRESSION_PREFIX = "$";

	/** The evaluators. */
	@Inject
	@Any
	private Instance<ExpressionEvaluator> evaluators;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/**
	* {@inheritDoc}
	*/
	@Override
	public ExpressionEvaluator getEvaluator(String expression) {
		if (StringUtils.isNullOrEmpty(expression)) {
			return null;
		}
		if (evaluators.isUnsatisfied()) {
			return null;
		}
		for (ExpressionEvaluator evaluator : evaluators) {
			if (evaluator.canHandle(expression)) {
				return evaluator;
			}
		}
		return null;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public Serializable evaluate(PropertyDefinition definition) {
		return evaluate(definition, createDefaultContext(null, definition, null));
	}

	@Override
	public Serializable evaluate(PropertyDefinition definition, ExpressionContext context) {
		if ((definition == null) || StringUtils.isNullOrEmpty(definition.getDefaultValue())) {
			return null;
		}
		ExpressionEvaluator evaluator = getEvaluator(definition.getDefaultValue());
		if (evaluator != null) {
			return (Serializable) typeConverter.convert(definition.getDataType(),
					evaluator.evaluate(definition.getDefaultValue(), context));
		}
		return (Serializable) typeConverter.convert(definition.getDataType(),
				definition.getDefaultValue());
	}

	@Override
	public Map<String, Serializable> evaluate(Collection<PropertyDefinition> definitions,
			ExpressionContext context) {
		if (definitions == null) {
			return null;
		}
		Map<String, Serializable> result = new LinkedHashMap<>(definitions.size());
		for (PropertyDefinition propertyDefinition : definitions) {
			Serializable value = evaluate(propertyDefinition, context);
			if (value != null) {
				result.put(propertyDefinition.getIdentifier(), value);
			}
		}
		return result;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public Map<String, Serializable> evaluateRules(Set<PropertyDefinition> definitions,
			boolean allowNull, Serializable... target) {
		return evaluateRules(definitions, createDefaultContext(null, null, null), allowNull, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> evaluateRules(Set<PropertyDefinition> definitions,
			ExpressionContext context, boolean allowNull, Serializable... target) {
		Map<String, Serializable> mapping = new LinkedHashMap<String, Serializable>((int) (definitions.size() * 1.1), 0.95f);

		for (PropertyDefinition propertyDefinition : definitions) {
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) propertyDefinition);
			ExpressionEvaluator evaluator = getEvaluator(propertyDefinition.getRnc());
			if (evaluator != null) {
				Serializable serializable = evaluator.evaluate(propertyDefinition.getRnc(), context, target);
				Serializable convert = (Serializable) typeConverter.convert(
						propertyDefinition.getDataType(), serializable);
				// does not save null fields
				if (allowNull || (convert != null)) {
					mapping.put(propertyDefinition.getName(), convert);
				}
			}
		}
		return mapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable evaluateRule(PropertyDefinition propertyDefinition,
			ExpressionContext context, Serializable... target) {
		String rnc = propertyDefinition.getRnc();
		if (StringUtils.isNullOrEmpty(rnc)) {
			return null;
		}
		ExpressionEvaluator evaluator = getEvaluator(rnc);
		if (evaluator != null) {
			Serializable serializable = evaluator.evaluate(propertyDefinition.getRnc(), context,
					target);
			return (Serializable) typeConverter.convert(propertyDefinition.getDataType(),
					serializable);
		}
		return null;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public <T extends Serializable> T evaluateRule(String expression, Class<T> returnType,
			Serializable... target) {
		return evaluateRule(expression, returnType, new ExpressionContext(), target);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public <T extends Serializable> T evaluateRule(String expression, Class<T> returnType,
			ExpressionContext context, Serializable... target) {
		ExpressionEvaluator evaluator = getEvaluator(expression);
		if (evaluator != null) {
			Serializable serializable = evaluator.evaluate(expression, context, target);
			return typeConverter.convert(returnType, serializable);
		}
		// if the expected type is the same as the input there is no reason to return null
		if (String.class.equals(returnType)) {
			return returnType.cast(expression);
		}
		return null;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public <T> T evaluate(String expression, Class<T> target) {
		if (StringUtils.isNullOrEmpty(expression)) {
			return null;
		}
		ExpressionEvaluator evaluator = getEvaluator(expression);
		if (evaluator != null) {
			return typeConverter.convert(target, evaluator.evaluate(expression));
		}
		return typeConverter.convert(target, expression);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public Serializable ruleConvertTo(PropertyDefinition definition, Serializable value) {
		if ((definition == null) || StringUtils.isNullOrEmpty(definition.getRnc())
				|| (value == null)) {
			return value;
		}
		String rules = definition.getRnc();
		String[] split = rules.split("\\|");
		return executeRule(split, "to.", value);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public Serializable ruleConvertFrom(PropertyDefinition definition, Serializable value) {
		if ((definition == null) || StringUtils.isNullOrEmpty(definition.getRnc())
				|| (value == null)) {
			return value;
		}
		String rule = definition.getRnc();
		String[] split = rule.split("\\|");
		return executeRule(split, "from.", value);
	}

	/**
	 * Execute rule.
	 *
	 * @param rules
	 *            the rules
	 * @param part
	 *            the part
	 * @param value
	 *            the value
	 * @return the serializable
	 */
	private Serializable executeRule(String[] rules, String part, Serializable value) {
		String rule = null;
		for (String string : rules) {
			if (string.contains(part)) {
				rule = string;
			}
		}
		if (rule == null) {
			return value;
		}

		ExpressionEvaluator evaluator = getEvaluator(rule);
		if (evaluator == null) {
			return value;
		}

		return evaluator.evaluate(rule, value);
	}

	@Override
	public ExpressionContext createDefaultContext(com.sirma.itt.emf.instance.model.Instance target,
			PropertyDefinition definition, Map<String, Serializable> additionalProperties) {
		// create proper size context where 4 is the number of fixed properties
		ExpressionContext context = new ExpressionContext(
				additionalProperties != null ? additionalProperties.size() + 4 : 4);

		if ((additionalProperties != null) && additionalProperties.isEmpty()) {
			context.putAll(additionalProperties);
		}
		if (target != null) {
			context.put(ExpressionContextProperties.CURRENT_INSTANCE, target);
		}
		if (definition != null) {
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) definition);
		}
		User user = SecurityContextManager.getFullAuthentication();
		if (user == null) {
			user = SecurityContextManager.getAdminUser();
		}
		context.put(ExpressionContextProperties.CURRENT_USER, user);
		context.put(ExpressionContextProperties.LANGUAGE,
				user.getProperties().get(ResourceProperties.LANGUAGE));
		return context;
	}
}
