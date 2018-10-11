package com.sirma.itt.seip.concurrent.collections;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Implementation of a concurrent attribute-multivalue pair collection.
 *
 * @author nvelkov
 */
@Singleton
public class ConcurrentMultiValueCollection {

	private ContextualMap<Serializable, Set<Serializable>> multiValueCollection;
	private TransactionSupport transactionSupport;

	/**
	 * Instantiates a new concurrent multi value collection.
	 *
	 * @param multiValueCollection
	 *            the multi value collection store
	 * @param transactionSupport
	 *            the transaction support
	 */
	@Inject
	public ConcurrentMultiValueCollection(ContextualMap<Serializable, Set<Serializable>> multiValueCollection,
			TransactionSupport transactionSupport) {
		this.multiValueCollection = multiValueCollection;
		this.transactionSupport = transactionSupport;
		this.multiValueCollection.initializeWith(ConcurrentHashMap::new);
	}

	/**
	 * Adds the value to the set of the given key. If the key is not present in the multimap, inserts it in the multimap
	 * and adds a new empty {@link Set} backed up by a
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true if the set of the given key did not already contain the specified element {@link ConcurrentHashMap}.
	 */
	public boolean addToKey(Serializable key, Serializable value) {
		Set<Serializable> values = multiValueCollection.get(key);
		if (values != null) {
			return values.add(value);
		}
		Set<Serializable> newSet = Collections.newSetFromMap(new ConcurrentHashMap<Serializable, Boolean>());
		multiValueCollection.put(key, newSet);
		return newSet.add(value);
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
		transactionSupport.invokeAfterTransactionCompletion(() -> removeFromKey(key, value));
	}

	/**
	 * Removes the value from the set of the given key. If the set of the given key is empty, after the removal, removes
	 * the key from the multimap. The value will be removed from the map after the transaction has been completed.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	void removeFromKey(Serializable key, Serializable value) {
		Set<Serializable> values = multiValueCollection.get(key);
		if (values != null) {
			values.remove(value);
			if (values.isEmpty()) {
				multiValueCollection.remove(key);
			}
		}
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
	 * Gets the multi value collection.
	 *
	 * @return the multi value collection
	 */
	public Map<Serializable, Set<Serializable>> getMultiValueCollection() {
		return multiValueCollection;
	}
}
