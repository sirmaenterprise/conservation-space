package com.sirma.itt.emf.solr.services;

import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.query.DefaultQueryVisitor;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * The SolrConnector is implementation that supports various request to solr server through http requests.
 *
 * @author Borislav Banchev
 */
@ApplicationScoped
public class SolrConnectorImpl implements SolrConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrConnectorImpl.class);
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	@Inject
	private SolrConfiguration solrConfig;

	@Inject
	private Statistics statistics;

	@Inject
	private SolrSearchConfiguration searchConfiguration;

	@Override
	public QueryResponse queryWithGet(SolrQuery parameters) throws SolrClientException {
		return query(parameters, METHOD.GET);
	}

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
			TimeTracker start = statistics.createTimeStatistics(getClass(), "solrSearch");
			if (TRACE_ENABLED) {
				start.begin();
			}
			parameters.set(CommonParams.OMIT_HEADER, true);
			if (TRACE_ENABLED) {
				parameters.set(CommonParams.DEBUG_QUERY, "true");
				LOGGER.trace("Executing solr search with params: " + parameters);
			}

			includeDefaults(parameters);

			QueryResponse response = new QueryRequest(parameters, method).process(solrConfig.getSolrServer());
			if (TRACE_ENABLED) {
				long stop = start.stop();
				Object debugInfo = parameters.get(CommonParams.Q);
				debugInfo = debugInfo == null ? parameters.toString() : debugInfo;
				LOGGER.trace("Solr search took {} ms for query '{}'.Result is of size: {}", stop, debugInfo,
						response.getResults() != null ? response.getResults().size() : -1);
				LOGGER.trace("Query Debug:" + response.getDebugMap());
			}
			return response;
		} catch (Exception e) {
			LOGGER.error("Solr search failed for query '{}'", parameters.get(CommonParams.Q));
			throw new SolrClientException(e);
		}

	}

	private void includeDefaults(SolrQuery query) {
		if (query.get(CommonParams.TZ) == null) {
			query.set(CommonParams.TZ, TimeZone.getDefault().getID());
		}
		if (query.get(CommonParams.ROWS) == null) {
			query.set(CommonParams.ROWS, 25);
		}
		if (query.get(CommonParams.Q) == null) {
			query.set(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_EMPTY);
		}
		if (query.get(CommonParams.DF) == null) {
			query.set(CommonParams.DF, "title,content");
		}
		if (searchConfiguration.getStatusFilterQuery().isSet()) {
			query.add(CommonParams.FQ, searchConfiguration.getStatusFilterQuery().get());
		}
	}

	@Override
	public QueryResponse suggest(SearchArguments<Object> query) throws SolrClientException {
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), "suggest").begin();
		try {
			SolrQuery parameters = new SolrQuery();
			String mQueryString = query.getStringQuery();
			if (mQueryString == null) {
				mQueryString = defaultVisit(query);
			}
			parameters.set(CommonParams.Q, mQueryString);
			parameters.set(CommonParams.QT, "/suggest");
			return new QueryRequest(parameters).process(solrConfig.getSolrServer());
		} catch (Exception e) {
			throw new SolrClientException(e);
		} finally {
			tracker.stop();
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
	private static String defaultVisit(SearchArguments<Object> query) throws Exception {
		DefaultQueryVisitor queryVisitor = new DefaultQueryVisitor() {

			@Override
			public void visit(Query q) throws Exception {
				getQuery().append(q.getKey()).append(":\"").append(q.getValue()).append("\"");
			}
		};
		queryVisitor.visit(query.getQuery());
		return queryVisitor.getQuery().toString();
	}

}
