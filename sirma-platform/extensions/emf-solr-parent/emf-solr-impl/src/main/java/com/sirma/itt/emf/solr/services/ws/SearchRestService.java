package com.sirma.itt.emf.solr.services.ws;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.web.rest.util.SearchResultTransformer;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.search.SearchQueryParameters;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * The SearchRestService handles query requests for .
 */
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SearchRestService extends EmfRestService {

	private static final String QUERY_CLIENT_ERROR = "Query client error: ";

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchRestService.class);

	/** The solr connector. */
	@Inject
	private SolrConnector solrConnector;

	/** The search service. */
	@Inject
	private SearchService searchService;

	/** The search result transformer. */
	@Inject
	private SearchResultTransformer searchResultTransformer;

	/** The solr core. */
	@Inject
	private SolrConfiguration solrConfiguration;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The parser. */
	@Inject
	private FTSQueryParser parser;

	/**
	 * Default solr query.
	 *
	 * @param query
	 *            the query is the query. it is phrase or term. current implementation does not support
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
	 * @param fq
	 *            the query filter
	 * @param timeout
	 *            the max time to search until return return the currently found
	 * @param shortenURIs
	 *            if true and uri is amongst fields, short uri is returned instead of full uri
	 * @param queryType
	 *            is term if this is only single term or complex phrase, or solr if this query includes fields to search
	 *            in
	 * @return the json object containing two main entries: data - list of entries with the corresponding fields, paging
	 *         - data for pagination
	 */
	@Path("/quick")
	@GET
	public String query(@QueryParam("q") String query, @QueryParam("max") String max, @QueryParam("skip") String skip,
			@QueryParam("sort") String sort, @QueryParam("dir") String dir, @QueryParam("fields") String fields,
			@QueryParam("fq") String fq, @QueryParam("timeout") String timeout,
			@QueryParam("returnShortURI") @DefaultValue("false") String shortenURIs,
			@QueryParam("qtype") @DefaultValue("solr") String queryType) {
		return queryInternal(query, max, skip, sort, dir, fields, fq, timeout, shortenURIs, queryType);
	}

	/**
	 * Returns specific fields from the found records in Solr. The search is based on the provided json array of
	 * instance IDs from which a query is build. The IDs should be provided like:
	 * {'ids':["\"*5af0ea5d\-0d2b\-4f59\-b203\-413c77e124b0\""]}
	 *
	 * @param ids
	 *            - the provided IDs
	 * @param fields
	 *            -the specific fields to be returned
	 * @param shortenURIs
	 *            - flag for shor or full URIs
	 * @return the solr result
	 */
	@Path("/find/id")
	@POST
	public String findById(String ids, @QueryParam("fields") String fields,
			@QueryParam("returnShortURI") @DefaultValue("false") String shortenURIs) {
		String query = SolrQueryConstants.QUERY_DEFAULT_EMPTY;
		String length = "0";
		JSONObject idsjson = JsonUtil.createObjectFromString(ids);
		if (idsjson != null) {
			JSONArray jsonArray = JsonUtil.getJsonArray(idsjson, "ids");
			length = String.valueOf(jsonArray.length());

			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append(DefaultProperties.URI).append(":(");
			for (int i = 0; i < jsonArray.length(); i++) {
				String id = JsonUtil.getStringFromArray(jsonArray, i);
				if (id == null) {
					continue;
				}
				if (i == 0) {
					queryBuilder.append(id);
				} else {
					queryBuilder.append(" OR ").append(id);
				}
			}
			queryBuilder.append(")");
			query = queryBuilder.toString();
		}
		return queryInternal(query, length, null, null, null, fields, null, null, shortenURIs, "solr");
	}

	/**
	 * Internal method for performing searches in Solr based on the provided parameters.
	 *
	 * @param query
	 *            the query is the query. it is phrase or term. current implementation does not support
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
	 * @param fq
	 *            the query filter
	 * @param timeout
	 *            the max time to search until return return the currently found
	 * @param shortenURIs
	 *            if true and uri is amongst fields, short uri is returned instead of full uri
	 * @param queryType
	 *            is term if this is only single term or complex phrase, or solr if this query includes fields to search
	 *            in
	 * @return the json object containing two main entries: data - list of entries with the corresponding fields, paging
	 *         - data for pagination
	 */
	private String queryInternal(String query, String max, String skip, String sort, String dir, String fields,
			String fq, String timeout, String shortenURIs, String queryType) {
		try {
			SolrQuery parameters = new SolrQuery();
			// fts is used more frequently, but for compatibility solr is default
			if ("term".equals(queryType)) {
				if (query != null && !query.trim().isEmpty()) {
					parameters.set(CommonParams.Q, parser.prepare(query));
				}
			} else if ("solr".equals(queryType)) {
				parameters.set(CommonParams.Q, query);
			}
			if (sort != null) {
				parameters.setSort(sort, dir != null && "asc".equals(dir) ? ORDER.asc : ORDER.desc);
			}
			if (fields != null) {
				parameters.set(CommonParams.FL, fields);
			}
			if (fq != null) {
				parameters.add(CommonParams.FQ, fq);
			}
			parameters.set(CommonParams.ROWS, max);
			if (timeout != null) {
				parameters.setTimeAllowed(Integer.valueOf(timeout));
			} else {
				// 0.8sec
				parameters.setTimeAllowed(800);
			}

			QueryResponse queryResponse = solrConnector.queryWithPost(parameters);
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
			LOGGER.error(QUERY_CLIENT_ERROR + e.getMessage(), e);
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
	@SuppressWarnings("unchecked")
	private JSONArray processSearchResult(SolrDocumentList results, String shortenURIs) {
		Iterator<SolrDocument> iterator = results.iterator();
		boolean isShorteningURIs = Boolean.valueOf(shortenURIs).booleanValue();
		JSONArray resultArr = new JSONArray();
		while (iterator.hasNext()) {
			SolrDocument nextDocument = iterator.next();
			JSONObject nextDocumentAsJSON = new JSONObject();
			if (isShorteningURIs) {
				Object uri = nextDocument.getFieldValues(DefaultProperties.URI);
				if (uri instanceof List) {
					toShortUries((List<Object>) uri);
				}
			}
			for (Entry<String, Object> entry : nextDocument) {
				Object value = entry.getValue();
				if (value instanceof Collection && ((Collection<?>) value).size() < 2) {
					value = nextDocument.getFirstValue(entry.getKey());
				}
				JsonUtil.addToJson(nextDocumentAsJSON, entry.getKey(), value);

			}
			resultArr.put(nextDocumentAsJSON);
		}

		return resultArr;
	}

	/**
	 * To short uries.
	 *
	 * @param uris
	 *            the uris
	 */
	private void toShortUries(List<Object> uris) {
		for (int i = 0; i < uris.size(); i++) {
			Object anUri = uris.get(i);
			if (anUri instanceof String) {
				uris.set(i, namespaceRegistryService.getShortUri(anUri.toString()));
			}
		}
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
	public String suggest(@QueryParam("q") String query) {
		try {
			SearchArguments<Object> args = new SearchArguments<>();
			args.setStringQuery(query);
			QueryResponse queryResponse = solrConnector.suggest(args);
			if (queryResponse != null) {
				Map<String, Suggestion> suggestionMap = queryResponse.getSpellCheckResponse().getSuggestionMap();

				queryResponse = null;
				JSONArray suggests = new JSONArray(suggestionMap.keySet());
				JSONObject result = new JSONObject();
				JsonUtil.addToJson(result, "data", suggests);
				return result.toString();
			}
		} catch (SolrClientException e) {
			LOGGER.error(QUERY_CLIENT_ERROR + e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("Processing error: " + e.getMessage(), e);
		}
		return "{}";
	}

	/**
	 * Performs solr object search.
	 *
	 * @param uriInfo
	 *            Contains search params.
	 * @return JSON object as string containing the results.
	 */
	@GET
	@Path("/solr")
	public String performSolrSearch(@Context UriInfo uriInfo) {
		JSONObject response = new JSONObject();

		String solrCore = solrConfiguration.getMainSolrCore();
		if (solrCore == null) {
			return response.toString();
		}

		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

		com.sirma.itt.seip.context.Context<String, Object> context = new com.sirma.itt.seip.context.Context<>();
		context.put("core", solrCore);

		String solrFilterQuery = queryParams.getFirst("fq");
		if (solrFilterQuery == null) {
			solrFilterQuery = "";
		}

		solrFilterQuery = solrFilterQuery.replaceAll("\\:", "\\\\:");

		SearchArguments<Instance> searchArgsCount = searchService.getFilter("QUERY_SOLR_SEARCH_TOTAL_RESULTS",
				Instance.class, context);
		searchArgsCount.getArguments().put("query", solrFilterQuery);
		searchService.search(CommonInstance.class, searchArgsCount);

		String pageNumber = queryParams.getFirst(SearchQueryParameters.PAGE_NUMBER);
		String pageSize = queryParams.getFirst(SearchQueryParameters.PAGE_SIZE);
		if (StringUtils.isNotBlank(pageNumber)) {
			int offset = (Integer.parseInt(pageNumber) - 1) * Integer.parseInt(pageSize);
			context.put("offset", String.valueOf(offset));
		}

		if (StringUtils.isNotBlank(pageSize)) {
			context.put("limit", pageSize);
		}

		String orderBy = queryParams.getFirst(SearchQueryParameters.ORDER_BY);
		if (StringUtils.isNotBlank(orderBy)) {
			String orderDirection = queryParams.getFirst("orderDirection");
			if (StringUtils.isNotBlank(orderDirection)) {
				context.put(SearchQueryParameters.ORDER_BY, orderBy);
				context.put("orderDirection", orderDirection);
			}
		}

		SearchArguments<Instance> searchArgs = searchService.getFilter("QUERY_SOLR_SEARCH", Instance.class, context);
		searchArgs.getArguments().put("query", solrFilterQuery);
		searchService.searchAndLoad(Instance.class, searchArgs);

		int totalCount = searchArgs.getTotalItems();
		List<Instance> countResult = searchArgsCount.getResult();
		if (countResult != null && !countResult.isEmpty()) {
			Map<String, Serializable> countProperties = countResult.get(0).getProperties();
			if (countProperties != null) {
				Serializable count = countProperties.get("count");
				if (count instanceof Number) {
					totalCount = ((Number) count).intValue();
				}
			}
		}

		List<Instance> result = searchArgs.getResult();
		User currentUser = getCurrentLoggedUser();
		if (result != null && currentUser != null) {
			Map<Instance, Boolean> resultsByPermission = authorityService.hasPermission(SecurityModel.PERMISSION_READ,
					result, currentUser);
			searchResultTransformer.transformResult(totalCount, resultsByPermission, null, response, null);
		}
		return response.toString();
	}
}
