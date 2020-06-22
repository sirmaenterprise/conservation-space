package com.sirma.itt.emf.semantic.patch;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Patch removes duplicate emf:version properties.
 * It is executed ont two steps:
 * 1. Select all instances with more than one emf:version property.
 * 2. Remove all emf:properties except the biggest one.
 *
 * @author Boyan Tonchev.
 */
public class RemoveDuplicateEmfVersionProperties extends UpdateSemanticTask {

    /**
     * Select all instance which have more than one emf:version property query.
     * <pre>
     *     select ?instance ?version where {
     *        ?instance emf:version ?version .
     *        {
     *            select ?instance (count(*) as ?versionCount) {
     *                ?instance emf:version ?version.
     *            }
     *            group by ?instance
     *        }
     *        filter(?versionCount > 1).
     *     }
     * </pre>
     */
    private static final String DUPLICATE_EMF_VERSION_QUERY = "select ?instance "
            + "?version where { ?instance emf:version ?version . { select ?instance (count(*) as ?versionCount) "
            + "{ ?instance emf:version ?version. } group by ?instance } filter(?versionCount > 1). }";

    @Override
    public void execute(Database database) throws CustomChangeException {
        RepositoryConnection repositoryConnection = connectionFactory.produceConnection();
        Map<Value, List<Value>> instanceVersions = fetchInstancesWithDuplicateEmfVersionProperty(repositoryConnection);
        Map<Value, List<Value>> instanceVersionForDeletion = calculateVersionForDeletion(instanceVersions);
        Model removeModel = buildDeleteEmfVersionModel(instanceVersionForDeletion);
        try {
            SemanticPersistenceHelper.removeModel(repositoryConnection, removeModel, EMF.DATA_CONTEXT);
        } finally {
            connectionFactory.disposeConnection(repositoryConnection);
        }
    }

    /**
     * Execute {@link #DUPLICATE_EMF_VERSION_QUERY} and parsed it.
     *
     * @param repositoryConnection
     *         - Active connection to the repository.
     * @return map with key {@link Value} (instance id) and value list with {@link Value} (versions).
     */
    private static Map<Value, List<Value>> fetchInstancesWithDuplicateEmfVersionProperty(
            RepositoryConnection repositoryConnection) {
        TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, DUPLICATE_EMF_VERSION_QUERY,
                                                                    CollectionUtils.emptyMap(), false);
        Map<Value, List<Value>> instanceVersions = new HashMap<>();
        try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
            for (BindingSet bindingSet : iterator) {
                Value instanceId = bindingSet.getValue(SPARQLQueryHelper.OBJECT);
                Value value = bindingSet.getValue(EMF.VERSION.getLocalName());
                instanceVersions.computeIfAbsent(instanceId, v -> new ArrayList<>()).add(value);
            }
        }
        return instanceVersions;
    }

    /**
     * Calculate which versions have to be removed.
     *
     * @param instanceVersions
     *         - map with key {@link Value} (instance id) and value list with {@link Value} (versions).
     * @return map with key {@link Value} (instance id) and value list with {@link Value} (versions) which have to be removed.
     */
    private static Map<Value, List<Value>> calculateVersionForDeletion(Map<Value, List<Value>> instanceVersions) {
        Map<Value, List<Value>> instanceVersionForDeletion = new HashMap<>();
        instanceVersions.forEach((key, value) -> {
            List<Value> versionsOfInstance = getVersionsWithoutTheBiggest(value);
            if (!versionsOfInstance.isEmpty()) {
                instanceVersionForDeletion.put(key, versionsOfInstance);
            }
        });
        return instanceVersionForDeletion;
    }

    /**
     * Create {@link Model} with instance version which have to be deleted.
     *
     * @param instanceVersionsForDeletion
     *         - map with key {@link Value} (instance id) and value list with {@link Value} (versions) which have to be removed.
     * @return the create model for deletion.
     */
    private static Model buildDeleteEmfVersionModel(Map<Value, List<Value>> instanceVersionsForDeletion) {
        Model removeModel = new LinkedHashModel();
        instanceVersionsForDeletion.forEach(
                (key, value) -> value.forEach(version -> removeModel.add((IRI) key, EMF.VERSION, version)));
        return removeModel;
    }

    /**
     * Calculate the biggest version and skip it from result.
     *
     * @param versions
     *         - list with version.
     * @return list with versions without the biggest one.
     */
    private static List<Value> getVersionsWithoutTheBiggest(List<Value> versions) {
        Optional<Float> max = versions.stream().map(Value::stringValue).map(Float::valueOf).max(Float::compareTo);
        if (max.isPresent()) {
            String maxVersion = max.get().toString();
            return versions.stream()
                    .filter(value -> !value.stringValue().equals(maxVersion))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public String getConfirmationMessage() {
        return "Duplicated properties emf:version are removed!";
    }
}