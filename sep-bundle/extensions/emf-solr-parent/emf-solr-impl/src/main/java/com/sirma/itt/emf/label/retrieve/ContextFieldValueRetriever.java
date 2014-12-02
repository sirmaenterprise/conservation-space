package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;

import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Converts entry URI to title
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 6)
public class ContextFieldValueRetriever implements FieldValueRetriever {
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<String>(1);
		SUPPORTED_FIELDS.add(FieldId.CONTEXT);
	}
	private static final Logger LOGGER = Logger.getLogger(ContextFieldValueRetriever.class);

	private static final String DEFAULT_QUERY = "*:*";

	private static final String CONTEXT_SOLR_QUERY = "(instanceType:projectinstance OR instanceType:caseinstance) AND -status:(DELETED) AND isDeleted:false";
	private static final String MAX_NUMBER_OF_RESULTS = "100000";

	@Inject
	private SolrConnector solrConnector;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Override
	public String getLabel(String... value) {
		if (!ArrayUtils.isEmpty(value) && value[0] != null) {
			SolrQuery parameters = new SolrQuery();
			parameters.setParam(CommonParams.Q, DEFAULT_QUERY);
			parameters.setParam(CommonParams.FL, DefaultProperties.TITLE);

			// Workaround for a bug in basic search where context can be with both short or full URI
			String uri = value[0];
			try {
				uri = namespaceRegistryService.buildFullUri(uri);
			} catch (Exception e) {
				// ignore
			}

			String filterQuery = "uri:\"" + ClientUtils.escapeQueryChars(uri) + "\"";
			parameters.setParam(CommonParams.FQ, filterQuery);
			try {
				QueryResponse queryResponse = solrConnector.queryWithGet(parameters);
				if (queryResponse != null) {
					SolrDocumentList solrDocumentList = queryResponse.getResults();
					if (!solrDocumentList.isEmpty()) {
						SolrDocument solrDocument = solrDocumentList.get(0);
						String title = getSolrFieldValue(solrDocument, DefaultProperties.TITLE);
						return title;
					}
				}
			} catch (SolrClientException e) {
				LOGGER.error(e.getMessage());
			}

			return value[0];
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit) {
		List<Pair<String, String>> results = new ArrayList<>();
		SolrQuery parameters = new SolrQuery();
		parameters.setParam(CommonParams.Q, DEFAULT_QUERY);
		parameters.setParam(CommonParams.FL, "instanceType,identifier,title,uri,instanceType");

		String filterQuery = CONTEXT_SOLR_QUERY;
		if (com.sirma.itt.commons.utils.string.StringUtils.isNotNullOrEmpty(filter)) {
			int whitespacePos = filter.indexOf(" ");
			if (whitespacePos != -1) {
				filter = filter.substring(0, whitespacePos);
			}
			filterQuery = filterQuery + " AND identifier:" + ClientUtils.escapeQueryChars(filter)
					+ "*";
		}

		parameters.setParam(CommonParams.FQ, filterQuery);
		parameters.setParam(CommonParams.START, String.valueOf(offset));
		parameters.setParam(CommonParams.ROWS, String.valueOf(limit));

		parameters.setTimeAllowed(2000);

		long total = 0;
		try {
			QueryResponse queryResponse = solrConnector.queryWithGet(parameters);
			if (queryResponse != null) {
				SolrDocumentList solrDocumentList = queryResponse.getResults();
				total = solrDocumentList.getNumFound();
				for (int i = 0; i < solrDocumentList.size(); i++) {
					SolrDocument solrDocument = solrDocumentList.get(i);
					String uri = getSolrFieldValue(solrDocument, DefaultProperties.URI);
					String shortURI = namespaceRegistryService.getShortUri(uri);
					String label = getSolrFieldValue(solrDocument, DefaultProperties.TITLE);
					results.add(new Pair<String, String>(shortURI, label));
				}
			}
		} catch (SolrClientException e) {
			LOGGER.error(e.getMessage());
		}

		return new RetrieveResponse(total, results);
	}

	/**
	 * Generate context label as specified in audit log.
	 * 
	 * @param solrDocument
	 *            the solr document
	 * @return the label
	 */
	private String generateContextLabel(SolrDocument solrDocument) {
		String instanceType = getSolrFieldValue(solrDocument, "instanceType");
		String rdfType = null;
		if ("projectinstance".equals(instanceType)) {
			rdfType = "emf:Project";
		} else if ("caseinstance".equals(instanceType)) {
			rdfType = "emf:Case";
		}

		ClassInstance classInstance = semanticDefinitionService.getClassInstance(rdfType);
		String type = null;
		if (classInstance != null
				&& classInstance.getProperties().get(DefaultProperties.TITLE) != null) {
			type = (String) classInstance.getProperties().get(DefaultProperties.TITLE);
		}

		String label = getSolrFieldValue(solrDocument, DefaultProperties.UNIQUE_IDENTIFIER) + " "
				+ getSolrFieldValue(solrDocument, DefaultProperties.TITLE)
				+ (type != null ? " (" + type + ")" : "");

		return label;
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
	private String getSolrFieldValue(SolrDocument document, String fieldName) {
		Object fieldValue = document.getFieldValue(fieldName);
		if (fieldValue instanceof ArrayList<?>) {
			ArrayList<?> values = ((ArrayList<?>) fieldValue);
			if (!values.isEmpty()) {
				return String.valueOf(values.get(0));
			}
		}
		return "";
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
