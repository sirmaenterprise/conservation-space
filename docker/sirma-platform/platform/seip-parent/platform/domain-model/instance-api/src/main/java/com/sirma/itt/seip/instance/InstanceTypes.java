package com.sirma.itt.seip.instance;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * Service to type verifications of instances.
 *
 * @author BBonev
 */
public interface InstanceTypes {

	/**
	 * Gets an {@link InstanceType} from the given id. The argument could be a full/short URI of the semantic class or
	 * an instance URI. Note that if instance id is passed this may trigger instance loading so instead the
	 * {@link InstanceTypeResolver} could be used to load instance reference that will have a type also.
	 *
	 * @param id
	 *            the semantic class name or instance id
	 * @return the optional that may contains a {@link InstanceType} that corresponds to the given id
	 */
	Optional<InstanceType> from(Serializable id);

	/**
	 * Gets the type for the given instance. The implementation may update the instance type if not present. If the
	 * instance does not have any indication that semantic type is an instance resolving may be performed.
	 *
	 * @param instance
	 *            the instance from which the type should be fetched
	 * @return the optional that may contains a {@link InstanceType} that corresponds to the given id
	 */
	Optional<InstanceType> from(Instance instance);

	/**
	 * Gets the type for the given instance reference. The implementation may update the reference type if not present.
	 * This method will may trigger instance resolving.
	 *
	 * @param reference
	 *            the instance reference from which the type should be fetched
	 * @return the optional that may contains a {@link InstanceType} that corresponds to the given id
	 */
	Optional<InstanceType> from(InstanceReference reference);

	/**
	 * Resolve the instance type that corresponds to the given {@link DefinitionModel}.
	 *
	 * @param definitionModel
	 *            the definition model
	 * @return the optional that may contains a {@link InstanceType} that corresponds to the given definition
	 */
	Optional<InstanceType> from(DefinitionModel definitionModel);

	/**
	 * Get all known instance types for the given category
	 *
	 * @param category
	 *            the category to resolve the types for
	 * @return the collection of all known types for the category or empty collection if category is <code>null</code>
	 *         or not valid
	 */
	Collection<InstanceType> forCategory(String category);

	/**
	 * Resolve types for for all instances present in the collection. The instances should be updated with the resolved
	 * types. Note that this may trigger instance resolving if the given instances does not have identified semantic
	 * type.
	 *
	 * @param instances
	 *            the instances to resolve the types for
	 */
	void resolveTypes(Collection<? extends Instance> instances);

	/**
	 * Checks if the instance of the given type.
	 *
	 * @param instance
	 *            the instance
	 * @param type
	 *            the type
	 * @return true, if the instance is of the given type and <code>false</code> if not or any of the arguments is
	 *         <code>null</code>.
	 */
	default boolean is(Instance instance, Serializable type) {
		if (instance == null || type == null) {
			return false;
		}
		return is(instance.toReference(), type);
	}

	/**
	 * Checks if the given instance reference is of the given type.
	 *
	 * @param reference
	 *            the reference
	 * @param type
	 *            the type
	 * @return true, if the instance pointed by the reference is of the given type and <code>false</code> if not or any
	 *         of the arguments is <code>null</code>.
	 */
	boolean is(InstanceReference reference, Serializable type);

	/**
	 * Checks if the given instance has the given trait.
	 *
	 * @param instance
	 *            the instance
	 * @param trait
	 *            the trait
	 * @return true, if the instance has the given trait and <code>false</code> if not or any of arguments is
	 *         <code>null</code>.
	 */
	default boolean hasTrait(Instance instance, Serializable trait) {
		if (instance == null || trait == null) {
			return false;
		}
		return hasTrait(instance.toReference(), trait);
	}

	/**
	 * Checks if the instance pointed by the given reference has the given trait.
	 *
	 * @param reference
	 *            the reference
	 * @param trait
	 *            the trait
	 * @return true, if the instance has the given trait anf <code>false</code> if not or any of the arguments is
	 *         <code>null</code>.
	 */
	boolean hasTrait(InstanceReference reference, Serializable trait);
}
