package com.sirma.itt.seip.context;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sirma.itt.seip.DestroyObservable;
import com.sirma.itt.seip.Destroyable;

/**
 * Defines a way to store and access data in different contexts. The most common use is with thread local context.
 *
 * @param <T>
 *            the generic type
 */
public interface Contextual<T> extends DestroyObservable<T>, Destroyable {

	/** The context id supplier used to initialize single context instances. */
	Supplier<String> CONTEXT_ID_SUPPLIER = () -> "$singleContext$";

	/**
	 * Gets the current context id.
	 *
	 * @return the context id
	 */
	String getContextId();

	/**
	 * Gets the stored context instance or <code>null</code> if no supplier is provided using the method
	 * {@link #initializeWith(Supplier)}.
	 *
	 * @return the context instance or <code>null</code>
	 */
	T getContextValue();

	/**
	 * Directly set the context value. This will override the supplier if any. If the new value is <code>null</code>
	 * this will effectively remove the value and the next time it's requested the supplier will be used if any.
	 *
	 * @param newValue
	 *            the new value
	 * @return the old value or <code>null</code> if not such value exists
	 */
	T replaceContextValue(T newValue);

	/**
	 * Provide a supplier that will be used when the context instance is requested and the stored context instance is
	 * <code>null</code>.
	 *
	 * @param initialValue
	 *            the initial value
	 */
	void initializeWith(Supplier<T> initialValue);

	/**
	 * Clear the stored value in the current context.
	 *
	 * @return the removed value
	 */
	T clearContextValue();

	/**
	 * Checks if value is set but it may be <code>null</code>. The method checks if the contextual object has been
	 * initialized for the current context.
	 *
	 * @return true, if value has been initialized
	 */
	boolean isSet();

	/**
	 * Checks if context value is not <code>null</code>.
	 *
	 * @return true, if is not null
	 */
	default boolean isNotNull() {
		return getContextValue() != null;
	}

	/**
	 * Resets the current context value. Calling this method will clear any value if present and then will call the
	 * provided supplier if any.
	 */
	default void reset() {
		clearContextValue();
		getContextValue();
	}

	/**
	 * Register a operation to be executed when calling the method {@link #destroy()}.
	 *
	 * @param destroyConsumer
	 *            the function to be invoked with the current value, if not null, before being deleted.
	 */
	@Override
	void onDestroy(Consumer<T> destroyConsumer);

	/**
	 * Calling the destroy method will call any destroy callback registered and reset the internal context. This means
	 * that if the object is used again then the {@link #initializeWith(Supplier)} will be called again for the same
	 * context. The difference between this method and {@link #replaceContextValue(Object)} or {@link #reset()} is that
	 * this will clear all active contexts.
	 */
	@Override
	void destroy();
}
