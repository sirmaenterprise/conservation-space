package com.sirma.itt.emf.semantic.model.init;

import static com.sirma.itt.seip.collections.CollectionUtils.*;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.DESCRIPTION;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.Binding;
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
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;
import com.sirma.seip.semantic.events.SemanticModelUpdatedEvent;

/**
 * Initializes the class description needed for all classes to be represented as objects in the system.
 * </p>
 * The default properties that are initialized are: rdf:type, emf:type, emf:status, emf:defaultTemplate,
 * emf:instanceType and emf:isDeleted. Title and description are copied from the existing properties. All data is
 * inserted in the graph {@link EMF#CLASS_DESCRIPTION_CONTEXT} for easy update.
 *
 * @author kirq4e
 */
public class ClassDescriptionGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private RepositoryConnection connection;

	@Inject
	public ClassDescriptionGenerator(RepositoryConnection connection) {
		this.connection = connection;
	}

	/**
	 * Intercepts {@link SemanticModelUpdatedEvent} which should trigger initialization of classes description.
	 *
	 * @param event that is intercepted
	 */
	@Transactional
	void onSemanticModelUpdate(@Observes SemanticModelUpdatedEvent event) {
		LOGGER.trace("Intercepted {}", event);
		initClassAndPropertiesDescription();
	}

	/**
	 * Selects all classes and enriches them with constants needed for the classes to be shown as objects in the system.
	 * The enriched data is inserted in the graph {@link EMF#CLASS_DESCRIPTION_CONTEXT}
	 */
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 5.5)
	@Transactional
	public void initClassAndPropertiesDescription() {
		LOGGER.info("Generating class description");

		Models classesInfo = loadModel(SemanticQueries.QUERY_CLASS_DESCRIPTION);
		Models currentClassesInfo = loadModel(SemanticQueries.QUERY_RUNTIME_CLASS_DESCRIPTION);

		Models propertiesInfo = loadModel(SemanticQueries.QUERY_PROPERTY_DESCRIPTION);
		Models currentPropertiesInfo = loadModel(SemanticQueries.QUERY_RUNTIME_PROPERTY_DESCRIPTION);

		Model insertModel = new LinkedHashModel();
		ValueFactory valueFactory = connection.getValueFactory();

		BiConsumer<IRI, Value> titleAdder = (subject, value) -> insertModel.add(subject, DCTERMS.TITLE, value);
		BiConsumer<IRI, Value> descriptionAdder = (subject, value) -> insertModel.add(subject, DCTERMS.DESCRIPTION, value);

		copyMissingLanguagesTo(classesInfo, currentClassesInfo, titleAdder, descriptionAdder);
		copyMissingLanguagesTo(propertiesInfo, currentPropertiesInfo, titleAdder, descriptionAdder);

		// generates default properties
		classesInfo.getIds().forEach(classUri -> addClassRequiredData(classUri, insertModel, valueFactory));

		SemanticPersistenceHelper.saveModel(connection, insertModel, EMF.CLASS_DESCRIPTION_CONTEXT);
	}

	private void copyMissingLanguagesTo(Models from, Models currentValues, BiConsumer<IRI, Value> onNewTitle,
			BiConsumer<IRI, Value> onNewDescription) {
		from.getModels().forEach(currentValues.addMissing(onNewTitle, onNewDescription));
	}

	private void addClassRequiredData(IRI classUri, Model insertModel, ValueFactory valueFactory) {
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

	private Models loadModel(SemanticQueries query) {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, query.getQuery(), emptyMap(), false);
		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			Models models = new Models();
			for (BindingSet bindingSet : resultIterator) {
				IRI iri = (IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue();
				Value title = bindingSet.getBinding(TITLE).getValue();
				Binding descriptionBinding = bindingSet.getBinding(DESCRIPTION);
				Value description = null;
				if (descriptionBinding != null) {
					description = descriptionBinding.getValue();
				}
				models.populate(iri, filterOutInvalidNodes(title), filterOutInvalidNodes(description));
			}
			return models;
		} catch (QueryEvaluationException | RepositoryException e) {
			LOGGER.warn("Error while executing query: {}\n{}", query.getName(), e.getMessage(), e);
			throw new EmfRuntimeException(e);
		}
	}

	private Value filterOutInvalidNodes(Value value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Literal) {
			return value;
		}
		LOGGER.debug("Filtering out non literal value: {}({})", value, value.getClass().getSimpleName());
		return null;
	}

	private static class Models {
		private Map<IRI, DisplayModel> modelMap = new HashMap<>();

		void populate(IRI id, Value title, Value description) {
			DisplayModel model = modelMap.computeIfAbsent(id, DisplayModel::new);
			if (title != null) {
				model.addTitle(title);
			}
			if (description != null) {
				model.addDescription(description);
			}
		}

		/**
		 * Return a consumer that accepts {@link DisplayModel} instance and add all of his properties to the current
		 * model. The the titles and properties are added only if value for such language does not exists in the current
		 * model. This only means that only single value from a certain language will be copied from the source model.
		 */
		Consumer<DisplayModel> addMissing(BiConsumer<IRI, Value> onMissingTitle,
				BiConsumer<IRI, Value> onMissingDescription) {
			return sourceModel -> {
				final IRI iri = sourceModel.id;
				sourceModel.getTitles().forEach(title -> {
					if (addTitleIfMissing(iri, title)) {
						onMissingTitle.accept(iri, title);
					}
				});

				sourceModel.getDescriptions().forEach(description -> {
					if (addDescriptionIfMissing(iri, description)) {
						onMissingDescription.accept(iri, description);
					}
				});
			};
		}

		Stream<IRI> getIds() {
			return modelMap.keySet().stream();
		}

		Stream<DisplayModel> getModels() {
			return modelMap.values().stream();
		}

		private boolean addTitleIfMissing(IRI id, Value title) {
			DisplayModel model = modelMap.computeIfAbsent(id, DisplayModel::new);
			return title != null && model.addTitleIfNewLang(title);
		}

		private boolean addDescriptionIfMissing(IRI id, Value description) {
			DisplayModel model = modelMap.computeIfAbsent(id, DisplayModel::new);
			return description != null && model.addDescriptionIfNewLang(description);
		}
	}

	private static class DisplayModel {
		private final IRI id;
		private Map<String, Set<Value>> titles = new HashMap<>();
		private Map<String, Set<Value>> descriptions = new HashMap<>();

		private DisplayModel(IRI id) {
			this.id = id;
		}

		Stream<Value> getTitles() {
			return titles.values()
					.stream()
					.flatMap(Collection::stream);
		}

		Stream<Value> getDescriptions() {
			return descriptions.values()
					.stream()
					.flatMap(Collection::stream);
		}

		boolean addTitle(Value value) {
			return addTo(titles, value);
		}

		boolean addDescription(Value value) {
			return addTo(descriptions, value);
		}

		private static boolean addTo(Map<String, Set<Value>> target, Value value) {
			if (value instanceof Literal) {
				String language = ((Literal) value).getLanguage().orElse("en");
				Set<Value> values = target.computeIfAbsent(language, lang -> new HashSet<>());
				return values.add(value);
			}

			throw invalidArgumentType(value);
		}

		private static IllegalArgumentException invalidArgumentType(Value value) {
			return new IllegalArgumentException("Expected Literal but got " +
					(value == null ? "null" : (value.toString() + "(" + value.getClass().getSimpleName() + ")")));
		}

		/**
		 * Returns true on successfully adding the title to the current {@link DisplayModel}
		 */
		private boolean addTitleIfNewLang(Value title) {
			return addIfNewLang(title, titles::containsKey, this::addTitle);
		}

		/**
		 * Returns true on successfully adding the description to the current {@link DisplayModel}
		 */
		private boolean addDescriptionIfNewLang(Value description) {
			return addIfNewLang(description, descriptions::containsKey, this::addDescription);
		}

		private boolean addIfNewLang(Value value, Predicate<String> languageCheck, Predicate<Value> valuePredicate) {
			if (value instanceof Literal) {
				String language = ((Literal) value).getLanguage().orElse("en");
				return !languageCheck.test(language) && valuePredicate.test(value);
			}
			throw invalidArgumentType(value);
		}
	}
}
