package com.sirma.itt.seip.definition;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Base class for defining a {@link Mergeable} definitions. The class overrides the {@link #equals(Object)} and
 * {@link #hashCode()} code methods using the {@link #getIdentifier()} value.
 *
 * @param <E>
 *            the element type
 * @author BBonev
 */
public abstract class MergeableBase<E extends MergeableBase<?>> implements Mergeable<E> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getIdentifier() == null ? 0 : getIdentifier().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof MergeableBase) {
			return EqualsHelper.nullSafeEquals(getIdentifier(), ((Identity) obj).getIdentifier());
		}
		return false;
	}

}
