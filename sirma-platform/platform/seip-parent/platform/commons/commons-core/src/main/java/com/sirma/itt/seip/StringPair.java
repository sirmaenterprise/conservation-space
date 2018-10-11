/**
 * Copyright (c) 2013 23.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip;

/**
 * Represents a pair of strings.
 *
 * @author Adrian Mitev
 */
public class StringPair extends Pair<String, String> {

	private static final long serialVersionUID = -8958388257400205291L;

	/** {@link StringPair} that contains nulls */
	public static final StringPair EMPTY_PAIR = new StringPair(null, null);

	/**
	 * Instantiates a new string pair.
	 */
	public StringPair() {
		super();
	}

	/**
	 * Initializes properties.
	 *
	 * @param first
	 *            first string.
	 * @param second
	 *            second string.
	 */
	public StringPair(String first, String second) {
		super(first, second);
	}

	/**
	 * Instantiates a new string pair by copying the data from other pair
	 *
	 * @param
	 * 			<P>
	 *            the generic type
	 * @param copyFrom
	 *            the copy from
	 */
	public <P extends Pair<? extends String, ? extends String>> StringPair(P copyFrom) {
		super(copyFrom);
	}
}
