package com.sirma.codelist.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.codelist.exception.CodelistException;
import com.sirma.codelist.ws.stub.Codelist;
import com.sirma.codelist.ws.stub.Item;
import com.sirma.itt.commons.utils.date.DateRange;

/**
 * Interface for CodelistService. <b>Important:</b> All methods that have
 * {@link Date} argument if the argument is <code>null</code> then cached
 * current date will be used to retrieve the data.
 * 
 * @author B.Bonev
 */
public interface CodelistService {

	/**
	 * The Enum CodeValuePropertyType.
	 */
	public enum CodeValuePropertyType {
		/** The VALUE. */
		VALUE,
		/** The DESCRIPTION. */
		DESCRIPTION,
		/** The EXTRA 1. */
		EXTRA1,
		/** The EXTRA 2. */
		EXTRA2,
		/** The EXTRA 3. */
		EXTRA3,
		/** The MASTER value. */
		MASTER_VALUE;
	}

	/**
	 * Returns the master value for the specified cl number and corresponding
	 * value.
	 * 
	 * @param codelist
	 *            Codelist number.
	 * @param value
	 *            Codelist value.
	 * @return The found masterValue, or <code>null</code> if nothing is found.
	 */
	Item getMasterValue(Integer codelist, String value);

	/**
	 * Retrieves the description of the given codelist number and value
	 * depending on the lang parameter. Default language is bulgarian.
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
	 */
	String getItemDescription(Integer codelist, String code)
			throws CodelistException;

	/**
	 * Gets the codelist object based on the its number.
	 * 
	 * @param codelist
	 *            is the codelist number
	 * @return the found codelist or <code>null</code> if the number is not
	 *         found.
	 */
	Codelist getCodelistDescriptor(Integer codelist);

	/**
	 * Retrieves all codelist values pointed by the given codelist number that
	 * are valid for the given date object. The cached instance of the current
	 * date will be used for ranging.
	 * 
	 * @param codelist
	 *            codelist number, cannot be <code>null</code>
	 * @return a list containing all codelist values and descriptions order
	 *         depending the <code>orderBy</code> and <code>direction</code>.
	 *         Never returns <code>null</code>.
	 * @throws CodelistException
	 *             if there is error during retrieving the information or
	 *             codelist number is <code>null</code>.
	 */
	List<Item> getItems(Integer codelist) throws CodelistException;

	/**
	 * Retrieves all codelist values pointed by the given codelist number that
	 * are valid for the given date object. If the date is <code>null</code>
	 * then cached instance of the current date will be used.
	 * 
	 * @param codelist
	 *            codelist number, cannot be <code>null</code>
	 * @param asOfDate
	 *            the issue date, can be <code>null</code>, if <code>null</code>
	 *            current date will be used
	 * @return a list containing all codelist values and descriptions order
	 *         depending the <code>orderBy</code> and <code>direction</code>.
	 *         Never returns <code>null</code>.
	 * @throws CodelistException
	 *             if there is error during retrieving the information or
	 *             codelist number is <code>null</code>.
	 */
	List<Item> getItems(Integer codelist, Date asOfDate)
			throws CodelistException;

	/**
	 * Retrieves all codelist values pointed by the given codelist number that
	 * are valid for the given date range.
	 * 
	 * @param codelist
	 *            codelist number, cannot be <code>null</code>
	 * @param dateRange
	 *            is the date range to retrieve the codelists.
	 * @return a list containing all codelist values and descriptions order
	 *         depending the <code>orderBy</code> and <code>direction</code>.
	 *         Never returns <code>null</code>.
	 * @throws CodelistException
	 *             if there is error during retrieving the information or
	 *             codelist number is <code>null</code>.
	 */
	List<Item> getItems(Integer codelist, DateRange dateRange)
			throws CodelistException;

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
	List<Item> getAllItems(Integer codelist, String code, DateRange date);

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
	String getValueByExtra(Integer codelist, String extra)
			throws CodelistException;
	
	/**
	 * Gets the value by property.
	 *
	 * @param codelist the codelist
	 * @param property the property
	 * @param propertyValue the property value
	 * @return the value by property
	 * @throws CodelistException the codelist exception
	 */
	String getValueByProperty(Integer codelist, CodeValuePropertyType property, String propertyValue)
			throws CodelistException;

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
	String getExtra(Integer codelist, String value) throws CodelistException;

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
	 * @param dateRange
	 *            is the request date range for the valid values
	 * @return the extra field value for the given codelist number or codelist
	 *         number - value pair, or <code>null</code> if there is no extra
	 *         information.
	 * @throws CodelistException
	 *             if there is a problem while retrieving the extra information,
	 *             or the required argument is <code>null</code>, or the given
	 *             value is not from the given codelist number.
	 */
	String getExtra(Integer codelist, String value, DateRange dateRange)
			throws CodelistException;

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
	String getExtra(Integer codelist, String value, Date date)
			throws CodelistException;

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
	boolean isValidCodelist(Integer codelist, String code, Date fromDate)
			throws CodelistException;

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
	 * @param dateRange
	 *            the issue date, can be <code>null</code>
	 * @return <code>true</code> if the given codelist value is contained in the
	 *         set of values pointed by the given codelist number and
	 *         <code>false</code> otherwise.
	 * @throws CodelistException
	 *             if there is a problem while checking the values or some of
	 *             the first two arguments is <code>null</code>
	 */
	boolean isValidCodelist(Integer codelist, String code, DateRange dateRange)
			throws CodelistException;

	/**
	 * Retrieve the {@link Item} by the number of codelist and the value.
	 * 
	 * @param codelist
	 *            number of codelist
	 * @param code
	 *            value of codelist
	 * @return Item bean
	 */
	Item getItems(Integer codelist, String code);

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
	Item getItems(Integer codelist, String code, Date date);

	/**
	 * Retrieve the {@link Item} by the number of codelist and the value.
	 * 
	 * @param codelist
	 *            number of codelist
	 * @param code
	 *            value of codelist
	 * @param dateRange
	 *            date of validity of codelist
	 * @return Item bean
	 */
	Item getItems(Integer codelist, String code, DateRange dateRange);

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
	Set<String> filterInvalidItems(Integer codelist, Set<String> values)
			throws CodelistException;

	/**
	 * Retrieves all CodeLists in the database.
	 * 
	 * @return the found list of states
	 */
	List<Codelist> getAllCodelists();

	/**
	 * Filter a given codelist by a prefix in its value.
	 * 
	 * @param list
	 *            Codelist entity.
	 * @param prefix
	 *            Prefix string.
	 * @return List of codelist's elements whose values contain the given
	 *         prefix.
	 */
	public List<Item> filterCodelistByPrefix(List<Item> list, String prefix);

	/**
	 * Return code values by filter
	 * 
	 * @param codelist
	 *            Codelist value
	 * @param valueSet
	 *            Set of code values to filter (by value)
	 * @return List<Item> cvList - list of code value objects
	 */
	public List<Item> filterItems(Integer codelist, Set<String> valueSet);

	/**
	 * Searches the {@link Item} table. The search applies search on the given
	 * fields with matching anywhere in the string.
	 * 
	 * @param args
	 *            Filter parameters.
	 * @return List of code value.
	 */
	List<Item> filterItem(Item args);

	/**
	 * Search {@link Item}.
	 * 
	 * @param args
	 *            Search arguments.
	 * @return {@link CodelistDTO}.
	 */
	CodelistDTO search(CodelistDTO args);

	/**
	 * Retrieves a set of codelist values that are valid for the given date.
	 * 
	 * @param codelist
	 *            is the codelist number
	 * @param date
	 *            is the validity date to check
	 * @return the set of valid values the given codelist have for the given
	 *         date.
	 */
	Set<String> getItemsSet(Integer codelist, Date date);

	/**
	 * Retrieves a set of codelist values that are valid for the given date.
	 * 
	 * @param codelist
	 *            is the codelist number
	 * @param dateRange
	 *            is the validity date to check
	 * @return the set of valid values the given codelist have for the given
	 *         date.
	 */
	Set<String> getItemsSet(Integer codelist, DateRange dateRange);

	/**
	 * Filters the specified code list extra1 field using a like expression
	 * (match mode - anywhere).
	 * 
	 * @param codelistNumber
	 *            Code list to filter.
	 * @param containedInExtra2
	 *            Contained in extra2 field.
	 * @return List of matched values, empty list if nothing is found.
	 */
	List<String> filterValueLikeExtra2(Integer codelistNumber,
			String containedInExtra2);

	/**
	 * Retrieves from data base the valid code values and national description
	 * for given codelist number and creates a mapping code value:description.
	 * 
	 * @param codelistNumber
	 *            is the codelist number
	 * @param date
	 *            is the date to get the code values or <code>null</code> to use
	 *            cached current date.
	 * @return a map with all values and descriptions of the given codelist or
	 *         empty map if nothing is found.
	 */
	Map<String, String> getCodeDescriptionMapping(Integer codelistNumber,
			Date date);

	/**
	 * Retrieves from data base the valid code values and national description
	 * for given codelist number and creates a mapping code value:description.
	 * 
	 * @param codelistNumber
	 *            is the codelist number
	 * @param dateRange
	 *            is the date to get the code values or <code>null</code> to use
	 *            cached current date.
	 * @return a map with all values and descriptions of the given codelist or
	 *         empty map if nothing is found.
	 */
	Map<String, String> getCodeDescriptionMapping(Integer codelistNumber,
			DateRange dateRange);

	/**
	 * Returns a mapping of code values and parsed extra 2 values. The extra 2
	 * is split to form a set of all elements. The sets are immutable.
	 * 
	 * @param codelistNumber
	 *            the codelist to extract
	 * @return the initialized map
	 */
	Map<String, Set<String>> getValueExtra2Mapping(Integer codelistNumber);

	/**
	 * Queries all {@link Item}s by his description.
	 * 
	 * @param codelistNumber
	 *            is the codelist number
	 * @param descr
	 *            is the needed description to find
	 * @param date
	 *            is the requested validity date
	 * @return the list of all values that meet the condition to have given
	 *         description.
	 */
	List<Item> getItemByDescription(Integer codelistNumber, String descr,
			Date date);

	/**
	 * Queries all {@link Item}s by his description.
	 * 
	 * @param codelistNumber
	 *            is the codelist number
	 * @param descr
	 *            is the needed description to find
	 * @param range
	 *            is the requested validity range
	 * @return the list of all values that meet the condition to have given
	 *         description.
	 */
	List<Item> getItemByDescription(Integer codelistNumber, String descr,
			DateRange range);

}