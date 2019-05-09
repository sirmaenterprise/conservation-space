package com.sirma.itt.emf.solr.services.impl.facet;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.solr.services.SolrQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.domain.search.facet.FacetValue;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.SearchablePropertiesService;
import com.sirma.itt.seip.search.facet.FacetArgumentTransformer;
import com.sirma.itt.seip.search.facet.FacetService;
import com.sirma.itt.seip.search.facet.FacetSortService;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Default implementation of facet service. Uses SOLR to perform faceting over a list of results.
 *
 * @author iborisov
 * @author mradkov
 * @author vtsonev
 * @author nvelkov
 * @author BBonev
 */
@ApplicationScoped
public class FacetServiceImpl implements FacetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String RDF_TYPE = "rdfType";
	private static final String TDATES = "tdates";

	private static final String INSTANCE_TYPE_NOT_SECTIONINSTANCE = "-instanceType:\"sectioninstance\"";
	private static final String OBJECT_NOT_DELETED = "-isDeleted:\"true\"";
	private static final String OBJECT = "object";

	@Inject
	private SearchConfiguration searchConfiguration;

	@Inject
	private FacetArgumentTransformer facetArgumentTransformer;

	@Inject
	private FacetResultTransformer facetResultTransformer;

	@Inject
	private FacetSortService facetSortService;

	@Inject
	private FacetSolrHelper facetSolrHelper;

	@Inject
	private FacetConfigurationProperties facetConfigurationProperties;

	@Inject
	private SolrConnector solrConnector;

	@Inject
	private SearchablePropertiesService searchablePropertiesService;

	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	private SearchService searchService;

	@Inject
	private FieldValueRetrieverService fieldValueRetrieverService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void prepareArguments(SearchRequest request,
			S searchArgs) {
		if (request.getFirstBoolean(FacetQueryParameters.REQUEST_FACET)) {
			searchArgs.requestAllFoundInstanceIds(true);
		}
		TimeTracker timeTracker = TimeTracker.createAndStart();

		boolean ignoreFacetConfiguration = request
				.getFirstBoolean(FacetQueryParameters.REQUEST_IGNORE_FACET_CONFIGURATION);
		searchArgs.setIgnoreFacetConfiguration(ignoreFacetConfiguration);
		boolean hasFacetArguments = CollectionUtils
				.isNotEmpty(request.get(FacetQueryParameters.REQUEST_FACET_ARGUMENTS));

		if (searchArgs.shouldReturnAllUries() || hasFacetArguments) {
			// Creates facets out of searchable properties based on the search
			// request
			facetArgumentTransformer.populateFacets(request, searchArgs);

			// Preserves any selection made on facets
			facetArgumentTransformer.preserveFacetSelections(request, searchArgs);

			// Merge the additional filter FQs with the existing ones in the
			// arguments map
			facetArgumentTransformer.mergeAdditionalFilterQueries(request, searchArgs);

			// Prepares any date arguments or uses the default ones
			facetArgumentTransformer.prepareDateArguments(request, searchArgs);

		}
		LOGGER.trace("Preparing facet arguments took {} seconds.", timeTracker.stopInSeconds());

	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void facet(S arguments) {
		boolean performFaceting = performFaceting(arguments);
		boolean ignoreFacetConfiguration = arguments.isIgnoreFacetConfiguration();
		boolean facetingOverMaxResults = shouldFacetOverMaxResults(arguments) || ignoreFacetConfiguration;

		// For SPARQL search. We get the results IDs and do a faceting on them.
		if (performFaceting && facetingOverMaxResults) {
			querySolr(arguments);
		}
		if (!arguments.isIgnoreFacetConfiguration()) {
			filterFacetsByObjectType(arguments);
		}
		facetResultTransformer.formatDatesToUTC(arguments);
	}

	private <E extends Instance, S extends SearchArguments<E>> void querySolr(S arguments) {
		TimeTracker tracker = TimeTracker.createAndStart();

		Collection<Serializable> uris = arguments.getUries();
		StringBuilder filterQuery = SolrQueryHelper.createUriQuery(uris, DefaultProperties.URI);

		// append the additional filters to the query
		String additionalFilters = (String) arguments.getArguments().get(CommonParams.FQ);
		if (StringUtils.isNotBlank(additionalFilters)) {
			filterQuery.append(" AND ").append(additionalFilters);
		}

		SolrQuery parameters = new SolrQuery();
		facetSolrHelper.addDefaultFacetParameters(parameters);
		parameters.setFilterQueries(filterQuery.toString());
		parameters.setParam(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_ALL);
		parameters.setRows(0);

		facetSolrHelper.assignFacetArgumentsToSolrQuery(arguments, parameters);

		Iterator<Facet> facetsIterator = arguments.getFacets().values().iterator();
		while (facetsIterator.hasNext()) {
			Facet facet = facetsIterator.next();
			if (TDATES.equals(facet.getSolrType())) {
				parameters.add(FacetParams.FACET_DATE, facet.getSolrFieldName());
			} else {
				parameters.add(FacetParams.FACET_FIELD, facet.getSolrFieldName());
			}
		}

		parameters.set(CommonParams.TZ, "UTC");
		try {
			QueryResponse queryResponse = solrConnector.query(parameters);

			facetResultTransformer.extractFacetsFromResponse(arguments, queryResponse);

			LOGGER.debug("Calculating facets in {} ms.", tracker.stop());
		} catch (SolrClientException e) {
			// TODO: rethrow?
			LOGGER.error("Error during calculation of facets in solr: " + e.getMessage(), e);
		}
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void filterObjectFacets(S arguments) {
		if (arguments.getFacets() != null) {
			TimeTracker tracker = TimeTracker.createAndStart();
			// TODO: Somehow calculate the initial size! Or lamba it.
			List<ObjectFacetTask> tasks = new ArrayList<>();

			Iterator<Entry<String, Facet>> iterator = arguments.getFacets().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Facet> facetEntry = iterator.next();
				Facet facet = facetEntry.getValue();
				List<FacetValue> facetValues = facet.getValues();
				if (facetValues != null && isFacettableObjectType(facet)) {
					ObjectFacetTask task = new ObjectFacetTask(facet);
					tasks.add(task);
				}
			}

			taskExecutor.execute(tasks);
			LOGGER.debug("Object facets filtered in {} ms.", tracker.stop());
		}
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void assignLabels(S arguments) {
		Map<String, Facet> facets = arguments.getFacets();
		if (facets != null) {
			TimeTracker tracker = TimeTracker.createAndStart();
			facetResultTransformer.assignLabels(facets.values());
			LOGGER.debug("Facets labels assigned in {} ms.", tracker.stop());
		}
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void sort(S arguments) {
		Map<String, Facet> facets = arguments.getFacets();
		if (facets != null) {
			TimeTracker tracker = TimeTracker.createAndStart();

			Collection<Facet> sortedFacets = facetSortService.sort(facets.values());

			facets.clear();
			sortedFacets.forEach(f -> facets.put(f.getId(), f));

			LOGGER.debug("Facets sorted in {} ms.", tracker.stop());
		}
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> S getAvailableFacets(SearchRequest request) {
		// Parse the search arguments and perform a search so we can get the returned results ids and instance types so
		// we can do facetting based on them.
		S searchArgs = searchService.parseRequest(request);
		searchService.search(Instance.class, searchArgs);
		filterFacetsByObjectType(searchArgs);
		sort(searchArgs);

		return searchArgs;
	}

	/**
	 * Retrieves the {@link FacetValue}s for the provided object {@link Facet} from Solr with batches. The batch size is
	 * defined in {@link FacetConfigurationProperties}.
	 *
	 * @param facet
	 *            - the provided object facet
	 */
	private void queryObjectFacetsWithBatchSize(Facet facet) {
		List<FacetValue> facetValues = facet.getValues();
		if (CollectionUtils.isEmpty(facetValues)) {
			return;
		}

		TimeTracker tracker = TimeTracker.createAndStart();
		int batchSize = facetConfigurationProperties.getBatchSize().get();

		FacetResultTransformer.removeUnselectedFacetValues(facet);

		List<String> allUris = new ArrayList<>();
		for (FacetValue facetValue : facetValues) {
			allUris.add(facetValue.getId());
		}

		SolrDocumentList allSolrDocuments = new SolrDocumentList();

		int countProcessed = 0;
		int allElementForSynchronization = allUris.size();

		while (countProcessed < allElementForSynchronization) {
			int toIndex = countProcessed + batchSize;
			if (toIndex > allElementForSynchronization) {
				toIndex = allElementForSynchronization;
			}

			Collection<String> subList = allUris.subList(countProcessed, toIndex);
			StringBuilder uriFilterQuery = SolrQueryHelper.createUriQuery(subList, DefaultProperties.URI);

			SolrQuery parameters = new SolrQuery();
			parameters.setParam(CommonParams.Q, "*:*");
			parameters.setFilterQueries(uriFilterQuery.toString());
			parameters.add(CommonParams.FQ, INSTANCE_TYPE_NOT_SECTIONINSTANCE);
			parameters.add(CommonParams.FQ, OBJECT_NOT_DELETED);
			parameters.add(CommonParams.FL, DefaultProperties.URI);
			parameters.set(CommonParams.ROWS, facetValues.size());

			QueryResponse queryResponse;
			try {
				queryResponse = solrConnector.query(parameters);
				SolrDocumentList solrDocument = queryResponse.getResults();
				allSolrDocuments.addAll(solrDocument);
			} catch (SolrClientException e) {
				LOGGER.error("Error during filtering of object facets in solr: " + e.getMessage(), e);
				// TODO: Should we stop the batching in this case?
			}
			countProcessed += batchSize;
		}

		Map<String, String> uriToHeaderMapping = getUriToHeaderMapping(allSolrDocuments);
		facetResultTransformer.removeInvalidObjectFacetValues(allSolrDocuments, facet);
		facetResultTransformer.assignFacetValuesLabels(uriToHeaderMapping, facet);

		LOGGER.debug("Object facet id=[{}] filtered in {} seconds.", facet.getId(), tracker.stopInSeconds());
	}

	/**
	 * Creates a mapping between URI and header from the provided {@link SolrDocumentList}.
	 *
	 * @param solrDocumentList
	 *            the provided list
	 * @return mapping between URI and header
	 */
	private Map<String, String> getUriToHeaderMapping(SolrDocumentList solrDocumentList) {
		String[] ids = solrDocumentList
				.stream()
					.map(e -> e.get(DefaultProperties.URI).toString())
					.collect(Collectors.toList())
					.stream()
					.toArray(String[]::new);
		return fieldValueRetrieverService.getLabels(FieldId.HEADER, ids, null);
	}

	/**
	 * Update facets with specific details (code lists) depending on returned rdfTypes. Remove non common facets if more
	 * than one rdfType is returned.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the search arguments
	 */
	private <E extends Instance, S extends SearchArguments<E>> void filterFacetsByObjectType(S arguments) {
		Collection<Serializable> uries = arguments.getUries();
		if (isNotEmpty(uries)) {
			Collection<InstanceReference> references = instanceTypeResolver.resolveReferences(uries);
			Set<String> rdfTypes = new HashSet<>();
			for (InstanceReference reference : references) {
				rdfTypes.add(reference.getType().getId().toString());
			}
			removeNonSearchableTypes(arguments.getFacets(), rdfTypes);
		}
	}

	/**
	 * Remove all non searchable rdf types and all sub types from the rdf type facet and retains only facets which
	 * belong to the chosen rdf type.
	 *
	 * @param facets
	 *            the facet mapping
	 * @param typesToFilter
	 *            the main object types (the ones that should be kept)
	 */
	private void removeNonSearchableTypes(Map<String, Facet> facets, Collection<String> typesToFilter) {

		Facet rdfTypeFacet = facets.get(RDF_TYPE);
		if (rdfTypeFacet != null && CollectionUtils.isNotEmpty(rdfTypeFacet.getValues())) {
			for (Iterator<FacetValue> iterator = rdfTypeFacet.getValues().iterator(); iterator.hasNext();) {
				FacetValue facetValue = iterator.next();
				if (!typesToFilter.contains(facetValue.getId())) {
					iterator.remove();
				}
			}
		}

		Set<String> addedFacets = new HashSet<>();
		String rdfTypes = org.apache.commons.lang3.StringUtils.join(typesToFilter, ",");
		addedFacets.add(RDF_TYPE);
		List<SearchableProperty> allSearchablePropertiesForType = searchablePropertiesService
				.getSearchableSolrProperties(rdfTypes, false, false, false);
		for (SearchableProperty searchableProperty : allSearchablePropertiesForType) {
			enrichFacet(addedFacets, searchableProperty, facets.get(searchableProperty.getId()));
		}
		// Retain only facets which belong to this rdf type
		facets.keySet().retainAll(addedFacets);

	}

	/**
	 * Enrich the facet with label and codelist and add it to the set of added facets.
	 *
	 * @param addedFacets
	 *            the set of added facets
	 * @param searchableProperty
	 *            the searchable property which contains the codelist and label
	 * @param facet
	 *            the facet to be enriched
	 */
	private static void enrichFacet(Set<String> addedFacets, SearchableProperty searchableProperty, Facet facet) {
		if (facet != null) {
			addedFacets.add(searchableProperty.getId());
			if (facet.getText() == null) {
				facet.setText(searchableProperty.getText());
			}
			Set<Integer> propertyCodeList = searchableProperty.getCodelists();
			Set<Integer> facetCodeList = facet.getCodelists();
			if (facetCodeList == null || CollectionUtils.isNotEmpty(propertyCodeList)) {
				facet.setCodelists(propertyCodeList);
			}
		}
	}

	/**
	 * Checks if the given facet is an object facet.
	 *
	 * @param facet
	 *            - the given facet
	 * @return true if the facet is an object facet or false otherwise
	 */
	private static boolean isFacettableObjectType(Facet facet) {
		return OBJECT.equals(facet.getPropertyType());
	}

	/**
	 * Checks if the service should perform faceting despite the maximum result size.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            - the search arguments
	 * @return true if yes or false otherwise
	 */
	private <E extends Instance, S extends SearchArguments<E>> boolean shouldFacetOverMaxResults(S arguments) {
		return !searchConfiguration.getSearchFacetResultExceedDisable().booleanValue()
				|| arguments.getTotalItems() < searchConfiguration.getSearchResultMaxSize().intValue();
	}

	/**
	 * Checks if the service should perform faceting based on the provided {@link SearchArguments}.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            - the provided search arguments
	 * @return true if yes or false otherwise
	 */
	private static <E extends Instance, S extends SearchArguments<E>> boolean performFaceting(S arguments) {
		if (CollectionUtils.isEmpty(arguments.getUries())) {
			return false;
		}
		if (!isNotEmpty(arguments.getFacets())) {
			return false;
		}
		// All is good.
		return true;
	}

	/**
	 * Task for filtering object facets.
	 *
	 * @author nvelkov
	 */
	private class ObjectFacetTask extends GenericAsyncTask {

		private static final long serialVersionUID = 6068933232241191778L;

		private final Facet facet;

		/**
		 * Initialize the task and set the facet on which operations will be performed.
		 *
		 * @param facet
		 *            the facet
		 */
		protected ObjectFacetTask(Facet facet) {
			this.facet = facet;
		}

		@Override
		protected boolean executeTask() throws Exception {
			FacetResultTransformer.removeNullValues(facet.getValues());
			queryObjectFacetsWithBatchSize(facet);
			return true;
		}

	}

}
