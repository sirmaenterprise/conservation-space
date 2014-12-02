/*
 *
 */
package com.sirma.itt.emf.codelist;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.codelist.adapter.CMFCodelistAdapter;
import com.sirma.itt.emf.codelist.event.CodelistFilterBinding;
import com.sirma.itt.emf.codelist.event.CodelistFiltered;
import com.sirma.itt.emf.codelist.event.ResetCodelistEvent;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.filter.Filter;
import com.sirma.itt.emf.filter.FilterService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.CurrentLocaleProducer;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.PropertyModelComparator;

/**
 * Default codelist service implementation. The service uses a cache entry with name:
 * <code>CODELIST_CACHE</code>>
 * 
 * @author BBonev
 */
@Named("cls")
@ApplicationScoped
public class CodelistServiceImpl implements CodelistService {

	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50), expiration = @Expiration(maxIdle = 900000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the values for the different codelists in the system."
			+ "<br>Minimal value expression: uniqueCodelists * 1.2"))
	private static final String CODELIST_CACHE = "CODELIST_CACHE";

	private static final Logger LOGGER = LoggerFactory.getLogger(CodelistServiceImpl.class);

	@Inject
	private EntityLookupCacheContext cacheContext;
	/** The codelist adapter. */
	@Inject
	private Instance<CMFCodelistAdapter> codelistAdapter;

	/** The locale producer. */
	@Inject
	private CurrentLocaleProducer localeProducer;

	@Inject
	@Any
	private Event<CodelistFiltered> clFilterEvent;

	/** The filter service. */
	@Inject
	private FilterService filterService;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		if (!cacheContext.containsCache(CODELIST_CACHE)) {
			cacheContext.createCache(CODELIST_CACHE, new CodelistCacheLookup());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, CodeValue> getCodeValues(Integer codelist) {
		return getCodeValues(codelist, false);
	}

	@Override
	public CodeValue getCodeValue(Integer codelist, String value) {
		Map<String, CodeValue> map = getCodeValues(codelist, false);
		if (map.isEmpty()) {
			return null;
		}
		return map.get(value);
	}

	/**
	 * {@inheritDoc}
	 */
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
		try {
			return localeProducer.getCurrentLocaleLanguage();
		} catch (ContextNotActiveException e) {
			User user = SecurityContextManager.getFullAuthentication();
			Serializable serializable = null;
			if (user != null) {
				serializable = user.getLanguage();
			}
			if (serializable == null) {
				serializable = Locale.getDefault().getLanguage();
			}
			return serializable.toString();
		}
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, CodeValue> getFilteredCodeValues(Integer codelist, String... filterId) {
		return getFilteredCodeValues(codelist, false, filterId);
	}

	@Override
	public Map<String, CodeValue> getFilteredCodeValues(Integer codelist, boolean sorted,
			String... filterId) {

		Map<String, CodeValue> values = copy(getCodeValues(codelist));

		if ((filterId == null) || (filterId.length == 0)) {
			return values;
		}

		List<Filter> filters = new ArrayList<Filter>(filterId.length);
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
		String result = null;
		if (codeValue != null) {
			Map<String, Serializable> properties = codeValue.getProperties();
			if (properties != null) {
				Serializable serializable = properties.get(getCurrentLanguage());

				result = (String) serializable;
			}
		}
		return result;
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
		}
		return map;
	}

	/**
	 * Sort the given map without modifying it and returns the result in new map instance, sorted by
	 * localized description.
	 * 
	 * @param toSort
	 *            the data to sort
	 * @return the sorted map
	 */
	protected Map<String, CodeValue> sort(Map<String, CodeValue> toSort) {
		List<CodeValue> list = new ArrayList<CodeValue>(toSort.values());
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
	protected Map<String, CodeValue> filterExternal(Integer codelist, String filterId,
			Map<String, CodeValue> values) {
		CodelistFiltered filtered = new CodelistFiltered(values);
		clFilterEvent.select(new CodelistFilterBinding(codelist, filterId)).fire(filtered);

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
	 * Copy the values from the source map to a new instance to protect the internal cache from
	 * modification
	 * 
	 * @param values
	 *            the values
	 * @return the map
	 */
	Map<String, CodeValue> copy(Map<String, CodeValue> values) {
		if ((values == null) || values.isEmpty()) {
			return CollectionUtils.emptyMap();
		}
		Map<String, CodeValue> map = CollectionUtils.createLinkedHashMap(values.size());
		for (Entry<String, CodeValue> entry : values.entrySet()) {
			map.put(entry.getKey(), entry.getValue().clone());
		}
		return map;
	}

	/**
	 * On codelist reset.
	 * 
	 * @param event
	 *            the event
	 */
	public void onCodelistReset(@Observes ResetCodelistEvent event) {
		if (codelistAdapter.isAmbiguous() || codelistAdapter.isUnsatisfied()) {
			return;
		}
		codelistAdapter.get().resetCodelist();
		getCache().clear();
	}

	/**
	 * Retrieves all CodeLists from database.
	 * 
	 * @return all found codelists
	 */
	@Override
	public Map<BigInteger, String> getAllCodelists() {
		if (codelistAdapter.isAmbiguous() || codelistAdapter.isUnsatisfied()) {
			return null;
		}

		Map<BigInteger, String> list = codelistAdapter.get().getAllCodelists();
		if ((list == null) || list.isEmpty()) {
			return null;
		}

		return list;
	}

	/**
	 * The Class CodelistCacheLookup.
	 * 
	 * @author BBonev
	 */
	public class CodelistCacheLookup extends
			EntityLookupCallbackDAOAdaptor<Integer, Map<String, CodeValue>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Integer, Map<String, CodeValue>> findByKey(Integer key) {
			if (key == null) {
				return null;
			}

			if (codelistAdapter.isAmbiguous() || codelistAdapter.isUnsatisfied()) {
				LOGGER.warn("No codelist adapter implementation found!");
				return null;
			}

			Map<String, CodeValue> map = codelistAdapter.get().getCodeValues(key);
			if ((map == null) || map.isEmpty()) {
				return null;
			}
			return new Pair<Integer, Map<String, CodeValue>>(key, map);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Integer, Map<String, CodeValue>> createValue(Map<String, CodeValue> value) {
			throw new UnsupportedOperationException("Codelists cannot be created");
		}
	}

}
