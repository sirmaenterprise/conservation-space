/**
 *
 */
package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Cultural heritage domain Ontology model
 *
 * @author kirq4e
 */
public final class CHD {

	/** Namespace of the ontology */
	public static final String NAMESPACE = "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#";

	/**
	 * Recommended prefix for the Ontology
	 */
	public static final String PREFIX = "chd";

	/**
	 * An immutable {@link Namespace} constant that represents the Inteligent Document namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////
	/**
	 * Cultural Object
	 */
	public static final URI CULTURAL_OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		// init classes and properties
		ValueFactory factory = ValueFactoryImpl.getInstance();
		CULTURAL_OBJECT = factory.createURI(NAMESPACE, "CulturalObject");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private CHD() {
		// nothing to do
	}
}
