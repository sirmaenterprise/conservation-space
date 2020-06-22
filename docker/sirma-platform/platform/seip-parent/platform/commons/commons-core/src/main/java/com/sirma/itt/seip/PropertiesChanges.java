package com.sirma.itt.seip;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Properties changes container. Stores all tracked changes and provides means for retrieving them. <br>
 * To add a change one of the methods {@link #add(String, Object) add},
 * {@link #update(String, Object, Object) update} or {@link #remove(String, Object) remove}
 * could be used. <br>
 * There are methods for automatic changes recording for collections and maps. To create such instance one of the methods
 * {@link #trackChanges(Map)} or {@link #trackChanges(String, Collection)} could be used.<br>
 * Note that the changes are not serializable. The wrapping collections are serializable but not the changes in them.
 * This allows them to be serialized like the delegating collections but the changes will be lost when serialized and
 * deserialized. <br>
 * Upon deserialization of wrapped collection it will no longer track any changes. To enable tracking again they should
 * be wrapped gain via one of the methods {@link #trackChanges(String, Collection)} or {@link #trackChanges(Map)}.<br>
 * This decision was made due to the fact that in the tracked collection is kept a reference to the all changes and some
 * of the value may not be fully serializable. Also serializing a complex object graph like tracked map with values
 * tracked collections will cause changes to be serialized multiple times.
 *
 * @param <S> the type if values stored in the changes
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/05/2018
 */
public class PropertiesChanges<S> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private LinkedList<PropertyChange<S>> changes = new LinkedList<>();
	private boolean pauseTracking;

	/**
	 * Register property change for adding new value to a property
	 *
	 * @param property the property name
	 * @param value the added property value
	 */
	public void add(String property, S value) {
		if (isRecordingActive()) {
			changes.add(PropertyChange.add(property, cloneValue(value)));
		}
	}

	/**
	 * Register property change for adding new value to a property
	 *
	 * @param property the property name
	 * @param value the added property value
	 */
	public void append(String property, S value) {
		if (isRecordingActive()) {
			changes.add(PropertyChange.append(property, cloneValue(value)));
		}
	}

	@SuppressWarnings("unchecked")
	private static <V> V cloneValue(Object value) {
		if (value instanceof Serializable) {
			Serializable valueToClone = null;
			if (value instanceof Collection) {
				valueToClone = (Serializable) unTrackChanges((Collection<V>) value);
			} else if (value instanceof Map) {
				valueToClone = (Serializable) unTrackChanges((Map<String, V>) value);
			}
			// clone only collection or map values, do not touch other serializable values
			if (valueToClone != null) {
				try {
					return (V) SerializationUtils.deserialize(SerializationUtils.serialize(valueToClone));
				} catch (SerializationException e) {
					LOGGER.trace("Can't clone value using serialization. Performing shallow value copy.", e);
					return shallowValuesCopy(valueToClone);
				}
			}
		}
		return (V) value;
	}

	@SuppressWarnings("unchecked")
	private static <V> V shallowValuesCopy(Serializable valueToClone) {
		if (valueToClone instanceof Collection) {
			int valueSize = ((Collection<?>) valueToClone).size();
			Collection<?> output = new ArrayList<>(valueSize);
			output.addAll((Collection) valueToClone);
			return (V) output;
		} else if (valueToClone instanceof Map) {
			Map<?, ?> output = CollectionUtils.createLinkedHashMap(((Map<?, ?>) valueToClone).size());
			output.putAll((Map) valueToClone);
			return (V) output;
		}
		return (V) valueToClone;
	}

	/**
	 * Register property change for updating property value
	 *
	 * @param property the property name
	 * @param newValue the new value to set
	 * @param oldValue the old value that's being overridden
	 */
	public void update(String property, S newValue, S oldValue) {
		if (isRecordingActive() && !Objects.equals(newValue, oldValue)) {
			changes.add(PropertyChange.update(property, cloneValue(newValue), cloneValue(oldValue)));
		}
	}

	/**
	 * Register property change for property value removal
	 *
	 * @param property the property name
	 * @param value the removed value
	 */
	public void remove(String property, S value) {
		if (isRecordingActive()) {
			changes.add(PropertyChange.remove(property, cloneValue(value)));
		}
	}

	/**
	 * Pause recording changes. Any changes done after calling this method will not be recorded. No changes to the
	 * objects graph are made by this method.
	 */
	public void pause() {
		pauseTracking = true;
	}

	/**
	 * Resume recording changes if recoding was paused
	 */
	public void resume() {
		pauseTracking = false;
	}

	/**
	 * Check if changes recording is paused or not
	 *
	 * @return true if recording is performed at the moment and false if it's not performed
	 */
	public boolean isRecordingActive() {
		return !pauseTracking;
	}

	/**
	 * Stream the recorded changes
	 *
	 * @return changes stream
	 */
	public Stream<PropertyChange<S>> changes() {
		return changes.stream();
	}

	/**
	 * Wrap the given collection into one that can track adding, updating and removing of elements in the collection.<br>
	 * All of the changes will be stored in the current instance
	 *
	 * @param key static key to be assigned to all property changes for the given collection. All changes generated by
	 * modification for the given collection will be written with the key passed when the method is called.
	 * @param delegate the delegate collection to wrap. The concrete supported types are Set and List. Any other
	 * collection sub type will be wrapped in general collection interface
	 * @return a collection that records it's changes in the current instance
	 */
	public <E extends S, C extends List<E>> List<E> trackChanges(String key, C delegate) {
		Objects.requireNonNull(delegate, "Cannot wrap null list");
		if (isTracking(delegate)) {
			return delegate;
		}
		return new TrackedList<>(key, unTrackChanges(delegate), this);
	}

	/**
	 * Wrap the given collection into one that can track adding, updating and removing of elements in the collection.<br>
	 * All of the changes will be stored in the current instance
	 *
	 * @param key static key to be assigned to all property changes for the given collection. All changes generated by
	 * modification for the given collection will be written with the key passed when the method is called.
	 * @param delegate the delegate collection to wrap. The concrete supported types are Set and List. Any other
	 * collection sub type will be wrapped in general collection interface
	 * @return a collection that records it's changes in the current instance
	 */
	public <E extends S, C extends Set<E>> Set<E> trackChanges(String key, C delegate) {
		Objects.requireNonNull(delegate, "Cannot wrap null set");
		if (isTracking(delegate)) {
			return delegate;
		}
		return new TrackedSet<>(unTrackChanges(delegate), this, ChangesRecorder.collectionRecorder(key));
	}

	/**
	 * Wrap the given collection into one that can track adding, updating and removing of elements in the collection.<br>
	 * All of the changes will be stored in the current instance
	 *
	 * @param key static key to be assigned to all property changes for the given collection. All changes generated by
	 * modification for the given collection will be written with the key passed when the method is called.
	 * @param delegate the delegate collection to wrap. The concrete supported types are Set and List. Any other
	 * collection sub type will be wrapped in general collection interface
	 * @return a collection that records it's changes in the current instance
	 */
	@SuppressWarnings("unchecked")
	public <E extends S, C extends Collection<E>> Collection<E> trackChanges(String key, C delegate) {
		Objects.requireNonNull(delegate, "Cannot wrap null collection");
		if (isTracking(delegate)) {
			return delegate;
		}
		C originalDelegate = unTrackChanges(delegate);
		if (originalDelegate instanceof Set) {
			return new TrackedSet<>(key, (Set<E>) originalDelegate, this);
		} else if (originalDelegate instanceof List) {
			return new TrackedList<>(key, (List<E>) originalDelegate, this);
		}
		return new TrackedCollection<>(key, originalDelegate, this);
	}

	/**
	 * Unwrap previously wrapped collection by the method {@link #trackChanges(String, Collection)}.
	 *
	 * @param collection the collection to unwrap.
	 * @return the original collection instance passed to the method {@link #trackChanges(String, Collection)} otherwise
	 * 		returns the argument itself
	 */
	@SuppressWarnings("unchecked")
	public static <E, C extends Collection<E>> C unTrackChanges(C collection) {
		if (collection instanceof TrackedCollection) {
			return ((TrackedCollection<E, E, C>) collection).delegate;
		}
		return collection;
	}

	/**
	 * Wrap a given map to track it's changes like addition, removal or value update. The map keys will be used as keys
	 * for the changes identifier. <br>
	 * All of the changes will be stored in the current instance
	 *
	 * @param delegate the map instance to wrap
	 * @return a map that records it's changes in the current instance
	 */
	public Map<String, S> trackChanges(Map<String, S> delegate) {
		Objects.requireNonNull(delegate, "Cannot wrap null map");
		if (isTracking(delegate)) {
			return delegate;
		}
		return new TrackedMap<>(unTrackChanges(delegate), this);
	}

	/**
	 * Unwrap previously wrapped map by the method {@link #trackChanges(Map)}.
	 *
	 * @param <S> value type
	 * @param map the map to unwrap.
	 * @return the original map instance passed to the method {@link #trackChanges(Map)} otherwise
	 * 		returns the argument itself
	 */
	public static <S> Map<String, S> unTrackChanges(Map<String, S> map) {
		if (map instanceof TrackedMap) {
			return ((TrackedMap<S>) map).delegate;
		}
		return map;
	}

	/**
	 * Checks if the given map is tracked and if it's done by the current instance
	 *
	 * @param map is the map to check if it's tracked and if this is done by the current instance
	 * @return true if the given map is tracked by the current instance
	 */
	public boolean isTracking(Map<String, S> map) {
		return map instanceof TrackedMap && ((TrackedMap) map).changes == this;
	}

	/**
	 * Checks if the given collection is tracked and if it's done by the current instance
	 *
	 * @param collection is the collection to check if it's tracked and if this is done by the current instance
	 * @return true if the given collection is tracked by the current instance
	 */
	public <E extends S, C extends Collection<E>> boolean isTracking(C collection) {
		return collection instanceof TrackedCollection && ((TrackedCollection) collection).changes == this;
	}

	/**
	 * Clear any recorded changes until this moment
	 */
	public void clear() {
		changes.clear();
	}

	private static class TrackedMap<V> implements Map<String, V>, Serializable {

		private final Map<String, V> delegate;
		private final transient PropertiesChanges<V> changes;

		private TrackedMap(Map<String, V> delegate, PropertiesChanges<V> changes) {
			this.delegate = delegate;
			this.changes = changes;
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return delegate.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return delegate.containsValue(value);
		}

		@Override
		public V get(Object key) {
			return delegate.get(key);
		}

		@Override
		public V put(String key, V value) {
			if (value == null) {
				changes.remove(key, delegate.get(key));
			} else {
				V oldValue = delegate.get(key);
				if (oldValue != null) {
					changes.update(key, value, oldValue);
				} else {
					changes.add(key, value);
				}
			}
			return delegate.put(key, value);
		}

		@Override
		public V remove(Object key) {
			V removedValue = delegate.remove(key);
			changes.remove(key.toString(), removedValue);
			return removedValue;
		}

		@Override
		public void putAll(Map<? extends String, ? extends V> m) {
			m.forEach(this::put);
		}

		@Override
		public void clear() {
			// first clone the key set as we are going to modify the map for each element
			new ArrayList<>(delegate.keySet()).forEach(this::remove);
		}

		@Override
		public Set<String> keySet() {
			// the map's key set does not allow additions or updates so we will handle only remove
			return new TrackedSet<>(delegate.keySet(), changes,
					ChangesRecorder.removeOnly((key, propertiesChanges) -> propertiesChanges.remove(key, get(key))));
		}

		@Override
		public Collection<V> values() {
			// for now we will not support map modification via it's values collection
			// current code base does not have a code that depends on this functionality
			return Collections.unmodifiableCollection(delegate.values());
		}

		@Override
		public Set<Entry<String, V>> entrySet() {
			// the map's entry set does not allow additions or updates so we will handle only remove
			return new TrackedSet<>(delegate.entrySet(), changes, ChangesRecorder.removeOnly(
					(entry, propertiesChanges) -> propertiesChanges.remove(entry.getKey(), entry.getValue())));
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Map)) {
				return false;
			}
			if (obj instanceof TrackedMap) {
				return delegate.equals(((TrackedMap) obj).delegate);
			}
			return delegate.equals(obj);
		}

		@Override
		public int hashCode() {
			return delegate.hashCode();
		}

		@Override
		public String toString() {
			return delegate.toString();
		}
	}

	private static class TrackedCollection<E, C, X extends Collection<E>> implements Collection<E>, Serializable {
		final String assignedKey;
		protected final X delegate;
		protected transient PropertiesChanges<C> changes;
		private transient ChangesRecorder<E, C> changesRecorder;

		private TrackedCollection(String assignedKey, X delegate, PropertiesChanges<C> changes) {
			this.assignedKey = assignedKey;
			this.delegate = delegate;
			this.changes = changes;
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return delegate.contains(o);
		}

		@Override
		public Iterator<E> iterator() {
			return new TrackedIterator<>(delegate.iterator(), changes, getChangesRecorder());
		}

		@Override
		public Object[] toArray() {
			return delegate.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return delegate.toArray(a);
		}

		@Override
		public boolean add(E e) {
			if (delegate.add(e)) {
				getChangesRecorder().append(e, changes);
				return true;
			}
			return false;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			// need to check if the value is in the collection otherwise the changes recorder cannot fetch dependent
			// value and added it to the changes
			// this is mainly used for map key set as the change happens before recording the old value
			if (delegate.contains((E) o)) {
				getChangesRecorder().remove((E) o, changes);
				return delegate.remove(o);
			}
			return false;
		}

		ChangesRecorder<E, C> getChangesRecorder() {
			if (changesRecorder == null) {
				changesRecorder = ChangesRecorder.collectionRecorder(assignedKey);
			}
			return changesRecorder;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return delegate.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			boolean changed = false;
			for (E e : c) {
				changed |= add(e);
			}
			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean changed = false;
			for (Object o : c) {
				changed |= remove(o);
			}
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			Iterator<E> it = iterator();
			boolean changed = false;
			while (it.hasNext()) {
				if (!c.contains(it.next())) {
					it.remove();
					changed = true;
				}
			}
			return changed;
		}

		@Override
		public void clear() {
			Iterator<E> it = iterator();
			while (it.hasNext()) {
				it.next();
				it.remove();
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof TrackedCollection)) {
				return false;
			}
			TrackedCollection<?, ?, ?> that = (TrackedCollection<?, ?, ?>) o;
			return Objects.equals(assignedKey, that.assignedKey) && Objects.equals(delegate, that.delegate);
		}

		@Override
		public int hashCode() {
			return Objects.hash(assignedKey, delegate);
		}

		@Override
		public String toString() {
			return delegate.toString();
		}
	}

	private static class ChangesRecorder<E, C> {
		private BiConsumer<E, PropertiesChanges<C>> onAdd;
		private BiConsumer<E, PropertiesChanges<C>> onAppend;
		private TriConsumer<E, E, PropertiesChanges<C>> onUpdate;
		private BiConsumer<E, PropertiesChanges<C>> onRemove;

		private ChangesRecorder(BiConsumer<E, PropertiesChanges<C>> onAdd,
				BiConsumer<E, PropertiesChanges<C>> onAppend,
				TriConsumer<E, E, PropertiesChanges<C>> onUpdate,
				BiConsumer<E, PropertiesChanges<C>> onRemove) {
			this.onAdd = onAdd;
			this.onAppend = onAppend;
			this.onUpdate = onUpdate;
			this.onRemove = onRemove;
		}

		static <E, C> ChangesRecorder<E, C> removeOnly(
				BiConsumer<E, PropertiesChanges<C>> onRemove) {
			return new ChangesRecorder<>(null, null, null, onRemove);
		}

		@SuppressWarnings("unchecked")
		static <E, S> ChangesRecorder<S, E> collectionRecorder(String key) {
			return new ChangesRecorder<>((value, c) -> c.add(key, (E) value), (value, c) -> c.append(key, (E) value),
					(newValue, oldValue, c) -> c.update(key, (E) newValue, (E) oldValue), (value, c) -> c.remove(key, (E) value));
		}

		void add(E addedValue, PropertiesChanges<C> changes) {
			if (changes != null) {
				onAdd.accept(addedValue, changes);
			}
		}

		void append(E addedValue, PropertiesChanges<C> changes) {
			if (changes != null) {
				onAppend.accept(addedValue, changes);
			}
		}

		void update(E newValue, E oldValue, PropertiesChanges<C> changes) {
			if (changes != null) {
				onUpdate.accept(newValue, oldValue, changes);
			}
		}

		void remove(E removedValue, PropertiesChanges<C> changes) {
			if (changes != null) {
				onRemove.accept(removedValue, changes);
			}
		}
	}

	private static class TrackedIterator<E, C> implements Iterator<E> {
		private final Iterator<E> delegate;
		private final PropertiesChanges<C> changes;
		private final ChangesRecorder<E, C> changesRecorder;
		private E previousValue;

		private TrackedIterator(Iterator<E> delegate, PropertiesChanges<C> changes,
				ChangesRecorder<E, C> changesRecorder) {
			this.delegate = delegate;
			this.changes = changes;
			this.changesRecorder = changesRecorder;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public E next() {
			previousValue = delegate.next();
			return previousValue;
		}

		@Override
		public void remove() {
			delegate.remove();
			changesRecorder.remove(previousValue, changes);
			previousValue = null;
		}
	}

	private static class TrackedSet<E, C> extends TrackedCollection<E, C, Set<E>>
			implements Set<E> {
		private final transient ChangesRecorder<E, C> changesRecorder;

		private TrackedSet(String assignedKey, Set<E> delegate, PropertiesChanges<C> changes) {
			super(assignedKey, delegate, changes);
			changesRecorder = null;
		}

		private TrackedSet(Set<E> delegate, PropertiesChanges<C> changes, ChangesRecorder<E, C> changesRecorder) {
			super(null, delegate, changes);
			this.changesRecorder = changesRecorder;
		}

		@Override
		ChangesRecorder<E, C> getChangesRecorder() {
			if (changesRecorder != null) {
				return changesRecorder;
			}
			return super.getChangesRecorder();
		}
	}

	private static class TrackedList<E, C> extends TrackedCollection<E, C, List<E>>
			implements List<E> {
		private TrackedList(String assignedKey, List<E> delegate, PropertiesChanges<C> changes) {
			super(assignedKey, delegate, changes);
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			ChangesRecorder<E, C> changesRecorder = getChangesRecorder();
			c.forEach(e -> changesRecorder.append(e, changes));
			return delegate.addAll(index, c);
		}

		@Override
		public E get(int index) {
			return delegate.get(index);
		}

		@Override
		public E set(int index, E element) {
			E previousValue = delegate.set(index, element);
			getChangesRecorder().update(element, previousValue, changes);
			return previousValue;
		}

		@Override
		public void add(int index, E element) {
			getChangesRecorder().append(element, changes);
			delegate.add(index, element);
		}

		@Override
		public E remove(int index) {
			E removed = delegate.remove(index);
			getChangesRecorder().remove(removed, changes);
			return removed;
		}

		@Override
		public int indexOf(Object o) {
			return delegate.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o) {
			return delegate.lastIndexOf(o);
		}

		@Override
		public ListIterator<E> listIterator() {
			return new TrackedListIterator<>(delegate.listIterator(), changes, getChangesRecorder());
		}

		@Override
		public ListIterator<E> listIterator(int index) {
			return new TrackedListIterator<>(delegate.listIterator(index), changes, getChangesRecorder());
		}

		@Override
		public List<E> subList(int fromIndex, int toIndex) {
			return new TrackedList<>(assignedKey, delegate.subList(fromIndex, toIndex), changes);
		}
	}

	private static class TrackedListIterator<E, C> implements ListIterator<E> {
		private final ListIterator<E> delegate;
		private final PropertiesChanges<C> changes;
		private final ChangesRecorder<E, C> changesRecorder;
		private E lastReturned;

		private TrackedListIterator(ListIterator<E> delegate, PropertiesChanges<C> changes,
				ChangesRecorder<E, C> changesRecorder) {
			this.delegate = delegate;
			this.changes = changes;
			this.changesRecorder = changesRecorder;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public E next() {
			lastReturned = delegate.next();
			return lastReturned;
		}

		@Override
		public boolean hasPrevious() {
			return delegate.hasPrevious();
		}

		@Override
		public E previous() {
			lastReturned = delegate.previous();
			return lastReturned;
		}

		@Override
		public int nextIndex() {
			return delegate.nextIndex();
		}

		@Override
		public int previousIndex() {
			return delegate.previousIndex();
		}

		@Override
		public void remove() {
			delegate.remove();
			changesRecorder.remove(lastReturned, changes);
		}

		@Override
		public void set(E e) {
			delegate.set(e);
			changesRecorder.update(e, lastReturned, changes);
			lastReturned = e;
		}

		@Override
		public void add(E e) {
			delegate.add(e);
			changesRecorder.append(e, changes);
		}
	}
}
