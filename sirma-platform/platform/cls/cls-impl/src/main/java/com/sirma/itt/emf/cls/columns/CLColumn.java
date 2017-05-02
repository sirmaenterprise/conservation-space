package com.sirma.itt.emf.cls.columns;

import jxl.Cell;
import jxl.Sheet;

/**
 * Enumerable containing specific values for validating and persisting an excel file with code lists and values.
 *
 * @author Nikolay Velkov
 */
public enum CLColumn {

	/**
	 * Column for the Code list's code.
	 */
	CL_VALUE("[0-9]{1,40}", "Code", true),

	/**
	 * Column for the Code value's code.
	 */
	CV_VALUE("[a-zA-Z0-9_/\\-\\.\\+]{1,100}", "Code", true),

	/**
	 * Column for English description.
	 */
	DESCREN("[\\s\\S.]*", "descrEn", true),

	/**
	 * Column for English comment.
	 */
	COMMENTEN("[\\s\\S.]*", "CommentEn", true),

	/**
	 * Column for BG description.
	 */
	DESCRBG("[\\s\\S.]*", "descrBg", false),

	/**
	 * Column for bulgarian comment.
	 */
	COMMENTBG("[\\s\\S.]*", "CommentBg", false),

	/**
	 * Column for display type.
	 */
	DISPLAY_TYPE("[123]?", "display type", true),

	/**
	 * Column for value order.
	 */
	ORDER("\\d?", "order", true),

	/**
	 * Column for master code list.
	 */
	SORT_BY("\\d?", "sort by", true),

	/**
	 * Column for master code list.
	 */
	MASTERCL("[a-zA-Z0-9_/\\-\\.\\,\\s]*", "masterCl/value", true),

	/**
	 * Column for the first extra.
	 */
	EXTRA1(".*", "extra1", true),

	/**
	 * Column for the second extra.
	 */
	EXTRA2(".*", "extra2", true),

	/**
	 * Column for the third extra.
	 */
	EXTRA3(".*", "extra3", true),

	/**
	 * Column for the fourth extra.
	 */
	EXTRA4(".*", "extra4", true),

	/**
	 * Column for the fifth extra.
	 */
	EXTRA5(".*", "extra5", true),

	/** Column for the valid from date. */
	VALID_FROM("^(\\d\\.){2}\\d{4}$", "Valid from", true),

	/** Column for the valid to date. */
	VALID_TO("^(\\d\\.){2}\\d{4}$", "Valid to", true);

	/** Column validation pattern. */
	private final String pattern;

	/** The name. */
	private final String name;

	/** Indicates if the column is mandatory **/
	private final boolean mandatory;

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
	private CLColumn(String pattern, String name, boolean mandatory) {
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
	 * Getter of column field.
	 *
	 * @param sheet
	 *            is the sheet
	 * @return the column
	 */
	public int getColumn(Sheet sheet) {
		// dynamically find the index of the column, achieving
		// non-strict column order and avoiding hard-coding of the index.
		Cell[] headRow = sheet.getRow(0);
		for (int i = 0; i < headRow.length; i++) {
			if (headRow[i].getContents().equals(name)) {
				return i;
			}
		}
		return -1;
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
}
