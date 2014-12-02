package com.sirma.itt.emf.semantic.documentation;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.repository.creator.RepositoryCreator;

/**
 * Generate Ontology description in Confluence wiki format with hierarchical diagrams
 * 
 * @author kirq4e
 */
public class GenerateOntologyDocumentation {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GenerateOntologyDocumentation.class);
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
	public static final String SKOS_ONTOLOGY = "skos/core";
	public static final String DC_TERMS_ONTOLOGY = "dc/";
	public static final String ALL_ONTOLOGIES = "ALL";

	public static final String SKOS_ONTOLOGY_FILES = "definitions/SKOS/skos.rdf";
	public static final String PROTON_ONTOLOGY_FILES = "definitions/Proton/protontop.ttl";
	public static final String DC_TERMS_ONTOLOGY_FILES = "definitions/DC/dcterms.rdf";
	public static final String SEMI_COLUMN = ";";
	public static final String DEFAULT_ONTOLOGY_FILES = SKOS_ONTOLOGY_FILES + SEMI_COLUMN
			+ PROTON_ONTOLOGY_FILES + SEMI_COLUMN + DC_TERMS_ONTOLOGY_FILES;
	public static final String EMF_ONTOLOGY_FILES = DEFAULT_ONTOLOGY_FILES + SEMI_COLUMN
			+ "definitions/EMFDomainModel/emf.ttl";

	/**
	 * Generates Hierarchical diagram from the ontology which is located on remote server and
	 * repository
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
	public void generateHierarchicalDiagram(String serverAddress, String repositoryName,
			String userName, String password, String ontology) {
		RepositoryConnection connection = getRepositoryConnection(serverAddress, repositoryName,
				userName, password, ontology);
		generateHierarchicalDiagram(connection, ontology);
	}

	/**
	 * Generates Hierarchical diagram from the ontology which is located on local repository
	 * 
	 * @param ontology
	 *            The ontology for which to generate the diagram
	 */
	public void generateHierarchicalDiagram(String ontology) {
		RepositoryConnection connection = getRepositoryConnection(null, null, null, null, ontology);
		generateHierarchicalDiagram(connection, ontology);
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
	public void generateOntologyDocumentation(String serverAddress, String repositoryName,
			String userName, String password, String ontology) {
		RepositoryConnection connection = getRepositoryConnection(serverAddress, repositoryName,
				userName, password, ontology);
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
	 * Generates ontology documentation in wiki format for Confluence for the given connection to
	 * repository
	 * 
	 * @param connection
	 *            Open connection to the repository
	 * @param ontology
	 *            The Ontology for which to generate documentation
	 */
	private void generateOntologyDocumentation(RepositoryConnection connection, String ontology) {

		ValueFactory vf = connection.getValueFactory();

		String query;
		try {
			query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
					.getResourceAsStream(CLASS_DESCRIPTION_QUERY_FILE));

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setBinding("ontology", vf.createLiteral(ontology));
			tupleQuery.setIncludeInferred(false);

			StringBuilder builder = new StringBuilder("{toc:maxLevel=2}\n\nh1. Classes \n\n");

			TupleQueryResult result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet tuple = result.next();
				Value classURI = tuple.getBinding("class").getValue();
				String description = tuple.getBinding("description").getValue().stringValue();

				builder.append(description);
				Binding tempBinding = null;
				String temp = null;

				// h4. Parent

				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(CLASS_DESCRIPTION_SUPER_CLASSES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				TupleQueryResult tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {

					tempBinding = tempResult.next().getBinding("superClasses");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Parent\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				// h4. Subclasses
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(CLASS_DESCRIPTION_SUB_CLASSES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("subClasses");
					temp = tempBinding.getValue().stringValue();

					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Subclasses\n").append(temp).append("\n\n");
					}
				}
				tempResult.close();

				// h4. Hierarchy
				StringBuilder hierarchy = new StringBuilder();

				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(CLASS_DESCRIPTION_SUPER_CLASSES_HIERARCHY_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("superClassesHierarchy");

					hierarchy.append("h4. Hierarchy\n").append(
							"{plantuml}\n@startuml\nhide empty attributes\nhide empty methods\n");
					hierarchy
							.append("skinparam class {\nArrowColor #436CA6\nBorderColor #436CA6\n}\n");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						hierarchy.append(temp).append("\n\n");
					}
				}
				tempResult.close();

				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(CLASS_DESCRIPTION_SUB_CLASSES_HIERARCHY_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("subClassesHierarchy");

					if (hierarchy.length() == 0) {
						hierarchy
								.append("h4. Hierarchy\n")
								.append("{plantuml}\n@startuml\nhide empty attributes\nhide empty methods\n");
						hierarchy
								.append("skinparam class {\nArrowColor #436CA6\nBorderColor #436CA6\n}\n");
					}

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						hierarchy.append(temp);

					}
				}

				if (hierarchy.length() != 0) {
					hierarchy.append("\n@enduml\n{plantuml}\n\n");
					builder.append(hierarchy);
				}
				tempResult.close();

				// h4. Object properties
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(CLASS_DESCRIPTION_OBJECT_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("objectProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Object properties\n");
						builder.append(temp).append("\n\n");
					}
				}
				tempResult.close();

				// h4. Relations
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(
								CLASS_DESCRIPTION_OBJECT_PROPERTIES_RELATIONS_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {

					tempBinding = tempResult.next().getBinding("objectPropertiesRelations");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Relations\n")
								.append("{plantuml}\n@startuml\nhide empty attributes\nhide empty methods\n");
						builder.append("skinparam class {\nArrowColor #436CA6\nBorderColor #436CA6\n}\n");
						builder.append(temp).append("\n@enduml\n{plantuml}\n\n");
					}
				}
				tempResult.close();

				// h4. Inherited Object properties
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(
								CLASS_DESCRIPTION_INHERITED_OBJECT_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("inheritedObjectProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Inherited Object properties\n");
						builder.append(temp).append("\n\n");
					}
				}
				tempResult.close();

				// h4. Data Properties
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(CLASS_DESCRIPTION_DATA_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("class", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {

					tempBinding = tempResult.next().getBinding("dataProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Data Properties\n");
						builder.append(temp).append("\n\n");
					}
				}
				tempResult.close();

				// h4. Inherited Data properties
				query = IOUtils
						.toString(GenerateOntologyDocumentation.class.getClassLoader()
								.getResourceAsStream(
										CLASS_DESCRIPTION_INHERITED_DATA_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setBinding("class", classURI);
				tupleQuery.setIncludeInferred(false);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("inheritedDataProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Inherited Data Properties\n");
						builder.append(temp).append("\n");
					}
				}
				tempResult.close();
				builder.append("----\n\n");

			}
			result.close();

			query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
					.getResourceAsStream(OBJECT_PROPERTIES_DESCRIPTION_QUERY_FILE));

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setBinding("ontology", vf.createLiteral(ontology));
			tupleQuery.setIncludeInferred(false);

			builder.append("h1. Object properties \n\n");

			result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet tuple = result.next();
				Value classURI = tuple.getBinding("property").getValue();
				String description = tuple.getBinding("description").getValue().stringValue();

				builder.append(description);
				Binding tempBinding = null;
				String temp = null;

				// h4. Domain

				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_DOMAIN_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				TupleQueryResult tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("domain");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Domain\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				// h4. Range
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_RANGE_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("range");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Range\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				// h4. Parent
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_SUPER_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("superProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Parent\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				// h4. Subproperties
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_SUB_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {

					tempBinding = tempResult.next().getBinding("subProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Subproperties\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				builder.append("----\n");
			}

			result.close();

			query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
					.getResourceAsStream(DATA_PROPERTIES_DESCRIPTION_QUERY_FILE));

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setBinding("ontology", vf.createLiteral(ontology));
			tupleQuery.setIncludeInferred(false);

			builder.append("h1. Data properties \n\n");

			result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet tuple = result.next();
				Value classURI = tuple.getBinding("property").getValue();
				String description = tuple.getBinding("description").getValue().stringValue();

				builder.append(description);
				Binding tempBinding = null;
				String temp = null;

				// h4. Domain

				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_DOMAIN_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				TupleQueryResult tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("domain");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Domain\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				// h4. Range
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_RANGE_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("range");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Range\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				// h4. Parent
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_SUPER_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {
					tempBinding = tempResult.next().getBinding("superProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Parent\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				// h4. Subproperties
				query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
						.getResourceAsStream(PROPERTIES_DESCRIPTION_SUB_PROPERTIES_QUERY_FILE));
				tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
				tupleQuery.setIncludeInferred(false);
				tupleQuery.setBinding("property", classURI);
				tempResult = tupleQuery.evaluate();
				if (tempResult.hasNext()) {

					tempBinding = tempResult.next().getBinding("subProperties");

					temp = tempBinding.getValue().stringValue();
					if (temp != null && !temp.trim().isEmpty()) {
						builder.append("h4. Subproperties\n").append(temp).append("\n");
					}
				}
				tempResult.close();

				builder.append("----\n");
			}

			result.close();

			LOGGER.info(builder.toString());

			disposeConnection(connection);
		} catch (IOException | RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
			LOGGER.error("Exception occured: ", e);
		}
	}

	/**
	 * Generates Hierarchical diagram for the given ontology on the open connection to the
	 * repository
	 * 
	 * @param connection
	 *            Open connection to the repository
	 * @param ontology
	 *            The Ontology for which to generate the hierarchical diagram
	 */
	private void generateHierarchicalDiagram(RepositoryConnection connection, String ontology) {
		String query;
		try {
			query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
					.getResourceAsStream(ONTOLOGY_CLASS_HIERARCHY_QUERY_FILE));

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setIncludeInferred(false);

			TupleQueryResult result = tupleQuery.evaluate();

			Binding hierarchy = result.next().getBinding("hierarchyDiagram");
			String stringHierarchy = hierarchy.getValue().stringValue();
			LOGGER.info(stringHierarchy);

			result.close();

			disposeConnection(connection);
		} catch (IOException | RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
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
		RepositoryConnection connection = getRepositoryConnection(null, null, null, null, ontology);
		String query;
		try {
			query = IOUtils.toString(GenerateOntologyDocumentation.class.getClassLoader()
					.getResourceAsStream(GENERATE_DOMAIN_OBJECTS_LIST_FOR_TYPES_XML));

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setIncludeInferred(true);

			TupleQueryResult result = tupleQuery.evaluate();

			Binding hierarchy = result.next().getBinding("classes");
			String stringHierarchy = hierarchy.getValue().stringValue();
			LOGGER.info(stringHierarchy);

			result.close();

			disposeConnection(connection);
		} catch (IOException | RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
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
	private RepositoryConnection getRepositoryConnection(String serverAddress,
			String repositoryName, String userName, String password, String ontology) {
		if (serverAddress != null) {
			try {
				RemoteRepositoryManager repositoryManager = RemoteRepositoryManager.getInstance(
						serverAddress, userName, password);
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
			properties.put(RepositoryCreator.REPOSITORY_TYPE,
					RepositoryCreator.REPOSITORY_TYPE_LOCAL);
			properties.put(RepositoryCreator.REPOSITORY_CONFIG_FILE,
					"repository_configurations/owlim-se.ttl");

			switch (ontology) {
				case EMF_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, EMF_ONTOLOGY_FILES);
					break;
				case CHD_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, EMF_ONTOLOGY_FILES + SEMI_COLUMN
							+ "definitions/CulturalHeritageDomain/culturalHeritageDomain.ttl");
					break;
				case PDM_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, EMF_ONTOLOGY_FILES + SEMI_COLUMN
							+ "definitions/ProductDomainModel/pdm.ttl" + SEMI_COLUMN + "definitions/eQMS/eQMS.ttl");
					break;
				case QMS_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, EMF_ONTOLOGY_FILES + SEMI_COLUMN
							+ "definitions/eQMS/eQMS.ttl");
					break;
				case HR_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, EMF_ONTOLOGY_FILES + SEMI_COLUMN
							+ "definitions/HR/hr.ttl");
					break;
				case PTOP_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, PROTON_ONTOLOGY_FILES);
					break;
				case TCRM_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, EMF_ONTOLOGY_FILES + SEMI_COLUMN
							+ "definitions/TCRM/tcrm.ttl");
					break;
				case SKOS_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, SKOS_ONTOLOGY_FILES);
					break;
				case DC_TERMS_ONTOLOGY:
					properties.put(RepositoryCreator.IMPORT_FILES, DC_TERMS_ONTOLOGY_FILES);
					break;
				case ALL_ONTOLOGIES:
					properties.put(RepositoryCreator.IMPORT_FILES, EMF_ONTOLOGY_FILES + SEMI_COLUMN
							+ "definitions/TCRM/tcrm.ttl" + SEMI_COLUMN + "definitions/HR/hr.ttl"
							+ SEMI_COLUMN + "definitions/eQMS/eQMS.ttl" + SEMI_COLUMN
							+ "definitions/ProductDomainModel/pdm.ttl" + SEMI_COLUMN
							+ "definitions/CulturalHeritageDomain/culturalHeritageDomain.ttl");
					break;
			}

			Repository repository = new RepositoryCreator().initializeRepository(properties);

			try {
				return repository.getConnection();
			} catch (RepositoryException e) {
				LOGGER.error("Exception occured while closing connection", e);
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

		instance.generateDomainObjectsList(PDM_ONTOLOGY);
	}

}
