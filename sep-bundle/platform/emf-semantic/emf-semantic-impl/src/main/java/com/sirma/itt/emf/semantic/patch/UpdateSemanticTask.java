package com.sirma.itt.emf.semantic.patch;

import java.io.InputStream;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.repository.creator.RepositoryCreator;
import com.sirma.itt.emf.semantic.repository.creator.RepositoryCreatorUtils;
import com.sirma.itt.emf.util.CDI;
import com.sirma.itt.semantic.ConnectionFactory;

/**
 * Using customChange tasks the ontology is updated. The class that executes the task is
 * com.sirma.itt.emf.semantic.patch.UpdateSemanticTask. it has two parameters: <br/>
 * - fileName - List of files to be executed on the repository separated with ';' <br/>
 * - fileType - turtle or sparql - type of the files
 *
 * @author kirq4e
 */
public class UpdateSemanticTask implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSemanticTask.class);

	public static final String FILE_TYPE_RDF = "RDF";
	public static final String FILE_TYPE_SPARQL = "SPARQL";

	private String fileName;
	private String fileType;

	private ResourceAccessor resourceAccessor;

	private ConnectionFactory connectionFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConfirmationMessage() {
		return "Successfully executed changes from files: " + fileName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUp() throws SetupException {
		connectionFactory = CDI.instantiateDefaultBean(ConnectionFactory.class,
			    CDI.lookupBeanManager());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		LOGGER.info("Processing files: {} with type: {}", fileName, fileType);

		RepositoryConnection repositoryConnection = null;
		try {
			repositoryConnection = connectionFactory.produceConnection();
			RepositoryCreator.refreshNamespaces(repositoryConnection);

			String[] importFiles = fileName.split("\\s*;\\s*");

			for (String name : importFiles) {
				LOGGER.info("Importing file: {}", name);

				if (FILE_TYPE_RDF.equals(fileType)) {
					RepositoryCreatorUtils.addFileToRepository(repositoryConnection, name);
				} else if (FILE_TYPE_SPARQL.equals(fileType)) {
					// execute sparql update file
					InputStream file = null;
					try {

						file = resourceAccessor.getResourceAsStream(name);
						if (file != null) {
							RepositoryCreatorUtils.executeSparqlFile(repositoryConnection, file);
						}
					} finally {
						if (file != null) {
							file.close();
						}
					}
				}
			}

			connectionFactory.disposeConnection(repositoryConnection);
		} catch (Exception e) {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.rollback();
				} catch (RepositoryException e1) {
					throw new CustomChangeException("Unable to rollback changes because of: "
							+ e.getMessage(), e1);
				}
			}

			throw new CustomChangeException("Unable to update semantic repository because of: "
					+ e.getMessage(), e);
		}
		LOGGER.info("Finished processing files: {} ", fileName);
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

}
