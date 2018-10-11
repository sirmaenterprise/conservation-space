package com.sirma.itt.seip.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.transaction.TransactionScoped;

import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Provides transactional caching capabilities. In order to minimize the resolving of definition models.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/07/2018
 */
@TransactionScoped
class InstancePropertyNameResolverCache implements Serializable {

	private Map<Serializable, DefinitionModel> instanceToModelMapping = new HashMap<>();

	/**
	 * Return the cached model for the given key or resolve new model using the provided supplier
	 *
	 * @param instanceId the instance id to use as a key
	 * @param modelSupplier the supplier that should be used to provide model for the given key if the key is not present in the cache.
	 * @return the found model
	 */
	DefinitionModel getOrResolveModel(Serializable instanceId, Supplier<DefinitionModel> modelSupplier) {
		if (instanceId == null) {
			return null;
		}
		return instanceToModelMapping.computeIfAbsent(instanceId, id -> modelSupplier.get());
	}
}
