package com.sirma.itt.seip.synchronization;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.util.Collections;
import java.util.Map;

/**
 * Wrapper object to contain the result of the synchronization process.
 * <p>
 * If the process does not support merging then the modified elements will be located in the {@link #getToAdd()} and
 * {@link #getToRemove()} maps. If merge is supported then the modified elements will be located in the
 * {@link #getModified()} and not will not be found in the other mappings.
 *
 * @author BBonev
 * @param <I>
 *            the element identifier type
 * @param <E>
 *            the element type
 */
public class SynchronizationResult<I, E> {

	/** Empty synchronization result */
	public static final SynchronizationResult<?, ?> EMPTY = new SynchronizationResult<>(Collections.emptyMap(),
			Collections.emptyMap(), Collections.emptyMap());

	private final Map<I, E> toAdd;
	private final Map<I, E> toRemove;
	private final Map<I, E> modified;

	/**
	 * Instantiates a new synchronization result.
	 *
	 * @param toAdd
	 *            elements found only in the source
	 * @param toRemove
	 *            the elements found inly in the destination and should be removed if supported
	 * @param modified
	 *            the modified merged elements
	 */
	public SynchronizationResult(Map<I, E> toAdd, Map<I, E> toRemove, Map<I, E> modified) {
		this.toAdd = toAdd;
		this.toRemove = toRemove;
		this.modified = modified;
	}

	/**
	 * Empty result.
	 *
	 * @param <I>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @return the synchronization result
	 */
	@SuppressWarnings("unchecked")
	public static <I, E> SynchronizationResult<I, E> emptyResult() {
		return (SynchronizationResult<I, E>) EMPTY;
	}

	/**
	 * Gets items only found in the source or modified if merge is not supported.
	 *
	 * @return the items for addition
	 */
	public Map<I, E> getToAdd() {
		return toAdd;
	}

	/**
	 * Gets the items only found the destination (removed from the source) or modified if merge is not supported.
	 *
	 * @return the items for removal
	 */
	public Map<I, E> getToRemove() {
		return toRemove;
	}

	/**
	 * Gets the merged modified items if merge is supported. If not then this will always returns empty {@link Map}.
	 *
	 * @return the modified merged items
	 */
	public Map<I, E> getModified() {
		return modified;
	}

	/**
	 * Checks if the result carries any changes found.
	 *
	 * @return true, if any changes are found and any of the data methods returns changes.
	 */
	public boolean hasChanges() {
		return isNotEmpty(toAdd) || isNotEmpty(toRemove) || isNotEmpty(modified);
	}
}
