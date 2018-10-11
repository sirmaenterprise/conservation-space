package com.sirma.sep.export.xlsx.components;

import java.io.Serializable;

/**
 * Representation a excel cell.
 *
 * @author Boyan Tonchev.
 */
public class ExportExcelCell {

	private String name;

	private Serializable value;

	private Type type;

	/**
	 * Initialize cell.
	 *
	 * @param name
	 * 		- property name (column name) of cell.
	 * @param value
	 * 		- the value of cell.
	 * @param type - the type of cell value.
	 */
	public ExportExcelCell(String name, Serializable value, Type type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	/**
	 * @return - the name of property (column name) for which value have to be apply.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return - the value of cell.
	 */
	public Serializable getValue() {
		return value;
	}

	/**
	 * @return return type of value. It can be:
	 * LINK it mean value is map contains link address and link label needed for construction of a link
	 * OBJECTS list with objects.
	 * OBJECT object.
	 * RICHTEXT value with html tags
	 * <p>
	 * object, entity (map with data needed for link creation)
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Represent excel cell value type. Possible values are object, list with objects, link and list with objects.
	 */
	public enum Type {
		/**
		 * Value is object.
		 */
		OBJECT,

		/**
		 * Value is list with objects.
		 */
		OBJECTS,

		/**
		 * Value is map is map contains link address and link label needed for construction of link
		 */
		LINK,

		/**
		 * Value richtext containing html tags and diferent styles
		 */
		RICHTEXT
	}
}
