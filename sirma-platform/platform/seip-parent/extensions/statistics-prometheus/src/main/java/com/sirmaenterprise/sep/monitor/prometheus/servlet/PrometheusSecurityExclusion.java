package com.sirmaenterprise.sep.monitor.prometheus.servlet;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;

/**
 * Security exclusion to allow accessing the metrics servlet without security.
 *
 * @author BBonev
 */
// TODO: this sould be secured - prometheus supports authentication
@Extension(target = SecurityExclusion.TARGET_NAME, order = 123)
public class PrometheusSecurityExclusion implements SecurityExclusion {

	@Override
	public boolean isForExclusion(String path) {
		return path.equals(PrometheusMetricsServlet.PATH);
	}

}
