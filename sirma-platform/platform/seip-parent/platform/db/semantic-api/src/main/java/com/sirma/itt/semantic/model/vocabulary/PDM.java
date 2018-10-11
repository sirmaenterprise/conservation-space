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
 * PDM Ontology Model
 *
 * @author kirq4e
 */
public final class PDM {

	/** Namespace of the ontology */
	public static final String NAMESPACE = "http://www.sirma.com/ontologies/2013/10/pdmConfiguration#";

	/**
	 * Recommended prefix for the Ontology
	 */
	public static final String PREFIX = "pdm";

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

	public static final IRI PDM_DOMAIN_OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		// init classes and properties
		ValueFactory factory = SimpleValueFactory.getInstance();
		PDM_DOMAIN_OBJECT = factory.createIRI(NAMESPACE, "PDMDomainObject");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private PDM() {
	}
}
