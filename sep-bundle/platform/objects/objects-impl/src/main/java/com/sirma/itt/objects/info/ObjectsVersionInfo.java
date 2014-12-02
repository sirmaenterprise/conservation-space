package com.sirma.itt.objects.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The {@link ObjectsVersionInfo} provides info for version of objects module.
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 5)
public class ObjectsVersionInfo extends AbstractVersionInfo {

	/** The LOCATION of pom settings. */
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.objects/objects-impl/pom.properties";

	@Override
	public String getModuleDescription() {
		return "Objects Module";
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
