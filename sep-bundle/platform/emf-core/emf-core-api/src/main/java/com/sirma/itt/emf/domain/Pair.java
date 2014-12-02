package com.sirma.itt.emf.domain;

import java.io.Serializable;

import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Utility class for containing two things that aren't like each other.
 *
 * @param <F> the generic type
 * @param <S> the generic type
 * @author BBonev
 */
public class Pair<F, S> implements Serializable, Cloneable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7495037741259710709L;

	/** The Constant NULL_PAIR. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Pair NULL_PAIR = new Pair(null, null);

	/**
	 * Null pair.
	 *
	 * @param <X> the generic type
	 * @param <Y> the generic type
	 * @return the pair
	 */
	@SuppressWarnings("unchecked")
	public static final <X, Y> Pair<X, Y> nullPair() {
		return NULL_PAIR;
	}

	/**
	 * The first member of the pair.
	 */
	private F first;

	/**
	 * The second member of the pair.
	 */
	private S second;

	/**
	 * Make a new one.
	 * 
	 * @param first
	 *            The first member.
	 * @param second
	 *            The second member.
	 */
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Get the first member of the tuple.
	 * 
	 * @return The first member.
	 */
	public F getFirst() {
		return first;
	}

	/**
	 * Get the second member of the tuple.
	 * 
	 * @return The second member.
	 */
	public S getSecond() {
		return second;
	}

	/**
	 * Sets the first.
	 *
	 * @param first the new first
	 */
	public void setFirst(F first) {
		this.first = first;
	}

	/**
	 * Sets the second.
	 *
	 * @param second the new second
	 */
	public void setSecond(S second) {
		this.second = second;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if ((other == null) || !(other instanceof Pair<?, ?>)) {
			return false;
		}
		Pair<?, ?> o = (Pair<?, ?>) other;
		return EqualsHelper.nullSafeEquals(this.first, o.first)
				&& EqualsHelper.nullSafeEquals(this.second, o.second);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public int hashCode() {
		return (first == null ? 0 : first.hashCode()) + (second == null ? 0 : second.hashCode());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	@Override
	public Pair<F, S> clone() {
		return new Pair<F, S>(getFirst(), getSecond());
	}
}
