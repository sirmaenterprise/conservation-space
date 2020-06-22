package com.sirma.itt.emf.solr.services.impl.facet;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.search.SearchablePropertiesService;
import com.sirma.itt.seip.search.facet.FacetArgumentTransformer;
import com.sirma.itt.seip.search.facet.FacetConfigurationProvider;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Helper class for preparing and transforming arguments for the faceted search.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public class FacetArgumentTransformerImpl implements FacetArgumentTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String REQUEST_FILTER_QUERIES = "fq[]";
	private static final String REQUEST_FOR_TYPE = "forType";

	private static final String RDF_TYPE = "rdfType";

	@Inject
	private FacetConfigurationProperties configuration;

	@Inject
	private FacetConfigurationProvider facetConfigurationProvider;

	@Inject
	private SearchablePropertiesService searchablePropertiesService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void populateFacets(SearchRequest request, S searchArgs) {
		Map<String, Facet> facets;
		boolean ignoreFacetConfiguration = request
				.getFirstBoolean(FacetQueryParameters.REQUEST_IGNORE_FACET_CONFIGURATION);
		if (StringUtils.isNotBlank(request.getFirst(FacetQueryParameters.REQUEST_FACET_FIELD))) {
			// If the facets are already provided in the search request (e.g. from the basic/advanced search).
			facets = CollectionUtils.createLinkedHashMap(request.get(FacetQueryParameters.REQUEST_FACET_FIELD).size());
			for (Facet facet : extractFacetsFromRequest(request)) {
				addFacet(facets, facet, ignoreFacetConfiguration);
			}
		} else {
			// If the facets are not provided in the search request (e.g. from the reporting widget).
			List<SearchableProperty> searchableSolrProperties = getSearchableSolrProperties(request);

			// Faster for parsing the response later
			facets = CollectionUtils.createLinkedHashMap(searchableSolrProperties.size());
			addFacet(facets, createRdfTypeFacet(), ignoreFacetConfiguration);
			for (SearchableProperty property : searchableSolrProperties) {
				addFacet(facets, searchablePropertyToFacet(property), ignoreFacetConfiguration);
			}
		}
		searchArgs.setFacets(facets);
	}

	/**
	 * Add the facet to the list of facets that are going to be requested and add it's configuration and text if it's
	 * configured.
	 *
	 * @param facets
	 *            the list of facets that are going to be used for faceting
	 * @param facet
	 *            the facet to be added
	 * @param ignoreFacetConfiguration
	 *            if this is true, the facet will not be added if it is not configured
	 */
	private void addFacet(Map<String, Facet> facets, Facet facet, boolean ignoreFacetConfiguration) {
		FacetConfiguration facetConfiguration = facetConfigurationProvider.getFacetConfigField(facet.getId());
		boolean addToResult = (facetConfiguration != null) || ignoreFacetConfiguration;
		if (facetConfiguration != null) {
			facet.setFacetConfiguration(facetConfiguration);
			facet.setText(facetConfiguration.getLabel());
		}
		if (addToResult) {
			facets.put(facet.getId(), facet);
		} else {
			LOGGER.trace("{} has no configuration and will be skipped.", facet.getId());
		}
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void preserveFacetSelections(SearchRequest request,
			S searchArgs) {
		List<String> facetArguments = request.get(FacetQueryParameters.REQUEST_FACET_ARGUMENTS);
		String forType = request.getFirst(REQUEST_FOR_TYPE);
		if (CollectionUtils.isEmpty(facetArguments)) {
			return;
		}
		searchArgs.setFacetsWithSelectedValues(new ArrayList<>(facetArguments.size()));
		for (String argument : facetArguments) {
			int indexOf = argument.indexOf(FacetQueryParameters.ARGUMENT_SEPARATOR);
			if (indexOf < 0) {
				continue;
			}
			String id = argument.substring(0, indexOf);
			String value = argument.substring(indexOf + 1);
			Facet facet = null;
			if (searchArgs.getFacets().get(id) != null) {
				facet = searchArgs.getFacets().get(id);
			} else if (RDF_TYPE.equals(id)) {
				facet = createRdfTypeFacet();
			} else {
				Optional<SearchableProperty> searchableProperty = searchablePropertiesService
						.getSearchableProperty(forType, id);
				if (searchableProperty.isPresent()) {
					facet = searchablePropertyToFacet(searchableProperty.get());
				}
			}
			if (facet != null) {
				Set<String> selectedValues = facet.getSelectedValues();
				if (selectedValues == null) {
					selectedValues = CollectionUtils.createLinkedHashSet(facetArguments.size());
					facet.setSelectedValues(selectedValues);
				}
				selectedValues.add(value);
				searchArgs.getFacetsWithSelectedValues().add(facet);
			} else {
				LOGGER.trace("There is no facet for {}.", id);
			}
		}
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void mergeAdditionalFilterQueries(SearchRequest request,
			S searchArgs) {
		List<String> filterQueries = request.get(REQUEST_FILTER_QUERIES);
		if (CollectionUtils.isEmpty(filterQueries)) {
			return;
		}

		// TODO: How to calculate the size for this.. ?
		StringBuilder enrichedFilters = new StringBuilder();
		String existingFilters = (String) searchArgs.getArguments().get(CommonParams.FQ);
		if (StringUtils.isNotBlank(existingFilters)) {
			enrichedFilters.append(existingFilters).append(" AND ");
		}

		boolean appendAnd = false;
		for (String filterQuery : filterQueries) {
			int indexOf = filterQuery.indexOf(FacetQueryParameters.ARGUMENT_SEPARATOR);
			if (indexOf > -1) {
				String id = filterQuery.substring(0, indexOf);
				String value = filterQuery.substring(indexOf + 1);
				if (appendAnd) {
					enrichedFilters.append(" AND ");
				} else {
					appendAnd = true;
				}
				enrichedFilters.append(id).append(FacetQueryParameters.ARGUMENT_SEPARATOR).append(value);
			}
		}

		if (enrichedFilters.length() > 0) {
			searchArgs.getArguments().put(CommonParams.FQ, enrichedFilters.toString());
		}
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void prepareDateArguments(SearchRequest request,
			S arguments) {
		Map<String, Serializable> facetArguments = arguments.getFacetArguments();

		String startDate = request.getFirst(FacetParams.FACET_DATE_START);
		if (StringUtils.isBlank(startDate)) {
			startDate = configuration.getDateStart().get();
		}
		facetArguments.put(FacetParams.FACET_DATE_START, startDate);

		String endDate = request.getFirst(FacetParams.FACET_DATE_END);
		if (StringUtils.isBlank(endDate)) {
			endDate = configuration.getDateEnd().get();
		}
		facetArguments.put(FacetParams.FACET_DATE_END, endDate);

		String dateGap = request.getFirst(FacetParams.FACET_DATE_GAP);
		if (StringUtils.isBlank(dateGap)) {
			dateGap = configuration.getDateGap().get();
		}
		facetArguments.put(FacetParams.FACET_DATE_GAP, dateGap);

		String other = request.getFirst(FacetParams.FACET_DATE_OTHER);
		if (StringUtils.isBlank(other)) {
			other = configuration.getOther().get();
		}
		facetArguments.put(FacetParams.FACET_DATE_OTHER, other);
	}

	/**
	 * Based on the provided search request, retrieves searchable Solr properties to create {@link Facet}s.
	 *
	 * @param request
	 *            - the given search request
	 * @return list of searchable properties
	 */
	private List<SearchableProperty> getSearchableSolrProperties(SearchRequest request) {
		String selectedObjectTypes = request.getFirst(REQUEST_FOR_TYPE);
		String selectedObjectTypesFromRdfTypeFacet = request.getFirst(FacetQueryParameters.REQUEST_FACET_ARGUMENTS);
		if (StringUtils.isNotBlank(selectedObjectTypesFromRdfTypeFacet)) {
			int colonIndex = selectedObjectTypesFromRdfTypeFacet.indexOf(RDF_TYPE);
			if (colonIndex > -1) {
				// Get only the value of the key-value pair by skipping the key
				// and the colon, hence the + 1.
				colonIndex += RDF_TYPE.length() + 1;
				if (!"".equals(selectedObjectTypes)) {
					selectedObjectTypes += ",";
				}
				selectedObjectTypes += namespaceRegistryService
						.getShortUri(selectedObjectTypesFromRdfTypeFacet.substring(colonIndex));
			}
		}
		return searchablePropertiesService.getSearchableSolrProperties(selectedObjectTypes, Boolean.FALSE,
				Boolean.FALSE, Boolean.FALSE);
	}

	@Override
	public Facet searchablePropertyToFacet(SearchableProperty property) {
		Facet facet = new Facet();
		facet.setCodelists(property.getCodelists());
		facet.setRangeClass(property.getRangeClass());
		facet.setPropertyType(property.getPropertyType());
		facet.setSolrType(property.getSolrType());
		facet.setText(property.getText());
		facet.setUri(property.getUri());
		facet.setSolrFieldName(property.getSolrFieldName());
		facet.setId(property.getId());
		return facet;
	}

	/**
	 * Extract facets from the {@link SearchRequest}.
	 *
	 * @param request
	 *            the search request
	 * @return a list of the extracted facets
	 */
	private List<Facet> extractFacetsFromRequest(SearchRequest request) {
		List<String> facetIds = request.get(FacetQueryParameters.REQUEST_FACET_FIELD);
		String forType = request.getFirst(REQUEST_FOR_TYPE);
		List<Facet> result = new ArrayList<>(facetIds.size());
		if (facetIds.contains(RDF_TYPE)) {
			result.add(createRdfTypeFacet());
			facetIds.remove(RDF_TYPE);
		}
		for (String facetId : facetIds) {
			Optional<SearchableProperty> property = searchablePropertiesService.getSearchableProperty(forType, facetId);
			if (property.isPresent()) {
				result.add(searchablePropertyToFacet(property.get()));
			}
		}
		return result;
	}

	/**
	 * Constructs the default facet containing the RDF types.
	 *
	 * @return a new facet for RDF type.
	 */
	private static Facet createRdfTypeFacet() {
		Facet rdfType = new Facet();
		rdfType.setUri(DefaultProperties.SEMANTIC_TYPE);
		rdfType.setId(RDF_TYPE);
		rdfType.setSolrFieldName(RDF_TYPE);
		rdfType.setPropertyType("definition");
		return rdfType;
	}

}
