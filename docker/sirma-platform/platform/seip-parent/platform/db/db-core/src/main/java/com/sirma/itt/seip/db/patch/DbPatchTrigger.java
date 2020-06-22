package com.sirma.itt.seip.db.patch;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantRemove;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Executes patches during startup for all tenants.
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
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 2.1, transactionMode = TransactionMode.NOT_SUPPORTED)
	@OnTenantRemove(order = 0) // run schema patches for deactivated tenant tenant being deleted
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
	@Startup(phase = StartupPhase.BEFORE_APP_START, async = true, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void runDataPatchesForAllTenants() throws RollbackedException {
		patchService.patchData();
	}

}
