package com.sirma.itt.emf.semantic.repository.creator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.io.ResourceSource;
import com.sirma.itt.semantic.model.vocabulary.EMF;

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
	 *            {@link RDFParser#setRDFHandler(org.eclipse.rdf4j.rio.RDFHandler)} should be called with non null
	 *            value. If <code>null</code> is passed then the default {@link StatementCollector} will be used
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
	 *            {@link RDFParser#setRDFHandler(org.eclipse.rdf4j.rio.RDFHandler)} should be called with non null
	 *            value. If <code>null</code> is passed then the default {@link StatementCollector} will be used
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
		if (StringUtils.isBlank(defaultNamespaceLocal)) {
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
		RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForFileName(resource.getName()).orElse(
				RDFFormat.TURTLE);
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

	@SuppressWarnings("boxing")
	private static Model writeModel(RepositoryConnection connection, Model importedModel) throws RepositoryException {
		Set<Pair<Resource, Boolean>> graphs = extractContextsFromModel(importedModel);
		if (graphs.isEmpty()) {
			LOGGER.info("Importing {} statements from model into Data Graph", importedModel.size());
			connection.add(importedModel, EMF.DATA_CONTEXT);
			return importedModel;
		}

		for (Pair<Resource, Boolean> context : graphs) {
			Resource contextIRI = context.getFirst();

			Model newStatements = new LinkedHashModel();
			Model removeStatements = new LinkedHashModel();
			// if the context is Ontology - compare the imported model with the ontology in the repository
			if (context.getSecond()) {
				Model originalOntology = loadGraph(connection, contextIRI);
				compareOntologies(originalOntology, importedModel, newStatements, removeStatements);
			} else if (EMF.REMOVE_GRAPH.equals(contextIRI)) {
				// remove all statements in the remove graph
				for (Statement statement : importedModel.filter(null, null, null, contextIRI)) {

					// strip the statement from the context
					removeStatements.add(statement.getSubject(), statement.getPredicate(), statement.getObject());
				}
			} else {
				// import all new statements that are not in the remove graph (and not in the repository) to the
				// specified Graph
				for (Statement statement : importedModel.filter(null, null, null, contextIRI)) {
					if (!connection.hasStatement(statement, false, statement.getContext())) {
						newStatements.add(statement);
					}
				}
			}

			// skip adding modified if the graph is not modified
			if (!EMF.REMOVE_GRAPH.equals(contextIRI) && (!newStatements.isEmpty() || !removeStatements.isEmpty())) {
				ValueFactory valueFactory = connection.getValueFactory();
				newStatements.add(contextIRI, DCTERMS.MODIFIED, valueFactory.createLiteral(new Date()),
						(Resource) null);
			}

			LOGGER.info("Updating graph <{}> with {} new statements and {} statements for delete", context.getFirst(),
					newStatements.size(), removeStatements.size());

			if (!removeStatements.isEmpty()) {
				LOGGER.debug("Removing statements from ontology <{}>: {}", context, removeStatements);

				for (Statement statement : removeStatements.filter(null, null, null)) {
					// if the predicate is emf:removeAll then remove all statements for this subject
					IRI predicate = null;
					Value object = null;
					if (!EMF.REMOVE_ALL.equals(statement.getPredicate())) {
						predicate = statement.getPredicate();
						// if the object is set to emf:removeAll remove all statements with this subject and predicate
						if (!EMF.REMOVE_ALL.equals(statement.getObject())) {
							object = statement.getObject();
						}
					}
					connection.remove(statement.getSubject(), predicate, object);
				}
			}
			if (!newStatements.isEmpty()) {
				LOGGER.debug("Importing statements to ontology <{}>: {}", context, newStatements);
				connection.add(newStatements, contextIRI);
			}
		}

		return importedModel;
	}

	private static Set<Pair<Resource, Boolean>> extractContextsFromModel(Model importedModel) {
		Set<Pair<Resource, Boolean>> contexts = new HashSet<>();

		for (Resource context : importedModel.contexts()) {
			if (context != null) {
				Model ontologies = importedModel.filter(null, RDF.TYPE, OWL.ONTOLOGY, context);
				if (!ontologies.subjects().isEmpty() && !EMF.REMOVE_GRAPH.equals(context)) {
					for (Resource ontology : ontologies.subjects()) {
						contexts.add(new Pair<>(ontology, Boolean.TRUE));
					}
				} else {
					contexts.add(new Pair<>(context, Boolean.FALSE));
				}
			}
		}

		if (contexts.isEmpty()) {
			Model ontologies = importedModel.filter(null, RDF.TYPE, OWL.ONTOLOGY, (Resource) null);
			for (Resource subject : ontologies.subjects()) {
				contexts.add(new Pair<>(subject, Boolean.TRUE));
			}
		}

		return contexts;
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
	public static void compareOntologies(Model originalOntology, Model comparedOntology, Model newStatements,
			Model removedStatements) {
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
			sparqlQuery = IOUtils.toString(modelStream, StandardCharsets.UTF_8);

			if (StringUtils.isNotBlank(sparqlQuery)) {
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

	public static String escapeRepositoryName(String text) {
		return text.replaceAll("[\\.-]+", "_");
	}

}
