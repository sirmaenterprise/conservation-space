package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	public static final IRI ANNOTATION;
	public static final IRI SPECIFIC_RESOURCE;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////
	public static final IRI HAS_SOURCE;
	public static final IRI MOTIVATED_BY;
	public static final IRI HAS_TARGET;
	public static final IRI HAS_BODY;

	public static final IRI COMMENTING;
	public static final IRI EDITING;


	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		// init context URIs

		// init Class URIs
		ANNOTATION = factory.createIRI(NAMESPACE, "Annotation");
		SPECIFIC_RESOURCE = factory.createIRI(NAMESPACE, "SpecificResource");

		// init property URIs
		HAS_SOURCE = factory.createIRI(NAMESPACE, "hasSource");
		MOTIVATED_BY = factory.createIRI(NAMESPACE, "motivatedBy");
		HAS_TARGET = factory.createIRI(NAMESPACE, "hasTarget");
		HAS_BODY = factory.createIRI(NAMESPACE, "hasBody");

		COMMENTING = factory.createIRI(NAMESPACE, "commenting");
		EDITING = factory.createIRI(NAMESPACE, "editing");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private OA() {
	}
}
