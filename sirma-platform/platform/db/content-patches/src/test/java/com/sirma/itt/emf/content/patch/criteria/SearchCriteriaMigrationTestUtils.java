package com.sirma.itt.emf.content.patch.criteria;

import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.sql.DataSource;

import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;

/**
 * Utility methods for the search criteria migration tests.
 * 
 * @author Mihail Radkov
 *
 */
public final class SearchCriteriaMigrationTestUtils {

	/**
	 * Private constructor for utility class.
	 */
	private SearchCriteriaMigrationTestUtils() {
		// No need for publicity.
	}

	public static void mockInstanceContentService(InstanceContentService contentService, String purpose,
			Collection<ContentInfo> contentInfos) {
		when(contentService.getContent(Matchers.anyCollectionOf(Serializable.class), Matchers.eq(purpose)))
				.thenReturn(contentInfos);
	}

	public static void mockDataSource(DataSource datasource) throws SQLException {
		Connection connection = Mockito.mock(Connection.class);
		when(datasource.getConnection()).thenReturn(connection);

		Statement statement = Mockito.mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);

		ResultSet resultSet = Mockito.mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true).thenReturn(false);
		when(resultSet.getString(Matchers.anyInt())).thenReturn("1");

		when(statement.executeQuery(Matchers.anyString())).thenReturn(resultSet);
	}
}
