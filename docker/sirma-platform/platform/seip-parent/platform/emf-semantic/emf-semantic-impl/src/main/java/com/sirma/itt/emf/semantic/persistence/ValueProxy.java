package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

/**
 * Proxy of {@link Value} instance. Used when loading data from semantic database. <br>
 * The proxy guarantees unique identity based on the value returned from {@link Value#stringValue()} for different
 * {@link Value} implementations when returned by semantic query. So that it can be used in maps or sets.
 *
 * @author BBonev
 * @param <V>
 *            the value type
 */
public class ValueProxy<V extends Value> implements Value {

	private static final long serialVersionUID = -3846479676961349316L;

	private V value;

	/**
	 * Instantiates a new value proxy.
	 *
	 * @param value
	 *            the value
	 */
	public ValueProxy(V value) {
		this.value = value;
	}

	/**
	 * Create a value proxy for the given value.
	 *
	 * @param <V>
	 *            the value type
	 * @param value
	 *            the value
	 * @return the value proxy that wraps the given original value
	 */
	@SuppressWarnings("unchecked")
	public static <V extends Value> ValueProxy<V> of(Value value) {
		if (value == null) {
			return null;
		} else if (value instanceof ValueProxy<?>) {
			return (ValueProxy<V>) value;
		} else if (value instanceof IRI) {
			return (ValueProxy<V>) new URIProxy((IRI) value);
		} else if (value instanceof Literal) {
			return (ValueProxy<V>) new LiteralProxy((Literal) value);
		}
		return new ValueProxy<>((V) value);
	}

	/**
	 * Checks if the given value is instance of {@link ValueProxy} and if it is extracts the value from it otherwise
	 * returns the same value.
	 *
	 * @param <V>
	 *            the expected type
	 * @param value
	 *            the value to check
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	public static <V extends Value> V getValue(Value value) {
		if (value instanceof ValueProxy) {
			return (V) ((ValueProxy<?>) value).getValue();
		}
		return (V) value;
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

	@Override
	public int hashCode() {
		return value == null ? 0 : value.stringValue().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ValueProxy)) {
			if (obj instanceof Value) {
				return nullSafeEquals(getValue().stringValue(), ((Value) obj).stringValue());
			}
			return false;
		}
		return nullSafeEquals(getValue().stringValue(), ((ValueProxy<?>) obj).getValue().stringValue());
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
	public V getValue() {
		return value;
	}
}