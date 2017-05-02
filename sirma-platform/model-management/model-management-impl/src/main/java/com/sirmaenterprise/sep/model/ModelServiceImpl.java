package com.sirmaenterprise.sep.model;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

/**
 * Default implementation of {@link ModelService}.
 * 
 * @author Vilizar Tsonev
 */
@ApplicationScoped
public class ModelServiceImpl implements ModelService {

	@Inject
	private javax.enterprise.inject.Instance<TransactionalRepositoryConnection> repositoryConnection;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private Statistics statistics;

	private static final String TITLE_KEY = "title";
	private static final String ONTOLOGY_KEY = "ontology";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String QUERY_ONTOLOGIES = ResourceLoadUtil.loadResource(ModelServiceImpl.class,
			"queryOntologies.sparql");

	@Override
	public List<Ontology> getOntologies() {
		TimeTracker timeTracker = statistics.createTimeStatistics(getClass(), "ontologiesRetrievalQueryExecution")
				.begin();
		try (TransactionalRepositoryConnection connection = repositoryConnection.get()) {
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection,
					QUERY_ONTOLOGIES, Collections.emptyMap(), false);
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
		if (StringUtils.isNullOrEmpty(ontologyId)) {
			return CollectionUtils.emptyList();
		}
		TimeTracker timeTracker = statistics.createTimeStatistics(getClass(), "getClassesForOntology").begin();
		List<ClassInstance> classes = semanticDefinitionService.getClassesForOntology(ontologyId);
		LOGGER.debug("{} classes for ontology [{}] retrieved for {} ms", classes.size(), ontologyId, timeTracker.stop());

		// if classes from the requested ontology have parents from other ontologies (external), add them to the result
		Stream<ClassInstance> externalSuperClasses = getExternalSuperClasses(ontologyId, classes);
		
		return Stream.concat(classes.stream(), externalSuperClasses)
				.map(ModelServiceImpl::toClassInfo)
				.collect(Collectors.toList());
	}

	private static Stream<ClassInstance> getExternalSuperClasses(String ontologyId,
			List<ClassInstance> retrievedClasses) {
		return retrievedClasses.stream()
				.filter(classInstance -> classInstance.getOwningInstance() != null)
				.filter(classInstance -> !ontologyId.equals(classInstance.getOwningInstance().getString(ONTOLOGY_KEY)))
				.map(classInstance -> (ClassInstance) classInstance.getOwningInstance())
				.distinct();
	}

	private static ClassInfo toClassInfo(ClassInstance classInstance) {
		ClassInfo classInfo = new ClassInfo().setId(classInstance.getId().toString()).setLabel(classInstance.getLabel())
				.setOntology(classInstance.getString(ONTOLOGY_KEY));
		if (classInstance.getOwningInstance() != null) {
			classInfo.setSuperClasses(Collections.singletonList(classInstance.getOwningInstance().getId().toString()));
		}
		return classInfo;
	}

	private static List<Ontology> extractOntologiesFromQueryResult(TupleQueryResult tupleQueryResult)
			throws QueryEvaluationException {
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
		return sourceList.stream().filter(o -> o.getId().equals(id)).findFirst().isPresent();
	}
}
