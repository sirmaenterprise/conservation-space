package com.sirma.sep.content.idoc.extensions.widgets;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.instance.version.VersionProperties.WidgetsHandlerContextProperties.VERSIONED_INSTANCES_CACHE_KEY;
import static com.sirma.itt.seip.instance.version.VersionProperties.WidgetsHandlerContextProperties.VERSION_DATE_KEY;
import static com.sirma.sep.content.idoc.WidgetSelectionMode.AUTOMATICALLY;
import static com.sirma.sep.content.idoc.WidgetSelectionMode.CURRENT;
import static com.sirma.sep.content.idoc.WidgetSelectionMode.MANUALLY;
import static com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetHandlersUtil.setObjectIdsToConfiguration;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.VersionDao;
import com.sirma.itt.seip.instance.version.VersionIdsCache;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.handler.SearchContentNodeHandler;
import com.sirma.sep.content.idoc.handler.VersionContentNodeHandler;

/**
 * Abstract class with default method for simple widgets version processing.
 * <p>
 * <b>NOTE</b> - this handler implementations requires the results for the {@link SearchContentNodeHandler}s, stored in
 * the widgets configurations via {@link WidgetConfiguration#setSearchResults(Object)}. If this results are not provided
 * the handlers most likely will do nothing.
 * <p>
 * The methods that this class contains are used for default widgets version processing, if no other processing is
 * needed. It will extract the ids of the instances, which data is displayed in the widget. This ids are stored in the
 * widget configurations for every widget via {@link WidgetConfiguration#setSearchResults(Object)} (usually retrieved by
 * executing search with provided criteria, see {@link SearchContentNodeHandler} and its implementations). After that
 * the ids are converted to version ids by using the {@link Date}, when the specific version is created. When the
 * conversing is done the version ids are stored back in the widget configuration in the properties for the selected
 * objects. In addition the selection mode from the widget is set to 'manually'. When specific version is opened, the
 * widgets are loaded with the information stored in the configuration, without performing additional searches for
 * retrieving data. This whole process is done, because we need a way to store the dynamic data in the widget for that
 * specific moment, when the instance version is created. <br />
 * The updated widget configuration is store at the moment in the view content for the version.
 *
 * @param <W> the type of the widget
 * @author A. Kunchev
 */
public abstract class AbstractWidgetVersionHandler<W extends Widget> implements VersionContentNodeHandler<W> {

	public static final String ORIGINAL_CONFIGURATION_DIFF = "originalConfigurationDiff";

	public static final String ADDITIONAL_CHANGES_DONE_BY_HANDLERS = "additionalChanges";

	public static final Gson GSON = new Gson();

	/**
	 * Could be used for converting {@link JsonObject} to {@link Map} by using {@link Gson#fromJson(JsonElement, Type)}.
	 */
	protected static final Type CONFIGURATION_MAP_TYPE = new TypeToken<Map<String, JsonElement>>() {
		// FSS
	}.getType();

	@Inject
	protected InstanceService instanceService;

	@Override
	public HandlerResult handle(W widget, HandlerContext context) {
		WidgetConfiguration configuration = widget.getConfiguration();
		WidgetResults searchResults = configuration.getSearchResults();
		Date versionDate = context.getIfSameType(VERSION_DATE_KEY, Date.class);
		if (!searchResults.areAny() || versionDate == null || CURRENT.equals(configuration.getSelectionMode())) {
			// nothing to do
			return new HandlerResult(widget);
		}

		// copy original configuration before changing it
		Map<String, JsonElement> configCopy = GSON.fromJson(configuration.getConfiguration(), CONFIGURATION_MAP_TYPE);
		HandlerResult result = processResults(widget, searchResults, context);
		storeConfigurationDiff(configCopy, configuration);
		return result;
	}

	/**
	 * Process search results, if all the required arguments are available. By default works with {@link Collection} of
	 * instance ids. First converts them in to version ids by using {@link VersionDao#findVersionIdsByTargetIdAndDate}
	 * and then stores them in the widget configuration as new property with key 'versionData'.
	 *
	 * @param widget the widget in which configuration should be stored results
	 * @param searchResults containing search results. They are retrieved from the {@link SearchContentNodeHandler}
	 *        processing
	 * @param context {@link HandlerContext} containing properties needed for versioning process
	 * @return {@link HandlerResult} with converted ids
	 */
	protected HandlerResult processResults(W widget, WidgetResults searchResults, HandlerContext context) {
		if (!searchResults.areAny()) {
			return null;
		}

		WidgetConfiguration configuration = widget.getConfiguration();
		if (AUTOMATICALLY.equals(configuration.getSelectionMode())) {
			configuration.setSelectionMode(MANUALLY);
		}

		Collection<Serializable> ids = extractResults(searchResults.isFoundBySearch(),
				() -> searchResults.getResultsAsCollection());
		VersionIdsCache versionedCache = context.getIfSameType(VERSIONED_INSTANCES_CACHE_KEY, VersionIdsCache.class);
		Map<Serializable, Serializable> versionIdsMap = versionedCache.getVersioned(ids);
		setObjectIdsToConfiguration(versionIdsMap.values(), configuration);
		return new HandlerResult(widget, GSON.toJsonTree(versionIdsMap));
	}

	/**
	 * Retrieves the results for from the search. If the results are actually found by search operation, the instance
	 * ids are returned directly, but if the results are retrieved directly from the widget configuration(when the
	 * widget shows manually selected results), those results are filtered additionally, before they are versioned. This
	 * is done in order not to pass id of deleted instance for versioning. This filtering is done by checking the ids in
	 * the semantic DB, where the most of the data is updated synchronous.
	 *
	 * @param skipDeletedFiltering whether we need to filter the ids additionally or the deleted are already filtered
	 * @param idsSupplier returns the ids of the instances that are shown in given widget
	 * @return the ids returned by the supplier. If the {@link WidgetResults#isFoundBySearch()} is {@code false} the ids
	 *         are additionally filtered to discard any id of deleted instance
	 */
	protected <S extends Serializable> Collection<S> extractResults(boolean skipDeletedFiltering,
			Supplier<Collection<S>> idsSupplier) {
		Collection<S> ids = idsSupplier.get();
		if (skipDeletedFiltering) {
			return new HashSet<>(ids);
		}

		if (ids.isEmpty()) {
			return emptySet();
		}

		return instanceService.exist(ids).get();
	}

	/**
	 * Finds the differences between the original configuration and the one that is handled by current handler and
	 * stores them in additional property in the configuration. This property is used, when the content for version is
	 * reverted. If there are no changes between the configurations the method will do nothing and no additional
	 * property will be added to the configuration.
	 *
	 * @param originalConfigurationMap {@link Map} containing the original widget configuration before the handler
	 *        changes it
	 * @param configuration current widget configuration, which is already processed by the handler
	 */
	@SuppressWarnings("static-method")
	protected void storeConfigurationDiff(Map<String, JsonElement> originalConfigurationMap,
			WidgetConfiguration configuration) {
		MapDifference<String, JsonElement> difference = Maps.difference(originalConfigurationMap,
				GSON.fromJson(configuration.getConfiguration(), CONFIGURATION_MAP_TYPE));
		if (difference.areEqual()) {
			return;
		}

		difference.entriesInCommon().keySet().stream().forEach(originalConfigurationMap::remove);
		handleAdditionalChangesIfAny(originalConfigurationMap, difference);
		configuration.addNotNullProperty(ORIGINAL_CONFIGURATION_DIFF, GSON.toJsonTree(originalConfigurationMap));
	}

	/**
	 * Adds the changes that are done by the handlers like: adding additional properties, setting some configuration
	 * property explicitly, etc. This changes should be removed when we want to restore the previous state of the widget
	 * and they should be handled in different way than the normal changes, because they are not recognised as changes
	 * by the common logic for configuration diff, as they are added in addition and not changed in the original
	 * configuration. <br>
	 * This changes are added to the diff as array of the id of the properties that are added. This array is mapped to
	 * key {@link #ADDITIONAL_CHANGES_DONE_BY_HANDLERS}. When we want to restore the previous state of the widget we
	 * need just to remove all properties that are stored in this array, if there are any.
	 */
	private static void handleAdditionalChangesIfAny(Map<String, JsonElement> originalConfigurationMap,
			MapDifference<String, JsonElement> difference) {
		Map<String, JsonElement> addByHandlers = difference.entriesOnlyOnRight();
		if (isNotEmpty(addByHandlers)) {
			originalConfigurationMap.put(ADDITIONAL_CHANGES_DONE_BY_HANDLERS, GSON.toJsonTree(addByHandlers.keySet()));
		}
	}
}