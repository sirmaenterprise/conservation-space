package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Vocabulary for Proton Top ontology of Ontotext
 *
 * @author kirq4e
 */
public final class Proton {

	/** http://www.ontotext.com/proton/protontop# */
	public static final String NAMESPACE = "http://www.ontotext.com/proton/protontop#";

	/**
	 * Recommended prefix for the InteligentDocument namespace: "ptop"
	 */
	public static final String PREFIX = "ptop";

	/**
	 * An immutable {@link Namespace} constant that represents the Proton namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	/**
	 * Person class
	 */
	public static final IRI PERSON;

	/**
	 * Agent class
	 */
	public static final IRI AGENT;

	/**
	 * Document class
	 */
	public static final IRI DOCUMENT;

	/**
	 * Object class
	 */
	public static final IRI OBJECT;

	/**
	 * Entity class
	 */
	public static final IRI ENTITY;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	/**
	 * Part of property
	 */
	public static final IRI PART_OF;

	/**
	 * hasDate property
	 */
	public static final IRI HAS_DATE;

	/**
	 * hasMember property
	 */
	public static final IRI HAS_MEMBER;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		// init Class URIs
		PERSON = factory.createIRI(NAMESPACE, "Person");
		AGENT = factory.createIRI(NAMESPACE, "Agent");
		DOCUMENT = factory.createIRI(NAMESPACE, "Document");
		OBJECT = factory.createIRI(NAMESPACE, "Object");
		ENTITY = factory.createIRI(NAMESPACE, "Entity");
		// init property URIs
		HAS_DATE = factory.createIRI(NAMESPACE, "hasDate");
		PART_OF = factory.createIRI(NAMESPACE, "partOf");
		HAS_MEMBER = factory.createIRI(NAMESPACE, "hasMember");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private Proton() {
	}
}
