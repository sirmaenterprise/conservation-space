package com.sirma.itt.seip.db.patch;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;

/**
 * Executes patches during startup for all tenants.
 * <p>
 * FIXME: remove when add db module refactorings
 *
 * @author BBonev
 */
@Singleton
public class DbPatchTrigger {

	@Inject
	private PatchService patchService;

	/**
	 * Run schema patches for all tenants.
	 *
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 2.1)
	public void runSchemaPatchesForAllTenants() throws RollbackedException {
		patchService.patchSchema();
	}

	/**
	 * Run data patches for all tenants.
	 *
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, async = true)
	public void runDataPatchesForAllTenants() throws RollbackedException {
		patchService.patchData();
	}

}
