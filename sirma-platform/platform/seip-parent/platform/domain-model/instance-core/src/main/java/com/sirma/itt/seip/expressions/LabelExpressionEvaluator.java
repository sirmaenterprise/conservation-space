package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ElExpressionParser;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Evaluator that can fetch a label from a property relative to the current instance.
 *
 * @author BBonev
 */
@Singleton
public class LabelExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 2239384584765053516L;
	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{label\\((?:(\\[[\\w/]+\\])|(.+?))\\)" + FROM_PATTERN + MATCHES + "\\}");

	@Inject
	private DefinitionService definitionService;

	@Inject
	private LabelProvider labelProvider;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "label";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String property = matcher.group(1);
		String labelKey = matcher.group(2);

		String label = "";

		if (StringUtils.isNotBlank(property)) {
			label = getLabelForProperty(property, matcher, context, values);
		} else if (StringUtils.isNotBlank(labelKey)) {
			label = getLabelForKey(labelKey);
		}

		// check if loaded label does not contains an expression and if so eval it before return
		if (ElExpressionParser.isExpression(label)) {
			String result = expressionManager.get().evaluateRule(label, String.class, context, values);
			if ((result != null) && !ElExpressionParser.isExpression(result)) {
				label = result;
			}
		}

		return label;
	}

	/**
	 * Gets the label by label key. The key could be a bundle key or definition label key
	 */
	private String getLabelForKey(String labelKey) {
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

	private String getLabelForProperty(String property, Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String fromExpression = matcher.group(FROM_GROUP);
		Serializable from = evaluateFrom(fromExpression, context, values);
		if (from instanceof Instance) {
			DefinitionModel definitionModel = definitionService.getInstanceDefinition((Instance) from);

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