package com.sirma.sep.model.management.persistence;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurations;

/**
 * Validates and persist the given semantic models. The validation checks if the given remove statements are actually in
 * the database at the moment of the method call and fails if they are not.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/08/2018
 */
public class SemanticDatabasePersistence {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RepositoryConnection repositoryConnection;
	@Inject
	private ValueFactory valueFactory;
	@Inject
	private ModelManagementDeploymentConfigurations deploymentConfigurations;

	/**
	 * Validates the given statements with the current database state. Any found inconsistencies will be reported in
	 * the response.
	 *
	 * @param statements the statements to validate
	 * @return the list of validation messages related to found data inconsistencies
	 */
	public List<ValidationMessage> validateDatabaseState(List<Statement> statements) {
		if (CollectionUtils.isEmpty(statements)) {
			return Collections.emptyList();
		}

		SemanticDatabaseMessageBuilder messageBuilder = new SemanticDatabaseMessageBuilder();
		for (Statement statement : statements) {
			RepositoryResult<Statement> repositoryResult = repositoryConnection.getStatements(statement.getSubject(),
					statement.getPredicate(), null);
			Set<Value> foundObjects = new LinkedHashSet<>();
			while (repositoryResult.hasNext()) {
				foundObjects.add(repositoryResult.next().getObject());
			}

			boolean emptyLiteral = isEmptyLiteralStatement(statement);
			if (emptyLiteral && !foundObjects.isEmpty()) {
				// Expected empty but found
				messageBuilder.excessResources(statement, foundObjects);
			} else if (!emptyLiteral && foundObjects.isEmpty()) {
				// Expected but found empty
				messageBuilder.missingResources(statement);
			} else if (!emptyLiteral && !foundObjects.contains(statement.getObject())) {
				// Expected but found
				messageBuilder.resourceMismatch(statement, foundObjects);
			}
		}
		return messageBuilder.getMessages();
	}

	/**
	 * Persist the given diff of statements to the database. The method will perform a validation on the statements that should be removed
	 * if they actually exist in the database and if not it will fail with {@link DatabaseException}
	 *
	 * @param newStatements the statements to add to the database
	 * @param oldStatements the statements to remove from the database
	 * @throws DatabaseException if data inconsistency is detected for the given statements for removal if any
	 */
	public void saveChanges(List<Statement> newStatements, List<Statement> oldStatements) {
		LOGGER.debug("Deploying semantic changes\n\tRemoving statements: {}\n\tAdding statements  : {}", oldStatements,
				newStatements);
		if (!oldStatements.isEmpty()) {
			for (Statement statement : oldStatements) {
				checkDatabaseState(statement).forEach(repositoryConnection::remove);
			}
		}
		if (!newStatements.isEmpty()) {
			IRI context = deploymentConfigurations.getSemanticContext().get();
			repositoryConnection.add(newStatements, context);
		}
	}

	private List<Statement> checkDatabaseState(Statement statement) {
			RepositoryResult<Statement> repositoryResult = repositoryConnection.getStatements(statement.getSubject(),
					statement.getPredicate(), null);
			List<Statement> found = new LinkedList<>();
			while (repositoryResult.hasNext()) {
				Statement next = repositoryResult.next();
				// copy statement without the context
				found.add(valueFactory.createStatement(next.getSubject(), next.getPredicate(), next.getObject()));
			}
		boolean emptyLiteral = isEmptyLiteralStatement(statement);
		if (found.isEmpty() && !emptyLiteral) {
			LOGGER.warn("Inconsistent database state detected! Didn't find anything but expected {}", statement);
			return Collections.emptyList();
		}
		if (found.contains(statement) && found.size() == 1) {
			// everything matches and we have single value - best case
			return Collections.singletonList(statement);
		}
		boolean stringNonLangLiteral = isStringNonLangLiteral(statement);
		if (stringNonLangLiteral) {
			// we have non language string literal statement, may be we can remove all statements
			List<Statement> stringLiterals = found.stream()
					.filter(this::isStringNonLangLiteral)
					.collect(Collectors.toList());
			LOGGER.info("Removing previous string literals statements: {}", stringLiterals);
			// the statement is contained in the found set but have more than one value
			// the statement is not found
			// in both cases the found literals will be removed and will be replaced with the new value
			return stringLiterals;
		}

		if (found.contains(statement)) {
			// non string literals and IRIs
			return Collections.singletonList(statement);
		} else if (statement.getObject() instanceof Literal) {
			String lang = ((Literal) statement.getObject()).getLanguage().orElse(null);
			if (lang != null) {
				// look for statements with literals with the same language
				return found.stream()
						.filter(s -> sameLanguageLiteralStatement(s, lang))
						.collect(Collectors.toList());
			}
		}
		LOGGER.warn("Inconsistent database state detected! Expected {} but got {}", statement, found);
		// we cannot return statements for removal as no language was matched or the objects where not literals as all
		return Collections.emptyList();
	}

	private boolean isStringNonLangLiteral(Statement statement) {
		return statement.getObject() instanceof Literal
				&& !Literals.isLanguageLiteral((Literal) statement.getObject())
				&& ((Literal) statement.getObject()).getDatatype().equals(XMLSchema.STRING);
	}

	private boolean sameLanguageLiteralStatement(Statement statement, String lang) {
		return statement.getObject() instanceof Literal && ((Literal) statement.getObject()).getLanguage()
				.filter(l -> l.equals(lang)).isPresent();
	}

	private static boolean isEmptyLiteralStatement(Statement statement) {
		Value statementValue = statement.getObject();
		return statementValue instanceof Literal && isEmpty(((Literal) statementValue).getLabel());
	}
}
