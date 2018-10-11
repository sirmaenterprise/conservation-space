package com.sirma.itt.emf.semantic.persistence;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 * Proxy of {@link Value} instance. Used when loading data from semantic database. <br>
 * The proxy guarantees unique identity based on the value returned from {@link Value#stringValue()} for different
 * {@link Value} implementations when returned by semantic query. So that it can be used in maps or sets.
 *
 * @author BBonev
 */
class URIProxy extends ValueProxy<IRI>implements IRI {

	private static final long serialVersionUID = 275652765882520874L;

	/**
	 * Instantiates a new value proxy.
	 *
	 * @param value
	 *            the value
	 */
	public URIProxy(IRI value) {
		super(value);
	}

	/**
	 * String value.
	 *
	 * @return the string
	 */
	@Override
	public String stringValue() {
		return getValue().stringValue();
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return getValue().toString();
	}

	@Override
	public String getNamespace() {
		return getValue().getNamespace();
	}

	@Override
	public String getLocalName() {
		return getValue().getLocalName();
	}
}