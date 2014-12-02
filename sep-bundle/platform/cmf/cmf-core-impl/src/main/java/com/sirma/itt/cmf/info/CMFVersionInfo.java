package com.sirma.itt.cmf.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The CMFVersionInfo provides info for version of cmf.
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 2)
public class CMFVersionInfo extends AbstractVersionInfo {

	/** The LOCATION of pom settings. */
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.cmf/cmf-core-impl/pom.properties";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getModuleDescription() {
		return "CMF Module";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
