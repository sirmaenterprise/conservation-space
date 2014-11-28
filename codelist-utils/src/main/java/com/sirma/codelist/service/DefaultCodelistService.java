package com.sirma.codelist.service;

import static com.sirma.itt.commons.utils.objects.ObjectUtils.isValid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.codelist.constants.MatchMode;
import com.sirma.codelist.exception.CodelistException;
import com.sirma.codelist.service.CodelistManager.CodeValuePropertiesEnum;
import com.sirma.codelist.service.ws.WSCodelistManager;
import com.sirma.codelist.ws.stub.Codelist;
import com.sirma.codelist.ws.stub.Item;
import com.sirma.itt.commons.utils.date.DateRange;
import com.sirma.itt.commons.utils.date.DateUtils;
import com.sirma.itt.commons.utils.date.DateUtils.DatePart;

/**
 * Codelist service implementation that uses a web service to fetch it's data.
 * 
 * @author B.Bonev
 * @author Adrian Mitev
 */
public class DefaultCodelistService implements CodelistService {

	/**
	 * Max codelist timeout 1 hour.
	 */
	private static final long MAX_TIMEOUT = 3600000;

	/** The Constant START_DATE. */
	private static final Date START_DATE = DateUtils.createDate(1900, 1, 1);

	/** The Constant END_DATE. */
	private static final Date END_DATE = DateUtils.createDate(2100, 12, 31);

	/** The last date. */
	private static volatile Date lastDate;

	/** The tomorrow. */
	private static volatile Date tomorrow;

	/** The last time. */
	private long lastTime = System.currentTimeMillis();

	/** The tomorrow access time. */
	private long tomorrowAccessTime = System.currentTimeMillis();

	/** The cache. */
	private CodelistManager manager;

	/**
	 * Initializes the underlying codelist manager.
	 * 
	 * @param manager
	 *            {@link CodelistManager} to use.
	 */
	public DefaultCodelistService(CodelistManager manager) {
		this.manager = manager;
		manager.initialize();
	}

	/**
	 * Gets results list filtered by start and end date.
	 * 
	 * @param results
	 *            are all possible code values
	 * @param from
	 *            is the start date
	 * @param to
	 *            is the end date
	 * @return results list filtered by start and end date
	 */
	private List<Item> getTimeFilteredResult(List<Item> results, Date from,
			Date to) {
		List<Item> temp = new ArrayList<Item>(results.size() >> 1);
		for (Item Item : results) {
			if ((Item != null) && isValidItem(Item, from, to)) {
				temp.add(Item);
			}
		}
		return temp;
	}

	/**
	 * Gets results list filtered by date.
	 * 
	 * @param results
	 *            are all possible code values
	 * @param date
	 *            is the particular date
	 * @return results list filtered by start and end date
	 */
	private List<Item> getTimeFilteredResult(List<Item> results, Date date) {
		List<Item> temp = new ArrayList<Item>(results.size());
		for (Item Item : results) {
			if ((Item != null) && isValidItem(Item, date, date)) {
				temp.add(Item);
			}
		}
		return temp;
	}

	/**
	 * Checks if the code value is valid for a particular date.
	 * 
	 * @param Item
	 *            is the value that will be checked
	 * @param from
	 *            is the start date
	 * @param to
	 *            is the end date
	 * @return true if the code value is valid for a particular date
	 */
	private boolean isValidItem(Item Item, Date from, Date to) {
		if (Item.getValidFrom() == null && Item.getValidTo() == null) {
			return true;
		}
		Date from1 = Item.getValidFrom();
		Date to1 = Item.getValidTo();
		if (from1 == null && to1 != null) {
			if (DateUtils.isBeforeOrSame(DatePart.DAY, to, to1)) {
				return true;
			}
		} else if (from1 != null && to1 == null) {
			if (DateUtils.isBeforeOrSame(DatePart.DAY, from1, to)) {
				return true;
			}
		} else if ((DateUtils.isBeforeOrSame(DatePart.DAY, from1, from) && DateUtils
				.isBefore(DatePart.DAY, from, to1))
				|| (DateUtils.isBeforeOrSame(DatePart.DAY, from1, to) && DateUtils
						.isBefore(DatePart.DAY, to, to1))
				|| (DateUtils.isBeforeOrSame(DatePart.DAY, from, from1) && DateUtils
						.isBefore(DatePart.DAY, to1, to))) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieves the description of the given codelist number and value.
	 * 
	 * @param codelist
	 *            codelist number, cannot be <code>null</code>
	 * @param code
	 *            codelist value, cannot be <code>null</code>
	 * @return description in the specified language, or <code>null</code> if
	 *         nothing is found
	 * @throws CodelistException
	 *             if there is a problem while retrieving the values, or some of
	 *             the required arguments is <code>null</code>
	 * @see ClLanguage
	 */
	public String getItemDescription(Integer codelist, String code)
			throws CodelistException {
		return manager.getItemProperty(codelist, code,
				CodelistManager.CodeValuePropertiesEnum.DESCRIPTION);
	}

	/**
	 * Gets the codelist object based on the its number.
	 * 
	 * @param codelist
	 *            is the codelist number
	 * @return the found codelist or <code>null</code> if the number is not
	 *         found.
	 */
	public Codelist getCodelistDescriptor(Integer codelist) {
		return manager.getCodelist(codelist);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getCodeDescriptionMapping(Integer codelist,
			Date date) {
		List<Item> list = manager.getAllItems(codelist);
		if (!isValid(list)) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new LinkedHashMap<String, String>();
		List<Item> values = getTimeFilteredResult(list, getNotNullDate(date));
		for (Item value : values) {
			map.put(value.getValue(), value.getDescription());
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getCodeDescriptionMapping(Integer codelist,
			DateRange date) {
		List<Item> list = manager.getAllItems(codelist);
		if (!isValid(list)) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new LinkedHashMap<String, String>();
		List<Item> values = getTimeFilteredResult(list, getFromDate(date),
				getToDate(date));
		for (Item value : values) {
			map.put(value.getValue(), value.getDescription());
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItemByDescription(Integer codelist, String descr,
			Date date) {
		List<Item> list = manager.getItemByProperty(codelist,
				CodelistManager.CodeValuePropertiesEnum.DESCRIPTION, descr,
				MatchMode.EXACT, false);
		if (!isValid(list)) {
			return Collections.emptyList();
		}
		return getTimeFilteredResult(list, getNotNullDate(date));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItemByDescription(Integer codelist, String descr,
			DateRange date) {
		List<Item> list = manager.getItemByProperty(codelist,
				CodelistManager.CodeValuePropertiesEnum.DESCRIPTION, descr,
				MatchMode.EXACT, false);
		if (!isValid(list)) {
			return Collections.emptyList();
		}
		return getTimeFilteredResult(list, getFromDate(date), getToDate(date));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItems(Integer codelist) throws CodelistException {
		Date date = null;
        return getItems(codelist, date);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItems(Integer codelist, Date asOfDate)
			throws CodelistException {
		List<Item> list = manager.getItems(codelist, false);
		if (!isValid(list)) {
			return Collections.emptyList();
		}
		return getTimeFilteredResult(list, getNotNullDate(asOfDate));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> getItems(Integer codelist, DateRange asOfDate)
			throws CodelistException {
		List<Item> list = manager.getItems(codelist, false);
		if (!isValid(list)) {
			return Collections.emptyList();
		}
		return getTimeFilteredResult(list, getFromDate(asOfDate),
				getToDate(asOfDate));
	}

	/**
	 * Finds the code list value by code list number and extra field. The both
	 * attributes cannot be <code>null</code> <b>Warning: Due to inconsistent
	 * database state the getValueByExtra operation is not supported for CL60.
	 * </b>
	 * 
	 * @param codelist
	 *            is the number of the code list, cannot be <code>null</code>
	 * @param extra
	 *            is the extra field description for the needed code list value,
	 *            cannot be <code>null</code>
	 * @return the code list value that is part of the given codelist number and
	 *         has the given extra information
	 * @throws CodelistException
	 *             if there is a problem while retrieving the values or some of
	 *             the arguments is <code>null</code>
	 */
	public String getValueByExtra(Integer codelist, String extra)
			throws CodelistException {
		String modExtra = escape(extra);
		List<Item> property = manager.getItemByProperty(codelist,
				CodelistManager.CodeValuePropertiesEnum.EXTRA1, modExtra,
				MatchMode.ANYWHERE, false);

		String value = null;
		if (!property.isEmpty()) {
			value = property.get(0).getValue();
		}
		return value;
	}
	
	
	/**
	* {@inheritDoc}
	*/
	public String getValueByProperty(Integer codelist, CodeValuePropertyType codeValueProperty, String propertyValue)
			throws CodelistException {
		
		CodeValuePropertiesEnum valueProperty = null;
		
		switch (codeValueProperty) {
		case EXTRA1: 
			valueProperty = CodeValuePropertiesEnum.EXTRA1;
			break;
		case EXTRA2:
			valueProperty = CodeValuePropertiesEnum.EXTRA2;
			break;
			
		case EXTRA3:
			valueProperty = CodeValuePropertiesEnum.EXTRA3;
			break;
			
		case DESCRIPTION:
			valueProperty = CodeValuePropertiesEnum.DESCRIPTION;
			break;
			
		case MASTER_VALUE:
			valueProperty = CodeValuePropertiesEnum.MASTER_VALUE;
			break;
			
		case VALUE:
			valueProperty = CodeValuePropertiesEnum.VALUE;
			break;

		default:
			break;
		}
		
		String escapedPropertyValue = escape(propertyValue);
		List<Item> property = manager.getItemByProperty(codelist,
				valueProperty, escapedPropertyValue,
				MatchMode.ANYWHERE, false);

		String value = null;
		if (!property.isEmpty()) {
			value = property.get(0).getValue();
		}
		return value;
	}

	/**
	 * Retrieves the extra information for given codelist number or codelist
	 * number and value pair. If the second parameter is <code>null</code> then
	 * the result is the extra field for the given codelist number. If both
	 * arguments are not <code>null</code> then the result is the extra field
	 * for the given pair.
	 * 
	 * @param codelist
	 *            is the codelist number, cannot be <code>null</code>
	 * @param value
	 *            is a value from the given codelist or <code>null</code>
	 * @return the extra field value for the given codelist number or codelist
	 *         number - value pair, or <code>null</code> if there is no extra
	 *         information.
	 * @throws CodelistException
	 *             if there is a problem while retrieving the extra information,
	 *             or the required argument is <code>null</code>, or the given
	 *             value is not from the given codelist number.
	 */
	public String getExtra(Integer codelist, String value)
			throws CodelistException {
		if (value == null) {
			Codelist codeList = getCodelistDescriptor(codelist);
			return codeList.getExtra1();
		}
		Item Item = getItems(codelist, value, getCurrentDate());
		if (Item == null) {
			return null;
		}
		return Item.getExtra1();
	}

	/**
	 * Retrieves the extra information for given codelist number or codelist
	 * number and value pair. If the second parameter is <code>null</code> then
	 * the result is the extra field for the given codelist number. If both
	 * arguments are not <code>null</code> then the result is the extra field
	 * for the given pair.
	 * 
	 * @param codelist
	 *            is the codelist number, cannot be <code>null</code>
	 * @param value
	 *            is a value from the given codelist or <code>null</code>
	 * @param date
	 *            is the request date
	 * @return the extra field value for the given codelist number or codelist
	 *         number - value pair, or <code>null</code> if there is no extra
	 *         information.
	 * @throws CodelistException
	 *             if there is a problem while retrieving the extra information,
	 *             or the required argument is <code>null</code>, or the given
	 *             value is not from the given codelist number.
	 */
	public String getExtra(Integer codelist, String value, Date date)
			throws CodelistException {
		if (value == null) {
			Codelist codeList = getCodelistDescriptor(codelist);
			return codeList.getExtra1();
		}
		Item Item = getItems(codelist, value, getNotNullDate(date));
		if (Item == null) {
			return null;
		}
		return Item.getExtra1();
	}

	/**
	 * Retrieves the extra information for given codelist number or codelist
	 * number and value pair. If the second parameter is <code>null</code> then
	 * the result is the extra field for the given codelist number. If both
	 * arguments are not <code>null</code> then the result is the extra field
	 * for the given pair.
	 * 
	 * @param codelist
	 *            is the codelist number, cannot be <code>null</code>
	 * @param value
	 *            is a value from the given codelist or <code>null</code>
	 * @param date
	 *            is the request date
	 * @return the extra field value for the given codelist number or codelist
	 *         number - value pair, or <code>null</code> if there is no extra
	 *         information.
	 * @throws CodelistException
	 *             if there is a problem while retrieving the extra information,
	 *             or the required argument is <code>null</code>, or the given
	 *             value is not from the given codelist number.
	 */
	public String getExtra(Integer codelist, String value, DateRange date)
			throws CodelistException {
		if (value == null) {
			Codelist codeList = getCodelistDescriptor(codelist);
			return codeList.getExtra1();
		}
		Item Item = getItems(codelist, value, date);
		if (Item == null) {
			return null;
		}
		return Item.getExtra1();
	}

	/**
	 * Verifies if the given codelist value is part of the set of values pointed
	 * by given codelist number. The <code>{@link java.util.Date}</code>
	 * argument is optional. If not specified (<code>null</code>), the check is
	 * done for the current date.
	 * 
	 * @param codelist
	 *            codelist number, cannot be <code>null</code>
	 * @param code
	 *            codelist value, cannot be <code>null</code>
	 * @param fromDate
	 *            the issue date, can be <code>null</code>
	 * @return <code>true</code> if the given codelist value is contained in the
	 *         set of values pointed by the given codelist number and
	 *         <code>false</code> otherwise.
	 * @throws CodelistException
	 *             if there is a problem while checking the values or some of
	 *             the first two arguments is <code>null</code>
	 */
	public boolean isValidCodelist(Integer codelist, String code, Date fromDate)
			throws CodelistException {
		if (!isValid(code)) {
			return false;
		}
		Item result = null;
		try {
			result = getItems(codelist, code, getNotNullDate(fromDate));
		} catch (RuntimeException e) {
			throw new CodelistException(e);
		}
		if ((result == null) || (result.getValue() == null)) {
			return false;
		}
		return result.getValue().compareTo(code) == 0;
	}

	/**
	 * Verifies if the given codelist value is part of the set of values pointed
	 * by given codelist number. The <code>{@link java.util.Date}</code>
	 * argument is optional. If not specified (<code>null</code>), the check is
	 * done for the current date.
	 * 
	 * @param codelist
	 *            codelist number, cannot be <code>null</code>
	 * @param code
	 *            codelist value, cannot be <code>null</code>
	 * @param range
	 *            the issue date, can be <code>null</code>
	 * @return <code>true</code> if the given codelist value is contained in the
	 *         set of values pointed by the given codelist number and
	 *         <code>false</code> otherwise.
	 * @throws CodelistException
	 *             if there is a problem while checking the values or some of
	 *             the first two arguments is <code>null</code>
	 */
	public boolean isValidCodelist(Integer codelist, String code,
			DateRange range) throws CodelistException {
		if (!isValid(code)) {
			return false;
		}
		Item result = null;
		try {
			result = getItems(codelist, code, range);
		} catch (RuntimeException e) {
			throw new CodelistException(e);
		}
		if ((result == null) || (result.getValue() == null)) {
			return false;
		}
		return result.getValue().compareTo(code) == 0;
	}

	/**
	 * Retrieve the {@link Item} by the number of codelist and the value.
	 * 
	 * @param codelist
	 *            number of codelist
	 * @param code
	 *            value of codelist
	 * @return Item bean
	 */
	public Item getItems(Integer codelist, String code) {
		return getItems(codelist, code, getCurrentDate());
	}

	/**
	 * Retrieve the {@link Item} by the number of codelist and the value.
	 * 
	 * @param codelist
	 *            number of codelist
	 * @param code
	 *            value of codelist
	 * @param date
	 *            date of validity of codelist
	 * @return Item bean
	 */
	public Item getItems(Integer codelist, String code, Date date) {
		List<Item> list = manager.getItems(codelist, code, false);
		if (!isValid(list)) {
			return null;
		}
		List<Item> values = getTimeFilteredResult(list, getNotNullDate(date));
		if (!isValid(values)) {
			return null;
		}
		return values.get(0);
	}

	/**
	 * Retrieve the {@link Item} by the number of codelist and the value.
	 * 
	 * @param codelist
	 *            number of codelist
	 * @param code
	 *            value of codelist
	 * @param date
	 *            date of validity of codelist
	 * @return Item bean
	 */
	public Item getItems(Integer codelist, String code, DateRange date) {
		List<Item> list = manager.getItems(codelist, code, false);
		if (!isValid(list)) {
			return null;
		}
		List<Item> values = getTimeFilteredResult(list, getFromDate(date),
				getToDate(date));
		if (!isValid(values)) {
			return null;
		}
		return values.get(0);
	}

	/**
	 * Retrieve the {@link Item} by the number of codelist and the value.
	 * 
	 * @param codelist
	 *            number of codelist
	 * @param code
	 *            value of codelist
	 * @param date
	 *            date of validity of codelist
	 * @return Item bean
	 */
	public List<Item> getAllItems(Integer codelist, String code, DateRange date) {
		List<Item> list = manager.getItems(codelist, code, false);
		if (!isValid(list)) {
			return Collections.emptyList();
		}
		List<Item> values = getTimeFilteredResult(list, getFromDate(date),
				getToDate(date));
		if (!isValid(values)) {
			return Collections.emptyList();
		}
		return values;
	}

	/**
	 * Filters the given code values for the specified codelist. The result is
	 * the invalid elements from the list which does not belong to the specified
	 * codelist. If all the elements are correct the method returns empty set.
	 * If all the elements are invalid then the result is the original set
	 * object. If the given set is <code>null</code> or empty returns it without
	 * executing any queries. If the codelist is invalid exception is thrown.
	 * <p>
	 * All checks are performed for the current day.
	 * 
	 * @param codelist
	 *            is the codelist number.
	 * @param values
	 *            are the values to be checked.
	 * @return the invalid elements if any, never returns <code>null</code>.
	 * @throws CodelistException
	 *             if the given codelist is invalid
	 */
	public Set<String> filterInvalidItems(Integer codelist, Set<String> values)
			throws CodelistException {
		Set<String> result = new HashSet<String>(values);
		if (!isValid(values)) {
			return result;
		}
		List<Item> list = manager.getItems(codelist, values, false);
		if (!isValid(list)) {
			return result;
		}
		List<Item> res = getTimeFilteredResult(list,
				getNotNullDate(getCurrentDate()));
		if (isValid(res)) {
			for (Item Item : res) {
				result.remove(Item.getValue());
			}
		}
		return result;
	}

	/**
	 * Retrieves all CodeLists in the database.
	 * 
	 * @return the found list of states
	 */
	public List<Codelist> getAllCodelists() {
		List<Codelist> resultList = manager.getAllCodelists();
		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> filterCodelistByPrefix(List<Item> list, String prefix) {
		if (!isValid(list)) {
			return list;
		}
		List<Item> result = new ArrayList<Item>(list.size());
		for (Item value : result) {
			if (value.getValue().startsWith(prefix)) {
				result.add(value);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> filterItems(Integer codelist, Set<String> valueSet) {
		if (!isValid(valueSet)) {
			return Collections.emptyList();
		}
		List<Item> resultList = manager.getItems(codelist, valueSet, true);
		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Item getMasterValue(Integer codelist, String value) {
		List<Item> result = manager.getItemByProperty(codelist,
				CodelistManager.CodeValuePropertiesEnum.MASTER_VALUE, value,
				MatchMode.EXACT, true);
		if (!result.isEmpty()) {
			return result.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getItemsSet(Integer codelist, Date date) {

		List<Item> list = manager.getItems(codelist, false);
		if (!isValid(list)) {
			return Collections.emptySet();
		}
		Set<String> result = new LinkedHashSet<String>();
		List<Item> res = getTimeFilteredResult(list, getNotNullDate(date));
		if (isValid(res)) {
			for (Item Item : res) {
				result.add(Item.getValue());
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getItemsSet(Integer codelist, DateRange date) {
		List<Item> list = manager.getItems(codelist, false);
		if (!isValid(list)) {
			return Collections.emptySet();
		}
		Set<String> result = new LinkedHashSet<String>();
		List<Item> res = getTimeFilteredResult(list, getFromDate(date),
				getToDate(date));
		if (isValid(res)) {
			for (Item Item : res) {
				result.add(Item.getValue());
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> filterValueLikeExtra2(Integer cl,
			String containedInExtra2) {
		List<Item> list = manager.getItemByProperty(cl,
				CodelistManager.CodeValuePropertiesEnum.EXTRA2,
				containedInExtra2, MatchMode.ANYWHERE, true);
		return WSCodelistManager.extractData(list,
				CodelistManager.CodeValuePropertiesEnum.VALUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Set<String>> getValueExtra2Mapping(Integer cl) {
		List<Item> Items = manager.getItems(cl, true);
		Map<String, Set<String>> resultMap = new LinkedHashMap<String, Set<String>>();
		for (Item Item : Items) {
			String value = Item.getValue();
			String extra = Item.getExtra2();
			if (isValid(extra)) {
				String[] split = extra.split("[,\\s]");
				Set<String> set = Collections
						.unmodifiableSet(new HashSet<String>(Arrays
								.asList(split)));
				resultMap.put(value, set);
			}
		}
		return resultMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Item> filterItem(Item args) {
		if (isValid(args)) {
			List<Item> toFilter;
			if (isValid(args.getId())) {
				// get specific codelist
				toFilter = new LinkedList<Item>(manager.getItems(args.getId()
						.intValue(), true));
			} else {
				// get all codelist values
				// As this is very slow operation, no results would be returned
				toFilter = new LinkedList<Item>();
			}
			for (Iterator<Item> it = toFilter.iterator(); it.hasNext();) {
				Item Item = it.next();
				if (isValid(args.getValue())) {
					if (!WSCodelistManager.hasPropertyValue(Item,
							CodelistManager.CodeValuePropertiesEnum.VALUE,
							args.getValue(), MatchMode.ANYWHERE)) {
						it.remove();
						continue;
					}
				}
				if (isValid(args.getMasterValue())) {
					if (!WSCodelistManager
							.hasPropertyValue(
									Item,
									CodelistManager.CodeValuePropertiesEnum.MASTER_VALUE,
									args.getMasterValue(), MatchMode.ANYWHERE)) {
						it.remove();
						continue;
					}
				}
				if (isValid(args.getExtra1())) {
					if (!WSCodelistManager.hasPropertyValue(Item,
							CodelistManager.CodeValuePropertiesEnum.EXTRA1,
							args.getExtra1(), MatchMode.ANYWHERE)) {
						it.remove();
						continue;
					}
				}
				if (isValid(args.getExtra2())) {
					if (!WSCodelistManager.hasPropertyValue(Item,
							CodelistManager.CodeValuePropertiesEnum.EXTRA2,
							args.getExtra2(), MatchMode.ANYWHERE)) {
						it.remove();
						continue;
					}
				}
				if (isValid(args.getExtra3())) {
					if (!WSCodelistManager.hasPropertyValue(Item,
							CodelistManager.CodeValuePropertiesEnum.EXTRA3,
							args.getExtra3(), MatchMode.ANYWHERE)) {
						it.remove();
						continue;
					}
				}
				if (isValid(args.getDescription())) {
					if (!WSCodelistManager
							.hasPropertyValue(
									Item,
									CodelistManager.CodeValuePropertiesEnum.DESCRIPTION,
									args.getDescription(), MatchMode.EXACT)) {
						it.remove();
						continue;
					}
				}
			}

			return toFilter;
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public CodelistDTO search(CodelistDTO args) {
		// gather all code values that a possible matches by code list
		List<Item> toFilter = new LinkedList<Item>();
		if (isValid(args.getCodelistNumbers())) {
			for (Integer codelistNumber : args.getCodelistNumbers()) {
				if (isValid(args.getMasterCl())) {
					Codelist clList = manager.getCodelist(codelistNumber);
					if (isValid(clList.getMasterCodelist())
							&& args.getMasterCl().equals(
									clList.getMasterCodelist())) {
						toFilter.addAll(manager.getItems(codelistNumber, false));
					}
				} else {
					toFilter.addAll(manager.getItems(codelistNumber, false));
				}
			}
		} else {
			// this is potentially slow operation
			// as not other filter is applied and will be skipped
		}

		// filter out the code values that does not match any of the criteria
		for (Iterator<Item> it = toFilter.iterator(); it.hasNext();) {
			Item Item = it.next();
			// check values
			if (isValid(args.getCodeValues())
					&& !filterValue(Item,
							CodelistManager.CodeValuePropertiesEnum.VALUE,
							args.getCodeValues(),
							args.getCodelistValuesMatchMode(),
							args.isCodelistValuesFilterMode(),
							args.isFilterDisjunction())) {
				it.remove();
				continue;
				// check master values
			} else if (isValid(args.getMasterValues())
					&& !filterValue(
							Item,
							CodelistManager.CodeValuePropertiesEnum.MASTER_VALUE,
							args.getMasterValues(),
							args.getMasterValuesMatchMode(),
							args.isMasterValuesFilterMode(), false)) {
				it.remove();
				continue;
				// check descriptions
			} else if (isValid(args.getDescriptions())
					&& !filterValue(
							Item,
							CodelistManager.CodeValuePropertiesEnum.DESCRIPTION,
							args.getDescriptions(),
							args.getDescriptionsMatchMode(),
							args.isDescriptionsFilterMode(), false)) {
				it.remove();
				continue;
				// check custom filtering
			} else if (isValid(args.getFilterMapValues())) {
				Map<String, Set<String>> values = args.getFilterMapValues();
				Map<String, MatchMode> matchModes = args
						.getFilterMapMatchModes();
				Map<String, Boolean> filterModes = args.getFilterMapModes();
				for (String key : values.keySet()) {
					Boolean filter = filterModes.get(key);
					if (filter == null) {
						filter = Boolean.TRUE;
					}
					if (!filterValue(Item,
							CodelistManager.CodeValuePropertiesEnum
									.getProperty(key), values.get(key),
							matchModes.get(key), filter.booleanValue(),
							args.isFilterDisjunction())) {
						it.remove();
						break;
					}
				}
			}
		}
		// filter the left values by date if any
		List<Item> list = toFilter;
		if (isValid(list)) {
			Date from = args.getValidFrom();
			if (from == null) {
				from = START_DATE;
			}
			Date to = args.getValidTo();
			if (to == null) {
				to = END_DATE;
			}
			if (isSingleInterval(from, to)) {
				args.setResult(getTimeFilteredResult(list, from));
			} else {
				args.setResult(getTimeFilteredResult(list, from, to));
			}

			if (isValid(args.getOrderBy())) {
				WSCodelistManager.sort(args.getResult(),
						CodelistManager.CodeValuePropertiesEnum
								.getProperty(args.getOrderBy()), !args
								.isOrderDirection());
			}
		} else {
			args.setResult(Collections.EMPTY_LIST);
		}
		return args;
	}

	/**
	 * Checks if the given code value property matches one of the values using
	 * the given match mode and filter mode.
	 * 
	 * @param Item
	 *            is the code value to check
	 * @param property
	 *            is the code value property to check
	 * @param values
	 *            is the allowed list of value to check against
	 * @param matchMode
	 *            is the match mode to use, can be <code>null</code>
	 * @param filterMode
	 *            is the filter mode. If <code>false</code> and inverted check
	 *            is performed for all values
	 * @param disjunction
	 *            to use a disjunctions (or) when matching different values
	 * @return <code>true</code> if the value matches the criteria
	 */
	private boolean filterValue(Serializable Item,
			WSCodelistManager.PropertyEnum property, Set<String> values,
			MatchMode matchMode, boolean filterMode, boolean disjunction) {

		if (!isValid(values)) {
			return true;
		}

		boolean accept = false;

		if (isValid(matchMode)) {
			int hitCount = 0;
			for (String code : values) {
				if (WSCodelistManager.hasPropertyValue(Item, property, code,
						matchMode)) {
					hitCount++;
					if (disjunction) {
						break;
					}
				}
			}
			if (disjunction) {
				accept = hitCount > 0;
			} else {
				accept = hitCount == values.size();
			}
		} else {
			Comparable<?> propertyValue = WSCodelistManager.getPropertyValue(
					Item, property);
			if (propertyValue instanceof String) {
				accept = values.contains(propertyValue);
			}
		}

		if (filterMode) {
			return accept;
		} else {
			return !accept;
		}
	}

	/**
	 * Removes the % and ; char occurrences from the given string.
	 * 
	 * @param string
	 *            is the string to escape
	 * @return the escaped string
	 */
	private String escape(String string) {
		return string.replaceAll("%", "").replaceAll(";", "");
	}

	/**
	 * Gets the current date. The date instance is cached for 1 hour.
	 * 
	 * @return the current date
	 */
	protected Date getCurrentDate() {
		if (System.currentTimeMillis() - lastTime > MAX_TIMEOUT) {
			lastDate = null;
		}
		if (lastDate == null) {
			lastDate = DateUtils.getToday().getTime();
			lastTime = System.currentTimeMillis();
		}
		return lastDate;
	}

	/**
	 * Gets the tomorrow date. The date instance is cached for 1 hour.
	 * 
	 * @return tomorrow
	 */
	protected Date getTomorrow() {
		if (System.currentTimeMillis() - tomorrowAccessTime > MAX_TIMEOUT) {
			tomorrow = null;
		}
		if (tomorrow == null) {
			// tomorrow
			Calendar calendar = DateUtils.getToday();
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			tomorrow = calendar.getTime();
			tomorrowAccessTime = System.currentTimeMillis();
		}
		return tomorrow;
	}

	/**
	 * Returns a not <code>null</code> date. If the argument is not
	 * <code>null</code> then returns it otherwise returns the current cached
	 * date instance.
	 * 
	 * @param date
	 *            is the date to check.
	 * @return the not <code>null</code> date.
	 */
	protected Date getNotNullDate(Date date) {
		if (date == null) {
			return getCurrentDate();
		}
		return date;
	}

	/**
	 * Returns a not <code>null</code> date. If the argument is not
	 * <code>null</code> returns current date if start it otherwise returns the
	 * current cached date instance.
	 * 
	 * @param dateRange
	 *            is the date to check.
	 * @return the not <code>null</code> date.
	 */
	protected Date getFromDate(DateRange dateRange) {
		if ((dateRange == null) || (dateRange.getFrom() == null)) {
			return START_DATE;
		}
		return dateRange.getFrom();
	}

	/**
	 * Returns a not <code>null</code> date. If the argument is not
	 * <code>null</code> then returns it otherwise returns the current cached
	 * date instance.
	 * 
	 * @param dateRange
	 *            is the date to check.
	 * @return the not <code>null</code> date.
	 */
	protected Date getToDate(DateRange dateRange) {
		if ((dateRange == null) || (dateRange.getTo() == null)) {
			return END_DATE;
		}
		return dateRange.getTo();
	}

	/**
	 * Check if the given range of dates represent a single date or interval.
	 * For a single date is assumed both ends of the date range to overlap.
	 * 
	 * @param from
	 *            is the start of the interval
	 * @param to
	 *            is the end of the interval
	 * @return <code>true</code> if the date range represents a single date and
	 *         <code>false</code> if given range is <code>null</code> or
	 *         represents an interval.
	 */
	protected boolean isSingleInterval(Date from, Date to) {
		if ((from == null) && (to == null)) {
			// NOTE: this here is because when we work without interval we
			// actually work with very wide interval.
			return false;
		}
		return DateUtils.isSame(DatePart.DAY, from, to);
	}

}
