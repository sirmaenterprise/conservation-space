package com.sirma.itt.emf.solr.services.ws;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.event.SearchExecutedEvent;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.solr.configuration.SolrConfigurationProperties;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.web.rest.util.SearchResultTransformer;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.search.FTSQueryParser;

// TODO: Auto-generated Javadoc
/**
 * The SearchRestService handles query requests for .
 */
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SearchRestService extends EmfRestService {

	/** The Constant URI_PATTERN. */
	private static final Pattern URI_PATTERN = Pattern.compile("\\\"(.*?)\\\"");
	/** The logger. */
	private Logger logger = LoggerFactory.getLogger(getClass());
	/** The solr connector. */
	@Inject
	private SolrConnector solrConnector;

	/** The fq status. */
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_CONFIG_FQ_STATUS, defaultValue = "-status:(DELETED) AND isDeleted:false")
	private String fqStatus;

	/** The search service. */
	@Inject
	private SearchService searchService;

	/** The search result transformer. */
	@Inject
	private SearchResultTransformer searchResultTransformer;

	/** The authority service. */
	@Inject
	private AuthorityService authorityService;

	/** The task executor. */
	@Inject
	private TaskExecutor taskExecutor;

	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;

	/** The solr core. */
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_SERVER_CORE)
	private String solrCore;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The parser. */
	@Inject
	private FTSQueryParser parser;

	/**
	 * Default solr query.
	 *
	 * @param query
	 *            the query is the query. it is phrase or term. current implementation does not
	 *            support
	 * @param max
	 *            the max - up to results - stick to 1 page
	 * @param skip
	 *            the skip - skip from start cound
	 * @param sort
	 *            the sort field - valid definition field or keyword as 'score'
	 * @param dir
	 *            the sort direction
	 * @param fields
	 *            the list of fields to be returned in the resulted json
	 * @param timeout
	 *            the max time to search until return return the currently found
	 * @param shortenURIs
	 *            if true and uri is amongst fields, short uri is returned instead of full uri
	 * @param queryType
	 *            is term if this is only single term or complex phrase, or solr if this query
	 *            includes fields to search in
	 * @return the json object containg two main entries: data - list of entries with the
	 *         coresponding fields, paging - data for pagination
	 */
	@Path("/quick")
	@GET
	public String query(final @QueryParam("q") String query, final @QueryParam("max") String max,
			final @QueryParam("skip") String skip, final @QueryParam("sort") String sort,
			final @QueryParam("dir") String dir, final @QueryParam("fields") String fields,
			final @QueryParam("timeout") String timeout,
			final @QueryParam("returnShortURI") @DefaultValue("false") String shortenURIs,
			final @QueryParam("qtype") @DefaultValue("solr") String queryType) {
		try {

			SolrQuery parameters = new SolrQuery();
			// fts is used more frequently, but for comptability solr is default
			if ("term".equals(queryType)) {
				if (query == null || query.trim().isEmpty()) {
					// dont return enything on invalid term
					parameters.set(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_EMPTY);
				} else {
					parameters.set(CommonParams.Q, parser.prepare(query));
				}
			} else if ("solr".equals(queryType)) {
				parameters.set(CommonParams.Q, query);
			}
			if (sort != null) {
				parameters.setSort(sort, (dir != null) && "asc".equals(dir) ? ORDER.asc
						: ORDER.desc);
			}
			if (fields != null) {
				parameters.set(CommonParams.FL, fields);
			}
			parameters.setParam(CommonParams.FQ, fqStatus);
			parameters.set(CommonParams.ROWS, max);
			if (timeout != null) {
				parameters.setTimeAllowed(Integer.parseInt(timeout));
			} else {
				// 0.8sec
				parameters.setTimeAllowed(800);
			}
			QueryResponse queryResponse = solrConnector.queryWithGet(parameters);
			if (queryResponse != null) {
				SolrDocumentList results = queryResponse.getResults();
				JSONArray resultArr = processSearchResult(results, shortenURIs);
				JSONObject queryResponseResult = new JSONObject();
				JsonUtil.addToJson(queryResponseResult, "data", resultArr);
				JSONObject paging = new JSONObject();
				JsonUtil.addToJson(paging, "total", results.getNumFound());
				JsonUtil.addToJson(paging, "found", resultArr.length());
				JsonUtil.addToJson(paging, "skip", skip != null ? skip : "0");
				JsonUtil.addToJson(queryResponseResult, "paging", paging);
				return queryResponseResult.toString();
			}
		} catch (SolrClientException e) {
			logger.error("Query client error: " + e.getMessage(), e);
		}
		return "{\"data\":[],\"paging\":{}}";
	}

	/**
	 * Iterates and prepare the result to be included in the final result object.
	 *
	 * @param results
	 *            the results to iterate over
	 * @param shortenURIs
	 *            if the uris should be shorten
	 * @return the JSON array of results
	 */
	private JSONArray processSearchResult(final SolrDocumentList results, final String shortenURIs) {
		Iterator<SolrDocument> iterator = results.iterator();
		boolean isShorteningURIs = Boolean.valueOf(shortenURIs).booleanValue();
		JSONArray resultArr = new JSONArray();
		while (iterator.hasNext()) {
			SolrDocument nextDocument = iterator.next();
			JSONObject nextDocumentAsJSON = new JSONObject();
			if (isShorteningURIs) {
				Object uri = nextDocument.getFieldValues(DefaultProperties.URI);
				if (uri instanceof ArrayList<?>) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> uris = (ArrayList<Object>) uri;
					for (int i = 0; i < uris.size(); i++) {
						Object anUri = uris.get(i);
						if (anUri instanceof String) {
							uris.set(i, namespaceRegistryService.getShortUri(anUri.toString()));
						}
					}
				}
			}
			for (Entry<String, Object> entry : nextDocument) {
				Object value = entry.getValue();
				if (value instanceof Collection) {
					if (((Collection<?>) value).size() < 2) {
						value = nextDocument.getFirstValue(entry.getKey());
					}
				}
				JsonUtil.addToJson(nextDocumentAsJSON, entry.getKey(), value);

			}
			resultArr.put(nextDocumentAsJSON);
		}

		return resultArr;
	}

	/**
	 * Solr query for facet search.
	 *
	 * @param query
	 *            the main solr query
	 * @param filter
	 *            queries as JSON that restrict the super set of documents that can be returned
	 *            example: {filter:['query','another query']}
	 * @param facetFields
	 *            the fields to be treated as facets
	 * @param dateField
	 *            the fields to be treated as date facets
	 * @param gap
	 *            the size of each date range expressed as an interval
	 * @param start
	 *            the lower bound for the first date range for all Date Faceting
	 * @param end
	 *            The minimum upper bound for the first date range for all Date Faceting on this
	 *            field
	 * @param includePrefix
	 *            Indicates whether the prefix of the _sort_ fields should be included. Useful only
	 *            when faceting by _sort_ fields
	 * @return the results from the faceted search
	 */
	@Path("/faceted")
	@POST
	public String facetQuery(final @QueryParam("q") String query, final String filter,
			final @QueryParam("field") List<String> facetFields,
			final @QueryParam("datefield") String dateField, final @QueryParam("gap") String gap,
			final @QueryParam("start") String start, final @QueryParam("end") String end,
			@DefaultValue("true") final @QueryParam("includePrefix") boolean includePrefix) {
		try {
			// Collects the filters as JSON
			JSONObject fromString = JsonUtil.createObjectFromString(filter);
			List<String> filterQueries = Collections.emptyList();
			if (fromString != null) {
				JSONArray jsonArray = JsonUtil.getJsonArray(fromString, "filter");
				if (jsonArray != null) {
					filterQueries = new ArrayList<String>(jsonArray.length());
					for (int i = 0; i < jsonArray.length(); i++) {
						Matcher m = URI_PATTERN.matcher(jsonArray.getString(i));
						StringBuilder singleFilter = new StringBuilder("");
						while (m.find()) {
							String uri = m.group(1);
							String fullUri = namespaceRegistryService.buildFullUri(uri);
							if (singleFilter.toString().equals("")) {
								singleFilter.append("uri:\"" + fullUri);
							} else {
								singleFilter.append("\" OR uri:\"" + fullUri);
							}

						}
						singleFilter.append("\"");
						filterQueries.add(singleFilter.toString());
					}
				}
			}

			SolrQuery params = new SolrQuery();
			params.setParam(CommonParams.Q, query);
			params.setParam(FacetParams.FACET, true);
			params.set(FacetParams.FACET_MINCOUNT, 1);
			if (!filterQueries.isEmpty()) {
				params.setParam(CommonParams.FQ,
						filterQueries.toArray(new String[filterQueries.size()]));

			}
			if (!facetFields.isEmpty()) {
				params.setParam(FacetParams.FACET_FIELD,
						facetFields.toArray(new String[facetFields.size()]));
			}
			if ((dateField != null) && (gap != null) && (start != null) && (end != null)) {
				params.setParam(FacetParams.FACET_DATE, dateField);
				params.setParam(FacetParams.FACET_DATE_GAP, gap);
				params.setParam(FacetParams.FACET_DATE_START, start);
				params.setParam(FacetParams.FACET_DATE_END, end);
			}
			QueryResponse queryResponse = solrConnector.queryWithPost(params);
			if (queryResponse != null) {
				JSONObject facetResults = new JSONObject();
				facetResults.put("facetFields",
						constructCounts(queryResponse.getFacetFields(), includePrefix));
				facetResults.put("facetDates",
						constructCounts(queryResponse.getFacetDates(), includePrefix));
				return facetResults.toString();
			}
		} catch (SolrClientException e) {
			logger.error("Query client error: " + e.getMessage(), e);
		} catch (JSONException e) {
			logger.error("JSON client error: " + e.getMessage(), e);
		}
		return "{}";
	}

	/**
	 * Iterates a collection of {@link FacetField} objects and creates a suitable json
	 * representation.
	 *
	 * @param facetFields
	 *            the facet fields, typically retrieved from a
	 * @param includePrefix
	 *            Indicates whether the prefix of the _sort_ fields should be included. Useful only
	 *            when faceting by _sort_ fields
	 * @return json representation of the input collection {@link QueryResponse} object
	 */
	private JSONArray constructCounts(List<FacetField> facetFields, boolean includePrefix) {
		JSONArray results = new JSONArray();

		try {
			for (FacetField facetField : facetFields) {
				JSONObject result = new JSONObject();

				List<Count> countFields = facetField.getValues();
				JSONArray countObjects = new JSONArray();
				for (Count countField : countFields) {
					JSONObject countObject = new JSONObject();
					countObject.put("name", countField.getName());
					countObject.put("count", countField.getCount());
					countObjects.put(countObject);
				}
				if (includePrefix) {
					result.put(facetField.getName(), countObjects);
				} else {
					result.put(facetField.getName().substring(6), countObjects);
				}
				results.put(result);
			}
		} catch (JSONException e) {
			logger.error("Error while parsing the facet fields collection" + e.getMessage(), e);
		}
		return results;
	}

	/**
	 * Suggest request based on a query.
	 *
	 * @param query
	 *            the query
	 * @return the json array string representation of the
	 */
	@Path("/suggest")
	@GET
	public String suggest(final @QueryParam("q") String query) {
		try {

			SearchArguments<Object> args = new SearchArguments<>();
			args.setStringQuery(query);
			QueryResponse queryResponse = solrConnector.suggest(args);
			if (queryResponse != null) {
				Map<String, Suggestion> suggestionMap = queryResponse.getSpellCheckResponse()
						.getSuggestionMap();

				queryResponse = null;
				JSONArray suggests = new JSONArray(suggestionMap.keySet());
				JSONObject result = new JSONObject();
				JsonUtil.addToJson(result, "data", suggests);
				return result.toString();
			}
		} catch (SolrClientException e) {
			logger.error("Query client error: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Processing error: " + e.getMessage(), e);
		}
		return "{}";
	}

	/**
	 * Performs solr object search.
	 *
	 * @param uriInfo
	 *            Contains search params.
	 * @param log
	 *            indicates whether the search should be logged in the audit log
	 * @return JSON object as string containing the results.
	 */
	@GET
	@Path("/solr")
	public String performSolrSearch(@Context UriInfo uriInfo,
			@DefaultValue("false") @QueryParam("log") boolean log) {
		JSONObject response = new JSONObject();
		if (solrCore != null) {
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

			com.sirma.itt.emf.domain.Context<String, Object> context = new com.sirma.itt.emf.domain.Context<String, Object>();
			context.put("core", solrCore);

			String solrFilterQuery = queryParams.getFirst("fq");
			if (solrFilterQuery == null) {
				solrFilterQuery = "";
			}

			solrFilterQuery = solrFilterQuery.replaceAll("\\:", "\\\\:");

			SearchArguments<Instance> searchArgsCount = searchService.getFilter(
					"QUERY_SOLR_SEARCH_TOTAL_RESULTS", Instance.class, context);
			searchArgsCount.getArguments().put("query", solrFilterQuery);
			searchService.search(CommonInstance.class, searchArgsCount);

			String pageNumber = queryParams.getFirst("pageNumber");
			String pageSize = queryParams.getFirst("pageSize");
			if (StringUtils.isNotBlank(pageNumber)) {
				int offset = (Integer.parseInt(pageNumber) - 1) * Integer.parseInt(pageSize);
				context.put("offset", String.valueOf(offset));
			}

			if (StringUtils.isNotBlank(pageSize)) {
				context.put("limit", pageSize);
			}

			String orderBy = queryParams.getFirst("orderBy");
			if (StringUtils.isNotBlank(orderBy)) {
				String orderDirection = queryParams.getFirst("orderDirection");
				if (StringUtils.isNotBlank(orderDirection)) {
					context.put("orderBy", orderBy);
					context.put("orderDirection", orderDirection);
				}
			}

			SearchArguments<Instance> searchArgs = searchService.getFilter("QUERY_SOLR_SEARCH",
					Instance.class, context);
			searchArgs.getArguments().put("query", solrFilterQuery);
			searchService.search(Instance.class, searchArgs);

			int totalCount = searchArgs.getTotalItems();
			List<Instance> countResult = searchArgsCount.getResult();
			if ((countResult != null) && !countResult.isEmpty()) {
				Map<String, Serializable> countProperties = countResult.get(0).getProperties();
				if (countProperties != null) {
					Serializable count = countProperties.get("count");
					if (count instanceof Integer) {
						totalCount = (Integer) count;
					}
				}
			}

			List<Pair<Class<? extends Instance>, Serializable>> transformedResult = new ArrayList<>(
					searchArgs.getResult().size());
			for (Instance instance : searchArgs.getResult()) {
				transformedResult.add(new Pair<Class<? extends Instance>, Serializable>(instance
						.getClass(), instance.getId()));
			}

			List<Instance> result = BatchEntityLoader.load(transformedResult, serviceRegister,
					taskExecutor);
			User currentUser = authenticationService.get().getCurrentUser();
			if ((result != null) && (currentUser != null)) {
				Map<Instance, Boolean> resultsByPermission = authorityService.hasPermission(
						SecurityModel.PERMISSION_READ, result, currentUser);
				try {
					searchResultTransformer.transformResult(totalCount, resultsByPermission, null,
							response);
					if (log) {
						eventService.fire(new SearchExecutedEvent("advancedSearch"));
					}
				} catch (JSONException e) {
					logger.error("", e);
				}
			}
		}
		return response.toString();
	}
}
