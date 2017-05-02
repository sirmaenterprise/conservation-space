package com.sirma.itt.seip.definition.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sirma.itt.commons.utils.string.StringUtils;
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
 * The Class DefaultLabelProvider.
 *
 * @author BBonev
 */
@ApplicationScoped
public class FilterServiceImpl implements FilterService, Serializable {

	private static final String FILTER_ID = "filterId";

	@CacheConfiguration(eviction = @Eviction(maxEntries = 100) , doc = @Documentation(""
			+ "Cache used to store the unique filter definitions by identifier. "
			+ "<br>Minimal value expression: filters * 1.2") )
	private static final String FILTER_CACHE = "FILTER_CACHE";

	private static final long serialVersionUID = -8997884886800754157L;

	@Inject
	private DbDao dbDao;

	@Inject
	private EntityLookupCacheContext cacheContext;

	@Inject
	private HashCalculator hashCalculator;

	/**
	 * Initializes the cache.
	 */
	@PostConstruct
	public void init() {
		if (!cacheContext.containsCache(FILTER_CACHE)) {
			cacheContext.createCache(FILTER_CACHE, new FilterLookup());
		}
	}

	@Override
	public Filter getFilter(String filterId) {
		return getFilterFromCache(filterId);
	}

	/**
	 * Gets the label from cache.
	 *
	 * @param filterId
	 *            the label id
	 * @return the label from cache
	 */
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
		if (filter == null || toFilter == null || toFilter.isEmpty()) {
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
		if (filters == null || filters.isEmpty() || toFilter == null || toFilter.isEmpty()) {
			return;
		}
		// if single filter, no need to execute the complex logic
		if (filters.size() == 1) {
			filter(filters.get(0), toFilter);
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

		// the left values in the inlcude are these that need to be returned
		toFilter.retainAll(include);
	}

	/**
	 * Gets the filter mode.
	 *
	 * @param filter
	 *            the filter
	 * @return the filter mode
	 */
	private static FilterMode getFilterMode(Filter filter) {
		FilterMode mode = FilterMode.INCLUDE;
		if (StringUtils.isNotNullOrEmpty(filter.getMode())) {
			mode = FilterMode.valueOf(filter.getMode().toUpperCase());
		}
		return mode;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean saveFilter(Filter filter) {
		if (filter == null) {
			return false;
		}
		Pair<String, Filter> pair = getFilterCache().getByKey(filter.getIdentifier());
		if (pair != null) {
			FilterDefinitionImpl second = (FilterDefinitionImpl) pair.getSecond();
			boolean updated = false;
			if (!EqualsHelper.nullSafeEquals(second.getMode(), filter.getMode(), true)) {
				second.setMode(filter.getMode());
				updated = true;
			}
			if (!hashCalculator.computeHash(second.getFilterValues()).equals(
					hashCalculator.computeHash(filter.getFilterValues()))) {
				second.setFilterValues(filter.getFilterValues());
				updated = true;
			}
			if (updated) {
				dbDao.saveOrUpdate(second);
			}
			return true;
		}

		Filter impl = dbDao.saveOrUpdate(filter);
		getFilterCache().setValue(impl.getIdentifier(), impl);
		return true;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean saveFilters(List<Filter> definitions) {
		if (definitions == null || definitions.isEmpty()) {
			return true;
		}
		Map<String, Filter> filterIds = new LinkedHashMap<>((int) (definitions.size() * 1.1), 0.95f);
		for (Filter filter : definitions) {
			filterIds.put(filter.getIdentifier(), filter);
		}
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>(FILTER_ID, filterIds.keySet()));
		List<FilterDefinitionImpl> list = dbDao.fetchWithNamed(FilterDefinitionImpl.QUERY_FILTERS_BY_ID_KEY, args);
		if (list == null) {
			list = CollectionUtils.EMPTY_LIST;
		}

		EntityLookupCache<String, Filter, Serializable> filterCache = getFilterCache();
		// update the labels that are int the DB
		for (FilterDefinitionImpl filterImpl : list) {
			Filter definition = filterIds.remove(filterImpl.getIdentifier());
			if (definition == null) {
				// should not happen or something is very wrong!
				continue;
			}
			boolean updated = false;
			if (!EqualsHelper.nullSafeEquals(filterImpl.getMode(), definition.getMode(), true)) {
				filterImpl.setMode(definition.getMode());
				updated = true;
			}
			if (hashCalculator.computeHash(filterImpl.getFilterValues()).compareTo(
					hashCalculator.computeHash(definition.getFilterValues())) == 0) {
				filterImpl.setFilterValues(definition.getFilterValues());
				updated = true;
			}
			if (updated) {
				dbDao.saveOrUpdate(filterImpl);
				filterCache.setValue(filterImpl.getIdentifier(), filterImpl);
			}
		}

		// persist new filters
		for (Filter filter : filterIds.values()) {
			Filter impl = dbDao.saveOrUpdate(filter);
			getFilterCache().setValue(impl.getIdentifier(), impl);
		}
		return true;
	}

	/**
	 * Getter method for labelCache.
	 *
	 * @return the labelCache
	 */
	private EntityLookupCache<String, Filter, Serializable> getFilterCache() {
		return cacheContext.getCache(FILTER_CACHE);
	}

	/**
	 * The Enum FilterMode.
	 */
	enum FilterMode {
		INCLUDE, EXCLUDE;
	}

	/**
	 * The Class FilterLookup.
	 *
	 * @author BBonev
	 */
	class FilterLookup extends EntityLookupCallbackDAOAdaptor<String, Filter, Serializable> {

		@Override
		public Pair<String, Filter> findByKey(String key) {
			List<FilterDefinitionImpl> list = dbDao.fetchWithNamed(FilterDefinitionImpl.QUERY_FILTER_BY_ID_KEY,
					Arrays.asList(new Pair<String, Object>(FILTER_ID, key)));
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
