package com.sirma.itt.seip.db.patch;

import java.lang.invoke.MethodHandles;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

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
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class PatchDbServiceImpl implements PatchService {

	public static final String SERVICE_NAME = "PatchDbService";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DatabaseConfiguration databaseConfiguration;

	@Inject
	@ExtensionPoint(DbSchemaPatch.TARGET_NAME)
	private Iterable<DbSchemaPatch> schemaPatches;

	@Inject
	@ExtensionPoint(DbDataPatch.TARGET_NAME)
	private Iterable<DbDataPatch> dataPatches;

	/**
	 * Executes the patching algorithm using the liquibase library.
	 *
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	@Override
	public void patchSchema() throws RollbackedException {
		LOGGER.info("Starting database patch system...");
		TimeTracker tracker = TimeTracker.createAndStart();

		DatabasePatcher.patchDatabase(databaseConfiguration.getDataSource(), schemaPatches);

		LOGGER.info("Database update complete in " + tracker.stop() + " ms");
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
		TimeTracker tracker = TimeTracker.createAndStart();

		DatabasePatcher.patchDatabase(databaseConfiguration.getDataSource(), dataPatches);

		LOGGER.info("Data update complete in " + tracker.stop() + " ms");
	}
}
