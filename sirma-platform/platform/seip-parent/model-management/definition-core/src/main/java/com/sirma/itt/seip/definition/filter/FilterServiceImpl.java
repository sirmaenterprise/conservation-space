package com.sirma.itt.seip.definition.filter;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.definition.model.FilterDefinitionImpl;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.filter.Filter;
import com.sirma.itt.seip.domain.filter.FilterService;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Implements common functions for managing filters.
 *
 * @author BBonev
 */
@ApplicationScoped
public class FilterServiceImpl implements FilterService {

	private static final String FILTER_ID = "filterId";

	@CacheConfiguration(eviction = @Eviction(maxEntries = 100) , doc = @Documentation(""
			+ "Cache used to store the unique filter definitions by identifier. "
			+ "<br>Minimal value expression: filters * 1.2") )
	private static final String FILTER_CACHE = "FILTER_CACHE";

	@Inject
	private DbDao dbDao;

	@Inject
	private EntityLookupCacheContext cacheContext;

	@Inject
	private HashCalculator hashCalculator;

	@PostConstruct
	void init() {
		if (!cacheContext.containsCache(FILTER_CACHE)) {
			cacheContext.createCache(FILTER_CACHE, new FilterLookup(dbDao));
		}
	}

	@Override
	public Filter getFilter(String filterId) {
		return getFilterFromCache(filterId);
	}

	private Filter getFilterFromCache(String filterId) {
		if (filterId == null) {
			return null;
		}

		Pair<String, Filter> pair = getFilterCache().getByKey(filterId);
		if (pair == null) {
			return null;
		}
		return pair.getSecond();
	}

	@Override
	public void filter(Filter filter, Set<String> toFilter) {
		if (filter == null || CollectionUtils.isEmpty(toFilter)) {
			return;
		}
		FilterMode mode = getFilterMode(filter);

		if (mode == FilterMode.INCLUDE) {
			toFilter.retainAll(filter.getFilterValues());
		} else if (mode == FilterMode.EXCLUDE) {
			toFilter.removeAll(filter.getFilterValues());
		}
	}

	@Override
	public void filter(List<Filter> filters, Set<String> toFilter) {
		if (CollectionUtils.isEmpty(filters) || CollectionUtils.isEmpty(toFilter)) {
			return;
		}
		// if single filter, no need to execute the complex logic
		if (filters.size() == 1) {
			filter(filters.get(0), toFilter);
			return;
		}

		// collect all filter data first
		Set<String> include = new LinkedHashSet<>(25);
		Set<String> exclude = new LinkedHashSet<>(25);
		for (Filter filter : filters) {
			FilterMode mode = getFilterMode(filter);
			if (mode == FilterMode.INCLUDE) {
				include.addAll(filter.getFilterValues());
			} else if (mode == FilterMode.EXCLUDE) {
				exclude.addAll(filter.getFilterValues());
			}
		}

		// we remove all values that need to be excluded
		include.removeAll(exclude);

		// the left values in the include are these that need to be returned
		toFilter.retainAll(include);
	}

	private static FilterMode getFilterMode(Filter filter) {
		FilterMode mode = FilterMode.INCLUDE;
		if (StringUtils.isNotBlank(filter.getMode())) {
			mode = FilterMode.valueOf(filter.getMode().toUpperCase());
		}
		return mode;
	}

	@Override
	@Transactional
	public boolean saveFilters(List<Filter> definitions) {
		if (CollectionUtils.isEmpty(definitions)) {
			return true;
		}
		Map<String, Filter> filterIds = definitions.stream().collect(CollectionUtils.toIdentityMap(Filter::getIdentifier));

		List<FilterDefinitionImpl> existingFilters = fetchExistingFilters(filterIds);

		EntityLookupCache<String, Filter, Serializable> filterCache = getFilterCache();
		// update the existing filters that are in the DB
		for (FilterDefinitionImpl existingFilter : existingFilters) {
			Filter definition = filterIds.remove(existingFilter.getIdentifier());
			if (definition == null) {
				// should not happen or something is very wrong!
				continue;
			}

			boolean changed = false;

			if (!EqualsHelper.nullSafeEquals(existingFilter.getMode(), definition.getMode(), true)) {
				existingFilter.setMode(definition.getMode());
				changed = true;
			}

			if (hashCalculator.computeHash(existingFilter.getFilterValues()).compareTo(
					hashCalculator.computeHash(definition.getFilterValues())) != 0) {
				existingFilter.setFilterValues(definition.getFilterValues());
				changed = true;
			}

			if (copyDefinedIn(definition, existingFilter)) {
				changed = true;
			}

			if (changed) {
				dbDao.saveOrUpdate(existingFilter);
				filterCache.setValue(existingFilter.getIdentifier(), existingFilter);
			}
		}

		// persist new filters
		for (Filter filter : filterIds.values()) {
			Filter persistedFilter = dbDao.saveOrUpdate(filter);
			getFilterCache().setValue(persistedFilter.getIdentifier(), persistedFilter);
		}

		return true;
	}

	private List<FilterDefinitionImpl> fetchExistingFilters(Map<String, Filter> filterIds) {
		return dbDao.fetchWithNamed(FilterDefinitionImpl.QUERY_FILTERS_BY_ID_KEY,
				Collections.singletonList(new Pair<>(FILTER_ID, filterIds.keySet())));
	}

	private boolean copyDefinedIn(Filter from, FilterDefinitionImpl to) {
		Set<String> definedIn = from.getDefinedIn();
		if (definedIn == null) {
			definedIn = Collections.emptySet();
		}
		return definedIn.stream().peek(to::addDefinedIn).count() > 0;
	}

	private EntityLookupCache<String, Filter, Serializable> getFilterCache() {
		return cacheContext.getCache(FILTER_CACHE);
	}

	private enum FilterMode {
		INCLUDE, EXCLUDE
	}

	private static class FilterLookup extends EntityLookupCallbackDAOAdaptor<String, Filter, Serializable> {

		private final DbDao dbDao;

		FilterLookup(DbDao dbDao) {
			this.dbDao = dbDao;
		}

		@Override
		public Pair<String, Filter> findByKey(String key) {
			List<FilterDefinitionImpl> list = dbDao.fetchWithNamed(FilterDefinitionImpl.QUERY_FILTER_BY_ID_KEY,
					Collections.singletonList(new Pair<>(FILTER_ID, key)));
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				throw new DatabaseException("More then one record found for filter: " + key);
			}
			FilterDefinitionImpl impl = list.get(0);
			return new Pair<>(key, impl);
		}

		@Override
		public Pair<String, Filter> createValue(Filter value) {
			throw new UnsupportedOperationException("Filters are persisted externaly");
		}
	}

}
