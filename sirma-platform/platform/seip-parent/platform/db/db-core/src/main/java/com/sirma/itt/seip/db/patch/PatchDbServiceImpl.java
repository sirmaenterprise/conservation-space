package com.sirma.itt.seip.db.patch;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.sql.DataSource;

import com.sirma.itt.seip.plugin.Plugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Patch service that initialize database or upgrade it if needed.
 * <p>
 * <b>NOTE 1:</b> In order to work properly the hibernate property <code>hibernate.hbm2ddl.auto</code> should not be set
 * at all in the configuration.
 * <p>
 * <b>NOTE 2:</b> The application have to ensure that this class is executed before any other have accessed the
 * database.
 * <p>
 *
 * @author BBonev
 */
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class PatchDbServiceImpl implements PatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DatabaseConfiguration databaseConfiguration;

	@Inject
	@ExtensionPoint(DbSchemaPatch.TARGET_NAME)
	private Plugins<DbSchemaPatch> schemaPatches;

	@Inject
	@ExtensionPoint(DbDataPatch.TARGET_NAME)
	private Plugins<DbDataPatch> dataPatches;

	/**
	 * Executes the patching algorithm using the liquibase library.
	 *
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	@Override
	public void patchSchema() throws RollbackedException {
		LOGGER.info("Starting database patch system...");
		runPatches(schemaPatches, databaseConfiguration.getDataSource());
	}

	/**
	 * Run data patches after deployment phase but before users could access the server.
	 *
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	@Override
	public void patchData() throws RollbackedException {
		LOGGER.info("Starting data patches ...");
		runPatches(dataPatches, databaseConfiguration.getDataSource());
	}

	private void runPatches(Iterable<? extends DbPatch> patches, DataSource dataSource) throws RollbackedException {
		TimeTracker tracker = TimeTracker.createAndStart();

		for (DbPatch patch : patches) {
			LOGGER.info("Running patch file: {}", patch.getPath());
			DatabasePatcher.runPatch(patch, dataSource);
		}

		LOGGER.info("Data update complete in {} ms", tracker.stop());
	}
}
