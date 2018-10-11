package com.sirma.itt.seip.search;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Provide equals and hashcode and toString implementations for {@link ResultItem}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/06/2017
 */
public abstract class AbstractResultItem implements ResultItem {

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof ResultItem)) {
			return false;
		}

		ResultItem that = (ResultItem) other;

		// checking for entry size may have performance improvement but breaks the equals/hashcode contract as the
		// iterators returns only non null values, but they are counted against the size

		// Compare other's values to own
		for (ResultValue item : that) {
			Serializable ownValue = getResultValue(item.getName());

			if (!item.getValue().equals(ownValue)) {
				// Unequal values for this name
				return false;
			}
		}

		return true;
	}

	@Override
	public final int hashCode() {
		int hashCode = 0;

		for (ResultValue item : this) {
			hashCode ^= item.getName().hashCode() ^ item.getValue().hashCode();
		}
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32 * size());

		sb.append('[');

		Iterator<ResultValue> it = iterator();
		while (it.hasNext()) {
			sb.append(it.next().toString());
			if (it.hasNext()) {
				sb.append(';');
			}
		}

		sb.append(']');

		return sb.toString();
	}
}
