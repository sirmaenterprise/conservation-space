package com.sirma.itt.emf.semantic.model.init;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
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
	private RepositoryConnection repositoryConnection;

	/**
	 * Post startup event.
	 *
	 * @param event
	 *            the event
	 */
	@Transactional
	public void onDefinitionLoad(@Observes DefinitionsChangedEvent event) {
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
			repositoryConnection.clear(EMF.DEFINITIONS_CONTEXT);
			SemanticPersistenceHelper.saveModel(repositoryConnection, model, EMF.DEFINITIONS_CONTEXT);
		}
	}
}
