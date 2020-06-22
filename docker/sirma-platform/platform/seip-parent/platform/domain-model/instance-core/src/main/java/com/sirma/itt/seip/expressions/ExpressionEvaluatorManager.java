package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.expressions.ElExpressionParser;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Provides easy access to all evaluators.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ExpressionEvaluatorManager implements ExpressionsManager {

	private static final String DEFAULT_EVAL_ID = "default";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Object SYNCHRONIZER = new Object();
	/**
	 * The expression prefix used to mark the beginning of expression string. The prefix should be the first character
	 * in the expression string
	 */
	public static final String EXPRESSION_PREFIX = "$";

	@Inject
	@Any
	private Instance<ExpressionEvaluator> evaluators;

	private Map<String, List<ExpressionEvaluator>> nameToEvaluatorMapping;

	// use cache not to iterate evaluators instance always that may generate multiple creational contexts
	private List<ExpressionEvaluator> evaluatorsCache;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private SecurityContext securityContext;

	@Override
	public ExpressionEvaluator getEvaluator(String expressionId, String expression) {
		if (StringUtils.isBlank(expression)) {
			LOGGER.trace("getEvaluator(String, String) - Requested evaluator for null expression");
			return null;
		}
		if (getEvaluatorMapping().isEmpty()) {
			LOGGER.trace("Evaluators mapping is empty");
			return null;
		}
		String id = expressionId;
		if (StringUtils.isBlank(id)) {
			id = DEFAULT_EVAL_ID;
		}

		List<ExpressionEvaluator> list = getEvaluatorMapping().get(id);
		if (list != null && !list.isEmpty()) {
			return getEvaluatorThatCanHandle(list, expression, () -> getEvaluator(expression));
		}
		LOGGER.trace("getEvaluator(String, String) - No evaluator found for id [{}] and expression [{}]", expressionId,
				expression);
		// not found by id try all of them to be sure
		return getEvaluator(expression);
	}

	/**
	 * Gets the evaluator mapping.
	 *
	 * @return the evaluator mapping
	 */
	@SuppressWarnings("findbugs:DC_DOUBLECHECK")
	private Map<String, List<ExpressionEvaluator>> getEvaluatorMapping() {
		if (nameToEvaluatorMapping == null) {
			synchronized (SYNCHRONIZER) {
				if (nameToEvaluatorMapping != null) {
					return nameToEvaluatorMapping;
				}
				LOGGER.trace("Populating key evaluators mapping");
				nameToEvaluatorMapping = new HashMap<>(64);
				initializeEvaluatorMapping(nameToEvaluatorMapping);
				LOGGER.trace("Initialized {} unique expression keys", nameToEvaluatorMapping.size());
			}
		}
		return nameToEvaluatorMapping;
	}

	/**
	 * Initialize evaluator mapping.
	 *
	 * @param target
	 *            the target
	 */
	private void initializeEvaluatorMapping(Map<String, List<ExpressionEvaluator>> target) {
		if (getEvaluators().isEmpty()) {
			LOGGER.trace("initializeEvaluatorMapping() - No evaluator found!");
			return;
		}
		for (ExpressionEvaluator evaluator : getEvaluators()) {
			String id = evaluator.getExpressionId();
			if (StringUtils.isBlank(id)) {
				id = DEFAULT_EVAL_ID;
			}
			CollectionUtils.addValueToMap(target, id, evaluator);
		}
	}

	@SuppressWarnings("findbugs:DC_DOUBLECHECK")
	private Collection<ExpressionEvaluator> getEvaluators() {
		if (evaluatorsCache == null) {
			synchronized (SYNCHRONIZER) {
				if (evaluatorsCache != null) {
					return evaluatorsCache;
				}
				List<ExpressionEvaluator> cache = new LinkedList<>();
				for (ExpressionEvaluator evaluator : evaluators) {
					cache.add(evaluator);
				}
				evaluatorsCache = new ArrayList<>(cache);
			}
		}
		return evaluatorsCache;
	}

	@Override
	public ExpressionEvaluator getEvaluator(String expression) {
		if (StringUtils.isBlank(expression)) {
			LOGGER.trace("getEvaluator(String) - Requested evaluator for null expression");
			return null;
		}
		if (getEvaluators().isEmpty()) {
			LOGGER.trace("getEvaluator() - No evaluator found!");
			return null;
		}
		return getEvaluatorThatCanHandle(getEvaluators(), expression, () -> null);
	}

	/**
	 * Gets the evaluator that can handle.
	 *
	 * @param evaluatorsToCheck
	 *            the evaluators to check
	 * @param expression
	 *            the expression
	 * @param ifNotFound
	 *            the if not found this supplier will be used to return a default value
	 * @return the evaluator that can handle
	 */
	private static ExpressionEvaluator getEvaluatorThatCanHandle(Collection<ExpressionEvaluator> evaluatorsToCheck,
			String expression, Supplier<ExpressionEvaluator> ifNotFound) {
		for (ExpressionEvaluator evaluator : evaluatorsToCheck) {
			if (evaluator.canHandle(expression)) {
				return evaluator;
			}
		}
		return ifNotFound.get();
	}

	@Override
	public Serializable evaluate(PropertyDefinition definition) {
		return evaluate(definition, createDefaultContext(null, definition, null));
	}

	@Override
	public Serializable evaluate(PropertyDefinition definition, ExpressionContext context) {
		if (definition == null || StringUtils.isBlank(definition.getDefaultValue())) {
			return null;
		}
		if (!ElExpressionParser.isExpression(definition.getDefaultValue())) {
			return (Serializable) typeConverter.convert(definition.getDataType().getJavaClass(),
					definition.getDefaultValue());
		}
		ExpressionEvaluator evaluator = getEvaluator(definition.getDefaultValue());
		if (evaluator != null) {
			return (Serializable) typeConverter.convert(definition.getDataType().getJavaClass(),
					evaluator.evaluate(definition.getDefaultValue(), context));
		}
		return (Serializable) typeConverter.convert(definition.getDataType().getJavaClass(),
				definition.getDefaultValue());
	}

	@Override
	public Map<String, Serializable> evaluate(Collection<PropertyDefinition> definitions, ExpressionContext context) {
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

	@Override
	public Map<String, Serializable> evaluateRules(Set<PropertyDefinition> definitions, boolean allowNull,
			Serializable... target) {
		return evaluateRules(definitions, createDefaultContext(null, null, null), allowNull, target);
	}

	@Override
	public Map<String, Serializable> evaluateRules(Set<PropertyDefinition> definitions, ExpressionContext context,
			boolean allowNull, Serializable... target) {
		Map<String, Serializable> mapping = new LinkedHashMap<>((int) (definitions.size() * 1.1), 0.95f);

		for (PropertyDefinition propertyDefinition : definitions) {
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) propertyDefinition);
			ExpressionEvaluator evaluator = getEvaluator(propertyDefinition.getRnc());
			if (evaluator != null) {
				Serializable serializable = evaluator.evaluate(propertyDefinition.getRnc(), context, target);
				Serializable convert = (Serializable) typeConverter
						.convert(propertyDefinition.getDataType().getJavaClass(), serializable);
				// does not save null fields
				if (allowNull || convert != null) {
					mapping.put(propertyDefinition.getName(), convert);
				}
			}
		}
		return mapping;
	}

	@Override
	public Serializable evaluateRule(PropertyDefinition propertyDefinition, ExpressionContext context,
			Serializable... target) {
		String rnc = propertyDefinition.getRnc();
		if (StringUtils.isBlank(rnc)) {
			return null;
		}
		ExpressionEvaluator evaluator = getEvaluator(rnc);
		if (evaluator != null) {
			Serializable serializable = evaluator.evaluate(propertyDefinition.getRnc(), context, target);
			return (Serializable) typeConverter.convert(propertyDefinition.getDataType().getJavaClass(), serializable);
		}
		return null;
	}

	@Override
	public <T extends Serializable> T evaluateRule(String expression, Class<T> returnType, Serializable... target) {
		return evaluateRule(expression, returnType, new ExpressionContext(), target);
	}

	@Override
	public <T extends Serializable> T evaluateRule(String expression, Class<T> returnType, ExpressionContext context,
			Serializable... target) {
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

	@Override
	public <T> T evaluate(String expression, Class<T> target) {
		if (StringUtils.isBlank(expression)) {
			return null;
		}
		ExpressionEvaluator evaluator = getEvaluator(expression);
		if (evaluator != null) {
			return typeConverter.convert(target, evaluator.evaluate(expression));
		}
		return typeConverter.convert(target, expression);
	}

	@Override
	public Serializable ruleConvertTo(PropertyDefinition definition, Serializable value) {
		if (definition == null || StringUtils.isBlank(definition.getRnc()) || value == null) {
			return value;
		}
		String rules = definition.getRnc();
		String[] split = rules.split("\\|");
		return executeRule(split, "to.", value);
	}

	@Override
	public Serializable ruleConvertFrom(PropertyDefinition definition, Serializable value) {
		if (definition == null || StringUtils.isBlank(definition.getRnc()) || value == null) {
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
	public ExpressionContext createDefaultContext(com.sirma.itt.seip.domain.instance.Instance target,
			PropertyDefinition definition, Map<String, Serializable> additionalProperties) {
		// create proper size context where 4 is the number of fixed properties
		ExpressionContext context = new ExpressionContext(
				additionalProperties != null ? additionalProperties.size() + 4 : 4);

		if (additionalProperties != null && additionalProperties.isEmpty()) {
			context.putAll(additionalProperties);
		}
		if (target != null) {
			context.put(ExpressionContextProperties.CURRENT_INSTANCE, target);
		}
		if (definition != null) {
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) definition);
		}
		User user = securityContext.getAuthenticated();
		context.put(ExpressionContextProperties.CURRENT_USER, user);
		context.put(ExpressionContextProperties.LANGUAGE, user.getLanguage());
		return context;
	}

	@Override
	public boolean isExpression(String value) {
		return ElExpressionParser.isExpression(value);
	}
}
