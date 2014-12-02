package com.sirma.itt.emf.db;

import java.io.Serializable;
import java.util.UUID;

/**
 * Default {@link DbIdGenerator} implementation that uses UUID to generate the database ids.
 * 
 * @author BBonev
 */
public class DefaultDbIdGenerator implements DbIdGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable generateId() {
		return "emf:" + UUID.randomUUID().toString();
	}

}
