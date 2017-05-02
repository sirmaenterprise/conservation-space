package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * {@link InstanceContextInitializer} implementation that queries the semantic database to fetch the parent context for
 * a given instance. The loading is done via single database query call.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SemanticContextInitializer implements InstanceContextInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private javax.enterprise.inject.Instance<TransactionalRepositoryConnection> connectionProvider;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;
	@Inject
	private InstanceTypeResolver typeResolver;
	@Inject
	private Statistics statistics;

	@Override
	public void restoreHierarchy(Instance selectedInstance) {
		if (selectedInstance == null) {
			return;
		}

		// we don't need to restore hierarchy for version instances at the moment(requirements). Also we'll need custom
		// logic for that, because version instances are not stored in the semantic
		if (InstanceVersionService.isVersion(selectedInstance.getId())) {
			selectedInstance.toReference().setParent(InstanceReference.ROOT_REFERENCE);
			return;
		}

		Collection<Serializable> parentIds = resolveParentIds(selectedInstance.getId().toString());

		Collection<Instance> parents = typeResolver.resolveInstances(parentIds);

		linkParents(selectedInstance, parents);
	}

	@Override
	public void restoreHierarchy(InstanceReference reference) {
		if (reference == null) {
			return;
		}

		// we don't need to restore hierarchy for version instances at the moment(requirements). Also we'll need custom
		// logic for that, because version instances are not stored in the semantic
		if (InstanceVersionService.isVersion(reference.getIdentifier())) {
			reference.setParent(InstanceReference.ROOT_REFERENCE);
			return;
		}

		Collection<Serializable> parentIds = resolveParentIds(reference.getIdentifier());

		Collection<InstanceReference> parents = typeResolver.resolveReferences(parentIds);

		linkParents(reference, parents);
	}

	private Collection<Serializable> resolveParentIds(String startInstanceId) {
		Map<Value, Set<Value>> hierarchyMapping = getHierarchy(startInstanceId);

		List<Value> hierarchyIds = restoreHierarchy(hierarchyMapping);

		return loadParents(hierarchyIds);
	}

	@SuppressWarnings("boxing")
	private Map<Value, Set<Value>> getHierarchy(String startInstanceId) {
		Map<Value, Set<Value>> hierarchyMapping = new HashMap<>();

		TimeTracker tracker = statistics.createTimeStatistics(getClass(), "resolveHierarchy").begin();
		URI initial = namespaceRegistryService.buildUri(startInstanceId);
		try (TransactionalRepositoryConnection connection = connectionProvider.get()) {

			String query = namespaceRegistryService.getNamespaces()
					+ SemanticQueries.QUERY_INSTANCE_HIERARCHY.getQuery();
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setIncludeInferred(true);
			tupleQuery.setBinding("initial", initial);
			executeQuery(tupleQuery, hierarchyMapping);

		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			LOGGER.warn("Could not fetch parents due to communication error", e);
		} finally {
			LOGGER.trace("Hierarchy resolution for instance {} took {} ms", startInstanceId, tracker.stop());
		}
		return hierarchyMapping;
	}

	private static void executeQuery(TupleQuery tupleQuery, Map<Value, Set<Value>> hierarchyMapping)
			throws QueryEvaluationException {
		try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(tupleQuery.evaluate())){
			for (BindingSet bindingSet : iterator) {
				Value parentValue = bindingSet.getBinding("parent").getValue();
				Binding binding = bindingSet.getBinding("parentOfParent");
				Value parentOfParentValue = null;
				if (binding != null) {
					parentOfParentValue = binding.getValue();
				}
				Set<Value> parents = hierarchyMapping.computeIfAbsent(parentValue, k -> new LinkedHashSet<>(8));
				addNonNullValue(parents, parentOfParentValue);
			}
		}
	}

	private Collection<Serializable> loadParents(List<Value> hierarchyIds) {
		return hierarchyIds
				.stream()
					.map(ValueConverter::convertValue)
					.filter(Objects::nonNull)
					.map(uri -> namespaceRegistryService.getShortUri(uri.toString()))
					.collect(Collectors.toList());
	}

	private static void linkParents(Instance selectedInstance, Collection<Instance> parents) {
		Instance current = selectedInstance;
		for (Instance instance : parents) {
			if (current instanceof OwnedModel) {
				// simultaneously build instance and instance reference hierarchy
				((OwnedModel) current).setOwningInstance(instance);
				((OwnedModel) current).setOwningReference(instance.toReference());
				current.toReference().setParent(instance.toReference());
			}
			current = instance;
		}
	}

	private static void linkParents(InstanceReference selectedInstance, Collection<InstanceReference> parents) {
		InstanceReference current = selectedInstance;
		for (InstanceReference reference : parents) {
			current.setParent(reference);
			current = reference;
		}
		// mark the last instance as root
		current.setParent(InstanceReference.ROOT_REFERENCE);
	}

	private static List<Value> restoreHierarchy(Map<Value, Set<Value>> hierarchyMapping) {
		if (hierarchyMapping.isEmpty()) {
			// we have top level instance without any parents
			return Collections.emptyList();
		}
		if (hierarchyMapping.size() == 1) {
			// we have only one element and the key is the single parent
			// this is instance with a parent that does not have any parents
			return Collections.singletonList(hierarchyMapping.keySet().iterator().next());
		}

		// For instance hierarchy instance1 -> instance2 -> instance3 -> instance4
		// The input data when searched for instance4 we have:
		// instance1 -> empty
		// instance2 -> instance1
		// instance3 -> instance1
		// instance3 -> instance2
		// that is transformed into:
		// instance1 -> []
		// instance2 -> [instance1]
		// instance3 -> [instance1, instance2]
		// we remove from hierarchyMapping entry that has empty value - this is the current root
		// and remove from all other sets the current root, the next root is the one with empty value
		// repeat
		// the result should be instance3 -> instance2 -> instance1

		List<Value> parentPath = new ArrayList<>(hierarchyMapping.size());
		for (Value root = getTopParent(hierarchyMapping); root != null; root = getNextParent(hierarchyMapping, root)) {
			addNonNullValue(parentPath, root);
		}
		Collections.reverse(parentPath);
		return parentPath;
	}

	private static Value getTopParent(Map<Value, Set<Value>> hierarchyMapping) {
		Value currentRoot = null;
		for (Entry<Value, Set<Value>> entry : hierarchyMapping.entrySet()) {
			if (isEmpty(entry.getValue())) {
				currentRoot = entry.getKey();
				break;
			}
		}
		if (currentRoot != null) {
			hierarchyMapping.remove(currentRoot);
		}
		return currentRoot;
	}

	private static Value getNextParent(Map<Value, Set<Value>> hierarchyMapping, Value toFind) {
		Value currentRoot = null;
		for (Entry<Value, Set<Value>> entry : hierarchyMapping.entrySet()) {
			Set<Value> set = entry.getValue();
			set.remove(toFind);
			if (isEmpty(set)) {
				currentRoot = entry.getKey();
				// does not stop here we need to iterate all elements
			}
		}
		if (currentRoot != null) {
			// we have an empty element that is the current root so remove it from the mapping
			hierarchyMapping.remove(currentRoot);
		}
		return currentRoot;
	}

}
