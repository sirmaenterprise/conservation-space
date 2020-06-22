package com.sirma.itt.seip.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default filter provider that produces filter configuration from user defined filters and predefined from definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchServiceFilterExtension.TARGET_NAME, order = 5)
public class DefaultSearchFilterProvider implements SearchServiceFilterExtension {

	private static final SearchFilterConfig EMPTY_FILTER = new SearchFilterConfig(new ArrayList<SearchFilter>(0),
			new ArrayList<SearchFilter>(0));

	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(Arrays.asList(SearchInstance.class));
	private static final String DASHLET_FILTER = "dashletFilter";
	private static final String QUERY = "query";
	private static final String SORT_FIELDS = "sortFields";
	private static final String DIALECT = "dialect";
	private static final String PARAM = "queryParam";
	private static final String CONFIG = "config";
	private static final String BINDING = "binding";
	private static final String SYSTEM_TIME_BINDING = "systemTime";
	private static final String LIMIT_PARAMETER = "limit";

	@Inject
	private DefinitionService definitionService;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private SearchConfiguration searchConfiguration;

	@Inject
	private javax.enterprise.inject.Instance<SearchService> searchService;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(String filterName, Context<String, Object> context) {
		List<SearchFilter> filters = getFilterConfiguration(filterName).getFilters();
		if (filters.isEmpty()) {
			// we can return something default
			return null;
		}
		Context<String, Object> localContext = context == null ? Context.emptyContext() : context;
		S arguments = buildSearchArguments(filters.get(0), localContext);
		arguments.setQueryName(filterName);
		return arguments;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Context<String, Object> context) {
		SearchArguments<?> arguments = new SearchArguments<Instance>();
		String rawQuery = filter.getDefinition().getDefaultValue();
		ControlDefinition controlDefinition = filter.getDefinition().getControlDefinition();

		Context<String, Object> localContext = context == null ? Context.emptyContext() : context;
		setCustomConfiguration(arguments, controlDefinition, context);
		Function<String, String> escape = searchService.get().escapeForDialect(arguments.getDialect());
		String buildedQuery = applyQueryParameters(rawQuery, localContext, getParameters(controlDefinition), escape);

		arguments.setStringQuery(buildedQuery);
		arguments.addSorter(localContext.getIfSameType("sorter", Sorter.class));
		arguments.setQueryName(filter.getValue());
		return (S) arguments;
	}

	@SuppressWarnings("boxing")
	private void setCustomConfiguration(SearchArguments<?> arguments, ControlDefinition controlDefinition,
			Context<String, Object> context) {
		List<ControlParam> configs = collectConfigurations(controlDefinition);

		// use default values from configuration where possible
		// ensure the dialect is with proper character case
		arguments.setDialect(typeConverter
				.convert(String.class, getConfiguration(configs, DIALECT, SearchDialects.SOLR))
					.toLowerCase());
		// we does not execute count queries all the time
		arguments.setCountOnly(typeConverter.booleanValue(getConfiguration(configs, "countOnly", Boolean.FALSE)));
		arguments.setQueryTimeout(TimeUnit.SECONDS,
				typeConverter.intValue(getConfiguration(configs, "queryTimeout", 0)));
		arguments.setPageSize(typeConverter
				.intValue(getConfiguration(configs, "maxSize", searchConfiguration.getSearchResultMaxSize())));
		arguments.setProjection(typeConverter.convert(String.class, getConfiguration(configs, "projection", null)));
		// odd use of external query but u can define a query name
		arguments.setQueryName(typeConverter.convert(String.class, getConfiguration(configs, "queryName", null)));

		// if we have more properties set them
		if (!configs.isEmpty()) {
			setBindings(arguments, context, configs);
		}
	}

	/**
	 * Sets the bindings that are defined in the definition if any.
	 */
	private static void setBindings(SearchArguments<?> arguments, Context<String, Object> context,
			List<ControlParam> configs) {
		for (ControlParam controlParam : configs) {
			// if we have a defined binding that need to copy to the query set the value from
			// context or
			String paramName = controlParam.getName();
			if (BINDING.equals(controlParam.getIdentifier())) {
				// if the context has defined property set copy it to the arguments
				if (context.containsKey(paramName)) {
					arguments.getArguments().put(paramName, (Serializable) context.get(paramName));
				} else if (StringUtils.isNotBlank(controlParam.getValue())) {
					// otherwise if there is not such value use the default value if any
					arguments.getArguments().put(paramName, controlParam.getValue());
				} else if (SYSTEM_TIME_BINDING.equals(paramName)) {
					arguments.getArguments().put(SYSTEM_TIME_BINDING, new Date());
				}
			} else if (CONFIG.equals(controlParam.getIdentifier())) {
				// in case of custom configurations for the query
				arguments.getQueryConfigurations().put(paramName, controlParam.getValue());
			} else if (StringUtils.isNotBlank(controlParam.getValue())) {
				arguments.getArguments().put(paramName, controlParam.getValue());
			}
		}
	}

	private static List<ControlParam> collectConfigurations(ControlDefinition controlDefinition) {
		if (controlDefinition == null || controlDefinition.getControlParams() == null) {
			return Collections.emptyList();
		}
		List<ControlParam> params = new ArrayList<>(controlDefinition.getControlParams().size());
		for (ControlParam controlParam : controlDefinition.getControlParams()) {
			// collect configurations and binding that must be passed to engine
			if (CONFIG.equals(controlParam.getIdentifier()) || BINDING.equals(controlParam.getIdentifier())) {
				params.add(controlParam);
			}
		}
		return params;
	}

	@Override
	public SearchFilterConfig getFilterConfiguration(String placeHolder) {
		if (StringUtils.isBlank(placeHolder)) {
			return EMPTY_FILTER;
		}
		// search a definition by it's root path - this way we can create filter identified by path
		// in a definition
		// first we fetch the definition then we will search for the filter in that definition
		DefinitionModel definition = definitionService.find(PathHelper.extractRootPath(placeHolder));

		if (definition == null) {
			return EMPTY_FILTER;
		}

		return loadFiltersForDashlet(definition, placeHolder);
	}

	private static List<ControlParam> getParameters(ControlDefinition controlDefinition) {
		if (controlDefinition == null || controlDefinition.getControlParams() == null) {
			return Collections.emptyList();
		}
		List<ControlParam> params = new ArrayList<>(controlDefinition.getControlParams().size());
		for (ControlParam controlParam : controlDefinition.getControlParams()) {
			if (PARAM.equals(controlParam.getIdentifier())) {
				params.add(controlParam);
			}
		}
		return params;
	}

	private String applyQueryParameters(String query, Context<String, Object> context, List<ControlParam> list,
			Function<String, String> escape) {
		if (context.isEmpty() && list.isEmpty()) {
			return query;
		}
		String localQuery = query;

		TypeConverter converter = typeConverter;
		Function<Object, String> convert = value -> converter.convert(String.class, value);
		convert = convert.andThen(escape);

		for (ControlParam controlParam : list) {
			String defaultValue = controlParam.getValue();
			// get from context of use the default parameter or else empty string
			String value = context.map(controlParam.getName(), convert,
					() -> org.apache.commons.lang.StringUtils.trimToEmpty(defaultValue));
			// TODO: add parameter to pattern mapping cache to optimize parameter value population
			localQuery = localQuery.replaceAll("\\{" + controlParam.getName() + "\\}", value);
		}

		if (localQuery.contains(LIMIT_PARAMETER)) {
			localQuery = localQuery.replaceAll("\\{" + LIMIT_PARAMETER + "\\}",
					searchConfiguration.getSearchResultMaxSize().toString());
		}

		return localQuery;
	}

	// maybe, just maybe this should be removed/moved in specific class
	private static SearchFilterConfig loadFiltersForDashlet(DefinitionModel definition, String path) {
		// check if we need only a particular filter
		Identity identity = PathHelper.iterateByPath(definition, path);
		if (identity instanceof PropertyDefinition) {
			// we need only a single filter not all so no need to build all of them
			List<SearchFilter> searchFilters = new ArrayList<>(1);
			SearchFilterConfig config = new SearchFilterConfig(searchFilters, null);
			buildFilterFromDefinition(searchFilters, config, (PropertyDefinition) identity);
			return config;
		}

		List<PropertyDefinition> fields = definition.getFields();

		List<SearchFilter> searchFilters = new ArrayList<>(fields.size());
		SearchFilterConfig config = new SearchFilterConfig(searchFilters, null);

		for (PropertyDefinition propertyDefinition : fields) {
			buildFilterFromDefinition(searchFilters, config, propertyDefinition);
		}
		// set default filter flag to first filter
		if (!searchFilters.isEmpty()) {
			searchFilters.get(0).setDefault(true);
		}

		return config;
	}

	private static void buildFilterFromDefinition(List<SearchFilter> searchFilters, SearchFilterConfig config,
			PropertyDefinition propertyDefinition) {
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		if (controlDefinition != null) {
			if (DASHLET_FILTER.equals(controlDefinition.getIdentifier())
					|| QUERY.equals(controlDefinition.getIdentifier())) {
				searchFilters.add(createFilter(propertyDefinition));
			} else if (SORT_FIELDS.equals(controlDefinition.getIdentifier())) {
				config.setSecond(loadSortersForDashlet(controlDefinition));
			}
		}
	}

	private static Serializable getConfiguration(List<ControlParam> params, String key, Serializable defaultValue) {
		if (CollectionUtils.isEmpty(params)) {
			return defaultValue;
		}
		for (Iterator<ControlParam> it = params.iterator(); it.hasNext();) {
			ControlParam controlParam = it.next();
			if (key.equals(controlParam.getName())) {
				// remove the found element from the list
				it.remove();
				return controlParam.getValue();
			}
		}
		return defaultValue;
	}

	private static List<SearchFilter> loadSortersForDashlet(DefinitionModel definition) {
		List<PropertyDefinition> fields = definition.getFields();

		List<SearchFilter> sorters = new ArrayList<>(fields.size());

		// collect information about the sort fields
		for (PropertyDefinition sorterDefinition : fields) {
			if (sorterDefinition.getDisplayType() == DisplayType.SYSTEM) {
				continue;
			}
			sorters.add(createFilter(sorterDefinition));
		}

		// set default sorter flag to first sorter
		if (!sorters.isEmpty()) {
			sorters.get(0).setDefault(true);
		}
		return sorters;
	}

	private static SearchFilter createFilter(PropertyDefinition definition) {
		return new SearchFilter(definition.getName(), definition.getLabel(), definition.getTooltip(), definition);
	}
}
