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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	public static final URI PDM_DOMAIN_OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		// init classes and properties
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PDM_DOMAIN_OBJECT = factory.createURI(NAMESPACE, "PDMDomainObject");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private PDM() {
	}
}
