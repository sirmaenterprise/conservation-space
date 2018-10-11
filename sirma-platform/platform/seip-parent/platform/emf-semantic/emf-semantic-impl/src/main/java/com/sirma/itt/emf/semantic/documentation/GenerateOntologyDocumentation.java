package com.sirma.itt.emf.semantic.documentation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.semantic.repository.creator.RepositoryCreator;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Generate Ontology description in Confluence wiki format with hierarchical diagrams
 *
 * @author kirq4e
 */
public class GenerateOntologyDocumentation {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateOntologyDocumentation.class);
	private static final String ONTOLOGY_CLASS_HIERARCHY_QUERY_FILE = "definitions/Documentation/class_hierarchy.sparql";

	private static final String CLASS_DESCRIPTION_QUERY_FILE = "definitions/Documentation/class_description.sparql";
	private static final String CLASS_DESCRIPTION_SUPER_CLASSES_QUERY_FILE = "definitions/Documentation/superClasses.sparql";
	private static final String CLASS_DESCRIPTION_SUB_CLASSES_QUERY_FILE = "definitions/Documentation/subClasses.sparql";
	private static final String CLASS_DESCRIPTION_SUPER_CLASSES_HIERARCHY_QUERY_FILE = "definitions/Documentation/superClassesHierarchy.sparql";
	private static final String CLASS_DESCRIPTION_SUB_CLASSES_HIERARCHY_QUERY_FILE = "definitions/Documentation/subClassesHierarchy.sparql";
	private static final String CLASS_DESCRIPTION_OBJECT_PROPERTIES_QUERY_FILE = "definitions/Documentation/objectProperties.sparql";
	private static final String CLASS_DESCRIPTION_OBJECT_PROPERTIES_RELATIONS_QUERY_FILE = "definitions/Documentation/objectPropertiesRelations.sparql";
	private static final String CLASS_DESCRIPTION_DATA_PROPERTIES_QUERY_FILE = "definitions/Documentation/dataProperties.sparql";
	private static final String CLASS_DESCRIPTION_INHERITED_OBJECT_PROPERTIES_QUERY_FILE = "definitions/Documentation/inheritedObjectProperties.sparql";
	private static final String CLASS_DESCRIPTION_INHERITED_DATA_PROPERTIES_QUERY_FILE = "definitions/Documentation/inheritedDataProperties.sparql";

	private static final String DATA_PROPERTIES_DESCRIPTION_QUERY_FILE = "definitions/Documentation/data_properties_description.sparql";
	private static final String OBJECT_PROPERTIES_DESCRIPTION_QUERY_FILE = "definitions/Documentation/object_properties_description.sparql";
	private static final String PROPERTIES_DESCRIPTION_SUB_PROPERTIES_QUERY_FILE = "definitions/Documentation/properties_subProperties.sparql";
	private static final String PROPERTIES_DESCRIPTION_SUPER_PROPERTIES_QUERY_FILE = "definitions/Documentation/properties_superProperties.sparql";
	private static final String PROPERTIES_DESCRIPTION_DOMAIN_QUERY_FILE = "definitions/Documentation/property_domain.sparql";
	private static final String PROPERTIES_DESCRIPTION_RANGE_QUERY_FILE = "definitions/Documentation/property_range.sparql";
	private static final String GENERATE_DOMAIN_OBJECTS_LIST_FOR_TYPES_XML = "definitions/scripts/generateDomainObjectsForTypesXML.sparql";

	public static final String EMF_ONTOLOGY = "enterpriseManagementFramework";
	public static final String CHD_ONTOLOGY = "culturalHeritageDomain";
	public static final String PDM_ONTOLOGY = "pdmConfiguration";
	public static final String QMS_ONTOLOGY = "eQMS";
	public static final String HR_ONTOLOGY = "hrConfiguration";
	public static final String PTOP_ONTOLOGY = "protontop";
	public static final String TCRM_ONTOLOGY = "tcrmConfiguration";
	public static final String SEC_ONTOLOGY = "security";
	public static final String SKOS_ONTOLOGY = "skos/core";
	public static final String DC_TERMS_ONTOLOGY = "dc/";
	public static final String ALL_ONTOLOGIES = "ALL";

	public static final String SKOS_ONTOLOGY_FILES = "definitions/SKOS/skos.rdf";
	public static final String PROTON_ONTOLOGY_FILES = "definitions/Proton/protontop.ttl";
	public static final String DC_TERMS_ONTOLOGY_FILES = "definitions/DC/dcterms.rdf";
	public static final String SEC_ONTOLOGY_FILE = "definitions/Security/security.ttl";
	public static final String SEMI_COLUMN = ";";
	public static final String DEFAULT_ONTOLOGY_FILES = SKOS_ONTOLOGY_FILES + SEMI_COLUMN + PROTON_ONTOLOGY_FILES
			+ SEMI_COLUMN + DC_TERMS_ONTOLOGY_FILES;
	public static final String EMF_ONTOLOGY_FILES = DEFAULT_ONTOLOGY_FILES + SEMI_COLUMN
			+ "definitions/EMFDomainModel/emf.ttl";

	private static final Map<String, String> ONTOLOGY_FILES;

	static {
		ONTOLOGY_FILES = new HashMap<>();
		ONTOLOGY_FILES.put(EMF_ONTOLOGY, EMF_ONTOLOGY_FILES);
		ONTOLOGY_FILES.put(CHD_ONTOLOGY,
				EMF_ONTOLOGY_FILES + SEMI_COLUMN + "definitions/CulturalHeritageDomain/culturalHeritageDomain.ttl");
		ONTOLOGY_FILES.put(PDM_ONTOLOGY, EMF_ONTOLOGY_FILES + SEMI_COLUMN + "definitions/ProductDomainModel/pdm.ttl"
				+ SEMI_COLUMN + "definitions/eQMS/eQMS.ttl");
		ONTOLOGY_FILES.put(QMS_ONTOLOGY, EMF_ONTOLOGY_FILES + SEMI_COLUMN + "definitions/eQMS/eQMS.ttl");
		ONTOLOGY_FILES.put(HR_ONTOLOGY, EMF_ONTOLOGY_FILES + SEMI_COLUMN + "definitions/HR/hr.ttl");
		ONTOLOGY_FILES.put(PTOP_ONTOLOGY, PROTON_ONTOLOGY_FILES);
		ONTOLOGY_FILES.put(TCRM_ONTOLOGY, EMF_ONTOLOGY_FILES + SEMI_COLUMN + "definitions/TCRM/tcrm.ttl");
		ONTOLOGY_FILES.put(SKOS_ONTOLOGY, SKOS_ONTOLOGY_FILES);
		ONTOLOGY_FILES.put(DC_TERMS_ONTOLOGY, DC_TERMS_ONTOLOGY_FILES);
		ONTOLOGY_FILES.put(SEC_ONTOLOGY, SEC_ONTOLOGY_FILE);
		ONTOLOGY_FILES.put(ALL_ONTOLOGIES,
				EMF_ONTOLOGY_FILES + SEMI_COLUMN + "definitions/TCRM/tcrm.ttl" + SEMI_COLUMN + "definitions/HR/hr.ttl"
						+ SEMI_COLUMN + "definitions/eQMS/eQMS.ttl" + SEMI_COLUMN
						+ "definitions/ProductDomainModel/pdm.ttl" + SEMI_COLUMN
						+ "definitions/CulturalHeritageDomain/culturalHeritageDomain.ttl");
	}

	/**
	 * Generates Hierarchical diagram from the ontology which is located on remote server and repository
	 *
	 * @param serverAddress
	 *            Server address
	 * @param repositoryName
	 *            Repository name
	 * @param userName
	 *            User name
	 * @param password
	 *            Password
	 * @param ontology
	 *            The Ontology for which to generate the diagram
	 */
	public void generateHierarchicalDiagram(String serverAddress, String repositoryName, String userName,
			String password, String ontology) {
		RepositoryConnection connection = getRepositoryConnection(serverAddress, repositoryName, userName, password,
				ontology);
		generateHierarchicalDiagram(connection);
	}

	/**
	 * Generates Hierarchical diagram from the ontology which is located on local repository
	 *
	 * @param ontology
	 *            The ontology for which to generate the diagram
	 */
	public void generateHierarchicalDiagram(String ontology) {
		RepositoryConnection connection = getRepositoryConnection(null, null, null, null, ontology);
		generateHierarchicalDiagram(connection);
	}

	/**
	 * Generates ontology documentation in wiki format for Confluence
	 *
	 * @param serverAddress
	 *            Server address
	 * @param repositoryName
	 *            Repository name
	 * @param userName
	 *            User name
	 * @param password
	 *            Password
	 * @param ontology
	 *            The Ontology for which to generate documentation
	 */
	public void generateOntologyDocumentation(String serverAddress, String repositoryName, String userName,
			String password, String ontology) {
		RepositoryConnection connection = getRepositoryConnection(serverAddress, repositoryName, userName, password,
				ontology);
		generateOntologyDocumentation(connection, ontology);
	}

	/**
	 * Generates ontology documentation in wiki format for Confluence
	 *
	 * @param ontology
	 *            The Ontology for which to generate documentation
	 */
	public void generateOntologyDocumentation(String ontology) {
		RepositoryConnection connection = getRepositoryConnection(null, null, null, null, ontology);
		generateOntologyDocumentation(connection, ontology);
	}

	/**
	 * Generates ontology documentation in wiki format for Confluence for the given connection to repository
	 *
	 * @param connection
	 *            Open connection to the repository
	 * @param ontology
	 *            The Ontology for which to generate documentation
	 */
	private void generateOntologyDocumentation(RepositoryConnection connection, String ontology) {
		ValueFactory vf = connection.getValueFactory();
		StringBuilder builder = new StringBuilder("{toc:maxLevel=2}\n\nh1. Classes \n\n");

		String query;
		try {
			query = IOUtils.toString(GenerateOntologyDocumentation.class
					.getClassLoader()
						.getResourceAsStream(CLASS_DESCRIPTION_QUERY_FILE));

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setBinding("ontology", vf.createLiteral(ontology));
			tupleQuery.setIncludeInferred(false);

			TupleQueryResult result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet tuple = result.next();
				Value classURI = tuple.getBinding("class").getValue();
				String description = tuple.getBinding("description").getValue().stringValue();

				builder.append(description);

				// h4. Parent
				String queryResult = executeQueryForClass(connection, classURI, "superClasses",
						CLASS_DESCRIPTION_SUPER_CLASSES_QUERY_FILE);
				builder.append("h4. Parent\n").append(queryResult).append("\n");

				// h4. Subclasses
				queryResult = executeQueryForClass(connection, classURI, "subClasses",
						CLASS_DESCRIPTION_SUB_CLASSES_QUERY_FILE);
				builder.append("h4. Subclasses\n").append(queryResult).append("\n");

				// h4. Hierarchy
				queryResult = executeQueryForClass(connection, classURI, "superClassesHierarchy",
						CLASS_DESCRIPTION_SUPER_CLASSES_HIERARCHY_QUERY_FILE);
				StringBuilder hierarchy = new StringBuilder();
				hierarchy.append("h4. Hierarchy\n").append(
						"{plantuml}\n@startuml\nhide empty attributes\nhide empty methods\n");
				hierarchy.append("skinparam class {\nArrowColor #436CA6\nBorderColor #436CA6\n}\n");
				hierarchy.append(queryResult).append("\n\n");

				queryResult = executeQueryForClass(connection, classURI, "subClassesHierarchy",
						CLASS_DESCRIPTION_SUB_CLASSES_HIERARCHY_QUERY_FILE);
				if (hierarchy.length() == 0) {
					hierarchy.append("h4. Hierarchy\n").append(
							"{plantuml}\n@startuml\nhide empty attributes\nhide empty methods\n");
					hierarchy.append("skinparam class {\nArrowColor #436CA6\nBorderColor #436CA6\n}\n");
				}

				if (hierarchy.length() != 0) {
					hierarchy.append("\n@enduml\n{plantuml}\n\n");
					builder.append(hierarchy);
				}

				// h4. Object properties
				queryResult = executeQueryForClass(connection, classURI, "objectProperties",
						CLASS_DESCRIPTION_OBJECT_PROPERTIES_QUERY_FILE);
				builder.append("h4. Object properties\n").append(queryResult).append("\n");

				// h4. Relations
				queryResult = executeQueryForClass(connection, classURI, "objectPropertiesRelations",
						CLASS_DESCRIPTION_OBJECT_PROPERTIES_RELATIONS_QUERY_FILE);
				builder.append("h4. Relations\n");
				if (StringUtils.isNotBlank(queryResult)) {
					builder.append("{plantuml}\n@startuml\nhide empty attributes\nhide empty methods\n");
					builder.append("skinparam class {\nArrowColor #436CA6\nBorderColor #436CA6\n}\n");
					builder.append(queryResult).append("\n@enduml\n{plantuml}\n");
				}
				builder.append("\n");

				// h4. Inherited Object properties
				queryResult = executeQueryForClass(connection, classURI, "inheritedObjectProperties",
						CLASS_DESCRIPTION_INHERITED_OBJECT_PROPERTIES_QUERY_FILE);
				builder.append("h4. Inherited Object properties\n").append(queryResult).append("\n");

				// h4. Data Properties
				queryResult = executeQueryForClass(connection, classURI, "dataProperties",
						CLASS_DESCRIPTION_DATA_PROPERTIES_QUERY_FILE);
				builder.append("h4. Data Properties\n").append(queryResult).append("\n");

				// h4. Inherited Data properties
				queryResult = executeQueryForClass(connection, classURI, "inheritedDataProperties",
						CLASS_DESCRIPTION_INHERITED_DATA_PROPERTIES_QUERY_FILE);
				builder.append("h4. Inherited Data Properties\n").append(queryResult).append("\n");

				builder.append("----\n\n");
			}
			result.close();

			builder.append("h1. Object properties \n\n");
			String propertiesDescription = parsePropertiesDescription(connection, ontology,
					OBJECT_PROPERTIES_DESCRIPTION_QUERY_FILE);
			builder.append(propertiesDescription);

			builder.append("h1. Data properties \n\n");
			propertiesDescription = parsePropertiesDescription(connection, ontology,
					DATA_PROPERTIES_DESCRIPTION_QUERY_FILE);
			builder.append(propertiesDescription);

			LOGGER.info(builder.toString());

			disposeConnection(connection);
		} catch (IOException | RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			LOGGER.error("Exception occured: ", e);
		}
	}

	/**
	 * Parses the description of the property that is received as result of the executed SPARQL query. The returned
	 * description is in Confluence wiki markup. The description contains the title, description, domain, range, sub
	 * properties and super properties
	 *
	 * @param connection
	 *            Connection to the semantic repository
	 * @param ontology
	 *            Ontology of the property
	 * @param queryFileName
	 *            SPARQL Query that generates the data for the description
	 * @return Description of the property in Confluence wiki markup
	 * @throws IOException
	 *             If an error occurs
	 * @throws RepositoryException
	 *             If an error occurs
	 * @throws MalformedQueryException
	 *             If the query is malformed
	 * @throws QueryEvaluationException
	 *             If the query cannot be evaluated
	 */
	private String parsePropertiesDescription(RepositoryConnection connection, String ontology, String queryFileName)
			throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		String query;
		TupleQuery tupleQuery;
		TupleQueryResult result;
		query = IOUtils
				.toString(GenerateOntologyDocumentation.class.getClassLoader().getResourceAsStream(queryFileName));

		ValueFactory vf = connection.getValueFactory();
		tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
		tupleQuery.setBinding("ontology", vf.createLiteral(ontology));
		tupleQuery.setIncludeInferred(false);

		StringBuilder builder = new StringBuilder();

		result = tupleQuery.evaluate();
		while (result.hasNext()) {
			BindingSet tuple = result.next();
			Value classURI = tuple.getBinding("property").getValue();
			String description = tuple.getBinding("description").getValue().stringValue();

			builder.append(description);
			// h4. Domain
			String queryResult = executeQueryForProperty(connection, classURI, "domain",
					PROPERTIES_DESCRIPTION_DOMAIN_QUERY_FILE);
			builder.append("h4. Domain\n").append(queryResult).append("\n");

			// h4. Range
			queryResult = executeQueryForProperty(connection, classURI, "range",
					PROPERTIES_DESCRIPTION_RANGE_QUERY_FILE);
			builder.append("h4. Range\n").append(queryResult).append("\n");

			// h4. Parent
			queryResult = executeQueryForProperty(connection, classURI, "superProperties",
					PROPERTIES_DESCRIPTION_SUPER_PROPERTIES_QUERY_FILE);
			builder.append("h4. Parent\n").append(queryResult).append("\n");

			// h4. Subproperties
			queryResult = executeQueryForProperty(connection, classURI, "subProperties",
					PROPERTIES_DESCRIPTION_SUB_PROPERTIES_QUERY_FILE);
			builder.append("h4. Subproperties\n").append(queryResult).append("\n");

			builder.append("----\n");
		}

		result.close();

		return builder.toString();
	}

	/**
	 * Executes SPARQL query for Class and generates description information
	 *
	 * @param connection
	 *            Connection to the repository
	 * @param inputURI
	 *            URI of the input class
	 * @param resultBinding
	 *            Result property name
	 * @param queryFileName
	 *            SPARQL Query file name
	 * @return Description of the class
	 * @throws IOException
	 *             If an error occurs
	 * @throws RepositoryException
	 *             If an error occurs
	 * @throws MalformedQueryException
	 *             If the query is malformed
	 * @throws QueryEvaluationException
	 *             If the query cannot be evaluated
	 */
	private String executeQueryForClass(RepositoryConnection connection, Value inputURI, String resultBinding,
			String queryFileName)
					throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		return executeQuery(connection, "class", inputURI, resultBinding, queryFileName);
	}

	/**
	 * Executes SPARQL query for Property and generates description information
	 *
	 * @param connection
	 *            Connection to the repository
	 * @param inputURI
	 *            URI of the input class
	 * @param resultBinding
	 *            Result property name
	 * @param queryFileName
	 *            SPARQL Query file name
	 * @return Description of the property
	 * @throws IOException
	 *             If an error occurs
	 * @throws RepositoryException
	 *             If an error occurs
	 * @throws MalformedQueryException
	 *             If the query is malformed
	 * @throws QueryEvaluationException
	 *             If the query cannot be evaluated
	 */
	private String executeQueryForProperty(RepositoryConnection connection, Value inputURI, String resultBinding,
			String queryFileName)
					throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		return executeQuery(connection, "property", inputURI, resultBinding, queryFileName);
	}

	/**
	 * Executes SPARQL query and generates description information for the input parameter
	 *
	 * @param connection
	 *            Connection to the repository
	 * @param inputBinding
	 *            Name of the inpurt parameter of the query
	 * @param inputURI
	 *            URI of the input class
	 * @param resultBinding
	 *            Result property name
	 * @param queryFileName
	 *            SPARQL Query file name
	 * @return Description of the property
	 * @throws IOException
	 *             If an error occurs
	 * @throws RepositoryException
	 *             If an error occurs
	 * @throws MalformedQueryException
	 *             If the query is malformed
	 * @throws QueryEvaluationException
	 *             If the query cannot be evaluated
	 */
	private String executeQuery(RepositoryConnection connection, String inputBinding, Value inputURI,
			String resultBinding, String queryFileName)
					throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		String query;
		TupleQuery tupleQuery;
		Binding tempBinding;

		query = IOUtils
				.toString(GenerateOntologyDocumentation.class.getClassLoader().getResourceAsStream(queryFileName));
		tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
		tupleQuery.setIncludeInferred(false);
		tupleQuery.setBinding(inputBinding, inputURI);
		TupleQueryResult tempResult = tupleQuery.evaluate();
		if (tempResult.hasNext()) {

			tempBinding = tempResult.next().getBinding(resultBinding);
			String resultString = tempBinding.getValue().stringValue();
			if (resultString != null && !resultString.trim().isEmpty()) {
				return resultString;
			}
		}
		tempResult.close();

		return "";
	}

	/**
	 * Generates Hierarchical diagram for the given ontology on the open connection to the repository
	 *
	 * @param connection
	 *            Open connection to the repository
	 */
	private void generateHierarchicalDiagram(RepositoryConnection connection) {
		String query;
		try {
			query = IOUtils.toString(GenerateOntologyDocumentation.class
					.getClassLoader()
						.getResourceAsStream(ONTOLOGY_CLASS_HIERARCHY_QUERY_FILE));

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setIncludeInferred(false);

			TupleQueryResult result = tupleQuery.evaluate();

			Binding hierarchy = result.next().getBinding("hierarchyDiagram");
			String stringHierarchy = hierarchy.getValue().stringValue();
			LOGGER.info(stringHierarchy);

			result.close();

			disposeConnection(connection);
		} catch (IOException | RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			LOGGER.error("Exception occured: ", e);
		}
	}

	/**
	 * Generate a list of URIs of Domain objects for types.xml
	 *
	 * @param ontology
	 *            Ontology for which the list will be generated
	 */
	public void generateDomainObjectsList(String ontology) {
		generateDomainObjectsList(null, null, null, null, ontology);
	}

	/**
	 * Generate a list of URIs of Domain objects for types.xml
	 *
	 * @param serverAddress
	 *            Semantic server address
	 * @param repositoryName
	 *            Semantic repository name
	 * @param userName
	 *            Required username for authenticating against the semantic server
	 * @param password
	 *            Required password for authenticating against the semantic server
	 * @param ontology
	 *            Ontology for which the list will be generated
	 */
	public void generateDomainObjectsList(String serverAddress, String repositoryName, String userName, String password,
			String ontology) {
		RepositoryConnection connection = getRepositoryConnection(serverAddress, repositoryName, userName, password,
				ontology);
		String query;
		try {
			query = IOUtils.toString(GenerateOntologyDocumentation.class
					.getClassLoader()
						.getResourceAsStream(GENERATE_DOMAIN_OBJECTS_LIST_FOR_TYPES_XML));

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setIncludeInferred(true);

			TupleQueryResult result = tupleQuery.evaluate();

			Binding hierarchy = result.next().getBinding("classes");
			String stringHierarchy = hierarchy.getValue().stringValue();
			LOGGER.info(stringHierarchy);

			result.close();

			disposeConnection(connection);
		} catch (IOException | RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			LOGGER.info("Exception occured: ", e);
		}
	}

	/**
	 * Opens connection to the repository
	 *
	 * @param serverAddress
	 *            Semantic Server address or null for local repository connection.
	 * @param repositoryName
	 *            Name of the repository for remote connection
	 * @param userName
	 *            User name
	 * @param password
	 *            Password
	 * @param ontology
	 *            Loads the ontology files for the given ontology when using local repository
	 * @return Open connection to the repository
	 */
	private static RepositoryConnection getRepositoryConnection(String serverAddress, String repositoryName,
			String userName, String password, String ontology) {
		if (serverAddress != null) {
			try {
				RemoteRepositoryManager repositoryManager = RemoteRepositoryManager.getInstance(serverAddress, userName,
						password);
				repositoryManager.initialize();

				Repository repository = repositoryManager.getRepository(repositoryName);
				repository.initialize();
				return repository.getConnection();
			} catch (RepositoryException | RepositoryConfigException e) {
				LOGGER.error("Repository initialization failed", e);
			}
		} else {
			Properties properties = new Properties();
			properties.put(RepositoryCreator.REPOSITORY_ID, "test");
			properties.put(RepositoryCreator.REPOSITORY_LABEL, "label");
			properties.put(RepositoryCreator.REPOSITORY_TYPE, RepositoryCreator.REPOSITORY_TYPE_LOCAL);
			properties.put(RepositoryCreator.REPOSITORY_CONFIG_FILE, "repository_configurations/owlim-se.ttl");

			String ontologyFiles = ONTOLOGY_FILES.get(ontology);
			if (StringUtils.isBlank(ontologyFiles)) {
				ontologyFiles = ONTOLOGY_FILES.get(ALL_ONTOLOGIES);
			}
			properties.put(RepositoryCreator.IMPORT_FILES, ontologyFiles);

			try {
				Repository repository = RepositoryCreator.initializeRepository(properties);
				return repository.getConnection();
			} catch (RepositoryException | IOException e) {
				LOGGER.error("Exception occured while initializing connection", e);
				throw new EmfRuntimeException(e);
			}
		}
		return null;
	}

	/**
	 * Close the connection to the repository
	 *
	 * @param connection
	 *            Open repository connection
	 */
	private void disposeConnection(RepositoryConnection connection) {
		try {
			connection.getRepository().shutDown();
			connection.close();
		} catch (RepositoryException e) {
			LOGGER.error("Exception occured while shutting down repository connection", e);
		}
	}

	/**
	 * Starts the program
	 *
	 * @param args
	 *            main arguments
	 */
	public static void main(String[] args) {
		GenerateOntologyDocumentation instance = new GenerateOntologyDocumentation();
		instance.generateDomainObjectsList(ALL_ONTOLOGIES);
	}

}
