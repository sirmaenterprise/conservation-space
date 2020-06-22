package com.sirma.sep.export;

import java.net.URI;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.rest.client.URIBuilderWrapper;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Contains logic for generating {@link URI} for specific instance. The generated {@link URI} represents link which
 * could be used to open the specified instance in print mode. To generated link is applied security token for
 * authentication in the system.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class ExportURIBuilderImpl implements ExportURIBuilder {

	private static final String PATH = "/#/idoc/";

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SecurityTokensManager securityTokensManager;

	@Override
	public URI generateURIForTabs(Collection<String> tabs, String instanceId, String token) {
		if (StringUtils.isBlank(instanceId)) {
			throw new IllegalArgumentException("InstanceId is required argument!");
		}

		String jwtToken = token;
		if (StringUtils.isBlank(jwtToken)) {
			jwtToken = getCurrentJwtToken();
		}

		URIBuilderWrapper fullURI = new URIBuilderWrapper("/", instanceId);
		tabs.forEach(tabId -> fullURI.addParameter("tab", tabId));
		fullURI.addParameter("jwt", jwtToken).addParameter("mode", "print");
		// use second builder since
		return URIBuilderWrapper.createURIByPaths(systemConfiguration.getUi2Url().get(), PATH, fullURI.build());
	}

	@Override
	public String getCurrentJwtToken() {
		return securityTokensManager.generate(securityContext.getAuthenticated());
	}

}