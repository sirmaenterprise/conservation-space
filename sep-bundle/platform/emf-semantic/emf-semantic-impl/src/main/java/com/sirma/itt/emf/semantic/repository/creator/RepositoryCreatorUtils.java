package com.sirma.itt.emf.semantic.repository.creator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * @author kirq4e
 */
public class RepositoryCreatorUtils {

	public static final String DEFAULT_NAMESPACE = "http://example.org#";

	/**
	 * Parse the given RDF file and return the contents as a Graph
	 *
	 * @param file
	 *            The file containing the RDF data
	 * @param format
	 *            format
	 * @param defaultNamespace
	 *            default namespace
	 * @return The contents of the file as an RDF graph
	 */
	public static Model parseRDFFile(InputStream file, RDFFormat format, String defaultNamespace) {

		if (file == null || format == null) {
			throw new EmfConfigurationException("Invalid input parameters " + file + " " + format + "!");
		}

		if (StringUtils.isNullOrEmpty(defaultNamespace)) {
			defaultNamespace = DEFAULT_NAMESPACE;
		}

		final Model model = new LinkedHashModel();
		RDFParser parser = Rio.createParser(format);
		parser.setRDFHandler(new StatementCollector(model));

		try {
			parser.parse(file, defaultNamespace);
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new EmfConfigurationException("Error parsing the file", e);
		}
		return model;
	}

	/**
	 * Parses file with ontology and returns the statements as a model. The file that is imported
	 * must be in the classpath
	 *
	 * @param fileName
	 *            File name
	 * @return Model with statements from the file
	 */
	public static Model parseFile(String fileName) {
		RDFFormat format = RDFFormat.forFileName(fileName);
		InputStream inputStream = RepositoryCreatorUtils.class.getClassLoader()
				.getResourceAsStream(fileName);
		return parseRDFFile(inputStream, format, null);
	}

	/**
	 * Import the file into the repository. If the file contains an ontology then it is imported to
	 * new context with URI of the ontology
	 *
	 * @param connection
	 *            Connection to the repository
	 * @param fileName
	 *            name of the file
	 * @return The model that is added to the repository
	 * @throws RepositoryException
	 *             If an error occurs then an exception is thrown
	 */
	public static Model addFileToRepository(RepositoryConnection connection, String fileName)
			throws RepositoryException {
		Model importedFile = parseFile(fileName);

		Model ontology = importedFile.filter(null, RDF.TYPE, OWL.ONTOLOGY, (Resource) null);
		Resource context = null;
		if (ontology.subjects().iterator().hasNext()) {
			context = ontology.subjects().iterator().next();
			connection.clear(context);

			importedFile.add(context, DCTERMS.MODIFIED, connection.getValueFactory().createLiteral(new Date()), (Resource) null);

			connection.add(importedFile, context);
		} else {
			connection.add(importedFile, EMF.DATA_CONTEXT);
		}

		return importedFile;
	}

	/**
	 * Executes file with update SPARQL query to the repository
	 *
	 * @param repositoryConnection
	 *            Connection to the repository
	 * @param file
	 *            Input stream to read the file content
	 */
	public static void executeSparqlFile(RepositoryConnection repositoryConnection, InputStream file) {
		if (file != null) {
			String sparqlQuery;
			try {
				sparqlQuery = IOUtils.toString(file);

				if (StringUtils.isNotNullOrEmpty(sparqlQuery)) {
					Update updateQuery = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL,
							sparqlQuery);
					updateQuery.execute();
				}
			} catch (IOException | UpdateExecutionException | RepositoryException
					| MalformedQueryException e) {
				throw new EmfConfigurationException("Error executing update file", e);
			}
		}
	}

}
