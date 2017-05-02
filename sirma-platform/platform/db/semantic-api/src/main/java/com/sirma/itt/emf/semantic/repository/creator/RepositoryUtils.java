package com.sirma.itt.emf.semantic.repository.creator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
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
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.io.ResourceSource;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import info.aduna.iteration.Iterations;

/**
 * @author kirq4e
 */
public class RepositoryUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String ERROR_EXECUTING_UPDATE_FILE = "Error executing update file ";

	/**
	 * Util class and should not be instantiated
	 */
	private RepositoryUtils() {
		// utility class
	}

	public static final String DEFAULT_NAMESPACE = "http://example.org#";

	/**
	 * Parse the given RDF file and return the contents as a Graph
	 *
	 * @param modelStream
	 *            the stream containing the RDF data
	 * @param format
	 *            format
	 * @param defaultNamespace
	 *            default namespace
	 * @return The contents of the file as an RDF graph
	 */
	public static Model parseRDFFile(InputStream modelStream, RDFFormat format, String defaultNamespace) {
		return parseRDFFile(modelStream, format, defaultNamespace, null);
	}

	/**
	 * Parse the given RDF file and return the contents as a Graph.
	 *
	 * @param modelStream
	 *            the stream containing the RDF data
	 * @param format
	 *            format
	 * @param defaultNamespace
	 *            default namespace
	 * @param parserConfigurator
	 *            the parser configurator that will be able to configure the parser before parsing of the input data.
	 *            Note that when using this parameter at least
	 *            {@link RDFParser#setRDFHandler(org.openrdf.rio.RDFHandler)} should be called with non null value. If
	 *            <code>null</code> is passed then the default {@link StatementCollector} will be used
	 * @return The contents of the file as an RDF graph
	 */
	public static Model parseRDFFile(InputStream modelStream, RDFFormat format, String defaultNamespace,
			Consumer<RDFParser> parserConfigurator) {
		if (modelStream == null || format == null) {
			throw new EmfConfigurationException("Invalid input parameters " + modelStream + " " + format + "!");
		}

		final Model model = new LinkedHashModel();
		RDFParser parser = prepareParser(format, model, parserConfigurator);

		try {
			parser.parse(modelStream, getDefaultNamespace(defaultNamespace));
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new EmfConfigurationException("Error parsing the file", e);
		}
		return model;
	}

	/**
	 * Parse the given RDF file as Reader and return the contents as a Graph
	 *
	 * @param modelReader
	 *            the reader containing the RDF data
	 * @param format
	 *            format
	 * @param defaultNamespace
	 *            default namespace
	 * @return The contents of the file as an RDF graph
	 */
	public static Model parseRDFFile(Reader modelReader, RDFFormat format, String defaultNamespace) {
		return parseRDFFile(modelReader, format, defaultNamespace, null);
	}

	/**
	 * Parse the given RDF file as Reader and return the contents as a Graph
	 *
	 * @param modelReader
	 *            the reader containing the RDF data
	 * @param format
	 *            format
	 * @param defaultNamespace
	 *            default namespace
	 * @param parserConfigurator
	 *            the parser configurator that will be able to configure the parser before parsing of the input data.
	 *            Note that when using this parameter at least
	 *            {@link RDFParser#setRDFHandler(org.openrdf.rio.RDFHandler)} should be called with non null value. If
	 *            <code>null</code> is passed then the default {@link StatementCollector} will be used
	 * @return The contents of the file as an RDF graph
	 */
	public static Model parseRDFFile(Reader modelReader, RDFFormat format, String defaultNamespace,
			Consumer<RDFParser> parserConfigurator) {
		if (modelReader == null || format == null) {
			throw new EmfConfigurationException("Invalid input parameters " + modelReader + " " + format + "!");
		}

		final Model model = new LinkedHashModel();
		RDFParser parser = prepareParser(format, model, parserConfigurator);

		try {
			parser.parse(modelReader, getDefaultNamespace(defaultNamespace));
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new EmfConfigurationException("Error parsing the file", e);
		}
		return model;
	}

	private static RDFParser prepareParser(RDFFormat format, final Model model,
			Consumer<RDFParser> parserConfigurator) {
		RDFParser parser = Rio.createParser(format);
		if (parserConfigurator != null) {
			parserConfigurator.accept(parser);
		} else {
			parser.setRDFHandler(new StatementCollector(model));
		}
		return parser;
	}

	private static String getDefaultNamespace(String defaultNamespace) {
		String defaultNamespaceLocal = defaultNamespace;
		if (StringUtils.isNullOrEmpty(defaultNamespaceLocal)) {
			defaultNamespaceLocal = DEFAULT_NAMESPACE;
		}
		return defaultNamespaceLocal;
	}

	/**
	 * Parses file with ontology and returns the statements as a model. The file that is imported must be in the
	 * classpath
	 *
	 * @param resource
	 *            the imported file as descriptor. Might be absolute or relative to be search internally in the
	 *            classpath
	 * @return Model with statements from the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Model parseFile(ResourceSource resource) throws IOException {
		RDFFormat format = RDFFormat.forFileName(resource.getName());
		try (InputStream inputStream = resource.load()) {
			return parseRDFFile(new InputStreamReader(inputStream, StandardCharsets.UTF_8), format, null);
		}
	}

	/**
	 * Parses string with ontology and returns the statements as a model
	 *
	 * @param statements
	 *            The string representation of a file with ontologies
	 * @param format
	 *            The format of the string representation of the ontology
	 * @return Model with statements from the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Model parseString(String statements, RDFFormat format) {
		return parseRDFFile(new StringReader(statements), format, null);
	}

	/**
	 * Import the file into the repository. If the file contains an ontology then it is imported to new context with URI
	 * of the ontology
	 *
	 * @param connection
	 *            Connection to the repository
	 * @param resource
	 *            the resource descriptor
	 * @return The model that is added to the repository
	 * @throws RepositoryException
	 *             If an error occurs then an exception is thrown
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Model addFileToRepository(RepositoryConnection connection, ResourceSource resource) {
		Model importedModel;
		try {
			importedModel = parseFile(resource);

			return writeModel(connection, importedModel);
		} catch (IOException | RepositoryException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	private static Model writeModel(RepositoryConnection connection, Model importedModel) throws RepositoryException {
		Model ontology = importedModel.filter(null, RDF.TYPE, OWL.ONTOLOGY, (Resource) null);
		Resource context = null;
		if (ontology.subjects().iterator().hasNext()) {
			context = ontology.subjects().iterator().next();
			Model originalOntology = loadGraph(connection, context);

			List<Statement> newStatements = new ArrayList<>();
			List<Statement> removeStatements = new ArrayList<>();
			compareOntologies(originalOntology, importedModel, newStatements, removeStatements);

			ValueFactory valueFactory = connection.getValueFactory();
			newStatements.add(valueFactory.createStatement(context, DCTERMS.MODIFIED,
					valueFactory.createLiteral(new Date()), (Resource) null));

			LOGGER.info("Updating ontology <{}> with {} new statements and {} statements for delete", context,
					newStatements.size(), removeStatements.size());

			if (!removeStatements.isEmpty()) {
				LOGGER.debug("Removing statements from ontology <{}>: {}", context, removeStatements);
				connection.remove(removeStatements, context);
			}
			if (!newStatements.isEmpty()) {
				LOGGER.debug("Importing statements to ontology <{}>: {}", context, newStatements);
				connection.add(newStatements, context);
			}
		} else {
			LOGGER.info("Importing {} statements from model into Data Graph", importedModel.size());
			connection.add(importedModel, EMF.DATA_CONTEXT);
		}
		return importedModel;
	}

	/**
	 * Load existing Graph from a Semantic repository to Model
	 * 
	 * @param connection
	 *            Connection to the Semantic repository
	 * @param graphURI
	 *            URI of the graph
	 * @return Model with all statements from the graph
	 */
	public static Model loadGraph(RepositoryConnection connection, Resource graphURI) {
		try {
			Model graphModel = new LinkedHashModel();
			connection.export(new StatementCollector(graphModel), graphURI);

			return graphModel;
		} catch (RepositoryException | RDFHandlerException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	/**
	 * Compares two models for differences. The new statements are added to the newStatements list and the statements to
	 * be removed to removedStatements list.
	 * 
	 * @param originalOntology
	 *            Original ontology to compare with
	 * @param comparedOntology
	 *            New ontology with changes
	 * @param newStatements
	 *            Statements to be added to the new ontology
	 * @param removedStatements
	 *            Statements to be removed from the original ontology
	 */
	public static void compareOntologies(Model originalOntology, Model comparedOntology, List<Statement> newStatements,
			List<Statement> removedStatements) {
		for (Statement statement : originalOntology) {
			if (!comparedOntology.contains(statement.getSubject(), statement.getPredicate(), statement.getObject())) {
				removedStatements.add(statement);
			} else {
				comparedOntology.remove(statement.getSubject(), statement.getPredicate(), statement.getObject());
			}
		}

		if (!comparedOntology.isEmpty()) {
			newStatements.addAll(comparedOntology);
		}
	}

	/**
	 * Executes file with update SPARQL query to the repository
	 *
	 * @param repositoryConnection
	 *            Connection to the repository
	 * @param resource
	 *            the resource descriptor for the file that will be executed
	 */
	public static void executeSparqlFile(RepositoryConnection repositoryConnection, ResourceSource resource) {
		executeSparqlFile(repositoryConnection, resource, true);
	}

	/**
	 * Executes file with update SPARQL query to the repository and specify if the query should include inferred
	 * statements
	 *
	 * @param repositoryConnection
	 *            Connection to the repository
	 * @param resource
	 *            the resource descriptor for the file that will be executed
	 * @param isInferred
	 *            Include inferred statements
	 */
	public static void executeSparqlFile(RepositoryConnection repositoryConnection, ResourceSource resource,
			boolean isInferred) {
		try (InputStream inputStream = resource.load()) {
			executeSparqlFile(repositoryConnection, inputStream, isInferred);
		} catch (IOException e) {
			throw new EmfConfigurationException(ERROR_EXECUTING_UPDATE_FILE + resource, e);
		}
	}

	/**
	 * Executes file with update SPARQL query to the repository
	 *
	 * @param repositoryConnection
	 *            Connection to the repository
	 * @param modelStream
	 *            Input stream to read the file content for SPARQL query
	 */
	public static void executeSparqlFile(RepositoryConnection repositoryConnection, InputStream modelStream) {
		executeSparqlFile(repositoryConnection, modelStream, true);
	}

	/**
	 * Executes file with update SPARQL query to the repository and specify if the query should include inferred
	 * statements
	 *
	 * @param repositoryConnection
	 *            Connection to the repository
	 * @param modelStream
	 *            Input stream to read the file content for SPARQL query
	 * @param isInferred
	 *            Include inferred statements
	 */
	public static void executeSparqlFile(RepositoryConnection repositoryConnection, InputStream modelStream,
			boolean isInferred) {
		if (modelStream == null) {
			return;
		}
		String sparqlQuery;
		try {
			sparqlQuery = IOUtils.toString(modelStream);

			if (StringUtils.isNotNullOrEmpty(sparqlQuery)) {
				Update updateQuery = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, sparqlQuery);
				updateQuery.setIncludeInferred(isInferred);
				
				updateQuery.execute();
			}
		} catch (IOException | UpdateExecutionException | RepositoryException | MalformedQueryException e) {
			throw new EmfConfigurationException("Error executing update file", e);
		}
	}

	/**
	 * Refreshes the namespaces in the repository which the connection is pointed to
	 *
	 * @param connection
	 *            Connection to the repository
	 * @param namespaces
	 *            Set of namespaces to init
	 * @throws RepositoryException
	 *             If an error occurs with the connection to the repositoru
	 */
	public static void refreshNamespaces(RepositoryConnection connection, Set<StringPair> namespaces)
			throws RepositoryException {
		RepositoryResult<Namespace> existingNamespaces = connection.getNamespaces();
		Set<Namespace> nsSet = Iterations.asSet(existingNamespaces);

		for (StringPair namespace : namespaces) {
			// remove existing namespaces with different prefixes
			for (Namespace ns : nsSet) {
				if (ns.getName().equals(namespace.getSecond())) {
					connection.removeNamespace(ns.getPrefix());
				}
			}
			// add namespace and its prefix
			connection.setNamespace(namespace.getFirst(), namespace.getSecond());
		}
	}

}
