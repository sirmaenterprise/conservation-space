package com.sirma.cmf.web.form.picklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * The ItemsConverter is centralized logic for converting string values to list or single value.
 * 
 * @author bbanchev
 */
public class ItemsConverter {

	/** The DELIMITER between values. */
	public static final String DELIMITER = "\u00B6";

	/**
	 * Convert object to items.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param value
	 *            the value
	 * @param multivalued
	 *            the multivalued
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertObjectToItems(Object value, boolean multivalued) {
		if (value instanceof String) {
			if (StringUtils.isNullOrEmpty((String) value)) {
				if (multivalued) {
					return (T) Collections.emptyList();
				}
				return (T) "";
			}
			if (multivalued) {
				String stringVal = value.toString();
				String[] items = null;
				if (stringVal.startsWith("[") && stringVal.endsWith("]")) {
					items = stringVal.substring(1, stringVal.length() - 1).split(", ");
				} else {
					items = stringVal.split(DELIMITER);
				}
				List<String> itemsList = new ArrayList<>(items.length);
				for (String item : items) {
					itemsList.add(item.trim());
				}
				return (T) itemsList;
			}
			return (T) value;
		} else if (value instanceof Collection) {
			return (T) value;
		}
		return (T) value;
	}

	/**
	 * Convert items to specific known format, that to be handled in hidden field.
	 * 
	 * @param propertyValue
	 *            the property value
	 * @return the string with values
	 */
	public static String convertItemsToString(Object propertyValue) {
		return convertItemsToString(propertyValue, DELIMITER);
	}

	/**
	 * Convert items to string using given delimiter.
	 * 
	 * @param propertyValue
	 *            the property value to convert to string
	 * @param delimiter
	 *            the delimiter is the separator
	 * @return the string
	 */
	public static String convertItemsToString(Object propertyValue, String delimiter) {
		StringBuilder splitted = new StringBuilder();

		if (propertyValue instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Iterator collection = ((Collection) propertyValue).iterator();
			while (collection.hasNext()) {
				Object object = collection.next();
				splitted.append(object);
				if (collection.hasNext()) {
					splitted.append(delimiter);
				}
			}
		} else if (propertyValue != null) {
			splitted.append(propertyValue);
		}
		return splitted.toString();
	}
}
