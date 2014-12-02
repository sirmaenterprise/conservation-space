package com.sirma.itt.emf.patch;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Patch service that initialize database or upgrade it if needed.
 * <p>
 * <b>NOTE 1:</b> In order to work properly the hibernate property
 * <code>hibernate.hbm2ddl.auto</code> should not be set at all in the configuration.
 * <p>
 * <b>NOTE 2:</b> The application have to ensure that this class is executed before any other have
 * accessed the database.
 * 
 * @author BBonev
 */
@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class PatchDbService {

	public static final String SERVICE_NAME = "PatchDbService";

	private static final Logger LOGGER = Logger.getLogger(PatchDbService.class);

	/** The data source. */
	@Resource(name = DbDao.DATASOURCE_NAME)
	private DataSource dataSource;

	@Inject
	@ExtensionPoint(DBSchemaPatch.TARGET_NAME)
	private Iterable<DBSchemaPatch> patches;

	/**
	 * Executes the patching algorithm using the liquibase library.
	 * 
	 * @throws Exception
	 *             when patching fails
	 */
	@PostConstruct
	public void patchDatabase() throws Exception {
		LOGGER.info("Starting database patch system...");

		TimeTracker tracker = TimeTracker.createAndStart();

		DatabaseConnection dbConnection = new JdbcConnection(dataSource.getConnection());
		Liquibase base = new Liquibase(DBSchemaPatchResourceAccessor.FICTIVE_CHANGELOG,
				new DBSchemaPatchResourceAccessor(patches), dbConnection);
		base.update(null);

		LOGGER.info("Database update complete in " + tracker.stopInSeconds() + " s");
	}
}
