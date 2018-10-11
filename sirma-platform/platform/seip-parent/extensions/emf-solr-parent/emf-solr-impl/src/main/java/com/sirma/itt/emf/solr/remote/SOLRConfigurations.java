package com.sirma.itt.emf.solr.remote;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * SolrConfiguration implementation that provides {@link SolrClient}s to access Solr server or Zookeeper.
 *
 * @author BBonev
 */
@Singleton
public class SOLRConfigurations implements SolrConfiguration {

	@ConfigurationPropertyDefinition(defaultValue = "http", sensitive = true, label = "Solr server protocol.")
	private static final String SOLR_SERVER_PROTOCOL = "solr.host.protocol";
	@ConfigurationPropertyDefinition(defaultValue = "localhost", sensitive = true, label = "Solr server ip or name.")
	private static final String SOLR_SERVER_HOST = "solr.host.servername";
	@ConfigurationPropertyDefinition(defaultValue = "8983", sensitive = true, type = Integer.class, label = "Solr server port.")
	private static final String SOLR_SERVER_PORT = "solr.host.serverport";
	@ConfigurationPropertyDefinition(defaultValue = "owlim", sensitive = true, label = "Solr server core to use.")
	private static final String SOLR_SERVER_CORE = "solr.host.core";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Solr server master url. This is the full url as 'http://localhost:8983/solr/ftsearch'.")
	private static final String SOLR_SERVER_MASTER_URL = "solr.host.masterurl";
	@ConfigurationPropertyDefinition(defaultValue = "5000", sensitive = true, type = Integer.class, label = "Solr socket timeout. If its set \"-1\" will disable setting the timeout.")
	private static final String SOLR_SOCKET_TIMEOUT = "solr.socket.timeout";

	@ConfigurationGroupDefinition(properties = { SOLR_SERVER_PROTOCOL, SOLR_SERVER_HOST, SOLR_SERVER_PORT,
			SOLR_SERVER_CORE, SOLR_SOCKET_TIMEOUT }, type = SolrClient.class)
	private static final String SOLR_CLIENT = "solr.client";

	@ConfigurationGroupDefinition(properties = { SOLR_SERVER_MASTER_URL, SOLR_SERVER_PROTOCOL, SOLR_SERVER_HOST,
			SOLR_SERVER_PORT, SOLR_SERVER_CORE }, type = SolrClient.class)
	private static final String SOLR_MASTER = "solr.master";

	@ConfigurationGroupDefinition(properties = { SOLR_SERVER_PROTOCOL, SOLR_SERVER_HOST,
			SOLR_SERVER_PORT, SOLR_SERVER_CORE }, type = String.class)
	private static final String SOLR_ADDRESS = "solr.address";

	@Inject
	@Configuration(SOLR_CLIENT)
	private ConfigurationProperty<SolrClient> solrClient;
	@Inject
	@Configuration(SOLR_MASTER)
	private ConfigurationProperty<SolrClient> solrMaster;
	@Inject
	@Configuration(SOLR_SERVER_CORE)
	private ConfigurationProperty<String> solrCore;
	@Inject
	@Configuration(SOLR_ADDRESS)
	private ConfigurationProperty<String> solrAddress;

	/**
	 * Creates the client.
	 *
	 * @param context
	 *            the context
	 * @return the solr client
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	@ConfigurationConverter(SOLR_CLIENT)
	static SolrClient createClient(GroupConverterContext context) {
		HttpSolrClient client = createClientFromConfiguration(context);

		// for some reason if this is not set the average solr query takes 500+ milliseconds
		ConfigurationProperty<Integer> timeout = context.getValue(SOLR_SOCKET_TIMEOUT);
		if (timeout.isSet() && timeout.get().intValue() != -1) {
			client.setSoTimeout(timeout.get().intValue());
		}
		return client;
	}

	/**
	 * Creates the master solr client
	 *
	 * @param context
	 *            the context
	 * @return the solr client
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	@ConfigurationConverter(SOLR_MASTER)
	static SolrClient createMaster(GroupConverterContext context) {
		String masterUrl = context.get(SOLR_SERVER_MASTER_URL);
		HttpSolrClient client;
		if (StringUtils.isBlank(masterUrl)) {
			client = createClientFromConfiguration(context);
		} else {
			client = new HttpSolrClient(masterUrl);
		}
		return client;
	}

	static HttpSolrClient createClientFromConfiguration(GroupConverterContext context) {
		String core = context.get(SOLR_SERVER_CORE);
		return new HttpSolrClient(buildSolrAddress(context) + "/" + core);
	}

	@ConfigurationConverter(SOLR_ADDRESS)
	static String buildSolrAddress(GroupConverterContext context) {
		String protocol = context.get(SOLR_SERVER_PROTOCOL);
		String host = context.get(SOLR_SERVER_HOST);
		Integer port = context.get(SOLR_SERVER_PORT);
		return new StringBuilder()
				.append(protocol)
					.append("://")
					.append(host)
					.append(":")
					.append(port)
					.append("/solr")
					.toString();
	}

	/**
	 * Gets the solr server.
	 *
	 * @return the solr server
	 */
	@Override
	public SolrClient getSolrServer() {
		return solrClient.get();
	}

	/**
	 * Gets the solr admin server.
	 *
	 * @return the solr admin server
	 */
	@Override
	public SolrClient getSolrMaster() {
		if (solrMaster.isSet()) {
			return solrMaster.get();
		}
		return solrClient.get();
	}

	@Override
	public String getMainSolrCore() {
		return solrCore.get();
	}

	@Override
	public String getSolrProtocolConfiguration() {
		return SOLR_SERVER_PROTOCOL;
	}

	@Override
	public String getSolrHostConfiguration() {
		return SOLR_SERVER_HOST;
	}

	@Override
	public String getSolrPortConfiguration() {
		return SOLR_SERVER_PORT;
	}

	@Override
	public String getSolrCoreConfiguration() {
		return SOLR_SERVER_CORE;
	}

	@Override
	public String getSolrAddress() {
		return solrAddress.get();
	}

}
