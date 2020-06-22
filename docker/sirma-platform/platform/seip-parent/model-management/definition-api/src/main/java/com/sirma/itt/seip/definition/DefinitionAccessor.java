package com.sirma.itt.seip.definition;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Defines common means of accessing different definitions and working with them.
 *
 * @author BBonev
 */
public interface DefinitionAccessor {

	/**
	 * Gets the supported objects by this accessor. The method should return the main definition interface that
	 * represents the current accessor and all instance classes that are created based on the supported definitions.
	 * This includes all instances that can be handled by the method {@link #getDefinition(Instance)}.
	 *
	 * @return the supported definition
	 */
	Set<Class<?>> getSupportedObjects();

	/**
	 * Gets the all definitions.
	 *
	 * @param <E>
	 *            the definition type
	 * @return the all definitions
	 */
	<E extends DefinitionModel> List<E> getAllDefinitions();

	/**
	 * Gets the highest revision for the given case definition id.
	 *
	 * @param defId
	 *            the def id
	 * @param <E>
	 *            the definition type
	 * @return the case definition
	 */
	<E extends DefinitionModel> E getDefinition(String defId);

	/**
	 * Gets the case definition by ID and revision.
	 *
	 * @param defId
	 *            the def id
	 * @param version
	 *            the version
	 * @param <E>
	 *            the definition type
	 * @return the case definition
	 */
	<E extends DefinitionModel> E getDefinition(String defId, Long version);

	/**
	 * Gets the definition for the given instance.
	 *
	 * @param <E>
	 *            the definition type
	 * @param instance
	 *            the instance
	 * @return the definition
	 */
	<E extends DefinitionModel> E getDefinition(Instance instance);

	/**
	 * Gets the default definition id for the given object.
	 *
	 * @param target
	 *            the target object used for determining the default definition for
	 * @return the default definition id or <code>null</code> if no such exists
	 */
	String getDefaultDefinitionId(Object target);

	/**
	 * Saves the given definition and updates the internal caches if any.
	 *
	 * @param <E>
	 *            the definition type
	 * @param definition
	 *            the definition
	 * @return the updated definition instance
	 */
	<E extends TopLevelDefinition> E saveDefinition(E definition);

	/**
	 * Removes the definition from database and caches. If the version is positive number then only the given number
	 * will be removed. If the version is less then 0 then all versions of the given definitions will be removed
	 *
	 * @param definition
	 *            the definition id to delete
	 * @param version
	 *            the version of the definition to delete. If -1 all version will be deleted
	 * @param mode
	 *            defines what definitions to delete
	 * @return a collection of deleted definitions info. If empty collection then nothig was deleted
	 */
	Collection<DeletedDefinitionInfo> removeDefinition(String definition, long version, DefinitionDeleteMode mode);

	/**
	 * Compute hash for the supported definition model.
	 *
	 * @param model
	 *            the model
	 * @return the int
	 */
	int computeHash(DefinitionModel model);

	/**
	 * Specifies the possible modes for definition deletion.
	 */
	enum DefinitionDeleteMode {
		ALL, SINGLE_REVISION, OLD_REVISIONS, LAST_REVISION;
	}
}
