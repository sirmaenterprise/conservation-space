package com.sirma.itt.seip.tenant.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
		Statement statement1 = mock(Statement.class);
		Statement statement2 = mock(Statement.class);
		Statement statement3 = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement1, statement2, statement3);

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");

		verify(statement1).executeUpdate(anyString());
		verify(statement2).executeUpdate(anyString());
		verify(statement3).executeUpdate(anyString());
	}

	@Test(expected = SQLException.class)
	public void dropDatabase_failOpenConnection() throws Exception {
		connectionSupplier = () -> {
			throw new RuntimeException();
		};

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");
	}

	@Test
	public void dropDatabase_failCreateStatement() throws Exception {
		when(connection.createStatement()).thenThrow(SQLException.class);

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");
	}

	@Test
	public void dropDatabase_failExecute() throws Exception {
		Statement statement1 = mock(Statement.class);
		Statement statement2 = mock(Statement.class);
		Statement statement3 = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement1, statement2, statement3);

		when(statement1.executeUpdate(anyString())).thenThrow(SQLException.class);
		when(statement2.executeUpdate(anyString())).thenThrow(SQLException.class);
		when(statement3.executeUpdate(anyString())).thenThrow(SQLException.class);

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");

		verify(statement1).executeUpdate(anyString());
		verify(statement2).executeUpdate(anyString());
		verify(statement3).executeUpdate(anyString());
	}

	@Test(expected = SQLException.class)
	public void dropDatabase_failClose() throws Exception {
		Statement statement1 = mock(Statement.class);
		Statement statement2 = mock(Statement.class);
		Statement statement3 = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement1, statement2, statement3);

		doThrow(SQLException.class).when(statement1).close();
		doThrow(SQLException.class).when(statement2).close();
		doThrow(SQLException.class).when(statement3).close();

		doThrow(SQLException.class).when(connection).close();

		provisioning.dropDatabase("database", "user", URI.create("localhost"), "superUser", "superPassword");
	}

	@Test
	public void createUserPasswordForTenantId() throws Exception {
		assertTrue(DbProvisioning.createUserPasswordForTenantId(new TenantInfo("tenant.com")).contains("tenant.com"));
	}

	@Test
	public void createUserNameFromTenantId() throws Exception {
		assertEquals("ten_ant_com", DbProvisioning.createUserNameFromTenantId(new TenantInfo("ten-ant.com")));
	}
}
