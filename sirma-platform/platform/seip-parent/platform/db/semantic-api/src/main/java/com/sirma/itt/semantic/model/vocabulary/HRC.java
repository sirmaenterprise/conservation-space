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
 * HR Ontology model
 *
 * @author kirq4e
 */
public final class HRC {

	/** Namespace of the ontology */
	public static final String NAMESPACE = "http://www.sirma.com/ontologies/2014/05/hrConfiguration#";

	/**
	 * Recommended prefix for the Ontology
	 */
	public static final String PREFIX = "hrc";

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
	public static final IRI HR_OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		// init classes and properties
		ValueFactory factory = SimpleValueFactory.getInstance();
		HR_OBJECT = factory.createIRI(NAMESPACE, "HRObjects");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private HRC() {
	}
}
