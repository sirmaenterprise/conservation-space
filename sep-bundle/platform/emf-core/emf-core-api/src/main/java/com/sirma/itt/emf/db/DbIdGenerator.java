package com.sirma.itt.emf.db;

import java.io.Serializable;

/**
 * The Interface DbIdGenerator.
 * 
 * @author BBonev
 */
public interface DbIdGenerator {

	/**
	 * Generate id.
	 * 
	 * @return the serializable
	 */
	Serializable generateId();
}
