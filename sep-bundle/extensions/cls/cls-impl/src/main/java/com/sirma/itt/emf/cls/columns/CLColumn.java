package com.sirma.itt.emf.cls.columns;

/**
 * Enumerable containing specific values for validating and persisting an excel file with code lists
 * and values.
 * 
 * @author Nikolay Velkov
 */
public enum CLColumn {

	/** Column for tenant id. */
	TENANT_ID(0, "\\d", "Tenant id"),

	/** Column for the value. */
	VALUE(1, "[a-zA-Z0-9_/\\-\\.]{1,40}", "Code"),

	/**
	 * Column for English description.
	 */
	DESCREN(2, "[\\s\\S.]*", "descrEn"),

	/**
	 * Column for BG description.
	 */
	DESCRBG(3, "[\\s\\S.]*", "descrBg"),

	/**
	 * Column for English comment.
	 */
	COMMENTEN(4, "[\\s\\S.]*", "CommentEn"),

	/**
	 * Column for bulgarian comment.
	 */
	COMMENTBG(5, "[\\s\\S.]*", "CommentBg"),

	/**
	 * Column for display type.
	 */
	DISPLAY_TYPE(6, "[123]?", "display type"),

	/**
	 * Column for value order.
	 */
	ORDER(7, "\\d?", "order"),

	/**
	 * Column for master code list.
	 */
	SORT_BY(8, "\\d?", "sort by"),

	/**
	 * Column for master code list.
	 */
	MASTERCL(9, "[a-zA-Z0-9_/\\-\\.\\,\\s]*", "masterCl/value"),

	/**
	 * Column for the first extra.
	 */
	EXTRA1(10, ".*", "extra1"),

	/**
	 * Column for the second extra.
	 */
	EXTRA2(11, ".*", "extra2"),

	/**
	 * Column for the third extra.
	 */
	EXTRA3(12, ".*", "extra3"),

	/**
	 * Column for the fourth extra.
	 */
	EXTRA4(13, ".*", "extra4"),

	/**
	 * Column for the fifth extra.
	 */
	EXTRA5(14, ".*", "extra5"),

	/** Column for the valid from date. */
	VALID_FROM(15, "^(\\d\\.){2}\\d{4}$", "Valid from"),

	/** Column for the valid to date. */
	VALID_TO(16, "^(\\d\\.){2}\\d{4}$", "Valid to");

	/** The column. */
	private final int column;

	/** Column validation pattern. */
	private final String pattern;

	/** The name. */
	private final String name;

	/**
	 * Private constructor.
	 * 
	 * @param column
	 *            is the excel column index
	 * @param pattern
	 *            is the validation pattern
	 * @param name
	 *            the name
	 */
	private CLColumn(int column, String pattern, String name) {
		this.column = column;
		this.pattern = pattern;
		this.name = name;
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
	 * Getter of column field.
	 * 
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
