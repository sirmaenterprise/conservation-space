package com.sirma.itt.seip.db.patch;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * The CoreDbPatchService is responsible to patch core db.
 *
 * @author bbanchev
 */
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class CoreDbPatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(DbCoreSchemaPatch.TARGET_NAME)
	private Plugins<DbCoreSchemaPatch> coreDbPatches;

	/**
	 * Executes the patching algorithm using the liquibase library.
	 *
	 * @param databaseConfiguration
	 *            the database configuration
	 * @throws RollbackedException
	 *             when patching fails
	 */
	@RunAsSystem(protectCurrentTenant = false)
	@Startup(name = "CorePatchDbService", phase = StartupPhase.DEPLOYMENT, order = 0, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void patchDatabase(DatabaseConfiguration databaseConfiguration) throws RollbackedException {
		LOGGER.info("Starting CORE database patch system...");
		TimeTracker tracker = TimeTracker.createAndStart();

		DataSource dataSource = databaseConfiguration.getCoreDataSource();
		for (DbCoreSchemaPatch coreSchemaPatch : coreDbPatches) {
			LOGGER.info("Running patch file: {}", coreSchemaPatch.getPath());
			DatabasePatcher.runPatch(coreSchemaPatch, dataSource);
		}

		LOGGER.info("CORE database update complete in {} ms", tracker.stop());
	}
}
