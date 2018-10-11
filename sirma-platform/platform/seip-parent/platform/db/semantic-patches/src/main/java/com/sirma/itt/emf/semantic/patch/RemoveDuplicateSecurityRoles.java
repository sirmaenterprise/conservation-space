package com.sirma.itt.emf.semantic.patch;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Removes duplicate security roles for instances and assigns the proper roles for the user for these instances
 * 
 * @author kirq4e
 */
public class RemoveDuplicateSecurityRoles extends UpdateSemanticTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String GET_SAME_ROLES = "select ?role2 ?predicate ?object ?user ?role where { "
			+ "graph <http://ittruse.ittbg.com/data/enterpriseManagementFramework> { "
			+ "?role2 ?predicate ?object . } { "
			+ "SELECT distinct ?role2 ?user ?role where { "
			+ "graph <http://ittruse.ittbg.com/data/enterpriseManagementFramework> { "
			+ "?role sec:hasPermission ?instance . "
			+ "?role sec:hasRoleType ?roleType . "
			+ "?role2 sec:hasPermission ?instance . "
			+ "?role2 sec:hasRoleType ?roleType . optional { "
			+ "?user sec:assignedTo ?role2 .} "
			+ "filter(?role != ?role2)}}}}";

	public static final String GET_DUPLICATE_ROLES_AND_INSTANCES = "SELECT distinct ?instance ?user  WHERE {\n"
			+ "    ?user sec:assignedTo ?role . \n" + "    ?role sec:hasPermission ?instance .\n" + "} \n"
			+ "group by ?instance ?user having(count(?instance) > 1)";

	public static final String GET_USER_ASSIGNED_TO_INSTANCE_ROLES = "select ?role ?roleType where {\n"
			+ "%s sec:assignedTo ?role . \n" + "    ?role sec:hasRoleType ?roleType .\n" + "?role sec:hasPermission %s"
			+ "}";

	public static final String GET_ROLE_MAPPING = "SELECT ?semanticRole ?sepRoleId WHERE {\n"
			+ "    ?semanticRole skos:inScheme conc:SecurityRoleTypes ;\n" + "        skos:exactMatch ?sepRoleUri .\n"
			+ "    ?sepRoleUri skos:prefLabel ?sepRoleId .\n" + "}";

	public static final String GET_CORRECT_ROLE_FOR_USER = "SELECT target_id as instance, authority, role\n"
			+ "  FROM sep_authority_role_assignment r join sep_entity_permission e on permission_id = e.id\n"
			+ "where target_id = '%s'  and authority = '%s'";

	private DataSource datasource;
	private NamespaceRegistryService namespaceRegistry;

	@Override
	public void setUp() throws SetupException {
		super.setUp();
		datasource = CDI
				.instantiateBean(DatabaseConfiguration.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral())
					.getDataSource();
		namespaceRegistry = CDI.instantiateBean(NamespaceRegistryService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		LOGGER.info("Executing Remove Duplicate Security Roles patch");
		TimeTracker tracker = TimeTracker.createAndStart();

		LOGGER.info("Removing security roles with same types!");
		removeSameRoles();
		LOGGER.info("Finished removing security roles with same types!");


		LOGGER.info("Executing Assign correct roles!");
		assignCorrectRole();
		LOGGER.info("Finished assign correct roles!");
		LOGGER.info("Remove Duplicate Security Roles patch executed for {} ms", tracker.stop());
	}

	private void assignCorrectRole() throws CustomChangeException {
		RepositoryConnection repositoryConnection = connectionFactory.produceConnection();
		Model removeModel = new LinkedHashModel();
		
		try {
			TimeTracker tracker = TimeTracker.createAndStart();
			
			Map<String, Value> rolesMapping = retrieveRoleMapping(repositoryConnection);
			try (TupleQueryResultIterator iterator = executeSparqlQuery(repositoryConnection,
					GET_DUPLICATE_ROLES_AND_INSTANCES)) {
				for (BindingSet bindingSet : iterator) {
					IRI user = (IRI) bindingSet.getBinding("user").getValue();
					String userShortUri = getShortUri(user);
					String instanceShortUri = getShortUri(bindingSet.getBinding("instance").getValue());

					String correctRole = getCorrectRole(instanceShortUri, userShortUri);
					if (correctRole == null) {
						// removing all assignments and log this action
						LOGGER.warn(
								"Couldn't find record in the RDB for instance: {} and authority: {}! Removing all assignments!",
								instanceShortUri, userShortUri);
					}
					Value semanticRoleId = rolesMapping.get(correctRole);
					String getUserAssignedToRoleQuery = String.format(GET_USER_ASSIGNED_TO_INSTANCE_ROLES, userShortUri,
							instanceShortUri);
					try (TupleQueryResultIterator roleIterator = executeSparqlQuery(repositoryConnection,
							getUserAssignedToRoleQuery)) {

						for (BindingSet userRole : roleIterator) {
							Value roleId = userRole.getBinding("role").getValue();
							Value roleType = userRole.getBinding("roleType").getValue();

							if (!roleType.equals(semanticRoleId)) {
								removeModel.add(user, Security.ASSIGNED_TO, roleId);
							}
						}
					}
				}
				LOGGER.info("Processed {} results for {} ms", iterator.getCount(), tracker.elapsedTime());
			}

			SemanticPersistenceHelper.removeModel(repositoryConnection, removeModel, EMF.DATA_CONTEXT);

		} catch (QueryEvaluationException | SQLException e) {
			UpdateSemanticTask.rollbackConnection(repositoryConnection, e);
			throw new CustomChangeException("Unable to update semantic repository because of: " + e.getMessage(), e);
		} finally {
			if (repositoryConnection != null) {
				connectionFactory.disposeConnection(repositoryConnection);
			}
		}
	}

	private void removeSameRoles() throws CustomChangeException {
		RepositoryConnection connection = connectionFactory.produceConnection();
		try (TupleQueryResultIterator iterator = executeSparqlQuery(connection, GET_SAME_ROLES)) {
			LinkedHashModel removeModel = new LinkedHashModel();
			LinkedHashModel addModel = new LinkedHashModel();

			Set<Value> processedRoles = new HashSet<>();
			for (BindingSet bindingSet : iterator) {
				Value originalRole = bindingSet.getValue("role");
				IRI role = (IRI) bindingSet.getValue("role2");
				if (!processedRoles.contains(role)) {
					removeModel.add(role, (IRI) bindingSet.getValue("predicate"),
							bindingSet.getValue("object"));

					Value user = bindingSet.getValue("user");

					if (user != null) {
						addModel.add((IRI) user, Security.ASSIGNED_TO, originalRole);
					}
					processedRoles.add(originalRole);
				}
			}
			
			SemanticPersistenceHelper.removeModel(connection, removeModel, EMF.DATA_CONTEXT);
			SemanticPersistenceHelper.saveModel(connection, addModel, EMF.DATA_CONTEXT);

		} catch (QueryEvaluationException e) {
			UpdateSemanticTask.rollbackConnection(connection, e);
			throw new CustomChangeException("Unable to update semantic repository because of: " + e.getMessage(), e);
		} finally {
			if (connection != null) {
				connectionFactory.disposeConnection(connection);
			}
		}
	}

	private static Map<String, Value> retrieveRoleMapping(RepositoryConnection repositoryConnection)
			throws QueryEvaluationException {

		Map<String, Value> rolesMapping = CollectionUtils.createHashMap(6);
		try (TupleQueryResultIterator iterator = executeSparqlQuery(repositoryConnection, GET_ROLE_MAPPING)) {
			for (BindingSet bindingSet : iterator) {
				rolesMapping.put(bindingSet.getBinding("sepRoleId").getValue().stringValue(),
						bindingSet.getBinding("semanticRole").getValue());
			}
		}
		return rolesMapping;
	}

	private static TupleQueryResultIterator executeSparqlQuery(RepositoryConnection connection, String query)
			throws QueryEvaluationException {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, query, CollectionUtils.emptyMap(),
				false);
		return new TupleQueryResultIterator(tupleQuery.evaluate());
	}

	private String getCorrectRole(String instance, String user) throws SQLException {
		String query = String.format(GET_CORRECT_ROLE_FOR_USER, instance, user);
		try (Connection connection = datasource.getConnection();
				PreparedStatement ps = connection.prepareStatement(query);
				ResultSet resultSet = ps.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getString("role");
			}
		}
		return null;
	}

	private String getShortUri(Value value) {
		IRI uri = (IRI) value;
		return namespaceRegistry.getShortUri(uri);
	}

}
