package com.sirma.itt.emf.audit.solr.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Used for interaction with the Solr server.
 *
 * @author N. Velkov
 * @author Mihail Radkov
 */
@ApplicationScoped
public class SolrServiceImpl implements SolrService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrServiceImpl.class);

	@Inject
	AuditConfiguration auditConfiguration;

	@Override
	public SolrResult getIDsFromQuery(SolrQuery solrQuery) throws SolrServiceException {
		if (solrQuery == null) {
			return generateEmptyResult();
		}

		if (solrQuery.getQuery() == null) {
			throw new SolrServiceException("Cannot query the solr with empty query.");
		}

		TimeTracker tracker = TimeTracker.createAndStart();

		// Server communication
		QueryResponse rsp = query(solrQuery);
		SolrResult result = generateResult(rsp);

		LOGGER.trace("Solr response received in {} ms; Query executed in: {} ms", tracker.stop(), rsp.getQTime());

		return result;
	}

	@Override
	public void deleteById(List<String> ids) throws SolrServiceException {
		if (CollectionUtils.isNotEmpty(ids)) {
			try {
				SolrClient server = auditConfiguration.getSolrClient().get();
				server.deleteById(ids);
				server.commit();
			} catch (SolrServerException e) {
				throw new SolrServiceException("Solr delete query failed - " + e.getMessage(), e);
			} catch (IOException e) {
				throw new SolrServiceException("Solr delete query failed - " + e.getMessage(), e);
			}
		}
	}

	// Refactor to use query()
	@Override
	public boolean dataImport(boolean cleanImport) {
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("qt", "/dataimport");
			params.set("command", "status");
			SolrClient client = auditConfiguration.getSolrClient().get();
			QueryResponse response = client.query(params);
			// Check status
			if (DATA_IMPORT_STATUS_BUSY.equals(response.getResponse().get("status"))) {
				LOGGER.debug("Solr data import is currently running.");
				return false;
			}

			params.set("command", "full-import");
			params.set("clean", cleanImport);
			params.set("commit", "true");

			if (!cleanImport) {
				long lastIndexedRecord = getLastIndexedRecord();
				params.set("lastId", Long.toString(lastIndexedRecord));
			}

			changeServerTimeout(0);
			client.query(params);
			changeServerTimeout(5000);

		} catch (Exception e) {
			LOGGER.error("Data import failed due to: " + e.getMessage());
			LOGGER.debug(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Changes the socket timeout of the Solr server to the specific milliseconds value.
	 *
	 * @param milliseconds
	 *            - the value
	 */
	private void changeServerTimeout(int milliseconds) {
		SolrClient server = auditConfiguration.getSolrClient().get();
		if (server instanceof HttpSolrClient) {
			((HttpSolrClient) server).setSoTimeout(milliseconds);
		}
	}

	/**
	 * Generates an empty result.
	 *
	 * @return an empty result
	 */
	private static SolrResult generateEmptyResult() {
		SolrResult empty = new SolrResult();
		empty.setTotal(0);
		empty.setIds(Collections.<Long> emptyList());
		return empty;
	}

	/**
	 * Generates {@link SolrResult} based on the Solr server response. Iterates the response and gets only the ID field.
	 * Finally sets the total search hits.
	 *
	 * @param rsp
	 *            the server response
	 * @return the generated result
	 */
	private static SolrResult generateResult(QueryResponse rsp) {
		SolrDocumentList docs = rsp.getResults();

		List<Long> ids = new ArrayList<>(docs.size());
		for (SolrDocument doc : docs) {
			ids.add(Long.valueOf(doc.get("id").toString()));
		}

		SolrResult result = new SolrResult();
		result.setIds(ids);
		result.setTotal(docs.getNumFound());

		return result;
	}

	/**
	 * Makes a call to the Solr server with the provided Solr query.
	 *
	 * @param solrQuery
	 *            the Solr query
	 * @return the Solr server response
	 * @throws SolrServiceException
	 *             if while making the call a problem occurs
	 */
	private QueryResponse query(SolrQuery solrQuery) throws SolrServiceException {
		try {
			return auditConfiguration.getSolrClient().get().query(solrQuery);
		} catch (Exception e) {
			throw new SolrServiceException("Solr query failed - " + e.getMessage(), e);
		}
	}

	/**
	 * Retrieves the id of the last indexed record in Solr.
	 *
	 * @return the id of the last indexed record
	 * @throws SolrServiceException
	 *             if something went wrong with the retrieval of the id
	 */
	private long getLastIndexedRecord() throws SolrServiceException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("*:*");
		solrQuery.setSort("id", ORDER.desc);
		solrQuery.setRows(Integer.valueOf(1));

		SolrResult result = getIDsFromQuery(solrQuery);
		if (result.getTotal() > 0) {
			return result.getIds().get(0).longValue();
		}

		return 0;
	}

}
