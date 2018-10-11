package com.sirma.itt.seip.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;

/**
 * The class generates regular expressions that could be used for validation of strings. The expressions are defined by
 * a specific string types.
 *
 * @author SKostadinov
 */
public final class RegExGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegExGenerator.class);

	private static final String NOT_DEFINED_VALIDATOR_MESSAGE = "Regex generator does not supported this data type: ";

	private String invalidValidatorTypeMessage;
	private String invalidRNCMessage;

	private static final int TEXT_AREA_MIN_CHARS = 40;

	private static final String TWO_SIGNS_REGEX = "\\.{2}";

	/**
	 * INVALID_TYPE keeps the value of the string that is returned if there is invalid string type.
	 */
	private static final String INVALID_TYPE = null;
	/**
	 * TIME_REG_EX keeps the pattern of the time fields that will be validated.
	 */
	private static final Pattern TIME_REG_EX = Pattern.compile("time");
	/**
	 * DATE_REG_EX keeps the pattern of the date fields that will be validated.
	 */
	private static final Pattern DATE_REG_EX = Pattern.compile("date");
	/**
	 * BOOLEAN_REG_EX keeps the pattern of the boolean fields that will be validated.
	 */
	private static final Pattern BOOLEAN_REG_EX = Pattern.compile("boolean");
	/**
	 * TEXT_REG_EX keeps the pattern of the plain text fields that will be validated.
	 */
	private static final Pattern TEXT_REG_EX = Pattern.compile("(text|blob|clob)");
	/**
	 * EMAIL_REG_EX keeps the pattern of the email fields that will be validated.
	 */
	private static final Pattern EMAIL_REG_EX = Pattern.compile("email");
	/**
	 * NONDIGIT_REG_EX keeps the pattern of the non digit fields that will be validated.
	 */
	private static final Pattern NONDIGIT_REG_EX = Pattern.compile("a(\\.{2})?\\d+");
	/**
	 * DIGIT_REG_EX keeps the pattern of the digit fields that will be validated.
	 */
	private static final Pattern DIGIT_REG_EX = Pattern.compile("n?n(?:\\.{2})?\\d+");
	/**
	 * ALL_SYMBOL_REG_EX keeps the pattern of the fields that contains any kind of symbols.
	 */
	private static final Pattern ALL_SYMBOL_REG_EX = Pattern.compile("an\\.{0,2}\\d+");
	/**
	 * FLOATING_POINT_REG_EX keeps the pattern of the fields that are validated as a floating point variables.
	 */
	private static final Pattern FLOATING_POINT_REG_EX = Pattern.compile("n?n(\\.{0,2})\\d{1,2}[\\,\\.]\\d{1,2}");
	/**
	 * DAYS_HOURS_TYPE keeps the pattern of the fields that are validated as letter 'D' or letter 'H' and two digits
	 * after that.
	 */
	private static final Pattern DAYS_HOURS_TYPE = Pattern.compile("DHn2(|c)");

	/**
	 * RNC_PREFIX_FUNCTION prefix identifier for escaping specific field validation.
	 */
	private static final String RNC_PREFIX_FUNCTION = "${";
	/**
	 * RNC_FIELD_REG_EX regex for characters that must not be included in the name of the file.
	 */
	private static final String NAME_RNC_FIELD_REG_EX = "[^|\\\\/*:<>\\\"?]";

	private List<Function<String, Pair<String, String>>> patternEvaluators;

	/**
	 * Instantiates a new reg ex generator.
	 *
	 * @param labelProvider
	 *            the label provider
	 */
	public RegExGenerator(Function<String, String> labelProvider) {
		if (labelProvider != null) {
			invalidValidatorTypeMessage = labelProvider.apply("validation.invalidType");
			invalidRNCMessage = labelProvider.apply("validation.lettersAndDigits");
		}
		patternEvaluators = new ArrayList<>();
		patternEvaluators.add(type -> evalPattern(TIME_REG_EX, type, RegExGenerator::getTime));
		patternEvaluators.add(type -> evalPattern(DATE_REG_EX, type, RegExGenerator::getDate));
		patternEvaluators.add(type -> evalPattern(BOOLEAN_REG_EX, type, RegExGenerator::getBoolean));
		patternEvaluators.add(type -> evalPattern(TEXT_REG_EX, type, RegExGenerator::getText));
		patternEvaluators
				.add(type -> evalPattern(EMAIL_REG_EX, type, () -> RegExGenerator.getEmailRegEx(labelProvider)));
		patternEvaluators.add(type -> evalPattern(NONDIGIT_REG_EX, type, () -> getNondigitRegEx(type, labelProvider)));
		patternEvaluators.add(type -> evalPattern(DIGIT_REG_EX, type, () -> getDigitRegEx(type, labelProvider)));
		patternEvaluators
				.add(type -> evalPattern(ALL_SYMBOL_REG_EX, type, () -> getAllSymbolRegEx(type, labelProvider)));
		patternEvaluators.add(
				type -> evalPattern(FLOATING_POINT_REG_EX, type, () -> getFloatingPointRegEx(type, labelProvider)));
		patternEvaluators.add(type -> evalPattern(DAYS_HOURS_TYPE, type, () -> getDaysOrHoursType(type)));
	}

	/**
	 * Gets the non digit regular expression.
	 *
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getNondigitRegEx(String type, Function<String, String> labelProvider) {
		String message = labelProvider.apply("validation.letters");
		String regex = null;
		if (type.split(TWO_SIGNS_REGEX).length == 2) {
			regex = "[^0-9]{1," + type.substring(3) + "}";
			return new Pair<>(regex, MessageFormat.format(message, type.substring(3)));
		}
		regex = "[^0-9]{" + type.substring(1) + "}";
		return new Pair<>(regex, MessageFormat.format(message, type.substring(1)));
	}

	/**
	 * Gets the digit regular expression.
	 *
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getDigitRegEx(String type, Function<String, String> labelProvider) {
		String message = labelProvider.apply("validation.digits");
		String regex = null;
		if (type.startsWith("nn")) {
			if (type.split(TWO_SIGNS_REGEX).length == 2) {
				regex = "-?\\d{1," + type.substring(4) + "}";
				return new Pair<>(regex, MessageFormat.format(message, type.substring(4)));
			}
			regex = "-?\\d{" + type.substring(2) + "}";
			return new Pair<>(regex, MessageFormat.format(message, type.substring(2)));
		}
		if (type.split(TWO_SIGNS_REGEX).length == 2) {
			regex = "\\d{1," + type.substring(3) + "}";
			return new Pair<>(regex, MessageFormat.format(message, type.substring(3)));
		}
		regex = "\\d{" + type.substring(1) + "}";
		return new Pair<>(regex, MessageFormat.format(message, type.substring(1)));
	}

	/**
	 * Gets the regular expression that validates variables with any kind of symbols.
	 *
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getAllSymbolRegEx(String type, Function<String, String> labelProvider) {
		String message = labelProvider.apply("validation.lettersAndDigits");
		String textAreaType = type.replace("an", "").replace("..", "");
		String regex = ".";
		if (Integer.parseInt(textAreaType) > TEXT_AREA_MIN_CHARS) {
			regex = "[\\s\\S]";
		}
		if (type.split(TWO_SIGNS_REGEX).length == 2) {
			regex = regex + "{1," + type.substring(4) + "}";
			return new Pair<>(regex, MessageFormat.format(message, type.substring(4)));
		}
		regex = regex + "{" + type.substring(2) + "}";
		return new Pair<>(regex, MessageFormat.format(message, type.substring(2)));
	}

	/**
	 * Gets the regular expression validates floating point variables.
	 *
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getFloatingPointRegEx(String type, Function<String, String> labelProvider) {
		String message = labelProvider.apply("validation.floatingPointNumber");
		Matcher matcher = FLOATING_POINT_REG_EX.matcher(type);
		if (!matcher.find()) {
			return new Pair<>(RegExGenerator.INVALID_TYPE, invalidValidatorTypeMessage);
		}
		String group = matcher.group(1);
		boolean fixedValue = StringUtils.isBlank(group);

		String[] digits = type.split("[^\\d]+");
		int digitsTotal = Integer.parseInt(digits[1]);
		int digitsFraction = Integer.parseInt(digits[2]);
		if (digitsTotal <= digitsFraction || digitsFraction == 0) {
			return new Pair<>(RegExGenerator.INVALID_TYPE, invalidValidatorTypeMessage);
		}
		String format = null;
		// if we have fixed value then everything is with fixed length
		if (fixedValue) {
			format = "\\d'{'{0}'}'\\.\\d'{'{1}'}'";
		} else {
			format = "\\d'{'1,{0}'}'(?:\\.\\d'{'1,{1}'}')?";
		}
		Integer base = Integer.valueOf(digitsTotal - digitsFraction);
		String result = MessageFormat.format(format, base, digits[2]);
		if (type.startsWith("nn")) {
			result = "-?" + result;
		}
		return new Pair<>(result, MessageFormat.format(message, base, digits[2]));
	}

	/**
	 * Gets regular expression for days or hours type.
	 *
	 * @param type
	 *            the type of the field
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getDaysOrHoursType(String type) {
		String regex = null;
		String message = NOT_DEFINED_VALIDATOR_MESSAGE + type;
		if (type.endsWith("c")) {
			regex = "(H\\d{2}|D[012]\\d|D30)";
		} else {
			regex = "(D|H)\\d{2}";
		}
		return new Pair<>(regex, message);
	}

	/**
	 * Gets regular expression for boolean.
	 *
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getBoolean() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<>("(true|false)", message);
	}

	/**
	 * Gets regular expression for time.
	 *
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getTime() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<>("((1|0)\\d|2[0-3]):[0-5]\\d", message);
	}

	/**
	 * Gets regular expression for text.
	 *
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getText() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<>("[\\w\\W]+", message);
	}

	/**
	 * Gets regular expression for date.
	 *
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getDate() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<>(
				"\\d{2}\\.\\d{2}\\.\\d{4}(?<=((([012]\\d|3[01])\\.(0[13578]|1[02])|([012]\\d|30)\\.(0[469]|11))\\.\\d{4}|(([01]\\d|2[0-9])\\.02\\.\\d{4}(?<=(([02468][048]|[13579][26])00|\\d{2}([02468][048]|[13579][26])(?<!\\d{2}00)))|([01]\\d|2[0-8])\\.02\\.\\d{4}(?<!(([02468][048]|[13579][26])00|\\d{2}([02468][048]|[13579][26])(?<!\\d{2}00))))))",
				message);
	}

	/**
	 * Gets regular expression for the email address.
	 *
	 * @return a Pair with generated regex and validation failior message.
	 */
	private static Pair<String, String> getEmailRegEx(Function<String, String> labelProvider) {
		String message = labelProvider.apply("validation.email");
		String regex = "^[A-Za-z0-9._-]{1,64}@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		return new Pair<>(regex, MessageFormat.format(message, Integer.valueOf(64)));
	}

	/**
	 * Gets the regular expression from the supplied rnc of the name field.
	 *
	 * @param rnc
	 *            supplied rnc of the field.
	 * @return a Pair with a supplied regex and validation failure message.
	 */
	Pair<String, String> getNameRegEx(String type, String rnc) {
		StringBuilder regexBuilder = new StringBuilder(rnc);
		String limit = type.replace("an..", "");
		if (!rnc.contains("{")) {
			regexBuilder.deleteCharAt(regexBuilder.length() - 2);
			regexBuilder.insert(regexBuilder.length() - 1, "{1," + limit + "}");
		}
		return new Pair<>(regexBuilder.toString(), MessageFormat.format(invalidRNCMessage, limit));
	}

	/**
	 * Gets the regular expression by defining a specific type string.
	 *
	 * @param type
	 *            the string type which defines the regular expression
	 * @param fieldPattern
	 *            the rnc string pattern for field validation
	 * @return a Pair with generated regex and validation failior message.
	 */
	public Pair<String, String> getPattern(String type, String fieldPattern) {
		if (StringUtils.isNotBlank(fieldPattern) && !fieldPattern.startsWith(RNC_PREFIX_FUNCTION)) {
			try {
				Pattern.compile(fieldPattern);
				if (fieldPattern.contains(NAME_RNC_FIELD_REG_EX)) {
					return getNameRegEx(type, fieldPattern);
				}
				String message = NOT_DEFINED_VALIDATOR_MESSAGE + type;
				return new Pair<>(fieldPattern, message);
			} catch (PatternSyntaxException patternException) {
				LOGGER.warn("RegExGenerator: Not valid pattern. ", patternException);
			}
		}
		return patternEvaluators.stream().map(f -> f.apply(type)).filter(Objects::nonNull).findFirst().orElseGet(
				() -> new Pair<>(RegExGenerator.INVALID_TYPE, invalidValidatorTypeMessage));
	}

	private static Pair<String, String> evalPattern(Pattern pattern, String type,
			Supplier<Pair<String, String>> result) {
		if (pattern.matcher(type).matches()) {
			return result.get();
		}
		return null;
	}

}