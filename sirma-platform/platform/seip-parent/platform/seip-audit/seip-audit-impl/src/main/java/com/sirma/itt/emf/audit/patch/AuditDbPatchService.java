package com.sirma.itt.emf.audit.patch;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.db.patch.DatabasePatcher;
import com.sirma.itt.seip.db.patch.DbAuditPatch;
import com.sirma.itt.seip.db.patch.DbRecentActivitiesPatch;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Patches the DB for BAM. Code is taken from EMF.
 *
 * @author Mihail Radkov
 */
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class AuditDbPatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(DbAuditPatch.TARGET_NAME)
	private Iterable<DbAuditPatch> auditDbPatches;

	@Inject
	@ExtensionPoint(DbRecentActivitiesPatch.TARGET_NAME)
	private Iterable<DbRecentActivitiesPatch> recentActivitiesPatches;

	/**
	 * Patches tenant audit databases.
	 *
	 * @param configuration
	 *            the configuration
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 3.5, async = true, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void patchAuditDatabaseForAllTenants(AuditConfiguration configuration) throws RollbackedException {
		patchAuditDatabase(configuration);
		patchRecentActivities(configuration);
	}

	/**
	 * Patches the system audit database.
	 *
	 * @param configuration
	 *            the configuration
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	@RunAsSystem(protectCurrentTenant = false)
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 3.4, async = true, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void patchAuditDatabaseForSystem(AuditConfiguration configuration) throws RollbackedException {
		patchAuditDatabase(configuration);
	}

	/**
	 * Patch audit database.
	 *
	 * @param configuration
	 *            the configuration
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	public void patchAuditDatabase(AuditConfiguration configuration) throws RollbackedException {
		LOGGER.info("Starting audit database patch..");
		TimeTracker tracker = TimeTracker.createAndStart();

		DatabasePatcher.patchDatabase(configuration.getDataSource(), auditDbPatches);
		LOGGER.info("Audit database patching completed in {} ms", tracker.stop());
	}

	/**
	 * Patch recent activities database.
	 *
	 * @param configuration
	 *            the configuration
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	public void patchRecentActivities(AuditConfiguration configuration) throws RollbackedException {
		LOGGER.info("Starting recent activities audit patch..");
		TimeTracker tracker = TimeTracker.createAndStart();
		DatabasePatcher.patchDatabase(configuration.getDataSource(), recentActivitiesPatches);
		LOGGER.info("recent activities audit patching completed in {} ms", tracker.stop());
	}
}
