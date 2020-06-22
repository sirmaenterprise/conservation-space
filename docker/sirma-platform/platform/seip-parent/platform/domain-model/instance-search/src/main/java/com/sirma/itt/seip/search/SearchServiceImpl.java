package com.sirma.itt.seip.search;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;
import com.sirma.itt.seip.search.facet.FacetService;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default implementation for the search service.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SearchServiceImpl implements SearchService {

	@Inject
	@ExtensionPoint(SearchServiceFilterExtension.TARGET_NAME)
	private Iterable<SearchServiceFilterExtension> extension;

	@Inject
	@ExtensionPoint(SearchEngine.TARGET_NAME)
	private Iterable<SearchEngine> engines;

	private Map<Class, SearchServiceFilterExtension> mapping;

	@Inject
	private javax.enterprise.inject.Instance<FacetService> facetService;

	@Inject
	private SearchConfiguration searchConfiguration;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	@ExtensionPoint(SearchArgumentProvider.ARGUMENTS_EXTENSION_POINT)
	private Iterable<SearchArgumentProvider> argumentProviders;

	@Inject
	private PropertiesService propertiesService;

	/**
	 * Initialize mappings.
	 */
	@PostConstruct
	public void initializeMappings() {
		mapping = PluginUtil.parseSupportedObjects(extension, true);
	}

	@Override
	public <E, S extends SearchArguments<E>> S getFilter(String filterName, Class<E> resultType,
			Context<String, Object> context) {
		return getExtension(resultType).buildSearchArguments(filterName, context);
	}

	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Class<?> resultType,
			Context<String, Object> context) {
		return getExtension(resultType).buildSearchArguments(filter, context);
	}

	@Override
	public <E> SearchFilterConfig getFilterConfiguration(String placeHolder, Class<E> resultType) {
		return getExtension(resultType).getFilterConfiguration(placeHolder);
	}

	@Override
	@Monitored({
		@MetricDefinition(name = "search_duration_seconds", type = Type.TIMER, descr = "Search duration in seconds."),
		@MetricDefinition(name = "search_hit_count", type = Type.COUNTER, descr = "Hit counter on the serach service method.")
	})
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments) {
		searchInternal(target, arguments, false);
	}

	@Override
	@Monitored({
		@MetricDefinition(name = "search_load_duration_seconds", type = Type.TIMER, descr = "Search and load duration in seconds."),
		@MetricDefinition(name = "search_load_hit_count", type = Type.COUNTER, descr = "Hit counter on the serach and load service method.")
	})
	public <E extends Instance, S extends SearchArguments<E>> void searchAndLoad(Class<?> target, S arguments) {
		searchInternal(target, arguments, true);
	}

	@Override
	public <R, S extends SearchArguments<? extends Instance>> Stream<R> stream(S arguments,
			ResultItemTransformer<R> resultTransformer) {
		for (SearchEngine engine : engines) {
			if (engine.isSupported(Instance.class, arguments)) {
				return engine.stream(arguments).map(resultTransformer::transform);
			}
		}
		return Stream.empty();
	}

	@Override
	public <S extends SearchArguments<? extends Instance>> Map<String, Map<String, Integer>> groupBy(S arguments) {
		if (isEmpty(arguments.getGroupBy())) {
			throw new IllegalArgumentException("Cannot perform grouping without specifying the grouping properties");
		}
		// make sure the grouping is enabled
		arguments.setShouldGroupBy(true);

		try (Stream<ResultItem> stream = stream(arguments, ResultItemTransformer.asIs())) {
			return stream.collect(groupingBy(ResultItem::getGroupBy,
					toMap(ResultItem::getGroupByValue, ResultItem::getGroupByCount)));
		}
	}

	/**
	 * Performs the search and gives option to skip additional loading for the found results.
	 */
	private <E extends Instance, S extends SearchArguments<E>> void searchInternal(Class<?> target, S arguments,
			boolean loadResults) {
		for (SearchEngine engine : engines) {
			if (engine.isSupported(target, arguments)) {
				engine.search(target, arguments);
				filterByFacet(arguments);
				if (loadResults) {
					decorateResults(arguments.getResult());
				}
				return;
			}
		}
		// REVIEW or throw an exception
	}

	/**
	 * Performs any additional faceting and filters object facets by removing the section objects, applying permissions
	 * and adding bread crumb headers to the returned facet values.
	 *
	 * @param arguments
	 *            - the arguments
	 */
	private <E extends Instance, S extends SearchArguments<E>> void filterByFacet(S arguments) {
		if (!facetService.isUnsatisfied() && arguments.isFaceted()) {
			facetService.get().facet(arguments);
			facetService.get().filterObjectFacets(arguments);
			facetService.get().assignLabels(arguments);
			// sorting is not needed when performing faceting for drawing a
			// report
			if (!arguments.isIgnoreFacetConfiguration()) {
				facetService.get().sort(arguments);
			}
		}
	}

	/**
	 * Decorate and loaded properties for the given instances.
	 *
	 * @param <I>
	 *            generic type
	 * @param instances
	 *            the instaces that should be decorated
	 */
	private <I extends Instance> void decorateResults(Collection<I> instances) {
		propertiesService.loadPropertiesBatch(instances);
		instanceLoadDecorator.decorateResult(instances);
	}

	/**
	 * Gets the extension.
	 *
	 * @param target
	 *            the target
	 * @return the extension
	 */
	private SearchServiceFilterExtension getExtension(Class<?> target) {
		SearchServiceFilterExtension serviceExtension = mapping.get(target);
		if (serviceExtension == null) {
			throw new EmfConfigurationException(
					"Could not found a search filter extension that can support class of type " + target);
		}
		return serviceExtension;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Instance, S extends SearchArguments<E>> S parseRequest(SearchRequest request) {
		if (!isRequestValid(request)) {
			return null;
		}
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		// If the query comes from definition and there's no chosen facet filtering than the query should be loaded.
		// Otherwise filtering will be made by facets
		if (request.getFirst(SearchQueryParameters.QUERY_FROM_DEFINITION) != null
				&& request.getFirst(FacetQueryParameters.REQUEST_FACET_ARGUMENTS) == null) {
			searchArgs = loadDefinitionQuery(request);
		}

		readDefaultSearchParameters(request, searchArgs);

		try {
			for (SearchEngine nextFacade : engines) {
				if (nextFacade.prepareSearchArguments(request, searchArgs)) {
					prepareFacets(request, searchArgs);
					return (S) searchArgs;
				}
			}
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed to parse search request " + request, e);
		}
		return null;
	}

	/**
	 * Reads default parameters from request into the search arguments.
	 *
	 * @param request
	 *            is the source for settings retrieval
	 * @param searchArgs
	 *            is the source argument to update
	 * @return the updated search arguments
	 */
	private <T> SearchArguments<T> readDefaultSearchParameters(SearchRequest request, SearchArguments<T> searchArgs) {
		String pageSize = request.getFirst(SearchQueryParameters.PAGE_SIZE);
		if (StringUtils.isNotBlank(pageSize)) {
			searchArgs.setPageSize(Integer.parseInt(pageSize));
		}

		String pageNumber = request.getFirst(SearchQueryParameters.PAGE_NUMBER);
		if (StringUtils.isNotBlank(pageNumber)) {
			searchArgs.setPageNumber(Integer.parseInt(pageNumber));
		}

		String maxSize = request.getFirst(SearchQueryParameters.MAX_SIZE);
		if (StringUtils.isNotBlank(maxSize)) {
			searchArgs.setMaxSize(Integer.parseInt(maxSize));
		} else {
			searchArgs.setMaxSize(searchConfiguration.getSearchResultMaxSize());
		}

		String orderBy = request.getFirst(SearchQueryParameters.ORDER_BY);
		if (StringUtils.isNotBlank(orderBy)) {
			searchArgs.setOrdered(true);
			String orderDirection = request.getFirst(SearchQueryParameters.ORDER_DIRECTION);
			Sorter sorter = new Sorter(orderBy,
									   EqualsHelper.nullSafeEquals(orderDirection, "desc", true) ? Sorter.SORT_DESCENDING
											   : Sorter.SORT_ASCENDING,
									   readCodelistNumbers(request.getFirst(SearchQueryParameters.ORDER_BY_CODELIST_NUMBERS)));
			// Allows to return objects which lack the property for sorting
			sorter.setAllowMissingValues();
			searchArgs.addSorter(sorter);
		}
		return searchArgs;
	}

	private static List<Integer> readCodelistNumbers(String codelistNumbers) {
		if (StringUtils.isEmpty(codelistNumbers)) {
			return Collections.emptyList();
		}
		return Arrays.stream(codelistNumbers.split("\\s*,\\s*")).map(Integer::valueOf).collect(Collectors.toList());
	}

	private static boolean isRequestValid(SearchRequest request) {
		return request != null && request.getRequest() != null;
	}

	private SearchArguments<Instance> loadDefinitionQuery(SearchRequest queryParams) {

		SearchFilterConfig searchCriteria = getFilterConfiguration(
				queryParams.getFirst(SearchQueryParameters.QUERY_FROM_DEFINITION), SearchInstance.class);
		List<SearchFilter> filters = searchCriteria.getFilters();

		// - get selected filter
		SearchFilter selectedSearchFilter = findSearchFilter(filters,
				queryParams.getFirst(SearchQueryParameters.QUERY_NAME));

		String userURI = queryParams.getFirst(SearchQueryParameters.USER_URI);
		// - create and populate search context
		Context<String, Object> context = getSearchContext(queryParams,
				queryParams.getFirst(SearchQueryParameters.INSTANCE_ID), userURI);

		// - get search arguments from search service
		SearchArguments<Instance> searchArguments = buildSearchArguments(selectedSearchFilter, SearchInstance.class,
				context);

		return searchArguments;
	}

	private static SearchFilter findSearchFilter(List<SearchFilter> list, String target) {
		for (SearchFilter item : list) {
			if (item.getValue().equals(target)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Gets the search context.
	 *
	 * @return the search context
	 */
	private Context<String, Object> getSearchContext(SearchRequest request, String contextURI, String userURI) {

		Context<String, Object> context = new Context<>();
		context.put(SearchQueryParameters.USER_ID, userURI);
		context.put(SearchQueryParameters.ASSIGNEE, userURI);
		context.put(SearchQueryParameters.OWNER, userURI);
		context.put(SearchQueryParameters.AUTHORITIES, "\"" + userURI + "\"");
		context.put(SearchQueryParameters.CONTEXT_URI, contextURI);
		for (SearchArgumentProvider argumentProvider : argumentProviders) {
			argumentProvider.provide(request, context);
		}
		return context;
	}

	/**
	 * Prepare facets.
	 *
	 * @param request
	 *            the request
	 * @param searchArgs
	 *            the search args
	 */
	private void prepareFacets(SearchRequest request, SearchArguments<Instance> searchArgs) {
		if (facetService.isUnsatisfied()) {
			return;
		}
		facetService.get().prepareArguments(request, searchArgs);
	}

	@Override
	public Function<String, String> escapeForDialect(String dialect) {
		for (SearchEngine engine : engines) {
			Function<String, String> function = engine.escapeForDialect(dialect);
			if (function != null) {
				return function;
			}
		}
		// default function
		return s -> s;
	}

}
