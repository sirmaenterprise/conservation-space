package com.sirma.itt.seip.rule.matchers;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.seip.context.Context;

/**
 * Base {@link RuleMatcher} that implements properly {@link #isApplicable(Context)} for the matcher purposes
 *
 * @author BBonev
 */
public abstract class BaseRuleMatcher extends BaseDynamicInstanceRule implements RuleMatcher {
	private static final Pattern SPLIT = Pattern.compile("\\s+");

	protected Boolean ignoreCase;
	protected Boolean containsMatch;
	protected Boolean exactMatch;
	protected Integer minimalLength;
	private Boolean invertMatch;

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			return false;
		}
		ignoreCase = configuration.getIfSameType(IGNORE_CASE, Boolean.class, Boolean.FALSE);
		containsMatch = configuration.getIfSameType(CONTAINS, Boolean.class, Boolean.FALSE);
		exactMatch = configuration.getIfSameType(EXACT_MATCH, Boolean.class, Boolean.FALSE);
		minimalLength = configuration.getIfSameType(MINIMAL_LENGTH, Integer.class, -1);
		invertMatch = configuration.getIfSameType(INVERT, Boolean.class, Boolean.FALSE);
		return true;
	}

	/**
	 * Return the result based on the invert matcher configuration.
	 *
	 * @param result
	 *            the result
	 * @return true, if successful
	 */
	protected boolean matcherResult(boolean result) {
		return invertMatch != result;
	}

	/**
	 * Prepare for matching.
	 *
	 * @param value
	 *            the value
	 * @param exact
	 *            the exact
	 * @return the that is ready for pattern building
	 */
	protected String prepareForMatching(Serializable value, boolean exact) {
		String stringValue = StringUtils.trimToEmpty(value.toString());
		StringBuilder builder = new StringBuilder();
		builder.append("\\b");
		if (exact) {
			builder.append(Pattern.quote(stringValue));
		} else {
			String[] split = SPLIT.split(stringValue);
			for (int i = 0; i < split.length; i++) {
				String part = split[i];
				builder.append(Pattern.quote(part));
				if (i + 1 < split.length) {
					builder.append("\\W+");
				}
			}
		}
		builder.append("\\b");
		return builder.toString();
	}

	/**
	 * Checks if is length allowed.
	 *
	 * @param string
	 *            the string
	 * @param length
	 *            the length
	 * @return true, if is length allowed
	 */
	protected boolean isLengthAllowed(String string, int length) {
		return length <= 0 || string.length() >= length;
	}

	/**
	 * Builds the pattern for the given string.
	 *
	 * @param input
	 *            the input
	 * @param exact
	 *            the exact
	 * @param shouldIgnoreCase
	 *            the should ignore case
	 * @return the pattern
	 */
	protected Pattern buildPattern(String input, boolean exact, boolean shouldIgnoreCase) {
		return Pattern.compile(prepareForMatching(input, exact), shouldIgnoreCase ? Pattern.CASE_INSENSITIVE : 0);
	}

	/**
	 * Builds the pattern for the given regex.
	 *
	 * @param pattern
	 *            the pattern to compile
	 * @param shouldIgnoreCase
	 *            the should ignore case
	 * @return the pattern
	 */
	protected Pattern buildPattern(String pattern, boolean shouldIgnoreCase) {
		return Pattern.compile(pattern, shouldIgnoreCase ? Pattern.CASE_INSENSITIVE : 0);
	}
}
