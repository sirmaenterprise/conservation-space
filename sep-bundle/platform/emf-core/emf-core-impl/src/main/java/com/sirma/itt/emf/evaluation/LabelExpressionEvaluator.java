package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.Displayable;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Evaluator that can fetch a label from a property relative to the current instance.
 *
 * @author BBonev
 */
public class LabelExpressionEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2239384584765053516L;
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{label\\((?:(\\[[\\w/]+\\])|(.+?))\\)" + FROM_PATTERN + MATCHES + "\\}");

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private LabelProvider labelProvider;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}


	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String property = matcher.group(1);
		String labelKey = matcher.group(2);

		if (StringUtils.isNotNullOrEmpty(property)) {
			return getLabelForProperty(property, matcher, context, values);
		} else if (StringUtils.isNotNullOrEmpty(labelKey)) {
			return getLabelForKey(labelKey);
		}
		return property;
	}

	/**
	 * Gets the label for key.
	 *
	 * @param labelKey
	 *            the label key
	 * @return the label for key
	 */
	private Serializable getLabelForKey(String labelKey) {
		String value = labelProvider.getValue(labelKey);
		if (value == null) {
			value = labelProvider.getLabel(labelKey);
			if (EqualsHelper.nullSafeEquals(labelKey, value)) {
				// if nothing is found
				return "";
			}
		}
		return value;
	}

	/**
	 * Gets the label for property.
	 *
	 * @param property
	 *            the property
	 * @param matcher
	 *            the matcher
	 * @param context
	 *            the context
	 * @param values
	 *            the values
	 * @return the label for property
	 */
	private Serializable getLabelForProperty(String property, Matcher matcher,
			ExpressionContext context, Serializable... values) {
		String fromExpression = matcher.group(FROM_GROUP);
		Serializable from = evaluateFrom(fromExpression, context, values);
		if (from instanceof Instance) {
			DefinitionModel definitionModel = dictionaryService.getInstanceDefinition((Instance) from);

			String local = property;
			if (isPropertyKey(local)) {
				local = extractProperty(local);
			}

			Node node = definitionModel.getChild(local);
			if (node instanceof Displayable) {
				return ((Displayable) node).getLabel();
			}
		}
		return property;
	}

}
