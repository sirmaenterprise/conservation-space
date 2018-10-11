package com.sirma.itt.seip.db.patch;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;

/**
 * Executes the database patches at provided data source using Liquibase library.
 *
 * @author BBonev
 */
public class DatabasePatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
		patchDatabase(dataSource, Arrays.asList(patch));
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
			Liquibase base = new Liquibase(DBSchemaPatchResourceAccessor.FICTIVE_CHANGELOG,
					new DBSchemaPatchResourceAccessor(patches), database);
			base.update((String) null);
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
}
