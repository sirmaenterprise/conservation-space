package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	/**
	 * http://purl.org/dc/dcmitype/Text
	 */
	public static final URI TEXT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		// init context URIs

		// init Class URIs
		TEXT = factory.createURI(NAMESPACE, "Text");

		// init property URIs
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private DCMI() {
		// utility class
	}
}
