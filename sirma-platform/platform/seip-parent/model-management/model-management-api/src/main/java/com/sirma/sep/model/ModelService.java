package com.sirma.sep.model;

import java.util.List;

/**
 * Provides a common entry point for accessing and manipulating the system data model.
 * 
 * @author Vilizar Tsonev
 */
public interface ModelService {
	
	/**
	 * Gets all ontologies available in the system.
	 * 
	 * @return a list of all available ontologies
	 */
	List<Ontology> getOntologies();

	/**
	 * Gets all classes belonging to the provided ontology.
	 * 
	 * @param ontologyId
	 *            is the ontology ID (URI)
	 * @return a list of all classes belonging to that ontology
	 */
	List<ClassInfo> getClassesForOntology(String ontologyId);

}
