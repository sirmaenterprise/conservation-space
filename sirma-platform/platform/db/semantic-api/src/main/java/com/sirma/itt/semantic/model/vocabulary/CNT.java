package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////
	public static final URI CHARS;
	public static final URI CHARACTER_ENCODING;
	public static final URI BYTES;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		// init context URIs

		// init Class URIs

		// init property URIs
		CHARS = factory.createURI(NAMESPACE, "chars");
		CHARACTER_ENCODING = factory.createURI(NAMESPACE, "characterEncoding");
		BYTES = factory.createURI(NAMESPACE, "bytes");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private CNT() {
		// utility class
	}
}
