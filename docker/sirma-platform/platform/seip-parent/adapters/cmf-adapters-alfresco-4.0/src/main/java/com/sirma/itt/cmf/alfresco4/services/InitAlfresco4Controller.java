package com.sirma.itt.cmf.alfresco4.services;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.seip.adapters.remote.RESTClient;

/**
 * The {@link InitAlfresco4Controller} is initialization tool for alfresco.
 */
@ApplicationScoped
public class InitAlfresco4Controller implements AlfrescoCommunicationConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RESTClient restClient;

	/**
	 * The InitConfiguration <br>
	 * Supported values are:
	 * <ul>
	 * <li>storeLocation = string path to root directory containing definitions.</li>
	 * <li>pm.enabled = true/false</li>
	 * <li>dom.enabled = true/false</li>
	 * </ul>
	 */
	public static class InitConfiguration {
		private String definitionsLocation = "";
		private boolean failOnMissing = true;
		private String siteId = "seip";

		/**
		 * Getter method for definitionStorage.
		 *
		 * @return the definitionStorage
		 */
		public String getDefinitionsLocation() {
			return definitionsLocation;
		}

		/**
		 * Setter method for definitionStorage.
		 *
		 * @param definitionsLocation
		 *            the definitionsLocation to set
		 */
		public void setDefinitionsLocation(String definitionsLocation) {
			this.definitionsLocation = definitionsLocation;
		}

		/**
		 * Setter method for siteId.
		 *
		 * @param siteId
		 *            the siteId to set
		 */
		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		/**
		 * Gets the site id.
		 *
		 * @return the site id
		 */
		public String getSiteId() {
			return siteId;
		}

		/**
		 * Setter method for fail on missing definitions but should be uploaded.
		 *
		 * @param failOnMissing
		 *            the failOnMissing to set
		 */
		public void setFailOnMissing(boolean failOnMissing) {
			this.failOnMissing = failOnMissing;
		}

		/**
		 * Check if {@link #failOnMissing} is set
		 *
		 * @return true if operation should fail on missing data
		 */
		public boolean isFailOnMissing() {
			return failOnMissing;
		}
	}

	/**
	 * Initialize the alfresco storage and optionally upload the selected definition types.
	 *
	 * @param configuration
	 *            the configuration properties.
	 * @param taskThread
	 *            the task thread
	 * @throws Exception
	 *             the exception
	 */
	public void initialize(InitConfiguration configuration) throws Exception { // NOSONAR
		LOGGER.info("Begin Init");
		String siteId = configuration.getSiteId();
		JSONObject request = new JSONObject();
		request.put("sites", siteId);

		HttpMethod createdMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
		String callWebScript = restClient.request("/pm/init", createdMethod);
		LOGGER.info("Finish structure init {}", callWebScript);
	}
}