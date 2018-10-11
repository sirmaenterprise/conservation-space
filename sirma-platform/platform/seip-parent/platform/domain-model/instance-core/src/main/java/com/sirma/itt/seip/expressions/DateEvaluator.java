package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.time.DateUtil;

/**
 * Date evaluator. Handles the fetching and formatting to date expressions. Supports all allowed characters for
 * {@link SimpleDateFormat}.
 * <p>
 * Also supports the following predefined configurable string formats:
 * <li>default - {@link DateConverter#getSystemDefaultFormat()}</li>
 * <li>short - {@link DateConverter#getSystemShortFormat()}</li>
 * <li>full - {@link DateConverter#getSystemFullFormat()}</li>
 *
 * @author BBonev
 */
@Singleton
public class DateEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 7430231957350428564L;

	private static final String FORMAT_FULL = "full";
	private static final String FORMAT_SHORT = "short";
	private static final String FORMAT_DEFAULT = "default";

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{date\\((\\[[\\w\\.:]+?\\]|(?:[\\d]{4}-[\\d]{2}-[\\d]{2}T[\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3}\\+[\\d]{2}:[\\d]{2}))\\)(?:\\.format\\(([yMmhHsSzdGYLwWDFEuakKZXT'_\\.:\\-\\s,/\\\\]+?|full|short|default)\\))?"
			+ FROM_PATTERN + "\\}");

	private static final Logger LOGGER = LoggerFactory.getLogger(DateEvaluator.class);

	@Inject
	private DateConverter dateConverter;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "date";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String property = matcher.group(1);
		String extractedFormat = extractFormat(matcher.group(2));
		String format = getFormatPattern(extractedFormat);

		Date date = null;
		if (isPropertyKey(property)) {
			property = extractProperty(property);
			Serializable value = getPropertyFrom(property, matcher, context, values);
			if (value == null) {
				return wrapDate(null, property, format);
			} else if (value instanceof Date) {
				date = (Date) value;
			} else if (value instanceof String) {
				try {
					date = converter.convert(Date.class, value);
				} catch (RuntimeException e) {
					// invalid format
					LOGGER.trace("Invalid date format for value {}", value, e);
				}
			}
		} else {
			date = converter.convert(Date.class, property);
		}
		if (date != null) {
			OffsetDateTime offsetDateTime = DateUtil.toOffsetDateTime(date, userPreferences.getUserTimezone());
			return wrapDate(format(offsetDateTime, format), property, format);
		}
		return wrapDate(date, property, format);
	}

	/**
	 * Wraps the date in a span with a data-format attribute, used in the UI to extract specified format.
	 *
	 * @param date
	 *            date to be wrapped or null if the format is just needed to be passed.
	 * @param format
	 *            format to be passed as a attribute
	 * @return date, wrapped in a span element.
	 */
	private static Serializable wrapDate(Serializable date, String property, String format) {
		StringBuilder wrappedDate = new StringBuilder("<span data-property=\"").append(property);
		if (StringUtils.isNotBlank(format)) {
			wrappedDate.append("\" data-format=\"").append(format);
		}
		wrappedDate.append("\">");

		if (date != null) {
			wrappedDate.append(date);
		}
		return wrappedDate.append("</span>").toString();
	}

	/**
	 * Format the given date with the given format type (default, short, full, or the provided)
	 *
	 * @param date
	 *            the date
	 * @param format
	 *            the format
	 * @return the serializable
	 */
	protected Serializable format(OffsetDateTime date, String format) {
		Calendar calendar = DateUtil.toCalendar(date);
		// do not modify the returned cached formatter as they are cached
		DateFormat dateFormat = new SimpleDateFormat(format, new Locale(userPreferences.getLanguage()));
		dateFormat.setTimeZone(calendar.getTimeZone());
		return dateFormat.format(calendar.getTime());
	}

	private String getFormatPattern(String formatKey) {
		switch (formatKey) {
			case FORMAT_DEFAULT:
				return ((SimpleDateFormat) dateConverter.getSystemDefaultFormat()).toPattern();
			case FORMAT_SHORT:
				return ((SimpleDateFormat) dateConverter.getSystemShortFormat()).toPattern();
			case FORMAT_FULL:
				return ((SimpleDateFormat) dateConverter.getSystemFullFormat()).toPattern();
			default:
				return formatKey;
		}
	}

	/**
	 * Returns the extracted format or returns default if an empty expression has been passed.
	 *
	 * @param group
	 *            format group
	 * @return extracted format group or default.
	 */
	private static String extractFormat(String group) {
		if (!org.apache.commons.lang3.StringUtils.isBlank(group)) {
			return group;
		}
		return FORMAT_DEFAULT;
	}

}
