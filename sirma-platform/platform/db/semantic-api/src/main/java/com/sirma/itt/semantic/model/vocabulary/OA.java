package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary for Open Annotation ontology
 *
 * @author kirq4e
 */
public class OA {

	/** http://www.w3.org/ns/oa# */
	public static final String NAMESPACE = "http://www.w3.org/ns/oa#";

	/**
	 * Recommended prefix for the Open Annotation namespace: "oa"
	 */
	public static final String PREFIX = "oa";

	/**
	 * An immutable {@link Namespace} constant that represents the Open Annotation Namespace
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	public static final URI ANNOTATION;
	public static final URI SPECIFIC_RESOURCE;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////
	public static final URI HAS_SOURCE;
	public static final URI MOTIVATED_BY;
	public static final URI HAS_TARGET;
	public static final URI HAS_BODY;

	public static final URI COMMENTING;
	public static final URI EDITING;


	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		// init context URIs

		// init Class URIs
		ANNOTATION = factory.createURI(NAMESPACE, "Annotation");
		SPECIFIC_RESOURCE = factory.createURI(NAMESPACE, "SpecificResource");

		// init property URIs
		HAS_SOURCE = factory.createURI(NAMESPACE, "hasSource");
		MOTIVATED_BY = factory.createURI(NAMESPACE, "motivatedBy");
		HAS_TARGET = factory.createURI(NAMESPACE, "hasTarget");
		HAS_BODY = factory.createURI(NAMESPACE, "hasBody");

		COMMENTING = factory.createURI(NAMESPACE, "commenting");
		EDITING = factory.createURI(NAMESPACE, "editing");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private OA() {
	}
}
