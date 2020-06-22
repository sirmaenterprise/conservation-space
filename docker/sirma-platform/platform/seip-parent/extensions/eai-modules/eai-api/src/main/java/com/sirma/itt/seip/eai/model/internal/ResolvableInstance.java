package com.sirma.itt.seip.eai.model.internal;

/**
 * The {@link ResolvableInstance} represent an instance that should be resolved by external service integration.
 */
@FunctionalInterface
public interface ResolvableInstance {

	/**
	 * Gets the external identifier.
	 *
	 * @return the external identifier
	 */
	public ExternalInstanceIdentifier getExternalIdentifier();

}
