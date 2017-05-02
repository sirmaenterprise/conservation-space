package com.sirma.itt.emf.audit.db;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;

import com.sirma.itt.emf.audit.converter.AuditActivityConverter;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;

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

}
