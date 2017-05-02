package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;

import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Converts entry URI to title
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 7)
public class ObjectFieldValueRetriever extends PairFieldValueRetriever {

	private static final Logger LOGGER = Logger.getLogger(ObjectFieldValueRetriever.class);

	/** maximum number of URIs to be send to SOLR with a single request */
	private static final int RECORDS_PER_SOLR_REQUEST = 1000;

	private static final String DEFAULT_FIELD = DefaultProperties.TITLE;
	private static final String QUOTE = "\"";

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.OBJECT);
	}

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.runtime.fq.audit.object", defaultValue = "-status:(DELETED) AND isDeleted:false "
			+ "AND -instanceType:sectioninstance AND -instanceType:user "
			+ "AND -instanceType:topicinstance AND -instanceType:commentinstance "
			+ "AND -rdfType:\"http://www.ontotext.com/proton/protontop#Group\"")
	private ConfigurationProperty<String> filterQueryObjects;

	@Inject
	private SolrConnector solrConnector;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (!StringUtils.isEmpty(value)) {
			String field = getField(additionalParameters);

			SolrQuery parameters = new SolrQuery();
			parameters.setParam(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_ALL);
			parameters.setParam(CommonParams.FL, field);

			// Workaround for a bug in basic search where context can be with both short or full URI
			String uri = value;
			try {
				uri = namespaceRegistryService.buildFullUri(uri);
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Failed while building the full uri from " + uri, e);
			}

			// 54 -> field + quotes + escaped uri
			StringBuilder filterQuery = new StringBuilder(54);
			filterQuery.append(DefaultProperties.URI).append(":");
			filterQuery.append(QUOTE);
			filterQuery.append(ClientUtils.escapeQueryChars(uri));
			filterQuery.append(QUOTE);

			parameters.setParam(CommonParams.FQ, filterQuery.toString());
			try {
				QueryResponse queryResponse = solrConnector.queryWithGet(parameters);
				if (queryResponse != null) {
					return readSolrDocumentResults(queryResponse.getResults(), field)
							.findAny()
								.orElse(Pair.nullPair())
								.getSecond();
				}
			} catch (SolrClientException e) {
				LOGGER.error("Failed while retrieving the context from solr ", e);
			}

			return value;
		}
		return value;
	}

	@Override
	public Map<String, String> getLabels(String[] values, SearchRequest additionalParameters) {
		Map<String, String> result = null;
		String field = getField(additionalParameters);

		SolrQuery parameters = new SolrQuery();
		parameters.setParam(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_ALL);
		parameters.setParam(CommonParams.FL, DefaultProperties.URI + "," + field);

		// fetch in batches with max size of RECORDS_PER_SOLR_REQUEST because of a Solr restriction
		int start = 0;
		while (values != null && values.length > start) {
			StringBuilder filterQuery = new StringBuilder();
			filterQuery.append(DefaultProperties.URI).append(":(");

			for (int i = start, limit = 0; i < values.length
					&& limit < RECORDS_PER_SOLR_REQUEST; i++, limit++, start++) {
				if (i != 0) {
					filterQuery.append(" OR ");
				}
				filterQuery.append(ClientUtils.escapeQueryChars(values[i]));
			}
			filterQuery.append(")");

			parameters.setParam(CommonParams.FQ, filterQuery.toString());
			parameters.setParam(CommonParams.START, String.valueOf(0));
			parameters.setParam(CommonParams.ROWS, String.valueOf(RECORDS_PER_SOLR_REQUEST));
			parameters.setTimeAllowed(2000);

			try {
				QueryResponse queryResponse = solrConnector.queryWithPost(parameters);
				if (queryResponse != null) {
					result = readSolrDocumentResults(queryResponse.getResults(), field).collect(Pair.toMap());
				}
			} catch (SolrClientException e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				if (result == null) {
					result = CollectionUtils.emptyMap();
				}
			}
		}
		return result;
	}

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		List<Pair<String, String>> results = null;
		String field = getField(additionalParameters);

		SolrQuery parameters = new SolrQuery();
		parameters.setParam(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_ALL);
		parameters.setParam(CommonParams.FL, DefaultProperties.URI + "," + field);

		StringBuilder filterQuery = createFilterQuery(filter, additionalParameters);

		parameters.setParam(CommonParams.FQ, filterQuery.toString());
		parameters.setParam(CommonParams.START, String.valueOf(offset));
		parameters.setParam(CommonParams.ROWS, String.valueOf(limit));

		parameters.setTimeAllowed(2000);

		long total = 0;
		try {
			QueryResponse queryResponse = solrConnector.queryWithGet(parameters);
			if (queryResponse != null) {
				SolrDocumentList solrDocumentList = queryResponse.getResults();
				total = solrDocumentList.getNumFound();

				results = readSolrDocumentResults(solrDocumentList, field).collect(Collectors.toList());
			}
		} catch (SolrClientException e) {
			LOGGER.error("Failed to retrieve all contexts from solr", e);
		} finally {
			if (results == null) {
				results = CollectionUtils.emptyList();
			}
		}

		return new RetrieveResponse(total, results);
	}

	/**
	 * Read solr document results and returns a stream that contains of pairs of the document uri and the value of the
	 * given field found in each solr document
	 *
	 * @param solrDocumentList
	 *            the solr document list
	 * @param field
	 *            the field
	 * @return the stream
	 */
	private Stream<Pair<String, String>> readSolrDocumentResults(SolrDocumentList solrDocumentList, String field) {
		return solrDocumentList.stream().map(solrDocument -> {
			String uri = getSolrFieldValue(solrDocument, DefaultProperties.URI);
			String shortURI = namespaceRegistryService.getShortUri(uri);
			String extractedField = getSolrFieldValue(solrDocument, field);
			return new Pair<>(shortURI, extractedField);
		});
	}

	/**
	 * Generate a filter query depending on the filter and additional parameters.
	 *
	 * @param filter
	 *            the filter
	 * @param additionalParameters
	 *            the additional parameters
	 * @return the generated filter query
	 */
	protected StringBuilder createFilterQuery(String filter, SearchRequest additionalParameters) {
		StringBuilder filterQuery = new StringBuilder();
		if (additionalParameters != null) {
			String fq = additionalParameters.getFirst(FieldValueRetrieverParameters.FILTER_QUERY);

			boolean ignoreDefaultQuery = additionalParameters
					.getFirstBoolean(FieldValueRetrieverParameters.IGNORE_DEFAULT_QUERY);

			if (!ignoreDefaultQuery) {
				filterQuery.append(filterQueryObjects.get());
			}

			if (StringUtils.isNotBlank(fq)) {
				if (filterQuery.length() > 0) {
					filterQuery.append(" AND ");
				}
				filterQuery.append(fq);
			}
		}
		if (StringUtils.isNotEmpty(filter)) {
			// what is this?
			int whitespacePos = filter.indexOf(' ');
			String localFilter = filter;
			if (whitespacePos != -1) {
				localFilter = localFilter.substring(0, whitespacePos);
			}

			String escapedFilter = ClientUtils.escapeQueryChars(localFilter);
			filterQuery.append(" AND (");
			filterQuery.append(DefaultProperties.UNIQUE_IDENTIFIER);
			filterQuery.append(":");
			filterQuery.append(escapedFilter);
			filterQuery.append("* OR ");
			filterQuery.append(DefaultProperties.TITLE);
			filterQuery.append(":");
			filterQuery.append(escapedFilter);
			filterQuery.append("*)");
		}
		return filterQuery;
	}

	/**
	 * Gets the solr field single value.
	 *
	 * @param document
	 *            the document
	 * @param fieldName
	 *            the field name
	 * @return the solr field value
	 */
	private static String getSolrFieldValue(SolrDocument document, String fieldName) {
		Object fieldValue = document.getFieldValue(fieldName);
		if (fieldValue instanceof List<?>) {
			List<?> values = (List<?>) fieldValue;
			if (!values.isEmpty()) {
				return String.valueOf(values.get(0));
			}
		} else if (fieldValue instanceof String) {
			return (String) fieldValue;
		}
		return "";
	}

	/**
	 * Gets the field for extraction from the provided parameters or the default value {@link #DEFAULT_FIELD} if no
	 * parameters are provided.
	 *
	 * @param additionalParameters
	 *            - the parameters
	 * @return - the field for extraction from Solr
	 */
	private static String getField(SearchRequest additionalParameters) {
		if (additionalParameters != null) {
			String field = additionalParameters.getFirst(FieldValueRetrieverParameters.FIELD);
			if (StringUtils.isNotEmpty(field)) {
				return field;
			}
		}
		return DEFAULT_FIELD;
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}

}
