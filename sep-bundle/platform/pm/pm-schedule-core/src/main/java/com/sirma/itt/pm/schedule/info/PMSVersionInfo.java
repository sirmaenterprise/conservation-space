package com.sirma.itt.pm.schedule.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The {@link VersionInfo} implementation for project schedule module.
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 4)
public class PMSVersionInfo extends AbstractVersionInfo {

	/** The LOCATION of pom settings. */
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.pm/pm-schedule-core/pom.properties";

	@Override
	public String getModuleDescription() {
		return "PM Schedule Module";
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
