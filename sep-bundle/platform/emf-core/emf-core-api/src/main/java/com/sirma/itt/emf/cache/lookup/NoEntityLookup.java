package com.sirma.itt.emf.cache.lookup;

import java.io.Serializable;

import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.domain.Pair;

/**
 * Empty implementation for {@link EntityLookupCallbackDAOAdaptor} used when no lookup is needed to
 * use entity cache functions
 * 
 * @author BBonev
 */
public class NoEntityLookup extends
		EntityLookupCallbackDAOAdaptor<Serializable, Object, Serializable> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Serializable, Object> findByKey(Serializable key) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Serializable, Object> createValue(Object value) {
		return null;
	}
}