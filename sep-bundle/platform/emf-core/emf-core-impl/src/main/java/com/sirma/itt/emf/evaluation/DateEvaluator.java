package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;

/**
 * Date evaluator. Handles the fetching and formatting to date expressions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DateEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7430231957350428564L;

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START
					+ "\\{date\\((\\[[\\w\\.:]+?\\]|(?:[\\d]{4}-[\\d]{2}-[\\d]{2}T[\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3}\\+[\\d]{2}:[\\d]{2}))\\)(?:\\.format\\(([yMmhHsSzdT_\\.:\\-\\s,/\\\\]+?|full|short|default)\\))?"
					+ FROM_PATTERN + "\\}");

	/** The default format. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_DATE_FORMAT)
	private String defaultFormat;
	/** The short format. */
	@Inject
	@Config(name = EmfConfigurationProperties.SHORT_DATE_FORMAT)
	private String shortFormat;
	/** The fullt format. */
	@Inject
	@Config(name = EmfConfigurationProperties.FULL_DATE_FORMAT)
	private String fulltFormat;

	/** The default formatter. */
	private DateFormat defaultFormatter;
	/** The short formatter. */
	private DateFormat shortFormatter;
	/** The full formatter. */
	private DateFormat fullFormatter;

	/** The converter. */
	@Inject
	private TypeConverter converter;

	/**
	 * Initialize the default converters
	 */
	@PostConstruct
	public void initialize() {
		if (StringUtils.isNotNullOrEmpty(defaultFormat)) {
			defaultFormatter = new SimpleDateFormat(defaultFormat);
		} else {
			defaultFormatter = new SimpleDateFormat();
		}

		if (StringUtils.isNotNullOrEmpty(shortFormat)) {
			shortFormatter = new SimpleDateFormat(shortFormat);
		} else {
			shortFormatter = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
		}

		if (StringUtils.isNotNullOrEmpty(fulltFormat)) {
			fullFormatter = new SimpleDateFormat(fulltFormat);
		} else {
			fullFormatter = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		}
	}

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
		String property = matcher.group(1);
		Date date = null;
		if (isPropertyKey(property)) {
			property = extractProperty(property);
			Serializable value = getPropertyFrom(property, matcher, context, values);
			if (value == null) {
				return null;
			} else if (value instanceof Date) {
				date = (Date) value;
			} else if (value instanceof String) {
				try {
					date = converter.convert(Date.class, value);
				} catch (RuntimeException e) {
					// invalid format
					logger.trace("Invalid date format for value " + value, e);
				}
			}
		} else {
			date = converter.convert(Date.class, property);
		}
		String format = matcher.group(2);
		if ((date != null) && StringUtils.isNotNullOrEmpty(format)) {
			return format(date, format);
		}
		return date;
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
	protected Serializable format(Date date, String format) {
		DateFormat dateFormat = null;
		if ("default".equals(format)) {
			dateFormat = defaultFormatter;
		} else if ("short".equals(format)) {
			dateFormat = shortFormatter;
		} else if ("full".equals(format)) {
			dateFormat = fullFormatter;
		} else {
			dateFormat = new SimpleDateFormat(format);
		}
		return dateFormat.format(date);
	}

}
