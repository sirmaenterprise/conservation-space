package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Evaluator class that handle codelist expressions. The expression handles fetching codelist information from fields or
 * direct codes. If the argument is a field:
 * <ul>
 * <li>the codelist could be fetched by the that field definition if any.
 * <li>the field could be located in other instance. The location will be done using the <code>.from()</code>
 * expression.
 * </ul>
 * If no code value field is defined then the returned value will be the code value description in the current user
 * language.<br>
 *
 * @author BBonev
 */
@Singleton
public class CodelistEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 4788745685763404720L;

	private static final Pattern VALUE_EXTRACTION = Pattern.compile(EXPRESSION_START
			+ "\\{CL(\\d{0,4})\\(([\\w\\-/]+?|(?:\\[[\\w:]+?\\]))\\)\\.?(\\w+?)?" + FROM_PATTERN + "\\}");
	private static final String COMMA = ",";
	@Inject
	private CodelistService codelistService;
	@Inject
	private DefinitionService definitionService;

	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	@Override
	public String getExpressionId() {
		return "CL";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String clNumberGroup = matcher.group(1);

		// the source field
		String propertyKey = matcher.group(2);
		Serializable propertyValue = null;
		boolean isProperty = isPropertyKey(propertyKey);

		Supplier<PropertyModel> modelSupplier = CachingSupplier.of(() -> {
			Serializable modelLocal = evaluateFrom(matcher.group(FROM_GROUP), context, values);
			if (modelLocal instanceof PropertyModel) {
				return (PropertyModel) modelLocal;
			}
			return null;
		});

		if (isProperty) {
			propertyKey = extractProperty(propertyKey);

			// get the target model
			PropertyModel model = modelSupplier.get();
			if (model != null) {
				propertyValue = model.get(propertyKey);
			}
			// if the property is not found then as key we will use the value into the brackets, if
			// someone broke it..
		}

		// codelist field
		String field = getCodelistField(matcher, context);

		Integer cl = getCodelistNumber(clNumberGroup, propertyKey, isProperty, modelSupplier);

		String codelistKey = getCodelistKey(propertyKey, propertyValue, isProperty);

		// if no codelist is specified and is not a field or the field does not have a codelist
		// definition then we cannot continue
		if (cl == null) {
			Serializable serializable = context.get(ExpressionContextProperties.TARGET_FIELD);
			if (serializable instanceof PropertyDefinition) {
				cl = ((PropertyDefinition) serializable).getCodelist();
			}
			if (cl == null) {
				return escape(codelistKey);
			}
		}

		if (codelistKey != null && codelistKey.contains(COMMA)) {

			return resolveMultipleExpressionResult(field, cl, codelistKey);
		}
		return resolveExpressionResult(field, cl, codelistKey);
	}

	private Serializable resolveMultipleExpressionResult(String field, Integer cl, String codelistKey) {
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(cl);
		String[] keys = codelistKey.split(COMMA);

		String fields = Arrays.stream(keys).map(key -> {
			CodeValue keyValue = codeValues.get(key);
			if (keyValue != null) {
				return (CharSequence) keyValue.getProperties().get(field) != null ? (CharSequence) keyValue.getProperties().get(field) : (CharSequence) key;
			}
			return (CharSequence) key;
		}).collect(Collectors.joining(COMMA));

		return escape(fields);
	}

	private Serializable resolveExpressionResult(String field, Integer cl, String codelistKey) {
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(cl);
		CodeValue codeValue = codeValues.get(codelistKey);
		if (codeValue != null) {
			Serializable serializable = codeValue.getProperties().get(field);
			return escape(serializable != null ? serializable : codeValue.getIdentifier());
		}
		return escape(codelistKey);
	}

	private String getCodelistField(Matcher matcher, ExpressionContext context) {
		String field = matcher.group(3);
		if (StringUtils.isBlank(field)) {
			// field is not set we will return the default user language
			field = getCurrentLanguage(context);
		}
		return field;
	}

	private Integer getCodelistNumber(String clNumberGroup, String propertyKey, boolean isProperty,
			Supplier<PropertyModel> modelSupplier) {
		Integer cl = null;
		// if codelist number is not set then we will try to fetch it from the property definition
		// if field at all
		if (StringUtils.isBlank(clNumberGroup)) {
			cl = getCodelistFromDefinition(modelSupplier.get(), propertyKey, isProperty, cl);
		} else {
			// we have explicit defined codelist
			cl = Integer.valueOf(clNumberGroup);
		}
		return cl;
	}

	private String getCodelistKey(String propertyKey, Serializable propertyValue, boolean isProperty) {
		String codelistKey = null;
		if (propertyValue == null) {
			// if we does not have a value from instance we will use the value that is set in the
			// expression but only if not property name
			if (!isProperty) {
				codelistKey = propertyKey;
			}
		} else {
			codelistKey = converter.convert(String.class, propertyValue);
		}
		return codelistKey;
	}

	private Integer getCodelistFromDefinition(PropertyModel model, String propertyKey, boolean isProperty, Integer cl) {
		Integer codelistNumber = cl;
		if (isProperty && model != null) {
			PropertyDefinition definition = definitionService.getProperty(propertyKey, model);
			if (definition != null) {
				codelistNumber = definition.getCodelist();
			}
		}
		return codelistNumber;
	}

}
