package com.sirma.itt.emf.solr.schema.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase.StringStream;

import com.sirma.itt.emf.solr.schema.model.SolrSchemaModel;

/**
 * The SolrSchemaRequest is request class, accessing /schema services
 *
 * @author bbanchev
 */
public class SolrSchemaRequest extends SolrRequest<SolrSchemaResponse> {

	/** serialVersionUID. */
	private static final long serialVersionUID = 636755691737752523L;
	private SolrSchemaModel model;

	/**
	 * Instantiates a new solr schema request.
	 *
	 * @param path
	 *            the service path
	 */
	public SolrSchemaRequest(String path) {
		super(METHOD.GET, path);
	}

	/**
	 * Instantiates a new solr schema request.
	 *
	 * @param method
	 *            the method
	 */
	public SolrSchemaRequest(METHOD method) {
		super(method, "/schema");
	}

	/**
	 * Instantiates a new solr schema request.
	 *
	 * @param model
	 *            the solr schema update model
	 */
	public SolrSchemaRequest(SolrSchemaModel model) {
		super(METHOD.POST, "/schema");
		this.model = model;
	}

	@Override
	public SolrParams getParams() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ContentStream> getContentStreams() throws IOException {
		if (model == null) {
			return null;
		}
		StringStream stringStream = new StringStream(model.build(), "application/json");
		Collection<ContentStream> result = new ArrayList<ContentStream>(1);
		result.add(stringStream);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SolrSchemaResponse createResponse(SolrClient server) {
		return new SolrSchemaResponse();
	}

}
