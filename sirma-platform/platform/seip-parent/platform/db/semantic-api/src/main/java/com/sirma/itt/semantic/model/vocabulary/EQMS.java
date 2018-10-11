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
 * eQMS Ontology model
 *
 * @author kirq4e
 */
public final class EQMS {

	/** Namespace of the ontology */
	public static final String NAMESPACE = "http://www.sirma.com/ontologies/2014/03/eQMS#";

	/**
	 * Recommended prefix for the Ontology
	 */
	public static final String PREFIX = "eqms";

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
	 * eQMSObject
	 */
	public static final IRI EQMS_OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		// init classes and properties
		ValueFactory factory = SimpleValueFactory.getInstance();
		EQMS_OBJECT = factory.createIRI(NAMESPACE, "eQMSObject");

	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private EQMS() {
	}
}
