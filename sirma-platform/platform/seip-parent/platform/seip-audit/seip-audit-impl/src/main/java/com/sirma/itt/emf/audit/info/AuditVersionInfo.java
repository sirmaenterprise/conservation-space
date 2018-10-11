package com.sirma.itt.emf.audit.info;

import com.sirma.itt.emf.info.AbstractVersionInfo;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The AuditVersionInfo provides info for the current version of audit.
 */
@Extension(target = VersionInfo.TARGET_NAME, order = 9)
public class AuditVersionInfo extends AbstractVersionInfo {

	/** The LOCATION of pom settings. */
	private static final String LOCATION = "/META-INF/maven/com.sirma.itt.emf.audit/seip-audit-impl/pom.properties";

	@Override
	public String getModuleDescription() {
		return "Audit Module";
	}

	@Override
	protected String getLocationPath() {
		return LOCATION;
	}
}
