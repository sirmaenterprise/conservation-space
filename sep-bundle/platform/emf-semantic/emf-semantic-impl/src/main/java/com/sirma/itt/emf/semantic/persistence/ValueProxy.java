package com.sirma.itt.emf.semantic.persistence;

import org.openrdf.model.Value;

/**
 * Proxy of {@link Value} instance. Used when loading data from semantic database. <br>
 * The proxy guarantees unique identity based on the value returned from {@link Value#stringValue()}
 * for different {@link Value} implementations when returned by semantic query. So that it can be
 * used in maps or sets.
 * 
 * @author BBonev
 */
class ValueProxy implements Value {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2028139005874885237L;
	/** The value. */
	private Value value;

	/**
	 * Instantiates a new value proxy.
	 *
	 * @param value
	 *            the value
	 */
	public ValueProxy(Value value) {
		this.value = value;
	}

	/**
	 * String value.
	 *
	 * @return the string
	 */
	@Override
	public String stringValue() {
		return value.stringValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((value == null) ? 0 : value.stringValue().hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ValueProxy other = (ValueProxy) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.stringValue().equals(other.value.stringValue())) {
			return false;
		}
		return true;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return value.toString();
	}

	/**
	 * Getter method for value.
	 *
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}
}