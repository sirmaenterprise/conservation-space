package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Base class for lazy collections. The implementation provides means to define a collection that is lazy expandable.
 * The collection may have an initial element that may be used for some operations like contains to perform some of the
 * collection operations without expanding the collection. During instance initialization a supplier that provides the
 * rest of the collection elements should be provided.
 *
 * @author BBonev
 * @param <E>
 *            the element collection type
 * @param <C>
 *            the collection type
 */
public class BaseLazyCollection<E, C extends Collection<E>> implements Collection<E> {

	private E initial;
	private C expandedStore;
	private Supplier<C> expandedSource;
	private final Supplier<C> emptySupplier;
	private BiFunction<E, C, C> initialValueMerger;
	private Consumer<E> onAfterMerge;

	/**
	 * Instantiates a new lazy collection. The instance will use a default initial value merger where new collection is
	 * created and all elements are copied in it and the initial value is replaced in the new collection.
	 *
	 * @param initial
	 *            the initial value to provide if any
	 * @param expandedSource
	 *            A supplier that should provide the rest of the elements for the collection data. it should be mutable
	 *            collection. If <code>null</code> or empty then the empty supplier will be called to initialize the
	 *            store.
	 * @param emptySupplier
	 *            the empty collection supplier called when the expandedSource supplier returns <code>null</code> or
	 *            empty collection. The returned collection should be mutable.
	 */
	public BaseLazyCollection(E initial, Supplier<C> expandedSource, Supplier<C> emptySupplier) {
		this(initial, expandedSource, emptySupplier,
				(init, coll) -> coll.stream().map(i -> EqualsHelper.nullSafeEquals(init, i) ? init : i).collect(
						Collectors.toCollection(emptySupplier)));
	}

	/**
	 * Instantiates a new lazy collection.
	 *
	 * @param initial
	 *            the initial value to provide if any
	 * @param expandedSource
	 *            A supplier that should provide the rest of the elements for the collection data. it should be mutable
	 *            collection. If <code>null</code> or empty then the empty supplier will be called to initialize the
	 *            store.
	 * @param emptySupplier
	 *            the empty collection supplier called when the expandedSource supplier returns <code>null</code> or
	 *            empty collection. The returned collection should be mutable.
	 * @param initialValueMerger
	 *            the initial value merger This method is called when expanded source supplier returns non empty
	 *            collection and the initial value is non null. The function should return a collection that is a merge
	 *            between the initial value and the expanded collection.
	 */
	public BaseLazyCollection(E initial, Supplier<C> expandedSource, Supplier<C> emptySupplier,
			BiFunction<E, C, C> initialValueMerger) {
		this.initial = initial;
		this.expandedSource = expandedSource;
		this.emptySupplier = emptySupplier;
		this.initialValueMerger = initialValueMerger;
	}

	/**
	 * Instantiates a new base lazy collection from other lazy collection
	 *
	 * @param <T>
	 *            the generic type
	 * @param copyFrom
	 *            the copy from
	 */
	public <T extends BaseLazyCollection<E, C>> BaseLazyCollection(T copyFrom) {
		BaseLazyCollection<E, C> source = copyFrom;
		initial = source.initial;
		expandedSource = source.expandedSource;
		expandedStore = source.expandedStore;
		emptySupplier = source.emptySupplier;
		initialValueMerger = source.initialValueMerger;
		onAfterMerge = source.onAfterMerge;
	}

	/**
	 * Lazy load the store. If the actual collection is not initialized then the {@link #initStore()} method will be
	 * called to do the initialization. The method is thread safe.
	 *
	 * @return the store
	 */
	protected synchronized C getStore() {
		// if not initialized, yet. Than initialize it now.
		if (expandedStore == null) {
			expandedStore = initStore();
		}
		return expandedStore;
	}

	/**
	 * Initialize a collection using store supplier, empty store supplier, the initial value and the initial value
	 * merger.
	 *
	 * @return a non null collection
	 */
	protected C initStore() {
		C list = expandedSource.get();
		if (CollectionUtils.isEmpty(list)) {
			list = emptySupplier.get();
			CollectionUtils.addNonNullValue(list, initial);
		} else {
			// if there is an initial value we will search for that value in the list and replace it's reference.
			list = mergeInitialValueInto(list);
		}
		callAfterMerge(list);
		return list;
	}

	/**
	 * Call after merge function if any
	 *
	 * @param list
	 *            the list
	 */
	protected void callAfterMerge(C list) {
		if (onAfterMerge != null) {
			list.forEach(onAfterMerge);
		}
	}

	/**
	 * Merges the initial value with the given collection using the initial value merger.
	 *
	 * @param list
	 *            the list
	 * @return the c
	 */
	protected C mergeInitialValueInto(C list) {
		return initialValueMerger.apply(initial, list);
	}

	/**
	 * Sets the on after merge function that will be called for each element of the new collection.
	 *
	 * @param <T>
	 *            result sub type of BaseLazyCollection
	 * @param onAfterMerge
	 *            the new on after merge
	 * @return the current instance
	 */
	@SuppressWarnings("unchecked")
	public <T extends BaseLazyCollection<E, C>> T setOnAfterMerge(Consumer<E> onAfterMerge) {
		this.onAfterMerge = onAfterMerge;
		return (T) this;
	}

	@Override
	public int size() {
		return getStore().size();
	}

	@Override
	public boolean isEmpty() {
		return initial == null && getStore().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (EqualsHelper.nullSafeEquals(initial, o)) {
			return true;
		}
		return getStore().contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return getStore().iterator();
	}

	@Override
	public Object[] toArray() {
		return getStore().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return getStore().toArray(a);
	}

	@Override
	public boolean add(E e) {
		return getStore().add(e);
	}

	@Override
	public boolean remove(Object o) {
		return getStore().remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return getStore().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return getStore().addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return getStore().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return getStore().retainAll(c);
	}

	@Override
	public void clear() {
		synchronized (this) {
			initial = null;
			// if the collection is initialized we will clear it otherwise it should never be initialized
			if (expandedStore != null) {
				expandedStore.clear();
			} else {
				expandedSource = emptySupplier;
			}
		}
	}
}
