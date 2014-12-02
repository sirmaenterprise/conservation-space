/*
 *
 */
package com.sirma.itt.cmf.test.mock;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterImpl;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionContextProperties;
import com.sirma.itt.emf.evaluation.ExpressionEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionEvaluatorManager;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;

/**
 * Provides easy access to all evaluators.
 *
 * @author bbanchev
 */
public class EvaluatorManagerMock extends ExpressionEvaluatorManager {

	/**
	 * The expression prefix used to mark the beginning of expression string. The prefix should be
	 * the first character in the expression string
	 */
	public static final String EXPRESSION_PREFIX = "$";

	/** The evaluators. */
	private Instance<ExpressionEvaluator> evaluators;

	/** The type converter. */
	/** The type converter. */
	private TypeConverter typeConverter = getConverter();


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
	 * Gets the converter.
	 *
	 * @return the converter
	 */
	private TypeConverter getConverter() {
		try {
			Field declaredField = TypeConverterUtil.class.getDeclaredField("typeConverter");
			declaredField.setAccessible(true);
			declaredField.set(null, new TypeConverterImpl());
			declaredField.setAccessible(false);
			return TypeConverterUtil.getConverter();
		} catch (Exception e) {
			e.printStackTrace();
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

	/**
	 * {@inheritDoc}
	 */
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
		Map<String, Serializable> mapping = new LinkedHashMap<String, Serializable>(
				(int) (definitions.size() * 1.1), 0.95f);

		for (PropertyDefinition propertyDefinition : definitions) {
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) propertyDefinition);
			ExpressionEvaluator evaluator = getEvaluator(propertyDefinition.getRnc());
			if (evaluator != null) {
				Serializable serializable = evaluator.evaluate(propertyDefinition.getRnc(),
						context, target);
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
			Serializable convert = (Serializable) typeConverter.convert(
					propertyDefinition.getDataType(), serializable);
			return convert;
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
			T convert = typeConverter.convert(returnType, serializable);
			return convert;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExpressionContext createDefaultContext(com.sirma.itt.emf.instance.model.Instance target,
			PropertyDefinition definition, Map<String, Serializable> additionalProperties) {
		ExpressionContext context = new ExpressionContext();
		if (target != null) {
			context.put(ExpressionContextProperties.CURRENT_INSTANCE, target);
		}
		if (definition != null) {
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) definition);
		}
		if ((additionalProperties != null) && additionalProperties.isEmpty()) {
			context.putAll(additionalProperties);
		}
		User user = SecurityContextManager.getFullAuthentication();
		if (user == null) {
			user = SecurityContextManager.getAdminUser();
		}
		context.put(ExpressionContextProperties.CURRENT_USER, user);
		context.put(ExpressionContextProperties.LANGUAGE, user.getLanguage());
		return context;
	}
}
