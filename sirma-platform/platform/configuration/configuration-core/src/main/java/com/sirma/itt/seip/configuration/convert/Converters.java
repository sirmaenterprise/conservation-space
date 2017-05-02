package com.sirma.itt.seip.configuration.convert;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Provider for common configuration converters
 *
 * @author BBonev
 */
public class Converters {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Pattern PROP_SPLIT_PATTERN = Pattern.compile("\\s*[,;]\\s*");

	/**
	 * Instantiates a new converters.
	 */
	private Converters() {
		// utility class
	}

	/**
	 * Convert to string.
	 *
	 * @param context
	 *            the context
	 * @return the string
	 */
	@ConfigurationConverter
	public static String convertToString(ConverterContext context) {
		return StringUtils.trimToNull(context.getRawValue());
	}

	/**
	 * Convert to integer.
	 *
	 * @param context
	 *            the context
	 * @return the integer
	 */
	@ConfigurationConverter
	public static Integer convertToInteger(ConverterContext context) {
		return numberConverter(context, Integer::valueOf);
	}

	/**
	 * Number converter method that uses the given converter function to convert string values to numbers. If the
	 * convert fails for the value with {@link NumberFormatException} then the default value is returned if convertible.
	 *
	 * @param <N>
	 *            the number type
	 * @param context
	 *            the context
	 * @return the converted value or default value if present. <code>null</code> if the primary value is
	 *         <code>null</code>.
	 */
	private static <N extends Number> N numberConverter(ConverterContext context, Function<String, N> converter) {
		String value = context.getRawValue();
		if (value == null) {
			return null;
		}
		try {
			return converter.apply(value);
		} catch (NumberFormatException e) {
			String defaultValue = context.getDefaultValue();
			if (StringUtils.isNotBlank(defaultValue)) {
				LOGGER.warn("The configuration {} has invalid number value {}. Will return the default value instead",
						value, context.getConfiguration().getName());
				try {
					return converter.apply(defaultValue);
				} catch (NumberFormatException e1) {
					throw new ConverterException(e1);
				}
			}
			throw new ConverterException(e);
		}
	}

	/**
	 * Convert to long.
	 *
	 * @param context
	 *            the context
	 * @return the long
	 */
	@ConfigurationConverter
	public static Long convertToLong(ConverterContext context) {
		return numberConverter(context, Long::valueOf);
	}

	/**
	 * Convert to atomic long.
	 *
	 * @param context
	 *            the context
	 * @return the long
	 */
	@ConfigurationConverter
	public static AtomicLong convertToAtomicLong(ConverterContext context) {
		Long asLong = convertToLong(context);
		if (asLong != null) {
			return new AtomicLong(asLong.longValue());
		}
		return null;
	}

	/**
	 * Convert the configuration value to long and then converts the value to bytes from mega bytes
	 *
	 * @param context
	 *            the context
	 * @return the value converted from mega bytes to bytes
	 */
	@ConfigurationConverter("KB to bytes")
	public static Long convertKBToBytes(ConverterContext context) {
		Long value = convertToLong(context);
		if (value == null) {
			return null;
		}
		return Long.valueOf(value.longValue() * 1024);
	}

	/**
	 * Convert the configuration value to long and then converts the value to bytes from mega bytes
	 *
	 * @param context
	 *            the context
	 * @return the value converted from mega bytes to bytes
	 */
	@ConfigurationConverter("MB to bytes")
	public static Long convertMBToBytes(ConverterContext context) {
		Long value = convertToLong(context);
		if (value == null) {
			return null;
		}
		return Long.valueOf(value.longValue() * 1024 * 1024);
	}

	/**
	 * Convert the configuration value to long and then converts the value to bytes from giga bytes
	 *
	 * @param context
	 *            the context
	 * @return the value converted from giga bytes to bytes
	 */
	@ConfigurationConverter("GB to bytes")
	public static Long convertGBToBytes(ConverterContext context) {
		Long value = convertToLong(context);
		if (value == null) {
			return null;
		}
		return Long.valueOf(value.longValue() * 1024 * 1024 * 1024);
	}

	/**
	 * Convert to double.
	 *
	 * @param context
	 *            the context
	 * @return the double
	 */
	@ConfigurationConverter
	public static Double convertToDouble(ConverterContext context) {
		return numberConverter(context, Double::valueOf);
	}

	/**
	 * Convert to float.
	 *
	 * @param context
	 *            the context
	 * @return the float
	 */
	@ConfigurationConverter
	public static Float convertToFloat(ConverterContext context) {
		return numberConverter(context, Float::valueOf);
	}

	/**
	 * Convert to short.
	 *
	 * @param context
	 *            the context
	 * @return the short
	 */
	@ConfigurationConverter
	public static Short convertToShort(ConverterContext context) {
		return numberConverter(context, Short::valueOf);
	}

	/**
	 * Convert to boolean.
	 *
	 * @param context
	 *            the context
	 * @return the boolean
	 */
	@ConfigurationConverter
	public static Boolean convertToBoolean(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return null; // NOSONAR
		}
		return Boolean.valueOf(rawValue);
	}

	/**
	 * Convert to file.
	 *
	 * @param context
	 *            the context
	 * @return the file
	 */
	@ConfigurationConverter
	public static File convertToFile(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return null;
		}
		File file = new File(rawValue);
		if (!file.exists()) {
			file = null;
		}
		return file;
	}

	/**
	 * Convert to string set.
	 *
	 * @param context
	 *            the context
	 * @return the sets the
	 */
	@ConfigurationConverter
	public static Set<String> convertToStringSet(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return null; // NOSONAR
		}
		String[] split = PROP_SPLIT_PATTERN.split(rawValue);
		Set<String> result = new LinkedHashSet<>();
		for (String prop : split) {
			if (!prop.isEmpty()) {
				result.add(prop);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Convert to string list.
	 *
	 * @param context
	 *            the context
	 * @return the list
	 */
	@ConfigurationConverter
	public static List<String> convertToStringList(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return null; // NOSONAR
		}
		String[] split = PROP_SPLIT_PATTERN.split(rawValue);
		List<String> result = new ArrayList<>(split.length);
		for (String prop : split) {
			if (!prop.isEmpty()) {
				result.add(prop);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Convert string to pattern object using default flags
	 *
	 * @param context
	 *            the context
	 * @return the pattern
	 */
	@ConfigurationConverter
	public static Pattern convertToPattern(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return null;
		}
		try {
			return Pattern.compile(rawValue);
		} catch (PatternSyntaxException e) {
			try {
				// fall back to default value if any
				if (context.getDefaultValue() != null) {
					return Pattern.compile(context.getDefaultValue());
				}
			} catch (PatternSyntaxException e1) {
				e1.addSuppressed(e);
				throw new ConfigurationException("Invalid pattern " + context.getDefaultValue(), e1);
			}
			throw new ConfigurationException("Invalid pattern " + rawValue, e);
		}
	}

	/**
	 * Convert a string to {@link URI}
	 *
	 * @param context
	 *            the context
	 * @return the uri
	 */
	@ConfigurationConverter
	public static URI convertToURI(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return null;
		}
		try {
			return URI.create(rawValue);
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException(context.getConfiguration().getName() + " configuration has value '"
					+ rawValue + "' that is not valid java.net.URI", e);
		}
	}

	/**
	 * Convert value to {@link StringPair} by splitting the value by {@code -}.
	 *
	 * @param context
	 *            the context
	 * @return the string pair
	 */
	@ConfigurationConverter
	public static StringPair convertToPair(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return new StringPair(splitValue(context.getDefaultValue()));
		}
		return new StringPair(splitValue(rawValue));
	}

	private static Pair<String, String> splitValue(String rawValue) {
		if (StringUtils.isBlank(rawValue)) {
			return Pair.NULL_PAIR;
		}
		String[] split = rawValue.split("-");
		if (split.length == 1) {
			return new Pair<>(rawValue, null);
		}
		return new Pair<>(split[0], split[1]);
	}

	/**
	 * Convert value to {@link StringPair} by splitting the value by {@code -}.
	 *
	 * @param context
	 *            the context
	 * @return the range or empty range if not valid values
	 */
	@ConfigurationConverter
	public static IntegerPair convertToRange(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return parse(splitValue(context.getDefaultValue()), null);
		}
		String defaultValue = context.getDefaultValue();
		return parse(splitValue(rawValue), defaultValue);
	}

	private static IntegerPair parse(Pair<String, String> pair, String defaultValue) {
		if (pair == null || Pair.NULL_PAIR.equals(pair)) {
			return IntegerPair.EMPTY_RANGE;
		}
		try {
			return new IntegerPair(Integer.valueOf(pair.getFirst()), Integer.valueOf(pair.getSecond()));
		} catch (NumberFormatException e) {
			LOGGER.trace("Range part from [{}-{}] is not a number", pair.getFirst(), pair.getSecond(), e);
			LOGGER.warn("Range [{}-{}] not composed of numbers", pair.getFirst(), pair.getSecond());
			return parse(splitValue(defaultValue), null);
		}
	}

	/**
	 * Converts a {@link String} to {@link Uri}.
	 *
	 * @param context
	 *            {@link ConverterContext}
	 * @param typeConverter
	 *            injected typeconverter instance.
	 * @return converted URI
	 */
	@ConfigurationConverter
	public static Uri convertToURI(ConverterContext context, TypeConverter typeConverter) {
		String rawValue = context.getRawValue();
		return typeConverter.convert(Uri.class, rawValue);
	}

	/**
	 * Converts a {@link String} to ISO {@link Date} by using {@link ISO8601DateFormat}.
	 *
	 * @param converterContext
	 *            the context from which is retrueved the raw value of the configuration
	 * @return {@link Date} in ISO format
	 */
	@ConfigurationConverter
	public static Date convertStringToDate(ConverterContext converterContext) {
		return ISO8601DateFormat.parse(converterContext.getRawValue());
	}

}
