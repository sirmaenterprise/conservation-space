/**
 *
 */
package com.sirma.itt.seip.tenant.db;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatasourceModel;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tenant.context.TenantInfo;

/**
 * Provides common functionality required to implement database tenant provisioning.
 *
 * @author BBonev
 */
@Singleton
public class DbProvisioning {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String POSTGRESQL_PRODUCT_NAME = "PostgreSQL";

	/**
	 * Creates the database.
	 *
	 * @param jdbcURL
	 *            the jdbc url
	 * @param databaseName
	 *            the database name
	 * @param superUserName
	 *            the super user name
	 * @param superUserPassword
	 *            the super user password
	 * @param dbGroupOwner
	 *            the db group owner
	 * @param dbUserName
	 *            the db user name
	 * @param dbUserPassword
	 *            the db user password
	 * @throws RollbackedException
	 *             on error
	 */
	public void createDatabase(String jdbcURL, String databaseName, String superUserName, String superUserPassword,
			String dbGroupOwner, String dbUserName, String dbUserPassword) throws RollbackedException {
		try (Connection c = openConnection(jdbcURL, superUserName, superUserPassword);
				Statement statement = c.createStatement()) {
			// creates custom user for for accessing the database for that
			// tenant
			statement.executeUpdate("CREATE ROLE " + dbUserName // NOSONAR
					+ " WITH LOGIN NOINHERIT NOSUPERUSER NOCREATEDB NOCREATEROLE ENCRYPTED PASSWORD '" + dbUserPassword
					+ "'");
			// add the created user to role with name the tenant id
			statement.executeUpdate("CREATE ROLE " + dbGroupOwner + " INHERIT ROLE " + dbUserName); // NOSONAR

			statement.executeUpdate("CREATE DATABASE " + databaseName + " WITH OWNER " + dbGroupOwner); // NOSONAR
		} catch (SQLException e) {
			throw new RollbackedException("Could not create tenant database due to error", e);
		}
	}

	/**
	 * Check if the database exists.
	 * 
	 * @param jdbcURL
	 *            the jdbc url
	 * @param databaseName
	 *            the database name to check
	 * @param superUserName
	 *            the super user name
	 * @param superUserPassword
	 *            the super user password
	 * @return true if it exists, false otherwise
	 * @throws RollbackedException
	 *             on error
	 */
	public boolean databaseExists(String jdbcURL, String databaseName, String superUserName, String superUserPassword)
			throws RollbackedException {
		try (Connection c = openConnection(jdbcURL, superUserName, superUserPassword)) {
			if (POSTGRESQL_PRODUCT_NAME.equals(c.getMetaData().getDatabaseProductName())) {
				return executeDbExistsQuery(c, databaseName);
			}
			LOGGER.info(
					"Non postgres databases are not supported. It will be assumed that the database doesn't exist.");
		} catch (SQLException e) {
			throw new RollbackedException("Could not retrieve database info due to error", e);
		}
		return false;
	}

	private static boolean executeDbExistsQuery(Connection connection, String databaseName) throws SQLException {
		try (Statement statement = connection.createStatement();
				ResultSet results = statement.executeQuery("SELECT datname FROM pg_database")) {
			while (results.next()) {
				if (databaseName.equals(results.getString(1))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Open JDBC connection using the given address and user/password
	 *
	 * @param jdbcURL
	 *            the jdbc url
	 * @param superUserName
	 *            the super user name
	 * @param superUserPassword
	 *            the super user password
	 * @return the connection
	 * @throws SQLException
	 *             the SQL exception
	 */
	@SuppressWarnings("static-method")
	protected Connection openConnection(String jdbcURL, String superUserName, String superUserPassword)
			throws SQLException {
		return DriverManager.getConnection(jdbcURL, superUserName, superUserPassword);
	}

	/**
	 * Drop database and created rules and users during provisioning. If the database's product name
	 * is postgres, release it's connections first otherwise we wont be able to delete it.
	 *
	 * @param database
	 *            the database
	 * @param dbUserToDrop
	 *            the db user to drop
	 * @param address
	 *            the address
	 * @param superUserName
	 *            the super user name
	 * @param superUserPassword
	 *            the super user password
	 * @throws SQLException
	 *             the SQL exception
	 */
	public void dropDatabase(String database, String dbUserToDrop, URI address, String superUserName,
			String superUserPassword) throws SQLException {
		String jdbcURL = toDbAddressUrl(address);
		try (Connection c = openConnection(jdbcURL, superUserName, superUserPassword)) {
			dropConnections(database, c);
			executeUpdate(c, "DROP DATABASE " + database, "Could not drop tenant database " + database);

			String tenantUser = dbUserToDrop;
			executeUpdate(c, "DROP ROLE " + tenantUser, "Could not drop tenant access user " + tenantUser);

			String groupRoleName = tenantUser + "_group";
			executeUpdate(c, "DROP ROLE " + groupRoleName, "Could not drop tenant group role " + groupRoleName);
		}
	}

	private static void dropConnections(String database, Connection c) throws SQLException {
		if (POSTGRESQL_PRODUCT_NAME.equals(c.getMetaData().getDatabaseProductName())) {
			try (PreparedStatement statement = c.prepareStatement(
					"SELECT pid, pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = ? AND pid <> pg_backend_pid()")) {
				// Revoke connect on the database to prevent future connections.
				executeUpdate(c, "REVOKE CONNECT ON DATABASE " + database + " FROM public",
						"Could not revoke connect on database " + database);
				// Drop all connections to that database except the current one.
				statement.setString(1, database);
				statement.executeQuery();
			}
		}
	}

	private static void executeUpdate(Connection connection, String update, String errorMsg) {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate(update);
		} catch (SQLException e) {
			LOGGER.warn(errorMsg, e.getMessage());
			LOGGER.trace(errorMsg, e);
		}
	}

	/**
	 * Creates the user password for tenant id.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @return the string
	 */
	public static String createUserPasswordForTenantId(TenantInfo tenantInfo) {
		String uuid = UUID.randomUUID().toString();
		return tenantInfo.getTenantId() + "-" + uuid.substring(0, uuid.indexOf('-'));
	}

	/**
	 * Creates the user name from tenant id.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @return the string
	 */
	public static String createUserNameFromTenantId(TenantInfo tenantInfo) {
		return tenantInfo.getTenantId().replaceAll("[\\.-]+", "_");
	}

	/**
	 * Convert the given address to database address uri format.
	 *
	 * @param address
	 *            the address
	 * @return the string
	 */
	public static String toDbAddressUrl(URI address) {
		StringBuilder jdbcURL = new StringBuilder(32);
		jdbcURL.append("jdbc:");
		jdbcURL.append(address.getScheme());
		jdbcURL.append("://");
		jdbcURL.append(address.getHost());
		jdbcURL.append(":");
		jdbcURL.append(address.getPort());
		jdbcURL.append("/");
		return jdbcURL.toString();
	}

	/**
	 * Construct a {@link DatasourceModel} object from parameters passed in the params map.
	 *
	 * @param params
	 *            the parameters
	 * @return the {@link DatasourceModel} model
	 */
	public static DatasourceModel getModel(Map<String, Serializable> params) {
		DatasourceModel model = new DatasourceModel();
		model.setUseJavaContext(Boolean.parseBoolean(params.getOrDefault("use-java-context", true).toString()));
		model.setUseCCM(Boolean.parseBoolean(params.getOrDefault("use-ccm", true).toString()));
		model.setMinPoolSize(Integer.parseInt(params.getOrDefault("min-pool-size", 5).toString()));
		model.setMaxPoolSize(Integer.parseInt(params.getOrDefault("max-pool-size", 100).toString()));
		model.setPoolPrefill(Boolean.parseBoolean(params.getOrDefault("pool-prefill", false).toString()));
		model.setValidConnectionSQL((String) params.getOrDefault("check-valid-connection-sql", "SELECT 1"));
		model.setAllocationRetries(Integer.parseInt(params.getOrDefault("allocation-retry", 1).toString()));
		model.setPreparedStatementCacheSize(
				Integer.parseInt(params.getOrDefault("prepared-statements-cache-size", 32).toString()));
		model.setSharePreparedStatements(
				Boolean.parseBoolean(params.getOrDefault("share-prepared-statements", true).toString()));

		return model;
	}
}
