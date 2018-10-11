package com.sirma.itt.emf.semantic.patch;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.sql.DataSource;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.domain.util.VersionUtil;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Restores back the version of the users and groups. The correct version is retrieved by archive table in relational DB
 * and then transfered in the semantic. The patch first retrieves max version for all users and groups that have at
 * least one version. Then builds semantic query for delete(for old statements) and inserts the new data.
 *
 * @author A. Kunchev
 */
public class RecoverUsersAndGroupsVersionPatch extends UpdateSemanticTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String GET_RESOURCES_MAX_VERSION = "select distinct targetid, max(majorversion * 100000 + minorversion) from emf_archivedentity  where definitionid in ('userDefinition', 'groupDefinition') group by targetid;";

	private static final String QUERY_PREFIXES = "PREFIX emf: <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#> ";

	private static final String EMPTY_TRIPLET = "{0} emf:version \"{1}\".";

	private DataSource datasource;

	private InstanceVersionService instanceVersionService;

	@Override
	public void setUp() throws SetupException {
		super.setUp();
		BeanManager manager = CDI.getCachedBeanManager();
		AnnotationLiteral<Default> annotationLiteral = CDI.getDefaultLiteral();
		datasource = CDI.instantiateBean(DatabaseConfiguration.class, manager, annotationLiteral).getDataSource();
		instanceVersionService = CDI.instantiateBean(InstanceVersionService.class, manager, annotationLiteral);
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		Map<String, String> resourcesWithVersions = getResourcesWithVersions();

		if (resourcesWithVersions.isEmpty()) {
			LOGGER.info("There are no resources with versions. The patch will do nothing.");
			return;
		}

		String migrationQuery = buildMigrationQuery(resourcesWithVersions);
		RepositoryConnection connection = connectionFactory.produceConnection();
		try {
			Update insertData = connection.prepareUpdate(QueryLanguage.SPARQL, migrationQuery);
			insertData.execute();
		} catch (RepositoryException | MalformedQueryException | UpdateExecutionException e) {
			throw new CustomChangeException("Could not insert data.", e);
		} finally {
			connectionFactory.disposeConnection(connection);
		}
	}

	private Map<String, String> getResourcesWithVersions() throws CustomChangeException {
		try (Connection connection = datasource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(GET_RESOURCES_MAX_VERSION)) {

			Map<String, String> resources = new HashMap<>();
			String initialVersion = instanceVersionService.getInitialInstanceVersion();
			while (resultSet.next()) {
				String resourceId = resultSet.getString(1);
				String correctVersion = getVersion(resultSet);
				// we don't need to update, if there is a only one version
				if (!initialVersion.equals(String.valueOf(correctVersion))) {
					resources.put(resourceId, String.valueOf(correctVersion));
				}
			}

			return resources;
		} catch (SQLException e) {
			throw new CustomChangeException("Could not fetch resources max versions.", e);
		}
	}

	/**
	 * Calculates the correct version from the instance. This is needed, because the query returns the version in weird
	 * format, which is needed so that we could get the maximal instance version correctly.
	 */
	private static String getVersion(ResultSet resultSet) throws SQLException {
		int version = resultSet.getInt(2);
		String versionString = String.valueOf(version);
		String minorVersionPart = versionString.substring(versionString.length() - 5);
		Integer minorVersion = Integer.valueOf(minorVersionPart);
		int majorVersion = (version - minorVersion) / 100000;
		return VersionUtil.combine(majorVersion, minorVersion);
	}

	private String buildMigrationQuery(Map<String, String> resources) {
		String initialVersion = instanceVersionService.getInitialInstanceVersion();
		StringBuilder query = new StringBuilder(QUERY_PREFIXES);

		// build delete
		query.append("DELETE {");
		for (String key : resources.keySet()) {
			query.append(MessageFormat.format(EMPTY_TRIPLET, key, initialVersion));
		}

		// build insert
		query.append("} INSERT {").append("GRAPH <").append(EMF.DATA_CONTEXT.stringValue()).append("#> {");
		for (Entry<String, String> entry : resources.entrySet()) {
			query.append(MessageFormat.format(EMPTY_TRIPLET, entry.getKey(), entry.getValue()));
		}

		query.append("}} WHERE { }");
		return query.toString();
	}

	@Override
	public String getConfirmationMessage() {
		return "Versions for the users and groups are restored !";
	}

}
