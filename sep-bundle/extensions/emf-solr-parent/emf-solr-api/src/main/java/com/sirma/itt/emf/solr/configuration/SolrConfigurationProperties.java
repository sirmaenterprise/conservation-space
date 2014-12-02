/*
 *
 */
package com.sirma.itt.emf.solr.configuration;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * SOLR configuration properties.
 *
 * @author bbanchev
 */
@Documentation("Solr configuration properties.")
public interface SolrConfigurationProperties extends Configuration {

	/** The solr server protocol. */
	@Documentation("Solr server protocol. <b>Default value is: http</b>")
	String SOLR_SERVER_PROTOCOL = "solr.host.protocol";

	/** The solr server host. */
	@Documentation("Solr server ip or name. <b>Default value is: localhost</b>")
	String SOLR_SERVER_HOST = "solr.host.servername";

	/** The solr server port. */
	@Documentation("Solr server port. <b>Default value is: 8983</b>")
	String SOLR_SERVER_PORT = "solr.host.serverport";

	/** The solr server core. */
	@Documentation("Solr server core to use. <b>Default value is: owlim</b>")
	String SOLR_SERVER_CORE = "solr.host.core";

	/** The solr config fl for instances. */
	@Documentation("Solr FL query param for dashlets. Split fields by ','. <b>Default value is: uri,instanceType</b>")
	String SOLR_CONFIG_DASHLETS_ALL_FL = "solr.runtime.dashlet.all.fl";

	/** The solr config fl for instances. */
	@Documentation("Solr FL query param for links. Split fields by ','. <b>Default value is: uri,instanceType,compact_header,mimetype</b>")
	String SOLR_CONFIG_LINKS_FL = "solr.runtime.links.fl";

	@Documentation("Solr filter query for status: <b>Default value is: -status:(DELETED) AND isDeleted:false</b>")
	String SOLR_CONFIG_FQ_STATUS = "solr.runtime.fq.status";

	@Documentation("Holds information about the fts query used in search. Default is: <code>all_text:({0})</code>")
	String FULL_TEXT_SEARCH_QUERY = "solr.search.ftsearch.query";

	@Documentation("Holds information about what to be auto escaped during request parse. Default is: <code>([:\\[\\]])</code>")
	String FULL_TEXT_SEARCH_ESCAPE_REGEX = "solr.search.ftsearch.regex.escape";

	@Documentation("Solr filter query used in basic search on the result: <b>default_header:*</b>")
	String FULL_TEXT_SEARCH_BASIC_FQ = "solr.search.ftsearch.basic.fq";
}
