package com.sirma.itt.emf.semantic.repository.creator;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.ResourceSource;
import com.sirma.itt.semantic.namespaces.DefaultNamespaces;

/**
 * Create repository from configuration file on remote server Import turtle files into the repository Print statistics
 * Also create task for cleaning of a repository and initialize it Create task for exporting a repository
 * <p/>
 * The Configuration about the repository creator is located in the configuration file
 * repository_creator_config.properties. Repository configurations for different types of repositories are located in
 * the folder repository_configurations. Before running one of these configurations must be set in the configuration
 * file
 *
 * @author kirq4e
 */
public class RepositoryCreator {

	public static final String SYSTEM_REPOSITORY_ID = "SYSTEM";

	public static final String REPOSITORY_CONFIG_FILE = "repository.config.file";
	public static final String REPOSITORY_CREATOR_MAIN_CONFIG_PROPERTIES = "repository_creator_config.properties";
	public static final String REPOSITORY_URI = "http://www.openrdf.org/config/repository#Repository";

	/**
	 * The operation that must be performed on the repository: Drop-Create repository Update repository Refresh
	 * namespaces Refresh all repositories namespaces
	 */
	public static final String CREATOR_OPERATION = "repository.creator.operation";
	public static final String CREATOR_OPERATION_DROP_CREATE = "create";
	public static final String CREATOR_OPERATION_UPDATE = "update";
	public static final String CREATOR_OPERATION_REFRESH_NAMESPACES = "refresh.namespaces";
	public static final String CREATOR_OPERATION_REFRESH_ALL_NAMESPACES = "refresh.all.namespaces";
	private static final String CREATOR_OPERATION_INFO = "info";

	public static final String REPOSITORY_ID = "repository.id";
	// local or remote
	public static final String REPOSITORY_TYPE = "repository.type";
	public static final String REPOSITORY_LOCATION = "repository.location";
	public static final String REPOSITORY_TYPE_LOCAL = "local";
	public static final String REPOSITORY_TYPE_REMOTE = "remote";

	public static final String REPOSITORY_LABEL = "repository.label";
	public static final String REPOSITORY_CONFIGURATION_FILE = "repository.config.file";
	public static final String SERVER_ADDRESS = "server.address";
	public static final String USERNAME = "server.username";
	public static final String CREDENTIAL = "server.password";
	// list of files to import separated with ';'
	public static final String IMPORT_FILES = "import.files";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private RepositoryCreator() {
		// utility class
	}

	/**
	 * Initialize repository for the given parameters
	 *
	 * @param properties
	 *            Repository properties
	 * @return initialized repository
	 * @throws IOException
	 *             when initialization fails.
	 */
	public static Repository initializeRepository(Properties properties) throws IOException {

		if (properties == null) {
			throw new IllegalArgumentException("Input parameters are NULL!");
		}

		LOGGER.info("Iniializing repository with properties: " + properties);

		String repositoryId = properties.getProperty(REPOSITORY_ID);
		String repositoryConfigFileName = properties.getProperty(REPOSITORY_CONFIGURATION_FILE);
		String repositoryLabel = properties.getProperty(REPOSITORY_LABEL);
		repositoryLabel = repositoryLabel + " Created on: " + new Date();

		Model repositoryRdfDescription = RepositoryUtils.parseFile(new ResourceSource(repositoryConfigFileName));

		Resource repositoryNode = getRepositoryNode(repositoryConfigFileName, repositoryRdfDescription);

		// set repository ID and label
		repositoryRdfDescription.remove(repositoryNode, RepositoryConfigSchema.REPOSITORYID, null);
		repositoryRdfDescription.add(repositoryNode, RepositoryConfigSchema.REPOSITORYID,
				SimpleValueFactory.getInstance().createLiteral(repositoryId), (Resource) null);

		repositoryRdfDescription.remove(repositoryNode, RDFS.LABEL, null);
		repositoryRdfDescription.add(repositoryNode, RDFS.LABEL,
				SimpleValueFactory.getInstance().createLiteral(repositoryLabel), (Resource) null);
		try {
			Repository repository = createAndInitializeRepository(properties, repositoryId, repositoryRdfDescription,
					repositoryNode);
			try (RepositoryConnection connection = repository.getConnection()) {

				RepositoryUtils.refreshNamespaces(connection, DefaultNamespaces.ALL_NAMESPACES);

				// import files
				importSparqlFiles(properties, connection);
			}
			return repository;
		} catch (RepositoryException e) {
			throw onFailedConnectionToSesameServer(e);
		} catch (RepositoryConfigException e) {
			throw onUnableToProcessRepositoryConfiguration(e);
		}
	}

	private static EmfRuntimeException onUnableToProcessRepositoryConfiguration(RepositoryConfigException e) {
		String error = "Unable to process the repository configuration: " + e.getMessage();
		return new EmfRuntimeException(error, e);
	}

	private static Resource getRepositoryNode(String repositoryConfigFileName, Model repositoryRdfDescription) {
		Iterator<Resource> iter = repositoryRdfDescription
				.filter(null, RDF.TYPE, SimpleValueFactory.getInstance().createIRI(REPOSITORY_URI))
					.subjects()
					.iterator();

		Resource repositoryNode = null;
		if (iter.hasNext()) {
			repositoryNode = iter.next();
		}

		if (repositoryNode == null) {
			String error = "The turtle configuration file '" + repositoryConfigFileName
					+ "' does not contain a valid repository description, because it is missing a resource with rdf:type <"
					+ REPOSITORY_URI + ">";
			throw new EmfRuntimeException(error);
		}
		return repositoryNode;
	}

	private static Repository createAndInitializeRepository(Properties properties, String repositoryId,
			Model repositoryRdfDescription, Resource repositoryNode) {
		Repository repository;
		String repositoryType = (String) properties.get(REPOSITORY_TYPE);
		RepositoryManager repositoryManager;
		if (REPOSITORY_TYPE_REMOTE.equals(repositoryType)) {
			// Create a manager for the remote Sesame server and initialize
			// it
			repositoryManager = createRepositoryManager(properties);

			// check for existing repository and remove it
			repository = repositoryManager.getRepository(repositoryId);
			if (repository != null) {
				if (repository.isInitialized()) {
					repository.shutDown();
				}
				repositoryManager.removeRepository(repositoryId);
			}

			// create new repository
			RepositoryConfig repositoryConfig = RepositoryConfig.create(repositoryRdfDescription, repositoryNode);
			repositoryManager.addRepositoryConfig(repositoryConfig);

			repository = repositoryManager.getRepository(repositoryId);
		} else if (REPOSITORY_TYPE_LOCAL.equals(repositoryType)) {
			repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		} else {
			throw invalidRepositoryType(repositoryType);
		}

		repository.initialize();
		return repository;
	}

	private static EmfRuntimeException invalidRepositoryType(String repositoryType) {
		return new EmfRuntimeException("Invalid repository type: '" + repositoryType + "'");
	}

	private static void importSparqlFiles(Properties properties, RepositoryConnection connection) {
		String importFilesList = properties.getProperty(IMPORT_FILES);
		if (importFilesList != null && !importFilesList.isEmpty()) {
			String[] importFilesSplitted = importFilesList.split(";");

			for (String fileName : importFilesSplitted) {
				connection.begin();
				if (fileName.endsWith("sparql")) {
					RepositoryUtils.executeSparqlFile(connection, new ResourceSource(fileName));
				} else {
					RepositoryUtils.addFileToRepository(connection, new ResourceSource(fileName));
				}
				connection.commit();
			}
		}
	}

	/**
	 * Creates repository manager from the configuration properties
	 */
	private static RepositoryManager createRepositoryManager(Properties properties) {
		RepositoryManager repositoryManager;
		RemoteRepositoryManager remote = new RemoteRepositoryManager(properties.getProperty(SERVER_ADDRESS));

		String username = properties.getProperty(USERNAME);
		String password = properties.getProperty(CREDENTIAL);

		remote.setUsernameAndPassword(username, password);
		repositoryManager = remote;

		repositoryManager.initialize();
		return repositoryManager;
	}

	/**
	 * Refreshes the namespaces for all repositories on the server
	 */
	private static void refreshNamespacesOnAllRepositories(RepositoryManager repositoryManager) {
		for (String repositoryId : repositoryManager.getRepositoryIDs()) {
			if (!SYSTEM_REPOSITORY_ID.equals(repositoryId)) {
				Repository repository = repositoryManager.getRepository(repositoryId);
				RepositoryConnection connection = repository.getConnection();
				RepositoryUtils.refreshNamespaces(connection, DefaultNamespaces.ALL_NAMESPACES);
			}
		}
	}

	/**
	 * Reads the configuration for creator
	 *
	 * @return Properties of the repository for the creator
	 */
	private static Properties readConfigurationFromFile() {
		Properties props = new Properties();
		try {
			props.load(RepositoryCreator.class.getClassLoader().getResourceAsStream(
					REPOSITORY_CREATOR_MAIN_CONFIG_PROPERTIES));

			String configurationFileName = (String) props.get(REPOSITORY_CONFIG_FILE);
			String operation = (String) props.get(CREATOR_OPERATION);

			props = new Properties();
			props.load(RepositoryCreator.class.getClassLoader().getResourceAsStream(configurationFileName));
			props.put(CREATOR_OPERATION, operation);
			return props;
		} catch (IOException e) {
			throw new EmfRuntimeException("Error reading the configuration file!", e);
		}
	}

	/**
	 * Runs the Repository creator
	 *
	 * @param args
	 *            Startup Arguments
	 * @throws IOException
	 *             when failed to read the input files
	 */
	public static void main(String[] args) throws IOException {
		Properties properties = readConfigurationFromFile();
		String operation = properties.getProperty(CREATOR_OPERATION);
		if (StringUtils.isBlank(operation)) {
			operation = CREATOR_OPERATION_DROP_CREATE;
		}
		switch (operation) {
			case CREATOR_OPERATION_DROP_CREATE:
				executeDropCreateOperation(properties);
				break;
			case CREATOR_OPERATION_REFRESH_ALL_NAMESPACES:
				doRefreshAllNamespacesOperation(properties);
				break;
			case CREATOR_OPERATION_REFRESH_NAMESPACES:
				refreshNamespacesOperation(properties);
				break;
			case CREATOR_OPERATION_UPDATE:
				updateRepository(properties);
				break;
			case CREATOR_OPERATION_INFO:
				printRepositoryInformation(properties);
				break;
			default:
				throw new IllegalArgumentException("Wrong operation: " + operation);
		}
	}

	private static void refreshNamespacesOperation(Properties properties) {
		try {
			RepositoryManager repositoryManager = createRepositoryManager(properties);

			Repository repo = repositoryManager.getRepository(properties.getProperty(REPOSITORY_ID));

			RepositoryConnection connection = repo.getConnection();
			RepositoryUtils.refreshNamespaces(connection, DefaultNamespaces.ALL_NAMESPACES);
			connection.close();

		} catch (RepositoryException | RepositoryConfigException e) {
			throw new EmfRuntimeException(e);
		}
	}

	private static void doRefreshAllNamespacesOperation(Properties properties) {
		try {
			RepositoryManager repositoryManager = createRepositoryManager(properties);
			refreshNamespacesOnAllRepositories(repositoryManager);

			repositoryManager.shutDown();

		} catch (RepositoryException | RepositoryConfigException e) {
			throw new EmfRuntimeException(e);
		}
	}

	private static void executeDropCreateOperation(Properties properties) throws IOException {
		Repository repository = initializeRepository(properties);
		try {
			repository.shutDown();
		} catch (RepositoryException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Prints information about the repository
	 *
	 * @param properties
	 *            repository properties
	 */
	private static void printRepositoryInformation(Properties properties) {
		if (properties == null) {
			throw new IllegalArgumentException("Input paramets are NULL!");
		}

		String repositoryId = properties.getProperty(REPOSITORY_ID);
		try {
			String repositoryType = (String) properties.get(REPOSITORY_TYPE);
			RepositoryManager repositoryManager;
			if (REPOSITORY_TYPE_REMOTE.equals(repositoryType)) {
				// Create a manager for the remote Sesame server and initialize
				// it
				repositoryManager = createRepositoryManager(properties);
				RepositoryConfig repositoryConf = repositoryManager.getRepositoryConfig(repositoryId);

				LOGGER.info(repositoryConf.toString());

			} else {
				throw invalidRepositoryType(repositoryType);
			}
		} catch (RepositoryException e) {
			throw onFailedConnectionToSesameServer(e);
		} catch (RepositoryConfigException e) {
			throw onUnableToProcessRepositoryConfiguration(e);
		}
	}

	/**
	 * Updates the ontologies of the repository from the configuration properties
	 *
	 * @param properties
	 *            Configuration properties
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private static void updateRepository(Properties properties) {

		if (properties == null) {
			throw new IllegalArgumentException("Input paramets are NULL!");
		}

		LOGGER.info("Updating repository with properties: " + properties);

		String repositoryId = properties.getProperty(REPOSITORY_ID);
		RepositoryManager repositoryManager = null;
		try {
			Repository repository;
			String repositoryType = (String) properties.get(REPOSITORY_TYPE);
			if (REPOSITORY_TYPE_REMOTE.equals(repositoryType)) {
				// Create a manager for the remote Sesame server and initialize
				// it
				repositoryManager = createRepositoryManager(properties);
				repository = repositoryManager.getRepository(repositoryId);
			} else if (REPOSITORY_TYPE_LOCAL.equals(repositoryType)) {
				repository = new SailRepository(new MemoryStore());
			} else {
				throw new IllegalArgumentException("Invalid repository type: '" + repositoryType + "'");
			}

			repository.initialize();
			RepositoryConnection connection = repository.getConnection();
			RepositoryUtils.refreshNamespaces(connection, DefaultNamespaces.ALL_NAMESPACES);

			// import files
			importFiles(properties, connection);
		} catch (RepositoryException e) {
			throw onFailedConnectionToSesameServer(e);
		} catch (RepositoryConfigException e) {
			throw onUnableToProcessRepositoryConfiguration(e);
		} finally {
			if (repositoryManager != null) {
				repositoryManager.shutDown();
			}
		}
	}

	private static EmfRuntimeException onFailedConnectionToSesameServer(RepositoryException e) {
		String error = "Unable to establish a connection with the Sesame server 'url': " + e.getMessage();
		return new EmfRuntimeException(error, e);
	}

	private static void importFiles(Properties properties, RepositoryConnection connection) {
		String importFilesList = properties.getProperty(IMPORT_FILES);
		if (importFilesList == null || importFilesList.isEmpty()) {
			return;
		}
		String[] importFilesSplitted = importFilesList.split(";");

		connection.begin();
		for (String fileName : importFilesSplitted) {
			if (fileName.endsWith("sparql")) {
				RepositoryUtils.executeSparqlFile(connection, new ResourceSource(fileName));
			} else {
				RepositoryUtils.addFileToRepository(connection, new ResourceSource(fileName));
			}
		}

		connection.commit();
		connection.close();
	}

	/**
	 * Create local repository
	 *
	 * @return Local repository
	 * @throws IOException
	 *             if failed to read the input files
	 */
	public static Repository createLocalRepository() throws IOException {
		Properties properties = new Properties();

		properties.put(REPOSITORY_ID, "test");
		properties.put(REPOSITORY_LABEL, "label");
		properties.put(REPOSITORY_TYPE, REPOSITORY_TYPE_LOCAL);
		properties.put(REPOSITORY_CONFIG_FILE, "repository_configurations/owlim-se.ttl");
		properties.put(IMPORT_FILES,
				Stream
						.of("patches/ontologies/Proton/protontop.ttl", "patches/ontologies/SKOS/skos.rdf",
								"patches/ontologies/EMFDomainModel/emf.ttl", "patches/ontologies/DC/dcterms.rdf",
								"patches/ontologies/TCRM/tcrm.ttl", "patches/ontologies/connector/connectors.ttl",
								"patches/ontologies/connector/default_connector_instance.trig")
							.collect(Collectors.joining(";")));

		return initializeRepository(properties);
	}

}
