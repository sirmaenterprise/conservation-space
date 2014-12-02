/**
 * Copyright (c) 2013 23.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.domain;

/**
 * Represents a pair of strings.
 * 
 * @author Adrian Mitev
 */
// FIXME: Do we really need this class?!
public class StringPair extends Pair<String, String> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8958388257400205291L;

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

}
