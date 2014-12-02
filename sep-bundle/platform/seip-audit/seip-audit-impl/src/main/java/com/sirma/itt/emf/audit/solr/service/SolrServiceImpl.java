package com.sirma.itt.emf.audit.solr.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfigurationProperties;
import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Used for interaction with the Solr server.
 * 
 * @author N. Velkov
 * @author Mihail Radkov
 */
@Stateless
public class SolrServiceImpl implements SolrService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrServiceImpl.class);

	/** The Solr server URL. */
	@Inject
	@Config(name = AuditConfigurationProperties.SOLR_ADDRESS, defaultValue = "http://localhost:8983/solr/db")
	private String url;

	/** Enables/disables the audit module. */
	@Inject
	@Config(name = AuditConfigurationProperties.AUDIT_ENABLED, defaultValue = "false")
	private Boolean auditEnabled;

	/** The Solr server. */
	private SolrServer server;

	/**
	 * Post construct.
	 */
	@PostConstruct
	public void postConstruct() {
		if (auditEnabled) {
			server = new HttpSolrServer(url);
			if (server instanceof HttpSolrServer) {
				((HttpSolrServer) server).setSoTimeout(5000);
			}
		}
	}

	@Override
	public SolrResult getIDsFromQuery(SolrQueryParams params) throws SolrServiceException {
		if (!auditEnabled || params == null) {
			SolrResult empty = new SolrResult();
			empty.setTotal(0);
			empty.setIds(Collections.<Long> emptyList());
			return empty;
		}

		TimeTracker tracker = TimeTracker.createAndStart();
		SolrQuery solrQuery = new SolrQuery();

		if (StringUtils.isEmpty(params.getQuery())) {
			throw new SolrServiceException("Emtpy Solr query failed");
		}

		solrQuery.setQuery(params.getQuery());
		solrQuery.setFilterQueries(params.getFilters());
		solrQuery.setStart(params.getStart());

		if ((params.getSortField() != null) && (params.getSortOrder() != null)) {
			String sortOrder = params.getSortOrder().trim();
			solrQuery.setSort(params.getSortField(), "asc".equalsIgnoreCase(sortOrder) ? ORDER.asc
					: ORDER.desc);
		}
		if (params.getRows() != 0) {
			solrQuery.setRows(params.getRows());
		}

		// Server communication
		QueryResponse rsp;
		try {
			rsp = server.query(solrQuery);
		} catch (SolrServerException e) {
			throw new SolrServiceException("Solr query failed {" + params.getQuery() + "}", e);
		} catch (SolrException ex) {
			throw new SolrServiceException("Solr query failed {" + params.getQuery() + "}", ex);
		}

		SolrDocumentList docs = rsp.getResults();

		List<Long> ids = new ArrayList<>(docs.size());
		for (SolrDocument doc : docs) {
			ids.add(Long.parseLong(doc.get("id").toString()));
		}

		SolrResult result = new SolrResult();
		result.setIds(ids);
		result.setTotal((int) docs.getNumFound());

		LOGGER.trace("Solr response recieved in {} ms; Query executed in: {} ms", tracker.stop(),
				rsp.getQTime());

		return result;
	}

	@Override
	public void deleteById(List<String> ids) throws SolrServiceException {
		if (auditEnabled && CollectionUtils.isNotEmpty(ids)) {
			try {
				server.deleteById(ids);
				server.commit();
			} catch (SolrServerException e) {
				throw new SolrServiceException("Solr delete query failed - " + e.getMessage(), e);
			} catch (IOException e) {
				throw new SolrServiceException("Solr delete query failed - " + e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean dataImport(boolean cleanImport) {
		if (!auditEnabled) {
			return true;
		}
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("qt", "/dataimport");
			params.set("command", "status");
			QueryResponse response = server.query(params);
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

			if (server instanceof HttpSolrServer) {
				((HttpSolrServer) server).setSoTimeout(0);
			}
			server.query(params);
			if (server instanceof HttpSolrServer) {
				((HttpSolrServer) server).setSoTimeout(5000);
			}
		} catch (SolrServerException e) {
			LOGGER.error("Data import failed due to: " + e.getMessage());
			LOGGER.debug(e.getMessage(), e);
			return false;
		} catch (SolrServiceException e) {
			LOGGER.error("Data import failed due to: " + e.getMessage());
			LOGGER.debug(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Retrieves the id of the last indexed record in Solr.
	 * 
	 * @return the id of the last indexed record
	 * @throws SolrServiceException
	 *             if something went wrong with the retrieval of the id
	 */
	private long getLastIndexedRecord() throws SolrServiceException {
		SolrQueryParams params = new SolrQueryParams();
		params.setQuery("*:*");
		params.setSortField("id");
		params.setSortOrder("desc");
		params.setRows(1);

		SolrResult result = getIDsFromQuery(params);
		if (result.getTotal() > 0) {
			return result.getIds().get(0);
		}

		return 0;
	}

}
