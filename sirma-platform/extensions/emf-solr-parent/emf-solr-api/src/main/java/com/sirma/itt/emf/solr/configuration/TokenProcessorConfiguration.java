package com.sirma.itt.emf.solr.configuration;

import java.util.regex.Pattern;

/**
 * Wrapper for token processor configurations. Having the pattern, replaces the given source with the already set
 * replacement on match - use {@link #process(String)} for simplification
 * 
 * @author bbanchev
 */
public class TokenProcessorConfiguration {
	private Pattern pattern;
	private String replacement;

	/**
	 * Constructs new {@link TokenProcessorConfiguration} with the required values
	 * 
	 * @param pattern
	 *            the pattern to set
	 * @param replacement
	 *            the pattern matching replacement.
	 */
	public TokenProcessorConfiguration(Pattern pattern, String replacement) {
		this.pattern = pattern;
		this.replacement = replacement;
	}

	/**
	 * Gets the pattern to match in token
	 * 
	 * @return the pattern value
	 */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Gets the replacement for matched pattern
	 * 
	 * @return the replacement value
	 */
	public String getReplacement() {
		return replacement;
	}

	/**
	 * Having the matcher, replaces the given source with the already set replacement
	 * 
	 * @param source
	 *            the value to update
	 * @return the update value, possibly the same
	 */
	public String process(String source) {
		return pattern.matcher(source).replaceAll(replacement);
	}
}
