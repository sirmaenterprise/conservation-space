package com.sirma.itt.cmf.codelist.services;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.codelist.constants.MatchMode;
import com.sirma.codelist.service.CodelistManager;
import com.sirma.codelist.service.CodelistService;
import com.sirma.codelist.service.DefaultCodelistService;
import com.sirma.codelist.service.ws.WSCodelistManager;
import com.sirma.codelist.ws.stub.Codelist;
import com.sirma.codelist.ws.stub.Item;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;

/**
 * Provides an instance to {@link CodelistService}. The instance is kept alive and initialized.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CodelistServiceProvider {

	private static final Logger LOGGER = Logger.getLogger(CodelistServiceProvider.class);

	/** The codelist file location. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_FILE_LOCATION)
	private String fileLocation;

	/** The wsdl location. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_WSDL_LOCATION)
	private String wsdlLocation;

	/** The instance. */
	private CodelistService instance;

	/** The lock. */
	private ReentrantLock lock = new ReentrantLock(false);

	/**
	 * Gets the service instance.
	 *
	 * @return the service instance
	 */
	@Produces
	@Default
	public CodelistService getServiceInstance() {
		if (instance == null) {
			boolean loading = false;
			// check if someone else is creating the cache instance
			if (lock.isLocked()) {
				loading = true;
			}
			lock.lock();
			try {
				// check if the instance is created from someone else while we
				// are
				// waiting for the access, if so return the instance
				if (loading && (instance != null)) {
					return instance;
				}
				CodelistManager manager;
				if (fileLocation != null) {
					manager = new MockupCodelistManager(fileLocation);
				} else {
					manager = new WSCodelistManager(wsdlLocation, true);
				}
				// NOTE: the initialization of the codelist takes some time
				// (>15s (sometimes > 60s))
				instance = new DefaultCodelistService(manager);
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	/**
	 * The Class MockupCodelistManager is mockup manager for codelists.
	 * <strong>codelist.fileLocation</strong> should be provided
	 */
	private class MockupCodelistManager implements CodelistManager {

		/** The codelists. */
		Map<Integer, List<Item>> codelists = new HashMap<Integer, List<Item>>();

		/**
		 * Instantiates a new mockup codelist manager.
		 *
		 * @param clLocation
		 *            the codelist file location
		 */
		public MockupCodelistManager(String clLocation) {
			Properties props = new Properties();
			try (FileInputStream codelistStream = new FileInputStream(clLocation);) {
				props.load(codelistStream);
				Set<Entry<Object, Object>> entrySet = props.entrySet();
				for (Entry<Object, Object> nextEntry : entrySet) {
					Integer code = Integer.valueOf(nextEntry.getKey().toString());
					List<Item> codeVals = new ArrayList<Item>();
					// split by ; as csv
					String[] vals = nextEntry.getValue().toString().split(";");
					for (String string : vals) {
						// code|desc|comment|extra1
						String[] currentVal = string.split("\\|");
						Item item = getItem(currentVal[0], currentVal[1]);
						if (currentVal.length > 2 && StringUtils.isNotNullOrEmpty(currentVal[2])) {
							item.setComment(currentVal[2]);
						}
						if (currentVal.length > 3 && StringUtils.isNotNullOrEmpty(currentVal[3])) {
							item.setExtra1(currentVal[3]);
						}
						item.setCodelistNumber(new BigInteger(nextEntry.getKey().toString()));
						codeVals.add(item);
					}
					codelists.put(code, codeVals);
				}
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#initialize()
		 */
		@Override
		public void initialize() {

		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#reInitialize(java.util .List)
		 */
		@Override
		public void reInitialize(List<Integer> clId) {

		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#commitChanges()
		 */
		@Override
		public void commitChanges() {

		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getAllCodelists()
		 */
		@Override
		public List<Codelist> getAllCodelists() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getCodelist(java.lang. Integer)
		 */
		@Override
		public Codelist getCodelist(Integer cl) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getItems(java.lang.Integer ,
		 * java.util.Set, boolean)
		 */
		@Override
		public List<Item> getItems(Integer cl, Set<String> values, boolean active) {
			List<Item> items = getItems(cl, active);
			List<Item> result = new ArrayList<Item>();
			for (Item item : items) {
				if (values.contains(item.getValue())) {
					result.add(item);
				}
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getItems(java.lang.Integer ,
		 * java.lang.String, boolean)
		 */
		@Override
		public List<Item> getItems(Integer cl, String valueCode, boolean active) {
			List<Item> items = getItems(cl, active);
			List<Item> result = new ArrayList<Item>();
			for (Item item : items) {
				if (valueCode.equals(item.getValue())) {
					result.add(item);
				}
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getItemProperty(java.lang .Integer,
		 * java.lang.String, com.sirma.codelist.service.CodelistManager.CodeValuePropertiesEnum)
		 */
		@Override
		public String getItemProperty(Integer cl, String valueCode, CodeValuePropertiesEnum property) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getItems(java.lang.Integer , boolean)
		 */
		@Override
		public List<Item> getItems(Integer cl, boolean active) {
			try {
				return codelists.get(cl);
			} catch (Exception e) {
				LOGGER.warn("Missing mock for: " + cl, e);
				return new ArrayList<Item>(1);
			}
		}

		/**
		 * Gets the item.
		 *
		 * @param val
		 *            the val
		 * @param desc
		 *            the desc
		 * @return the item
		 */
		private Item getItem(String val, String desc) {
			Item item = new Item();
			item.setValue(val);
			item.setDescription(desc);
			return item;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getAllItems(java.lang. Integer)
		 */
		@Override
		public List<Item> getAllItems(Integer cl) {
			return getItems(cl, true);
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getItemByDescription(java .lang.Integer,
		 * java.lang.String)
		 */
		@Override
		public List<Item> getItemByDescription(Integer cl, String description) {
			List<Item> items = getItems(cl, true);
			List<Item> itemsreturn = new ArrayList<Item>();
			for (Item item : items) {
				if (description.equals(item.getDescription())) {
					itemsreturn.add(item);
				}
			}
			return itemsreturn;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getItemByExtra2(java.lang .Integer,
		 * java.lang.String)
		 */
		@Override
		public List<Item> getItemByExtra2(Integer cl, String extra2) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.codelist.service.CodelistManager#getItemByProperty(java .lang.Integer,
		 * com.sirma.codelist.service.CodelistManager.CodeValuePropertiesEnum, java.lang.String,
		 * com.sirma.codelist.constants.MatchMode, boolean)
		 */
		@Override
		public List<Item> getItemByProperty(Integer codelist, CodeValuePropertiesEnum property,
				String propertyValue, MatchMode matchMode, boolean active) {
			return null;
		}

	}

	/**
	 * Reset codelist service instance.
	 */
	public void resetCodelistServiceInstance() {
		lock.lock();
		try {
			instance = null;
		} finally {
			lock.unlock();
		}
	}
}
