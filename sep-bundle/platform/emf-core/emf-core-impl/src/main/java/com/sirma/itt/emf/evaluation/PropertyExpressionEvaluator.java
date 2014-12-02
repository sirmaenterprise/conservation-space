package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Evaluator that can fetch a property from current instance. The expression could have a second
 * argument that is the default value to return if the original resulted to <code>null</code> witch
 * means it was missing or with <code>null</code> value. <b>Note</b> the alternative value will
 * always be of type string.
 * 
 * @author BBonev
 */
public class PropertyExpressionEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2239384584765053516L;
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{get\\((\\[[\\w:]+\\])\\s*,?\\s*(?:cast\\((.+?)\\s*as\\s*(.+?)\\)|(.*?))\\)"
			+ FROM_PATTERN + MATCHES + "\\}");

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String property = matcher.group(1);
		String castType = matcher.group(3);
		String defaultValue;
		if (StringUtils.isNullOrEmpty(castType)) {
			defaultValue = matcher.group(4);
		} else {
			defaultValue = matcher.group(2);
		}
		if ("".equals(defaultValue)) {
			defaultValue = null;
		}

		Serializable serializable = getPropertyFrom(property, matcher, context, values);
		return validateAndReturn(serializable, defaultValue, matcher, castType);
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
	 * @return the serializable
	 */
	protected Serializable validateAndReturn(Serializable result, Serializable valueIfInvalid,
			Matcher matcher, String castType) {
		if (isResultValid(result, matcher)) {
			return result;
		}
		if (StringUtils.isNullOrEmpty(castType)) {
			return valueIfInvalid;
		}
		Class<? extends Serializable> targetClass = getTargetClass(castType);
		if ((targetClass != null) && (valueIfInvalid != null)) {
			try {
				return converter.convert(targetClass, valueIfInvalid);
			} catch (TypeConversionException e) {
				logger.warn("Cannot cast the value " + valueIfInvalid + " to type " + castType
						+ " due to: " + e.getMessage());
			}
		}
		logger.warn("The type " + castType
				+ " provided for a cast is not supported! The argument was not modified.");
		// given type was not supported, yet
		return valueIfInvalid;
	}

	/**
	 * Gets the target class.
	 * 
	 * @param castType
	 *            the cast type
	 * @return the target class
	 */
	private Class<? extends Serializable> getTargetClass(String castType) {
		String type = castType.toLowerCase();
		return TYPE_MAPPING.get(type);
	}

}
