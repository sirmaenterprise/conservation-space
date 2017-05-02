package com.sirma.itt.seip.definition;

/**
 * Provider for the inverse relation of the given relation. The inverse relation is controlled in the domain model. The
 * returned relation could be the same for symmetric relations or other for non symmetric or <code>null</code> if no
 * inverse relation is defined.
 */
@FunctionalInterface
public interface InverseRelationProvider {

	/**
	 * Returns the inverse relation of the given one if defined and the given argument points to a valid relation
	 *
	 * @param relationId
	 *            the relation id to look for
	 * @return the inverse relation if or <code>null</code> if no such is defined
	 */
	String inverseOf(String relationId);
}