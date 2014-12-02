package com.sirma.itt.cmf.config;

import com.sirma.itt.emf.configuration.SystemConfigProvider;

/**
 * Load the java args <code>-Dcmf.config.path</code> provided config. Higher priority is set (101)
 * so loading to be processed as final step after the default system configuration file that is with
 * priority 100.
 * 
 * @author bbanchev
 */
public class CmfSystemConfigProvider extends SystemConfigProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPropertyValue() {
		return System.getProperty("cmf.config.path");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getOrder() {
		return 101;
	}
}
