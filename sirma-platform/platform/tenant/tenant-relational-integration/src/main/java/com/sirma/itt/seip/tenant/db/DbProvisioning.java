/**
 *
 */
package com.sirma.itt.seip.tenant.db;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tenant.context.TenantInfo;

/**
 * Provides commoon functionality required to implement database tenant provisioning.
 *
 * @author BBonev
 */
@Singleton
public class DbProvisioning {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
	public void createDatabase(String jdbcURL, String databaseName, String superUserName,
			String superUserPassword, String dbGroupOwner, String dbUserName, String dbUserPassword)
					throws RollbackedException {
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
	 * Drop database and created rules and users during provisioning.
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
			executeUpdate(c, "DROP DATABASE " + database, "Could not drop tenant database " + database);

			String tenantUser = dbUserToDrop;
			executeUpdate(c, "DROP ROLE " + tenantUser, "Could not drop tenant access user " + tenantUser);

			String groupRoleName = tenantUser + "_group";
			executeUpdate(c, "DROP ROLE " + groupRoleName, "Could not drop tenant group role " + groupRoleName);
		}
	}

	private static void executeUpdate(Connection connection, String update, String errorMsg) {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate(update);
		} catch (SQLException e) {
			LOGGER.warn(errorMsg, e);
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
}
