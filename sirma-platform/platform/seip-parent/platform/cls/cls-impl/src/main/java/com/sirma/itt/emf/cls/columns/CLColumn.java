package com.sirma.itt.emf.cls.columns;

/**
 * Enumerable containing specific values for validating and persisting an excel file with code lists and values.
 *
 * @author Nikolay Velkov
 */
public enum CLColumn {

	/**
	 * Column for the Code list's code.
	 */
	CL_VALUE("[a-zA-Z0-9_/\\-\\.\\+]{1,40}", "Code", true),

	/**
	 * Column for the Code value's code.
	 */
	CV_VALUE("[a-zA-Z0-9_/\\-\\.\\+]{1,100}", "Code", true),

	/**
	 * Column for language
	 */
	LANGUAGE("[a-zA-Z]{2}", "Language", false),

	/**
	 * Column for description.
	 */
	DESCR("[\\s\\S.]*", "descr", false), // NOSONAR

	/**
	 * Column for comment.
	 */
	COMMENT("[\\s\\S.]*", "Comment", false),

	/**
	 * Column for the first extra.
	 */
	EXTRA1(".*", "extra1", false),

	/**
	 * Column for the second extra.
	 */
	EXTRA2(".*", "extra2", false),

	/**
	 * Column for the third extra.
	 */
	EXTRA3(".*", "extra3", false),

	/**
	 * Column for value's active state
	 */
	ACTIVE("(?i)^true|false$", "Active", false);

	/**
	 * Validation pattern.
	 */
	private final String pattern;

	/**
	 * The name.
	 */
	private final String name;

	/**
	 * Indicates if the column is mandatory
	 **/
	private final boolean mandatory;

	/**
	 * Private constructor.
	 *
	 * @param pattern
	 *            is the validation pattern
	 * @param name
	 *            the name
	 * @param mandatory
	 *            if the column is mandatory
	 */
	CLColumn(String pattern, String name, boolean mandatory) {
		this.pattern = pattern;
		this.name = name;
		this.mandatory = mandatory;
	}

	/**
	 * Getter method for pattern.
	 *
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter method for mandatory.
	 *
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	@Override
	public String toString() {
		return name;
	}
}
