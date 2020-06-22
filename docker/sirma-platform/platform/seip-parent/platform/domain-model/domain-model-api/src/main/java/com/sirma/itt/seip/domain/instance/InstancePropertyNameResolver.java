package com.sirma.itt.seip.domain.instance;

import java.util.function.Function;

import com.sirma.itt.seip.annotation.NoOperation;

/**
 * Special resolver that resolves the instance property name for the given URI. It can be used to safely convert the
 * uris to definition property names.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/07/2018
 */
public interface InstancePropertyNameResolver {

	/**
	 * Converter instances that returns the passed field value as is
	 */
	InstancePropertyNameResolver NO_OP_INSTANCE = new PassThroughFieldConverter();

	/**
	 * Resolve the property name for the uri that is part of the given instance.
	 *
	 * @param instance the requesting instance
	 * @param fieldUri the property that need to be resolved.
	 * @return the resolved URI.
	 */
	String resolve(Instance instance, String fieldUri);

	/**
	 * Creates and returns a resolver function that is capable of converting series of properties belonging to
	 * particular instance.
	 *
	 * @param instance the instance that is going to be modified or accessed and need to be used for definition resolving
	 * @return a function capable of resolving property names
	 */
	default Function<String, String> resolverFor(Instance instance) {
		return fieldUri -> resolve(instance, fieldUri);
	}

	/**
	 * Some default non injectable implementation that pass the argument as a result
	 *
	 * @author BBonev
	 */
	@NoOperation
	class PassThroughFieldConverter implements InstancePropertyNameResolver {

		@Override
		public String resolve(Instance instance, String fieldUri) {
			return fieldUri;
		}
	}
}
