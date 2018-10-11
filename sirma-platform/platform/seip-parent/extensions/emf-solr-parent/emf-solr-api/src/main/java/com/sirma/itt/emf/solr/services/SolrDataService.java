package com.sirma.itt.emf.solr.services;

import java.util.Collection;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import com.sirma.itt.emf.solr.exception.SolrClientException;

/**
 * Solr integration service for storing and manipulating data accessible via the provided {@link SolrClient}. The
 * service provides convenient methods for adding or modifying solr data.
 *
 * @author A. Kunchev
 * @author BBonev
 */
public interface SolrDataService {

	/**
	 * Imports data passed as map to specific solr client. The passed map is transformed into {@link SolrInputDocument}
	 * with specific fields and then passed to the client as {@link UpdateRequest} to be processed. The
	 * <code>keys</code> of the map represents the name of the different fields and the values are the values of the
	 * filed. The value may be passed as single value of any type and array of values, if we need to pass multi value
	 * field.
	 *
	 * @param client
	 *            the clent which should process the requests
	 * @param data
	 *            the data which should be imported. The map represents the {@link SolrInputField}s that should be
	 *            imported, where the keys are the names of the fields and the values are the value/s of the field
	 * @return the {@link SolrResponse} from the processed request
	 * @throws SolrClientException
	 *             the solr client exception
	 * @see SolrInputField#setValue(Object, float)
	 */
	SolrResponse addData(SolrClient client, Map<String, Object> data) throws SolrClientException;

	/**
	 * Imports data passed as collection of maps to specific solr client. For each map in the passed collection is build
	 * new {@link SolrInputDocument} with specific fields. After that this documents are processed by specific solr
	 * client as {@link UpdateRequest}. The <code>keys</code> of the maps represents names of the different fields and
	 * the map values are field values. They could be passed as single value of any type or array of values, if the
	 * field is multivalued.
	 *
	 * @param client
	 *            the clent which should process the requests
	 * @param data
	 *            the data which should be imported, passed as collection of maps. Each map represents
	 *            {@link SolrInputField}s that should be imported. The keys of the maps are names of the fields and the
	 *            values are the filed values
	 * @return the {@link SolrResponse} from the processed request
	 * @throws SolrClientException
	 *             the solr client exception on communication
	 */
	SolrResponse addData(SolrClient client, Collection<? extends Map<String, Object>> data) throws SolrClientException;

}
