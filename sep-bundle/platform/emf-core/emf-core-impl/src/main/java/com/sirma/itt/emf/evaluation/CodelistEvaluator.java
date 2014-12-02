package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.properties.model.PropertyModel;

/**
 * Evaluator class that handle codelist expressions. The expression handles fetching codelist
 * information from fields or direct codes. If the argument is a field:
 * <ul>
 * <li>the codelist could be fetched by the that field definition if any.
 * <li>the field could be located in other instance. The location will be done using the
 * <code>.from()</code> expression.
 * </ul>
 * If no code value field is defined then the returned value will be the code value description in
 * the current user language.<br>
 *
 * @author BBonev
 */
public class CodelistEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4788745685763404720L;

	/** The Constant VALUE_EXTRACTION. */
	private static final Pattern VALUE_EXTRACTION = Pattern.compile(EXPRESSION_START
			+ "\\{CL(\\d{0,4})\\(([\\w\\-/]+?|(?:\\[[\\w]+?\\]))\\)\\.?(\\w+?)?" + FROM_PATTERN
			+ "\\}");

	/** The codelist service. */
	@Inject
	private CodelistService codelistService;

	@Inject
	private DictionaryService dictionaryService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String clNumberGroup = matcher.group(1);

		boolean isProperty = false;
		PropertyModel model = null;
		// the source field
		String propertyKey = matcher.group(2);
		Serializable propertyValue = null;
		if (isPropertyKey(propertyKey)) {
			propertyKey = extractProperty(propertyKey);
			isProperty = true;

			// get the target model
			Serializable modelLocal = evaluateFrom(matcher.group(FROM_GROUP), context, values);
			if (modelLocal instanceof PropertyModel) {
				model = (PropertyModel) modelLocal;
				propertyValue = model.getProperties().get(propertyKey);
			}
			// if the property is not found then as key we will use the value into the brackets, if
			// someone broke it..
		}
		// codelist field
		String field = matcher.group(3);
		if (StringUtils.isNullOrEmpty(field)) {
			// field is not set we will return the default user language
			field = getCurrentLanguage(context);
		}

		Integer cl = null;
		// if codelist number is not set then we will try to fetch it from the property definition
		// if field at all
		if (StringUtils.isNullOrEmpty(clNumberGroup)) {
			if (isProperty && (model != null)) {
				PropertyDefinition definition = dictionaryService.getProperty(propertyKey, model.getRevision(), model);
				if (definition != null) {
					cl = definition.getCodelist();
				}
			}
		} else {
			// we have explicit defined codelist
			cl = Integer.valueOf(clNumberGroup);
		}

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

		// if no codelist is specified and is not a field or the field does not have a codelist
		// definition then we cannot continue
		if (cl == null) {
			Serializable serializable = context.get(ExpressionContextProperties.TARGET_FIELD);
			if (serializable instanceof PropertyDefinition) {
				cl = ((PropertyDefinition) serializable).getCodelist();
			}
			if (cl == null) {
				return codelistKey;
			}
		}
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(cl);
		CodeValue codeValue = codeValues.get(codelistKey);
		if (codeValue != null) {
			Serializable serializable = codeValue.getProperties().get(field);
			return serializable != null ? serializable : codeValue.getIdentifier();
		}
		return codelistKey;
	}

}
