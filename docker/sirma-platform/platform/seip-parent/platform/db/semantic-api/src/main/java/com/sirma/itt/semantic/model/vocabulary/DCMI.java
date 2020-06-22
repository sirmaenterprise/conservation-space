package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Vocabulary for DCMI Metadata Terms ontology
 *
 * @author BBonev
 */
public class DCMI {

	/** http://purl.org/dc/dcmitype/ */
	public static final String NAMESPACE = "http://purl.org/dc/dcmitype/";

	/**
	 * Recommended prefix for the DCMI Metadata namespace: "dctypes"
	 */
	public static final String PREFIX = "dctypes";

	/**
	 * An immutable {@link Namespace} constant that represents the DCMI Metadata Namespace
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	/**
	 * http://purl.org/dc/dcmitype/Text
	 */
	public static final IRI TEXT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		// init context IRIs

		// init Class IRIs
		TEXT = factory.createIRI(NAMESPACE, "Text");

		// init property IRIs
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private DCMI() {
		// utility class
	}
}
