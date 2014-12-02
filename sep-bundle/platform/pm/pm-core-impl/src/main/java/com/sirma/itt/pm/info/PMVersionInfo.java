package com.sirma.itt.pm.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The PMVersionInfo provides info for version of pm
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 3)
public class PMVersionInfo extends AbstractVersionInfo {

	/** The LOCATION of pom settings. */
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.pm/pm-core-impl/pom.properties";

	@Override
	public String getModuleDescription() {
		return "PM Module";
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
