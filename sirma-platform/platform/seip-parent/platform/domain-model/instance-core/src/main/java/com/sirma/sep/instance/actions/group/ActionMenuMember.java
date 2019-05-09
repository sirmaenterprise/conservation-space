package com.sirma.sep.instance.actions.group;

import javax.json.JsonObjectBuilder;

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
	String getParent();

	/**
	 * Getter for identifier.
	 *
	 * @return identifier
	 */
	String getIdentifier();

	/**
	 * Order getter.
	 *
	 * @return order
	 */
	Integer getOrder();

	/**
	 * Getter for helper object used to build {@JsonObject}.
	 *
	 * @return helper object
	 */
	JsonObjectBuilder toJsonHelper();
}
