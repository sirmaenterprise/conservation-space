/**
 * Copyright (c) 2010 10.08.2010 , Sirma ITT.
 */
package com.sirma.codelist.constants;


/**
 * Enumeration of possible values for codelist status.
 * 
 * @author B.Bonev
 */
public enum ClStatusCode implements EnumValue<String> {
	/**
	 * Created record
	 */
	CREATED("C"),
	/**
	 * Updated record
	 */
	UPDATED("U"),
	/**
	 * INvalidated record
	 */
	INACTIVE("I");

	private final String value;

	/**
	 * Initialize the enum value
	 * 
	 * @param value
	 *            is the code value of the enum
	 */
	private ClStatusCode(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

}
