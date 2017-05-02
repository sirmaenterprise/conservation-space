package com.sirma.itt.seip;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The Class Quad.
 *
 * @param <A>
 *            the generic type
 * @param <B>
 *            the generic type
 * @param <C>
 *            the generic type
 * @param <D>
 *            the generic type
 * @author BBonev
 */
public class Quad<A, B, C, D> extends Triplet<A, B, C> {
	private static final long serialVersionUID = 2258558748257315378L;
	/** The forth. */
	private D forth;

	/**
	 * Instantiates a new quad.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @param third
	 *            the third
	 * @param forth
	 *            the forth
	 */
	public Quad(A first, B second, C third, D forth) {
		super(first, second, third);
		this.setForth(forth);
	}

	/**
	 * Getter method for forth.
	 *
	 * @return the forth
	 */
	public D getForth() {
		return forth;
	}

	/**
	 * Setter method for forth.
	 *
	 * @param forth
	 *            the forth to set
	 */
	public void setForth(D forth) {
		this.forth = forth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (forth == null ? 0 : forth.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Quad)) {
			return false;
		}
		Quad<?, ?, ?, ?> other = (Quad<?, ?, ?, ?>) obj;
		return EqualsHelper.nullSafeEquals(forth, other.forth);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "(" + getFirst() + ", " + getSecond() + ", " + getThird() + ", " + forth + ")";
	}

}
