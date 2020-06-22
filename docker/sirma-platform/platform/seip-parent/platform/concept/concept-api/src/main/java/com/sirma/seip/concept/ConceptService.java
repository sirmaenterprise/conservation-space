package com.sirma.seip.concept;

import java.util.List;

/**
 * Provides functionality to retrieve and manage skos models.
 * 
 * @author Vilizar Tsonev
 */
public interface ConceptService {

	/**
	 * Retrieves the concept hierarchy according to the passed scheme. The hierarchy is built according to the
	 * skos:broader relationship
	 * 
	 * @param schemeId
	 *            is the scheme id
	 * @return the concept hierarchy
	 */
	List<Concept> getConceptsByScheme(String schemeId);

	/**
	 * Retrieves the concept tree(s) down from given concept (broader). The hierarchy is built according to the
	 * skos:broader relationship
	 * 
	 * @param broaderId
	 *            is the id of the broader concept
	 * @return the concept hierarchy
	 */
	List<Concept> getConceptsByBroader(String broaderId);

}
