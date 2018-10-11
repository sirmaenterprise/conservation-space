package com.sirma.sep.instance.suggest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.ControlParamMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Tests for {@link InstanceSuggestRestService}
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class InstanceSuggestRestServiceTest {

	private static final String DEFINITION_ID = "DT0004";
	private static final String KEYWORDS = "key words";
	private static final String URI = "emf:rangedUri";

	private static final String PROPERTY_NAME_WITH_RANGE = "propertyNameWithRange";
	private static final String PROPERTY_NAME_WITH_SEMANTIC_RESTRICTION = "propertyNameWithSemanticRestriction";
	private static final String PROPERTY_NAME_WITHOUT_RANGE = "propertyNameWithoutRange";
	private static final String MISSING_PROPERTY = "missingProperty";
	private static final String PROPERTY_NAME_WITH_RESTRICTION = "propertyNameWithRestriction";

	private static final String RULE_RESTRICTION_VALUE = "{}";

	@Mock
	private SearchArguments searchArguments;

	@Mock
	private DefinitionModel model;

	@Mock
	private SearchService searchService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private ConfigurationProperty<Integer> instanceSuggestResultsCount;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private JsonToConditionConverter jsonToConditionConverter;

	@InjectMocks
	private InstanceSuggestRestService instanceSuggestRestService;

	@Before
	public void init() {
		Mockito.when(definitionService.find(DEFINITION_ID)).thenReturn(model);
		Mockito.when(model.getField(PROPERTY_NAME_WITH_RANGE)).thenReturn(createPropertyWithRange());
		Mockito.when(model.getField(PROPERTY_NAME_WITH_SEMANTIC_RESTRICTION))
				.thenReturn(createPropertyWithUri(URI));
		Mockito.when(model.getField(PROPERTY_NAME_WITHOUT_RANGE)).thenReturn(createPropertyWithoutRange());
		Mockito.when(model.getField(MISSING_PROPERTY)).thenReturn(Optional.empty());
		Mockito.when(model.getField(PROPERTY_NAME_WITH_RESTRICTION)).thenReturn(createPropertyWithRestriction());
		Mockito.when(instanceSuggestResultsCount.get()).thenReturn(3);
		Mockito.when(searchService.parseRequest(Matchers.any())).thenReturn(searchArguments);
	}

	@Test
	public void should_executeSearchWithSemanticRangeSearchClause_When_UriOfPropertyHasRdfs_Range() {
		InstanceSuggestRelationsRequest instanceSuggestRelationsRequest = createRequest(DEFINITION_ID,
																						PROPERTY_NAME_WITH_SEMANTIC_RESTRICTION,
																						KEYWORDS);
		PropertyInstance propertyInstance = Mockito.mock(PropertyInstance.class);
		Mockito.when(propertyInstance.getRangeClass()).thenReturn("emf:Document");

		Mockito.when(semanticDefinitionService.getRelation(URI)).thenReturn(propertyInstance);

		instanceSuggestRestService.suggestRelation(instanceSuggestRelationsRequest);

		Mockito.verify(searchService)
				.parseRequest(Mockito.argThat(matchSearchWithSemanticRange(KEYWORDS, "emf:Document")));
	}

	@Test
	public void should_HaveErrorMessage_When_SearchThrowsException() {
		InstanceSuggestRelationsRequest instanceSuggestRelationsRequest = createRequest(DEFINITION_ID,
																						PROPERTY_NAME_WITH_RANGE,
																						KEYWORDS);
		Mockito.doAnswer((invocation) -> {
			throw new EmfRuntimeException();
		}).when(searchService).searchAndLoad(Matchers.any(), Matchers.any());

		instanceSuggestRestService.suggestRelation(instanceSuggestRelationsRequest);

		Mockito.verify(searchArguments).setSearchError(Matchers.any());
	}

	@Test
	public void should_executeSearchWithTypeCondition_When_PropertyDefinitionHasControlParamRange() {
		InstanceSuggestRelationsRequest instanceSuggestRelationsRequest = createRequest(DEFINITION_ID,
																						PROPERTY_NAME_WITH_RANGE,
																						KEYWORDS);
		instanceSuggestRestService.suggestRelation(instanceSuggestRelationsRequest);

		Mockito.verify(searchService).parseRequest(Mockito.argThat(matchSearchWithRange(KEYWORDS)));
		Mockito.verify(searchArguments).setMaxSize(3);
	}

	@Test
	public void should_executeSearchWithoutTypeCondition_When_PropertyDefinitionHasNotControlParamRange() {
		InstanceSuggestRelationsRequest instanceSuggestRelationsRequest = createRequest(DEFINITION_ID,
																						PROPERTY_NAME_WITHOUT_RANGE,
																						KEYWORDS);
		instanceSuggestRestService.suggestRelation(instanceSuggestRelationsRequest);

		Mockito.verify(searchService).parseRequest(Mockito.argThat(matchSearchWithoutRange(KEYWORDS)));
	}

	@Test
	public void should_executeSearchWithoutTypeCondition_When_PropertyDefinitionIsNotFound() {
		InstanceSuggestRelationsRequest instanceSuggestRelationsRequest = createRequest(DEFINITION_ID, MISSING_PROPERTY,
																						KEYWORDS);
		instanceSuggestRestService.suggestRelation(instanceSuggestRelationsRequest);

		Mockito.verify(searchService).parseRequest(Mockito.argThat(matchSearchWithoutRange(KEYWORDS)));
	}

	@Test
	public void should_executeSearchWithRestrictionCondition_When_ThereIsPropertyConfiguredWithRestriction() {
		InstanceSuggestRelationsRequest instanceSuggestRelationsRequest = createRequest(DEFINITION_ID,
																						PROPERTY_NAME_WITH_RESTRICTION,
																						KEYWORDS);

		Condition restrictionsCondition = Mockito.mock(Condition.class);
		Mockito.when(jsonToConditionConverter.parseCondition(Matchers.any())).thenReturn(restrictionsCondition);

		instanceSuggestRestService.suggestRelation(instanceSuggestRelationsRequest);

		Mockito.verify(searchService).parseRequest(Mockito.argThat(matchSearchWithRestriction(restrictionsCondition, KEYWORDS)));
	}

	private InstanceSuggestRelationsRequest createRequest(String definitionId, String propertyName, String keywords) {
		InstanceSuggestRelationsRequest request = new InstanceSuggestRelationsRequest();
		request.setDefinitionId(definitionId);
		request.setPropertyName(propertyName);
		request.setKeywords(keywords);
		return request;
	}

	private CustomMatcher<SearchRequest> matchSearchWithSemanticRange(String keywords, String rdfRange) {
		return CustomMatcher.of((searchRequest) -> {
			Condition searchTree = searchRequest.getSearchTree();
			List<SearchNode> rules = searchTree.getRules();
			Assert.assertEquals(2, rules.size());
			Rule searchNode = (Rule) rules.get(0);

			Assert.assertEquals("suggest", searchNode.getOperation());
			Assert.assertEquals("altTitle", searchNode.getField());
			Assert.assertEquals("", searchNode.getType());
			Assert.assertEquals(1, searchNode.getValues().size());
			Assert.assertEquals(keywords, searchNode.getValues().get(0));

			Rule semanticSearchNode = (Rule) ((Condition) rules.get(1)).getRules().get(0);

			Assert.assertEquals("equals", semanticSearchNode.getOperation());
			Assert.assertEquals("types", semanticSearchNode.getField());
			Assert.assertEquals("", semanticSearchNode.getType());
			Assert.assertEquals(1, semanticSearchNode.getValues().size());
			Assert.assertEquals(rdfRange, semanticSearchNode.getValues().get(0));
		});
	}

	private CustomMatcher<SearchRequest> matchSearchWithoutRange(String keywords) {
		return CustomMatcher.of((searchRequest) -> {
			Condition searchTree = searchRequest.getSearchTree();
			List<SearchNode> rules = searchTree.getRules();
			Assert.assertEquals(1, rules.size());
			Rule searchNode = (Rule) rules.get(0);

			Assert.assertEquals("suggest", searchNode.getOperation());
			Assert.assertEquals("altTitle", searchNode.getField());
			Assert.assertEquals("", searchNode.getType());
			Assert.assertEquals(1, searchNode.getValues().size());
			Assert.assertEquals(keywords, searchNode.getValues().get(0));
		});
	}

	private CustomMatcher<SearchRequest> matchSearchWithRange(String keywords) {
		return CustomMatcher.of((searchRequest) -> {
			Condition searchTree = searchRequest.getSearchTree();
			List<SearchNode> rules = searchTree.getRules();
			Assert.assertEquals(2, rules.size());

			// Verifies keywords search clause
			Rule searchNode = (Rule) rules.get(0);

			Assert.assertEquals("suggest", searchNode.getOperation());
			Assert.assertEquals("altTitle", searchNode.getField());
			Assert.assertEquals("", searchNode.getType());
			Assert.assertEquals(1, searchNode.getValues().size());
			Assert.assertEquals(keywords, searchNode.getValues().get(0));

			Condition rangeSearchClauses = (Condition) rules.get(1);

			// Verifies type user search clause
			Rule userSearchClause = (Rule) rangeSearchClauses.getRules().get(0);
			Assert.assertEquals("equals", userSearchClause.getOperation());
			Assert.assertEquals("types", userSearchClause.getField());
			Assert.assertEquals("", userSearchClause.getType());
			Assert.assertEquals(1, userSearchClause.getValues().size());
			Assert.assertEquals("emf:User", userSearchClause.getValues().get(0));

			// Verifies type document search clause
			Rule documentSearchClause = (Rule) rangeSearchClauses.getRules().get(1);
			Assert.assertEquals("equals", documentSearchClause.getOperation());
			Assert.assertEquals("types", documentSearchClause.getField());
			Assert.assertEquals("", documentSearchClause.getType());
			Assert.assertEquals(1, documentSearchClause.getValues().size());
			Assert.assertEquals("emf:Document", documentSearchClause.getValues().get(0));

		});
	}

	private CustomMatcher<SearchRequest> matchSearchWithRestriction(Condition restrictionsCondition, String keywords) {
		return CustomMatcher.of((searchRequest) -> {
			Condition searchTree = searchRequest.getSearchTree();
			List<SearchNode> rules = searchTree.getRules();
			Assert.assertEquals(2, rules.size());
			Rule searchNode = (Rule) rules.get(0);
			Assert.assertEquals("suggest", searchNode.getOperation());
			Assert.assertEquals("altTitle", searchNode.getField());
			Assert.assertEquals("", searchNode.getType());
			Assert.assertEquals(1, searchNode.getValues().size());
			Assert.assertEquals(keywords, searchNode.getValues().get(0));

			Assert.assertEquals(restrictionsCondition, rules.get(1));
		});
	}

	private Optional<PropertyDefinition> createPropertyWithUri(String uri) {
		Optional<PropertyDefinition> property = createPropertyWithoutRange();
		property.ifPresent(propertyDefinition -> ((PropertyDefinitionMock) property.get()).setUri(uri));
		return property;
	}

	private Optional<PropertyDefinition> createPropertyWithRange() {
		PropertyDefinitionMock propertyWithRange = new PropertyDefinitionMock();
		propertyWithRange.setName(PROPERTY_NAME_WITH_RANGE);

		ControlParamMock rangeControlParam = new ControlParamMock();
		rangeControlParam.setName("range");
		rangeControlParam.setValue("emf:User, emf:Document");

		ControlDefintionMock controlDefintionMock = new ControlDefintionMock();
		controlDefintionMock.setControlParams(Collections.singletonList(rangeControlParam));

		propertyWithRange.setControlDefinition(controlDefintionMock);

		return Optional.of(propertyWithRange);
	}

	private Optional<PropertyDefinition> createPropertyWithoutRange() {
		PropertyDefinitionMock propertyWithoutRange = new PropertyDefinitionMock();
		propertyWithoutRange.setName(PROPERTY_NAME_WITHOUT_RANGE);

		ControlDefintionMock controlDefintionMock = new ControlDefintionMock();

		propertyWithoutRange.setControlDefinition(controlDefintionMock);

		return Optional.of(propertyWithoutRange);
	}

	private Optional<PropertyDefinition> createPropertyWithRestriction() {
		PropertyDefinitionMock propertyWithoutRange = new PropertyDefinitionMock();
		propertyWithoutRange.setName(PROPERTY_NAME_WITHOUT_RANGE);

		ControlParamMock rangeControlParam = new ControlParamMock();
		rangeControlParam.setName("restrictions");
		rangeControlParam.setValue(RULE_RESTRICTION_VALUE);

		ControlDefintionMock controlDefintionMock = new ControlDefintionMock();
		controlDefintionMock.setControlParams(Collections.singletonList(rangeControlParam));

		propertyWithoutRange.setControlDefinition(controlDefintionMock);

		return Optional.of(propertyWithoutRange);
	}
}