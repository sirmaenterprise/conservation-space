package com.sirma.cmf.web.form;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * The class generates regular expressions that could be used for validation of strings. The
 * expressions are defined by a specific string types.
 * 
 * @author SKostadinov
 */
public final class RegExGenerator {

	/** The logger. */
	@Inject
	private Logger logger;

	/** The Constant NOT_DEFINED_VALIDATOR_MESSAGE. */
	private static final String NOT_DEFINED_VALIDATOR_MESSAGE = "Regex generator does not supported this data type:";

	/** The label provider. */
	private LabelProvider labelProvider;

	/** The invalid validator type message. */
	private String invalidValidatorTypeMessage;

	/** The Constant TEXT_AREA_MIN_CHARS. */
	private static final int TEXT_AREA_MIN_CHARS = 40;

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
	 * FLOATING_POINT_REG_EX keeps the pattern of the fields that are validated as a floating point
	 * variables.
	 */
	private static final Pattern FLOATING_POINT_REG_EX = Pattern
			.compile("n?n(\\.{0,2})\\d{1,2}[\\,\\.]\\d");

	/**
	 * DAYS_HOURS_TYPE keeps the pattern of the fields that are validated as letter 'D' or letter
	 * 'H' and two digits after that.
	 */
	private static final Pattern DAYS_HOURS_TYPE = Pattern.compile("DHn2(|c)");

	/**
	 * RNC_PREFIX_FUNCTION prefix identifier for escaping specific field validation.
	 */
	private static final String RNC_PREFIX_FUNCTION = "${";

	/**
	 * Instantiates a new reg ex generator.
	 * 
	 * @param labelProvider
	 *            the label provider
	 */
	public RegExGenerator(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
		if (labelProvider != null) {
			this.invalidValidatorTypeMessage = labelProvider.getValue("validation.invalidType");
		}
	}

	/**
	 * Gets the non digit regular expression.
	 * 
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getNondigitRegEx(String type) {
		String message = labelProvider.getValue("validation.letters");
		String regex = null;
		if (type.split("\\.{2}").length == 2) {
			regex = "[^0-9]{1," + type.substring(3) + "}";
			return new Pair<String, String>(regex, MessageFormat.format(message, type.substring(3)));
		}
		regex = "[^0-9]{" + type.substring(1) + "}";
		return new Pair<String, String>(regex, MessageFormat.format(message, type.substring(1)));
	}

	/**
	 * Gets the digit regular expression.
	 * 
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getDigitRegEx(String type) {
		String message = labelProvider.getValue("validation.digits");
		String regex = null;
		if (type.startsWith("nn")) {
			if (type.split("\\.{2}").length == 2) {
				regex = "-?\\d{1," + type.substring(4) + "}";
				return new Pair<String, String>(regex, MessageFormat.format(message,
						type.substring(4)));
			}
			regex = "-?\\d{" + type.substring(2) + "}";
			return new Pair<String, String>(regex, MessageFormat.format(message, type.substring(2)));
		}
		if (type.split("\\.{2}").length == 2) {
			regex = "\\d{1," + type.substring(3) + "}";
			return new Pair<String, String>(regex, MessageFormat.format(message, type.substring(3)));
		}
		regex = "\\d{" + type.substring(1) + "}";
		return new Pair<String, String>(regex, MessageFormat.format(message, type.substring(1)));
	}

	/**
	 * Gets the regular expression that validates variables with any kind of symbols.
	 * 
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getAllSymbolRegEx(String type) {
		String message = labelProvider.getValue("validation.lettersAndDigits");
		String textAreaType = type.replace("an", "").replace("..", "");
		String regex = ".";
		if (Integer.parseInt(textAreaType) > TEXT_AREA_MIN_CHARS) {
			regex = "[\\s\\S]";
		}
		if (type.split("\\.{2}").length == 2) {
			regex = regex + "{1," + type.substring(4) + "}";
			return new Pair<String, String>(regex, MessageFormat.format(message, type.substring(4)));
		}
		regex = regex + "{" + type.substring(2) + "}";
		return new Pair<String, String>(regex, MessageFormat.format(message, type.substring(2)));
	}

	/**
	 * Gets the regular expression validates floating point variables.
	 * 
	 * @param type
	 *            the type of the string that will be parsed
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getFloatingPointRegEx(String type) {
		String message = labelProvider.getValue("validation.floatingPointNumber");
		Matcher matcher = FLOATING_POINT_REG_EX.matcher(type);
		if (!matcher.find()) {
			return new Pair<String, String>(RegExGenerator.INVALID_TYPE,
					invalidValidatorTypeMessage);
		}
		String group = matcher.group(1);
		boolean fixedValue = StringUtils.isNullOrEmpty(group);

		String[] digits = type.split("[^\\d]+");
		int digitsTotal = Integer.valueOf(digits[1]).intValue();
		int digitsFraction = Integer.valueOf(digits[2]).intValue();
		if ((digitsTotal <= digitsFraction) || (digitsFraction == 0)) {
			return new Pair<String, String>(RegExGenerator.INVALID_TYPE,
					invalidValidatorTypeMessage);
		}
		String format = null;
		// if we have fixed value then everything is with fixed length
		if (fixedValue) {
			format = "\\d'{'{0}'}'\\.\\d'{'{1}'}'";
			message = "";
		} else {
			format = "\\d'{'1,{0}'}'(?:\\.\\d'{'1,{1}'}')?";
			message = "";
		}
		String result = MessageFormat.format(format, Integer.valueOf(digitsTotal - digitsFraction),
				digits[2]);
		if (type.startsWith("nn")) {
			result = "-?" + result;
		}
		return new Pair<String, String>(result, MessageFormat.format(message,
				(digitsTotal - digitsFraction), digits[2]));
	}

	/**
	 * Gets regular expression for days or hours type.
	 * 
	 * @param type
	 *            the type of the field
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getDaysOrHoursType(String type) {
		String regex = null;
		String message = NOT_DEFINED_VALIDATOR_MESSAGE + type;
		if (type.endsWith("c")) {
			regex = "(H\\d{2}|D[012]\\d|D30)";
		} else {
			regex = "(D|H)\\d{2}";
		}
		return new Pair<String, String>(regex, message);
	}

	/**
	 * Gets regular expression for boolean.
	 * 
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getBoolean() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<String, String>("(true|false)", message);
	}

	/**
	 * Gets regular expression for time.
	 * 
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getTime() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<String, String>("((1|0)\\d|2[0-3]):[0-5]\\d", message);
	}

	/**
	 * Gets regular expression for text.
	 * 
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getText() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<String, String>("[\\w\\W]+", message);
	}

	/**
	 * Gets regular expression for date.
	 * 
	 * @return a Pair with generated regex and validation failior message.
	 */
	private Pair<String, String> getDate() {
		String message = NOT_DEFINED_VALIDATOR_MESSAGE;
		return new Pair<String, String>(
				"\\d{2}\\.\\d{2}\\.\\d{4}(?<=((([012]\\d|3[01])\\.(0[13578]|1[02])|([012]\\d|30)\\.(0[469]|11))\\.\\d{4}|(([01]\\d|2[0-9])\\.02\\.\\d{4}(?<=(([02468][048]|[13579][26])00|\\d{2}([02468][048]|[13579][26])(?<!\\d{2}00)))|([01]\\d|2[0-8])\\.02\\.\\d{4}(?<!(([02468][048]|[13579][26])00|\\d{2}([02468][048]|[13579][26])(?<!\\d{2}00))))))",
				message);
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
		Matcher matcher = null;
		if (StringUtils.isNotNullOrEmpty(fieldPattern)
				&& !fieldPattern.startsWith(RNC_PREFIX_FUNCTION)) {
			try {
				Pattern.compile(fieldPattern);
				String message = NOT_DEFINED_VALIDATOR_MESSAGE;
				return new Pair<String, String>(fieldPattern, message);
			} catch (PatternSyntaxException patternException) {
				logger.warn("RegExGenerator: Not valid pattern. ", patternException);
			}
		}
		matcher = RegExGenerator.DAYS_HOURS_TYPE.matcher(type);
		if (matcher.matches()) {
			return getDaysOrHoursType(type);
		}
		matcher = RegExGenerator.NONDIGIT_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getNondigitRegEx(type);
		}
		matcher = RegExGenerator.ALL_SYMBOL_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getAllSymbolRegEx(type);
		}
		matcher = RegExGenerator.FLOATING_POINT_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getFloatingPointRegEx(type);
		}
		matcher = RegExGenerator.DIGIT_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getDigitRegEx(type);
		}
		matcher = RegExGenerator.BOOLEAN_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getBoolean();
		}
		matcher = RegExGenerator.TIME_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getTime();
		}
		matcher = RegExGenerator.DATE_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getDate();
		}
		matcher = RegExGenerator.TEXT_REG_EX.matcher(type);
		if (matcher.matches()) {
			return getText();
		}

		return new Pair<String, String>(RegExGenerator.INVALID_TYPE, invalidValidatorTypeMessage);
	}

}