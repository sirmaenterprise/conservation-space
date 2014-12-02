package com.sirma.itt.emf.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.emf.util.SortableComparator;

/**
 * Default filter provider that produces filter configuration from user defined filters and
 * predefined from definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchServiceFilterExtension.TARGET_NAME, order = 5)
public class DefaultSearchFilterProvider implements SearchServiceFilterExtension {

	/** The Constant EMPTY_FILTER. */
	private static final SearchFilterConfig EMPTY_FILTER = new SearchFilterConfig(
			new ArrayList<SearchFilter>(0), new ArrayList<SearchFilter>(0));

	/** The Constant SORTABLE_COMPARATOR. */
	private static final SortableComparator SORTABLE_COMPARATOR = new SortableComparator();
	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(
			Arrays.asList(SearchInstance.class));
	/** The Constant DASHLET_FILTER. */
	private static final String DASHLET_FILTER = "dashletFilter";
	/** The Constant QUERY. */
	private static final String QUERY = "query";

	/** The Constant SORT_FIELDS. */
	private static final String SORT_FIELDS = "sortFields";

	/** The Constant DIALECT. */
	private static final String DIALECT = "dialect";

	/** The Constant PARAM. */
	private static final String PARAM = "queryParam";
	/** The Constant CONFIG. */
	private static final String CONFIG = "config";

	/** The dictionary service instance. */
	@Inject
	private DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(String filterName,
			Context<String, Object> context) {
		List<SearchFilter> filters = getFilterConfiguration(filterName).getFilters();
		if (filters.isEmpty()) {
			// we can return something default
			return null;
		}
		return buildSearchArguments(filters.get(0), context);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter,
			Context<String, Object> context) {
		SearchArguments<?> arguments = new SearchArguments<Instance>();
		String rawQuery = filter.getDefinition().getDefaultValue();
		ControlDefinition controlDefinition = filter.getDefinition().getControlDefinition();

		String buildedQuery = applyQueryParameters(rawQuery, context,
				getParameters(controlDefinition));

		arguments.setStringQuery(buildedQuery);
		setCustomConfiguration(arguments, controlDefinition);
		if (context != null) {
			arguments.setSorter(context.getIfSameType("sorter", Sorter.class));
		}

		return (S) arguments;
	}

	/**
	 * Sets the custom configuration.
	 *
	 * @param arguments
	 *            the arguments
	 * @param controlDefinition
	 *            the control definition
	 */
	private void setCustomConfiguration(SearchArguments<?> arguments,
			ControlDefinition controlDefinition) {
		List<ControlParam> configs = collectConfigurations(controlDefinition);

		// use default values from configuration where possible
		// ensure the dialect is with proper character case
		arguments.setDialect(typeConverter.convert(String.class,
				getConfiguration(configs, DIALECT, SearchDialects.SOLR)).toLowerCase());
		// we does not execute count queries all the time
		arguments.setCountOnly(typeConverter.convert(Boolean.class, getConfiguration(configs, "countOnly", Boolean.FALSE)));
		arguments.setQueryTimeout(typeConverter.convert(Integer.class, getConfiguration(configs, "queryTimeout", 0)));
		arguments.setMaxSize(typeConverter.convert(Integer.class, getConfiguration(configs, "maxSize", 25)));
		arguments.setSparqlQuery(typeConverter.convert(Boolean.class, getConfiguration(configs, "isSparqlQuery", SearchDialects.SPARQL.equals(arguments.getDialect()))));
		arguments.setProjection(typeConverter.convert(String.class,
				getConfiguration(configs, "projection", null)));
		// odd use of external query but u can define a query name
		arguments.setQueryName(typeConverter.convert(String.class,
				getConfiguration(configs, "queryName", null)));

		// if we have more properties set them
		if (!configs.isEmpty()) {
			Map<String, Serializable> otherConfigs = CollectionUtils.createHashMap(configs.size());
			for (ControlParam controlParam : configs) {
				if (StringUtils.isNotNullOrEmpty(controlParam.getValue())) {
					otherConfigs.put(controlParam.getIdentifier(), controlParam.getValue());
				}
			}
		}
	}

	/**
	 * Collect configurations.
	 *
	 * @param controlDefinition
	 *            the control definition
	 * @return the list
	 */
	private List<ControlParam> collectConfigurations(ControlDefinition controlDefinition) {
		if ((controlDefinition == null) || (controlDefinition.getControlParams() == null)) {
			return Collections.emptyList();
		}
		List<ControlParam> params = new ArrayList<ControlParam>(controlDefinition
				.getControlParams().size());
		for (ControlParam controlParam : controlDefinition.getControlParams()) {
			if (CONFIG.equals(controlParam.getIdentifier())) {
				params.add(controlParam);
			}
		}
		return params;
	}

	@Override
	public SearchFilterConfig getFilterConfiguration(String placeHolder) {
		if (StringUtils.isNullOrEmpty(placeHolder)) {
			return EMPTY_FILTER;
		}
		// search a definition by it's root path - this way we can create filter identified by path
		// in a definition
		// first we fetch the definition then we will search for the filter in that definition
		GenericDefinition definition = dictionaryService.getDefinition(GenericDefinition.class,
				PathHelper.extractRootPath(placeHolder));

		if (definition == null) {
			return EMPTY_FILTER;
		}

		return loadFiltersForDashlet(definition, placeHolder);
	}

	/**
	 * Gets the parameters.
	 *
	 * @param controlDefinition
	 *            the control definition
	 * @return the parameters
	 */
	private List<ControlParam> getParameters(ControlDefinition controlDefinition) {
		if ((controlDefinition == null) || (controlDefinition.getControlParams() == null)) {
			return Collections.emptyList();
		}
		List<ControlParam> params = new ArrayList<ControlParam>(controlDefinition
				.getControlParams().size());
		for (ControlParam controlParam : controlDefinition.getControlParams()) {
			if (PARAM.equals(controlParam.getIdentifier())) {
				params.add(controlParam);
			}
		}
		return params;
	}

	/**
	 * Apply query parameters.
	 *
	 * @param query
	 *            the query
	 * @param context
	 *            the context
	 * @param list
	 *            the list
	 * @return the string
	 */
	private String applyQueryParameters(String query, Context<String, Object> context,
			List<ControlParam> list) {
		if ((context == null) || context.isEmpty() || list.isEmpty()) {
			return query;
		}
		String localQuery = query;

		for (ControlParam controlParam : list) {
			String value = controlParam.getValue();
			Object object = context.get(controlParam.getName());
			if (object != null) {
				value = typeConverter.convert(String.class, object);
			}
			// if null try to remove it from the query
			if (value == null) {
				value = "";
			}
			localQuery = localQuery.replaceAll("\\{" + controlParam.getName() + "\\}", value);
		}

		return localQuery;
	}

	/**
	 * Load filters for dashlet.
	 *
	 * @param definition
	 *            the definition
	 * @param path
	 *            the filter path to return
	 * @return the list
	 */
	private SearchFilterConfig loadFiltersForDashlet(DefinitionModel definition, String path) {
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
		Collections.sort(fields, SORTABLE_COMPARATOR);

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

	/**
	 * Builds the filter from definition.
	 *
	 * @param searchFilters
	 *            the search filters
	 * @param config
	 *            the config
	 * @param propertyDefinition
	 *            the property definition
	 */
	private void buildFilterFromDefinition(List<SearchFilter> searchFilters,
			SearchFilterConfig config, PropertyDefinition propertyDefinition) {
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		if (controlDefinition != null) {
			if (DASHLET_FILTER.equals(controlDefinition.getIdentifier())
					|| QUERY.equals(controlDefinition.getIdentifier())) {
				SearchFilter filter = createFilter(propertyDefinition);
				// we have predefined filters
				// if (!controlDefinition.getFields().isEmpty()) {
				// config.setSecond(loadSortersForDashlet(controlDefinition));
				// }
				searchFilters.add(filter);
			} else if (SORT_FIELDS.equals(controlDefinition.getIdentifier())) {
				config.setSecond(loadSortersForDashlet(controlDefinition));
			}
		}
	}

	/**
	 * Gets the dialect configuration.
	 *
	 * @param params
	 *            the control definition
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the dialect
	 */
	private Serializable getConfiguration(List<ControlParam> params, String key, Serializable defaultValue) {
		if ((params == null) || params.isEmpty()) {
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

	/**
	 * Load sorters for dashlet.
	 *
	 * @param definition
	 *            the definition
	 * @return the list
	 */
	private List<SearchFilter> loadSortersForDashlet(DefinitionModel definition) {
		List<PropertyDefinition> fields = definition.getFields();
		Collections.sort(fields, SORTABLE_COMPARATOR);

		List<SearchFilter> sorters = new ArrayList<>(fields.size());

		// collect information about the sort fields
		for (PropertyDefinition sorterDefinition : fields) {
			sorters.add(createFilter(sorterDefinition));
		}

		// set default sorter flag to first sorter
		if (!sorters.isEmpty()) {
			sorters.get(0).setDefault(true);
		}
		return sorters;
	}

	/**
	 * Creates the filter.
	 *
	 * @param definition
	 *            the definition
	 * @return the search filter
	 */
	private SearchFilter createFilter(PropertyDefinition definition) {
		return new SearchFilter(definition.getName(), definition.getLabel(),
				definition.getTooltip(), definition);
	}

}
