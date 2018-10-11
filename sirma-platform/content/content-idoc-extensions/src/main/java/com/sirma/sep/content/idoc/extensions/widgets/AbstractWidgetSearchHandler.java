package com.sirma.sep.content.idoc.extensions.widgets;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.WidgetSelectionMode;
import com.sirma.sep.content.idoc.handler.SearchContentNodeHandler;

/**
 * Abstract class with default method for simple widgets search. It contain init method that initialize
 * {@link SearchService} and {@link JsonToConditionConverter} on deployment phase of the application start.
 * <p>
 * The methods that this class contains are used for default widgets search, if no other processing is needed. The
 * search is done by, extracting the widget configuration, getting from it the search criteria from which should be
 * build search request and then the search is executed. The results will be returned as collection of instance id that
 * are found.
 * <p>
 * By default uses {@link JsonToConditionConverter} to convert the search criteria to {@link Condition} with which is
 * build search request. Also by default the search will be done in the semantic DB.
 * <p>
 * The handler processes the case where the instances are selected/passed as manually selected objects and not by search
 * criteria. This is handled by retrieving the values of the properties that are used for this purpose.
 * <p>
 * The results from the search are also stored in the widget configurations via
 * {@link WidgetConfiguration#setSearchResults(Object)}. This is done so that they could be easily accessed from other
 * handlers, if they need additional processing (like versioning or something else).
 * <p>
 * Note that when the search for the widget is executed, all permissions checks will be skipped, because we need to get
 * all of the instances.
 *
 * @param <W> the type of the widget
 * @author A. Kunchev
 */
public abstract class AbstractWidgetSearchHandler<W extends Widget> implements SearchContentNodeHandler<W> {

	@Inject
	protected SearchService searchService;

	@Inject
	protected JsonToConditionConverter jsonToConditionConverter;

	@Inject
	protected JsonToDateRangeConverter jsonToDateRangeConverter;

	@Override
	public HandlerResult handle(W node, HandlerContext context) {
		WidgetSelectionMode selectionMode = node.getConfiguration().getSelectionMode();
		if (WidgetSelectionMode.CURRENT.equals(selectionMode)) {
			return new HandlerResult(node);
		} else if (WidgetSelectionMode.MANUALLY.equals(selectionMode)) {
			return collectSelectedObjectsAndStoreThemAsResults(node);
		} else {
			return executeSearchAndStoreResults(node, context);
		}
	}

	/**
	 * Collects all of the selected objects stored in the widget configuration and sets them as search results. Mainly
	 * used when selection mode is 'manually'.
	 *
	 * @param node the node from which configuration will be extracted selected objects
	 * @return {@link HandlerResult} object with the current node
	 */
	protected HandlerResult collectSelectedObjectsAndStoreThemAsResults(W node) {
		WidgetConfiguration configuration = node.getConfiguration();
		Set<Serializable> selectedObjects = configuration.getAllSelectedObjects();
		configuration.setSearchResults(WidgetResults.fromConfiguration(selectedObjects));
		return new HandlerResult(node, selectedObjects);
	}

	private HandlerResult executeSearchAndStoreResults(W node, HandlerContext context) {
		WidgetConfiguration configuration = node.getConfiguration();
		Object instanceIds = configuration
				.getSearchCriteria()
					.map(jsonToConditionConverter::parseCondition)
					.map(condition -> buildSearchRequest(condition, node))
					.map(request -> search(request, context.getCurrentInstanceId()))
					.map(this::handleResults)
					// only if current object is selected we don't have search criteria. In that case its fine to return
					// empty result, because we don't need to process the widget further (for now)
					.orElse(null);
		configuration.setSearchResults(WidgetResults.fromSearch(instanceIds));
		return new HandlerResult(node, instanceIds);
	}

	/**
	 * Builds {@link SearchRequest} used for the search execution.
	 * <p>
	 * If you override this method make sure that you're building valid search request. See
	 * {@link SearchService#parseRequest(SearchRequest)}
	 *
	 * @param condition the parsed condition from the widget search criteria
	 * @param widget the widget for which is executed search
	 * @return {@link SearchRequest} with which the search will be executed
	 */
	protected SearchRequest buildSearchRequest(Condition condition, W widget) {
		ConditionBuilder builder = SearchCriteriaBuilder.createConditionBuilder().from(condition);
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(builder, condition);
		SearchRequest request = new SearchRequest(new HashMap<>(0));
		request.setSearchTree(builder.build());
		return request;
	}

	/**
	 * Parses given search request and perform search, if the {@link SearchArguments} is build correctly. The result
	 * from the search could be retrieved by {@link SearchArguments#getResult()}.
	 *
	 * @param request {@link SearchRequest} object that is used to build search arguments
	 * @return {@link SearchArguments} object build for the passed search request. It contains the results from the
	 *         search after execution
	 * @param currentInstanceId the id of the current instance, used to replace the string 'current_object' in the
	 *        search query
	 * @exception IllegalArgumentException when the search request is not valid
	 */
	protected SearchArguments<Instance> search(SearchRequest request, String currentInstanceId) {
		SearchArguments<Instance> arguments = searchService.parseRequest(request);
		if (arguments == null) {
			throw new IllegalArgumentException("Valid search request is requeried!");
		}

		// TODO (need better solution for this, but its fine for now) This happen when we search in the context of
		// the current object, in the widget criteria is set 'current_object' instead of actual instance id
		String newQuery = arguments.getStringQuery().replaceAll("current_object", currentInstanceId);
		arguments.setStringQuery(newQuery);
		arguments.setMaxSize(-1);
		// permission will be applied when the versions are displayed, however if we use this handler for other
		// functionalities we need to pass this as some kind of configuration
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		searchService.search(Instance.class, arguments);
		return arguments;
	}

	/**
	 * Converts the results from the {@link SearchArguments} in to format suitable for the widgets.
	 * <p />
	 * <b>NOTE:</b> If the search does not return any results the widget handlers will expect a null value. Searches in
	 * SES can either return empty list or a null value. We need to make sure here if no results are returned as empty
	 * list then we return null.
	 *
	 * @param arguments the search arguments that contain the results for the executed search
	 * @return result object suitable for widgets
	 */
	@SuppressWarnings("static-method")
	protected Object handleResults(SearchArguments<Instance> arguments) {
		if (CollectionUtils.isEmpty(arguments.getResult())) {
			return null;
		}
		return arguments.getResult().stream().map(Instance::getId).collect(Collectors.toSet());
	}

	/**
	 * Retrieves the main search results as {@link List}. This results are the ids of the found instances from the main
	 * search.
	 */
	protected static List<String> getResults(HandlerResult searchResult) {
		Optional<Collection<String>> result = searchResult.getResult();
		if (result.isPresent()) {
			return new ArrayList<>(result.get());
		}
		return emptyList();
	}
}