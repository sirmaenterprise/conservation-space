package com.sirma.itt.emf.semantic.model.init;

import static com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper.createStatement;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Initializes all object relations to extend the default relation emf:hasRelation. This is done because the search for
 * any relation to have an existing relation to search on. The relations rdf:type and emf:hasRelation are skipped,
 * because they are not an actual object relation
 * </p>
 * This initialization may take some time on a server with large amount of data
 *
 * @author kirq4e
 */
@ApplicationScoped
public class DefaultRelationInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Any
	private Instance<DefinitionAccessor> accessors;

	@Inject
	private RepositoryConnection repositoryConnection;

	@Inject
	private ValueFactory valueFactory;

	@Inject
	private NamespaceRegistryService registryService;

	/**
	 * Retrieve all relations and a flag if they are already sub property of the default relation. The relations
	 * rdf:type and emf:hasRelation are skipped, because they are not an actual object relation
	 */
	private static final String QUERY_GET_ALL_RELATIONS = "select ?instance ?extendsDefaultRelation where { "
			+ "?instance a owl:ObjectProperty . filter(str(?instance) != \"" + RDF.TYPE.stringValue() + "\" &&"
			+ "str(?instance) != \"" + EMF.HAS_RELATION.stringValue() + "\")"
			+ "bind(if(exists { ?instance rdfs:subPropertyOf <" + EMF.HAS_RELATION.stringValue()
			+ "> }, \"true\"^^xsd:boolean, \"false\"^^xsd:boolean ) as ?extendsDefaultRelation ) }";

	/**
	 * Post startup event.
	 *
	 * @param event
	 *            the event
	 */
	@Transactional
	public void onDefinitionLoad(@Observes DefinitionsChangedEvent event) {
		LOGGER.info("Triggered initialization of default relation due to completed definition update/reload.");
		getModelFromDefinitionsOnLoad();
	}

	/**
	 * Builds RDF Model for initializing relations to extend the default relation emf:hasRelation. All relations from
	 * the definitions are checked against the existing relations. If a new relation is added then it is made to extend
	 * the default relation.
	 */
	@SuppressWarnings("boxing")
	private void getModelFromDefinitionsOnLoad() {
		Model addModel = new LinkedHashModel();

		Map<String, Boolean> existingRelations = getExistingRelations();
		Set<String> definitionRelations = new HashSet<>();

		for (DefinitionAccessor accessor : accessors) {
			// change to use dictionary service and his cache to access all definitions
			for (DefinitionModel definition : accessor.getAllDefinitions()) {
				definitionRelations.addAll(definition
						.fieldsStream()
							.flatMap(PropertyDefinition::stream)
							.filter(PropertyDefinition.isObjectProperty())
							.map(PropertyDefinition.resolveUri())
							.filter(StringUtils::isNotBlank)
							.collect(Collectors.toSet()));
			}
		}

		// iterate only over the set of relations because they are duplicated in many definitions
		for(String relationIri : definitionRelations) {
			setExtendDefaultRelation(relationIri, existingRelations, addModel);
		}

		if (!addModel.isEmpty()) {
			LOGGER.trace("Saving model for initialization of default relation {}", addModel);
			SemanticPersistenceHelper.saveModel(repositoryConnection, addModel, EMF.DEFAULT_RELATION_CONTEXT);
		}
		// TODO add remove of relation when definitions can be removed
	}

	@SuppressWarnings("boxing")
	private void setExtendDefaultRelation(String relationIri, Map<String, Boolean> existingRelations, Model addModel) {
		if (!existingRelations.containsKey(relationIri)) {
			LOGGER.warn("No such relation exists in the ontology {}!", relationIri);
		}

		if (existingRelations.containsKey(relationIri) && !existingRelations.get(relationIri)) {
			// add new relations
			LOGGER.trace("Adding relation {} to extend the default relation", relationIri);
			addModel.add(
					createStatement(relationIri, RDFS.SUBPROPERTYOF, EMF.HAS_RELATION, registryService, valueFactory));
		}
		existingRelations.remove(relationIri);
	}

	@SuppressWarnings("boxing")
	private Map<String, Boolean> getExistingRelations() {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, QUERY_GET_ALL_RELATIONS,
				CollectionUtils.emptyMap(), false);

		Map<String, Boolean> existingRelations = CollectionUtils.createHashMap(100);
		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : resultIterator) {
				String relationUri = registryService
						.getShortUri((IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue());
				Boolean extendsDefaultRelation = ((Literal) bindingSet.getBinding("extendsDefaultRelation").getValue())
						.booleanValue();
				existingRelations.put(relationUri, extendsDefaultRelation);
			}
		}
		return existingRelations;
	}

}
