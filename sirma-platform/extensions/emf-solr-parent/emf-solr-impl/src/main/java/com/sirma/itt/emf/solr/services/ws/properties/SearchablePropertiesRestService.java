package com.sirma.itt.emf.solr.services.ws.properties;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.search.SearchablePropertiesService;

/**
 * Handles requests for searchable properties retrieval from the solr and semantic services.
 *
 * @author Mihail Radkov
 */
@ApplicationScoped
@Path("/properties/searchable")
public class SearchablePropertiesRestService extends EmfRestService {

	@Inject
	private SearchablePropertiesService searchablePropertiesService;

	/**
	 * Retrieves the searchable definition properties for the provided type that have a corresponding property in the
	 * semantics and intersects them with those indexed in Solr if specified.
	 *
	 * @param forType
	 *            the for type. If getting a definition's properties, the semantic short type of the definition should
	 *            be attached to her with a '_' symbol e.g. (emf:Project_PRJ10001)
	 * @param commonOnly
	 *            If the service should return the intersection of the properties.
	 * @param multiValued
	 *            Indicates whether the definition properties should be compared to the multi-valued fields in solr or
	 *            to their single valued equivalents.
	 * @param faceted
	 *            indicates whether the definition properties should be compared to the faceted fields in solr. Faceted
	 *            fields in solr are marked as such in the solr connector. This is used to limit the returned results
	 *            for the faceted search.
	 * @param skipObjectProperties
	 *            if the service should return object properties or to filter them from the result
	 * @return the fields by type
	 */
	@GET
	@Path("/solr")
	public Response getSearchableSolrFields(@QueryParam("forType") String forType,
			@DefaultValue("false") @QueryParam("commonOnly") boolean commonOnly,
			@DefaultValue("true") @QueryParam("multiValued") boolean multiValued,
			@DefaultValue("false") @QueryParam("faceted") boolean faceted,
			@DefaultValue("false") @QueryParam("skipObjectProperties") boolean skipObjectProperties) {
		List<SearchableProperty> properties = searchablePropertiesService.getSearchableSolrProperties(forType,
				commonOnly, multiValued, skipObjectProperties);
		JSONArray result = new JSONArray();
		for (SearchableProperty property : properties) {
			result.put(JsonRepresentable.toJson(property));
		}
		return buildOkResponse(result.toString());
	}

	/**
	 * Retrieves the searchable definition properties for the provided type that have a corresponding property in the
	 * semantics but does <b>not</b> intersect them with those indexed in Solr.
	 *
	 * @param forType
	 *            the for type. If getting a definition's properties, the semantic short type of the definition should
	 *            be attached to her with a '_' symbol e.g. (emf:Project_PRJ10001)
	 * @return the fields by type
	 */
	@GET
	@Path("/semantic")
	public Response getSearchableSemanticFields(@QueryParam("forType") String forType) {
		List<SearchableProperty> properties = searchablePropertiesService.getSearchableSemanticProperties(forType);
		JSONArray result = new JSONArray();
		for (SearchableProperty property : properties) {
			result.put(JsonRepresentable.toJson(property));
		}
		return buildOkResponse(result.toString());
	}

	/**
	 * Invokes the searchable properties reloading.
	 */
	@PUT
	@Path("/reload")
	public void reloadProperties() {
		searchablePropertiesService.reset();
	}

}