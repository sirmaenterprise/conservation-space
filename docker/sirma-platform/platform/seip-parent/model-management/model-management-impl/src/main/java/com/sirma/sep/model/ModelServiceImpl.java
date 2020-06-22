package com.sirma.sep.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.util.file.ArchiveUtil;
import com.sirma.itt.semantic.NamespaceRegistryService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFWriter;
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
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.semantic.ReadOnly;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurations;

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

	private static final Set<IRI> SKIPPED_PREDICATES = Stream.of(EMF.CREATED_BY, EMF.CREATED_ON, EMF.MODIFIED_ON,
			EMF.MODIFIED_BY, EMF.VERSION).collect(Collectors.toSet());

	static final String QUERY_ONTOLOGIES = ResourceLoadUtil.loadResource(ModelServiceImpl.class,
			"queryOntologies.sparql");

	@Inject
	@ReadOnly
	private RepositoryConnection repositoryConnection;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private InstanceContextService contextService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private ModelManagementDeploymentConfigurations modelManagementDeploymentConfigurations;

	@Override
	@Monitored(@MetricDefinition(name = "ontology_retrieve_duration_seconds", type = Type.TIMER, descr = "Retrieval of all ontologies duration in seconds."))
	public List<Ontology> getOntologies() {
		try {
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, QUERY_ONTOLOGIES,
					Collections.emptyMap(), false);
			try (TupleQueryResultIterator queryResult = new TupleQueryResultIterator(tupleQuery.evaluate())) {
				return extractOntologiesFromQueryResult(queryResult);
			}
		} catch (QueryEvaluationException | RepositoryException e) {
			throw new SemanticPersistenceException("Failed evaluating query for ontologies retrieval", e);
		}
	}

	@Override
	@Monitored(@MetricDefinition(name = "ontology_class_retrieve_duration_seconds", type = Type.TIMER, descr = "Retrieval of all classes for ontology duration in seconds."))
	public List<ClassInfo> getClassesForOntology(String ontologyId) {
		if (StringUtils.isBlank(ontologyId)) {
			return CollectionUtils.emptyList();
		}
		List<ClassInstance> classes = semanticDefinitionService.getClassesForOntology(ontologyId);
		LOGGER.debug("{} classes for ontology [{}] retrieved.", classes.size(), ontologyId);

		// if classes from the requested ontology have parents from other ontologies (external), add them to the result
		Stream<ClassInstance> externalSuperClasses = getExternalSuperClasses(ontologyId, classes);
		return Stream.concat(classes.stream(), externalSuperClasses).map(this::toClassInfo).collect(
				Collectors.toList());
	}

	@Override
	public File exportOntologies(List<Ontology> ontologies) {
		File ontologiesDir = tempFileProvider.createUniqueTempDir("ONTOLOGIES");

		ontologies.forEach(ontology -> exportSingleOntology(ontology, ontologiesDir));
		return zipOntologiesDir(ontologiesDir);
	}

	private void exportSingleOntology(Ontology ontology, File ontologiesDir) {
		IRI ontologyIri = namespaceRegistryService.buildUri(ontology.getId());
		String filename = createFileName(ontologyIri);
		Boolean prettyPrintEnabled = modelManagementDeploymentConfigurations.getPrettyPrintEnabled()
				.computeIfNotSet(() -> Boolean.TRUE);

		try (OutputStream outputStream = new FileOutputStream(new File(ontologiesDir, filename))) {
			SepTurtleWriter statementWriter = new SepTurtleWriter(outputStream);
			if (prettyPrintEnabled) {
				statementWriter.setPrettyPrintEnabled();
			}
			RDFWriter filteredWriter = new FilteringRDFWriter(statementWriter).setStatementFilter(filerSystemPredicates());
			repositoryConnection.export(filteredWriter, ontologyIri);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Predicate<Statement> filerSystemPredicates() {
		return st -> !SKIPPED_PREDICATES.contains(st.getPredicate());
	}

	private String createFileName(IRI ontologyIri) {
		return URI.create(ontologyIri.toString())
				.getPath()
				// remove any path separators
				.replaceAll("[/#:]+", " ")
				// remove any dates from the URI
				.replaceAll("\\d+", "")
				// remove any leading and trailing white spaces
				.trim()
				// remove intermediate white spaces
				.replaceAll("\\s+", "_") + ".ttl";
	}

	private File zipOntologiesDir(File ontologiesDir) {
		// the empty zip file is created in a separate dir, because otherwise it will archive itself
		File archiveDir = tempFileProvider.createUniqueTempDir("modelArchiveDir");
		File ontologiesZip = new File(archiveDir, "ontologies.zip");
		ArchiveUtil.zipFile(ontologiesDir, ontologiesZip);
		return ontologiesZip;
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
