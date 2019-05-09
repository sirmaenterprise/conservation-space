package com.sirma.sep.model.management.meta;

import java.util.Objects;

/**
 * Describes a control option param for a {@link com.sirma.sep.model.ModelNode}'s
 * {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Stella D
 */
public class ControlOptionParam {

	private String id;

	private String type;

	private String name;

	private String defaultValue = "";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ControlOptionParam)) {
			return false;
		}
		ControlOptionParam that = (ControlOptionParam) o;
		return id == that.id && type == that.type && name == that.name && defaultValue == that.defaultValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, name, defaultValue);
	}
}
