package com.sirma.itt.emf.semantic.patch.validation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParserFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.ParseErrorCollector;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.patch.SemanticModelPatch;
import com.sirma.itt.emf.semantic.patch.SemanticSchemaPatches;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * Semantic changelog patches validator. Validates all patches defined in the semantic changelog. It validates SPARQL
 * Update queries and RDF Files
 * 
 * @author kirq4e
 */
public class SemanticChangelogValidator {

	private static final String FILE_NAME = "fileName";
	private static final String FILE_TYPE = "fileType";
	private static final String SPARQL = "SPARQL";
	private static final String RDF = "RDF";

	private SemanticChangelogValidator() {
	}

	/**
	 * Validates all patches in the defined changelog file. The broken changelog id will be in the exception message
	 * message
	 * 
	 * @param path
	 *            Semantic Changelog provider {@link SemanticSchemaPatches}
	 * @throws SemanticPersistenceException
	 *             If there is an error with in some of the patches
	 */
	public static void validateChangelog(SemanticSchemaPatches changelogProvider) {
		Objects.requireNonNull(changelogProvider, "Input parameter 'path' must not be NULL!");
		String path = changelogProvider.getPath();

		try {
			ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
			ChangeLogParser changeLogParser = ChangeLogParserFactory.getInstance().getParser(path, resourceAccessor);
			DatabaseChangeLog databaseChangeLog = changeLogParser.parse(path, null, resourceAccessor);

			for (ChangeSet changeSet : databaseChangeLog.getChangeSets()) {
				validateChangeSet(changeSet);
			}
		} catch (IOException | LiquibaseException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	private static void validateChangeSet(ChangeSet changeSet) throws IOException {
		try {
			for (Change change : changeSet.getChanges()) {
				Map<String, String> params = (Map<String, String>) change.getSerializableFieldValue("param");
				if (!params.containsKey(FILE_NAME)) {
					continue;
				}
				String fileName = params.get(FILE_NAME);
				String fileType = params.get(FILE_TYPE);
				String[] importFiles = fileName.split("\\s*;\\s*");
				for (String file : importFiles) {
					if (RDF.equals(fileType)) {
						RDFFormat fileFormat = Rio.getParserFormatForFileName(file).orElseThrow(
								() -> new SemanticPersistenceException(
										changeSet.getId() + ": Unsupported file format for file: " + file));
						RDFParser parser = Rio.createParser(fileFormat);
						try (InputStream input = SemanticModelPatch.class.getClassLoader().getResourceAsStream(file)) {
							ParseErrorCollector parseErrorCollector = new ParseErrorCollector();
							parser.setParseErrorListener(parseErrorCollector);
							parser.parse(input, "http://test/test");
						}
					} else if (SPARQL.equals(fileType)) {
						SPARQLParserFactory factory = new SPARQLParserFactory();

						String query = FileUtils.readFileToString(
								new File(SemanticModelPatch.class.getClassLoader().getResource(file).getPath()));
						factory.getParser().parseUpdate(query, "http://test/test");
					} else if ("NAMESPACES".equals(fileType)) {
						// TODO Add namespace validator
					} else {
						throw new SemanticPersistenceException(
								changeSet.getId() + ": Unsupported patch type: " + fileType);
					}
				}
			}
		} catch (MalformedQueryException | RDFParseException e) {
			throw new SemanticPersistenceException(
					changeSet.getId() + ": Unable to validate patch, because of: " + e.getMessage());
		}
	}
}
