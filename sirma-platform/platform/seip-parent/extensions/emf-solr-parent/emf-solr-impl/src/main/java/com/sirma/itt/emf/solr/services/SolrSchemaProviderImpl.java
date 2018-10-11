package com.sirma.itt.emf.solr.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.solr.schema.SolrSchemaPatch;
import com.sirma.itt.emf.solr.schema.model.SolrSchemaModel;
import com.sirma.itt.emf.solr.services.impl.SolrSchemaService;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * @author bbanchev
 */
@ApplicationScoped
public class SolrSchemaProviderImpl implements SolrSchemaPatch {

	/** The solr schema service. */
	@Inject
	SolrSchemaService solrSchemaService;

	@Override
	@Startup(async = true, phase = StartupPhase.AFTER_APP_START)
	public SolrSchemaModel updateModel() {
		// SolrSchemaModel currentModel = solrSchemaService.updateModel();
		// return currentModel;
		return null;
	}

}
