/**
 *
 */
package com.sirma.itt.seip.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;

/**
 * Wrapper object that stores a single instance references based on a context id supplier.
 * <p>
 * Current implementation uses a map to store the values for the different contexts. When the value is requested and is
 * <code>null</code> it will be initialized using the provided supplier via the method {@link #initializeWith(Supplier)}
 * . If no supplier is provided then the method will return <code>null</code>.<br>
 * In order to reset the value the method {@link #clearContextValue()} should be called so that next time the
 * {@link #getContextValue()} is called it will force the initial value supplier to be called again. Note that if the
 * initial value supplier returns <code>null</code> the next time {@link #getContextValue()} is called it will be called
 * again.
 * <p>
 * If extension of this class require value validation it should override the method
 * {@link #validateInitialValue(Object)} and throw exception if needed.
 *
 * @author BBonev
 * @param <T>
 *            the generic type
 */
@Vetoed
public class ContextualReference<T> implements Contextual<T> {

	private final Supplier<String> contextIdSupplier;
	private final Map<String, T> store;
	private Supplier<T> initialValueSupplier;
	private Consumer<T> destroyConsumer;

	/**
	 * Instantiates a new contextual reference with a single context
	 */
	public ContextualReference() {
		this(CONTEXT_ID_SUPPLIER);
	}

	/**
	 * Creates contextual reference with fixed context.
	 *
	 * @param <T>
	 *            the generic type
	 * @return the contextual reference
	 */
	public static <T> ContextualReference<T> create() {
		return new ContextualReference<>();
	}

	/**
	 * Instantiates a new contextual reference.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 */
	public ContextualReference(Supplier<String> contextIdSupplier) {
		Objects.requireNonNull(contextIdSupplier, "Cannot initalize with null context supplier!");

		this.contextIdSupplier = contextIdSupplier;
		store = new HashMap<>();
	}

	/**
	 * Instantiates a new contextual reference.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 */
	public ContextualReference(Supplier<String> contextIdSupplier, Supplier<T> initialValue) {
		this(contextIdSupplier);
		if (initialValue != null) {
			initializeWith(initialValue);
		}
	}

	@Override
	public void initializeWith(Supplier<T> initialValue) {
		Objects.requireNonNull(initialValue, "Cannot initialize with null supplier");
		initialValueSupplier = initialValue;
	}

	@Override
	public String getContextId() {
		return contextIdSupplier.get();
	}

	@Override
	public T replaceContextValue(T newValue) {
		if (newValue == null) {
			return clearContextValue();
		}
		return addToStore(newValue);
	}

	@Override
	public T getContextValue() {
		T t = getFromStore();
		if (t == null) {
			t = createNewValueAndStoreIt();
		}
		return t;
	}

	@Override
	public T clearContextValue() {
		return removeFromStore(getContextId());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		// print value but do not initialize if not already
		builder
				.append("ContextualReference [context=")
					.append(contextIdSupplier.get())
					.append(", value=")
					.append(getFromStore())
					.append("]");
		return builder.toString();
	}

	/**
	 * Creates the new value and store it. The method invokes the initial value supplier, validates the created value
	 * and adds it to the store.
	 *
	 * @return the created value or <code>null</code> if <code>null</code> was produces
	 */
	protected synchronized T createNewValueAndStoreIt() {
		// check if other parallel call to the method didn't create the value if so just return it
		T t = getFromStore();
		if (t != null) {
			return t;
		}
		t = createInitValue();
		validateInitialValue(t);
		addToStore(t);
		return t;
	}

	/**
	 * Gets the value from store.
	 *
	 * @return the from store that could be <code>null</code> if nothing was in the store
	 */
	protected T getFromStore() {
		return store.get(getContextId());
	}

	/**
	 * Adds the given value to store and returns the old value if any.
	 *
	 * @param newValue
	 *            the new value to set
	 * @return the old value or <code>null</code>
	 */
	protected T addToStore(T newValue) {
		return store.put(getContextId(), newValue);
	}

	/**
	 * Creates the initial value using the supplier if any.
	 *
	 * @return the initial value or <code>null</code>
	 */
	protected T createInitValue() {
		return initialValueSupplier == null ? null : initialValueSupplier.get();
	}

	/**
	 * This method is called to validate the produced initial value. The default behavior is to do nothing.
	 *
	 * @param initialValue
	 *            the initial value to validate
	 */
	protected void validateInitialValue(T initialValue) {
		// nothing to do
	}

	/**
	 * Removes the from store using the given key.
	 *
	 * @param id
	 *            the id
	 * @return the removed value if any
	 */
	protected T removeFromStore(String id) {
		return store.remove(id);
	}

	@Override
	public boolean isSet() {
		return store.containsKey(getContextId());
	}

	@Override
	public void onDestroy(Consumer<T> destroyer) {
		this.destroyConsumer = destroyer;
	}

	@Override
	public void destroy() {
		if (destroyConsumer != null) {
			store.values().stream().filter(Objects::nonNull).forEach(destroyConsumer);
		}
		store.clear();
	}

}
