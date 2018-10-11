package com.sirma.itt.seip.controlers;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.sirma.itt.seip.CMFToolBaseFrame;
import com.sirma.itt.seip.PropertyConfigsWrapper;
import com.sirma.itt.seip.alfresco4.remote.AbstractRESTClient;

/**
 * The SolrController to execute solr requests
 */
public class SolrController {
	/** The LOGGER. */
	protected static final Logger LOGGER = Logger.getLogger(SolrController.class);

	/**
	 * Execute solr call to the specified service configured in 'solr.requests'.Single request is separated with | to
	 * the others. Request uri is split from the body by #
	 *
	 * @param base
	 *            is the ui to display messages at
	 * @param host
	 *            the solr host
	 * @throws Exception
	 *             on any error
	 */
	public void executeSolrCall(CMFToolBaseFrame base, String host) throws Exception {
		PropertyConfigsWrapper configsWrapper = PropertyConfigsWrapper.getInstance();

		AbstractRESTClient solrClient = new AbstractRESTClient();
		solrClient.setUseDmsServiceBase(false);
		String requests = configsWrapper.getProperty("solr.requests");
		String[] requestsRaw = requests.split("\\|");
		for (String req : requestsRaw) {
			try {

				String[] reqParts = req.split("#");
				// split host,port,base service
				String[] hostAndPort = host.split(":");
				String port = hostAndPort[1].substring(0, hostAndPort[1].indexOf("/"));
				String service = hostAndPort[1].substring(hostAndPort[1].indexOf("/"));
				solrClient.setDefaultCredentials(hostAndPort[0], Integer.parseInt(port), "", "");
				HttpMethod createdMethod = solrClient.createMethod(new PostMethod(), reqParts[1], false);
				String invokeWithResponse = solrClient.invokeWithResponse(service + reqParts[0], createdMethod);
				LOGGER.info("Solr response " + invokeWithResponse);
			} catch (Exception e) {
				base.log(e);
			}
		}

	}
}
