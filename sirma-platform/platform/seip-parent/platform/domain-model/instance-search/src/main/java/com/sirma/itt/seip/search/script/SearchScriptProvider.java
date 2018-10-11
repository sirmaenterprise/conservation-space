package com.sirma.itt.seip.search.script;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.search.SearchService;

/**
 * Search service script API provider
 *
 * @author Valeri Tishev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 6.006)
public class SearchScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchScriptProvider.class);

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private SearchService searchService;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("search", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Search for {@link Instance}s with given {@link SearchArguments}
	 *
	 * @param searchArguments
	 *            {@link SearchArguments} to be matched
	 * @return an array of {@link ScriptInstance}s matching passed {@link SearchArguments}
	 */
	public ScriptInstance[] with(SearchArguments<Instance> searchArguments) {
		Objects.requireNonNull(searchArguments, "Undefined search arguments.");

		searchService.searchAndLoad(Instance.class, searchArguments);

		LOGGER.debug("Found [{}] instances matching following search arguments [{}]",
				searchArguments.getResult().size(), searchArguments.getArguments());

		Collection<ScriptInstance> scriptNodes = typeConverter.convert(ScriptInstance.class,
				searchArguments.getResult());

		return scriptNodes.toArray(new ScriptInstance[scriptNodes.size()]);
	}

	/**
	 * Builds and empty {@link SearchArguments} object with unlimited result max size
	 *
	 * @return empty {@link SearchArguments}
	 */
	public SearchArguments<Instance> buildEmptySearchArguments() {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setMaxSize(0);
		searchArguments.setPermissionsType(QueryResultPermissionFilter.NONE);

		return searchArguments;
	}

	/**
	 * Exposes {@link SearchService#getFilter(String, Class, Context)} method. Can be used to execute predefined queries
	 * by passing the file(definition) where the query is stored and the name of the query.
	 *
	 * @param filterName
	 *            the path to the predefined query for example: customQueries/queryName
	 * @param context
	 *            the context map which will be added to the search arguments
	 * @return builded {@link SearchArguments} for the passed query, if the filterName is null or empty, empty
	 *         {@link SearchArguments} object will be returned.
	 */
	public SearchArguments<SearchInstance> buildArgumentsForPredefinedQuery(String filterName,
			Context<String, Object> context) {
		SearchArguments<SearchInstance> arguments = new SearchArguments<>();
		if (StringUtils.isBlank(filterName)) {
			arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
			return arguments;
		}

		if (context == null) {
			arguments = searchService.getFilter(filterName, SearchInstance.class, new Context<>());
		} else {
			arguments = searchService.getFilter(filterName, SearchInstance.class, context);
		}

		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		return arguments;
	}

}
