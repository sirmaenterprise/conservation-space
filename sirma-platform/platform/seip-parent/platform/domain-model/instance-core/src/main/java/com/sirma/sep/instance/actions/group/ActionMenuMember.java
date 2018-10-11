package com.sirma.sep.instance.actions.group;

/**
 * Interface for action menu objects. Implements adapter design pattern.
 *
 * @author T. Dossev
 */
public interface ActionMenuMember {

	/**
	 * Getter for parent.
	 *
	 * @return parent
	 */
	public String getParent();

	/**
	 * Getter for identifier.
	 *
	 * @return identifier
	 */
	public String getIdentifier();

	/**
	 * Order getter.
	 *
	 * @return order
	 */
	public Integer getOrder();

	/**
	 * Getter for helper object used to build {@JsonObject}. Action instance returns builded immutable JsonObject.
	 * TransitionGroupDefinition object returns a Map of properties to build one.
	 *
	 * @return helper object
	 */
	public Object toJsonHelper();
}
