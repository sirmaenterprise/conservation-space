package com.sirma.itt.seip.instance.location;

import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.relation.LinkConstants;

/**
 * Defines methods for processing default location for instances created with specific definition. There are methods for
 * binding specific definition(by id) to specific instance, which will be default location for the other instance
 * created with this definition. Also contains methods for removing, updating and retrieving the locations for the
 * definitions.
 *
 * @author A. Kunchev
 */
public interface InstanceDefaultLocationService {

	/**
	 * Adds default location to given definition. The definition and the instance location are passed as map, where the
	 * definition is passed as key and the location for it as value. Mostly they are binded with link
	 * {@link LinkConstants#IS_DEFAULT_LOCATION} in the semantic DB.
	 *
	 * @param defaultLocations
	 *            map containing definition instance reference (definition) as key and instance reference (location) as
	 *            value
	 */
	void addDefaultLocations(Map<InstanceReference, InstanceReference> defaultLocations);

	/**
	 * Retrieves the possible instances, which could be parents for the instance that is created with the definition,
	 * which id is passed to the method.
	 *
	 * @param definitionId
	 *            the definition id for which will be returned default location, if there is any
	 * @return collection of instances, which can be parents\locations for this instances created with this definition
	 */
	Collection<? extends Instance> retrieveLocations(String definitionId);

	/**
	 * Retrieves all links {@link LinkConstants#IS_DEFAULT_LOCATION} for the given definition id.
	 *
	 * @param reference
	 *            the definition reference for which will be searched default location links
	 * @return collection of references with which the passed definition reference is linked or empty collection if
	 *         there are no links
	 */
	Collection<InstanceReference> retrieveOnlyDefaultLocations(InstanceReference reference);

	/**
	 * Updates the default locations for the given definitions and their corresponding instances. The passed map should
	 * contain the definition references and their locations that should be updated.
	 *
	 * @param defaultLocations
	 *            map containing definition instance reference (definition) as key and instance reference (location) as
	 *            value
	 */
	void updateDefaultLocations(Map<InstanceReference, InstanceReference> defaultLocations);

	/**
	 * Removes default locations for the given definition references. First extracts the bindings between the passed
	 * definition references and location instances and then removes this bindings. Mostly this bindings are link
	 * {@link LinkConstants#IS_DEFAULT_LOCATION} between the instance and the definition id. The remove is simply to
	 * unlink them.
	 *
	 * @param definitionsReferences
	 *            collecting of definition references for which will be removed default locations
	 */
	void removeDefaultLocations(Collection<InstanceReference> definitionsReferences);
}
