package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Vocabulary for Content ontology
 *
 * @author BBonev
 */
public class CNT {

	/** http://www.w3.org/2011/content# */
	public static final String NAMESPACE = "http://www.w3.org/2011/content#";

	/**
	 * Recommended prefix for the Content namespace: "cnt"
	 */
	public static final String PREFIX = "cnt";

	/**
	 * An immutable {@link Namespace} constant that represents the Content Namespace
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////
	public static final IRI CHARS;
	public static final IRI CHARACTER_ENCODING;
	public static final IRI BYTES;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		// init context IRIs

		// init Class IRIs

		// init property IRIs
		CHARS = factory.createIRI(NAMESPACE, "chars");
		CHARACTER_ENCODING = factory.createIRI(NAMESPACE, "characterEncoding");
		BYTES = factory.createIRI(NAMESPACE, "bytes");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private CNT() {
		// utility class
	}
}
