package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;

import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Retrieve value label pairs from solr.
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 9)
public class SolrFieldValueRetriever extends PairFieldValueRetriever {
	private static final Logger LOGGER = Logger.getLogger(SolrFieldValueRetriever.class);

	private static final String AND = " AND ";
	private static final String OR = " OR ";
	private static final String DEFAULT_QUERY = "*:*";
	private static final String BASE_FILTER_QUERY = "-status:(DELETED) AND isDeleted:false";
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.SOLR);
	}

	@Inject
	private SolrConnector solrConnector;

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		String valueField = additionalParameters.getFirst(FieldValueRetrieverParameters.VALUE_FIELD);
		String labelField = additionalParameters.getFirst(FieldValueRetrieverParameters.LABEL_FIELD);
		if (StringUtils.isNotBlank(valueField) && StringUtils.isNotBlank(labelField)) {
			SolrQuery parameters = new SolrQuery();
			parameters.setParam(CommonParams.Q, DEFAULT_QUERY);
			parameters.setParam(CommonParams.FL, valueField);
			String filterQuery = BASE_FILTER_QUERY + AND + labelField + ":\"" + value + "\"";
			parameters.setParam(CommonParams.FQ, filterQuery);
			parameters.setParam(CommonParams.START, "0");
			parameters.setParam(CommonParams.ROWS, "1");
			parameters.setTimeAllowed(2000);
			try {
				QueryResponse queryResponse = solrConnector.query(parameters);
				if (queryResponse != null) {
					SolrDocumentList solrDocumentList = queryResponse.getResults();
					if (!solrDocumentList.isEmpty()) {
						return getSolrFieldValue(solrDocumentList.get(0), valueField);
					}
				}
			} catch (SolrClientException e) {
				LOGGER.error("Failed to retrieve all contexts from solr", e);
			}
		}
		return value;
	}

	/**
	 * @param filter
	 *            - filter to be applied on the results. Contains comparison is used
	 * @param additionalParameters
	 *            - map with additional parameters for the method:
	 *            <ul>
	 *            <li><b>valuefield</b> - solr field to be returned as a value. <b>Required</b></li>
	 *            <li><b>labelfield</b> - solr field to be returned as a label. It is also used for applying filter.
	 *            <b>Required</b></li>
	 *            <li><b>fq</b> - additional filter query to be performed</li>
	 *            </ul>
	 * @param offset
	 *            returned results offset
	 * @param limit
	 *            number of returned results
	 * @return {@link RetrieveResponse} object containing total number of results and a list with value-label pairs
	 */
	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		List<Pair<String, String>> results = new ArrayList<>();
		long total = 0;
		String valueField = additionalParameters.getFirst(FieldValueRetrieverParameters.VALUE_FIELD);
		String labelField = additionalParameters.getFirst(FieldValueRetrieverParameters.LABEL_FIELD);
		if (StringUtils.isNotBlank(valueField) && StringUtils.isNotBlank(labelField)) {
			String fq = additionalParameters.getFirst(FieldValueRetrieverParameters.FILTER_QUERY);

			SolrQuery parameters = new SolrQuery();
			parameters.setParam(CommonParams.Q, DEFAULT_QUERY);
			parameters.setParam(CommonParams.FL, valueField + "," + labelField);

			StringBuilder filterQuery = new StringBuilder(BASE_FILTER_QUERY);
			if (StringUtils.isNotBlank(fq)) {
				filterQuery.append(AND).append(fq);
			}
			if (StringUtils.isNotBlank(filter)) {
				String[] filters = filter.split("\\s+");
				StringBuilder additionalFilter = new StringBuilder();

				if (filters.length > 1) {
					additionalFilter.append("(");
				}
				for (int i = 0; i < filters.length; i++) {
					if (i > 0) {
						additionalFilter.append(OR);
					}
					additionalFilter
							.append(labelField)
								.append(":*")
								.append(ClientUtils.escapeQueryChars(filters[i]))
								.append("*");
				}
				if (filters.length > 1) {
					additionalFilter.append(")");
				}

				filterQuery.append(AND).append(additionalFilter);
			}
			parameters.setParam(CommonParams.FQ, filterQuery.toString());
			parameters.setParam(CommonParams.START, String.valueOf(offset));
			parameters.setParam(CommonParams.ROWS, String.valueOf(limit));
			parameters.setTimeAllowed(2000);
			try {
				QueryResponse queryResponse = solrConnector.query(parameters);
				if (queryResponse != null) {
					SolrDocumentList solrDocumentList = queryResponse.getResults();
					total = solrDocumentList.getNumFound();
					for (int i = 0; i < solrDocumentList.size(); i++) {
						SolrDocument solrDocument = solrDocumentList.get(i);
						String value = getSolrFieldValue(solrDocument, valueField);
						String label = getSolrFieldValue(solrDocument, labelField);
						results.add(new Pair<>(value, label));
					}
				}
			} catch (SolrClientException e) {
				LOGGER.error("Failed to retrieve all contexts from solr", e);
			}

		} else {
			LOGGER.error(FieldValueRetrieverParameters.VALUE_FIELD + " and " + FieldValueRetrieverParameters.LABEL_FIELD
					+ " are required.");
		}

		return new RetrieveResponse(total, results);
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
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
		if (fieldValue instanceof ArrayList<?>) {
			List<?> values = (ArrayList<?>) fieldValue;
			if (!values.isEmpty()) {
				return String.valueOf(values.get(0));
			}
		}
		return "";
	}

}
