package com.sirma.itt.emf.semantic.patch;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.semantic.ConnectionFactory;

import liquibase.exception.CustomChangeException;

/**
 * Test for {@link RecoverUsersAndGroupsVersionPatch}.
 *
 * @author A. Kunchev
 */
public class RecoverUsersAndGroupsVersionPatchTest {

	private static final String EXPECTED_QUERY = "PREFIX emf: <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#> "
			+ "DELETE {emf:group-id-1 emf:version \"1.0\".emf:user-id-2 emf:version \"1.0\".emf:user-id-1 emf:version \"1.0\".} "
			+ "INSERT {GRAPH <http://ittruse.ittbg.com/data/enterpriseManagementFramework#> "
			+ "{emf:group-id-1 emf:version \"3.4\".emf:user-id-2 emf:version \"2.105\".emf:user-id-1 emf:version \"1.6\".}} WHERE { }";

	@InjectMocks
	private RecoverUsersAndGroupsVersionPatch patch;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private DataSource dataSource;

	@Mock
	private ConnectionFactory connectionFactory;

	@Before
	public void setUp() throws Exception {
		patch = new RecoverUsersAndGroupsVersionPatch();
		MockitoAnnotations.initMocks(this);

		when(instanceVersionService.getInitialInstanceVersion()).thenReturn("1.0");
	}

	@Test
	public void execute_without_resourcesToRecover() throws SQLException, CustomChangeException {
		stubRelationalDBResults(dataSource, false);
		patch.execute(null);
		verify(connectionFactory, never()).produceConnection();
	}

	@Test
	public void execute_successful_migration() throws Exception {
		stubRelationalDBResults(dataSource, true);
		RepositoryConnection connection = mock(RepositoryConnection.class);
		when(connection.prepareUpdate(eq(QueryLanguage.SPARQL), anyString())).thenReturn(mock(Update.class));
		when(connectionFactory.produceConnection()).thenReturn(connection);
		patch.execute(null);

		// this is so #$%#@, but for some reason, when I try to get it from file, the queries doesn't match
		verify(connection).prepareUpdate(eq(QueryLanguage.SPARQL), eq(EXPECTED_QUERY));
	}

	private static void stubRelationalDBResults(DataSource dataSource, boolean withResources) throws SQLException {
		try (ResultSet results = mock(ResultSet.class);
				Statement statement = mock(Statement.class);
				Connection connection = mock(Connection.class)) {

			if (withResources) {
				prepareResutls(results);
			} else {
				when(results.next()).thenReturn(false);
			}
			when(statement.executeQuery(anyString())).thenReturn(results);
			when(connection.createStatement()).thenReturn(statement);
			when(dataSource.getConnection()).thenReturn(connection);
		}
	}

	/**
	 * Prepares results set like this:
	 *
	 * <pre>
	 * <table border=1>
	 * 		<tr align=center>
	 * 			<td><b>id</b></td>
	 * 			<td><b>version</b></td>
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:user-id-1</td>
	 * 			<td>100006</td>
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:user-id-2</td>
	 * 			<td>200105</td>
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:group-id-1</td>
	 * 			<td>300004</td>
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:group-id-2</td>
	 * 			<td>100000</td>
	 * 		</tr>
	 * </table>
	 * </pre>
	 */
	private static void prepareResutls(ResultSet results) throws SQLException {
		when(results.next()).thenReturn(true, true, true, true, false);
		when(results.getString(1)).thenReturn("emf:user-id-1", "emf:user-id-2", "emf:group-id-1", "emf:group-id-2");
		when(results.getInt(2)).thenReturn(100006, 200105, 300004, 100000);
	}

}
