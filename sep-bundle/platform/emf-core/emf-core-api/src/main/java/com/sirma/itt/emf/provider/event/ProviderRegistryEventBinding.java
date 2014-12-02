package com.sirma.itt.emf.provider.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class ProviderRegistryEventBinding.
 *
 * @author BBonev
 */
public final class ProviderRegistryEventBinding extends AnnotationLiteral<ProviderRegistryEvent>
		implements ProviderRegistryEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7409462962067420657L;

	/** The value. */
	private final String value;

	/**
	 * Instantiates a new provider registry event binding.
	 * 
	 * @param value
	 *            the value
	 */
	public ProviderRegistryEventBinding(String value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value() {
		return value;
	}

}
