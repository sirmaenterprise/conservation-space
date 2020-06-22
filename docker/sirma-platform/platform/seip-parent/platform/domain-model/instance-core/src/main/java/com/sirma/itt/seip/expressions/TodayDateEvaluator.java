package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Date evaluator. Handles the today expression with option of date offset.
 *
 * @author BBonev
 */
@Singleton
public class TodayDateEvaluator extends DateEvaluator {

	private static final long serialVersionUID = 8328373895251849576L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{today\\(?([+-]?\\d*?)\\)?(?:\\.format\\(([yMmhHsSzdT_\\.:\\-\\s,]+?|full|simple|default)\\))?\\}");

	@Override
	public String getExpressionId() {
		return "today";
	}

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String offset = matcher.group(1);
		OffsetDateTime date = OffsetDateTime.now(userPreferences.getUserTimezone().toZoneId());
		if (StringUtils.isNotBlank(offset)) {
			if (offset.charAt(0) == '+') {
				offset = offset.substring(1);
			}
			date = date.withDayOfYear(Integer.parseInt(offset));
		}
		String format = matcher.group(2);
		if (StringUtils.isNotBlank(format)) {
			return format(date, format);
		}
		return date;
	}

}
