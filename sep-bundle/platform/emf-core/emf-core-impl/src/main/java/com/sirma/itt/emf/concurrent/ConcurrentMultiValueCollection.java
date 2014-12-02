package com.sirma.itt.emf.concurrent;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;


/**
 * Implementation of a concurrent attribute-multivalue pair collection.
 * 
 * @author nvelkov
 */
@ApplicationScoped
public class ConcurrentMultiValueCollection {

	/** The multi-value pair collection. */
	private ConcurrentHashMap<Serializable, Set<Serializable>> multiValueCollection = new ConcurrentHashMap<>();
	/** The events. */
	@Inject
	private Event<ConcurrentMultiValueCollectionEvent> events;

	/**
	 * Adds the value to the set of the given key. If the key is not present in the multimap,
	 * inserts it in the multimap and adds a new empty {@link Set} backed up by a
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true if the set of the given key did not already contain the specified element
	 *         {@link ConcurrentHashMap}.
	 */
	public boolean addToKey(Serializable key, Serializable value) {
		Set<Serializable> values = multiValueCollection.get(key);
		if (values != null) {
			return values.add(value);
		} else {
			Set<Serializable> newSet = Collections
					.newSetFromMap(new ConcurrentHashMap<Serializable, Boolean>());
			multiValueCollection.put(key, newSet);
			return newSet.add(value);
		}
	}

	/**
	 * Fires an event that will remove the value after the transaction has ended.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void removeAfterTransaction(Serializable key, Serializable value) {
		events.fire(new ConcurrentMultiValueCollectionEvent(key, value));
	}

	/**
	 * Returns true if the set of the given key contains the value.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true, if the set of the given key contains the value.
	 */
	public boolean keyContains(Serializable key, Serializable value) {
		Set<Serializable> values = multiValueCollection.get(key);
		if (values != null) {
			return values.contains(value);
		}
		return false;
	}

	/**
	 * Removes the value from the set of the given key. If the set of the given key is empty, after
	 * the removal, removes the key from the multimap. The value will be removed from the map after
	 * the transaction has been completed.
	 * 
	 * 
	 * @param entity
	 *            the entity
	 */
	public void removeFromKey(
			@Observes(during = TransactionPhase.AFTER_COMPLETION) ConcurrentMultiValueCollectionEvent entity) {
		Set<Serializable> values = multiValueCollection.get(entity.getKey());
		if (values != null) {
			values.remove(entity.getValue());
			if (values.isEmpty()) {
				multiValueCollection.remove(entity.getKey());
			}
		}
	}

	/**
	 * Gets the multi value collection.
	 *
	 * @return the multi value collection
	 */
	public ConcurrentHashMap<Serializable, Set<Serializable>> getMultiValueCollection() {
		return multiValueCollection;
	}

	/**
	 * Sets the multi value collection.
	 *
	 * @param multiValueCollection the multi value collection
	 */
	public void setMultiValueCollection(
			ConcurrentHashMap<Serializable, Set<Serializable>> multiValueCollection) {
		this.multiValueCollection = multiValueCollection;
	}

}
