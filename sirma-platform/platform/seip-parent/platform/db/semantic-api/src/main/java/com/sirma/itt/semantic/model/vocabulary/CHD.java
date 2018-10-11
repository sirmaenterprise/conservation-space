/**
 *
 */
package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////
	/**
	 * Cultural Object
	 */
	public static final IRI CULTURAL_OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		// init classes and properties
		ValueFactory factory = SimpleValueFactory.getInstance();
		CULTURAL_OBJECT = factory.createIRI(NAMESPACE, "CulturalObject");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private CHD() {
		// nothing to do
	}
}
