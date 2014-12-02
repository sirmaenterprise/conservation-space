package com.sirma.itt.emf.solr.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.query.DefaultQueryVisitor;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * The SolrConnector is implementation that supports various request to solr server through http
 * requests.
 */
@ApplicationScoped
public class SolrConnectorImpl implements SolrConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(SolrConnectorImpl.class);
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();
	private static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();
	/** The solr server. */
	@Inject
	private SolrServer solrServer;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryResponse queryWithGet(SolrQuery parameters) throws SolrClientException {
		return query(parameters, METHOD.GET);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryResponse queryWithPost(SolrQuery parameters) throws SolrClientException {
		return query(parameters, METHOD.POST);
	}

	/**
	 * Executes a remote query over solr with the specified method.
	 *
	 * @param parameters
	 *            the parameters
	 * @param method
	 *            the specified method
	 * @return the solr response with results and all the relevant data.
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	private QueryResponse query(SolrQuery parameters, METHOD method) throws SolrClientException {
		try {
			TimeTracker start = null;
			if (DEBUG_ENABLED) {
				start = TimeTracker.createAndStart();
			}
			parameters.set(CommonParams.OMIT_HEADER, true);
			// TODO from config what is possible
			if (parameters.get(CommonParams.ROWS) == null) {
				parameters.set(CommonParams.ROWS, 25);
			}
			if (parameters.get(CommonParams.Q) == null) {
				parameters.set(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_EMPTY);
			}
			if (parameters.get(CommonParams.DF) == null) {
				parameters.set(CommonParams.DF, "all_text");
			}
			if (TRACE_ENABLED) {
				parameters.set(CommonParams.DEBUG_QUERY, "true");
				LOGGER.trace("Executing solr search with params: " + parameters);
			}

			QueryRequest queryRequest = new QueryRequest(parameters, method);
			QueryResponse response = queryRequest.process(solrServer);
			if (DEBUG_ENABLED) {
				long stop = start.stop();
				LOGGER.debug("Solr search took {} ms for query '{}'.Result is of size: {}", stop,
						parameters.get(CommonParams.Q), response.getResults() != null ? response
								.getResults().size() : -1);
			}
			if (TRACE_ENABLED) {
				LOGGER.trace("Query Debug:" + response.getDebugMap());
			}
			return response;
		} catch (Exception e) {
			LOGGER.error("Solr search failed for query '{}'", parameters.get(CommonParams.Q));
			throw new SolrClientException(e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryResponse suggest(SearchArguments<Object> query) throws SolrClientException {
		try {
			// /https://cwiki.apache.org/confluence/display/solr/Suggester
			SolrQuery parameters = new SolrQuery();
			String mQueryString = query.getStringQuery();
			if (mQueryString == null) {
				mQueryString = defaultVisit(query);
			}
			parameters.set(CommonParams.Q, mQueryString);
			parameters.set(CommonParams.QT, "/suggest");
			QueryRequest request = new QueryRequest(parameters);
			QueryResponse process = request.process(solrServer);
			return process;
		} catch (Exception e) {
			throw new SolrClientException(e);
		}

	}

	/**
	 * Default visit for queries.
	 *
	 * @param query
	 *            the query
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	private String defaultVisit(SearchArguments<Object> query) throws Exception {
		DefaultQueryVisitor queryVisitor = new DefaultQueryVisitor() {

			@Override
			public void visit(Query query) throws Exception {
				getQuery().append(query.getKey()).append(":\"").append(query.getValue())
						.append("\"");

			}
		};
		queryVisitor.visit(query.getQuery());
		return queryVisitor.getQuery().toString();
	}

}
