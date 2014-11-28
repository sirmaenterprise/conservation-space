/**
 * Copyright (c) 2012 27.03.2012 , Sirma ITT.
 */
package com.sirma.codelist.service.ws;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.sirma.codelist.cache.BaseCache;
import com.sirma.codelist.constants.ClStatusCode;
import com.sirma.codelist.constants.MatchMode;
import com.sirma.codelist.service.CodelistAdministrationService;
import com.sirma.codelist.service.CodelistManager;
import com.sirma.codelist.ws.stub.Codelist;
import com.sirma.codelist.ws.stub.Item;
import com.sirma.codelist.ws.stub.Items;

/**
 * Provides codelist management and cache.
 * 
 * @author B.Bonev
 * @author Valeri Tishev
 * @author Adrian Mitev
 */
public class WSCodelistManager extends
		BaseCache<WSCodelistManager.CodelistKey, Map<String, List<Item>>>
		implements Serializable, CodelistManager {

	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = -8179539675688971188L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(WSCodelistManager.class.getSimpleName());

	/**
	 * Lock for codelist/code values updates. We use fair lock because of the
	 * many read operations and few write.
	 */
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

	/** The temporary CL cache. */
	private Map<WSCodelistManager.CodelistKey, Map<String, List<Item>>> tempCache;

	/** The is update running. */
	private volatile boolean isUpdateRunning = false;

	private CodelistAdministrationService codelistService;

	private boolean initLazy;

	/**
	 * Initializes
	 * 
	 * @param wsdlLocation
	 *            address to the wsdl where the service is located.
	 */
	public WSCodelistManager(String wsdlLocation) {
		codelistService = new CodelistAdministrationService(wsdlLocation,
				"http://ws.ais.egov.sirma.com/codelists", "Codelists");
	}

	/**
	 * Instantiates a new wS codelist manager.
	 * 
	 * @param wsdlLocation
	 *            the wsdl location
	 * @param initLazy
	 *            if <code>true</code> the cache will be lazy initialized
	 */
	public WSCodelistManager(String wsdlLocation, boolean initLazy) {
		this.initLazy = initLazy;
		codelistService = new CodelistAdministrationService(wsdlLocation,
				"http://ws.ais.egov.sirma.com/codelists", "Codelists");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		long start = System.currentTimeMillis();
		LinkedHashMap<CodelistKey, Map<String, List<Item>>> cache = new LinkedHashMap<CodelistKey, Map<String, List<Item>>>(
				(int) (250 * 1.1), 0.95f);

		LOGGER.info("Beggining cache initialization..");
		List<Codelist> codelists = codelistService.getCodelists();
		LOGGER.info("Found " + codelists.size() + " codelist to load.");

		// initialize all codelist cache
		for (Codelist codelist : codelists) {
			Integer clId = codelist.getId().intValue();
			if (clId == null) {
				LOGGER.warning("Found unsupported CL number "
						+ codelist.getId().intValue() + ". Skipping it");
				continue;
			}

			List<Item> list = codelist.getItems().getItem();

			LinkedHashMap<String, List<Item>> valuesMap = new LinkedHashMap<String, List<Item>>(
					(int) (list.size() * 1.1), 0.95f);
			cache.put(new CodelistKey(clId, new CodelistProxy(codelist)),
					valuesMap);

			initializeValues(clId, valuesMap, codelist);
		}

		// apply changes from the temp variables to actual cache
		commitChanges();

		LOGGER.info("Cache initialization complete in "
				+ (System.currentTimeMillis() - start) / 1000.0 + " sec");

		lock.writeLock().lock();
		try {
			// if the current cache is full we release it.
			if (getCache() != null) {
				LOGGER.info("Cleaning codelist cache");
				getCache().clear();
				setCache(null);

				System.gc();
				LOGGER.info("Clean compleate. GC called.");
			}

			setCache(cache);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reInitialize(List<Integer> clId) {
		if ((clId == null) || isUpdateRunning) {
			return;
		}
		isUpdateRunning = true;

		Map<CodelistKey, Map<String, List<Item>>> localCache = new LinkedHashMap<CodelistKey, Map<String, List<Item>>>(
				clId.size() << 1, 0.9f);
		for (Integer cl : clId) {
			if (cl == null) {
				LOGGER.info("Trying to reinitialize unsupported CL . Skipping it");
				continue;
			}
			LOGGER.info("Reinitializing " + cl);
			CodelistKey key = new CodelistKey(cl);
			Map<String, List<Item>> oldValues = getCache().get(key);
			int size = 50;
			if (oldValues != null) {
				size = oldValues.size();
			}
			Map<String, List<Item>> newValues = new LinkedHashMap<String, List<Item>>(
					(int) (size * 1.2), 0.95f);

			initializeValues(cl, newValues, codelistService);
			localCache.put(key, newValues);
		}
		tempCache = localCache;
		isUpdateRunning = false;
	}

	/**
	 * Initialize values.
	 * 
	 * @param clId
	 *            the cl id
	 * @param valuesMap
	 *            is the values map to populate
	 * @param codelist
	 *            the codelist to initialize
	 */
	private void initializeValues(Integer clId,
			Map<String, List<Item>> valuesMap, Codelist codelist) {
		Items itemsInCodelist = codelist.getItems();

		if (itemsInCodelist != null) {
			List<Item> list = itemsInCodelist.getItem();

			for (Item Item : list) {
				List<Item> values = valuesMap.get(Item.getValue());
				if (values == null) {
					values = new LinkedList<Item>();
					valuesMap.put(Item.getValue(), values);
				} else if (!values.get(0).getValue().equals(Item.getValue())) {
				}
				values.add(new ItemProxy(Item));
			}
		}
	}

	/**
	 * Initialize values.
	 * 
	 * @param clId
	 *            the cl id
	 * @param valuesMap
	 *            is the values map to populate
	 * @param codelistService
	 *            the codelist service
	 */
	private void initializeValues(Integer clId,
			Map<String, List<Item>> valuesMap,
			CodelistAdministrationService codelistService) {
		List<Codelist> codelists = codelistService.getCodelistById(clId);

		for (Codelist codelist : codelists) {
			initializeValues(clId, valuesMap, codelist);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commitChanges() {
		LOGGER.info("Applying cache changes..");
		// synch codelist changes
		lock.readLock().lock();
		if (tempCache != null) {
			lock.readLock().unlock();
			lock.writeLock().lock();
			try {
				if (tempCache != null) {
					// the base cache here should never be null!!!!
					// this method call should not override the key!!
					// if this happens the codelist value will be lost until
					// next full initialization
					getCache().putAll(tempCache);

					tempCache.clear();
					tempCache = null;
				}
			} finally {
				lock.writeLock().unlock();
			}
			LOGGER.info("Updated base codelist cache");
		} else {
			lock.readLock().unlock();
		}

		LOGGER.info("Applying cache changes: done");
		System.gc();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Codelist> getAllCodelists() {
		lock.readLock().lock();
		try {
			Set<CodelistKey> set = getCache().keySet();
			List<Codelist> result = new ArrayList<Codelist>(set.size());
			for (CodelistKey codelistKey : set) {
				result.add(codelistKey.codelist);
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Codelist getCodelist(Integer cl) {
		if (cl == null) {
			return null;
		}
		lock.readLock().lock();
		try {
			Set<CodelistKey> set = getCache().keySet();
			for (CodelistKey codelistKey : set) {
				if (codelistKey.clKey.equals(cl)) {
					return codelistKey.codelist;
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItems(Integer cl, Set<String> values, boolean active) {
		Map<String, List<Item>> valuesMap = getCacheValue(cl);
		List<Item> result = new ArrayList<Item>(values.size());
		for (String value : values) {
			List<Item> list = valuesMap.get(value);
			if (active) {
				Item activeValue = getActiveValue(list);
				// we add all versions of the code values
				if (activeValue != null) {
					result.add(activeValue);
				}
			} else {
				if (list != null) {
					result.addAll(list);
				}
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItems(Integer cl, String valueCode, boolean active) {
		Map<String, List<Item>> valuesMap = getCacheValue(cl);
		List<Item> list = valuesMap.get(valueCode);
		if (list == null) {
			Collections.emptyList();
		}
		if (active) {
			Item activeValue = getActiveValue(list);
			if (activeValue != null) {
				return Arrays.asList(activeValue);
			}
		} else {
			return list;
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getItemProperty(Integer cl, String valueCode,
			CodeValuePropertiesEnum property) {
		Map<String, List<Item>> valuesMap = getCacheValue(cl);
		List<Item> list = valuesMap.get(valueCode);
		Comparable<?> value = getPropertyValue(getActiveValue(list), property);
		return toString(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItems(Integer cl, boolean active) {
		Map<String, List<Item>> valuesMap = getCacheValue(cl);
		List<Item> result = new ArrayList<Item>((int) (valuesMap.size() * 1.5));
		for (List<Item> list : valuesMap.values()) {
			if (active) {
				Item Item = getActiveValue(list);
				if (Item != null) {
					result.add(Item);
				}
			} else {
				result.addAll(list);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getAllItems(Integer cl) {
		Map<String, List<Item>> valuesMap = getCacheValue(cl);
		List<Item> result = new ArrayList<Item>(valuesMap.size() << 1);
		for (List<Item> list : valuesMap.values()) {
			result.addAll(list);
		}

		return result;
	}

	/**
	 * Gets the cache value.
	 * 
	 * @param cl
	 *            the cl
	 * @return the cache value
	 */
	private Map<String, List<Item>> getCacheValue(Integer cl) {
		lock.readLock().lock();
		CodelistKey key = new CodelistKey(cl);
		try {
			Map<String, List<Item>> map = getCache().get(key);
			if (map != null && !map.isEmpty()) {
				return map;
			}
		} finally {
			lock.readLock().unlock();
		}
		while (isUpdateRunning) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.warning("Interrupted while waiting for other thread to finish cache update");
			}
		}
		reInitialize(new LinkedList<Integer>(Arrays.asList(cl)));
		commitChanges();

		lock.readLock().lock();
		try {
			Map<String, List<Item>> map = getCache().get(key);
			return map;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItemByDescription(Integer cl, String description) {
		Map<String, List<Item>> cacheValue = getCacheValue(cl);
		List<Item> result = new LinkedList<Item>();
		for (List<Item> list : cacheValue.values()) {
			Item activeValue = getActiveValue(list);
			if ((activeValue != null)
					&& activeValue.getDescription().contains(description)) {
				result.add(activeValue);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItemByExtra2(Integer cl, String extra2) {
		Map<String, List<Item>> cacheValue = getCacheValue(cl);
		List<Item> result = new LinkedList<Item>();
		for (List<Item> list : cacheValue.values()) {
			Item activeValue = getActiveValue(list);
			if ((activeValue != null)
					&& activeValue.getExtra2().contains(extra2)) {
				result.add(activeValue);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItemByProperty(Integer codelist,
			CodeValuePropertiesEnum property, String propertyValue,
			MatchMode matchMode, boolean active) {
		Map<String, List<Item>> cacheValue = getCacheValue(codelist);
		List<Item> result = new LinkedList<Item>();
		for (List<Item> list : cacheValue.values()) {
			if (active) {
				Item activeValue = getActiveValue(list);
				if ((activeValue != null)
						&& hasPropertyValue(activeValue, property,
								propertyValue, matchMode)) {
					result.add(activeValue);
				}
			} else {
				for (int i = 0; i < list.size(); i++) {
					Item value = list.get(i);
					if (hasPropertyValue(value, property, propertyValue,
							matchMode)) {
						result.add(value);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Extract data.
	 * 
	 * @param list
	 *            the list
	 * @param property
	 *            the property
	 * @return the list
	 */
	public static List<String> extractData(List<Item> list,
			CodeValuePropertiesEnum property) {
		List<String> result = new ArrayList<String>(list.size());
		for (Item Item : list) {
			String value = toString(getPropertyValue(Item, property));
			if (value != null) {
				result.add(value);
			}
		}

		return result;
	}

	/**
	 * Checks for property value.
	 * 
	 * @param source
	 *            the source
	 * @param property
	 *            the property
	 * @param propertyValue
	 *            the property value
	 * @param matchMode
	 *            the match mode
	 * @return true, if successful
	 */
	public static boolean hasPropertyValue(Serializable source,
			PropertyEnum property, String propertyValue, MatchMode matchMode) {
		String valueToCheck = toString(getPropertyValue(source, property));
		if ((valueToCheck == null) && (propertyValue == null)) {
			return true;
		} else if ((valueToCheck == null) ^ (propertyValue == null)) {
			return false;
		} else {
			if (matchMode == null) {
				return valueToCheck.equals(propertyValue);
			}
			switch (matchMode) {
			case EXACT:
				return valueToCheck.equals(propertyValue);
			case ANYWHERE:
			case MATCH_EXACT_WORD:
				return valueToCheck.contains(propertyValue);
			case START:
				return valueToCheck.startsWith(propertyValue);
			case START_ANY_WORD:
				return hasPropertyValue(source, property, propertyValue,
						MatchMode.START)
						|| hasPropertyValue(source, property, " "
								+ propertyValue, MatchMode.ANYWHERE);
			case END:
			default:
				return valueToCheck.endsWith(propertyValue);
			}
		}
	}

	/**
	 * Gets the property value.
	 * 
	 * @param entity
	 *            the entity
	 * @param property
	 *            the property
	 * @return the property value
	 */
	public static Comparable<?> getPropertyValue(Serializable entity,
			PropertyEnum property) {
		if (entity == null) {
			return null;
		}
		if (property instanceof CodeValuePropertiesEnum) {
			CodeValuePropertiesEnum prop = (CodeValuePropertiesEnum) property;
			Item codeValue = (Item) entity;
			switch (prop) {
			case VALUE:
				return codeValue.getValue();
			case DESCRIPTION:
				return codeValue.getDescription();
			case EXTRA1:
				return codeValue.getExtra1();
			case EXTRA2:
				return codeValue.getExtra2();
			case EXTRA3:
				return codeValue.getExtra3();
			case MASTER_VALUE:
				return codeValue.getMasterValue();
			}
		}
		return null;
	}

	/**
	 * Returns {@link String} representation of the given object. If the object
	 * is <code>null</code> then the same is returned. If not <code>null</code>
	 * then the method {@link Object#toString()} is called.
	 * 
	 * @param o
	 *            is the target object
	 * @return the string representation of the given object
	 */
	private static String toString(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof BigDecimal) {
			return ((BigDecimal) o).toPlainString();
		}
		return o.toString();
	}

	/**
	 * Sort.
	 * 
	 * @param <E>
	 *            the element type
	 * @param data
	 *            the data
	 * @param property
	 *            the property
	 * @param ascending
	 *            the ascending
	 */
	public static <E extends Serializable> void sort(List<E> data,
			PropertyEnum property, boolean ascending) {
		if ((data == null) || (property == null)) {
			return;
		}
		Collections.sort(data, new PropertyComparator<E>(property, ascending));
	}

	/**
	 * Gets the active value.
	 * 
	 * @param values
	 *            the values
	 * @return the active value
	 */
	private Item getActiveValue(List<Item> values) {
		if (values == null) {
			return null;
		}
		for (int i = 0; i < values.size(); i++) {
			Item value = values.get(i);
			if (isActive(value)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Checks if is active.
	 * 
	 * @param value
	 *            the value
	 * @return true, if is active
	 */
	private boolean isActive(Item value) {
		return !ClStatusCode.INACTIVE.getValue().equals(value.getStatusCode());
	}

	/**
	 * The Class PropertyComparator.
	 * <p>
	 * <b>NOTE:</b>Null values are always forced at the end of the sorted list
	 * 
	 * @param <C>
	 *            the generic type
	 */
	static class PropertyComparator<C extends Serializable> implements
			Comparator<C> {

		/** The property. */
		private final PropertyEnum property;

		/** The ascending. */
		private final boolean ascending;

		/**
		 * Instantiates a new property comparator.
		 * 
		 * @param property
		 *            the property
		 * @param ascending
		 *            the ascending
		 */
		public PropertyComparator(PropertyEnum property, boolean ascending) {
			this.property = property;
			this.ascending = ascending;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public int compare(C o1, C o2) {
			Comparable p1 = getPropertyValue(o1, property);
			Comparable p2 = getPropertyValue(o2, property);
			if (p1 == null) {
				if (p2 == null) {
					return 0;
				} else {
					return 1;
				}
			} else {
				if (p2 == null) {
					return -1;
				} else {
					return (ascending ? 1 : -1) * p1.compareTo(p2);
				}
			}
		}

	}

	/**
	 * The Class CodelistKey.
	 */
	class CodelistKey {

		/** The cl key. */
		Integer clKey;

		/** The codelist. */
		Codelist codelist;

		/**
		 * Instantiates a new codelist key.
		 * 
		 * @param codelistNumber
		 *            the codelist number
		 */
		public CodelistKey(Integer codelistNumber) {
			this.clKey = codelistNumber;
		}

		/**
		 * Instantiates a new codelist key.
		 * 
		 * @param codelistNumber
		 *            the cl key
		 * @param codelist
		 *            the codelist
		 */
		public CodelistKey(Integer codelistNumber, Codelist codelist) {
			this.clKey = codelistNumber;
			this.codelist = codelist;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clKey == null) ? 0 : clKey.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof CodelistKey)) {
				return false;
			}
			CodelistKey other = (CodelistKey) obj;
			if (clKey == null) {
				if (other.clKey != null) {
					return false;
				}
			} else if (!clKey.equals(other.clKey)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * The Interface PropertyEnum.
	 */
	public interface PropertyEnum {

	}

	/**
	 * The Class CodelistProxy.
	 * 
	 * @author B.Bonev
	 */
	private class CodelistProxy extends Codelist {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -2321939383651641215L;

		/**
		 * Instantiates a new codelist proxy.
		 * 
		 * @param src
		 *            the src
		 */
		public CodelistProxy(Codelist src) {
			super.setId(src.getId());
			super.setDescription(src.getDescription());
			super.setExtra1(src.getExtra1());
			super.setExtra2(src.getExtra2());
			super.setExtra3(src.getExtra3());
			super.setMasterCodelist(src.getMasterCodelist());
			super.setComment(src.getComment());
			super.setDisplayType(src.getDisplayType());
		}
	}

	/**
	 * The Class ItemProxy.
	 */
	private class ItemProxy extends Item {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = 9053786795169276895L;

		/**
		 * Instantiates a new code value proxy.
		 * 
		 * @param src
		 *            the src
		 */
		public ItemProxy(Item src) {
			super.setId(src.getId());
			super.setValue(src.getValue());
			super.setDescription(src.getDescription());
			super.setExtra1(src.getExtra1());
			super.setExtra2(src.getExtra2());
			super.setExtra3(src.getExtra3());
			super.setComment(src.getComment());
			super.setMasterValue(src.getMasterValue());
			super.setStatusCode(src.getStatusCode());
			super.setValidFrom(src.getValidFrom());
			super.setValidTo(src.getValidTo());
			super.setCodelistNumber(src.getCodelistNumber());
		}
	}
}
