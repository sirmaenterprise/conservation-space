package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Date evaluator. Handles the today expression with option of date offset.
 * 
 * @author BBonev
 */
public class TodayDateEvaluator extends DateEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8328373895251849576L;
	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START
					+ "\\{today\\(?([+-]?\\d*?)\\)?(?:\\.format\\(([yMmhHsSzdT_\\.:\\-\\s,]+?|full|simple|default)\\))?\\}");

	/**
	 * {@inheritDoc}
	 */
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
		String offset = matcher.group(1);
		Date date = null;
		if (StringUtils.isNullOrEmpty(offset)) {
			date = new Date();
		} else {
			if (offset.charAt(0) == '+') {
				offset = offset.substring(1);
			}
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(offset));
			date = calendar.getTime();
		}
		String format = matcher.group(2);
		if (StringUtils.isNotNullOrEmpty(format)) {
			return format(date, format);
		}
		return date;
	}

}
