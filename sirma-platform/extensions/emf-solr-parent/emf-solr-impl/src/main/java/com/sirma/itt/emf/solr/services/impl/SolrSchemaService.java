package com.sirma.itt.emf.solr.services.impl;

import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.emf.solr.schema.SolrSchemaPatch;
import com.sirma.itt.emf.solr.schema.model.SolrSchemaModel;
import com.sirma.itt.emf.solr.schema.remote.SolrSchemaRequest;
import com.sirma.itt.emf.solr.schema.remote.SolrSchemaResponse;

/**
 * The SolrSchemaService is the default implementation for {@link SolrSchemaPatch}. Requests are dispatched to master
 * server
 *
 * @author bbanchev
 */
public class SolrSchemaService implements SolrSchemaPatch {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SolrSchemaService.class);

	/** The master solr server. */
	@Inject
	private SolrConfiguration solrConfig;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SolrSchemaModel updateModel() {
		try {
			SolrSchemaResponse response = new SolrSchemaRequest(METHOD.GET).process(solrConfig.getSolrMaster());
			SolrSchemaModel currentModel = response.getSolrModel();
			SolrSchemaModel newModel = currentModel;// TODO this should be from semantic
			SolrSchemaModel executable = newModel.diff(currentModel);
			SolrSchemaRequest solrSchemaRequest = new SolrSchemaRequest(executable);
			solrSchemaRequest.process(solrConfig.getSolrMaster());
			return executable;
		} catch (Exception e) {
			LOGGER.error("Solr schema parse failed!", e);
		}
		return null;
	}
}
