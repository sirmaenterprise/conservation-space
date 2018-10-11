/**
 *
 */
package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Evaluator that convers a full uri to short
 *
 * @author BBonev
 */
@Singleton
public class ShortUriEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 5335894107037053357L;

	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START + "\\{shortUri\\(([:\\w./#-]+)\\)\\}");

	private static final String NULL_VALUE = "null";

	@Inject
	private TypeConverter typeConverter;

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "shortUri";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String uri = matcher.group(1);
		if (NULL_VALUE.equals(uri)) {
			return null;
		}
		ShortUri shortUri = typeConverter.convert(ShortUri.class, uri);
		return shortUri != null ? shortUri.toString() : null;
	}

}
