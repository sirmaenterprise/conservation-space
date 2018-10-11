package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import com.sirma.itt.seip.Pair;

/**
 * {@link EntityLookupCallbackDAO} adaptor for read only single key entries. The only method that need to be implemented
 * is the {@link #findByKey(Serializable)}
 *
 * @param <K>
 *            the primary key type
 * @param <V>
 *            the value type
 * @since 2017-03-27
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public abstract class ReadOnlyEntityLookupCallbackDAOAdaptor<K extends Serializable, V>
		extends EntityLookupCallbackDAOAdaptor<K, V, Serializable> {

	/**
	 * Return a {@link EntityLookupCallbackDAO} instance that acts as {@link ReadOnlyEntityLookupCallbackDAOAdaptor}
	 *
	 * @param keyValueMapper
	 *            key value mapping functions that provides a value based on the given key. Required
	 * @return implementation of {@link EntityLookupCallbackDAO} that provides values by primary key
	 */
	public static <A extends Serializable, R> EntityLookupCallbackDAO<A, R, Serializable> from(
			Function<A, R> keyValueMapper) {
		Objects.requireNonNull(keyValueMapper, "Key/value mapping function is required");
		return new ReadOnlyEntityLookupCallback<>(keyValueMapper);
	}

	@Override
	public Pair<K, V> createValue(V value) {
		throw new UnsupportedOperationException("Objects are created externally");
	}

	private static final class ReadOnlyEntityLookupCallback<K extends Serializable, V>
			extends ReadOnlyEntityLookupCallbackDAOAdaptor<K, V> {
		private final Function<K, V> keyValueMapper;

		ReadOnlyEntityLookupCallback(Function<K, V> keyValueMapper) {
			this.keyValueMapper = keyValueMapper;
		}

		@Override
		public Pair<K, V> findByKey(K key) {
			V value = keyValueMapper.apply(key);
			if (value != null) {
				return new Pair<>(key, value);
			}
			return null;
		}
	}
}
