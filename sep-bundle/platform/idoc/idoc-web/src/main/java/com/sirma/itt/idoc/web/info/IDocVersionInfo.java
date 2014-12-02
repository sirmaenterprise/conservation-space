package com.sirma.itt.idoc.web.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The {@link IDocVersionInfo} provides info for version of idoc modules.
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 8)
public class IDocVersionInfo extends AbstractVersionInfo {

	/** The LOCATION of pom settings. */
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.idoc/idoc-web/pom.properties";

	@Override
	public String getModuleDescription() {
		return "IDoc Module";
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
