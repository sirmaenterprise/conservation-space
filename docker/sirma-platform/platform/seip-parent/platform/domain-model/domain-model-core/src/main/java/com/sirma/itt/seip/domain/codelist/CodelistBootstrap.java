/*
 *
 */
package com.sirma.itt.seip.domain.codelist;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;

/**
 * Forcefully initialize codelists on server startup.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CodelistBootstrap {

	/** The codelist service. */
	@Inject
	private CodelistService codelistService;
	/** The event service. */

	/**
	 * Bootstrap codelists.
	 */
	@Startup
	@RunAsAllTenantAdmins
	public void bootstrapCodelists() {
		codelistService.getCodeValues(0);
	}
}
