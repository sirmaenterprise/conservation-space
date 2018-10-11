package com.sirma.sep.model.management;

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic class for storing different types of model attributes.
 *
 * @author Mihail Radkov
 */
public class ModelAttribute {

	private String name;

	private String type;

	private Serializable value;

	public String getName() {
		return name;
	}

	public ModelAttribute setName(String name) {
		this.name = name;
		return this;
	}

	public String getType() {
		return type;
	}

	public ModelAttribute setType(String type) {
		this.type = type;
		return this;
	}

	public Serializable getValue() {
		return value;
	}

	public ModelAttribute setValue(Serializable value) {
		this.value = value;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModelAttribute that = (ModelAttribute) o;
		return Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(value, that.value);
	}

	/**
	 * Creates new instance with the attribute data from the current instance.
	 *
	 * @return new {@link ModelAttribute} with the same data
	 */
	public ModelAttribute copy() {
		return new ModelAttribute().setName(name).setType(type).setValue(value);
	}

}
