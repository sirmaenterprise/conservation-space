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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////
	/**
	 * eQMSObject
	 */
	public static final URI EQMS_OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		// init classes and properties
		ValueFactory factory = ValueFactoryImpl.getInstance();
		EQMS_OBJECT = factory.createURI(NAMESPACE, "eQMSObject");

	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private EQMS() {
	}
}
