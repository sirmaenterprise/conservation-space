package com.sirma.itt.emf.semantic.model.init;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.DESCRIPTION;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;
import com.sirma.seip.semantic.events.SemanticModelUpdatedEvent;

/**
 * Initializes the class description needed for all classes to be represented as objects in the system.
 * </p>
 * The default properties that are initialized are: rdf:type, emf:type, emf:status, emf:defaultTemplate,
 * emf:instanceType and emf:isDeleted. Title and description are copied from the existing properties. All data is
 * inserted in the graph The enriched data is inserted in the graph {@link EMF#CLASS_DESCRIPTION_CONTEXT} for easy
 * update
 *
 * @author kirq4e
 */
@ApplicationScoped
public class ClassDescriptionGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Statistics statistics;

	@Inject
	private RepositoryConnection connection;

	/**
	 * Intercepts {@link SemanticModelUpdatedEvent} which should trigger initialization of classes description.
	 *
	 * @param event that is intercepted
	 */
	@Transactional
	public void onSemanticModelUpdate(@Observes SemanticModelUpdatedEvent event) {
		LOGGER.trace("Intercepted {}", event.toString());
		initClassDescription();
	}

	/**
	 * Selects all classes and enriches them with constants needed for the classes to be shown as objects in the system.
	 * The enriched data is inserted in the graph {@link EMF#CLASS_DESCRIPTION_CONTEXT}
	 */
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 5.5)
	@Transactional
	public void initClassDescription() {
		LOGGER.info("Generating class description");
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), "classDescriptionInitialization").begin();

		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection,
				SemanticQueries.QUERY_CLASS_DESCRIPTION.getQuery(), CollectionUtils.emptyMap(), false);

		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			Model insertModel = new LinkedHashModel();
			ValueFactory valueFactory = connection.getValueFactory();
			for (BindingSet bindingSet : resultIterator) {
				IRI classUri = (IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue();
				if (!insertModel.contains(classUri, null, null, (Resource) null)) {
					// generates default properties
					insertModel.add(classUri, RDF.TYPE, EMF.CLASS_DESCRIPTION, (Resource) null);
					insertModel.add(classUri, EMF.STATUS, valueFactory.createLiteral("APPROVED"), (Resource) null);
					insertModel.add(classUri, EMF.DEFAULT_TEMPLATE, valueFactory.createLiteral("ontologyClassTemplate"),
							(Resource) null);
					insertModel.add(classUri, EMF.TYPE, valueFactory.createLiteral("classDefinition"), (Resource) null);
					insertModel.add(classUri, EMF.INSTANCE_TYPE, valueFactory.createLiteral("objectinstance"),
							(Resource) null);
					insertModel.add(classUri, EMF.IS_DELETED, valueFactory.createLiteral(false), (Resource) null);
					insertModel.add(classUri, valueFactory.createIRI(EMF.NAMESPACE, "partOfOntology"),
							valueFactory.createLiteral(classUri.getNamespace()), (Resource) null);
				}
				// copy existing properties (multiple titles for different languages)
				insertModel.add(classUri, DCTERMS.TITLE, bindingSet.getBinding(TITLE).getValue(), (Resource) null);
				insertModel.add(classUri, DCTERMS.DESCRIPTION, bindingSet.getBinding(DESCRIPTION).getValue(),
						(Resource) null);
			}

			// clear existing graph so we doesn't need to make a diff
			connection.clear(EMF.CLASS_DESCRIPTION_CONTEXT);
			SemanticPersistenceHelper.saveModel(connection, insertModel, EMF.CLASS_DESCRIPTION_CONTEXT);
			LOGGER.debug("Class descritpion initialization took {} ms", tracker.stop());

		} catch (QueryEvaluationException | RepositoryException e) {
			LOGGER.warn("Error while executing query: {}\n{}", SemanticQueries.QUERY_CLASS_DESCRIPTION.getName(),
					e.getMessage(), e);
			throw new EmfRuntimeException(e);
		}
	}
}
