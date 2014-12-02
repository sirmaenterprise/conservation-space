package com.sirma.itt.emf.semantic.definitions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.event.DefinitionMigrationEvent;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Observer that listens for changes in used definitions for instances and synchronizes the semantic
 * instance for definition revision changes. The implementation first inserts the new revisions for
 * the definitions. Then executes a query to migrate all instances that use that definitions and
 * then deletes the inserted data in the first step.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionMigrationObserver {

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The repository connection. */
	@Inject
	private Instance<RepositoryConnection> repositoryConnection;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;
	/** The context name. */
	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_DB_CONTEXT_NAME, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework")
	private String contextName;

	private String migrationQueryStart;
	private String migrationQueryEnd;

	/**
	 * Pre build the migration queries.
	 */
	@PostConstruct
	public void init() {
		StringBuilder startBuilder = new StringBuilder();
		startBuilder.append(" DELETE {\r\n	?instance emf:");
		startBuilder.append(EMF.REVISION.getLocalName());
		startBuilder.append(" ?revision.\r\n } INSERT {\r\n	graph <");
		startBuilder.append(contextName);
		startBuilder.append("> {\r\n		?instance emf:");
		startBuilder.append(EMF.REVISION.getLocalName());
		startBuilder.append(" ?newRevision .\r\n	}\r\n } where {\r\n	?instance emf:");
		startBuilder.append(EMF.REVISION.getLocalName());
		startBuilder.append(" ?revision.\r\n");
		migrationQueryStart = startBuilder.toString();

		StringBuilder endBuilder = new StringBuilder();
		endBuilder.append("	?instance emf:");
		endBuilder.append(EMF.DEFINITION_ID.getLocalName());
		endBuilder.append(" ?definition .\r\n	BIND(URI(CONCAT(\"");
		endBuilder.append(EMF.NAMESPACE);
		endBuilder
				.append("\", ENCODE_FOR_URI(REPLACE(?definition, \"\\\\$\", \"_\")))) as ?definitionId).\r\n	?definitionId emf:");
		endBuilder.append(EMF.REVISION.getLocalName());
		endBuilder.append(" ?newRevision.\r\n }");
		migrationQueryEnd = endBuilder.toString();
	}

	/**
	 * This method is called when information about an DefinitionMigration which was previously
	 * requested using an asynchronous interface becomes available.
	 * 
	 * @param event
	 *            the event
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 * @throws UpdateExecutionException
	 *             the update execution exception
	 * @throws UnsupportedEncodingException
	 */
	public void onDefinitionMigration(@Observes DefinitionMigrationEvent event)
			throws RepositoryException, MalformedQueryException, UpdateExecutionException,
			UnsupportedEncodingException {
		Class<?> target = event.getTarget();
		List<Pair<String, Long>> ids = event.getDefinitionIds();
		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(target
				.getName());
		if ((typeDefinition == null) || typeDefinition.getUries().isEmpty()) {
			return;
		}
		Set<String> uries = typeDefinition.getUries();

		String definitionStatement = generateDefinitionStatement(ids);

		dbDao.invokeInNewTx(new ExecuteQuery(namespaceRegistryService.getNamespaces() + "INSERT"
				+ definitionStatement, repositoryConnection));
		try {
			String migrationScript = buldMigrationScript(uries);
			repositoryConnection.get().prepareUpdate(QueryLanguage.SPARQL, migrationScript)
					.execute();
		} finally {
			// this could be executed in the same transaction also but the current transaction could
			// broken by exception so we better have clean one
			dbDao.invokeInNewTx(new ExecuteQuery(namespaceRegistryService.getNamespaces()
					+ "DELETE" + definitionStatement, repositoryConnection));
		}
	}

	/**
	 * Generates delete/insert statement to change revisions for instances identified by the given
	 * list if classes.
	 * 
	 * @param uries
	 *            the uries
	 * @return the string
	 */
	private String buldMigrationScript(Set<String> uries) {
		StringBuilder builder = new StringBuilder(1024);
		builder.append(namespaceRegistryService.getNamespaces());
		builder.append(migrationQueryStart);

		for (Iterator<String> it = uries.iterator(); it.hasNext();) {
			String uri = it.next();
			builder.append("	{ ?instance a ").append(namespaceRegistryService.getShortUri(uri))
					.append(". }");
			if (it.hasNext()) {
				builder.append(" UNION ");
			}
		}

		builder.append(migrationQueryEnd);
		return builder.toString();
	}

	/**
	 * Generate statement for adding/removing definition information to semantic. NOTE: the query is
	 * not complete INSERT or DELETE should be added in front of the statement to complete it.
	 * 
	 * @param ids
	 *            the ids
	 * @return the build query
	 * @throws UnsupportedEncodingException
	 */
	private String generateDefinitionStatement(List<Pair<String, Long>> ids)
			throws UnsupportedEncodingException {
		// we does not expect the row size to be more than 50 chars and +10 for to be safe
		StringBuilder builder = new StringBuilder(ids.size() * 60);
		builder.append(" DATA { \n");
		for (Pair<String, Long> pair : ids) {
			builder.append("emf:").append(encodeUri(pair.getFirst())).append(" emf:revision \"")
					.append(pair.getSecond()).append("\"^^xsd:integer .\n");
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Encode the definition id for URL - due to bug/problem with decoding '$' character in semantic
	 * implementation we replace it with '_'.
	 * 
	 * @param id
	 *            the id
	 * @return the string
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private String encodeUri(String id) throws UnsupportedEncodingException {
		return URLEncoder.encode(id.replace('$', '_'), "UTF-8");
	}

	/**
	 * Callable definition to execute update query in new transaction - used for insert - delete.
	 * 
	 * @author BBonev
	 */
	private class ExecuteQuery implements Callable<Void> {

		/** The query. */
		private String query;
		/** The connection. */
		private Instance<RepositoryConnection> connection;

		/**
		 * Instantiates a new execute query.
		 * 
		 * @param query
		 *            the query
		 * @param connection
		 *            the connection
		 */
		public ExecuteQuery(String query, Instance<RepositoryConnection> connection) {
			this.query = query;
			this.connection = connection;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void call() throws Exception {
			Update update = connection.get().prepareUpdate(QueryLanguage.SPARQL, query);
			update.execute();
			return null;
		}

	}

}
