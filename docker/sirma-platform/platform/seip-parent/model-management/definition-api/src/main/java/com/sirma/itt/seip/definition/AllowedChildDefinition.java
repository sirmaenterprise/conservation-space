package com.sirma.itt.seip.definition;

import java.util.List;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Allowed child id definitions for restricting children that can be added/started to/on the parent definition.
 */
public interface AllowedChildDefinition extends Identity {

	/**
	 * Gets the allowed child type.
	 *
	 * @return the type
	 */
	String getType();

	/**
	 * Checks if the current definition is default for the current type.
	 *
	 * @return true, if is default
	 */
	boolean isDefault();

	/**
	 * Gets the allowed and denied workflows.
	 *
	 * @return the permissions
	 */
	List<AllowedChildConfiguration> getPermissions();

	/**
	 * Gets property filters.
	 *
	 * @return the filters
	 */
	List<AllowedChildConfiguration> getFilters();

	/**
	 * Getter method for caseDefinition.
	 *
	 * @return the caseDefinition
	 */
	DefinitionModel getParentDefinition();

}
