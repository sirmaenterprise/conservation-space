package com.sirma.itt.emf.semantic.content;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.parser.sparql.SPARQLUtil;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.content.ContentPersister;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Synchronizes document content with the semantic DB. Removes the old content field for the target instance and adds
 * the new value.
 *
 * @author Vilizar Tsonev
 * @author BBonev
 */
@ApplicationScoped
public class SemanticContentPersister implements ContentPersister {

	private static final String NEW_VALUE = "?newValue";
	private static final String SUBJECT = "?instance";
	private static final String GRAPH = "?graph";
	private static final String PROP_NAME = "?propName";

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticContentPersister.class);

	private static final Pattern WHITE_SPACE = Pattern.compile("\\s\\s+");

	/**
	 * Control characters pattern.
	 *
	 * <pre>
	 *	 \u0000 - 'NULL'
	 *	 \u0001 - 'START OF HEADING'
	 *	 \u0002 - 'START OF TEXT'
	 *	 \u0003 - 'END OF TEXT'
	 *	 \u0004 - 'END OF TRANSMISSION'
	 *	 \u0005 - 'ENQUIRY'
	 *	 \u0006 - 'ACKNOWLEDGE'
	 *	 \u0007 - 'BELL'
	 *	 \u0008 - 'BACKSPACE'
	 *	 \u0009 - 'CHARACTER TABULATION'
	 *	 \u000A - 'LINE FEED (LF)'
	 *	 \u000B - 'LINE TABULATION'
	 *	 \u000C - 'FORM FEED (FF)'
	 *	 \u000D - 'CARRIAGE RETURN (CR)'
	 *	 \u000E - 'SHIFT OUT'
	 *	 \u000F - 'SHIFT IN'
	 *	 \u0010 - 'DATA LINK ESCAPE'
	 *	 \u0011 - 'DEVICE CONTROL ONE'
	 *	 \u0012 - 'DEVICE CONTROL TWO'
	 *	 \u0013 - 'DEVICE CONTROL THREE'
	 *	 \u0014 - 'DEVICE CONTROL FOUR'
	 *	 \u0015 - 'NEGATIVE ACKNOWLEDGE'
	 *	 \u0016 - 'SYNCHRONOUS IDLE'
	 *	 \u0017 - 'END OF TRANSMISSION BLOCK'
	 *	 \u0018 - 'CANCEL'
	 *	 \u0019 - 'END OF MEDIUM'
	 *	 \u001A - 'SUBSTITUTE'
	 *	 \u001B - 'ESCAPE'
	 *	 \u001C - 'INFORMATION SEPARATOR FOUR'
	 *	 \u001D - 'INFORMATION SEPARATOR THREE'
	 *	 \u001E - 'INFORMATION SEPARATOR TWO'
	 *	 \u001F - 'INFORMATION SEPARATOR ONE'
	 * </pre>
	 */
	private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\u0000-\u001F]");

	private static final String DELETE_QUERY_TEMPLATE = "delete { GRAPH<" + GRAPH + "> { " + SUBJECT + " " + PROP_NAME
			+ " ?oldValue. }}";
	private static final String INSERT_QUERY_TEMPLATE = " insert { GRAPH<" + GRAPH + "> { " + SUBJECT + " " + PROP_NAME
			+ " " + NEW_VALUE + ". }}";
	private static final String WHERE_QUERY_TEMPLATE = " where { GRAPH<" + GRAPH + "> { optional { " + SUBJECT + " "
			+ PROP_NAME + " ?oldValue. }}}";

	@Inject
	private javax.enterprise.inject.Instance<TransactionalRepositoryConnection> connection;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private ValueFactory valueFactory;

	/**
	 * Saves the content of the primary view for instance identified by the given instance.
	 *
	 * @param instanceId
	 *            the instance id to assign the extracted content
	 * @param content
	 *            the content to assign. if <code>null</code> it will remove any content already assigned
	 */
	@Override
	@Transactional
	public void savePrimaryView(Serializable instanceId, String content) {
		saveContent(instanceId, EMF.VIEW, content);
	}

	@Override
	@Transactional
	public void saveWidgetsContent(Serializable instanceId, String content) {
		saveContent(instanceId, EMF.VIEW_WIDGETS_CONTENT, content);
	}
	/**
	 * Saves a primary content for instance identified by the given instance.
	 *
	 * @param instanceId
	 *            the instance id to assign the extracted content
	 * @param content
	 *            the content to assign. if <code>null</code> it will remove any content already assigned
	 */
	@Override
	@Transactional
	public void savePrimaryContent(Serializable instanceId, String content) {
		saveContent(instanceId, EMF.CONTENT, content);
	}

	@SuppressWarnings("resource")
	private void saveContent(Serializable instanceId, URI contentURI, String extractedText) {
		Objects.requireNonNull(instanceId, "Cannot update content for null instance identifier");
		Objects.requireNonNull(contentURI, "Cannot update content for null content property identifier");

		RepositoryConnection repositoryConnection = connection.get();
		try {
			StringBuilder updateQuery = new StringBuilder(512);
			URI subject = namespaceRegistryService.buildUri((String) instanceId);
			updateQuery.append(DELETE_QUERY_TEMPLATE);

			// replace any repeating white space blocks with a single space
			String text = trimWhiteSpaces(extractedText);

			// remove any control characters
			text = removeControlCharacters(text);

			Literal literal = null;
			if (StringUtils.isNotBlank(text)) {
				// escape the text before building the literal object, otherwise we could face malformed query exception
				// or some kind of injection will be possible
				literal = valueFactory.createLiteral(SPARQLUtil.encodeString(text));
				updateQuery.append(INSERT_QUERY_TEMPLATE);
			}
			updateQuery.append(WHERE_QUERY_TEMPLATE);

			String query = applyParameters(updateQuery, subject, contentURI, literal);

			SPARQLQueryHelper.prepareUpdateQuery(repositoryConnection, query, Collections.emptyMap(), false).execute();
		} catch (UpdateExecutionException e) {
			LOGGER.warn("Failed to update content for instance {}", instanceId, e);
			throw new SemanticPersistenceException(e);
		}
	}

	private String applyParameters(StringBuilder updateQuery, URI subject, URI propertyName, Literal newValue) {
		return updateQuery
				.toString()
					.replace(SUBJECT, convertUri(subject))
					.replace(PROP_NAME, convertUri(propertyName))
					.replace(NEW_VALUE, Objects.toString(newValue))
					.replace(GRAPH, namespaceRegistryService.getDataGraph().toString());
	}

	private static String convertUri(Object uri) {
		if (uri instanceof URI) {
			return "<" + uri + ">";
		}
		// if the value is null this will return "null" string
		return Objects.toString(uri);
	}

	private static String trimWhiteSpaces(String extractedText) {
		if (extractedText == null) {
			return null;
		}
		return WHITE_SPACE.matcher(extractedText).replaceAll(" ").trim();
	}

	/**
	 * Removes any control character from string.
	 *
	 * @param text
	 *            to process
	 * @return clean text
	 */
	private static String removeControlCharacters(String text) {
		if (text == null) {
			return null;
		}
		return CONTROL_CHARACTERS.matcher(text).replaceAll("");
	}
}