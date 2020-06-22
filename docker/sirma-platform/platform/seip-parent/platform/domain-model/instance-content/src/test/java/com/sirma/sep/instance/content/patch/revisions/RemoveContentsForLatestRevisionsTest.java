package com.sirma.sep.instance.content.patch.revisions;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

import liquibase.exception.CustomChangeException;

/**
 * Test for {@link RemoveContentsForLatestRevisions}.
 *
 * @author A. Kunchev
 */
public class RemoveContentsForLatestRevisionsTest {

	private static final String QUERY = "SELECT id FROM seip_content WHERE instance_id like '%rlatest' and purpose = 'primaryView'";

	@InjectMocks
	private RemoveContentsForLatestRevisions patch;

	@Mock
	private DataSource datasource;

	@Mock
	private InstanceContentService instanceContentService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void setup() {
		patch = new RemoveContentsForLatestRevisions();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void execute_shouldFailOnQueryExecution() throws SQLException {
		when(datasource.getConnection()).thenThrow(new SQLException());
		try {
			patch.execute(null);
		} catch (Exception e) {
			if (!CustomChangeException.class.isInstance(e.getCause())) {
				fail("Expected cause of the exception should be CustomChangeException, but it was "
						+ e.getCause().getClass().getSimpleName());
			}
		}
	}

	@Test
	public void execute_noContentsForDelete() throws SQLException, CustomChangeException {
		stubRelationalDBResults(datasource, false);
		patch.execute(null);
		verifyZeroInteractions(instanceContentService);
	}

	@Test
	public void execute_withContentsForDelete() throws SQLException, CustomChangeException {
		stubRelationalDBResults(datasource, true);
		patch.execute(null);
		verify(instanceContentService, times(4)).deleteContent(any(Serializable.class), eq(Content.PRIMARY_VIEW),
				eq(10), eq(TimeUnit.MINUTES));
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
			when(statement.executeQuery(QUERY)).thenReturn(results);
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
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:content-id-1</td>
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:content-id-2</td>
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:content-id-1</td>
	 * 		</tr>
	 * 		<tr align=center>
	 * 			<td>emf:content-id-2</td>
	 * 		</tr>
	 * </table>
	 * </pre>
	 */
	private static void prepareResutls(ResultSet results) throws SQLException {
		when(results.next()).thenReturn(true, true, true, true, false);
		when(results.getString(1)).thenReturn("emf:content-id-1", "emf:content-id-2", "emf:content-id-1",
				"emf:content-id-2");
	}
}
