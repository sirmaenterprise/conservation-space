package com.sirma.seip.concept;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
 * Tests {@link ConceptServiceImpl}.
 *
 * @author Vilizar Tsonev
 */
public class ConceptServiceImplTest {

	@Mock
	private SearchService searchService;

	@InjectMocks
	private ConceptServiceImpl conceptService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_If_Scheme_Not_Provided() {
		conceptService.getConceptsByScheme("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_If_Broader_Not_Provided() {
		conceptService.getConceptsByBroader("");
	}

	@Test
	public void should_Filter_Out_Concept_Branches_With_Cycles() {
		Instance eiffelTower = constructConcept("concept-Eiffel_Tower", "Eiffel tower", "concept-Paris");
		Instance paris = constructConcept("concept-Paris", "Paris", "concept-France");
		Instance france = constructConcept("concept-France", "France", "concept-Europe");
		Instance europe = constructConcept("concept-Europe", "Europe", "concept-Eiffel_Tower");

		Instance asia = constructConcept("concept-Asia", "Asia", "concept-world");
		Instance china = constructConcept("concept-China", "China", "concept-Asia");
		Instance beijing = constructConcept("concept-Beijing", "Beijing", "concept-China");

		Instance northAmerica = constructConcept("concept-North-America", "North America", "concept-Toronto");
		Instance canada = constructConcept("concept-Canada", "Canada", "concept-North-America");
		Instance toronto = constructConcept("concept-Toronto", "Toronro", "concept-Canada");

		withExistingConcepts(
				Arrays.asList(eiffelTower, paris, france, europe, asia, china, beijing, northAmerica, canada, toronto));

		// expected hierarchy (Europe and N.America contain cycles and should be filtered out)
		Concept conceptBeijing = new Concept("concept-Beijing", "Beijing", Collections.emptyList(), "concept-China");
		Concept conceptChina = new Concept("concept-China", "China", Arrays.asList(conceptBeijing), "concept-Asia");
		Concept conceptAsia = new Concept("concept-Asia", "Asia", Arrays.asList(conceptChina), null);

		List<Concept> hierarchy = conceptService.getConceptsByScheme("scheme-Geography");
		assertEquals(Arrays.asList(conceptAsia), hierarchy);
	}

	@Test
	public void should_Properly_Restore_The_Concept_Hierarchy() {
		Instance eiffelTower = constructConcept("concept-Eiffel_Tower", "Eiffel tower", "concept-Paris");
		Instance paris = constructConcept("concept-Paris", "Paris", "concept-France");
		Instance lyon = constructConcept("concept-Lyon", "Lyon", "concept-France");
		Instance france = constructConcept("concept-France", "France", "concept-Europe");
		Instance europe = constructConcept("concept-Europe", "Europe", "concept-world");

		Instance asia = constructConcept("concept-Asia", "Asia", "concept-world");
		Instance china = constructConcept("concept-China", "China", "concept-Asia");
		Instance beijing = constructConcept("concept-Beijing", "Beijing", "concept-China");

		Instance northAmerica = constructConcept("concept-North-America", "North America", "concept-world");
		Instance canada = constructConcept("concept-Canada", "Canada", "concept-North-America");
		Instance toronto = constructConcept("concept-Toronto", "Toronro", "concept-Canada");

		withExistingConcepts(
				Arrays.asList(eiffelTower, paris, lyon, france, europe, asia, china, beijing, northAmerica, canada,
						toronto));

		Concept conceptBeijing = new Concept("concept-Beijing", "Beijing", Collections.emptyList(), "concept-China");
		Concept conceptChina = new Concept("concept-China", "China", Arrays.asList(conceptBeijing), "concept-Asia");
		Concept conceptAsia = new Concept("concept-Asia", "Asia", Arrays.asList(conceptChina), null);

		Concept conceptEiffelTower = new Concept("concept-Eiffel_Tower", "Eiffel tower", new ArrayList<Concept>(),
				"concept-Paris");
		Concept conceptParis = new Concept("concept-Paris", "Paris", Arrays.asList(conceptEiffelTower),
				"concept-France");
		Concept conceptLyon = new Concept("concept-Lyon", "Lyon", new ArrayList<Concept>(), "concept-France");
		Concept conceptFrance = new Concept("concept-France", "France", Arrays.asList(conceptParis, conceptLyon),
				"concept-Europe");
		Concept conceptEurope = new Concept("concept-Europe", "Europe", Arrays.asList(conceptFrance), null);

		Concept conceptToronto = new Concept("concept-Toronto", "Toronro", new ArrayList<Concept>(), "concept-Canada");
		Concept conceptCanada = new Concept("concept-Canada", "Canada", Arrays.asList(conceptToronto),
				"concept-North-America");
		Concept conceptNorthAmerica = new Concept("concept-North-America", "North America",
				Arrays.asList(conceptCanada), null);

		List<Concept> hierarchy = conceptService.getConceptsByScheme("scheme-Geography");
		assertEquals(Arrays.asList(conceptAsia, conceptEurope, conceptNorthAmerica), hierarchy);
	}

	@Test
	public void should_Build_Correct_Search_Tree_When_Searching_By_Scheme() {
		withExistingConcepts(CollectionUtils.emptyList());
		conceptService.getConceptsByScheme("scheme-Geography");

		ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
		verify(searchService).parseRequest(captor.capture());

		Rule expectedRule = SearchCriteriaBuilder.createRuleBuilder()
				.setType(SKOS.CONCEPT.toString()).setOperation("set_to")
				.setField(SKOS.PREFIX + ":" + SKOS.IN_SCHEME.getLocalName())
				.setValues(Arrays.asList("scheme-Geography")).build();

		SearchRequest request = captor.getValue();
		Condition actualCondition = request.getSearchTree();
		Rule actualRule = (Rule) actualCondition.getRules().get(0);

		assertEquals(expectedRule.getType(), actualRule.getType());
		assertEquals(expectedRule.getField(), actualRule.getField());
		assertEquals(expectedRule.getValues(), actualRule.getValues());
		assertEquals(expectedRule.getOperation(), actualRule.getOperation());
	}

	@Test
	public void should_Build_Correct_Search_Tree_When_Searching_By_Broader() {
		withExistingConcepts(CollectionUtils.emptyList());
		conceptService.getConceptsByBroader("sample-Concept");

		ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
		verify(searchService).parseRequest(captor.capture());

		Rule expectedRule = SearchCriteriaBuilder.createRuleBuilder().setType(SKOS.CONCEPT.toString())
				.setOperation("set_to").setField(SKOS.PREFIX + ":" + SKOS.BROADER_TRANSITIVE.getLocalName())
				.setValues(Arrays.asList("sample-Concept"))
				.build();

		SearchRequest request = captor.getValue();
		Condition actualCondition = request.getSearchTree();
		Rule actualRule = (Rule) actualCondition.getRules().get(0);

		assertEquals(expectedRule.getType(), actualRule.getType());
		assertEquals(expectedRule.getField(), actualRule.getField());
		assertEquals(expectedRule.getValues(), actualRule.getValues());
		assertEquals(expectedRule.getOperation(), actualRule.getOperation());
	}

	@Test
	public void should_Consider_Concepts_With_Empty_Broaders_As_Roots() {
		Instance asia = constructConcept("concept-Asia", "Asia", null);
		Instance china = constructConcept("concept-China", "China", "concept-Asia");
		Instance beijing = constructConcept("concept-Beijing", "Beijing", "concept-China");

		Instance northAmerica = constructConcept("concept-North-America", "North America", null);
		Instance canada = constructConcept("concept-Canada", "Canada", "concept-North-America");
		Instance toronto = constructConcept("concept-Toronto", "Toronro", "concept-Canada");

		withExistingConcepts(Arrays.asList(asia, china, beijing, northAmerica, canada, toronto));

		Concept conceptBeijing = new Concept("concept-Beijing", "Beijing", Collections.emptyList(), "concept-China");
		Concept conceptChina = new Concept("concept-China", "China", Arrays.asList(conceptBeijing), "concept-Asia");
		Concept conceptAsia = new Concept("concept-Asia", "Asia", Arrays.asList(conceptChina), null);

		Concept conceptToronto = new Concept("concept-Toronto", "Toronro", new ArrayList<Concept>(), "concept-Canada");
		Concept conceptCanada = new Concept("concept-Canada", "Canada", Arrays.asList(conceptToronto),
				"concept-North-America");
		Concept conceptNorthAmerica = new Concept("concept-North-America", "North America",
				Arrays.asList(conceptCanada), null);

		List<Concept> hierarchy = conceptService.getConceptsByScheme("scheme-Geography");
		assertEquals(Arrays.asList(conceptAsia, conceptNorthAmerica), hierarchy);
	}

	private static Instance constructConcept(String id, String title, String broader) {
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(id);
		when(instance.getAsString(DefaultProperties.TITLE)).thenReturn(title);
		if (StringUtils.isNotBlank(broader)) {
			when(instance.getAsCollection(eq(SKOS.BROADER.getLocalName()), any())).thenReturn(Arrays.asList(broader));
		} else {
			when(instance.getAsCollection(eq(SKOS.BROADER.getLocalName()), any()))
					.thenReturn(CollectionUtils.emptyList());
		}
		return instance;
	}

	private void withExistingConcepts(List<Instance> conceptInstances) {
		SearchArguments<Instance> argumentsToReturn = new SearchArguments<>();
		argumentsToReturn.setResult(conceptInstances);
		when(searchService.parseRequest(any(SearchRequest.class))).thenReturn(argumentsToReturn);
	}
}
