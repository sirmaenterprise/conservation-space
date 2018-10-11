package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Evaluator that can fetch a property from current instance. The expression could have a second argument that is the
 * default value to return if the original resulted to <code>null</code> witch means it was missing or with
 * <code>null</code> value. <b>Note</b> the alternative value will always be of type string.
 *
 * @author BBonev
 */
@Singleton
public class PropertyExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 2239384584765053516L;
	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{get\\((\\[[\\w:]+\\])\\s*,?\\s*(?:cast\\((.+?)\\s*as\\s*(.+?)\\)|(.*?))\\)"
					+ FROM_PATTERN + MATCHES + "\\}");
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyExpressionEvaluator.class);
	private static final Map<String, Class<? extends Serializable>> TYPE_MAPPING;

	static {
		TYPE_MAPPING = CollectionUtils.createHashMap(10);
		TYPE_MAPPING.put("int", Integer.class);
		TYPE_MAPPING.put("integer", Integer.class);
		TYPE_MAPPING.put("long", Long.class);
		TYPE_MAPPING.put("float", Float.class);
		TYPE_MAPPING.put("double", Double.class);
		TYPE_MAPPING.put("decimal", Double.class);
		TYPE_MAPPING.put("date", Date.class);
		TYPE_MAPPING.put("datetime", Date.class);
		TYPE_MAPPING.put("time", Date.class);
		TYPE_MAPPING.put("string", String.class);
		TYPE_MAPPING.put("text", String.class);
	}

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "get";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String property = matcher.group(1);
		String castType = matcher.group(3);
		String defaultValue;
		if (StringUtils.isBlank(castType)) {
			defaultValue = matcher.group(4);
		} else {
			defaultValue = matcher.group(2);
		}
		if ("".equals(defaultValue)) {
			defaultValue = null;
		}

		String local = property;
		if (isPropertyKey(local)) {
			local = extractProperty(local);
		}

		Serializable serializable = getPropertyFrom(property, matcher, context, values);
		// does not escape header fields
		return validateAndReturn(serializable, defaultValue, matcher, castType,
				!DefaultProperties.DEFAULT_HEADERS.contains(local));
	}

	/**
	 * Validate and return.
	 *
	 * @param result
	 *            the result
	 * @param valueIfInvalid
	 *            the value if invalid
	 * @param matcher
	 *            the matcher
	 * @param castType
	 *            the cast type
	 * @param escape
	 *            the escape
	 * @return the serializable
	 */
	protected Serializable validateAndReturn(Serializable result, Serializable valueIfInvalid, Matcher matcher,
			String castType, boolean escape) {
		if (isResultValid(result, matcher)) {
			return escapeConditional(flatValue(result), escape);
		}
		if (StringUtils.isBlank(castType)) {
			return escapeConditional(valueIfInvalid, escape);
		}
		Class<? extends Serializable> targetClass = getTargetClass(castType);
		if (targetClass != null && valueIfInvalid != null) {
			try {
				return escapeConditional(converter.convert(targetClass, valueIfInvalid), escape);
			} catch (TypeConversionException e) {
				String string = "Cannot cast the value {} to type {} due to: {}";
				LOGGER.warn(string, valueIfInvalid, castType, e.getMessage());
				LOGGER.trace(string, valueIfInvalid, castType, e.getMessage(), e);
			}
		}
		LOGGER.warn("The type {} provided for a cast is not supported! The argument was not modified.", castType);
		// given type was not supported, yet
		return escapeConditional(valueIfInvalid, escape);
	}

	private static Serializable flatValue(Serializable result) {
		// remove the wrapping collection for single value collections
		if (result instanceof Collection && ((Collection<?>) result).size() == 1) {
			return (Serializable) ((Collection<?>) result).iterator().next();
		}
		return result;
	}

	/**
	 * Escape conditional.
	 *
	 * @param valueToEscape
	 *            the value if invalid
	 * @param escape
	 *            the escape
	 * @return the serializable
	 */
	protected Serializable escapeConditional(Serializable valueToEscape, boolean escape) {
		return escape ? escape(valueToEscape) : valueToEscape;
	}

	/**
	 * Gets the target class.
	 *
	 * @param castType
	 *            the cast type
	 * @return the target class
	 */
	private static Class<? extends Serializable> getTargetClass(String castType) {
		String type = castType.toLowerCase();
		return TYPE_MAPPING.get(type);
	}

}
