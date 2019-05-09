package com.sirma.itt.emf.semantic.patch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.semantic.repository.creator.RepositoryUtils;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.io.ResourceSource;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.namespaces.DefaultNamespaces;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Using customChange tasks the ontology is updated. The class that executes the task is
 * com.sirma.itt.emf.semantic.patch.UpdateSemanticTask. it has two parameters: <br/>
 * - fileName - List of files to be executed on the repository separated with ';' <br/>
 * - fileType - turtle, sparql or namespaces- type of the files
 *
 * @author kirq4e
 */
public class UpdateSemanticTask implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSemanticTask.class);

	public static final String FILE_TYPE_RDF = "RDF";
	public static final String FILE_TYPE_SPARQL = "SPARQL";
	public static final String FILE_TYPE_NAMESPACES = "NAMESPACES";

	public static final String NAMESPACES_FILE_EXTENSION = "." + UpdateSemanticTask.FILE_TYPE_NAMESPACES.toLowerCase();
	public static final String NS_FILE_EXTENSION = ".ns";
	public static final String SPARQL_FILE_EXTENSION = "." + UpdateSemanticTask.FILE_TYPE_SPARQL.toLowerCase();

	private static final String URI_SEPARATOR_REGEX = "\\s*\\|\\s*";
	private static final Pattern NAMESPACE_SEPARATOR_PATTERN = Pattern.compile(URI_SEPARATOR_REGEX);
	private static final String COMMENT_CHARACTER = "#";

	private String fileName;
	private String fileType;
	private boolean includeInferred = true;

	protected ConnectionFactory connectionFactory;

	@Override
	public String getConfirmationMessage() {
		return "Successfully executed changes from files: " + fileName;
	}

	@Override
	public void setUp() throws SetupException {
		connectionFactory = CDI.instantiateDefaultBean(ConnectionFactory.class, CDI.getCachedBeanManager());
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// implements method from interface
	}

	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		LOGGER.info("Processing files: {} with type: {}", fileName, fileType);

		String[] importFiles = fileName.split("\\s*;\\s*");
		RepositoryConnection repositoryConnection = connectionFactory.produceConnection();
		try {
			for (String file : importFiles) {
				UpdateSemanticTask.loadFileToSemanticDatabase(repositoryConnection, file, fileType, includeInferred);
			}
		} catch (RepositoryException | IOException e) {
			UpdateSemanticTask.rollbackConnection(repositoryConnection, e);
			throw new CustomChangeException("Unable to update semantic repository because of: " + e.getMessage(), e);
		} finally {
			if (repositoryConnection != null) {
				connectionFactory.disposeConnection(repositoryConnection);
			}
		}
		LOGGER.info("Finished processing files: {} ", fileName);
	}

	/**
	 * Rollback connection.
	 *
	 * @param repositoryConnection
	 *            the repository connection
	 * @param e
	 *            the e
	 * @throws CustomChangeException
	 *             the custom change exception
	 */
	protected static void rollbackConnection(RepositoryConnection repositoryConnection, Exception e)
			throws CustomChangeException {
		if (repositoryConnection != null) {
			try {
				repositoryConnection.rollback();
			} catch (RepositoryException e1) {
				throw new CustomChangeException("Unable to rollback changes because of: " + e.getMessage(), e1);
			}
		}
	}

	/**
	 * Load file to semantic database.
	 *
	 * @param repositoryConnection
	 *            the repository connection
	 * @param name
	 *            the name of the imported resource. Could be absolute path file or classpath resource
	 * @param type
	 *            the type of the resource
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CustomChangeException
	 *             If the file is missing
	 */
	protected static void loadFileToSemanticDatabase(RepositoryConnection repositoryConnection, String name,
			String type, boolean inferred) throws IOException, CustomChangeException {
		ResourceSource resource = new ResourceSource(name);

		if (!resource.isAccessible()) {
			throw new FileNotFoundException("Couldn't import file: " + name + " because it is missing!");
		}
		LOGGER.info("Importing file: {}", resource.getName());
		if (FILE_TYPE_RDF.equals(type)) {
			RepositoryUtils.addFileToRepository(repositoryConnection, resource);
		} else if (FILE_TYPE_SPARQL.equals(type)) {
			// execute sparql update file
			RepositoryUtils.executeSparqlFile(repositoryConnection, resource, inferred);
		} else if (FILE_TYPE_NAMESPACES.equals(type)) {
			refreshNamespaces(repositoryConnection, resource);
		}
	}

	/**
	 * Initalizes the namespaces of the repository. If a file is passed then the namespaces are read from the file and
	 * adds them to the default namespaces. If no file is passed then only the default namespaces are inserted
	 * 
	 * @throws RepositoryException
	 *             If the repository is not writable or there is no connection
	 * @throws CustomChangeException
	 *             If an error occurs while reading the namespaces from the file
	 */
	protected static void refreshNamespaces(RepositoryConnection repositoryConnection, ResourceSource resource)
			throws CustomChangeException {
		Set<StringPair> namespaces = new HashSet<>();
		namespaces.addAll(DefaultNamespaces.DEFAULT_NAMESPACES);
		namespaces.addAll(readNamespacesFromFile(resource));
		RepositoryUtils.refreshNamespaces(repositoryConnection, namespaces);
	}

	private static Set<StringPair> readNamespacesFromFile(ResourceSource resource) throws CustomChangeException {
		Set<StringPair> namespaces = new HashSet<>();
		try {
			List<String> lines = IOUtils.readLines(resource.load(), StandardCharsets.UTF_8);
			for (String line : lines) {
				if (StringUtils.isNotBlank(line) && !line.startsWith(COMMENT_CHARACTER)) {
					String[] namespacePair = NAMESPACE_SEPARATOR_PATTERN.split(line, 2);
					namespaces.add(new StringPair(namespacePair[0].trim(), namespacePair[1].trim()));
				}
			}
			return namespaces;
		} catch (IOException e) {
			throw new CustomChangeException("Failed to read namespaces file due to: " + e.getMessage(), e);
		}
	}

	/**
	 * Getter method for fileName.
	 *
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Setter method for fileName.
	 *
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Getter method for fileType.
	 *
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * Setter method for fileType.
	 *
	 * @param fileType
	 *            the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the inferred
	 */
	public boolean getIncludeInferred() {
		return includeInferred;
	}

	/**
	 * @param inferred
	 *            the inferred to set
	 */
	public void setIncludeInferred(boolean inferred) {
		this.includeInferred = inferred;
	}

}
