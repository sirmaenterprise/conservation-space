package com.sirma.itt.seip.instance.validator.validators;

/**
 * List of constants for the different types in the definitions models according to which we have to validate.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public enum ValidationTypes {

	ANY("any"), URI("uri"), BOOLEAN("boolean"), TEXT("text"), LONG("long"), FLOAT("float"), DOUBLE("double"), DATE_TIME(
			"datetime"), INT("int"), DATE("date");

	private final String text;

	ValidationTypes(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
