package com.sirma.itt.emf.content.patch.criteria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import liquibase.exception.CustomChangeException;

/**
 * Utility class related for search criteria migration.
 * 
 * @author Mihail Radkov
 * @see ContentSearchCriteriaModelPatch
 */
public final class SearchCriteriaMigrationUtil {

	public static final String WIDGET_SELECTOR = ".widget";
	public static final String CONFIG_ATTRIBUTE = "config";

	private SearchCriteriaMigrationUtil() {
		// Utility class...
	}

	/**
	 * Queries the database from the given datasource with the provided query and collects the content of the first
	 * column from the result.
	 * 
	 * @param datasource
	 *            - the given datasource
	 * @param query
	 *            - the provided query for execution
	 * @return - the collected first column. For example it could be table identifiers.
	 * @throws CustomChangeException
	 *             - if the database cannot be queried.
	 */
	public static Collection<String> queryIdentifiers(DataSource datasource, String query)
			throws CustomChangeException {
		Collection<String> ids = new ArrayList<>();
		try (Connection connection = datasource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query)) {
			while (resultSet.next()) {
				ids.add(resultSet.getString(1));
			}
		} catch (SQLException e) {
			throw new CustomChangeException("Failed to query instance IDs!", e);
		}
		return ids;
	}

}
