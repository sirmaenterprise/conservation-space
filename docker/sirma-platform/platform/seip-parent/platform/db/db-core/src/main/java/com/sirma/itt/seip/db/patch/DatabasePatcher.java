package com.sirma.itt.seip.db.patch;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.util.NetUtil;

/**
 * Executes the database patches at provided data source using Liquibase library.
 *
 * @author BBonev
 */
public class DatabasePatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String HOST_NAME;
	private static final String HOST_ADDRESS;
	private static final String HOST_DESCRIPTION;

	static {
		try {
			// this is used by liqubase to determine the host name and address used when specifying who locked the patched database
			// we are using it to check if the lock was not left behind previous start of the current node or failed to unlock due to
			// some other reason
			HOST_NAME = NetUtil.getLocalHostName();
			HOST_ADDRESS = NetUtil.getLocalHostAddress();
			HOST_DESCRIPTION = System.getProperty("liquibase.hostDescription") == null ?
					null :
					"#" + System.getProperty("liquibase.hostDescription");
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Instantiates a new database patcher.
	 */
	private DatabasePatcher() {
		// utility class
	}

	/**
	 * Run patch against the given {@link DataSource}
	 *
	 * @param patch
	 *            the patch
	 * @param dataSource
	 *            the data source
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	public static void runPatch(DbPatch patch, DataSource dataSource) throws RollbackedException {
		patchDatabase(dataSource, Collections.singletonList(patch));
	}

	/**
	 * Patch database using the given data source and patch scripts
	 *
	 * @param <D>
	 *            the generic type
	 * @param dataSource
	 *            the data source
	 * @param patches
	 *            the patches
	 * @throws RollbackedException
	 *             on any patch error
	 */
	public static <D extends DbPatch> void patchDatabase(DataSource dataSource, Iterable<D> patches)
			throws RollbackedException {
		if (!patches.iterator().hasNext()) {
			// no patches to execute
			return;
		}

		Database database = null;
		try {
			DatabaseConnection dbConnection = new JdbcConnection(dataSource.getConnection());
			database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConnection);

			if (isLockedByCurrentNode(database)) {
				LOGGER.info("Current database is locked by previous failed patch operation. Forcing unlock!");
				forceUnlockDatabase(database);
			}
			executePatches(patches, database);
		} catch (Exception e) {
			throw new RollbackedException(e);
		} finally {
			if (database != null) {
				try {
					database.close();
				} catch (DatabaseException e) {
					LOGGER.warn("Problem during database connection after patching", e);
				}
			}
		}
	}

	private static void forceUnlockDatabase(Database database) throws LockException, DatabaseException {
		LockService lockService = LockServiceFactory.getInstance().getLockService(database);
		lockService.forceReleaseLock();
	}

	private static boolean isLockedByCurrentNode(Database database) {
		LockService lockService = LockServiceFactory.getInstance().getLockService(database);
		DatabaseChangeLogLock[] locks = new DatabaseChangeLogLock[0];
		try {
			locks = lockService.listLocks();
		} catch (LockException e) {
			LOGGER.warn("Could not list available locks due to: {}", e.getMessage());
		}
		return Arrays.stream(locks)
				.filter(lock -> lock.getLockGranted() != null && lock.getLockedBy() != null)
				.map(DatabaseChangeLogLock::getLockedBy)
				.anyMatch(DatabasePatcher::isCurrentNode);
	}

	private static boolean isCurrentNode(String lockedBy) {
		if (HOST_DESCRIPTION != null) {
			return lockedBy.contains(HOST_DESCRIPTION);
		}
		return lockedBy.contains(HOST_NAME) || lockedBy.contains(HOST_ADDRESS);
	}

	private static <D extends DbPatch> void executePatches(Iterable<D> patches, Database database) throws LiquibaseException {
		Liquibase base = new Liquibase(DBSchemaPatchResourceAccessor.FICTIVE_CHANGELOG, new DBSchemaPatchResourceAccessor(patches), database);
		try {
			base.update((String) null);
		} catch (LockException e) {
			LOGGER.warn("{}. Forcing unlock and will try again", e.getMessage());
			forceUnlockDatabase(database);
			base.update((String) null);
		}
	}
}
