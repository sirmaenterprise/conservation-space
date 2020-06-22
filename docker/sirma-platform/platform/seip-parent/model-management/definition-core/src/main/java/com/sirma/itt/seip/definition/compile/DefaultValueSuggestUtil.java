package com.sirma.itt.seip.definition.compile;

import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.domain.definition.ControlParam;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class that is used to parse template strings and create control-params from them.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 30/08/2017
 */
class DefaultValueSuggestUtil {
	private static final Pattern PROPERTIES_PATTERN = Pattern.compile("\\$\\[(.*?)]");

	/**
	 * Prevent instantiation.
	 */
	private DefaultValueSuggestUtil() {
	}

	/**
	 * Constructs a list of {@link ControlParam} objects for functions from a default_value_suggest string.
	 *
	 * @param value
	 * 		the default value suggest template.
	 * @return list of control params.
	 */
	public static List<ControlParam> constructFunctionControlParams(String value) {
		return getBalancedSubstrings(value, '{', '}', false).stream().map(functionExpression -> {
			ControlParamImpl param = new ControlParamImpl();
			param.setIdentifier("function");
			param.setType(DefinitionCompilerHelper.DEFAULT_VALUE_PATTERN_TYPE);
			String[] split = functionExpression.split("\\|");
			// if expression is not correct we return what we have. The expression validation is done before that so
			// that the user is notified already.
			if (split.length < 2) {
				param.setValue("");
				param.setName("");
				return param;
			}
			param.setValue(split[0]);
			param.setName(split[1]);
			return param;
		}).collect(Collectors.toList());
	}

	/**
	 * Constructs a list of {@link ControlParam} for property bindings.
	 *
	 * @param value
	 * 		the default value suggest template.
	 * @return list of control params.
	 */
	public static List<ControlParam> constructPropertyBindings(String value) {
		List<ControlParam> propertyControlParams = new ArrayList<>();

		Matcher matcherProperties = PROPERTIES_PATTERN.matcher(value);
		while (matcherProperties.find()) {
			ControlParamImpl param = new ControlParamImpl();
			param.setIdentifier("propertyNameBinding");
			param.setType(DefinitionCompilerHelper.DEFAULT_VALUE_PATTERN_TYPE);
			param.setName(matcherProperties.group(1));
			param.setValue("");
			propertyControlParams.add(param);
		}
		return propertyControlParams;
	}

	/**
	 * Gets a balanced substring between two symbols. This can be used to extract the most outer part of strings with
	 * nested brackets, which cannot be done using regular expressions. For example running this method with text
	 * "some text (text(text(text))) more text"  and start and end marker equal to "(" and ")", will return
	 * (text(text(text))).
	 *
	 * @param string
	 * 		the input string
	 * @param startMark
	 * 		the starting marker.
	 * @param endMark
	 * 		the end marker.
	 * @param shouldIncludeMarks
	 * 		variable used to determine if we should include the start and end mark in the substring.
	 * @return list of strings.
	 */
	private static List<String> getBalancedSubstrings(String string, Character startMark, Character endMark,
			Boolean shouldIncludeMarks) {
		List<String> subTreeList = new ArrayList<>();
		int level = 0;
		int lastOpenDelimiter = -1;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == startMark) {
				level++;
				if (level == 1) {
					lastOpenDelimiter = (shouldIncludeMarks ? i : i + 1);
				}
			} else if (c == endMark) {
				if (level == 1) {
					subTreeList.add(string.substring(lastOpenDelimiter, (shouldIncludeMarks ? i + 1 : i)));
				}
				level--;
			}
		}
		return subTreeList;
	}
}