package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary for Proton Top ontology of Ontotext
 * 
 * @author kirq4e
 */
public class Proton {

	/** http://www.ontotext.com/proton/protontop# */
	public static final String NAMESPACE = "http://www.ontotext.com/proton/protontop#";

	/**
	 * Recommended prefix for the InteligentDocument namespace: "ptop"
	 */
	public static final String PREFIX = "ptop";

	/**
	 * An immutable {@link Namespace} constant that represents the Proton namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	/**
	 * Person class
	 */
	public static final URI PERSON;

	/**
	 * Agent class
	 */
	public static final URI AGENT;

	/**
	 * Document class
	 */
	public static final URI DOCUMENT;

	/**
	 * Object class
	 */
	public static final URI OBJECT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	/**
	 * Part of property
	 */
	public static final URI PART_OF;

	/**
	 * hasDate property
	 */
	public static final URI HAS_DATE;

	/**
	 * hasMember property
	 */
	public static final URI HAS_MEMBER;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		// init Class URIs
		PERSON = factory.createURI(NAMESPACE, "Person");
		AGENT = factory.createURI(NAMESPACE, "Agent");
		DOCUMENT = factory.createURI(NAMESPACE, "Document");
		OBJECT = factory.createURI(NAMESPACE, "Object");
		// init property URIs
		HAS_DATE = factory.createURI(NAMESPACE, "hasDate");
		PART_OF = factory.createURI(NAMESPACE, "partOf");
		HAS_MEMBER = factory.createURI(NAMESPACE, "hasMember");
	}

}
