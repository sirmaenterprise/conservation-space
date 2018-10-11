package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.transaction.TransactionScoped;

/**
 * Transactional buffer that stores instance ids of changed instances in the current transaction.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/07/2018
 */
@TransactionScoped
class ChangedInstancesBuffer implements Serializable {

	private Collection<Serializable> buffer = new HashSet<>();

	/**
	 * Adds the given ids to the biffer
	 *
	 * @param ids the ids to add
	 */
	void addAll(Collection<? extends Serializable> ids) {
		buffer.addAll(ids);
	}

	/**
	 * Gets a copy of the buffer.
	 *
	 * @return a copy of the buffered ids
	 */
	Collection<Serializable> getBuffer() {
		return new ArrayList<>(buffer);
	}

	/**
	 * Returns a copy of the buffer and clears it's contents
	 *
	 * @return the contents of the buffer before the clear. The same as the {@link #getBuffer()}
	 */
	synchronized Collection<Serializable> drainBuffer() {
		Collection<Serializable> copy = getBuffer();
		buffer.clear();
		return copy;
	}
}
