package com.sirma.itt.emf.solr.services;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.seip.domain.search.SearchArguments;

/**
 * The Interface SolrConnector.
 */
public interface SolrConnector {

	/**
	 * Executes a remote query over solr with GET method.
	 *
	 * @param query
	 *            the query
	 * @return the solr response with results and all the relevant data.
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	QueryResponse queryWithGet(SolrQuery query) throws SolrClientException;

	/**
	 * Executes a remote query over solr with POST method.
	 *
	 * @param query
	 *            the query
	 * @return the solr response with results and all the relevant data.
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	QueryResponse queryWithPost(SolrQuery query) throws SolrClientException;

	/**
	 * Suggest query executions over solr. Default field is used ( same as suggest component is configured)
	 *
	 * @param query
	 *            the query
	 * @return the solr response with results and all the relevant data.
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	QueryResponse suggest(SearchArguments<Object> query) throws SolrClientException;
}
