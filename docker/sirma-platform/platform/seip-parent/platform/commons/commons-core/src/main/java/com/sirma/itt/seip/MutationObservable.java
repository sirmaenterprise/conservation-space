/**
 *
 */
package com.sirma.itt.seip;

/**
 * Class that implements this interface states that it's internal state can change and supports a notification for the
 * change. The internal state could be a configuration or something else. When the state changes the implementation
 * should call all registered objects for notifications.
 *
 * @author BBonev
 */
@FunctionalInterface
public interface MutationObservable {

	/**
	 * Adds the mutation observer. The given executable will be called on change.
	 *
	 * @param executable
	 *            the executable to be notified
	 */
	void addMutationObserver(Executable executable);

	/**
	 * Register the given executable to all observable objects that implement the {@link MutationObservable} interface
	 *
	 * @param iterable
	 *            the collection of observables to register to.
	 * @param executable
	 *            the executable to register
	 */
	static void registerToAll(Iterable<?> iterable, Executable executable) {
		iterable.forEach(m -> {
			if (m instanceof MutationObservable) {
				((MutationObservable) m).addMutationObserver(executable);
			}
		});
	}
}
