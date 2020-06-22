package com.sirma.seip.concept;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.search.SearchService;

/**
 * Default implementation of {@link ConceptService}.
 *
 * @author Vilizar Tsonev
 */
@Singleton
public class ConceptServiceImpl implements ConceptService {

	@Inject
	private SearchService searchService;

	private static final String SET_TO_OPERATION = "set_to";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public List<Concept> getConceptsByScheme(String schemeId) {
		if (StringUtils.isBlank(schemeId)) {
			throw new IllegalArgumentException("Unable to fetch concept hierarchy if schemeId is not provided");
		}

		Rule searchRule = SearchCriteriaBuilder.createRuleBuilder()
				.setType(SKOS.CONCEPT.toString())
				.setOperation(SET_TO_OPERATION)
				.setField(SKOS.PREFIX + ":" + SKOS.IN_SCHEME.getLocalName())
				.setValues(Arrays.asList(schemeId))
				.build();

		return restoreConceptHierarchy(findConcepts(searchRule));
	}

	@Override
	public List<Concept> getConceptsByBroader(String broaderId) {
		if (StringUtils.isBlank(broaderId)) {
			throw new IllegalArgumentException("Unable to fetch concept hierarchy if broaderId is not provided");
		}

		Rule searchRule = SearchCriteriaBuilder.createRuleBuilder()
				.setType(SKOS.CONCEPT.toString())
				.setOperation(SET_TO_OPERATION)
				.setField(SKOS.PREFIX + ":" + SKOS.BROADER_TRANSITIVE.getLocalName())
				.setValues(Arrays.asList(broaderId))
				.build();

		return restoreConceptHierarchy(findConcepts(searchRule));
	}

	private static Concept toConcept(Instance instance) {
		String id = (String) instance.getId();
		String title = instance.getAsString(DefaultProperties.TITLE);
		String broader = getInstanceBroader(instance);
		return new Concept(id, title, new ArrayList<>(), broader);
	}

	private List<Instance> findConcepts(Rule rule) {
		Condition tree = SearchCriteriaBuilder.createConditionBuilder().setRules(Collections.singletonList(rule))
				.build();
		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(2));
		searchRequest.setSearchTree(tree);
		SearchArguments<Instance> searchArgs = searchService.parseRequest(searchRequest);
		searchArgs.setPageSize(searchArgs.getMaxSize());
		searchArgs.setPageNumber(1);
		searchService.searchAndLoad(Instance.class, searchArgs);

		return searchArgs.getResult();
	}

	private static List<Concept> restoreConceptHierarchy(List<Instance> conceptInstances) {

		List<Concept> concepts = conceptInstances.stream()
				.map(ConceptServiceImpl::toConcept)
				.collect(Collectors.toList());

		Map<String, Concept> index = CollectionUtils.toIdentityMap(concepts, Concept::getId);

		logConceptHierarchyCycles(index);

		for (Concept concept : concepts) {
			Concept parent = index.get(concept.getParent());
			if (parent != null) {
				parent.getAncestors().add(concept);
			} else {
				// if the parent is not present in the found concepts, the current one is the
				// top-level concept for that sub-tree
				concept.setParent(null);
			}
		}
		// return only the top-level concepts (the roots of the (sub)trees). Returning only the concepts with a null
		// parent will also ensure that (sub)trees that form cycles will be filtered out (in a cycle, there is no null
		// parent).
		return index.values()
				.stream()
				.filter(concept -> concept.getParent() == null)
				.collect(Collectors.toList());
	}

	private static String getInstanceBroader(Instance instance) {
		List<String> broaders = new ArrayList<>(
				instance.getAsCollection(SKOS.BROADER.getLocalName(), ArrayList::new));
		if (!broaders.isEmpty()) {
			return broaders.get(0);
		}
		return null;
	}

	private static void logConceptHierarchyCycles(Map<String, Concept> concepts) {
		for (String conceptId : concepts.keySet()) {
			List<String> visited = new ArrayList<>();
			String current = conceptId;

			while (current != null) {
				if (visited.contains(current)) {
					visited.add(current);
					String visitedNodes = StringUtils.join(visited, " -> ");
					LOGGER.warn("Concept [{}] contains hierarchy cycle and will be skipped: {}", conceptId,
							visitedNodes);
					break;
				}
				visited.add(current);
				Concept currentConcept = concepts.get(current);
				if (currentConcept != null) {
					current = currentConcept.getParent();
				} else {
					current = null;
				}
			}
		}
	}
}
