package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * @author kirq4e
 */
public class Connectors {

	/** http://www.sirma.com/ontologies/2017/06/connectors# */
	public static final String NAMESPACE = "http://www.sirma.com/ontologies/2017/06/connectors#";

	/**
	 * Recommended prefix for the Connector ontology: cnctr
	 */
	public static final String PREFIX = "cnctr";

	/**
	 * An immutable {@link Namespace} constant that represents the Inteligent Document namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////
	public static final IRI CONNECTORS_DATA_GRAPH;

	public static final IRI DEFAULT_CONNECTOR;
	
	public static final IRI CONNECTOR;
	
	public static final IRI IS_SORTABLE;
	public static final IRI HAS_FIELD;

	public static final IRI INITIALIZATION_DATA;
	public static final IRI RECREATED_ON;
	public static final IRI ADDRESS;
	public static final IRI CONNECTOR_NAME;
	public static final IRI IS_DEFAULT_CONNECTOR;

	public static final IRI RECREATE;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		// init default connector IRI
		DEFAULT_CONNECTOR = factory.createIRI(NAMESPACE, "default_fts");
		CONNECTORS_DATA_GRAPH = factory.createIRI("http://www.sirma.com/ontologies/2017/06/connectors/data");
		
		CONNECTOR = factory.createIRI(NAMESPACE, "Connector");
		
		IS_SORTABLE = factory.createIRI(NAMESPACE, "isSortable");
		HAS_FIELD = factory.createIRI(NAMESPACE, "hasField");
		INITIALIZATION_DATA = factory.createIRI(NAMESPACE, "initializationData");
		RECREATED_ON = factory.createIRI(NAMESPACE, "recreatedOn");
		ADDRESS = factory.createIRI(NAMESPACE, "address");
		CONNECTOR_NAME = factory.createIRI(NAMESPACE, "connectorName");
		IS_DEFAULT_CONNECTOR = factory.createIRI(NAMESPACE, "isDefaultConnector");
		RECREATE = factory.createIRI(NAMESPACE, "recreate");
	}


	/**
	 * Hide default constructor
	 */
	private Connectors() {
	}

}
