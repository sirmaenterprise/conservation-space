package com.sirma.codelist.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.codelist.constants.CodeValueDef;
import com.sirma.codelist.constants.MatchMode;
import com.sirma.codelist.service.ws.WSCodelistManager.PropertyEnum;
import com.sirma.codelist.ws.stub.Codelist;
import com.sirma.codelist.ws.stub.Item;

/**
 * Codelist manager that should be implemented for use by the CodelistService.
 * 
 * @author Adrian Mitev
 */
public interface CodelistManager {

	/**
	 * Initializes the cache.
	 */
	public abstract void initialize();

	/**
	 * Re initialize the cache for the particular codelist.
	 * 
	 * @param clId
	 *            is the codelist to reinitialize
	 */
	public abstract void reInitialize(List<Integer> clId);

	/**
	 * Merges the cache changes with the actual cache. If no changes are done
	 * the method does not modify the cached values at all.
	 */
	public abstract void commitChanges();

	/**
	 * Gets the all codelists.
	 * 
	 * @return the all codelists
	 */
	public abstract List<Codelist> getAllCodelists();

	/**
	 * Gets a codelist.
	 * 
	 * @param cl
	 *            the cl
	 * @return the codelist
	 */
	public abstract Codelist getCodelist(Integer cl);

	/**
	 * Gets the code values.
	 * 
	 * @param cl
	 *            the cl
	 * @param values
	 *            the values
	 * @param active
	 *            the active
	 * @return the code values
	 */
	public abstract List<Item> getItems(Integer cl, Set<String> values,
			boolean active);

	/**
	 * Gets the code values.
	 * 
	 * @param cl
	 *            the cl
	 * @param valueCode
	 *            the value code
	 * @param active
	 *            the active
	 * @return the code values
	 */
	public abstract List<Item> getItems(Integer cl, String valueCode,
			boolean active);

	/**
	 * Gets the code values.
	 * 
	 * @param cl
	 *            the cl
	 * @param valueCode
	 *            the value code
	 * @param property
	 *            the property
	 * @return the code values
	 */
	public abstract String getItemProperty(Integer cl, String valueCode,
			CodeValuePropertiesEnum property);

	/**
	 * Gets the code values.
	 * 
	 * @param cl
	 *            the cl
	 * @param active
	 *            the active
	 * @return the code values
	 */
	public abstract List<Item> getItems(Integer cl, boolean active);

	/**
	 * Gets the all code values.
	 * 
	 * @param cl
	 *            the cl
	 * @return the all code values
	 */
	public abstract List<Item> getAllItems(Integer cl);

	/**
	 * Gets the Item by description.
	 * 
	 * @param cl
	 *            the cl
	 * @param description
	 *            the description
	 * @return the Item by description
	 */
	public abstract List<Item> getItemByDescription(Integer cl,
			String description);

	/**
	 * Gets the Item by extra2.
	 * 
	 * @param cl
	 *            the cl
	 * @param extra2
	 *            the extra2
	 * @return the Item by extra2
	 */
	public abstract List<Item> getItemByExtra2(Integer cl, String extra2);

	/**
	 * Gets the code value by property.
	 * 
	 * @param codelist
	 *            the codelist number
	 * @param property
	 *            the property
	 * @param propertyValue
	 *            the property value
	 * @param matchMode
	 *            the match mode
	 * @param active
	 *            the active
	 * @return the code value by property
	 */
	public abstract List<Item> getItemByProperty(Integer codelist,
			CodeValuePropertiesEnum property, String propertyValue,
			MatchMode matchMode, boolean active);

	/**
	 * The Enum ItemPropertiesEnum.
	 */
	public enum CodeValuePropertiesEnum implements PropertyEnum {

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

		/** The mapping. */
		private static Map<String, CodeValuePropertiesEnum> mapping = new HashMap<String, CodeValuePropertiesEnum>();
		static {
			mapping.put(CodeValueDef.VALUE, VALUE);
			mapping.put(CodeValueDef.DESCR, DESCRIPTION);
			mapping.put(CodeValueDef.EXTRA1, EXTRA1);
			mapping.put(CodeValueDef.EXTRA2, EXTRA2);
			mapping.put(CodeValueDef.EXTRA3, EXTRA3);
			mapping.put(CodeValueDef.MASTER_VALUE, MASTER_VALUE);
		}

		/**
		 * Gets the property.
		 * 
		 * @param name
		 *            the name
		 * @return the property
		 */
		public static CodeValuePropertiesEnum getProperty(String name) {
			return mapping.get(name);
		}
	}

}