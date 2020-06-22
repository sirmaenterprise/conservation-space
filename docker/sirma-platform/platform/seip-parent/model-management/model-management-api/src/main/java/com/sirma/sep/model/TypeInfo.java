package com.sirma.sep.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Wraps basic information about a single type. Types can be either a data or an object type. Data types are represented
 * by simple primitives such as: strings, booleans, numbers etc. While Object types are represented by composite types
 * which represent a relation of some sort. Usually semantic classes.
 * 
 * @author Svetlozar Iliev
 */
public class TypeInfo {

	public static final String DATA_TYPE = "DATA_TYPE";

	public static final String OBJECT_TYPE = "OBJECT_TYPE";

	private Serializable id;

	private Map<String, String> labels;

	/**
	 * Gets the id of the type
	 * 
	 * @return the id of the type
	 */
	public Serializable getId() {
		return id;
	}

	/**
	 * Sets the id of the type
	 * 
	 * @param id
	 *            the id of the type
	 * @return reference to this object
	 */
	public TypeInfo setId(Serializable id) {
		this.id = id;
		return this;
	}

	/**
	 * Gets the label of the type
	 * 
	 * @return the label of the type
	 */
	public Map<String, String> getLabels() {
		return labels;
	}

	/**
	 * Sets the label of the type
	 * 
	 * @param label
	 *            the label of the type
	 * @return reference to this object
	 */
	public TypeInfo setLabels(Map<String, String> labels) {
		this.labels = labels;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeInfo)) {
			return false;
		}

		TypeInfo that = (TypeInfo) obj;
		return Objects.equals(id, that.id) && Objects.equals(labels, that.labels);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, labels);
	}
}
