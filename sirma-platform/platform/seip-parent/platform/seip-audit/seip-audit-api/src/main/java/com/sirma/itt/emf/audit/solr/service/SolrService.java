package com.sirma.itt.emf.audit.solr.service;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;

import com.sirma.itt.emf.audit.solr.query.SolrResult;

/**
 * Used for interaction with the solr server.
 *
 * @author Nikolay Velkov
 */
public interface SolrService {

	String DATA_IMPORT_STATUS_BUSY = "busy";

	/**
	 * Returns RDB IDs from the given solr query. This method makes a http request to solr with the query and transforms
	 * the retrieved solar documents to an array of DB IDs later to be retrieved from the actual database.
	 *
	 * @param solrQuery
	 *            the solr query parameters
	 * @return a {@link SolrResult} wrapping the retrieved IDs and the total amount of results
	 * @throws SolrServiceException
	 *             if something went wrong with the retrieval of the IDs
	 */
	SolrResult getIDsFromQuery(SolrQuery solrQuery) throws SolrServiceException;

	/**
	 * Delete records from solr by identifier.
	 *
	 * @param ids
	 *            list with ids to be deleted
	 * @throws SolrServiceException
	 *             if something went wrong with the deletion of the ids
	 */
	void deleteById(List<String> ids) throws SolrServiceException;

	/**
	 * Run solr data import functionality.
	 *
	 * @param cleanImport
	 *            if true imports all records. If false performs delta import. Use false if not sure
	 * @return true, if successful. False if there is a problem or data import is currently running
	 */
	boolean dataImport(boolean cleanImport);
}
