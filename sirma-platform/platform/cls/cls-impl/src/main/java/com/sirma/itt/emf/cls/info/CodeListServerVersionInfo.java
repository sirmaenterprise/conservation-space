package com.sirma.itt.emf.cls.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides info for the code list's module version.
 *
 * @author Mihail Radkov
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 10)
public class CodeListServerVersionInfo extends AbstractVersionInfo {

	private static final String NAME = "Code List Server";

	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.emf.cls/cls-impl/pom.properties";

	@Override
	public String getModuleDescription() {
		return NAME;
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}

}
