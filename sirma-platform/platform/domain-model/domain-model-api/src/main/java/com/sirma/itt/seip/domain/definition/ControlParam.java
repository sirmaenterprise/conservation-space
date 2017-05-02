package com.sirma.itt.seip.domain.definition;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.function.Predicate;

import com.sirma.itt.seip.domain.PathElement;

/**
 * The Interface ControlParam.
 *
 * @author BBonev
 */
public interface ControlParam extends PathElement {

	/**
	 * Filter that selects a control by it's name.
	 *
	 * @param name
	 *            the name to match.
	 * @return the predicate that selects a control that matches the given name.
	 * @see #getName()
	 */
	static Predicate<ControlParam> byName(String name) {
		return property -> nullSafeEquals(property.getName(), name, true);
	}

	/**
	 * Gets the value of the value property.
	 *
	 * @return possible object is {@link String }
	 */
	String getValue();

	/**
	 * Gets the value of the name property.
	 *
	 * @return possible object is {@link String }
	 */
	String getName();

	/**
	 * Getter method for controlDefinition.
	 *
	 * @return the controlDefinition
	 */
	ControlDefinition getControlDefinition();

}