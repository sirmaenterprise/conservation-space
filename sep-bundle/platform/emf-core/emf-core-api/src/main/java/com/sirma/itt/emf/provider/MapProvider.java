package com.sirma.itt.emf.provider;

import java.util.Map;

/**
 * Specific provider that works with map objects
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author BBonev
 */
public interface MapProvider<K, V> extends Provider<Map<K, V>> {

}
