package com.sirma.itt.seip.synchronization;

import java.util.Set;

/**
 * A synchronization summary object.
 *
 * @author BBonev
 */
public class SynchronizationResultState {

	private final String name;
	private Set<Object> added;
	private Set<Object> removed;
	private Set<Object> modified;
	private SynchronizationException exception;
	private long duration;
	private SynchronizationResult<?, ?> result;

	/**
	 * Instantiates a new synchronization result state.
	 *
	 * @param name
	 *            the name of the run synchronization
	 * @param result
	 *            the result of the run synchronization
	 * @param duration
	 *            the duration of the synchronization
	 */
	@SuppressWarnings("unchecked")
	public SynchronizationResultState(String name, SynchronizationResult<?, ?> result, long duration) {
		this.name = name;
		this.result = result;
		this.duration = duration;
		added = (Set<Object>) result.getToAdd().keySet();
		removed = (Set<Object>) result.getToRemove().keySet();
		modified = (Set<Object>) result.getModified().keySet();
	}

	/**
	 * Instantiates a new synchronization result state.
	 *
	 * @param name
	 *            the name of the run synchronization
	 * @param exception
	 *            the exception that caused the synchronization termination
	 */
	public SynchronizationResultState(String name, SynchronizationException exception) {
		this.name = name;
		this.exception = exception;
	}

	/**
	 * Gets the name of the synchronization run
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the identifiers of the added items
	 *
	 * @return the added
	 */
	public Set<Object> getAdded() {
		return added;
	}

	/**
	 * Gets the identifiers of the removed items
	 *
	 * @return the removed
	 */
	public Set<Object> getRemoved() {
		return removed;
	}

	/**
	 * Gets the identifiers of the modified items
	 *
	 * @return the modified
	 */
	public Set<Object> getModified() {
		return modified;
	}

	/**
	 * Gets the duration of the synchronization in milliseconds
	 *
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Gets the exception that failed the synchronization.
	 *
	 * @return the exception
	 */
	public SynchronizationException getException() {
		return exception;
	}

	/**
	 * Gets the actual result if any.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the result
	 */
	@SuppressWarnings("unchecked")
	public <K, V> SynchronizationResult<K, V> getResult() {
		return (SynchronizationResult<K, V>) result;
	}
}
