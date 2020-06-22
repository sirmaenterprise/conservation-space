package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Provides logic for operating with semantic repository for classes that initializes some data in the repository
 *
 * @author kirq4e
 */
public class SemanticPersistenceHelper {

	private static final String FAILED_ERROR_MESSAGE = "Failed to {} properties due to {}";
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticPersistenceHelper.class);

	private SemanticPersistenceHelper() {
	}

	/**
	 * Saves all statements from the given model to the semantic repository.
	 *
	 * @param connection
	 *            Connection to the semantic repository
	 * @param model
	 *            Model with semantic statements
	 * @param graph
	 *            Graph to insert the statements from the model
	 */
	public static void saveModel(RepositoryConnection connection, Model model, IRI graph) {
		try {
			if (CollectionUtils.isNotEmpty(model)) {
				connection.add(model, graph);
			}
		} catch (RepositoryException e) {
			LOGGER.error(FAILED_ERROR_MESSAGE, "save", e.getMessage(), e);
			throw new SemanticPersistenceException(e);
		}
	}

	/**
	 * Updates the given model. Removes all statements from the passed module and inserts them again
	 *
	 * @param connection
	 *            Connection to the semantic repository
	 * @param model
	 *            Model with semantic statements
	 * @param graph
	 *            Graph to insert the statements from the model
	 */
	public static void updateModel(RepositoryConnection connection, Model model, IRI graph) {
		try {
			if (CollectionUtils.isNotEmpty(model)) {
				connection.remove(model);
				connection.add(model, graph);
			}
		} catch (RepositoryException e) {
			LOGGER.error(FAILED_ERROR_MESSAGE, "update", e.getMessage(), e);
			throw new SemanticPersistenceException(e);
		}
	}

	/**
	 * Removes the given model from the repository connection
	 *
	 * @param connection
	 *            Connection to the semantic repository
	 * @param model
	 *            Model with semantic statements
	 */
	public static void removeModel(RepositoryConnection connection, Model model) {
		removeModel(connection, model, null);
	}

	/**
	 * Removes the given model from the repository connection. The method removes all values for statements that
	 * contains date or date time as literal values to prevent for date diff errors
	 * <p>
	 * <b>Note that this effectively disables multi value date fields</b> .<br>
	 * See CMF-18001
	 *
	 * @param connection
	 *            Connection to the semantic repository
	 * @param model
	 *            Model with semantic statements
	 * @param graph
	 *            Graph to insert the statements from the model
	 */
	public static void removeModel(RepositoryConnection connection, Model model, IRI graph) {
		try {
			if (CollectionUtils.isNotEmpty(model)) {
				List<Statement> dateStatements = extractDateStatements(model);
				connection.remove(model, graph);
				// remove all Date values from the database
				for (Statement statement : dateStatements) {
					connection.remove(statement.getSubject(), statement.getPredicate(), null, graph);
				}
			}
		} catch (RepositoryException e) {
			LOGGER.error(FAILED_ERROR_MESSAGE, "remove", e.getMessage(), e);
			throw new SemanticPersistenceException(e);
		}
	}

	private static List<Statement> extractDateStatements(Model model) {
		List<Statement> dateStatements = new LinkedList<>();
		for (Iterator<Statement> it = model.iterator(); it.hasNext();) {
			Statement statement = it.next();
			if (isDateValue(statement.getObject())) {
				dateStatements.add(statement);
				it.remove();
			}
		}
		return dateStatements;
	}

	private static boolean isDateValue(Value value) {
		if (value instanceof Literal) {
			IRI datatype = ((Literal) value).getDatatype();
			return XMLSchema.DATE.equals(datatype) || XMLSchema.DATETIME.equals(datatype);
		}
		return false;
	}

	/**
	 * Creates statement from the given subject, predicate and value
	 *
	 * @param subjectUri
	 *            The IRI of the subject
	 * @param predicate
	 *            The predicate
	 * @param value
	 *            The value
	 * @param registryService
	 *            Registry service for transforming the URIs
	 * @param valueFactory
	 *            Value factory for creating the statement
	 * @return the statement combined by the given subject, predicate and value
	 */
	public static Statement createStatement(Object subjectUri, Object predicate, Serializable value,
			NamespaceRegistryService registryService, ValueFactory valueFactory) {
		return createStatement(subjectUri, predicate, value, false, registryService, valueFactory);
	}

	/**
	 * Creates statement from the given subject, predicate and value, that will be transformed into literal
	 *
	 * @param subjectUri
	 *            The uri of the subject
	 * @param predicate
	 *            The predicate
	 * @param literal
	 *            The literal value
	 * @param registryService
	 *            Registry service for transforming the URIs
	 * @param valueFactory
	 *            Value factory for creating the statement
	 * @return the statement combined by the given subject, predicate and value
	 */
	public static Statement createLiteralStatement(Object subjectUri, Object predicate, Serializable literal,
			NamespaceRegistryService registryService, ValueFactory valueFactory) {
		return createStatement(subjectUri, predicate, literal, true, registryService, valueFactory);
	}

	/**
	 * Creates statement for the given parameters.
	 *
	 * @param subjectUri
	 *            The IRI of the subject
	 * @param predicate
	 *            The IRI of the predicate
	 * @param value
	 *            The value
	 * @param isLiteral
	 *            If the value is explicitly literal
	 * @param registryService
	 *            Namespace registry service
	 * @param valueFactory
	 *            Value factory
	 * @return Built statement from the input parameters or null if one of them is null
	 */
	private static Statement createStatement(Object subjectUri, Object predicate, Serializable value, boolean isLiteral,
			NamespaceRegistryService registryService, ValueFactory valueFactory) {
		if (Objects.isNull(subjectUri) || Objects.isNull(predicate) || Objects.isNull(value)) {
			return null;
		}

		IRI subject = createURI(subjectUri, registryService);
		IRI predicateURI = createURI(predicate, registryService);
		Value convertedValue = createValue(value, isLiteral, registryService);

		return valueFactory.createStatement(subject, predicateURI, convertedValue, null);
	}

	/**
	 * Creates literal or IRI for the given value
	 *
	 * @param value
	 *            the value
	 * @param isLiteral
	 *            the is literal
	 * @param registryService
	 *            the registry service
	 * @return the value
	 */
	public static Value createValue(Serializable value, boolean isLiteral, NamespaceRegistryService registryService) {
		Value convertedValue = null;
		if (value instanceof Value) {
			convertedValue = (Value) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.contains(SPARQLQueryHelper.URI_SEPARATOR) && !isLiteral) {
				convertedValue = registryService.buildUri(stringValue);
			}
		}
		if (convertedValue == null) {
			convertedValue = ValueConverter.createLiteral(value);
		}
		return convertedValue;
	}

	private static IRI createURI(Object instanceUri, NamespaceRegistryService registryService) {
		IRI subject = null;
		if (instanceUri instanceof String) {
			subject = registryService.buildUri((String) instanceUri);
		} else if (instanceUri instanceof IRI) {
			subject = (IRI) instanceUri;
		} else if (instanceUri instanceof Uri) {
			subject = registryService.buildUri(instanceUri.toString());
		}

		return subject;
	}

}
