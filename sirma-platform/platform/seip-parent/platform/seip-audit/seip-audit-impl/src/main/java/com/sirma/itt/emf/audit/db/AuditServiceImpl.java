package com.sirma.itt.emf.audit.db;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;

import com.sirma.itt.emf.audit.converter.AuditActivityConverter;
import com.sirma.itt.emf.audit.rest.AuditSearchRequest;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.solr.services.query.SolrSearchQueryBuilder;

/**
 * Combines the functionality of the RDB and the Solr services and retrieves all audit activities by the requested solr
 * query parameters.
 *
 * @author Vilizar Tsonev
 * @author Mihail Radkov
 */
@ApplicationScoped
public class AuditServiceImpl implements AuditService {

	/** The solr service. */
	@Inject
	private SolrService solrService;

	/** The audit relational DB DAO. */
	@Inject
	private AuditDao auditDao;

	@Inject
	private AuditActivityConverter converter;

	@Inject
	private SolrSearchQueryBuilder solrSearchQueryBuilder;

	@Override
	public ServiceResult getActivitiesBySolrQuery(SolrQuery solrQuery) throws SolrServiceException {
		SolrResult solrResult = solrService.getIDsFromQuery(solrQuery);
		ServiceResult dbResult = auditDao.getActivitiesByIDs(solrResult.getIds());

		if (dbResult.getRecords().isEmpty()) {
			return dbResult;
		}

		converter.convertActivities(dbResult.getRecords());
		dbResult.setTotal(solrResult.getTotal());

		return dbResult;
	}


	@Override
	public ServiceResult getActivities(AuditSearchRequest auditSearchRequest) throws SolrServiceException {
		return getActivitiesBySolrQuery(buildSolrQuery(auditSearchRequest));
	}

	/**
	 * Builds the solr query from the given arguments
	 *
	 * @param searchRequest - {@link AuditSearchRequest} containg all the information to build {@link SolrQuery}
	 * @return the solr query
	 */
	private SolrQuery buildSolrQuery(AuditSearchRequest searchRequest) {
		String query = solrSearchQueryBuilder.buildSolrQuery(searchRequest.getSearchTree());
		SolrQuery solrQuery = new SolrQuery(query);
		solrQuery.setSort("eventdate", SolrQuery.ORDER.desc);
		setSkip(searchRequest, solrQuery);
		setRows(searchRequest, solrQuery);
		return solrQuery;
	}

	/**
	 * Sets the rows.
	 *
	 * @param searchRequest
	 *            the searchRequest
	 * @param query
	 *            the solr query
	 */
	private static void setRows(AuditSearchRequest searchRequest, SolrQuery query) {
		List<String> pageSize = searchRequest.getRequest().get("pageSize");
		query.setRows(Integer.valueOf(pageSize.get(0)));
	}

	/**
	 * Sets the skip.
	 *
	 * @param searchRequest
	 *            the searchRequest
	 * @param query
	 *            the solr query
	 */
	private static void setSkip(AuditSearchRequest searchRequest, SolrQuery query) {
		List<String> pageSize = searchRequest.getRequest().get("pageSize");
		List<String> pageNumber = searchRequest.getRequest().get("pageNumber");
		int	skipSize = Math.max(0, Math.max(Integer.valueOf(pageSize.get(0)), 0) * (Integer.valueOf(pageNumber.get(0)) - 1));
		if (skipSize > 0) {
			query.setStart(skipSize);
		}
	}

}
