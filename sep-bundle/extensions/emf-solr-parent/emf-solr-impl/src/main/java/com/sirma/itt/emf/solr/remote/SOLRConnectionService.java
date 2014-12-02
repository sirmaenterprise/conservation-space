package com.sirma.itt.emf.solr.remote;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.solr.configuration.SolrConfigurationProperties;

/**
 * The SOLRConnectionService provides solr server connection to single solr server or zookeeper.
 */
@ApplicationScoped
public class SOLRConnectionService {

	/** The protocol. */
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_SERVER_PROTOCOL, defaultValue = "http")
	private String protocol;
	/** The host. */
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_SERVER_HOST, defaultValue = "localhost")
	private String host;

	/** The port. */
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_SERVER_PORT, defaultValue = "8983")
	private int port;
	/** The port. */
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_SERVER_CORE, defaultValue = "owlim")
	private String core;

	/**
	 * Gets the solr server.
	 *
	 * @return the solr server
	 */
	@Produces
	@ApplicationScoped
	public SolrServer getSolrServer() {
		return new HttpSolrServer(new StringBuilder(protocol).append("://").append(host)
				.append(":").append(port).append("/solr/").append(core).toString());
	}
}
