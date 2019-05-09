package com.sirma.itt.emf.solr.services;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
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
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.monitor.annotations.Monitored;

/**
 * The SolrConnector is implementation that supports various request to solr
 * server through http requests.
 *
 * @author Borislav Banchev
 */
@ApplicationScoped
public class SolrConnectorImpl implements SolrConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SolrConfiguration solrConfig;

	@Inject
	private SolrSearchConfiguration searchConfiguration;

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> retrieveSchema() {
		SolrQuery query = new SolrQuery();
		query.setParam(CommonParams.QT, "/schema");
		query.setParam(CommonParams.WT, "json");

		try {
			Map<String, Object> res = (Map<String, Object>) runQuery(query, METHOD.GET).getResponse().get("schema");
			return new LinkedHashMap<>(res);
		} catch (SolrClientException e) {
			LOGGER.error("unable to load solr schema", e);
		}
		return null;
	}

	@Override
	@Monitored(@MetricDefinition(name = "solr_search_duration_seconds", type = Type.TIMER, descr = "Search in solr duration in seconds."))
	public QueryResponse query(SolrQuery query) throws SolrClientException {
		return runQuery(query, METHOD.POST);
	}

	private QueryResponse runQuery(SolrQuery query, METHOD method) throws SolrClientException {
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

		boolean trace = LOGGER.isTraceEnabled();
		if (trace) {
			query.set(CommonParams.DEBUG_QUERY, "true");
		}

		try {
			QueryResponse response = new QueryRequest(query, method).process(solrConfig.getSolrServer());

			if (trace) {
				Object debugInfo = query.get(CommonParams.Q);
				LOGGER.trace("solr search query: {}", debugInfo);
				LOGGER.trace("solr search query debug: {}", response.getDebugMap());
			}
			return response;
		} catch (SolrServerException | IOException e) {
			throw new SolrClientException(e);
		}
	}

	@Override
	public QueryResponse suggest(SearchArguments<Object> query) throws SolrClientException {
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
