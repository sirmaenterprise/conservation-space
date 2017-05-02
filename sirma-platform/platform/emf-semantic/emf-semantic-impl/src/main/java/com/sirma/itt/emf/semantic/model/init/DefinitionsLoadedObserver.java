package com.sirma.itt.emf.semantic.model.init;

import java.util.List;
import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.definitions.SemanticDefinitionsModelProvider;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Handles the building of the rdf model for the all definitions, after their load/reload. Intercepts event, which shows
 * that all definitions are loaded and ready for processing.
 *
 * @author BBonev
 * @author A. Kunchev
 */
@ApplicationScoped
public class DefinitionsLoadedObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionsLoadedObserver.class);

	@Inject
	@Any
	private javax.enterprise.inject.Instance<DefinitionAccessor> accessors;

	@Inject
	private SemanticDefinitionsModelProvider semanticDefinitionsModelProvider;

	@Inject
	private Instance<RepositoryConnection> repositoryConnection;

	@Inject
	private TransactionSupport dbDao;

	/**
	 * Post startup event.
	 *
	 * @param event
	 *            the event
	 */
	public void onDefinitionLoad(@Observes AllDefinitionsLoaded event) {
		LOGGER.info("Triggered update due to completed definition update/reload.");
		getModelFromDefinitionsOnLoad();
	}

	/**
	 * Builds rdf model for definitions on their load/reload. Extracts all definitions and then processes them through
	 * extensions, which builds the model, before it save.
	 */
	private void getModelFromDefinitionsOnLoad() {
		Model model = new LinkedHashModel();

		for (DefinitionAccessor accessor : accessors) {
			// change to use dictionary service and his cache to access all definitions
			List<DefinitionModel> definitions = accessor.getAllDefinitions();
			for (DefinitionModel definition : definitions) {
				semanticDefinitionsModelProvider.provideModelStatements(definition, model);
			}
		}
		if (!model.isEmpty()) {
			dbDao.invokeInNewTx(new ClearGraph(EMF.DEFINITIONS_CONTEXT, repositoryConnection));
			SemanticPersistenceHelper.saveModel(repositoryConnection.get(), model, EMF.DEFINITIONS_CONTEXT);
		}
	}

	private static class ClearGraph implements Callable<Void> {
		/** The graph URI. */
		private URI graph;
		/** The connection. */
		private Instance<RepositoryConnection> connection;

		/**
		 * Initializes which graph to be cleared and which connection to use
		 * 
		 * @param graphUri
		 *            The Graph URI that will be cleared
		 * @param repositoryConnection
		 *            Repository connection
		 */
		public ClearGraph(URI graphUri, Instance<RepositoryConnection> repositoryConnection) {
			graph = graphUri;
			connection = repositoryConnection;
		}

		@Override
		public Void call() throws Exception {
			LOGGER.debug("Executing Clear Graph operation in new Tx:\n{}", graph);
			connection.get().clear(graph);
			return null;
		}
	}

}
