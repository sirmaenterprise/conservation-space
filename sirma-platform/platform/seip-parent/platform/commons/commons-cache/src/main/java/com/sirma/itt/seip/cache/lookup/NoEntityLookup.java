package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;

import com.sirma.itt.seip.Pair;

/**
 * Empty implementation for {@link EntityLookupCallbackDAOAdaptor} used when no lookup is needed to use entity cache
 * functions
 *
 * @author BBonev
 */
public class NoEntityLookup extends EntityLookupCallbackDAOAdaptor<Serializable, Object, Serializable> {

	@Override
	public Pair<Serializable, Object> findByKey(Serializable key) {
		return null;
	}

	@Override
	public Pair<Serializable, Object> createValue(Object value) {
		return null;
	}
}