package com.sirma.sep.instance.suggest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;

/**
 * Services which gives opportunity of instance suggestions.
 *
 * @author Boyan Tonchev.
 */
@Path("/relations/suggest")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstanceSuggestRestService {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "instance.suggest.results.count", defaultValue = "10", type = Integer.class,
			label = "Defines how many instance relation suggestions have to be returned.")
	private ConfigurationProperty<Integer> instanceSuggestResultsCount;

	@Inject
	private SearchService searchService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private JsonToConditionConverter jsonToConditionConverter;

	/**
	 * This service gives suggestion of instances which can be added as property to a field of instance.
	 *
	 * @param instanceSuggestRelationsRequest - object holder for property name of the field, definition id where
	 *                                        the field is described and keywords used in search.
	 * @return - {@link SearchArguments} object which hold instances which can be added as property to the requested field.
	 */
	@POST
	public SearchArguments<Instance> suggestRelation(InstanceSuggestRelationsRequest instanceSuggestRelationsRequest) {
		SearchRequest request = createSearchRequest(instanceSuggestRelationsRequest.getDefinitionId(),
													instanceSuggestRelationsRequest.getPropertyName(),
													instanceSuggestRelationsRequest.getKeywords());
		SearchArguments<Instance> searchArguments = searchService.parseRequest(request);
		// Remove sorters if any.
		searchArguments.getSorters().clear();
		searchArguments.setMaxSize(instanceSuggestResultsCount.get());
		searchArguments.setPageSize(instanceSuggestResultsCount.get());
		try {
			searchService.searchAndLoad(Instance.class, searchArguments);
		} catch (Exception e) {
			searchArguments.setSearchError(e);
		}
		return searchArguments;
	}

	private SearchRequest createSearchRequest(String definitionId, String propertyName, String keyword) {
		ConditionBuilder conditionBuilder = new ConditionBuilder();
		conditionBuilder.setCondition(Condition.Junction.AND);
		conditionBuilder.addRule(createKeyWordsSearchClause(keyword));

		DefinitionModel model = definitionService.find(definitionId);
		model.getField(propertyName).ifPresent(propertyDefinition -> {
			createRestrictionSearchClause(propertyDefinition).ifPresent(conditionBuilder::addRule);
			createDefinitionRangeSearchClauses(propertyDefinition).ifPresent(rangeSearchClauses -> {
				ConditionBuilder typeConditionBuilder = new ConditionBuilder();
				typeConditionBuilder.setCondition(Condition.Junction.OR);
				typeConditionBuilder.setRules(rangeSearchClauses);
				conditionBuilder.addRule(typeConditionBuilder.build());
			});

			createSemanticRangeSearchClause(propertyDefinition).ifPresent(semanticRangeSearchClause -> {
				ConditionBuilder semanticSearchConditionBuilder = new ConditionBuilder();
				semanticSearchConditionBuilder.setCondition(Condition.Junction.AND);
				semanticSearchConditionBuilder.addRule(semanticRangeSearchClause);
				conditionBuilder.addRule(semanticSearchConditionBuilder.build());
			});
		});

		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(3));
		searchRequest.setSearchTree(conditionBuilder.build());
		return searchRequest;
	}

	private static Optional<List<SearchNode>> createDefinitionRangeSearchClauses(
			PropertyDefinition propertyDefinition) {
		return Optional.ofNullable(propertyDefinition.getControlDefinition())
				.flatMap(controlDefinition -> controlDefinition.getParam("range"))
				.map(ControlParam::getValue)
				.filter(StringUtils::isNotBlank)
				.map(typeRange -> Arrays.stream(typeRange.split(","))
						.map(String::trim)
						.filter(StringUtils::isNotBlank)
						.map(InstanceSuggestRestService::createRangeSearchClause)
						.collect(Collectors.toList()));
	}

	private Optional<Rule> createSemanticRangeSearchClause(PropertyDefinition propertyDefinition) {
		return Optional.ofNullable(propertyDefinition.getUri())
				.filter(StringUtils::isNotBlank)
				.map(semanticDefinitionService::getRelation)
				.map(PropertyInstance::getRangeClass)
				.map(InstanceSuggestRestService::createRangeSearchClause);
	}

	private Optional<Condition> createRestrictionSearchClause(PropertyDefinition propertyDefinition) {
		return Optional.ofNullable(propertyDefinition.getControlDefinition())
				.flatMap(controlDefinition -> controlDefinition.getParam("restrictions"))
				.map(ControlParam::getValue)
				.map(restriction -> JSON.readObject(restriction, jsonToConditionConverter::parseCondition));
	}

	private static Rule createRangeSearchClause(String value) {
		return new RuleBuilder()
				.setType("")
				.setField("types")
				.setOperation("equals")
				.addValue(value)
				.build();
	}

	private static Rule createKeyWordsSearchClause(String keyword) {
		return new RuleBuilder()
				.setOperation("suggest")
				.setField("altTitle")
				.addValue(keyword)
				.setType("")
				.build();
	}
}
