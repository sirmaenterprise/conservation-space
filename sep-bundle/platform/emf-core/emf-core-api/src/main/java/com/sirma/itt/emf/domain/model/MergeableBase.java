package com.sirma.itt.emf.domain.model;


/**
 * The Class MergeableBase.
 * 
 * @param <E>
 *            the element type
 * @author BBonev
 */
public abstract class MergeableBase<E extends MergeableBase<?>> implements Mergeable<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
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
		if (!(obj instanceof MergeableBase)) {
			return false;
		}
		MergeableBase<?> other = (MergeableBase<?>) obj;
		if (getIdentifier() == null) {
			if (other.getIdentifier() != null) {
				return false;
			}
		} else if (!getIdentifier().equals(other.getIdentifier())) {
			return false;
		}
		return true;
	}

}
