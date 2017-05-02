package com.sirma.itt.seip;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The Class Triplet.
 *
 * @param <X>
 *            the generic type
 * @param <Y>
 *            the generic type
 * @param <Z>
 *            the generic type
 * @author BBonev
 */
public class Triplet<X, Y, Z> extends Pair<X, Y> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8920623968958176538L;

	/** The third. */
	private Z third;

	/**
	 * Instantiates a new triplet.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @param third
	 *            the third
	 */
	public Triplet(X first, Y second, Z third) {
		super(first, second);
		this.third = third;
	}

	/**
	 * Getter method for third.
	 *
	 * @return the third
	 */
	public Z getThird() {
		return third;
	}

	/**
	 * Setter method for third.
	 *
	 * @param third
	 *            the third to set
	 */
	public void setThird(Z third) {
		this.third = third;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (third == null ? 0 : third.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Triplet<?, ?, ?>)) {
			return false;
		}
		Triplet<?, ?, ?> o = (Triplet<?, ?, ?>) other;
		return super.equals(o) && EqualsHelper.nullSafeEquals(this.third, o.third);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "(" + getFirst() + ", " + getSecond() + ", " + third + ")";
	}

}
