package com.sirma.itt.seip.eai.cs.model.internal;

import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;

/**
 * The {@link ResolvableInstance} implementation for CS holding the namespace as well.
 */
public class CSResolvableInstance implements ResolvableInstance {
	private String namespace;
	private CSExternalInstanceId externalId;

	/**
	 * Instantiates a new CS resolvable instance.
	 *
	 * @param namespace
	 *            the namespace
	 * @param externalId
	 *            the external id
	 */
	public CSResolvableInstance(String namespace, CSExternalInstanceId externalId) {
		this.namespace = namespace;
		this.externalId = externalId;
	}

	/**
	 * Gets the namespace the instance is related to.
	 *
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	@Override
	public CSExternalInstanceId getExternalIdentifier() {
		return externalId;
	}

	@Override
	public String toString() {
		return getExternalIdentifier() + "@" + getNamespace();
	}

}
