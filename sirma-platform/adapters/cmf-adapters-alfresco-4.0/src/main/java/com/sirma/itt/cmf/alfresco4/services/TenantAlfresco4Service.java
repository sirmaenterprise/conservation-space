package com.sirma.itt.cmf.alfresco4.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSTenantAdapterService;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;

/**
 * Default implementation for the service to fetch enabled containers.<br>
 * REVIEW: think this could be moved to generic adapters module if any.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TenantAlfresco4Service implements DMSTenantAdapterService, AlfrescoCommunicationConstants {
	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getEmfContainers() throws DMSException {
		JSONObject request = new JSONObject();
		Set<String> results = null;
		try {

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String callWebScript = restClient.request(ServiceURIRegistry.CMF_SEARCH_CASE_CONTAINERS, createMethod);
			if (callWebScript == null) {
				return Collections.emptySet();
			}
			JSONObject result = new JSONObject(callWebScript);
			JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
			results = new HashSet<String>(nodes.length());
			for (int i = 0; i < nodes.length(); i++) {
				JSONObject jsonObject = (JSONObject) nodes.get(i);
				// we look for containers only
				String containerId = null;
				if (jsonObject.has(KEY_SITE_ID)) {
					containerId = jsonObject.getString(KEY_SITE_ID);
					results.add(containerId);
				}
			}
			return results;
		} catch (DMSClientException e) {
			throw new DMSException(AlfrescoErrorReader.parse(e));
		} catch (Exception e) {
			throw new DMSException("EMF containers retreivement failed!", e);
		}
	}

}
