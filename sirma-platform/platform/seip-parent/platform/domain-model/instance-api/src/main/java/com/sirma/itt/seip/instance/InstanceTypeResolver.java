package com.sirma.itt.seip.instance;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides methods for resolving instance types.
 *
 * @author BBonev
 */
public interface InstanceTypeResolver extends Plugin {

	/** Plugin name. */
	String TARGET_NAME = "instanceTypeResolver";

	String RDF_TYPE = "rdfType";

	/**
	 * Resolve instance data type based on the given id.
	 *
	 * @param id the instance id
	 * @return the data type definition or empty optional if the instance is not found/deleted/or id is
	 * <code>null</code>
	 */
	Optional<InstanceType> resolve(Serializable id);

	/**
	 * Resolve instance reference for the given instance id.
	 *
	 * @param id the instance id
	 * @return the instance reference or empty optional if the instance is not found/deleted or id is <code>null</code>
	 */
	Optional<InstanceReference> resolveReference(Serializable id);

	/**
	 * Resolve the data types for the given ids and returns a mapping instance id - type. If any of the instances is not
	 * found or is deleted it will not be present in the result.
	 *
	 * @param ids the instance ids
	 * @return a mapping of instance id and their type.
	 */
	Map<Serializable, InstanceType> resolve(Collection<Serializable> ids);

	/**
	 * Resolve instance references for the given instance ids. The result collection will contain only references to
	 * instances that are found.
	 *
	 * @param <S> instance id type
	 * @param ids the instance ids
	 * @return the collection of instance references.
	 */
	<S extends Serializable> Collection<InstanceReference> resolveReferences(Collection<S> ids);

	/**
	 * Resolve instances for the given instance ids. The result collection will contain only instances to instances that
	 * are found and the user has required permissions to access.
	 *
	 * @param <S> instance id type
	 * @param ids the instance ids
	 * @return the collection of instance references.
	 */
	<S extends Serializable> Collection<Instance> resolveInstances(Collection<S> ids);

	/**
	 * Checks if the given instances exists and is not deleted
	 *
	 * @param <S> instance id type
	 * @param identifiers the instance ids to check
	 * @return a mapping indicating if instance exists or not
	 */
	<S extends Serializable> Map<S, Boolean> exist(Collection<S> identifiers);
}
