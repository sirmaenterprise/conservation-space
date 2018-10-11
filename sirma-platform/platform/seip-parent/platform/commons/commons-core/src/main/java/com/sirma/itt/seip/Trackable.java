package com.sirma.itt.seip;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Defines a contract to enable changes tracking on an object. The implementor is responsible for collecting and
 * providing the list of changes in the order of occurring. <p>
 * For changes collection the class {@link PropertiesChanges} could be used. It provides means of tracking collection
 * and map instance for changes.
 * </p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/06/2018
 */
public interface Trackable<S extends Serializable> {

	/**
	 * Enable tracking if applicable and not already tracked. If already tracked nothing will be done
	 *
	 * @param trackable the instance to enable tracking
	 * @return true if tracking was enabled successfully and false if not applicable
	 */
	static boolean enableTracking(Object trackable) {
		if (trackable instanceof Trackable) {
			if (((Trackable) trackable).isTracked()) {
				return true;
			}
			((Trackable) trackable).enableChangesTracking();
			return true;
		}
		return false;
	}

	/**
	 * Disable tracking if applicable and tracked. If tracking is already disabled nothing will be done.
	 *
	 * @param trackable the instance to disable tracking
	 */
	static void disableTracking(Object trackable) {
		if (trackable instanceof Trackable && ((Trackable) trackable).isTracked()) {
			((Trackable) trackable).disableChangesTracking();
		}
	}

	/**
	 * Enable changes tracking for the current object. Any changes to the underling data structure should be reported
	 * by the method {@link #changes()} until the method {@link #disableChangesTracking()} is called.
	 *
	 * @throws IllegalStateException if tracking is already enabled
	 */
	void enableChangesTracking();

	/**
	 * Disable changes tracking. If changes tracking was not enabled in the first place this method should throw
	 * {@link IllegalStateException}.
	 * <p>Any changes done after enabling the tracking should still be reporable by the method {@link #changes()}.
	 * Any new changes done to the object should not be tracked and recorded anymore until enabled again</p>
	 *
	 * @throws IllegalStateException if tracking is not enabled
	 */
	void disableChangesTracking();

	/**
	 * Check if tracking is enabled.<br>
	 * If this method returns {@code true} calling {@link #enableChangesTracking()} should throw {@link IllegalStateException} <br>
	 * If this method returns {@code false} calling {@link #disableChangesTracking()} should throw {@link IllegalStateException}
	 *
	 * @return true if tracking is enabled.
	 */
	boolean isTracked();

	/**
	 * Clear any changes recorded. If tracking is not enabled this method should do nothing.<br>
	 * Calling this method and then {@link #changes()} should return empty stream
	 */
	void clearChanges();

	/**
	 * Stream the changes from the moment tracking is enabled or from the last time the method {@link #clearChanges()}
	 * is invoked.
	 *
	 * @return a stream of all changes recorded in the order of their occurrence
	 */
	Stream<PropertyChange<S>> changes();
}
