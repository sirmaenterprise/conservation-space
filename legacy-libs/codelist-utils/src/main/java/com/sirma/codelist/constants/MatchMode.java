package com.sirma.codelist.constants;

/**
 * Enum containing <code>LIKE</code> statement match modes.
 * 
 * @author Yasen Terziivanov.
 */
public enum MatchMode {

	/**
	 * Match the beginning of a string.
	 */
	START,
	/**
	 * Match the end of a string.
	 */
	END,
	/**
	 * Exact match.
	 */
	EXACT,
	/**
	 * Matches a word in a string.
	 */
	MATCH_EXACT_WORD,
	/**
	 * Match anywhere in the string.
	 */
	ANYWHERE,
	/**
	 * Match the start of any word in a string
	 * 
	 * <pre>
	 * e.g. if the pattern is "aaa" the
	 * strings "aaabbb" and "bbb aaa" will be matched.
	 * </pre>
	 */
	START_ANY_WORD;

}
