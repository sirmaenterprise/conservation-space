package com.sirma.itt.emf.info;

import com.sirma.itt.seip.plugin.Extension;

/**
 * The EMF core info provider
 *
 * @author bbanchev
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 1)
public class EMFCoreVersionInfo extends AbstractVersionInfo {
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.emf/emf-core-impl/pom.properties";

	@Override
	public String getModuleDescription() {
		return "EMF Core";
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
