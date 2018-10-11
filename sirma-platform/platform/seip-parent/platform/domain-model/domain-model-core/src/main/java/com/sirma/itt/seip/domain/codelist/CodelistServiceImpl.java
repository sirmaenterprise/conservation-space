/*
 *
 */
package com.sirma.itt.seip.domain.codelist;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.domain.codelist.adapter.CodelistAdapter;
import com.sirma.itt.seip.domain.codelist.event.CodelistFilterBinding;
import com.sirma.itt.seip.domain.codelist.event.CodelistFiltered;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.filter.Filter;
import com.sirma.itt.seip.domain.filter.FilterService;
import com.sirma.itt.seip.domain.instance.PropertyModelComparator;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.annotation.SecureObserver;

/**
 * Default codelist service implementation. The service uses a cache entry with name: <code>CODELIST_CACHE</code>
 *
 * @author BBonev
 */
@Named("cls")
@ApplicationScoped
public class CodelistServiceImpl implements CodelistService {

	@CacheConfiguration(eviction = @Eviction(maxEntries = 50), expiration = @Expiration(maxIdle = 900000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the values for the different codelists in the system."
			+ "<br>Minimal value expression: uniqueCodelists * 1.2"))
	protected static final String CODELIST_CACHE = "CODELIST_CACHE";

	private static final Logger LOGGER = LoggerFactory.getLogger(CodelistServiceImpl.class);

	private static final Pattern CODEVALUE_PROPERTY_SPLIT_PATTERN = Pattern.compile("[,\\s]+");

	@Inject
	private EntityLookupCacheContext cacheContext;
	/**
	 * The codelist adapter.
	 */
	@Inject
	private Instance<CodelistAdapter> codelistAdapter;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private EventService eventService;

	/**
	 * The filter service.
	 */
	@Inject
	private FilterService filterService;

	@Inject
	private Contextual<CodelistAdapter> adapterInstance;

	@Inject
	private SystemConfiguration systemConfigs;

	/**
	 * Inits the service cache and internal context
	 */
	@PostConstruct
	void init() {
		if (!cacheContext.containsCache(CODELIST_CACHE)) {
			cacheContext.createCache(CODELIST_CACHE, new CodelistCacheLookup());
		}
		adapterInstance.initializeWith(this::instantiateAdapter);
	}

	@Override
	public Map<String, CodeValue> getCodeValues(Integer codelist) {
		return getCodeValuesInternal(codelist);
	}

	private Map<String, CodeValue> getCodeValuesInternal(Integer codelist) {
		return getCodeValues(codelist, false);
	}

	@Override
	public CodeValue getCodeValue(Integer codelist, String value) {
		Map<String, CodeValue> map = getCodeValuesInternal(codelist);
		if (map.isEmpty()) {
			return null;
		}
		return map.get(value);
	}

	@Override
	public String getDescription(Integer codelist, String value) {
		return getDescription(codelist, value, getCurrentLanguage());
	}

	/**
	 * Gets the current language.
	 *
	 * @return the current language
	 */
	private String getCurrentLanguage() {
		return userPreferences.getLanguage();
	}

	@Override
	public String getDescription(Integer codelist, String value, String language) {
		if (codelist == null) {
			return null;
		}
		Pair<Integer, Map<String, CodeValue>> pair = getCache().getByKey(codelist);
		if (pair == null) {
			return null;
		}
		CodeValue codeValue = pair.getSecond().get(value);
		String result = null;
		if (codeValue != null) {
			Map<String, Serializable> properties = codeValue.getProperties();
			if (properties != null) {
				Serializable serializable = properties.get(language);

				result = (String) serializable;
			}
		}
		return result;
	}

	@Override
	public Map<String, CodeValue> getFilteredCodeValues(Integer codelist, String... filterId) {
		return getFilteredCodeValues(codelist, false, filterId);
	}

	@Override
	public Map<String, CodeValue> getFilteredCodeValues(Integer codelist, boolean sorted, String... filterId) {

		Map<String, CodeValue> values = copy(getCodeValues(codelist));

		if (filterId == null || filterId.length == 0) {
			return values;
		}

		List<Filter> filters = new ArrayList<>(filterId.length);
		for (String aFilterId : filterId) {
			Filter filter = filterService.getFilter(aFilterId);
			if (filter == null) {
				values = filterExternal(codelist, filterId[0], values);
			} else {
				filters.add(filter);
			}
		}

		filterService.filter(filters, values.keySet());

		if (sorted) {
			values = sort(values);
		}

		return values;
	}

	@Override
	public String getDescription(CodeValue codeValue) {
		if (codeValue == null) {
			return null;
		}

		Map<String, Serializable> properties = codeValue.getProperties();
		if (properties == null) {
			return null;
		}

		Serializable serializable = properties.get(getCurrentLanguage());
		return (String) serializable;
	}

	@Override
	public Map<String, CodeValue> getCodeValues(Integer codelist, boolean sorted) {
		Pair<Integer, Map<String, CodeValue>> pair = getCache().getByKey(codelist);
		if (pair == null) {
			return CollectionUtils.emptyMap();
		}
		Map<String, CodeValue> map = pair.getSecond();
		// if we need to sort the result then..
		if (sorted) {
			map = sort(map);
		} else {
			map = copy(map);
		}
		return map;
	}

	/**
	 * Sort the given map without modifying it and returns the result in new map instance, sorted by localized
	 * description.
	 *
	 * @param toSort
	 *            the data to sort
	 * @return the sorted map
	 */
	protected Map<String, CodeValue> sort(Map<String, CodeValue> toSort) {
		List<CodeValue> list = new ArrayList<>(toSort.values());
		// sort the values by the user locale
		String locale = getCurrentLanguage();
		Collections.sort(list, new PropertyModelComparator(true, locale));
		// copy sorted list to the map
		Map<String, CodeValue> map = CollectionUtils.createLinkedHashMap(list.size());
		for (CodeValue codeValue : list) {
			map.put(codeValue.getValue(), codeValue);
		}
		return map;
	}

	/**
	 * Calls an external filter via event.
	 *
	 * @param codelist
	 *            the codelist
	 * @param filterId
	 *            the filter id
	 * @param values
	 *            the values
	 * @return the map
	 */
	protected Map<String, CodeValue> filterExternal(Integer codelist, String filterId, Map<String, CodeValue> values) {
		CodelistFiltered filtered = new CodelistFiltered(values);
		eventService.fire(filtered, new CodelistFilterBinding(codelist.intValue(), filterId));

		if (filtered.getValues() == values) {
			return values;
		} else if (filtered.getValues() == null) {
			return CollectionUtils.emptyMap();
		} else {
			return filtered.getValues();
		}
	}

	protected EntityLookupCache<Integer, Map<String, CodeValue>, Serializable> getCache() {
		return cacheContext.getCache(CODELIST_CACHE);
	}

	/**
	 * Copy the values from the source map to a new instance to protect the internal cache from modification
	 *
	 * @param values
	 *            the values
	 * @return the map
	 */
	private static Map<String, CodeValue> copy(Map<String, CodeValue> values) {
		if (values == null || values.isEmpty()) {
			return CollectionUtils.emptyMap();
		}
		Map<String, CodeValue> map = CollectionUtils.createLinkedHashMap(values.size());
		for (Entry<String, CodeValue> entry : values.entrySet()) {
			map.put(entry.getKey(), entry.getValue().createCopy());
		}
		return map;
	}

	/**
	 * On codelist reset.
	 *
	 * @param event
	 *            the event
	 */
	@SecureObserver
	public void onCodelistReset(@Observes ResetCodelistEvent event) {
		CodelistAdapter adapter = getAdapterInstance();
		if (adapter == null) {
			return;
		}
		adapter.resetCodelist();
		getCache().clear();
	}

	/**
	 * Retrieves all CodeLists from database.
	 *
	 * @return all found codelists
	 */
	@Override
	public Map<BigInteger, String> getAllCodelists() {
		CodelistAdapter adapter = getAdapterInstance();
		if (adapter == null) {
			return null;
		}

		Map<BigInteger, String> list = adapter.getAllCodelists(systemConfigs.getSystemLanguage());
		if (list == null || list.isEmpty()) {
			return null;
		}

		return list;
	}

	CodelistAdapter getAdapterInstance() {
		return adapterInstance.getContextValue();
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private CodelistAdapter instantiateAdapter() {
		if (codelistAdapter.isUnsatisfied()) {
			LOGGER.warn("No codelist adapter found!");
			return null;
		}
		for (CodelistAdapter adapter : codelistAdapter) {
			if (adapter.isConfigured()) {
				adapter.addMutationObserver(this::resetAdapterInstance);
				return adapter;
			}
		}
		LOGGER.warn("No configured codelist adapter found!");
		return null;
	}

	/**
	 * Reset adapter instance.
	 */
	void resetAdapterInstance() {
		adapterInstance.clearContextValue();
	}

	/**
	 * The Class CodelistCacheLookup.
	 *
	 * @author BBonev
	 */
	public class CodelistCacheLookup
			extends EntityLookupCallbackDAOAdaptor<Integer, Map<String, CodeValue>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Integer, Map<String, CodeValue>> findByKey(Integer key) {
			if (key == null) {
				return null;
			}

			CodelistAdapter adapter = getAdapterInstance();
			if (adapter == null) {
				return null;
			}

			Map<String, CodeValue> map = adapter.getCodeValues(key, systemConfigs.getSystemLanguage());
			if (map == null || map.isEmpty()) {
				return null;
			}
			return new Pair<>(key, map);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Integer, Map<String, CodeValue>> createValue(Map<String, CodeValue> value) {
			throw new UnsupportedOperationException("Codelists cannot be created");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, CodeValue> filterCodeValues(Integer codelist, boolean inclusive, String field,
			String... filterValues) {
		Map<String, CodeValue> codeValues = copy(getCodeValues(codelist));

		if (filterValues == null || filterValues.length == 0) {
			return codeValues;
		}

		List<String> filterValuesList = Arrays.asList(filterValues);

		Iterator<Map.Entry<String, CodeValue>> entries = codeValues.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, CodeValue> entry = entries.next();

			Map<String, Serializable> codeValueProperties = entry.getValue().getProperties();
			String codeValueProperty = (String) codeValueProperties.get(field);
			if (codeValueProperty != null) {
				String[] splittedProperty = CODEVALUE_PROPERTY_SPLIT_PATTERN.split(codeValueProperty);

				Set<String> splittedPropertyAsSet = new HashSet<>(Arrays.asList(splittedProperty));
				filterInternal(inclusive, filterValuesList, entries, splittedPropertyAsSet, filterValues);
			} else {
				entries.remove();
			}
		}

		return codeValues;
	}

	private static void filterInternal(boolean inclusive, List<String> filterValuesList,
			Iterator<Map.Entry<String, CodeValue>> entries, Set<String> splittedPropertyAsSet, String... filterValues) {
		if (inclusive) {
			if (!splittedPropertyAsSet.containsAll(filterValuesList)) {
				entries.remove();
			}
		} else {
			for (String filterValue : filterValues) {
				if (splittedPropertyAsSet.contains(filterValue)) {
					break;
				}
				entries.remove();
			}
		}
	}

}
