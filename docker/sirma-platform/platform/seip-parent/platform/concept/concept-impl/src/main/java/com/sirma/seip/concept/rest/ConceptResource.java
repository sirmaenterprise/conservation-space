package com.sirma.seip.concept.rest;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.seip.concept.Concept;
import com.sirma.seip.concept.ConceptService;

/**
 * Provides rest endpoints for retrieving and managing skos models.
 * 
 * @author Vilizar Tsonev
 */
@Path("concept")
@Transactional(TxType.REQUIRED)
@Produces(Versions.V2_JSON)
@Singleton
public class ConceptResource {

	@Inject
	private ConceptService conceptService;

	/**
	 * Retrieves the concept hierarchy according to the passed scheme or broader. If a broader is provided, the scheme
	 * parameter is ignored, and the tree(s) down from given concept is returned. The hierarchy is built according to
	 * the skos:broader relationship
	 * 
	 * @param scheme
	 *            is the scheme id
	 * @param broader
	 *            (optional) is a concept id to get a sub-tree(s) from.
	 * @return the concept hierarchy
	 */
	@GET
	public List<Concept> getConceptHierarchy(@QueryParam("scheme") String scheme,
			@QueryParam("broader") String broader) {
		if (StringUtils.isNotBlank(broader)) {
			return conceptService.getConceptsByBroader(broader);
		}
		return conceptService.getConceptsByScheme(scheme);
	}

}
