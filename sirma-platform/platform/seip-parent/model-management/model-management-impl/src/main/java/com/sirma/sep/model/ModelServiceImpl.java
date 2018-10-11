package com.sirma.sep.model;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.ReadOnly;

/**
 * Default implementation of {@link ModelService}.
 *
 * @author Vilizar Tsonev
 */
@ApplicationScoped
public class ModelServiceImpl implements ModelService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String TITLE_KEY = "title";
	private static final String ONTOLOGY_KEY = "ontology";

	static final String QUERY_ONTOLOGIES = ResourceLoadUtil.loadResource(ModelServiceImpl.class,
			"queryOntologies.sparql");

	@Inject
	@ReadOnly
	private RepositoryConnection repositoryConnection;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private Statistics statistics;

	@Inject
	private InstanceContextService contextService;

	@Override
	public List<Ontology> getOntologies() {
		TimeTracker timeTracker = statistics
				.createTimeStatistics(getClass(), "ontologiesRetrievalQueryExecution")
				.begin();
		try {
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, QUERY_ONTOLOGIES,
					Collections.emptyMap(), false);
			try (TupleQueryResultIterator queryResult = new TupleQueryResultIterator(tupleQuery.evaluate())) {
				return extractOntologiesFromQueryResult(queryResult);
			}
		} catch (QueryEvaluationException | RepositoryException e) {
			throw new SemanticPersistenceException("Failed evaluating query for ontologies retrieval", e);
		} finally {
			LOGGER.debug("Ontologies retrieval took {} ms", timeTracker.stop());
		}
	}

	@Override
	public List<ClassInfo> getClassesForOntology(String ontologyId) {
		if (StringUtils.isBlank(ontologyId)) {
			return CollectionUtils.emptyList();
		}
		TimeTracker timeTracker = statistics.createTimeStatistics(getClass(), "getClassesForOntology").begin();
		List<ClassInstance> classes = semanticDefinitionService.getClassesForOntology(ontologyId);
		LOGGER.debug("{} classes for ontology [{}] retrieved for {} ms", classes.size(), ontologyId,
				timeTracker.stop());

		// if classes from the requested ontology have parents from other ontologies (external), add them to the result
		Stream<ClassInstance> externalSuperClasses = getExternalSuperClasses(ontologyId, classes);
		return Stream.concat(classes.stream(), externalSuperClasses).map(this::toClassInfo).collect(
				Collectors.toList());
	}

	private static Stream<ClassInstance> getExternalSuperClasses(String ontologyId,
			List<ClassInstance> retrievedClasses) {
		return retrievedClasses
				.stream()
					.flatMap(semanticClass -> semanticClass.getSuperClasses().stream())
					.filter(semanticClass -> !ontologyId.equals(semanticClass.getString(ONTOLOGY_KEY)))
					.distinct();
	}

	private ClassInfo toClassInfo(ClassInstance classInstance) {
		ClassInfo classInfo = new ClassInfo()
				.setId(classInstance.getId().toString())
					.setLabel(classInstance.getLabel())
					.setOntology(classInstance.getString(ONTOLOGY_KEY));

		if (!classInstance.getSuperClasses().isEmpty()) {
			classInfo.setSuperClasses(classInstance
					.getSuperClasses()
						.stream()
						.map(ClassInstance::getId)
						.map(Serializable::toString)
						.collect(Collectors.toList()));
			return classInfo;
		}

		Optional<InstanceReference> owning = contextService.getContext(classInstance);
		owning.ifPresent(instanceReference -> classInfo.setSuperClasses(Collections.singletonList(instanceReference.getId())));

		return classInfo;
	}

	private static List<Ontology> extractOntologiesFromQueryResult(TupleQueryResult tupleQueryResult) {
		if (!tupleQueryResult.hasNext()) {
			return Collections.emptyList();
		}
		List<Ontology> ontologies = new LinkedList<>();
		while (tupleQueryResult.hasNext()) {
			BindingSet row = tupleQueryResult.next();
			String ontologyId = row.getValue(SPARQLQueryHelper.OBJECT).stringValue();

			// some ontologies have both title and label defined, so they are returned twice.
			// If the ontology has already been processed, avoid duplication, skipping the second one
			if (containsOntologyWithId(ontologies, ontologyId)) {
				continue;
			}
			// if title is not present for the ontology, its ID will be used
			String ontologyTitle = ontologyId;
			Value ontologyTitleValue = row.getValue(TITLE_KEY);
			if (ontologyTitleValue != null) {
				ontologyTitle = ontologyTitleValue.stringValue();
			}
			Ontology ontology = new Ontology(ontologyId, ontologyTitle);
			ontologies.add(ontology);
		}
		return ontologies;
	}

	private static boolean containsOntologyWithId(final List<Ontology> sourceList, final String id) {
		return sourceList.stream().anyMatch(o -> o.getId().equals(id));
	}
}
