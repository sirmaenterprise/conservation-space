package com.sirma.itt.emf.semantic.security;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.TransactionScoped;

/**
 * Transaction scoped buffer to store {@link PermissionChangeRequest}s
 *
 * @author BBonev
 */
@TransactionScoped
public class PermissionChangeRequestBuffer implements Serializable {

	private static final long serialVersionUID = 7319581200298405180L;

	private Set<PermissionChangeRequest> changes = new HashSet<>();

	/**
	 * Adds a permission change request to the buffer. The buffer will ignore <code>null</code> or duplicate requests
	 *
	 * @param changeRequest
	 *            the change request
	 * @return true, if successfully added the change request to the buffer. This is the case then the request was not
	 *         <code>null</code> and not already present.
	 */
	public boolean add(PermissionChangeRequest changeRequest) {
		return addNonNullValue(changes, changeRequest);
	}

	/**
	 * Gets the all stored changes
	 *
	 * @return the a copy of all stored changes
	 */
	public synchronized Collection<PermissionChangeRequest> getAll() {
		return new ArrayList<>(changes);
	}

	/**
	 * Return all stored changes and clears the buffer.
	 *
	 * @return all changes
	 */
	public synchronized Collection<PermissionChangeRequest> drainAll() {
		List<PermissionChangeRequest> copy = new ArrayList<>(changes);
		changes.clear();
		return copy;
	}
}
