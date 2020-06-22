package com.sirma.itt.seip.tenant.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DatasourceModel;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tenant.context.TenantInfo;

/**
 * Tests for {@link DbProvisioning}
 *
 * @author BBonev
 */
public class DbProvisioningTest {

	@Mock
	private Connection connection;
	private Supplier<Connection> connectionSupplier;

	private DbProvisioning provisioning = new DbProvisioning() {
		@Override
		protected java.sql.Connection openConnection(String jdbcURL, String superUserName, String superUserPassword)
				throws java.sql.SQLException {
			try {
				return connectionSupplier.get();
			} catch (RuntimeException e) {
				throw new SQLException(e);
			}
		}
	};

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		connectionSupplier = () -> connection;
	}

	@Test
	public void createDatabase() throws Exception {
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);

		provisioning.createDatabase("address", "dbName", "superUser", "superPassword", "groupOwner", "dbuser",
				"dbPassword");

		verify(statement, times(3)).executeUpdate(anyString());
	}

	@Test(expected = RollbackedException.class)
	public void createDatabase_failOpenConnection() throws Exception {
		connectionSupplier = () -> {
			throw new RuntimeException();
		};

		provisioning.createDatabase("address", "dbName", "superUser", "superPassword", "groupOwner", "dbuser",
				"dbPassword");
	}

	@Test(expected = RollbackedException.class)
	public void createDatabase_failCreateStatement() throws Exception {
		when(connection.createStatement()).thenThrow(SQLException.class);

		provisioning.createDatabase("address", "dbName", "superUser", "superPassword", "groupOwner", "dbuser",
				"dbPassword");
	}

	@Test(expected = RollbackedException.class)
	public void createDatabase_failCloseConnection() throws Exception {
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);
		doThrow(SQLException.class).when(connection).close();

		provisioning.createDatabase("address", "dbName", "superUser", "superPassword", "groupOwner", "dbuser",
				"dbPassword");
	}

	@Test(expected = NullPointerException.class)
	public void createDatabase_nullConnection() throws Exception {
		when(connection.createStatement()).thenReturn(null);

		provisioning.createDatabase("address", "dbName", "superUser", "superPassword", "groupOwner", "dbuser",
				"dbPassword");
	}

	@Test(expected = RollbackedException.class)
	public void createDatabase_failExecute() throws Exception {
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);

		when(statement.executeUpdate(anyString())).thenThrow(SQLException.class);

		provisioning.createDatabase("address", "dbName", "superUser", "superPassword", "groupOwner", "dbuser",
				"dbPassword");
	}

	@Test
	public void dropDatabase() throws Exception {
		Statement revokeConnectionsStatement = mock(Statement.class);
		PreparedStatement dropConnectionsStatement = mock(PreparedStatement.class);
		Statement dropDatabaseStatement = mock(Statement.class);
		Statement dropUserRoleStatement = mock(Statement.class);
		Statement dropGroupRoleStatement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(revokeConnectionsStatement,
				dropDatabaseStatement, dropUserRoleStatement, dropGroupRoleStatement);
		when(connection.prepareStatement(anyString())).thenReturn(dropConnectionsStatement);
		mockMetadata("PostgreSQL");

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");

		verify(revokeConnectionsStatement).executeUpdate(anyString());
		verify(dropConnectionsStatement).executeQuery();
		verify(dropDatabaseStatement).executeUpdate(anyString());
		verify(dropUserRoleStatement).executeUpdate(anyString());
		verify(dropGroupRoleStatement).executeUpdate(anyString());
	}

	@Test
	public void should_notReleaseConnections_ifDbProductNotPostgres() throws SQLException {
		Statement dropDatabaseStatement = mock(Statement.class);
		Statement dropUserRoleStatement = mock(Statement.class);
		Statement dropGroupRoleStatement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(dropDatabaseStatement, dropUserRoleStatement,
				dropGroupRoleStatement);
		mockMetadata("db");

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");
		verify(dropDatabaseStatement).executeUpdate(anyString());
		verify(dropUserRoleStatement).executeUpdate(anyString());
		verify(dropGroupRoleStatement).executeUpdate(anyString());
	}

	@Test
	public void should_notCheckIfDatabaseExists_ifDbProductNotPostgres() throws RollbackedException, SQLException {
		mockMetadata("db");
		assertFalse(provisioning.databaseExists("url", "databaseName", "admin", "admin"));
	}
	
	@Test(expected = SQLException.class)
	public void dropDatabase_failOpenConnection() throws Exception {
		connectionSupplier = () -> {
			throw new RuntimeException();
		};

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");
	}

	@Test(expected = SQLException.class)
	public void dropDatabase_failCreateStatement() throws Exception {
		when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
		mockMetadata("PostgreSQL");
		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");
	}

	@Test()
	public void dropDatabase_failExecute() throws Exception {
		Statement revokeConnectionsStatement = mock(Statement.class);
		PreparedStatement dropConnectionsStatement = mock(PreparedStatement.class);
		Statement dropDatabaseStatement = mock(Statement.class);
		Statement dropUserRoleStatement = mock(Statement.class);
		Statement dropGroupRoleStatement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(revokeConnectionsStatement,
				dropDatabaseStatement, dropUserRoleStatement, dropGroupRoleStatement);
		when(connection.prepareStatement(anyString())).thenReturn(dropConnectionsStatement);
		mockMetadata("PostgreSQL");

		when(revokeConnectionsStatement.executeUpdate(anyString())).thenThrow(SQLException.class);

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");

		verify(revokeConnectionsStatement).executeUpdate(anyString());
		verify(dropConnectionsStatement).executeQuery();
		verify(dropDatabaseStatement).executeUpdate(anyString());
		verify(dropUserRoleStatement).executeUpdate(anyString());
		verify(dropGroupRoleStatement).executeUpdate(anyString());
	}

	@Test(expected = SQLException.class)
	public void dropDatabase_failClose() throws Exception {
		Statement revokeConnectionsStatement = mock(Statement.class);
		PreparedStatement dropConnectionsStatement = mock(PreparedStatement.class);
		Statement dropDatabaseStatement = mock(Statement.class);
		Statement dropUserRoleStatement = mock(Statement.class);
		Statement dropGroupRoleStatement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(revokeConnectionsStatement,
				dropDatabaseStatement, dropUserRoleStatement, dropGroupRoleStatement);
		when(connection.prepareStatement(anyString())).thenReturn(dropConnectionsStatement);
		mockMetadata("PostgreSQL");

		doThrow(SQLException.class).when(revokeConnectionsStatement).close();
		doThrow(SQLException.class).when(dropConnectionsStatement).close();
		doThrow(SQLException.class).when(dropDatabaseStatement).close();

		doThrow(SQLException.class).when(connection).close();

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");
	}

	@Test
	public void createUserPasswordForTenantId() throws Exception {
		assertTrue(DbProvisioning.createUserPasswordForTenantId(new TenantInfo("tenant.com")).contains("tenant.com"));
	}

	@Test
	@SuppressWarnings("resource")
	public void should_returnTrue_when_databaseExistsAlready() throws Exception {
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);
		mockMetadata("PostgreSQL");

		ResultSet resultSet = Mockito.mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true, false);
		when(resultSet.getString(1)).thenReturn("databaseName");
		when(statement.executeQuery(Matchers.anyString())).thenReturn(resultSet);

		Assert.assertTrue(provisioning.databaseExists("url", "databaseName", "admin", "admin"));
	}

	@Test
	@SuppressWarnings("resource")
	public void should_returnFalse_when_databaseDoesntExist() throws Exception {
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);
		mockMetadata("PostgreSQL");

		ResultSet resultSet = Mockito.mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true, false);
		when(resultSet.getString(1)).thenReturn("databaseName2");
		when(statement.executeQuery(Matchers.anyString())).thenReturn(resultSet);

		Assert.assertFalse(provisioning.databaseExists("url", "databaseName", "admin", "admin"));
	}

	@Test(expected = RollbackedException.class)
	public void should_throwException_when_databaseExceptionOccurs() throws Exception {
		when(connection.createStatement()).thenThrow(new SQLException());
		mockMetadata("PostgreSQL");
		provisioning.databaseExists("url", "databaseName", "admin", "admin");
	}

	@Test
	public void createUserNameFromTenantId() throws Exception {
		assertEquals("ten_ant_com", DbProvisioning.createUserNameFromTenantId(new TenantInfo("ten-ant.com")));
	}

	@Test
	public void should_getModel() {
		Map<String, Serializable> params = new HashMap<>();
		params.put("use-java-context", false);
		params.put("use-ccm", true);

		DatasourceModel model = DbProvisioning.getModel(params);
		Assert.assertFalse(model.isUseJavaContext());
		Assert.assertTrue(model.isUseCCM());
		Assert.assertEquals(5, model.getMinPoolSize());
	}

	private void mockMetadata(String productName) throws SQLException {
		DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
		when(metaData.getDatabaseProductName()).thenReturn(productName);
		when(connection.getMetaData()).thenReturn(metaData);
	}
}
