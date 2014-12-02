package com.sirma.itt.objects.web.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;

import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.domain.StringPair;
import com.sirma.itt.emf.instance.model.PropertyInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * REST service for working with relationships.
 * 
 * @author yasko
 * 
 */
@ApplicationScoped
@Path("/relationships")
@Produces(MediaType.APPLICATION_JSON)
public class RelationshipsRestService extends EmfRestService {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	
	/**
	 * Retrieves all possible relationships between two objects, or optionally
	 * filtered by domain and range classes. Note that this method returns a
	 * union rather then an intersection if both filters are supplied.
	 * 
	 * @param domainFilter
	 *            List of class URIs to filter a relationship by it'sdomain
	 *            class.
	 * @param rangeFilter
	 *            List of class URIs to filter a relationship by it's range
	 *            class.
	 * @return A JSON array containing all matched relationships or all
	 *         relationships if no filters are applied.
	 */
	@GET
	public String retrieveRelationshipsForTypes(
			@QueryParam("domainFilter") List<String> domainFilter,
			@QueryParam("rangeFilter") List<String> rangeFilter) {
		
		boolean dommainFilterEmpty = CollectionUtils.isEmpty(domainFilter);
		boolean rangeFilterEmpty = CollectionUtils.isEmpty(rangeFilter);
		
		Set<PropertyInstance> allTheRelationships = new HashSet<>(semanticDefinitionService.getRelations());
		
		if (!dommainFilterEmpty) {
			/*
			 * If there are domain class filters get them all in a set, plus all
			 * of their super types
			 */
			Set<String> allTheDomainClassFilters = new HashSet<>();
			for (String domainClassId : domainFilter) {
				allTheDomainClassFilters.addAll(semanticDefinitionService.getHierarchy(domainClassId));
			}
			
			Iterator<PropertyInstance> iterator = allTheRelationships.iterator();
			/*
			 * iterate all relationships and if there's a relationship with a
			 * domain class that is not in the set of domain classes from the
			 * filter - remove the relationship.
			 */
			while (iterator.hasNext()) {
				PropertyInstance rel = iterator.next();
				if (!allTheDomainClassFilters.contains(rel.getDomainClass())) {
					iterator.remove();
				}
			}
		}
		
		// same as the domain filter
		if (!rangeFilterEmpty) {
			Set<String> allTheRangeClassFilters = new HashSet<>();
			for (String rangeClassId : rangeFilter) {
				allTheRangeClassFilters.addAll(semanticDefinitionService.getHierarchy(rangeClassId));
			}
			
			Iterator<PropertyInstance> iterator = allTheRelationships.iterator();
			while (iterator.hasNext()) {
				PropertyInstance rel = iterator.next();
				if (!allTheRangeClassFilters.contains(rel.getRangeClass())) {
					iterator.remove();
				}
			}
		}

		return convertPropertyInstancesToJson(allTheRelationships).toString();
	}
	
	/**
	 * Converts a collection of {@link PropertyInstance}s to a JSON array.
	 * 
	 * @param properties
	 *            Collection of properties to convert.
	 * @param additionalFields
	 *            Optional list of additional field to include to the JSON
	 *            property representation.
	 * @return A {@link JSONArray} with the converted properties.
	 */
	public JSONArray convertPropertyInstancesToJson(Collection<PropertyInstance> properties, StringPair ...additionalFields) {
		StringPair[] props = { 
				new StringPair(DefaultProperties.NAME, "instance"),
				new StringPair(DefaultProperties.TITLE, DefaultProperties.TITLE),
				new StringPair("domainClass", "domainClass"), 
				new StringPair("rangeClass", "rangeClass") 
		};
		
		if (additionalFields != null && additionalFields.length > 0) {
			props = (StringPair[]) ArrayUtils.addAll(props, additionalFields);
		}
		JSONArray result = new JSONArray();
		for (PropertyInstance propertyInstance : properties) {
			result.put(JsonUtil.transformInstance(propertyInstance, props));
		}
		return result;
	}
}
