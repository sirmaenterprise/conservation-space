package com.sirma.itt.emf.solr.services.query.parser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.params.CommonParams;

import com.sirma.itt.cmf.search.SearchPreprocessorEngine;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.solr.configuration.SolrConfigurationProperties;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.time.ISO8601DateFormat;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * The SolrSearchFacade preprocess query for solr engine. Currently the engine does not support all
 * fields queries or relation queries.
 *
 * @author bbanchev
 */
@Extension(target = SearchPreprocessorEngine.NAME, priority = 1, enabled = true, order = 1)
@ApplicationScoped
public class SolrSearchFacade implements SearchPreprocessorEngine {

	private static final String PREFIX_SORT_FIELD = "_sort_";

	/** The parser. */
	@Inject
	private FTSQueryParser parser;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The default fields. */
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_CONFIG_DASHLETS_ALL_FL, defaultValue = "uri,instanceType")
	private String defaultFields;

	/** The fq results. */
	@Inject
	@Config(name = SolrConfigurationProperties.FULL_TEXT_SEARCH_BASIC_FQ, defaultValue = "default_header:*")
	private String fqBasicSearch;

	/** The converter date format pattern. */
	@Inject
	@Config(name = EmfConfigurationProperties.CONVERTER_DATE_FORMAT, defaultValue = "dd.MM.yyyy")
	private String converterDateFormatPattern;

	/**
	 * On startup prepare the field list to be populated on the result
	 *
	 * @param event
	 *            the event
	 */
	public void onStartup(@Observes ApplicationStartupEvent event) {
		if (defaultFields.contains(DefaultProperties.HEADER_DEFAULT)) {
			return;
		}
		if (defaultFields.contains(DefaultProperties.HEADER_COMPACT)) {
			defaultFields = defaultFields.replace(DefaultProperties.HEADER_COMPACT,
					DefaultProperties.HEADER_DEFAULT);
		} else {
			defaultFields += ("," + DefaultProperties.HEADER_DEFAULT);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepareBasicQuery(MultivaluedMap<String, String> queryParams,
			SearchArguments<Instance> searchArgs) throws Exception {
		searchArgs.setQuery(Query.getEmpty());
		searchArgs.setSparqlQuery(false);
		searchArgs.setDialect(SearchDialects.SOLR);
		searchArgs.setProjection(defaultFields);

		String filterQuery = queryParams.getFirst("fq");
		if (StringUtils.isNotBlank(filterQuery)) {
			searchArgs.setStringQuery("(" + filterQuery + ")");
		} else {
			searchArgs.setStringQuery(parseQueryArguments(queryParams, searchArgs).toString());
		}

		// String queryText = queryParams.getFirst("queryText");
		// if (StringUtils.isNotBlank(queryText)) {
		// searchArgs.setStringQuery("(" + queryText + ")");
		// } else {
		//searchArgs.setStringQuery(parseQueryArguments(queryParams, searchArgs).toString());
		// }
		String pageNumber = queryParams.getFirst("pageNumber");
		if (StringUtils.isNotBlank(pageNumber)) {
			searchArgs.setPageNumber(Integer.parseInt(pageNumber));
		}
		String pageSize = queryParams.getFirst("pageSize");
		if (StringUtils.isNotBlank(pageSize)) {
			// solr return only those items in the current page
			searchArgs.setPageSize(Integer.parseInt(pageSize));
			searchArgs.setMaxSize(searchArgs.getPageSize());
		}
		// update the orderBy fields - might need mapping for different properties
		String orderBy = queryParams.getFirst("orderBy");
		if (StringUtils.isNotBlank(orderBy)) {
			searchArgs.setOrdered(true);
			int indexOf = orderBy.indexOf(":");
			if (indexOf > 0) {
				orderBy = PREFIX_SORT_FIELD + orderBy.substring(indexOf + 1);
			}
			searchArgs.setSorter(new Sorter(orderBy, queryParams.getFirst("orderDirection")));
		} else {
			orderBy = PREFIX_SORT_FIELD + DefaultProperties.MODIFIED_ON;
			searchArgs.setOrdered(true);
			searchArgs.setSorter(new Sorter(orderBy, Sorter.SORT_DESCENDING));
		}
	}

	/**
	 * Parses the query arguments. All the supported query arguments are contacted in a single
	 * query. If no arguments are provided an empty {@link SolrQueryConstants#QUERY_DEFAULT_EMPTY}
	 * is returned. <br>
	 * FQ argument is also set in the arguments map, since there might have valid instances for the
	 * query, but currently are supported only that contains compact_header
	 *
	 * @param queryParams
	 *            the query params to process
	 * @param searchArgs
	 *            the search args to update
	 * @return the string containg the single processed query
	 * @throws Exception
	 *             on any error
	 */
	private StringBuilder parseQueryArguments(MultivaluedMap<String, String> queryParams,
			SearchArguments<Instance> searchArgs) throws Exception {
		// TODO do we need to configure this set of solr properties?
		StringBuilder resultQuery = new StringBuilder();
		List<String> location = queryParams.get("location[]");
		if ((location != null) && !location.isEmpty()) {
			resultQuery.append("partOfRelation:").append(
					joinArgumentList(location, " OR ", true, true));
		}
		String metaText = queryParams.getFirst("metaText");
		if (StringUtils.isNotBlank(metaText)) {
			startNextClause(resultQuery);
			resultQuery.append("(" + parser.prepare(metaText) + ")");
		}

		List<String> subType = queryParams.get("subType[]");
		Set<String> rdfTypes = new HashSet<>();

		List<String> objectTypes = queryParams.get("objectType[]");
		if ((objectTypes != null) && !objectTypes.isEmpty()) {
			for (String objectType : objectTypes) {
				rdfTypes.add(objectType);
			}
		}
		if ((subType != null) && !subType.isEmpty()) {
			Set<String> subTypes = new HashSet<>();
			for (String subTypeString : subType) {
				// check the subType if it is URI
				if (subTypeString.contains(":")) {
					rdfTypes.add(subTypeString);
				} else {
					subTypes.add(subTypeString);
				}
			}
			if (!subTypes.isEmpty()) {
				startNextClause(resultQuery);
				resultQuery.append("type:").append(
						joinArgumentList(new ArrayList<String>(subTypes), " OR ", false, true));
			}
		}
		// when there is an URI in the subType list
		if (!rdfTypes.isEmpty()) {
			startNextClause(resultQuery);
			resultQuery.append("rdfType:").append(
					joinArgumentList(new ArrayList<String>(rdfTypes), " OR ", true, true));
		}
		String mimeType = queryParams.getFirst("mimetype");
		if (StringUtils.isNotBlank(mimeType)) {
			startNextClause(resultQuery);
			// replace the regex otherwise consider it is full mimetype id
			if (mimeType.startsWith("^")) {
				mimeType = "*" + mimeType.substring(1) + "*";
			}
			resultQuery.append("mimetype:\"").append(mimeType).append("\"");
		}

		SimpleDateFormat dateFormatter = null;

		String identifier = queryParams.getFirst("identifier");
		if (StringUtils.isNotBlank(identifier)) {
			startNextClause(resultQuery);
			resultQuery.append("identifier:\"")
					.append(namespaceRegistryService.buildFullUri(identifier)).append("\"");
		}
		// create the range query
		String createdFrom = queryParams.getFirst("createdFromDate");
		StringBuilder createdOnRange = null;
		if (StringUtils.isNotBlank(createdFrom)) {
			dateFormatter = new SimpleDateFormat(converterDateFormatPattern, Locale.ENGLISH);
			Date start = dateFormatter.parse(createdFrom);
			Calendar calendar = GregorianCalendar.getInstance();
			// solr works only with UTC format - use TZ query param to fix the offset
			calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
			calendar.setTime(start);
			createdOnRange = new StringBuilder("[");
			String formated = ISO8601DateFormat.format(calendar);
			createdOnRange.append(formated);
		}
		String createdTo = queryParams.getFirst("createdToDate");
		if (StringUtils.isNotBlank(createdTo)) {
			if (createdOnRange != null) {
				createdOnRange.append(" TO ");
			} else {
				createdOnRange = new StringBuilder("[* TO ");
				dateFormatter = new SimpleDateFormat(converterDateFormatPattern, Locale.ENGLISH);
			}
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(dateFormatter.parse(createdTo));
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			long paramTime = calendar.getTimeInMillis();
			calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
			calendar.setTimeInMillis(paramTime);
			String formated = ISO8601DateFormat.format(calendar);
			createdOnRange.append(formated).append("]");
		} else {
			if (createdOnRange != null) {
				createdOnRange.append(" TO *]");
			}
		}

		if (createdOnRange != null) {
			startNextClause(resultQuery);
			resultQuery.append("createdOn:").append(createdOnRange);
		}
		List<String> createdByList = queryParams.get("createdBy[]");
		if ((createdByList != null) && !createdByList.isEmpty()) {
			startNextClause(resultQuery);
			resultQuery.append("createdBy:").append(
					joinArgumentList(createdByList, " OR ", true, true));
		}
		if (resultQuery.length() == 0) {
			resultQuery.append(SolrQueryConstants.QUERY_DEFAULT_ALL);
		}
		searchArgs.getArguments().put(CommonParams.FQ, fqBasicSearch);
		return resultQuery;
	}

	/**
	 * Start next clause. Check whether to put AND clause in the query
	 *
	 * @param resultQuery
	 *            the result query to update
	 */
	private void startNextClause(StringBuilder resultQuery) {
		if (resultQuery.length() > 0) {
			resultQuery.append(" AND ");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isApplicable(MultivaluedMap<String, String> queryParams) {
		if (queryParams == null) {
			return false;
		}
		// TODO make a sophisticated check about the actual syntax - probably spraql
		if (StringUtils.isNotBlank(queryParams.getFirst("queryText"))) {
			return false;
		}
		List<String> objectRelationship = queryParams.get("objectRelationship[]");
		if ((objectRelationship != null) && !objectRelationship.isEmpty()) {
			return false;
		}
		// List<String> selectedFields = queryParams.get("fields[]");
		// if ((selectedFields != null) && !selectedFields.isEmpty()) {
		// return false;
		// }
		List<String> selectedLocations = queryParams.get("location[]");
		if ((selectedLocations != null) && !selectedLocations.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Join argument list and process each argument - make the namespace in full format and surround
	 * the whole argument in "".
	 *
	 * @param params
	 *            the params to join
	 * @param joinClause
	 *            the join clause to concat elements with
	 * @param updateNamespace
	 *            the update namespace if the argument list contains semantic namespace prefixes
	 * @param makePhrase
	 *            to surround each argument with ""
	 * @return the join arguments surrounded in ( )
	 */
	private StringBuilder joinArgumentList(List<String> params, String joinClause,
			boolean updateNamespace, boolean makePhrase) {
		StringBuilder result = new StringBuilder("( ");
		int size = params.size();
		for (int i = 0; i < size; i++) {
			if (makePhrase) {
				result.append("\"");
			}
			String nextArg = params.get(i);
			if (updateNamespace) {
				result.append(namespaceRegistryService.buildFullUri(nextArg));
			} else {
				result.append(nextArg);
			}
			if (makePhrase) {
				result.append("\"");
			}
			if ((i + 1) < size) {
				result.append(joinClause);
			}
		}
		result.append(" )");
		return result;

	}
}
