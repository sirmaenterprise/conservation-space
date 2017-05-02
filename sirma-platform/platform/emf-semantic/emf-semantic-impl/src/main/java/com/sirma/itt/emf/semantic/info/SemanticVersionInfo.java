package com.sirma.itt.emf.semantic.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The {@link SemanticVersionInfo} provides info for version of semantic modules.
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 7)
public class SemanticVersionInfo extends AbstractVersionInfo {

	/** The LOCATION of pom settings. */
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.emf/emf-semantic-impl/pom.properties";

	@Override
	public String getModuleDescription() {
		return "Semantic Module";
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
