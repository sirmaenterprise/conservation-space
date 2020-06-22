package com.sirma.itt.emf.audit.db;

import com.sirma.itt.emf.audit.rest.AuditSearchRequest;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * The general service that combines the rdb and Solr services and provides one more level of abstraction.
 *
 * @author Nikolay Velkov
 */
public interface AuditService {

	/**
	 * Retrieves the ids of the activities from Solr with the specified query and queries the relational database for
	 * the complete records with the given ids.
	 *
	 * @param solrQuery
	 *            the solr used for id retrieval
	 * @return list of activities matching the solr query
	 * @throws SolrServiceException
	 *             if a problem occurs while querying Solr
	 */
	ServiceResult getActivitiesBySolrQuery(SolrQuery solrQuery) throws SolrServiceException;

	/**
	 * Retrieves the ids of the activities from Solr with the specified search request and queries the relational database for
	 * the complete records with the given ids.
	 *
	 * @param searchRequest
	 *            the search request object used for building {@link SolrQuery}
	 * @return list of activities matching the search request
	 * @throws SolrServiceException
	 *             if a problem occurs while querying Solr
	 */
	ServiceResult getActivities(AuditSearchRequest searchRequest) throws SolrServiceException;
}
