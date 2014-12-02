package com.sirma.itt.emf.audit.db;

import java.util.ListIterator;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.converter.AuditActivityConverter;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;

/**
 * Combines the functionality of the RDB and the Solr services and retrieves all audit activities by
 * the requested solr query parameters.
 * 
 * @author Vilizar Tsonev
 */
@Stateless
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
	public ServiceResult getActivitiesBySolrQuery(SolrQueryParams solrQuery)
			throws SolrServiceException {

		SolrResult solrResult = solrService.getIDsFromQuery(solrQuery);
		ServiceResult dbResult = auditDao.getActivitiesByIDs(solrResult.getIds());
		for (ListIterator<AuditActivity> iter = dbResult.getRecords().listIterator(); iter
				.hasNext();) {
			AuditActivity activityCopy = new AuditActivity(iter.next());
			converter.convertActivity(activityCopy);
			iter.set(activityCopy);
		}
		dbResult.setTotal(solrResult.getTotal());
		return dbResult;
	}
}
